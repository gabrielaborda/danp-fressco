package com.example.danpfressco.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CarritoItemCreateDto(
    @Json(name = "lote_id") val loteId: Int,
    val cantidad: Int
)

@JsonClass(generateAdapter = true)
data class CarritoItemUpdateDto(
    val cantidad: Int
)

@JsonClass(generateAdapter = true)
data class CarritoItemResponseDto(
    val id: Int,
    @Json(name = "lote_id") val loteId: Int,
    val cantidad: Int,
    @Json(name = "precio_aplicado") val precioAplicado: String,
    @Json(name = "agregado_en") val agregadoEn: String,
    @Json(name = "nombre_producto") val nombreProducto: String?,
    @Json(name = "imagen_url") val imagenUrl: String?,
    val subtotal: String?
)

@JsonClass(generateAdapter = true)
data class CarritoResponseDto(
    val items: List<CarritoItemResponseDto>,
    val total: String
)
