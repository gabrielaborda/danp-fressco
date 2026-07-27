package com.example.danpfressco.data.model

import java.time.LocalDate

data class Lote(
    val id: String,
    val productoId: String,
    val fechaVencimiento: LocalDate,
    val cantidadRestante: Int,
    val precioDescuento: Double,
    // TODO: En Etapa 2/3, esto debe convertirse en tiendaId: String (referenciando la entidad Tienda real del backend)
    // NOTA: Sigue como nombreTienda debido a que el backend (en el endpoint /lotes-disponibles) solo envía "nombre_tienda" y no "tienda_id".
    val nombreTienda: String,
    val precioLote: Double,
    val porcentajeDescuento: Double,
    val diasParaVencer: Int,
    val tieneDescuentoManual: Boolean
)
