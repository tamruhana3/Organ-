package com.netforge.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netforge.app.NetForgeApp
import com.netforge.app.ui.theme.*
import com.netforge.app.ui.util.Haptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as NetForgeApp
    val dataStore = app.dataStoreManager
    val scope = rememberCoroutineScope()

    val isDusk by dataStore.isDuskThemeFlow.collectAsStateWithLifecycle(initialValue = true)
    var autoConnect by remember { mutableStateOf(false) }
    var killSwitch by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = Typography.titleLarge, color = NetForgeChalk) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NetForgeChalk)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NetForgeInk)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("APPEARANCE", style = Typography.labelSmall, color = NetForgeSlate)

            SettingSwitchRow(
                label = "Dusk Dark Theme",
                description = "Deep OLED black canvas with violet accents (vs Dawn)",
                checked = isDusk,
                onCheckedChange = { checked ->
                    Haptics.selection(context as android.view.View)
                    scope.launch { dataStore.setDuskTheme(checked) }
                }
            )

            HorizontalDivider(color = NetForgeBorder)

            Text("CONNECTION DISCIPLINE", style = Typography.labelSmall, color = NetForgeSlate)

            SettingSwitchRow(
                label = "Auto-Connect on Boot",
                description = "Initiate default tunnel when device starts up",
                checked = autoConnect,
                onCheckedChange = { autoConnect = it }
            )

            SettingSwitchRow(
                label = "Strict Leak Prevention",
                description = "Drop all non-VPN network traffic if tunnel halts",
                checked = killSwitch,
                onCheckedChange = { killSwitch = it }
            )

            HorizontalDivider(color = NetForgeBorder)

            Text("DATA MANAGEMENT", style = Typography.labelSmall, color = NetForgeSlate)

            Button(
                onClick = {
                    scope.launch {
                        app.securePrefs.clearAll()
                        Haptics.heavy(context)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NetForgePaper2),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reset Hardware Keystore Salts", color = NetForgeRust)
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = NetForgeChalk)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, style = Typography.bodySmall, color = NetForgeSlate)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NetForgeChalk,
                checkedTrackColor = NetForgeAccent,
                uncheckedThumbColor = NetForgeSlate,
                uncheckedTrackColor = NetForgePaper
            )
        )
    }
}
