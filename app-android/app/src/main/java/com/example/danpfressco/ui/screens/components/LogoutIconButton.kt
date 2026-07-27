package com.example.danpfressco.ui.screens.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.danpfressco.ui.viewmodel.LogoutViewModel

/**
 * Botón de logout autónomo — mismo patrón que [CarritoIconButton].
 *
 * Gestiona su propio [LogoutViewModel] internamente (SRP): cada pantalla que
 * lo incluya no necesita conocer nada de sesión ni agregar dependencias al
 * ViewModel de su pantalla.
 *
 * @param onLogout Lambda de navegación invocada tras el logout exitoso.
 *                 Debe limpiar el back stack completo (popUpTo(0) inclusive).
 */
@Composable
fun LogoutIconButton(
    onLogout: () -> Unit,
    viewModel: LogoutViewModel = hiltViewModel()
) {
    val logoutDone by viewModel.logoutDone.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    // Gatillo de navegación — análogo al LaunchedEffect en LoginScreen
    LaunchedEffect(logoutDone) {
        if (logoutDone) onLogout()
    }

    IconButton(onClick = { mostrarDialogo = true }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = "Cerrar sesión",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                        viewModel.logout()
                    }
                ) {
                    Text(
                        "Cerrar sesión",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
