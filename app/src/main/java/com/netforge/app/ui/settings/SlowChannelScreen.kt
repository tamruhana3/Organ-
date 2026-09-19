package com.netforge.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlowChannelScreen(
    onNavigateBack: () -> Unit
) {
    var nameserver by remember { mutableStateOf("1.1.1.1") }
    var queryDomain by remember { mutableStateOf("slow.netforge.internal") }
    var publicKey by remember { mutableStateOf("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef") }
    var recordType by remember { mutableStateOf("TXT") }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = { Text("Slow Channel (DNS)", style = Typography.titleLarge, color = NetForgeChalk) },
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
            Text(
                text = "DNS COVERT TUNNELING CONFIGURATION",
                style = Typography.labelSmall,
                color = NetForgeSlate
            )

            Text(
                text = "Slow mode routes encapsulated packets over recursive DNS resolvers when all standard TCP/TLS ports are firewalled.",
                style = Typography.bodySmall,
                color = NetForgeSlate
            )

            OutlinedTextField(
                value = nameserver,
                onValueChange = { nameserver = it },
                label = { Text("Recursive Nameserver / DoH Resolver", color = NetForgeSlate) },
                singleLine = true,
                colors = customSlowFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = queryDomain,
                onValueChange = { queryDomain = it },
                label = { Text("Root Query Domain (NS Authority)", color = NetForgeSlate) },
                singleLine = true,
                colors = customSlowFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = publicKey,
                onValueChange = { publicKey = it },
                label = { Text("Server Ed25519 Public Key (Hex)", color = NetForgeSlate) },
                maxLines = 2,
                textStyle = MonoTextStyle.copy(fontSize = 11.sp),
                colors = customSlowFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = recordType,
                onValueChange = { recordType = it },
                label = { Text("DNS Query Record Type (TXT / NULL / CNAME)", color = NetForgeSlate) },
                singleLine = true,
                colors = customSlowFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onNavigateBack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NetForgeAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save DNS Channel Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun customSlowFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NetForgeAccent,
    unfocusedBorderColor = NetForgeBorder,
    focusedContainerColor = NetForgePaper,
    unfocusedContainerColor = NetForgePaper,
    focusedTextColor = NetForgeChalk,
    unfocusedTextColor = NetForgeChalk
)
