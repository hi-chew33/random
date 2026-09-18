package com.vocis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocis.core.data.entity.InteractionEntity
import com.vocis.core.domain.model.RiskLevel
import com.vocis.intelligence.hub.InteractionHub
import com.vocis.ui.theme.VocisAmber
import com.vocis.ui.theme.VocisAmberLight
import com.vocis.ui.theme.VocisBlue
import com.vocis.ui.theme.VocisBlueLight
import com.vocis.ui.theme.VocisBorder
import com.vocis.ui.theme.VocisCardWhite
import com.vocis.ui.theme.VocisCream
import com.vocis.ui.theme.VocisCreamDarker
import com.vocis.ui.theme.VocisDark
import com.vocis.ui.theme.VocisDarkGrey
import com.vocis.ui.theme.VocisGreen
import com.vocis.ui.theme.VocisGreenDark
import com.vocis.ui.theme.VocisGreenLight
import com.vocis.ui.theme.VocisGreenText
import com.vocis.ui.theme.VocisLightGrey
import com.vocis.ui.theme.VocisMediumGrey
import com.vocis.ui.theme.VocisRed
import com.vocis.ui.theme.VocisRedLight

@Composable
fun DashboardScreen(
    interactionHub: InteractionHub? = null,
    onNavigateToDigitalArrest: () -> Unit = {},
    onNavigateToEmergency: () -> Unit = {},
    onSelectInteraction: (InteractionEntity) -> Unit = {}
) {
    val interactions by (interactionHub?.interactions?.collectAsState() ?: rememberMockInteractionsState())
    val activeIncident by (interactionHub?.activeIncident?.collectAsState() ?: rememberMockIncidentState())

    val totalScanned = interactions.size
    val totalThreats = interactions.count { it.riskLevel >= RiskLevel.HIGH || it.isBlocked }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VocisCream)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // ── Figma Hero Card: Protection Active Banner ─────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = VocisGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "VOCIS SHIELD",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Protection Active",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Status pill badge with checkmark
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✓", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Real-time call screening, voice clone neural defense, and SMS telemetry sensors running locally on device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // ── Quick Stats Grid ──────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Calls Scanned",
                    count = totalScanned.toString(),
                    subtitle = "All safe",
                    modifier = Modifier.weight(1f),
                    badgeColor = VocisGreenLight,
                    badgeTextColor = VocisGreenText
                )

                StatCard(
                    title = "Threats Blocked",
                    count = totalThreats.toString(),
                    subtitle = if (totalThreats > 0) "Action taken" else "Zero threats",
                    modifier = Modifier.weight(1f),
                    badgeColor = if (totalThreats > 0) VocisRedLight else VocisGreenLight,
                    badgeTextColor = if (totalThreats > 0) VocisRed else VocisGreenText
                )
            }
        }

        // ── Active Incident Banner (if any) ───────────────────────────────────
        if (activeIncident != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, VocisAmber, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToDigitalArrest() },
                    colors = CardDefaults.cardColors(containerColor = VocisAmberLight),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚠️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Threat: ${activeIncident?.incidentType?.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VocisDark
                            )
                            Text(
                                text = activeIncident?.explanation ?: "Suspicious activity detected.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VocisDarkGrey
                            )
                        }
                    }
                }
            }
        }

        // ── Emergency Quick Launch Row ────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, VocisBorder, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToDigitalArrest() },
                    colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "⚖️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Digital Arrest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VocisDark
                        )
                        Text(
                            text = "10-Phase Guide",
                            style = MaterialTheme.typography.labelSmall,
                            color = VocisMediumGrey
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, VocisBorder, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToEmergency() },
                    colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "🚨", fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Emergency SOS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VocisRed
                        )
                        Text(
                            text = "Family & Alarm",
                            style = MaterialTheme.typography.labelSmall,
                            color = VocisMediumGrey
                        )
                    }
                }
            }
        }

        // ── Recent Activity Section Header ────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    color = VocisDark,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${interactions.size} total",
                    style = MaterialTheme.typography.labelMedium,
                    color = VocisMediumGrey
                )
            }
        }

        // ── Activity Stream Items ─────────────────────────────────────────────
        if (interactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, VocisBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = VocisCardWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🛡️", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All communications safe",
                            style = MaterialTheme.typography.titleMedium,
                            color = VocisDark
                        )
                        Text(
                            text = "Incoming calls and SMS will be screened and logged here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VocisMediumGrey,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(interactions) { interaction ->
                InteractionListItem(
                    interaction = interaction,
                    onClick = { onSelectInteraction(interaction) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badgeColor: Color,
    badgeTextColor: Color
) {
    Card(
        modifier = modifier.border(1.dp, VocisBorder, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = VocisMediumGrey
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = count,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = VocisDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeTextColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun InteractionListItem(
    interaction: InteractionEntity,
    onClick: () -> Unit
) {
    val (badgeBg, badgeColor) = when (interaction.riskLevel) {
        RiskLevel.LOW -> VocisGreenLight to VocisGreenText
        RiskLevel.ELEVATED -> VocisBlueLight to VocisBlue
        RiskLevel.HIGH -> VocisAmberLight to VocisAmber
        RiskLevel.CRITICAL -> VocisRedLight to VocisRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VocisBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (interaction.callerDisplayName.isNotBlank()) interaction.callerDisplayName else interaction.callerPhoneNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VocisDark
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = interaction.summary.ifBlank { "Telephony interaction verified" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = VocisMediumGrey,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = interaction.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = VocisLightGrey
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeBg)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = interaction.riskLevel.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
        }
    }
}

@Composable
private fun rememberMockInteractionsState() = androidx.compose.runtime.remember {
    androidx.compose.runtime.mutableStateOf(emptyList<InteractionEntity>())
}

@Composable
private fun rememberMockIncidentState() = androidx.compose.runtime.remember {
    androidx.compose.runtime.mutableStateOf<com.vocis.core.data.entity.SecurityIncidentEntity?>(null)
}
