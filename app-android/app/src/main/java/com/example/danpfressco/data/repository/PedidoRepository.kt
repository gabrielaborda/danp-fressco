package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.ItemCarrito
import com.example.danpfressco.data.model.Pedido
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato del repositorio de pedidos.
 * Los ViewModels dependen únicamente de esta interfaz (Dependency Inversion).
 * En Etapa 2/3, [PedidoRepositoryImpl] se reemplazará por una implementación
 * Retrofit sin alterar esta interfaz.
 */
interface PedidoRepository {
    /** Lista reactiva de todos los pedidos creados en esta sesión. */
    val pedidos: StateFlow<List<Pedido>>

    /**
     * Crea un nuevo pedido a partir de los [items] actuales del carrito y los
     * datos de contacto provistos.
     *
     * @return [Result.success] con el [Pedido] creado, o [Result.failure] si
     *         ocurre algún error inesperado.
     */
    suspend fun crearPedido(
        items: List<ItemCarrito>,
        nombreContacto: String,
        telefonoContacto: String,
        horarioRecogida: String
    ): Result<Pedido>
}
