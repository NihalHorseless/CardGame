package com.example.cardgame.ui.navigations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cardgame.ui.screens.GameScreen
import com.example.cardgame.ui.screens.MainMenuScreen
import com.example.cardgame.ui.viewmodel.GameViewModel
import com.example.cardgame.ui.viewmodel.GameViewModelFactory

@Composable
fun CardGameNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(
                onStartGame = { navController.navigate("game") },
                onShowDeckBuilder = { navController.navigate("deck_builder") },
                onShowOptions = { navController.navigate("options") }
            )
        }

        composable("game") {
            val gameViewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(navController.context)
            )

            GameScreen(viewModel = gameViewModel, onNavigateToMain = {
                navController.popBackStack(
                    route = "main_menu",
                    inclusive = false
                )
            })
        }

        composable("deck_builder") {
            // Deck builder screen would go here
           // Text("Deck Builder - Not Implemented Yet")
        }

        composable("options") {
            // Options screen would go here
          //  Text("Options - Not Implemented Yet")
        }
    }
}