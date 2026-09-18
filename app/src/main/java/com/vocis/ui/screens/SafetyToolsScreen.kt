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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocis.ui.theme.VocisBorder
import com.vocis.ui.theme.VocisCardWhite
import com.vocis.ui.theme.VocisCream
import com.vocis.ui.theme.VocisDark
import com.vocis.ui.theme.VocisMediumGrey

@Composable
fun SafetyToolsScreen(
    onNavigateToDigitalArrest: () -> Unit = {},
    onNavigateToEmergency: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VocisCream)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Safety Tools",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = VocisDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Specialized forensic counters and emergency response tools.",
            style = MaterialTheme.typography.bodyMedium,
            color = VocisMediumGrey
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                SafetyToolCard(
                    icon = "⚖️",
                    title = "Digital Arrest Defense",
                    description = "10-phase guided recovery, institutional scam detection & court-ready sealed PDF",
                    badge = "Phase 15",
                    onClick = onNavigateToDigitalArrest
                )
            }

            item {
                SafetyToolCard(
                    icon = "🚨",
                    title = "Emergency Family SOS",
                    description = "Instant 85% siren override, keyguard-bypassing alert & TextBee cloud SMS dispatch",
                    badge = "Phase 14",
                    onClick = onNavigateToEmergency
                )
            }

            item {
                SafetyToolCard(
                    icon = "💬",
                    title = "SMS Scam Pattern Analyzer",
                    description = "60+ regex heuristic rules screening banking OTP theft and fake utility threats",
                    badge = "Linguistic AI",
                    onClick = {}
                )
            }

            item {
                SafetyToolCard(
                    icon = "🎙️",
                    title = "Voice Clone Spectral Scanner",
                    description = "On-device neural synthetic speech verification with AASIST & Resemblyzer",
                    badge = "AASIST",
                    onClick = {}
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SafetyToolCard(
    icon: String,
    title: String,
    description: String,
    badge: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VocisBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(VocisCream),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VocisDark
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VocisMediumGrey,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = "›", fontSize = 24.sp, color = VocisMediumGrey, fontWeight = FontWeight.Bold)
        }
    }
}
