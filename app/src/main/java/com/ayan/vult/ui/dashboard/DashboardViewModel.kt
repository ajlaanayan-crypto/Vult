package com.ayan.vult.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayan.vult.data.AppInfo
import com.ayan.vult.data.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: AppRepository
) : ViewModel() {

    val apps: StateFlow<List<AppInfo>> = repository.getInstalledApps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleAppBlock(app: AppInfo) {
        viewModelScope.launch {
            repository.toggleAppBlock(app)
        }
    }
}
