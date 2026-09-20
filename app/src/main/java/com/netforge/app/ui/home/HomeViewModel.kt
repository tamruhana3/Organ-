package com.netforge.app.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netforge.app.NetForgeApp
import com.netforge.app.domain.model.Metrics
import com.netforge.app.domain.model.Mode
import com.netforge.app.domain.model.Node
import com.netforge.app.domain.model.Profile
import com.netforge.app.domain.model.TunnelPhase
import com.netforge.app.domain.node.NodeCatalog
import com.netforge.app.logging.ConsoleBus
import com.netforge.app.service.NetForgeVpnService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NetForgeApp
    private val repo = app.repository
    private val dataStore = app.dataStoreManager

    val phaseFlow: StateFlow<TunnelPhase> = NetForgeVpnService.phaseFlow
    val metricsFlow: StateFlow<Metrics> = NetForgeVpnService.metricsFlow

    private val _selectedNode = MutableStateFlow(NodeCatalog.defaultNodes.first())
    val selectedNode: StateFlow<Node> = _selectedNode.asStateFlow()

    private val _currentProfile = MutableStateFlow(
        Profile(
            name = "USA Fast Gateway",
            mode = Mode.SslTunnel,
            host = NodeCatalog.defaultNodes.first().host,
            port = NodeCatalog.defaultNodes.first().port,
            sni = "cloudflare.com"
        )
    )
    val currentProfile: StateFlow<Profile> = _currentProfile.asStateFlow()

    val profilesList: StateFlow<List<Profile>> = repo.allProfilesFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        viewModelScope.launch {
            dataStore.selectedNodeIdFlow.collect { id ->
                val match = NodeCatalog.defaultNodes.find { it.id == id }
                if (match != null) {
                    _selectedNode.value = match
                }
            }
        }

        viewModelScope.launch {
            dataStore.activeProfileIdFlow.collect { profileId ->
                val profile = repo.getProfileById(profileId)
                if (profile != null) {
                    _currentProfile.value = profile
                }
            }
        }
    }

    fun selectNode(node: Node) {
        _selectedNode.value = node
        viewModelScope.launch {
            dataStore.setSelectedNodeId(node.id)
            // Update current profile host/port with node
            val updated = _currentProfile.value.copy(
                host = node.host,
                port = node.port
            )
            _currentProfile.value = updated
            repo.saveProfile(updated)
        }
    }

    fun selectMode(mode: Mode) {
        val updated = _currentProfile.value.copy(mode = mode)
        _currentProfile.value = updated
        viewModelScope.launch {
            repo.saveProfile(updated)
        }
    }

    fun updateProfile(profile: Profile) {
        _currentProfile.value = profile
        viewModelScope.launch {
            repo.saveProfile(profile)
        }
    }

    fun toggleTunnel(context: Context, onPermissionRequired: () -> Unit) {
        when (phaseFlow.value) {
            TunnelPhase.Ready, TunnelPhase.Halted, TunnelPhase.Error -> {
                val prepareIntent = android.net.VpnService.prepare(context)
                if (prepareIntent != null) {
                    onPermissionRequired()
                } else {
                    startTunnel(context)
                }
            }
            TunnelPhase.Opening, TunnelPhase.Live -> {
                stopTunnel(context)
            }
        }
    }

    fun startTunnel(context: Context) {
        ConsoleBus.info("HomeViewModel", "User initiated tunnel session for '${_currentProfile.value.name}'")
        NetForgeVpnService.start(context, _currentProfile.value)
    }

    fun stopTunnel(context: Context) {
        ConsoleBus.info("HomeViewModel", "User terminated tunnel session")
        NetForgeVpnService.stop(context)
    }
}
