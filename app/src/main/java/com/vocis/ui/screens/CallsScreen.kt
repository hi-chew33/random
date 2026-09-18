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
import com.vocis.ui.theme.VocisDark
import com.vocis.ui.theme.VocisDarkGrey
import com.vocis.ui.theme.VocisGreenLight
import com.vocis.ui.theme.VocisGreenText
import com.vocis.ui.theme.VocisLightGrey
import com.vocis.ui.theme.VocisMediumGrey
import com.vocis.ui.theme.VocisRed
import com.vocis.ui.theme.VocisRedLight

@Composable
fun CallsScreen(
    interactionHub: InteractionHub? = null,
    onSelectCall: (InteractionEntity) -> Unit = {}
) {
    val interactions by (interactionHub?.interactions?.collectAsState() ?: androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(emptyList<InteractionEntity>())
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VocisCream)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header matching Figma "Calls" title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calls Log",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = VocisDark
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(VocisCardWhite)
                    .border(1.dp, VocisBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${interactions.size} calls",
                    style = MaterialTheme.typography.labelSmall,
                    color = VocisMediumGrey
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (interactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📞", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No call records yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = VocisDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Incoming calls screened by VOCIS will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VocisMediumGrey
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(interactions) { call ->
                    CallHistoryCard(call = call, onClick = { onSelectCall(call) })
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun CallHistoryCard(
    call: InteractionEntity,
    onClick: () -> Unit
) {
    val (pillBg, pillTextColor) = when (call.riskLevel) {
        RiskLevel.LOW -> VocisGreenLight to VocisGreenText
        RiskLevel.ELEVATED -> VocisBlueLight to VocisBlue
        RiskLevel.HIGH -> VocisAmberLight to VocisAmber
        RiskLevel.CRITICAL -> VocisRedLight to VocisRed
    }

    val iconText = when {
        call.isBlocked -> "🚫"
        call.riskLevel >= RiskLevel.HIGH -> "⚠️"
        else -> "📞"
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(VocisCream),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconText, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (call.callerDisplayName.isNotBlank()) call.callerDisplayName else call.callerPhoneNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = VocisDark
                )
                if (call.callerDisplayName.isNotBlank() && call.callerPhoneNumber.isNotBlank()) {
                    Text(
                        text = call.callerPhoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VocisMediumGrey
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = call.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = VocisLightGrey
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(pillBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = call.riskLevel.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = pillTextColor
                )
            }
        }
    }
}
