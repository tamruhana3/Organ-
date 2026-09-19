package com.netforge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.ui.theme.*

enum class PasswordStrength(val label: String, val score: Int, val color: Color) {
    Empty("Empty", 0, NetForgeSlate),
    Weak("Weak", 1, NetForgeRust),
    Fair("Fair", 2, NetForgeAmber),
    Strong("Strong", 3, NetForgeMoss),
    Fortified("Fortified", 4, NetForgeAccent)
}

@Composable
fun StrengthMeter(
    passphrase: String,
    modifier: Modifier = Modifier
) {
    val strength = remember(passphrase) {
        if (passphrase.isEmpty()) return@remember PasswordStrength.Empty
        var points = 0
        if (passphrase.length >= 8) points++
        if (passphrase.length >= 12) points++
        if (passphrase.any { it.isUpperCase() } && passphrase.any { it.isLowerCase() }) points++
        if (passphrase.any { it.isDigit() } || passphrase.any { !it.isLetterOrDigit() }) points++

        when (points) {
            0, 1 -> PasswordStrength.Weak
            2 -> PasswordStrength.Fair
            3 -> PasswordStrength.Strong
            else -> PasswordStrength.Fortified
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..4) {
                val filled = strength.score >= i
                val barColor = if (filled) strength.color else NetForgePaper2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Key strength",
                style = Typography.labelSmall.copy(fontSize = 10.sp),
                color = NetForgeSlate
            )
            Text(
                text = strength.label,
                style = Typography.labelSmall.copy(fontSize = 10.sp),
                color = strength.color
            )
        }
    }
}
