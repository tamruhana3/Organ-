package com.netforge.app.ui.nav

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.netforge.app.ui.bench.*
import com.netforge.app.ui.console.ConsoleScreen
import com.netforge.app.ui.console.ConsoleViewModel
import com.netforge.app.ui.editor.PayloadEditorScreen
import com.netforge.app.ui.editor.PayloadEditorViewModel
import com.netforge.app.ui.exportflow.ExportScreen
import com.netforge.app.ui.exportflow.ExportViewModel
import com.netforge.app.ui.home.HomeScreen
import com.netforge.app.ui.home.HomeViewModel
import com.netforge.app.ui.importflow.ImportScreen
import com.netforge.app.ui.importflow.ImportViewModel
import com.netforge.app.ui.intro.IntroScreen
import com.netforge.app.ui.profiles.ProfileDetailScreen
import com.netforge.app.ui.profiles.ProfileDetailViewModel
import com.netforge.app.ui.profiles.ProfileListScreen
import com.netforge.app.ui.profiles.ProfileListViewModel
import com.netforge.app.ui.settings.*
import com.netforge.app.ui.theme.Motion

@Composable
fun NetForgeNavHost(
    startDestination: String = Routes.HOME,
    onIntroFinished: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = viewModel()

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            homeViewModel.startTunnel(context)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(Motion.durationBase)) },
        exitTransition = { fadeOut(tween(Motion.durationFast)) },
        popEnterTransition = { fadeIn(tween(Motion.durationBase)) },
        popExitTransition = { fadeOut(tween(Motion.durationFast)) }
    ) {
        composable(Routes.INTRO) {
            IntroScreen(
                onFinish = {
                    onIntroFinished()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.INTRO) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateProfiles = { navController.navigate(Routes.PROFILES) },
                onNavigateConsole = { navController.navigate(Routes.CONSOLE) },
                onNavigateBench = { navController.navigate(Routes.BENCH) },
                onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateDeviceIdentity = { navController.navigate(Routes.SETTINGS_DEVICE_IDENTITY) },
                onNavigateShellAccess = { navController.navigate(Routes.SETTINGS_SHELL_ACCESS) },
                onNavigateSlowChannel = { navController.navigate(Routes.SETTINGS_SLOW_CHANNEL) },
                onNavigateImport = { navController.navigate(Routes.IMPORT_FLOW) },
                onNavigateAbout = { navController.navigate(Routes.ABOUT) },
                onNavigateHostChecker = { navController.navigate(Routes.HOST_CHECKER) },
                onNavigatePayloadEditor = { id -> navController.navigate(Routes.payloadEditor(id)) },
                onPermissionRequired = {
                    val intent = VpnService.prepare(context)
                    if (intent != null) {
                        vpnPermissionLauncher.launch(intent)
                    }
                }
            )
        }

        composable(Routes.PROFILES) {
            val listViewModel: ProfileListViewModel = viewModel()
            ProfileListScreen(
                viewModel = listViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateDetail = { id -> navController.navigate(Routes.profileDetail(id)) },
                onNavigateExport = { id -> navController.navigate(Routes.exportFlow(id)) },
                onNavigateImport = { navController.navigate(Routes.IMPORT_FLOW) }
            )
        }

        composable(
            route = "${Routes.PROFILE_DETAIL}/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: 1L
            val detailViewModel: ProfileDetailViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ProfileDetailViewModel(context.applicationContext as android.app.Application, profileId) as T
                    }
                }
            )
            ProfileDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigatePayloadEditor = { id -> navController.navigate(Routes.payloadEditor(id)) },
                onNavigateExport = { id -> navController.navigate(Routes.exportFlow(id)) }
            )
        }

        composable(
            route = "${Routes.PAYLOAD_EDITOR}/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: 1L
            val editorViewModel: PayloadEditorViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return PayloadEditorViewModel(context.applicationContext as android.app.Application, profileId) as T
                    }
                }
            )
            PayloadEditorScreen(
                viewModel = editorViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.IMPORT_FLOW) {
            val importViewModel: ImportViewModel = viewModel()
            ImportScreen(
                viewModel = importViewModel,
                onNavigateBack = { navController.popBackStack() },
                onImportComplete = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.EXPORT_FLOW}/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: 1L
            val exportViewModel: ExportViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ExportViewModel(context.applicationContext as android.app.Application, profileId) as T
                    }
                }
            )
            ExportScreen(
                viewModel = exportViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CONSOLE) {
            val consoleViewModel: ConsoleViewModel = viewModel()
            ConsoleScreen(
                viewModel = consoleViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HOST_CHECKER) {
            com.netforge.app.ui.checker.HostCheckerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.BENCH) {
            BenchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateWhereAmI = { navController.navigate(Routes.BENCH_WHERE_AM_I) },
                onNavigatePingline = { navController.navigate(Routes.BENCH_PINGLINE) },
                onNavigateTimekeeper = { navController.navigate(Routes.BENCH_TIMEKEEPER) },
                onNavigateBridge = { navController.navigate(Routes.BENCH_BRIDGE) },
                onNavigateFlowmeter = { navController.navigate(Routes.BENCH_FLOWMETER) },
                onNavigateTraceback = { navController.navigate(Routes.BENCH_TRACEBACK) },
                onNavigateHostChecker = { navController.navigate(Routes.HOST_CHECKER) }
            )
        }

        composable(Routes.BENCH_WHERE_AM_I) {
            WhereAmIScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.BENCH_PINGLINE) {
            PinglineScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.BENCH_TIMEKEEPER) {
            TimekeeperScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.BENCH_BRIDGE) {
            BridgeScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.BENCH_FLOWMETER) {
            FlowmeterScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.BENCH_TRACEBACK) {
            TracebackScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS_DEVICE_IDENTITY) {
            DeviceIdentityScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS_SHELL_ACCESS) {
            ShellAccessScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS_SLOW_CHANNEL) {
            SlowChannelScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.ABOUT) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
