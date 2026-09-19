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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = { Text("About NetForge", style = Typography.titleLarge, color = NetForgeChalk) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NetForgeAccent.copy(alpha = 0.15f))
                    .border(2.dp, NetForgeAccent, RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = "NF",
                    style = Typography.displayLarge.copy(color = NetForgeAccent, fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = "NetForge",
                style = Typography.displayLarge,
                color = NetForgeChalk
            )

            Text(
                text = "Private routing, plainly done",
                style = Typography.bodyLarge,
                color = NetForgeAccent
            )

            Text(
                text = "Version 1.0  •  Crafted by Axiom Collective",
                style = Typography.labelSmall,
                color = NetForgeSlate
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "CLEAN-ROOM ARCHITECTURE",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NetForgeMoss
                    )

                    Text(
                        text = "NetForge is built strictly from scratch according to open IETF Internet standards without borrowing code from existing injector or tunnel tools. Traffic routing is performed natively via Android VpnService, SSH-2.0 RFC 4253, and a local high-performance forwarder.",
                        style = Typography.bodySmall,
                        color = NetForgeSlate,
                        lineHeight = 20.sp
                    )

                    HorizontalDivider(color = NetForgeBorder)

                    Text(
                        text = "CRYPTOGRAPHIC SPECIFICATION",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NetForgeAccent
                    )

                    Text(
                        text = "• KDF: Argon2id (t=3, m=64MB, p=4)\n• Cipher: AES-256-GCM (12-byte IV, 16-byte tag)\n• Hardware KeyStore: AndroidKeyStore AES-GCM salt seals\n• Verification: HMAC-SHA256 tamper guards\n• Telemetry: 100% Zero tracking or analytics",
                        style = MonoTextStyle.copy(fontSize = 11.sp),
                        color = NetForgeChalk
                    )
                }
            }
        }
    }
}
