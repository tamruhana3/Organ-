package com.netforge.app.ui.bench

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.ui.theme.*
import java.net.NetworkInterface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracebackScreen(
    onNavigateBack: () -> Unit
) {
    val ifaces = remember {
        try {
            val list = mutableListOf<String>()
            val enumeration = NetworkInterface.getNetworkInterfaces()
            while (enumeration.hasMoreElements()) {
                val n = enumeration.nextElement()
                val addrs = n.inetAddresses.asSequence().map { it.hostAddress }.joinToString(", ")
                list.add("${n.name} (MTU=${n.mtu}, Up=${n.isUp}):\n  $addrs")
            }
            list
        } catch (e: Exception) {
            listOf("Failed to enumerate: ${e.message}")
        }
    }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = { Text("Traceback Interfaces", style = Typography.titleLarge, color = NetForgeChalk) },
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
                text = "ACTIVE DEVICE INTERFACES & TUNNEL BOUNDARIES",
                style = Typography.labelSmall,
                color = NetForgeSlate
            )

            for (iface in ifaces) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NetForgePaper)
                        .border(1.dp, NetForgeBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = iface,
                        style = MonoTextStyle.copy(fontSize = 12.sp),
                        color = if (iface.startsWith("tun")) NetForgeMoss else NetForgeChalk
                    )
                }
            }
        }
    }
}
