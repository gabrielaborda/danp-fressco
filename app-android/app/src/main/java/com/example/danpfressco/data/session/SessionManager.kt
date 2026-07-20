package com.example.danpfressco.data.session

import com.example.danpfressco.data.model.Usuario
import kotlinx.coroutines.flow.Flow

interface SessionManager {
    suspend fun saveSession(user: Usuario)
    fun getSessionToken(): Flow<String?>
    fun getUserName(): Flow<String?>
    suspend fun clearSession()
}
