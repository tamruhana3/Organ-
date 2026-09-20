package com.netforge.app.domain.tunnel

import com.netforge.app.domain.model.Mode
import com.netforge.app.domain.model.Profile

object TunnelEngineFactory {

    fun create(profile: Profile): TunnelEngine {
        return when (profile.mode) {
            Mode.CustomPayload -> WrappedPlusTunnel(profile)
            Mode.SslTunnel -> WrappedTunnel(profile)
            Mode.SslProxy -> SslProxyTunnel(profile)
            Mode.SslHttp -> WrappedPlusTunnel(profile)
            Mode.SlowDns -> SlowTunnel(profile)
            Mode.SshDirect -> DirectTunnel(profile)

            Mode.Direct -> DirectTunnel(profile)
            Mode.Wrapped -> WrappedTunnel(profile)
            Mode.WrappedPlus -> WrappedPlusTunnel(profile)
            Mode.Slow -> SlowTunnel(profile)
            Mode.Live -> LiveTunnel(profile)
        }
    }
}
