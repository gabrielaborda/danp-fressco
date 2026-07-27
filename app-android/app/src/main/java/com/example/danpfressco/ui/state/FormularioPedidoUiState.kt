package com.example.danpfressco.ui.state

/**
 * Estado completo de la pantalla Formulario de Pedido.
 *
 * Sigue el mismo patrón que [LoginUiState] y [RegistroUiState]:
 * - Campos de entrada con su valor actual
 * - Error nullable por campo (null = sin error)
 * - Flags de estado de la operación
 * - [pedidoConfirmado] actúa como gatillo de navegación (análogo a isSuccess)
 */
data class FormularioPedidoUiState(
    // Campos del formulario
    val nombreContacto: String = "",
    val telefonoContacto: String = "",
    val horarioRecogida: String? = null,      // null = no seleccionado aún

    // Errores de validación en tiempo real (null = sin error)
    val nombreError: String? = null,
    val telefonoError: String? = null,
    val horarioError: String? = null,

    // Estado de la operación
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pedidoConfirmado: Boolean = false     // gatillo de navegación a Mis Pedidos
)
