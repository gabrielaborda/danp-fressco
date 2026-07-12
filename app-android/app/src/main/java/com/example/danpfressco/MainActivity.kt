package com.example.danpfressco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.danpfressco.ui.navigation.FresscoNavGraph
import com.example.danpfressco.ui.theme.DANPFresscoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DANPFresscoTheme {
                FresscoNavGraph()
            }
        }
    }
}
