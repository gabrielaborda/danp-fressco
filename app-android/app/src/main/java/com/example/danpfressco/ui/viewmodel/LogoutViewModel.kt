package com.example.danpfressco.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danpfressco.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel del componente [LogoutIconButton].
 *
 * Responsabilidad única (SRP): gestionar el cierre de sesión.
 * La UI observa [logoutDone] y navega a Login cuando se vuelve true.
 * No depende de [SessionManager] directamente — delega a [AuthRepository] (DIP).
 */
@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _logoutDone = MutableStateFlow(false)
    /** Gatillo de navegación: true indica que la sesión fue cerrada. */
    val logoutDone: StateFlow<Boolean> = _logoutDone.asStateFlow()

    /**
     * Borra la sesión JWT del DataStore y señaliza que el logout completó.
     * Llamado desde [LogoutIconButton] tras confirmación del usuario.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _logoutDone.value = true
        }
    }
}
