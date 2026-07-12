package com.example.danpfressco.ui.screens.registro

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RegistroScreen() {
    Scaffold { innerPadding ->
        Text(
            text = "Registro",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
