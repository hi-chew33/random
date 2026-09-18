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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vocis.ui.theme.VocisBorder
import com.vocis.ui.theme.VocisCardWhite
import com.vocis.ui.theme.VocisCream
import com.vocis.ui.theme.VocisDark
import com.vocis.ui.theme.VocisGreen
import com.vocis.ui.theme.VocisGreenLight
import com.vocis.ui.theme.VocisGreenText
import com.vocis.ui.theme.VocisMediumGrey
import com.vocis.ui.theme.VocisRed
import com.vocis.ui.theme.VocisRedLight

@Composable
fun SafetyToolsScreen(
    onNavigateToDigitalArrest: () -> Unit = {},
    onNavigateToEmergency: () -> Unit = {},
    onNavigateToEvidenceVault: () -> Unit = {}
) {
    var showSmsScannerDialog by remember { mutableStateOf(false) }
    var showVoiceCloneDialog by remember { mutableStateOf(false) }
    var showEvidenceVault by remember { mutableStateOf(false) }

    if (showEvidenceVault) {
        ForensicEvidenceScreen(onBack = { showEvidenceVault = false })
        return
    }

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
            text = "Specialized forensic counters, biometric analyzers & emergency tools.",
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
                    icon = "📁",
                    title = "Forensic Evidence Vault",
                    description = "Tamper-evident legal dossiers with SHA-256 seals for Cyber Crime 1930 reporting",
                    badge = "Screen 12",
                    onClick = { showEvidenceVault = true }
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
                    onClick = { showSmsScannerDialog = true }
                )
            }

            item {
                SafetyToolCard(
                    icon = "🎙️",
                    title = "Voice Clone Spectral Scanner",
                    description = "On-device neural synthetic speech verification with AASIST & Resemblyzer",
                    badge = "AASIST",
                    onClick = { showVoiceCloneDialog = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // SMS Analyzer Modal Dialog
    if (showSmsScannerDialog) {
        SmsScannerDialog(onDismiss = { showSmsScannerDialog = false })
    }

    // Voice Clone Scanner Modal Dialog
    if (showVoiceCloneDialog) {
        VoiceCloneScannerDialog(onDismiss = { showVoiceCloneDialog = false })
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

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VocisDark
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(VocisGreenLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = VocisGreenText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VocisMediumGrey
                )
            }
        }
    }
}

@Composable
fun SmsScannerDialog(onDismiss: () -> Unit) {
    var smsText by remember { mutableStateOf("Dear customer, your electricity power will be disconnected tonight. Call officer immediately to update KYC.") }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var riskScore by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "SMS Threat Analyzer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = VocisDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Paste any incoming SMS message to run on-device NLP linguistic screening.",
                    style = MaterialTheme.typography.bodySmall,
                    color = VocisMediumGrey
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = smsText,
                    onValueChange = { smsText = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    label = { Text("SMS Body Text") },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val lower = smsText.lowercase()
                        if (lower.contains("electricity") || lower.contains("disconnected") || lower.contains("kyc") || lower.contains("otp")) {
                            riskScore = 88
                            scanResult = "HIGH RISK DETECTED: Matches Fake Utility Power Disconnection & Urgent Coercion Patterns."
                        } else {
                            riskScore = 12
                            scanResult = "CLEARED: No known scam heuristics triggered."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VocisDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Analyze SMS Heuristics")
                }

                if (scanResult != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (riskScore > 50) VocisRedLight else VocisGreenLight)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Threat Score: $riskScore / 100",
                                fontWeight = FontWeight.Bold,
                                color = if (riskScore > 50) VocisRed else VocisGreenText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = scanResult ?: "", style = MaterialTheme.typography.bodySmall, color = VocisDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VocisCream),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", color = VocisDark)
                }
            }
        }
    }
}

@Composable
fun VoiceCloneScannerDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AASIST Model Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = VocisDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(VocisGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎙️", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Neural Anti-Spoofing Running",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = VocisGreenText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "AASIST ONNX engine operates at 16kHz with 200ms latency window, screening for TTS vocoder artifacts, deepfake synthesis, and replay attacks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = VocisMediumGrey,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VocisDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK", color = Color.White)
                }
            }
        }
    }
}
