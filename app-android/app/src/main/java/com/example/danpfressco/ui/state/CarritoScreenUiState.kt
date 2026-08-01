package com.example.danpfressco.ui.state

import com.example.danpfressco.data.model.ItemCarrito

data class CarritoScreenUiState(
    val items: List<ItemCarrito> = emptyList(),
    val total: Double = 0.0,
    val cantidadTotal: Int = 0
)
