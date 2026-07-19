package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.OfertaProducto

interface ProductoRepository {
    suspend fun obtenerOfertas(): Result<List<OfertaProducto>>
    suspend fun obtenerOfertaPorId(loteId: String): Result<OfertaProducto?>
}
