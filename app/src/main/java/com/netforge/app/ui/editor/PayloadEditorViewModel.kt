package com.netforge.app.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netforge.app.NetForgeApp
import com.netforge.app.domain.model.Profile
import com.netforge.app.domain.payload.PayloadContext
import com.netforge.app.domain.payload.PayloadEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PayloadEditorViewModel(
    application: Application,
    private val profileId: Long
) : AndroidViewModel(application) {

    private val app = application as NetForgeApp
    private val repo = app.repository

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _template = MutableStateFlow("")
    val template: StateFlow<String> = _template.asStateFlow()

    private val _renderedOutput = MutableStateFlow("")
    val renderedOutput: StateFlow<String> = _renderedOutput.asStateFlow()

    init {
        viewModelScope.launch {
            val p = repo.getProfileById(profileId)
            _profile.value = p
            val initial = p?.payloadTemplate ?: "CONNECT [host_port] HTTP/1.1[crlf]Host: [host][crlf]User-Agent: [ua][crlf]Connection: Upgrade[crlf]Upgrade: websocket[crlf][crlf]"
            _template.value = initial
            recomputeRender(initial, p)
        }
    }

    fun onTemplateChanged(newText: String) {
        _template.value = newText
        recomputeRender(newText, _profile.value)
    }

    fun insertPlaceholder(tag: String) {
        val updated = _template.value + tag
        onTemplateChanged(updated)
    }

    fun applyPreset(presetTemplate: String) {
        onTemplateChanged(presetTemplate)
    }

    private fun recomputeRender(tpl: String, p: Profile?) {
        val ctx = PayloadContext(
            host = p?.host ?: "127.0.0.1",
            port = p?.port ?: 443,
            frontHost = p?.frontHost?.ifBlank { "cdn.cloudflare.com" },
            sshUser = p?.sshUser?.ifBlank { "user" },
            sshPass = p?.sshPass?.ifBlank { "secret" },
            tls = true
        )
        val rendered = PayloadEngine.render(tpl, ctx)
        _renderedOutput.value = rendered
    }

    fun save() {
        val p = _profile.value ?: return
        val updated = p.copy(payloadTemplate = _template.value)
        viewModelScope.launch {
            repo.saveProfile(updated)
        }
    }
}
