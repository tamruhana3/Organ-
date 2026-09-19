package com.netforge.app.domain.tunnel

import android.os.ParcelFileDescriptor
import com.netforge.app.domain.model.Metrics
import com.netforge.app.domain.model.TunnelPhase
import kotlinx.coroutines.flow.StateFlow

interface TunnelEngine {
    val phaseFlow: StateFlow<TunnelPhase>
    val metricsFlow: StateFlow<Metrics>

    fun open(tunFd: ParcelFileDescriptor)
    fun close()
}
