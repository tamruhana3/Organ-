package com.netforge.app.ui.settings

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
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellAccessScreen(
    onNavigateBack: () -> Unit
) {
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var user by remember { mutableStateOf("root") }
    var pass by remember { mutableStateOf("") }
    var command by remember { mutableStateOf("uname -a; uptime") }
    var outputText by remember { mutableStateOf("") }
    var isExecuting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun executeShell() {
        if (host.isBlank()) return
        isExecuting = true
        outputText = "Connecting to $host:$port via SSH...\n"
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                val ssh = SSHClient()
                ssh.addHostKeyVerifier(PromiscuousVerifier())
                try {
                    ssh.connect(host, port.toIntOrNull() ?: 22)
                    ssh.authPassword(user, pass)
                    val session = ssh.startSession()
                    val cmd = session.exec(command)
                    val out = cmd.inputStream.bufferedReader().use(BufferedReader::readText)
                    cmd.join()
                    session.close()
                    ssh.disconnect()
                    out.ifEmpty { "(Command completed with zero output)" }
                } catch (e: Exception) {
                    "SSH error: ${e.message}"
                } finally {
                    try { ssh.disconnect() } catch (_: Exception) {}
                }
            }
            outputText = result
            isExecuting = false
        }
    }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = { Text("SSH Shell Terminal", style = Typography.titleLarge, color = NetForgeChalk) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("SSH Host", color = NetForgeSlate) },
                    singleLine = true,
                    colors = customFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(2.5f)
                )
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port", color = NetForgeSlate) },
                    singleLine = true,
                    colors = customFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Username", color = NetForgeSlate) },
                    singleLine = true,
                    colors = customFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Password", color = NetForgeSlate) },
                    singleLine = true,
                    colors = customFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                label = { Text("Shell Command", color = NetForgeSlate) },
                singleLine = true,
                colors = customFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { executeShell() },
                enabled = !isExecuting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NetForgeAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isExecuting) "Running Command..." else "Execute Command")
            }

            // Output Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = outputText.ifEmpty { "Command standard output will appear here." },
                    style = MonoTextStyle.copy(fontSize = 12.sp),
                    color = NetForgeChalk
                )
            }
        }
    }
}

@Composable
private fun customFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NetForgeAccent,
    unfocusedBorderColor = NetForgeBorder,
    focusedContainerColor = NetForgePaper,
    unfocusedContainerColor = NetForgePaper,
    focusedTextColor = NetForgeChalk,
    unfocusedTextColor = NetForgeChalk
)
