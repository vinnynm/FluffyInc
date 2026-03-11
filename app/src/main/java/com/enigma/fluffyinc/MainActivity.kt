package com.enigma.fluffyinc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enigma.fluffyinc.navigation.MainNavigation
import com.enigma.fluffyinc.ui.theme.FluffyIncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            FluffyIncTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0.dp)
                ) { innerPadding ->
                    Column (
                        modifier = Modifier.padding(innerPadding)
                    ){
                        MainNavigation()
                    }
                }
            }
        }
    }
}
