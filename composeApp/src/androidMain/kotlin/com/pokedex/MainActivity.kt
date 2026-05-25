// Caminho: composeApp/src/androidMain/kotlin/com/pokedex/MainActivity.kt
package com.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Passa o context para que o Room possa abrir o banco no Android
            App(context = this)
        }
    }
}
