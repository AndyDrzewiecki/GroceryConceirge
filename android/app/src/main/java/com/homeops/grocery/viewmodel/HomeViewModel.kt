package com.homeops.grocery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeops.grocery.network.RetrofitClient
import com.homeops.grocery.network.models.DashboardResponse
import com.homeops.grocery.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val dashboard: DashboardResponse) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class ConnectionState {
    object Idle : ConnectionState()
    object Testing : ConnectionState()
    data class Success(val message: String) : ConnectionState()
    data class Failure(val message: String) : ConnectionState()
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    /** Exposed so HomeScreen can display the currently active server URL. */
    val currentBaseUrl: String get() = SettingsManager.getBaseUrl()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val api = RetrofitClient.getApi(SettingsManager.getBaseUrl())
            try {
                val response = api.getDashboard()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = HomeUiState.Success(response.body()!!)
                } else {
                    _uiState.value = HomeUiState.Error("HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.Testing
            val api = RetrofitClient.getApi(SettingsManager.getBaseUrl())
            try {
                val response = api.health()
                if (response.isSuccessful) {
                    _connectionState.value = ConnectionState.Success(
                        "OK – ${SettingsManager.getBaseUrl()} → status: ${response.body()?.status}"
                    )
                } else {
                    _connectionState.value = ConnectionState.Failure("HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Failure(e.message ?: "Cannot reach backend")
            }
        }
    }
}
