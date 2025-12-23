package com.dinzio.bookingapp.features.main.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinzio.bookingapp.common.navigation.Screen
import com.dinzio.bookingapp.core.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            tokenManager.getToken().collect { token ->
                if (token.isNullOrEmpty()) {
                    _startDestination.value = Screen.Login.route
                } else {
                    _startDestination.value = Screen.BookingList.route
                }
                _isLoading.value = false
            }
        }
    }
}