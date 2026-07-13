package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.Usuario
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    // Lista mutable para poder agregar usuarios registrados en tiempo de ejecución
    private val users: MutableList<Usuario> = mutableListOf(
        Usuario(id = "1", nombre = "Administrador Fressco", email = "admin@fressco.com", token = "mock-jwt-token-123"),
        Usuario(id = "2", nombre = "Juan Pérez", email = "cliente@fressco.com", token = "mock-jwt-token-123")
    )

    // Mapa en memoria: email (lowercase) → password en texto plano
    // Permite validar login() contra usuarios registrados en la misma sesión
    private val passwords: MutableMap<String, String> = mutableMapOf(
        "admin@fressco.com" to "admin123",
        "cliente@fressco.com" to "cliente123"
    )

    override suspend fun login(email: String, password: String): Result<Usuario> {
        delay(1500) // Simular latencia de red

        val normalizedEmail = email.trim().lowercase()
        val user = users.find { it.email.lowercase() == normalizedEmail }

        return if (user != null) {
            val expectedPassword = passwords[normalizedEmail]
            if (expectedPassword != null && password == expectedPassword) {
                Result.success(user)
            } else {
                Result.failure(Exception("Contraseña incorrecta"))
            }
        } else {
            Result.failure(Exception("El correo ingresado no está registrado"))
        }
    }

    override suspend fun registrar(nombre: String, email: String, password: String): Result<Usuario> {
        delay(1200) // Simular latencia de red

        val normalizedEmail = email.trim().lowercase()

        // Verificar si el email ya existe
        if (users.any { it.email.lowercase() == normalizedEmail }) {
            return Result.failure(Exception("Este correo ya está registrado"))
        }

        val nuevoUsuario = Usuario(
            id = UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            email = email.trim(),
            token = "mock-jwt-token-123"
        )

        users.add(nuevoUsuario)
        passwords[normalizedEmail] = password // Persistir password para login posterior

        return Result.success(nuevoUsuario)
    }
}
