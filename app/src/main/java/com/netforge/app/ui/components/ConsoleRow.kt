package com.netforge.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.domain.model.ConsoleLine
import com.netforge.app.domain.model.LogLevel
import com.netforge.app.ui.theme.*

@Composable
fun ConsoleRow(
    line: ConsoleLine,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val levelColor = when (line.level) {
        LogLevel.TRACE -> Color(0xFF6B7280)
        LogLevel.DEBUG -> NetForgeSlate
        LogLevel.INFO -> NetForgeMoss
        LogLevel.WARN -> NetForgeAmber
        LogLevel.ERROR -> NetForgeRust
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !line.details.isNullOrBlank()) { expanded = !expanded }
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = line.formattedTime(),
                style = MonoTextStyle.copy(fontSize = 11.sp),
                color = NetForgeSlate
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(levelColor.copy(alpha = 0.15f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = line.level.name,
                    style = MonoTextStyle.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = levelColor
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "[${line.tag}]",
                style = MonoTextStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = NetForgeAccent
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = line.message,
            style = MonoTextStyle.copy(fontSize = 12.sp),
            color = NetForgeChalk,
            modifier = Modifier.padding(start = 2.dp)
        )

        AnimatedVisibility(visible = expanded && !line.details.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NetForgeInk)
                    .padding(8.dp)
            ) {
                Text(
                    text = line.details ?: "",
                    style = MonoTextStyle.copy(fontSize = 10.sp),
                    color = NetForgeSlate
                )
            }
        }
    }
}
