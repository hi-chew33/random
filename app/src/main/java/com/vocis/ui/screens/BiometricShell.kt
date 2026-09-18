package com.vocis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocis.core.data.entity.InteractionEntity
import com.vocis.digitalarrest.DigitalArrestController
import com.vocis.intelligence.hub.InteractionHub
import com.vocis.ui.theme.VocisBorder
import com.vocis.ui.theme.VocisCardWhite
import com.vocis.ui.theme.VocisCream
import com.vocis.ui.theme.VocisDark
import com.vocis.ui.theme.VocisGreen
import com.vocis.ui.theme.VocisMediumGrey

sealed class VocisTab(val index: Int, val label: String, val icon: String) {
    object Dashboard : VocisTab(0, "Shield", "🛡️")
    object Calls : VocisTab(1, "Calls", "📞")
    object Voices : VocisTab(2, "Voices", "🎙️")
    object Tools : VocisTab(3, "Tools", "🔧")
    object Settings : VocisTab(4, "Settings", "⚙️")
}

@Composable
fun BiometricShell(
    interactionHub: InteractionHub? = null,
    digitalArrestController: DigitalArrestController? = null,
    onNavigateToDigitalArrest: () -> Unit = {},
    onNavigateToEmergency: () -> Unit = {},
    onSelectCallDetail: (InteractionEntity) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = VocisCream,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .border(1.dp, VocisBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                containerColor = VocisCardWhite,
                tonalElevation = 0.dp
            ) {
                val tabs = listOf(
                    VocisTab.Dashboard,
                    VocisTab.Calls,
                    VocisTab.Voices,
                    VocisTab.Tools,
                    VocisTab.Settings
                )

                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab.index },
                        icon = {
                            Text(
                                text = tab.icon,
                                fontSize = if (isSelected) 22.sp else 18.sp
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) VocisDark else VocisMediumGrey
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = VocisCream,
                            selectedIconColor = VocisDark,
                            unselectedIconColor = VocisMediumGrey
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    interactionHub = interactionHub,
                    onNavigateToDigitalArrest = onNavigateToDigitalArrest,
                    onNavigateToEmergency = onNavigateToEmergency,
                    onSelectInteraction = onSelectCallDetail
                )
                1 -> CallsScreen(
                    interactionHub = interactionHub,
                    onSelectCall = onSelectCallDetail
                )
                2 -> VoicesScreen(
                    onEnrollNewProfile = {}
                )
                3 -> SafetyToolsScreen(
                    onNavigateToDigitalArrest = onNavigateToDigitalArrest,
                    onNavigateToEmergency = onNavigateToEmergency
                )
                4 -> SettingsScreen()
            }
        }
    }
}
