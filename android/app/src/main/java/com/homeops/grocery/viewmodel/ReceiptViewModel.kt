package com.homeops.grocery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeops.grocery.network.RetrofitClient
import com.homeops.grocery.network.models.Receipt
import com.homeops.grocery.network.models.ReceiptMetadataUpdate
import com.homeops.grocery.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReceiptUiState {
    object Loading : ReceiptUiState()
    data class Success(val receipts: List<Receipt>) : ReceiptUiState()
    data class Error(val message: String) : ReceiptUiState()
}

class ReceiptViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ReceiptUiState>(ReceiptUiState.Loading)
    val uiState: StateFlow<ReceiptUiState> = _uiState

    init {
        loadReceipts()
    }

    fun loadReceipts() {
        viewModelScope.launch {
            _uiState.value = ReceiptUiState.Loading
            val api = RetrofitClient.getApi(SettingsManager.getBaseUrl())
            try {
                val response = api.getReceipts()
                if (response.isSuccessful) {
                    _uiState.value = ReceiptUiState.Success(response.body() ?: emptyList())
                } else {
                    _uiState.value = ReceiptUiState.Error("HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ReceiptUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateMetadata(receiptId: Int, store: String?, total: Double?, purchaseDate: String?) {
        viewModelScope.launch {
            val api = RetrofitClient.getApi(SettingsManager.getBaseUrl())
            try {
                val response = api.updateReceiptMetadata(
                    receiptId,
                    ReceiptMetadataUpdate(
                        store = store?.ifBlank { null },
                        totalAmount = total,
                        purchaseDate = purchaseDate?.ifBlank { null },
                    )
                )
                if (response.isSuccessful) {
                    loadReceipts()
                }
            } catch (_: Exception) {
                // Silently fail.
            }
        }
    }
}
