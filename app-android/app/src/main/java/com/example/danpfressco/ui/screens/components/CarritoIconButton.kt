package com.example.danpfressco.ui.screens.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.danpfressco.ui.viewmodel.CarritoViewModel

@Composable
fun CarritoIconButton(
    onClick: () -> Unit,
    viewModel: CarritoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cantidadTotal = uiState.cantidadTotal

    BadgedBox(
        badge = {
            if (cantidadTotal > 0) {
                Badge { Text(text = "$cantidadTotal") }
            }
        }
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Carrito"
            )
        }
    }
}
