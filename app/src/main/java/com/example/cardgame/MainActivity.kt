package com.example.cardgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cardgame.ui.navigations.CardGameNavigation
import com.example.cardgame.ui.viewmodel.GameViewModel
import com.example.cardgame.ui.screens.GameScreen
import com.example.cardgame.ui.theme.CardGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CardGameTheme {
                CardGameNavigation()
            }
        }
    }
}

