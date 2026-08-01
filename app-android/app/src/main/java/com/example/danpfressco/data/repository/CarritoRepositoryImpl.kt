package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.ItemCarrito
import com.example.danpfressco.data.remote.ApiService
import com.example.danpfressco.data.remote.dto.CarritoItemCreateDto
import com.example.danpfressco.data.remote.dto.CarritoItemUpdateDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarritoRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val productoRepository: ProductoRepository
) : CarritoRepository {

    private val _items = MutableStateFlow<List<ItemCarrito>>(emptyList())
    override val items: StateFlow<List<ItemCarrito>> = _items.asStateFlow()

    override suspend fun cargarCarrito(): Result<Unit> {
        return try {
            val response = apiService.getCarrito()
            val listItems = response.items.map { dto ->
                val oferta = productoRepository.obtenerOfertaPorId(dto.loteId.toString()).getOrNull()
                val cantidadRestante = oferta?.lote?.cantidadRestante
                ItemCarrito(
                    id = dto.id.toString(),
                    loteId = dto.loteId.toString(),
                    nombreProducto = dto.nombreProducto ?: (oferta?.producto?.nombre ?: "Producto"),
                    imagenUrl = dto.imagenUrl ?: oferta?.producto?.imagenUrl,
                    cantidad = dto.cantidad,
                    precioAplicado = dto.precioAplicado.toDoubleOrNull() ?: (oferta?.lote?.precioDescuento ?: 0.0),
                    subtotal = dto.subtotal?.toDoubleOrNull() ?: ((dto.precioAplicado.toDoubleOrNull() ?: 0.0) * dto.cantidad),
                    cantidadRestanteAprox = cantidadRestante
                )
            }
            _items.value = listItems
            Result.success(Unit)
        } catch (e: HttpException) {
            val message = if (e.code() == 409) {
                "Stock insuficiente o no se pudo completar la operación"
            } else {
                "Error del servidor, intenta de nuevo"
            }
            Result.failure(Exception(message, e))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión, intenta de nuevo", e))
        }
    }

    override suspend fun agregarItem(loteId: String, cantidad: Int): Result<Unit> {
        return try {
            val loteIdInt = loteId.toIntOrNull() ?: return Result.failure(Exception("ID de lote inválido"))
            apiService.agregarAlCarrito(CarritoItemCreateDto(loteId = loteIdInt, cantidad = cantidad))
            cargarCarrito()
        } catch (e: HttpException) {
            val message = if (e.code() == 409) {
                "Stock insuficiente o no se pudo completar la operación"
            } else {
                "Error del servidor, intenta de nuevo"
            }
            Result.failure(Exception(message, e))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión, intenta de nuevo", e))
        }
    }

    override suspend fun actualizarCantidad(itemId: String, nuevaCantidad: Int): Result<Unit> {
        return try {
            val itemIdInt = itemId.toIntOrNull() ?: return Result.failure(Exception("ID de item inválido"))
            apiService.actualizarItemCarrito(itemIdInt, CarritoItemUpdateDto(cantidad = nuevaCantidad))
            cargarCarrito()
        } catch (e: HttpException) {
            val message = if (e.code() == 409) {
                "Stock insuficiente o no se pudo completar la operación"
            } else {
                "Error del servidor, intenta de nuevo"
            }
            Result.failure(Exception(message, e))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión, intenta de nuevo", e))
        }
    }

    override suspend fun eliminarItem(itemId: String): Result<Unit> {
        return try {
            val itemIdInt = itemId.toIntOrNull() ?: return Result.failure(Exception("ID de item inválido"))
            apiService.eliminarItemCarrito(itemIdInt)
            cargarCarrito()
        } catch (e: HttpException) {
            val message = if (e.code() == 409) {
                "Stock insuficiente o no se pudo completar la operación"
            } else {
                "Error del servidor, intenta de nuevo"
            }
            Result.failure(Exception(message, e))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión, intenta de nuevo", e))
        }
    }

    override suspend fun vaciarCarrito(): Result<Unit> {
        return try {
            apiService.vaciarCarritoRemoto()
            _items.value = emptyList()
            Result.success(Unit)
        } catch (e: HttpException) {
            val message = if (e.code() == 409) {
                "Stock insuficiente o no se pudo completar la operación"
            } else {
                "Error del servidor, intenta de nuevo"
            }
            Result.failure(Exception(message, e))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión, intenta de nuevo", e))
        }
    }
}
