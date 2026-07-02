package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SantoGuiaApp
import com.example.ui.SantoGuiaViewModel
import com.example.ui.theme.SantoGuiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SantoGuiaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val viewModel: SantoGuiaViewModel = viewModel()
                    SantoGuiaApp(viewModel = viewModel)
                }
            }
        }
    }
}
