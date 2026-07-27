package com.example.danpfressco.ui.state

import com.example.danpfressco.data.model.Pedido

/**
 * Representa un pedido con sus datos pre-formateados para la UI.
 */
data class PedidoItemUiState(
    val pedido: Pedido,
    val fechaFormateada: String,
    val totalFormateado: String
)

/**
 * Estado completo de la pantalla Mis Pedidos.
 * La lista [pedidos] ya viene ordenada desde el ViewModel.
 */
data class MisPedidosUiState(
    val pedidos: List<PedidoItemUiState> = emptyList(),
    val isLoading: Boolean = false
)
