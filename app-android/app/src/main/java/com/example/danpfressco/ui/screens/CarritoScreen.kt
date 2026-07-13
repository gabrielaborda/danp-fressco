package com.example.danpfressco.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun CarritoScreen() {
    Scaffold { innerPadding ->
        Text(
            text = "Carrito",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
