package com.example.danpfressco.ui.screens.mispedidos

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MisPedidosScreen() {
    Scaffold { innerPadding ->
        Text(
            text = "Mis Pedidos",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
