package com.netforge.app.ui.nav

object Routes {
    const val INTRO = "intro"
    const val HOME = "home"
    const val PROFILES = "profiles"
    const val PROFILE_DETAIL = "profile_detail"
    const val PAYLOAD_EDITOR = "payload_editor"
    const val IMPORT_FLOW = "import_flow"
    const val EXPORT_FLOW = "export_flow"
    const val CONSOLE = "console"
    const val BENCH = "bench"
    const val BENCH_WHERE_AM_I = "bench_whereami"
    const val BENCH_PINGLINE = "bench_pingline"
    const val BENCH_TIMEKEEPER = "bench_timekeeper"
    const val BENCH_BRIDGE = "bench_bridge"
    const val BENCH_FLOWMETER = "bench_flowmeter"
    const val BENCH_TRACEBACK = "bench_traceback"
    const val SETTINGS = "settings"
    const val SETTINGS_DEVICE_IDENTITY = "settings_device_identity"
    const val SETTINGS_SHELL_ACCESS = "settings_shell_access"
    const val SETTINGS_SLOW_CHANNEL = "settings_slow_channel"
    const val ABOUT = "about"

    fun profileDetail(id: Long) = "$PROFILE_DETAIL/$id"
    fun payloadEditor(id: Long) = "$PAYLOAD_EDITOR/$id"
    fun exportFlow(id: Long) = "$EXPORT_FLOW/$id"
}
