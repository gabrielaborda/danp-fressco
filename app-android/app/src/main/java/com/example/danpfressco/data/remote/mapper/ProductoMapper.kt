package com.example.danpfressco.data.remote.mapper

import com.example.danpfressco.data.model.Lote
import com.example.danpfressco.data.model.Producto
import com.example.danpfressco.data.remote.dto.LoteResponseDto
import com.example.danpfressco.data.remote.dto.ProductoResponseDto
import java.time.LocalDate

fun ProductoResponseDto.toDomain(): Producto {
    return Producto(
        id = this.id.toString(),
        nombre = this.nombre,
        descripcion = this.descripcion,
        imagenUrl = this.imagenUrl,
        categoria = this.categoria
    )
}

fun LoteResponseDto.toDomain(): Lote {
    val fecha = try {
        LocalDate.parse(this.fechaVencimiento)
    } catch (e: Exception) {
        LocalDate.now()
    }
    return Lote(
        id = this.id.toString(),
        productoId = this.productoId.toString(),
        fechaVencimiento = fecha,
        cantidadRestante = this.cantidad,
        precioDescuento = this.precioConDescuento.toDoubleOrNull() ?: 0.0,
        nombreTienda = this.nombreTienda,
        precioLote = this.precioLote.toDoubleOrNull() ?: 0.0,
        porcentajeDescuento = this.porcentajeDescuento.toDoubleOrNull() ?: 0.0,
        diasParaVencer = this.diasParaVencer,
        tieneDescuentoManual = this.tieneDescuentoManual
    )
}
