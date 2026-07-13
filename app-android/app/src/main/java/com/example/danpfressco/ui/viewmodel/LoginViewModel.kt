package com.example.danpfressco.ui.viewmodel

import com.example.danpfressco.ui.state.LoginUiState

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danpfressco.data.repository.AuthRepository
import com.example.danpfressco.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    fun onEmailChanged(email: String) {
        _uiState.update { state ->
            val emailError = when {
                email.isBlank() -> "El correo no puede estar vacío"
                !email.matches(emailRegex) -> "Formato de correo no válido"
                else -> null
            }
            state.copy(
                email = email,
                emailError = emailError,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { state ->
            val passwordError = if (password.isBlank()) "La contraseña no puede estar vacía" else null
            state.copy(
                password = password,
                passwordError = passwordError,
                errorMessage = null
            )
        }
    }

    fun login() {
        val currentEmail = _uiState.value.email
        val currentPassword = _uiState.value.password

        val emailError = when {
            currentEmail.isBlank() -> "El correo no puede estar vacío"
            !currentEmail.matches(emailRegex) -> "Formato de correo no válido"
            else -> null
        }
        val passwordError = if (currentPassword.isBlank()) "La contraseña no puede estar vacía" else null

        if (emailError != null || passwordError != null) {
            _uiState.update { state ->
                state.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.login(currentEmail, currentPassword)
            result.fold(
                onSuccess = { usuario ->
                    sessionManager.saveSession(usuario)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Ocurrió un error inesperado"
                        )
                    }
                }
            )
        }
    }
}
