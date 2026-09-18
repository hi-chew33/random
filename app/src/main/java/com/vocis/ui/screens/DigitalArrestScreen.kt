package com.vocis.ui.screens

import android.content.Context
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocis.digitalarrest.DigitalArrestController
import com.vocis.digitalarrest.DigitalArrestPhase
import com.vocis.ui.theme.VocisAmber
import com.vocis.ui.theme.VocisAmberLight
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
fun DigitalArrestScreen(
    controller: DigitalArrestController,
    onBack: () -> Unit = {}
) {
    val state by controller.state.collectAsState()
    val context = LocalContext.current
    val progress = (state.phaseIndex.toFloat() / 10f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VocisCream)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Digital Arrest Defense",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = VocisDark
                )
                Text(
                    text = "Step ${state.phaseIndex} of 10: ${state.currentPhase.name.replace('_', ' ')}",
                    style = MaterialTheme.typography.labelMedium,
                    color = VocisMediumGrey
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(VocisRedLight)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "DEFENSE ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = VocisRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step Progress Bar
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = VocisGreen,
            trackColor = VocisBorder
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Advisory Card matching Figma
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, VocisAmber, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = VocisAmberLight),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Legal Fact Check",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VocisDark
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Under Indian Law, the CBI, Police, or ED NEVER conduct arrests, trials, or summons via Skype, WhatsApp, or video calls. Digital arrest does not legally exist.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VocisDark
                        )
                    }
                }
            }

            // Phase Details Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, VocisBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = state.currentPhase.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VocisDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.currentPhase.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = VocisMediumGrey
                        )
                    }
                }
            }

            // Evidence & Rules Evaluation Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, VocisBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Forensic Rule Evaluation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VocisDark
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val report = state.ruleReport
                        if (report != null) {
                            for (rule in report.rules) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${rule.ruleId}: ${rule.ruleName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                        color = VocisDark
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (rule.isViolated) VocisRedLight else VocisGreenLight)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (rule.isViolated) "VIOLATED" else "CLEAR",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (rule.isViolated) VocisRed else VocisGreenText
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Evaluating incoming claims...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VocisMediumGrey
                            )
                        }
                    }
                }
            }

            // Cryptographic Evidence Seal (if generated)
            if (state.evidenceSeal != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, VocisGreen, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = VocisGreenLight),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🔒 Cryptographic Evidence Seal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VocisGreenText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.evidenceSeal ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = VocisDark,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Court-admissible SHA-256 sealed PDF saved on device storage.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VocisGreenText
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.phaseIndex > 1) {
                OutlinedButton(
                    onClick = { controller.previousPhase() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Previous")
                }
            }

            if (state.phaseIndex < 10) {
                Button(
                    onClick = {
                        if (state.currentPhase == DigitalArrestPhase.FORENSIC_REPORT_GENERATION) {
                            controller.generateForensicReport(context)
                        }
                        controller.nextPhase()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VocisDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (state.currentPhase == DigitalArrestPhase.FORENSIC_REPORT_GENERATION) "Seal & Next" else "Next Step",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        controller.generateForensicReport(context)
                        onBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VocisGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Complete Defense (1930)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
