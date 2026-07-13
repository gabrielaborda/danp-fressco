package com.example.danpfressco.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OfertasEspecialesScreen() {
    Scaffold { innerPadding ->
        Text(
            text = "Ofertas Especiales",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
