package com.example.podlodka.fpsample.cleanarchitecture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import com.example.podlodka.fpsample.theme.FPSampleTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      FPSampleTheme {
        Box {}
      }
    }
  }
}