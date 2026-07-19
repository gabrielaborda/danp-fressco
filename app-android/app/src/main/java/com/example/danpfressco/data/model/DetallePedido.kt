package com.example.danpfressco.data.model

/**
 * Línea de un pedido confirmado.
 *
 * [precioPagadoUnitario] es un **snapshot** del precio al momento de confirmar el
 * pedido. Nunca se recalcula desde [Lote.precioDescuento] después; así el historial
 * en Mis Pedidos siempre refleja lo que realmente pagó el usuario.
 */
data class DetallePedido(
    val loteId: String,
    val productoId: String,
    val nombreProducto: String,           // snapshot del nombre en el momento del pedido
    val precioPagadoUnitario: Double,     // snapshot de Lote.precioDescuento al confirmar
    val cantidad: Int,
    val subtotal: Double                  // precioPagadoUnitario * cantidad, calculado al crear
)
