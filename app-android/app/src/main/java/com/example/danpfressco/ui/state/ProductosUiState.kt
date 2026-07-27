package com.example.danpfressco.ui.state

import com.example.danpfressco.data.model.OfertaProducto

data class ProductosUiState(
    val isLoading: Boolean = true,
    val oferta: OfertaProducto? = null,
    val errorMessage: String? = null,
    val mostrarDialogoCambioTienda: Boolean = false,
    val tiendaEnCarrito: String = "",
    val tiendaNueva: String = "",
    val ofertaPendiente: OfertaProducto? = null,
    val cantidadPendiente: Int = 0
)
