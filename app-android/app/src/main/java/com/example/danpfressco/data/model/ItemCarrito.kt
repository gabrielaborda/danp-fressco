package com.example.danpfressco.data.model

data class ItemCarrito(
    val id: String,
    val loteId: String,
    val nombreProducto: String,
    val imagenUrl: String?,
    val cantidad: Int,
    val precioAplicado: Double,
    val subtotal: Double,
    val cantidadRestanteAprox: Int? = null
)
