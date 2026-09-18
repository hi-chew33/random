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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.vocis.core.domain.model.ProtectionMode
import com.vocis.ui.theme.VocisBorder
import com.vocis.ui.theme.VocisCardWhite
import com.vocis.ui.theme.VocisCream
import com.vocis.ui.theme.VocisDark
import com.vocis.ui.theme.VocisGreen
import com.vocis.ui.theme.VocisGreenText
import com.vocis.ui.theme.VocisMediumGrey

@Composable
fun SettingsScreen() {
    var selectedMode by remember { mutableStateOf(ProtectionMode.BALANCED) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VocisCream)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = VocisDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text(
                    text = "Protection Policy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VocisDark
                )
            }

            item {
                ProtectionModeOption(
                    mode = ProtectionMode.BALANCED,
                    title = "Balanced Mode (Recommended)",
                    description = "Screen unknown callers, auto-block critical threats (Score > 75), alert on suspicious speech.",
                    selected = selectedMode == ProtectionMode.BALANCED,
                    onSelect = { selectedMode = ProtectionMode.BALANCED }
                )
            }

            item {
                ProtectionModeOption(
                    mode = ProtectionMode.AGGRESSIVE,
                    title = "Strict High-Security Mode",
                    description = "Immediately reject all high-risk numbers (>50), enforce voiceprint verification on contacts.",
                    selected = selectedMode == ProtectionMode.AGGRESSIVE,
                    onSelect = { selectedMode = ProtectionMode.AGGRESSIVE }
                )
            }

            item {
                ProtectionModeOption(
                    mode = ProtectionMode.PASSIVE,
                    title = "Monitor Only Mode",
                    description = "Log threat scores without blocking active calls or modifying audio stream.",
                    selected = selectedMode == ProtectionMode.PASSIVE,
                    onSelect = { selectedMode = ProtectionMode.PASSIVE }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Emergency Response",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VocisDark
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, VocisBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Emergency Keyword",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = VocisDark
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VocisCream)
                                    .border(1.dp, VocisBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "VOCIS",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = VocisGreenText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Inbound SMS containing this keyword from authorized contacts instantly activates the emergency alarm loop.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VocisMediumGrey
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, VocisBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "About VOCIS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = VocisDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "VOCIS Voice Security Platform v1.0.0\nRole D Clean-Room Integration Build",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VocisMediumGrey
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProtectionModeOption(
    mode: ProtectionMode,
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (selected) VocisGreen else VocisBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = VocisCardWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = VocisGreen,
                    unselectedColor = VocisMediumGrey
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = VocisDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VocisMediumGrey,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
