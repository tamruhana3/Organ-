package com.netforge.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.domain.model.BannerInfo
import com.netforge.app.domain.model.BannerStyle
import com.netforge.app.ui.theme.*

@Composable
fun BannerPreview(
    banner: BannerInfo,
    modifier: Modifier = Modifier
) {
    if (!banner.enabled || banner.message.isBlank()) return
    val context = LocalContext.current

    val baseColor = when (banner.style) {
        BannerStyle.Info -> NetForgeAccent
        BannerStyle.Warning -> NetForgeAmber
        BannerStyle.Success -> NetForgeMoss
        BannerStyle.Custom -> try {
            Color(android.graphics.Color.parseColor(banner.customColorHex))
        } catch (_: Exception) {
            NetForgeAccent
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(baseColor.copy(alpha = 0.12f))
            .border(1.dp, baseColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = banner.message,
                style = Typography.bodyMedium.copy(color = NetForgeChalk),
                fontSize = 14.sp
            )

            if (banner.buttonLabel.isNotBlank() && banner.buttonUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(banner.buttonUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = baseColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = banner.buttonLabel,
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
