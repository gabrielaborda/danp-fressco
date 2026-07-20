package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.OfertaProducto
import com.example.danpfressco.data.remote.ApiService
import com.example.danpfressco.data.remote.mapper.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductoRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProductoRepository {

    private var cachedOfertas: List<OfertaProducto> = emptyList()

    override suspend fun obtenerOfertas(): Result<List<OfertaProducto>> {
        return try {
            val productosDto = apiService.getProductos()
            val lotesDto = apiService.getLotesDisponibles()

            val productosMap = productosDto
                .map { it.toDomain() }
                .associateBy { it.id }

            val ofertas = lotesDto.mapNotNull { loteDto ->
                val producto = productosMap[loteDto.productoId.toString()]
                if (producto != null) {
                    OfertaProducto(
                        producto = producto,
                        lote = loteDto.toDomain()
                    )
                } else {
                    null
                }
            }

            cachedOfertas = ofertas
            Result.success(ofertas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerOfertaPorId(loteId: String): Result<OfertaProducto?> {
        val oferta = cachedOfertas.find { it.lote.id == loteId }
        return Result.success(oferta)
    }
}
