package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.MetodoPago
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación simulada del repositorio de pagos — 100% local, sin Retrofit.
 *
 * Comportamiento de la simulación:
 * - Introduce un delay de 1 500 ms para emular latencia de red.
 * - Cada 5.º intento retorna [Result.failure] con un mensaje descriptivo,
 *   lo que permite demostrar el manejo de error en la sustentación de forma
 *   reproducible (a diferencia de un fallo aleatorio).
 *
 * Cuando se integre un endpoint real de pagos, solo se reemplaza esta clase
 * por una implementación Retrofit, sin tocar [PagoRepository] ni [PagoViewModel].
 */
@Singleton
class PagoRepositoryImpl @Inject constructor() : PagoRepository {

    private var intentoCount = 0

    override suspend fun procesarPago(metodo: MetodoPago, monto: Double): Result<Unit> {
        // Simula latencia de red
        delay(1_500)

        intentoCount++

        // Cada 5.º intento simula un rechazo (demostrable en sustentación)
        if (intentoCount % 5 == 0) {
            return Result.failure(
                Exception("Pago rechazado por el banco. Intenta con otro método o tarjeta.")
            )
        }

        return Result.success(Unit)
    }
}
