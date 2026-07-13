package com.example.danpfressco.data.model

import java.time.LocalDate

data class Lote(
    val id: String,
    val productoId: String,
    val fechaVencimiento: LocalDate,
    val cantidadRestante: Int,
    val precioDescuento: Double,
    // TODO: En Etapa 2/3, esto debe convertirse en tiendaId: String (referenciando la entidad Tienda real del backend)
    val nombreTienda: String
)
