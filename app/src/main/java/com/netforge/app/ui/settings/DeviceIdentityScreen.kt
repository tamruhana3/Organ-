package com.netforge.app.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.domain.util.DeviceIdentity
import com.netforge.app.ui.theme.*
import com.netforge.app.ui.util.Haptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceIdentityScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val deviceId = remember { DeviceIdentity.getDeviceId(context) }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = { Text("Device Identity", style = Typography.titleLarge, color = NetForgeChalk) },
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = NetForgeAccent, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Hardware Device Fingerprint",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NetForgeChalk
                        )
                    }

                    Text(
                        text = "This unique ID is derived from hardware attributes. Tunnel creators use this token to lock .nfg profiles exclusively to your handset.",
                        style = Typography.bodySmall,
                        color = NetForgeSlate
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NetForgeInk)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = deviceId,
                            style = MonoTextStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = NetForgeMoss
                        )
                    }

                    Button(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("NetForge Device ID", deviceId))
                            Toast.makeText(context, "Device ID copied to clipboard", Toast.LENGTH_SHORT).show()
                            Haptics.light(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NetForgeAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Device ID Token")
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DEVICE PLATFORM DETAILS", style = Typography.labelSmall, color = NetForgeSlate)
                    Text("Model: ${Build.MANUFACTURER} ${Build.MODEL}", style = Typography.bodyMedium, color = NetForgeChalk)
                    Text("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", style = Typography.bodyMedium, color = NetForgeChalk)
                    Text("Architecture: ${Build.SUPPORTED_ABIS.joinToString(", ")}", style = Typography.bodyMedium, color = NetForgeChalk)
                }
            }
        }
    }
}
