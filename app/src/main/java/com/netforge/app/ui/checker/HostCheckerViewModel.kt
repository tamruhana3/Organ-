package com.netforge.app.ui.checker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netforge.app.NetForgeApp
import com.netforge.app.logging.ConsoleBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class HostCheckResult(
    val statusCode: Int = 0,
    val statusMessage: String = "",
    val protocol: String = "HTTP/1.1",
    val headers: Map<String, String> = emptyMap(),
    val bodySnippet: String = "",
    val dnsLookupMs: Long = 0L,
    val totalTimeMs: Long = 0L,
    val resolvedIp: String = "",
    val error: String? = null
)

class HostCheckerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NetForgeApp
    private val repo = app.repository

    val targetUrl = MutableStateFlow("https://fast.com")
    val requestMethod = MutableStateFlow("GET")
    val injectionMode = MutableStateFlow("Direct (Normal)")
    val proxyHost = MutableStateFlow("")
    val proxyPort = MutableStateFlow("8080")
    val customUserAgent = MutableStateFlow("NetForge-HostChecker/1.0")

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _result = MutableStateFlow<HostCheckResult?>(null)
    val result: StateFlow<HostCheckResult?> = _result.asStateFlow()

    fun checkHost() {
        val rawInput = targetUrl.value.trim()
        if (rawInput.isBlank()) return

        val urlString = if (!rawInput.startsWith("http://") && !rawInput.startsWith("https://")) {
            "https://$rawInput"
        } else {
            rawInput
        }

        _isChecking.value = true
        _result.value = null

        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                performHostCheck(urlString)
            }
            _result.value = res
            _isChecking.value = false
            ConsoleBus.info("HostChecker", "Checked $urlString -> Status ${res.statusCode} (${res.totalTimeMs}ms)")
        }
    }

    private fun performHostCheck(urlString: String): HostCheckResult {
        val t0 = System.currentTimeMillis()
        var dnsTime = 0L
        var resolvedIp = ""

        try {
            val url = URL(urlString)
            val host = url.host

            val tDnsStart = System.currentTimeMillis()
            try {
                val inet = InetAddress.getByName(host)
                dnsTime = System.currentTimeMillis() - tDnsStart
                resolvedIp = inet.hostAddress ?: ""
            } catch (e: Exception) {
                dnsTime = System.currentTimeMillis() - tDnsStart
                resolvedIp = "Resolution failed"
            }

            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = requestMethod.value
            connection.setRequestProperty("User-Agent", customUserAgent.value)
            connection.setRequestProperty("Accept", "*/*")
            connection.setRequestProperty("Connection", "close")
            connection.instanceFollowRedirects = false

            if (connection is HttpsURLConnection) {
                connection.sslSocketFactory = com.netforge.app.domain.tunnel.SslHelper.trustingSocketFactory
                connection.setHostnameVerifier { _, _ -> true }
            }

            val tConnectStart = System.currentTimeMillis()
            connection.connect()

            val statusCode = connection.responseCode
            val statusMessage = connection.responseMessage ?: ""
            val totalTime = System.currentTimeMillis() - t0

            val headerMap = mutableMapOf<String, String>()
            connection.headerFields.forEach { (key, values) ->
                if (key != null && values.isNotEmpty()) {
                    headerMap[key] = values.joinToString(", ")
                }
            }

            val snippet = try {
                val stream = if (statusCode in 200..399) connection.inputStream else connection.errorStream
                stream?.let {
                    BufferedReader(InputStreamReader(it)).use { reader ->
                        val sb = StringBuilder()
                        var line: String?
                        var count = 0
                        while (reader.readLine().also { line = it } != null && count < 8) {
                            sb.appendLine(line)
                            count++
                        }
                        sb.toString().trim()
                    }
                } ?: ""
            } catch (_: Exception) {
                ""
            }

            return HostCheckResult(
                statusCode = statusCode,
                statusMessage = statusMessage,
                protocol = "HTTP/1.1",
                headers = headerMap,
                bodySnippet = snippet,
                dnsLookupMs = dnsTime,
                totalTimeMs = totalTime,
                resolvedIp = resolvedIp
            )

        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - t0
            return HostCheckResult(
                statusCode = 0,
                statusMessage = "Connection failed",
                error = e.message ?: "Unknown error",
                dnsLookupMs = dnsTime,
                totalTimeMs = totalTime,
                resolvedIp = resolvedIp
            )
        }
    }

    fun applyToCurrentProfile(onComplete: () -> Unit) {
        val rawInput = targetUrl.value.trim()
        val cleanHost = rawInput
            .removePrefix("https://")
            .removePrefix("http://")
            .split("/").firstOrNull() ?: rawInput

        viewModelScope.launch {
            val currentProfiles = repo.getAllProfilesOnce()
            val target = currentProfiles.firstOrNull()
            if (target != null) {
                val updated = target.copy(
                    sni = cleanHost,
                    frontHost = cleanHost
                )
                repo.saveProfile(updated)
                ConsoleBus.info("HostChecker", "Applied host '$cleanHost' as SNI to profile '${target.name}'")
            }
            onComplete()
        }
    }
}
