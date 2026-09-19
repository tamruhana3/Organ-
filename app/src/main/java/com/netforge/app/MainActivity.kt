package com.netforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.netforge.app.ui.nav.NetForgeNavHost
import com.netforge.app.ui.nav.Routes
import com.netforge.app.ui.theme.NetForgeInk
import com.netforge.app.ui.theme.NetForgeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as NetForgeApp
        val dataStore = app.dataStoreManager

        setContent {
            val isDusk by dataStore.isDuskThemeFlow.collectAsState(initial = true)
            val hasCompletedOnboarding by dataStore.hasCompletedOnboardingFlow.collectAsState(initial = null)

            if (hasCompletedOnboarding != null) {
                NetForgeTheme(isDusk = isDusk) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = NetForgeInk
                    ) {
                        val startDest = if (hasCompletedOnboarding == true) Routes.HOME else Routes.INTRO
                        NetForgeNavHost(
                            startDestination = startDest,
                            onIntroFinished = {
                                lifecycleScope.launch {
                                    dataStore.setOnboardingCompleted(true)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
