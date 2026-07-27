package com.example.danpfressco.ui.state

import com.example.danpfressco.data.model.OfertaProducto

data class PrincipalUiState(
    val isLoading: Boolean = true,
    val ofertas: List<OfertaProducto> = emptyList(),
    val errorMessage: String? = null
)
