package com.netforge.app.logging

import com.netforge.app.domain.model.ConsoleLine
import com.netforge.app.domain.model.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList

object ConsoleBus {

    private const val MAX_BUFFER_SIZE = 500
    private val buffer = CopyOnWriteArrayList<ConsoleLine>()
    private val _linesFlow = MutableStateFlow<List<ConsoleLine>>(emptyList())
    val linesFlow: StateFlow<List<ConsoleLine>> = _linesFlow.asStateFlow()

    fun log(level: LogLevel, tag: String, message: String, details: String? = null) {
        val entry = ConsoleLine(
            level = level,
            tag = tag,
            message = message,
            details = details
        )
        buffer.add(entry)
        while (buffer.size > MAX_BUFFER_SIZE) {
            buffer.removeAt(0)
        }
        _linesFlow.value = buffer.toList()
    }

    fun trace(tag: String, message: String) = log(LogLevel.TRACE, tag, message)
    fun debug(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun info(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun warn(tag: String, message: String, details: String? = null) = log(LogLevel.WARN, tag, message, details)
    fun error(tag: String, message: String, details: String? = null) = log(LogLevel.ERROR, tag, message, details)

    fun clear() {
        buffer.clear()
        _linesFlow.value = emptyList()
    }

    fun exportToLogText(): String {
        return buildString {
            for (line in buffer) {
                append("[${line.formattedTime()}] [${line.level.name}] [${line.tag}] ${line.message}")
                if (!line.details.isNullOrBlank()) {
                    append("\n  ${line.details.replace("\n", "\n  ")}")
                }
                append("\n")
            }
        }
    }
}
