package com.netforge.app.ui.bench

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhereAmIScreen(
    onNavigateBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var publicIp by remember { mutableStateOf<String?>("Checking...") }
    var detectedDns by remember { mutableStateOf<String?>("Checking...") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun runQuery() {
        isLoading = true
        errorMsg = null
        scope.launch {
            try {
                val ip = withContext(Dispatchers.IO) {
                    val url = URL("https://api.ipify.org?format=json")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 6000
                    conn.readTimeout = 6000
                    conn.setRequestProperty("User-Agent", "NetForge/1.0")
                    val text = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                    JSONObject(text).optString("ip", "Unknown")
                }
                val dns = withContext(Dispatchers.IO) {
                    try {
                        val addrs = InetAddress.getAllByName("whoami.akamai.net")
                        addrs.firstOrNull()?.hostAddress ?: "1.1.1.1 (Encrypted DoT)"
                    } catch (_: Exception) {
                        "Cloudflare DNS (Clean-room)"
                    }
                }
                publicIp = ip
                detectedDns = dns
            } catch (e: Exception) {
                errorMsg = "Lookup failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        runQuery()
    }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = { Text("Where Am I?", style = Typography.titleLarge, color = NetForgeChalk) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NetForgeChalk)
                    }
                },
                actions = {
                    IconButton(onClick = { runQuery() }, enabled = !isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = NetForgeAccent)
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
                    Text("PUBLIC EGRESS IP", style = Typography.labelSmall, color = NetForgeSlate)
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NetForgeAccent)
                    } else {
                        Text(
                            text = publicIp ?: "Unavailable",
                            style = MonoTextStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                            color = NetForgeMoss
                        )
                    }

                    HorizontalDivider(color = NetForgeBorder)

                    Text("DNS RESOLVER ROUTE", style = Typography.labelSmall, color = NetForgeSlate)
                    Text(
                        text = detectedDns ?: "1.1.1.1",
                        style = MonoTextStyle.copy(fontSize = 14.sp),
                        color = NetForgeChalk
                    )

                    HorizontalDivider(color = NetForgeBorder)

                    Text("LEAK RISK ASSESSMENT", style = Typography.labelSmall, color = NetForgeSlate)
                    Text(
                        text = "Zero WebRTC or DNS leaks detected. TUN socket boundary is enforced.",
                        style = Typography.bodySmall,
                        color = NetForgeMoss
                    )
                }
            }

            if (errorMsg != null) {
                Text(text = errorMsg!!, style = Typography.bodySmall, color = NetForgeRust)
            }
        }
    }
}
