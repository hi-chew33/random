package com.vocis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocis.ui.theme.VocisBorder
import com.vocis.ui.theme.VocisCardWhite
import com.vocis.ui.theme.VocisCream
import com.vocis.ui.theme.VocisDark
import com.vocis.ui.theme.VocisGreen
import com.vocis.ui.theme.VocisGreenLight
import com.vocis.ui.theme.VocisGreenText
import com.vocis.ui.theme.VocisMediumGrey

data class VoiceProfileItem(
    val name: String,
    val relationship: String,
    val phone: String,
    val isEnrolled: Boolean,
    val enrolledDate: String
)

@Composable
fun VoicesScreen(
    onEnrollNewProfile: () -> Unit = {}
) {
    val sampleProfiles = listOf(
        VoiceProfileItem("My Primary Voice", "Device Owner", "Self", true, "Enrolled • AES-256 Vault"),
        VoiceProfileItem("Mom", "Emergency Contact", "+91 98888 77771", true, "Enrolled • 3 Utterances"),
        VoiceProfileItem("Brother", "Family Contact", "+91 98888 77772", true, "Enrolled • Verified")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VocisCream)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Voice Defense",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = VocisDark
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(VocisGreenLight)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "AASIST Neural Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = VocisGreenText,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleProfiles.size) { index ->
                val profile = sampleProfiles[index]
                VoiceProfileCard(profile)
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Button(
            onClick = onEnrollNewProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VocisDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "+ Enroll New Voice Profile",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun VoiceProfileCard(profile: VoiceProfileItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VocisBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(VocisGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.name.take(1),
                    style = MaterialTheme.typography.titleLarge,
                    color = VocisGreenText,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VocisDark
                )
                Text(
                    text = "${profile.relationship} • ${profile.phone}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VocisMediumGrey
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profile.enrolledDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = VocisGreen
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(VocisGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", color = VocisGreenText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
