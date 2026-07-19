package com.example.danpfressco.data.model

import java.time.LocalDateTime

/**
 * Estado del ciclo de vida de un pedido.
 * En Etapa 1 solo se usa [PENDIENTE]; los demás valores se preparan para Etapas 2/3
 * cuando el backend maneje transiciones de estado.
 */
enum class EstadoPedido {
    PENDIENTE,    // Recién creado, esperando confirmación/recogida en tienda
    CANCELADO,    // Cancelado por el cliente o por la tienda
    ENTREGADO     // El cliente ya recogió el pedido en tienda
}

/**
 * Cabecera de un pedido confirmado.
 * Contiene los datos de contacto, el nombre de la tienda (snapshot), y la lista
 * de [DetallePedido] con precios snapshots incluidos.
 */
data class Pedido(
    val id: String,                        // UUID generado localmente en Etapa 1
    val nombreContacto: String,
    val telefonoContacto: String,
    val horarioRecogida: String,           // Ej. "11:00–13:00"
    val nombreTienda: String,              // snapshot del nombre de tienda al confirmar
    val fechaCreacion: LocalDateTime,
    val estado: EstadoPedido,
    val detalles: List<DetallePedido>
)
