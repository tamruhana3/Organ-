package com.netforge.app.domain.model

enum class TunnelPhase(val label: String) {
    Ready("Ready"),
    Opening("Opening"),
    Live("Live"),
    Halted("Halted"),
    Error("Error")
}
