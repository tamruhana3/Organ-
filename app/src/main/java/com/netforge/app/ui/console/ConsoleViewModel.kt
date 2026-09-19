package com.netforge.app.ui.console

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netforge.app.domain.model.ConsoleLine
import com.netforge.app.domain.model.LogLevel
import com.netforge.app.logging.ConsoleBus
import kotlinx.coroutines.flow.*

class ConsoleViewModel(application: Application) : AndroidViewModel(application) {

    val allLogs: StateFlow<List<ConsoleLine>> = ConsoleBus.linesFlow

    private val _selectedFilter = MutableStateFlow<LogLevel?>(null)
    val selectedFilter: StateFlow<LogLevel?> = _selectedFilter.asStateFlow()

    val filteredLogs: StateFlow<List<ConsoleLine>> = combine(allLogs, _selectedFilter) { logs, filter ->
        if (filter == null) logs else logs.filter { it.level == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: LogLevel?) {
        _selectedFilter.value = filter
    }

    fun clearLogs() {
        ConsoleBus.clear()
    }

    fun getFullLogText(): String {
        return allLogs.value.joinToString("\n") { line ->
            "${line.formattedTime()} [${line.level.name}] [${line.tag}]: ${line.message}" +
                    if (!line.details.isNullOrBlank()) "\n${line.details}" else ""
        }
    }
}
