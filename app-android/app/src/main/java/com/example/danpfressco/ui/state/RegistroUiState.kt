package com.example.danpfressco.ui.state

data class RegistroUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val nombreError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,  // error del servidor (email duplicado, etc.)
    val isSuccess: Boolean = false     // gatillo para navegación tras registro exitoso
)
