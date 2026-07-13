package com.example.danpfressco.ui.viewmodel

import com.example.danpfressco.ui.state.RegistroUiState

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
class RegistroViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    fun onNombreChanged(nombre: String) {
        _uiState.update { state ->
            val nombreError = if (nombre.isBlank()) "El nombre no puede estar vacío" else null
            state.copy(nombre = nombre, nombreError = nombreError, errorMessage = null)
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { state ->
            val emailError = when {
                email.isBlank() -> "El correo no puede estar vacío"
                !email.matches(emailRegex) -> "Formato de correo no válido"
                else -> null
            }
            state.copy(email = email, emailError = emailError, errorMessage = null)
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { state ->
            val passwordError = when {
                password.isBlank() -> "La contraseña no puede estar vacía"
                password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                else -> null
            }
            state.copy(password = password, passwordError = passwordError, errorMessage = null)
        }
    }

    fun registrar() {
        val currentState = _uiState.value

        // Validar todos los campos antes de enviar
        val nombreError = if (currentState.nombre.isBlank()) "El nombre no puede estar vacío" else null
        val emailError = when {
            currentState.email.isBlank() -> "El correo no puede estar vacío"
            !currentState.email.matches(emailRegex) -> "Formato de correo no válido"
            else -> null
        }
        val passwordError = when {
            currentState.password.isBlank() -> "La contraseña no puede estar vacía"
            currentState.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        if (nombreError != null || emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    nombreError = nombreError,
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.registrar(
                nombre = currentState.nombre.trim(),
                email = currentState.email.trim(),
                password = currentState.password
            )
            result.fold(
                onSuccess = { usuario ->
                    // Auto-login: guardar sesión igual que LoginViewModel
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
