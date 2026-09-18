package com.vocis.intelligence.identity

import android.content.Context
import android.provider.ContactsContract
import com.vocis.core.data.dao.CallerIdentityDao
import com.vocis.core.data.entity.CallerIdentityEntity

// ── Domain model returned to callers ─────────────────────────────────────────

enum class ReputationLevel { TRUSTED, SAFE, NEUTRAL, UNKNOWN, SUSPICIOUS, HIGH_RISK }

data class CallerIdentity(
    val e164Number: String,
    val displayName: String?,
    val reputationLevel: ReputationLevel,
    val isKnownContact: Boolean,
    val source: String,               // "CONTACT" | "CACHE" | "PROVIDER" | "UNKNOWN"
    val spamReports: Int = 0,
    val fraudReports: Int = 0
) {
    val phoneNumber: String get() = e164Number

    constructor(
        phoneNumber: String,
        displayName: String?,
        reputationLevel: ReputationLevel,
        isKnownContact: Boolean,
        source: String,
        spamReports: Int = 0,
        fraudReports: Int = 0
    ) : this(
        e164Number = phoneNumber,
        displayName = displayName,
        reputationLevel = reputationLevel,
        isKnownContact = isKnownContact,
        source = source,
        spamReports = spamReports,
        fraudReports = fraudReports
    )
}

// ── Risk contribution from identity (used by EvidenceFusionEngine) ─────────

fun CallerIdentity.baseRiskWeight(): Int = when {
    isKnownContact -> 0
    e164Number == "UNKNOWN" -> 25            // anonymous / private
    reputationLevel == ReputationLevel.SUSPICIOUS -> 50
    reputationLevel == ReputationLevel.HIGH_RISK -> 80
    reputationLevel == ReputationLevel.UNKNOWN -> 15
    else -> 0
}

// ── Provider interface (pluggable — real API unknown) ─────────────────────────

interface CallerReputationProvider {
    suspend fun lookup(e164: String): CallerIdentity
}

// ponytail: mock impl, upgrade to real REST adapter when commercial API contract confirmed
object MockReputationProvider : CallerReputationProvider {
    override suspend fun lookup(e164: String) = CallerIdentity(
        e164Number = e164,
        displayName = null,
        reputationLevel = ReputationLevel.UNKNOWN,
        isKnownContact = false,
        source = "PROVIDER"
    )
}

// ── E.164 normalizer ──────────────────────────────────────────────────────────

object E164Normalizer {
    fun normalize(raw: String?): String {
        if (raw.isNullOrBlank()) return "UNKNOWN"
        val stripped = raw.removePrefix("tel:").trim()
            .filter { it.isDigit() || it == '+' }
        if (stripped.isEmpty()) return "UNKNOWN"
        if (stripped.startsWith("+")) return stripped
        // 10-digit bare Indian number → prepend +91
        if (stripped.length == 10 && stripped.first().isDigit()) return "+91$stripped"
        return stripped
    }
}

// ── Main resolver ─────────────────────────────────────────────────────────────

private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L   // 24 h

class CallerIdentityResolver(
    private val context: Context,
    private val dao: CallerIdentityDao,
    private val provider: CallerReputationProvider = MockReputationProvider
) {

    suspend fun resolve(rawNumber: String?): CallerIdentity {
        val e164 = E164Normalizer.normalize(rawNumber)
        if (e164 == "UNKNOWN") return unknownIdentity()

        // 1. ContactsProvider — fastest path
        val contactName = queryContacts(e164)
        if (contactName != null) {
            val identity = CallerIdentity(
                e164Number = e164,
                displayName = contactName,
                reputationLevel = ReputationLevel.TRUSTED,
                isKnownContact = true,
                source = "CONTACT"
            )
            cacheIdentity(identity)
            return identity
        }

        // 2. Local cache
        val cached = dao.getByPhoneNumber(e164)
        if (cached != null && System.currentTimeMillis() - cached.lastUpdated < CACHE_TTL_MS) {
            return cached.toDomain()
        }

        // 3. External provider (1000 ms budget enforced by caller via withTimeout)
        val identity = provider.lookup(e164)
        cacheIdentity(identity)
        return identity
    }

    private fun queryContacts(e164: String): String? {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
            .appendPath(e164).build()
        return context.contentResolver.query(
            uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst())
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
            else null
        }
    }

    private suspend fun cacheIdentity(identity: CallerIdentity) {
        dao.insert(CallerIdentityEntity(
            phoneNumber = identity.e164Number,
            displayName = identity.displayName,
            category = identity.reputationLevel.name,
            reputationLevel = identity.reputationLevel.name,
            spamReports = identity.spamReports,
            fraudReports = identity.fraudReports,
            isVerified = identity.isKnownContact,
            lastUpdated = System.currentTimeMillis()
        ))
    }

    private fun unknownIdentity() = CallerIdentity(
        e164Number = "UNKNOWN",
        displayName = null,
        reputationLevel = ReputationLevel.UNKNOWN,
        isKnownContact = false,
        source = "UNKNOWN"
    )
}

private fun CallerIdentityEntity.toDomain() = CallerIdentity(
    e164Number = phoneNumber,
    displayName = displayName,
    reputationLevel = runCatching { ReputationLevel.valueOf(reputationLevel) }
        .getOrDefault(ReputationLevel.UNKNOWN),
    isKnownContact = isVerified,
    source = "CACHE",
    spamReports = spamReports,
    fraudReports = fraudReports
)
