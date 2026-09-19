package com.netforge.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
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
import com.netforge.app.ui.theme.*
import com.netforge.app.ui.util.Haptics

val PLACEHOLDERS = listOf(
    "[host_port]",
    "[front_host]",
    "[real_host]",
    "[host]",
    "[port]",
    "[ssh_user]",
    "[ssh_pass]",
    "[protocol]",
    "[nonce]",
    "[ua]",
    "[crlf]",
    "[lf]",
    "[cr]"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayloadEditorScreen(
    viewModel: PayloadEditorViewModel,
    onNavigateBack: () -> Unit
) {
    val template by viewModel.template.collectAsStateWithLifecycle()
    val rendered by viewModel.renderedOutput.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Payload Template Editor",
                        style = Typography.titleMedium,
                        color = NetForgeChalk
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NetForgeChalk
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Haptics.success(context)
                        viewModel.save()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = NetForgeAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NetForgeInk)
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Presets row
            Text("QUICK PRESETS", style = Typography.labelSmall, color = NetForgeSlate)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetChip("WebSocket Upgrade") {
                    viewModel.applyPreset("CONNECT [host_port] HTTP/1.1[crlf]Host: [front_host][crlf]User-Agent: [ua][crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]")
                }
                PresetChip("HTTP CONNECT") {
                    viewModel.applyPreset("CONNECT [host_port] HTTP/1.1[crlf]Host: [host][crlf]User-Agent: [ua][crlf]Proxy-Connection: Keep-Alive[crlf][crlf]")
                }
                PresetChip("GET Fronting") {
                    viewModel.applyPreset("GET / HTTP/1.1[crlf]Host: [front_host][crlf]User-Agent: [ua][crlf]X-Forwarded-For: [real_host][crlf]Connection: Upgrade[crlf][crlf]")
                }
            }

            // Placeholder chips
            Text("TAP TO INSERT PLACEHOLDER", style = Typography.labelSmall, color = NetForgeSlate)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (ph in PLACEHOLDERS) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NetForgePaper2)
                            .border(1.dp, NetForgeAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                Haptics.light(context)
                                viewModel.insertPlaceholder(ph)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = ph,
                            style = MonoTextStyle.copy(fontSize = 11.sp),
                            color = NetForgeAccent
                        )
                    }
                }
            }

            // Template Input Box
            Text("TEMPLATE PATTERN", style = Typography.labelSmall, color = NetForgeSlate)
            OutlinedTextField(
                value = template,
                onValueChange = { viewModel.onTemplateChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                textStyle = MonoTextStyle.copy(fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NetForgeAccent,
                    unfocusedBorderColor = NetForgeBorder,
                    focusedContainerColor = NetForgePaper,
                    unfocusedContainerColor = NetForgePaper,
                    focusedTextColor = NetForgeChalk,
                    unfocusedTextColor = NetForgeChalk
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Real-time Preview Box
            Text("LIVE RENDER PREVIEW", style = Typography.labelSmall, color = NetForgeMoss)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = rendered.ifEmpty { "(Empty payload template)" },
                    style = MonoTextStyle.copy(fontSize = 12.sp),
                    color = NetForgeChalk
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NetForgePaper)
            .border(1.dp, NetForgeBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = NetForgeChalk
        )
    }
}
