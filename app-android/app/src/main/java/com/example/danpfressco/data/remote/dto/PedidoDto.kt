package com.example.danpfressco.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CrearPedidoRequestDto(
    val notas: String? = null
)

@JsonClass(generateAdapter = true)
data class PedidoItemResponseDto(
    val id: Int,
    @Json(name = "lote_id") val loteId: Int,
    val cantidad: Int,
    @Json(name = "precio_unitario_aplicado") val precioUnitarioAplicado: String,
    val subtotal: String,
    @Json(name = "nombre_producto") val nombreProducto: String?
)

@JsonClass(generateAdapter = true)
data class PedidoResponseDto(
    val id: Int,
    @Json(name = "cliente_id") val clienteId: Int,
    @Json(name = "tienda_id") val tiendaId: Int,
    @Json(name = "fecha_pedido") val fechaPedido: String,
    val estado: String,
    val total: String,
    val notas: String?,
    val items: List<PedidoItemResponseDto>
)
