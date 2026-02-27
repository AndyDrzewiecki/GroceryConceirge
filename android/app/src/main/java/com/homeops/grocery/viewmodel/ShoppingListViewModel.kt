package com.homeops.grocery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeops.grocery.network.RetrofitClient
import com.homeops.grocery.network.models.ShoppingListItem
import com.homeops.grocery.network.models.ShoppingListItemPatch
import com.homeops.grocery.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ShoppingListUiState {
    object Loading : ShoppingListUiState()
    data class Success(val items: List<ShoppingListItem>) : ShoppingListUiState()
    data class Error(val message: String) : ShoppingListUiState()
}

class ShoppingListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ShoppingListUiState>(ShoppingListUiState.Loading)
    val uiState: StateFlow<ShoppingListUiState> = _uiState

    init {
        loadShoppingList()
    }

    fun loadShoppingList() {
        viewModelScope.launch {
            _uiState.value = ShoppingListUiState.Loading
            val api = RetrofitClient.getApi(SettingsManager.getBaseUrl())
            try {
                val response = api.getShoppingList()
                if (response.isSuccessful) {
                    _uiState.value = ShoppingListUiState.Success(response.body() ?: emptyList())
                } else {
                    _uiState.value = ShoppingListUiState.Error("HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ShoppingListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun togglePurchased(item: ShoppingListItem) {
        viewModelScope.launch {
            val api = RetrofitClient.getApi(SettingsManager.getBaseUrl())
            try {
                val response = api.patchShoppingItem(item.id, ShoppingListItemPatch(purchased = !item.purchased))
                if (response.isSuccessful) {
                    loadShoppingList()
                }
            } catch (_: Exception) {
                // Silently fail – the list will not reflect the toggle if network is down.
            }
        }
    }
}
