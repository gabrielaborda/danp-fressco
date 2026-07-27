package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.MetodoPago

/**
 * Contrato del repositorio de pagos simulados.
 *
 * [PagoViewModel] depende únicamente de esta interfaz (Dependency Inversion).
 * Si el backend expone un endpoint real de pagos en el futuro, solo se
 * reemplaza [PagoRepositoryImpl] sin tocar el ViewModel ni la UI.
 */
interface PagoRepository {
    /**
     * Simula el procesamiento de un pago.
     *
     * @param metodo El método de pago elegido por el usuario.
     * @param monto El total a cobrar, calculado a partir del carrito vigente.
     * @return [Result.success] si el pago fue procesado, [Result.failure] con un
     *         mensaje descriptivo si falló.
     */
    suspend fun procesarPago(metodo: MetodoPago, monto: Double): Result<Unit>
}
