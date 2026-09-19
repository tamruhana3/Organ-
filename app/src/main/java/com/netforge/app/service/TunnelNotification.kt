package com.netforge.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R
import com.netforge.app.MainActivity
import com.netforge.app.domain.model.Metrics
import com.netforge.app.domain.model.Profile
import com.netforge.app.domain.model.TunnelPhase

object TunnelNotification {

    const val CHANNEL_ID = "netforge_tunnel_channel"
    const val NOTIFICATION_ID = 4040

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NetForge Active Routing"
            val description = "Displays active tunnel routing statistics and session controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
                setShowBadge(false)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun build(
        context: Context,
        profile: Profile,
        phase: TunnelPhase,
        metrics: Metrics
    ): Notification {
        createNotificationChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, NetForgeVpnService::class.java).apply {
            action = NetForgeVpnService.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (phase) {
            TunnelPhase.Live -> "NetForge — Active (${profile.name})"
            TunnelPhase.Opening -> "NetForge — Opening tunnel..."
            TunnelPhase.Error -> "NetForge — Tunnel error"
            TunnelPhase.Halted -> "NetForge — Tunnel halted"
            TunnelPhase.Ready -> "NetForge — Ready"
        }

        val content = if (phase == TunnelPhase.Live) {
            "↑ ${metrics.formattedUp()}   ↓ ${metrics.formattedDown()}  •  ${metrics.pingMs} ms ping"
        } else {
            "Mode: ${profile.mode.displayName}  •  ${profile.host}:${profile.port}"
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(openPendingIntent)
            .setOngoing(phase == TunnelPhase.Opening || phase == TunnelPhase.Live)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "End session", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
