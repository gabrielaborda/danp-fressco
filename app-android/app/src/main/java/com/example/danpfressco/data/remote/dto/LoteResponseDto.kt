package com.example.danpfressco.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoteResponseDto(
    val id: Int,
    @Json(name = "producto_id") val productoId: Int,
    val cantidad: Int,
    @Json(name = "fecha_vencimiento") val fechaVencimiento: String,
    @Json(name = "precio_lote") val precioLote: String,
    @Json(name = "precio_con_descuento") val precioConDescuento: String,
    @Json(name = "porcentaje_descuento") val porcentajeDescuento: String,
    @Json(name = "dias_para_vencer") val diasParaVencer: Int,
    @Json(name = "tiene_descuento_manual") val tieneDescuentoManual: Boolean,
    @Json(name = "nombre_producto") val nombreProducto: String,
    @Json(name = "nombre_tienda") val nombreTienda: String,
    val categoria: String
)
