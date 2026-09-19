package com.netforge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.netforge.app.ui.theme.*

@Composable
fun VaultMenu(
    onNavigateSettings: () -> Unit,
    onNavigateDeviceIdentity: () -> Unit,
    onNavigateShellAccess: () -> Unit,
    onNavigateSlowChannel: () -> Unit,
    onNavigateImport: () -> Unit,
    onNavigateBench: () -> Unit,
    onNavigateAbout: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Vault Menu",
                tint = NetForgeChalk
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(NetForgePaper2)
        ) {
            DropdownMenuItem(
                text = { Text("Settings", color = NetForgeChalk) },
                onClick = { expanded = false; onNavigateSettings() }
            )
            DropdownMenuItem(
                text = { Text("Device Identity", color = NetForgeChalk) },
                onClick = { expanded = false; onNavigateDeviceIdentity() }
            )
            DropdownMenuItem(
                text = { Text("Shell Access", color = NetForgeChalk) },
                onClick = { expanded = false; onNavigateShellAccess() }
            )
            DropdownMenuItem(
                text = { Text("Slow Channel (DNS)", color = NetForgeChalk) },
                onClick = { expanded = false; onNavigateSlowChannel() }
            )
            DropdownMenuItem(
                text = { Text("Import .nfg Profile", color = NetForgeChalk) },
                onClick = { expanded = false; onNavigateImport() }
            )
            DropdownMenuItem(
                text = { Text("Diagnostics (Bench)", color = NetForgeChalk) },
                onClick = { expanded = false; onNavigateBench() }
            )
            DropdownMenuItem(
                text = { Text("About NetForge", color = NetForgeChalk) },
                onClick = { expanded = false; onNavigateAbout() }
            )
            HorizontalDivider(color = NetForgeBorder)
            DropdownMenuItem(
                text = { Text("Quit Session", color = NetForgeRust) },
                onClick = { expanded = false; onQuit() }
            )
        }
    }
}
