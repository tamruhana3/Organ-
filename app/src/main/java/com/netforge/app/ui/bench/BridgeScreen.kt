package com.netforge.app.ui.bench

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BridgeScreen(
    onNavigateBack: () -> Unit
) {
    var host by remember { mutableStateOf("cloudflare.com") }
    var port by remember { mutableStateOf("443") }
    var useTls by remember { mutableStateOf(true) }
    var isTesting by remember { mutableStateOf(false) }
    var logOutput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun testBridge() {
        isTesting = true
        logOutput = "Initiating socket probe...\n"
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                val sb = StringBuilder()
                try {
                    val p = port.toIntOrNull() ?: 443
                    val t0 = System.currentTimeMillis()
                    val plainSocket = Socket()
                    sb.append("Connecting to $host:$p (TCP)...\n")
                    plainSocket.connect(InetSocketAddress(host, p), 5000)
                    val tcpElapsed = System.currentTimeMillis() - t0
                    sb.append("TCP connection open in ${tcpElapsed}ms\n")

                    if (useTls) {
                        sb.append("Starting TLS handshake (SNI: $host)...\n")
                        val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
                        val sslSocket = sslFactory.createSocket(plainSocket, host, p, true) as SSLSocket
                        val tTls0 = System.currentTimeMillis()
                        sslSocket.startHandshake()
                        val tlsElapsed = System.currentTimeMillis() - tTls0
                        val session = sslSocket.session
                        sb.append("TLS Handshake completed in ${tlsElapsed}ms\n")
                        sb.append("Protocol: ${session.protocol}\n")
                        sb.append("Cipher suite: ${session.cipherSuite}\n")
                        sb.append("Peer principal: ${session.peerPrincipal?.name ?: "Unknown"}\n")
                        sslSocket.close()
                    } else {
                        plainSocket.close()
                    }
                    sb.append("Probe completed successfully.")
                } catch (e: Exception) {
                    sb.append("Failed: ${e.message}")
                }
                sb.toString()
            }
            logOutput = result
            isTesting = false
        }
    }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = { Text("Bridge Socket & TLS", style = Typography.titleLarge, color = NetForgeChalk) },
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Probe Host", color = NetForgeSlate) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NetForgeAccent,
                        unfocusedBorderColor = NetForgeBorder,
                        focusedContainerColor = NetForgePaper,
                        unfocusedContainerColor = NetForgePaper,
                        focusedTextColor = NetForgeChalk,
                        unfocusedTextColor = NetForgeChalk
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(2.5f)
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port", color = NetForgeSlate) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NetForgeAccent,
                        unfocusedBorderColor = NetForgeBorder,
                        focusedContainerColor = NetForgePaper,
                        unfocusedContainerColor = NetForgePaper,
                        focusedTextColor = NetForgeChalk,
                        unfocusedTextColor = NetForgeChalk
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Inspect TLS Handshake", style = Typography.bodyMedium, color = NetForgeChalk)
                Switch(
                    checked = useTls,
                    onCheckedChange = { useTls = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = NetForgeAccent)
                )
            }

            Button(
                onClick = { testBridge() },
                enabled = !isTesting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NetForgeEmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isTesting) "Probing Socket..." else "Run Socket Handshake")
            }

            // Results box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = logOutput.ifEmpty { "Socket probe output will appear here." },
                    style = MonoTextStyle.copy(fontSize = 12.sp),
                    color = NetForgeChalk
                )
            }
        }
    }
}
