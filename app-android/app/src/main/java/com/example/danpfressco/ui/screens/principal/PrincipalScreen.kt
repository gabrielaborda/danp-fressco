package com.example.danpfressco.ui.screens.principal

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PrincipalScreen() {
    Scaffold { innerPadding ->
        Text(
            text = "Principal",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
