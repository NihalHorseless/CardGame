package com.example.cardgame.ui.navigations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cardgame.ui.screens.CampaignSelectionScreen
import com.example.cardgame.ui.screens.DeckSelectionScreen
import com.example.cardgame.ui.screens.GameScreen
import com.example.cardgame.ui.screens.LevelSelectionScreen
import com.example.cardgame.ui.screens.MainMenuScreen
import com.example.cardgame.ui.viewmodel.GameViewModel
import com.example.cardgame.ui.viewmodel.GameViewModelFactory

@Composable
fun CardGameNavigation() {
    val navController = rememberNavController()
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(navController.context)
    )

    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(
                onStartGame = {
                    navController.navigate("deck_selection")
                    gameViewModel.playMenuSoundOne()
                },
                onShowDeckBuilder = {
                    navController.navigate("deck_builder")
                    gameViewModel.playMenuSoundOne()
                },
                onShowOptions = {
                    navController.navigate("options")
                    gameViewModel.playMenuSoundOne()
                },
                onShowCampaign = {
                    navController.navigate("campaign_selection")
                    gameViewModel.playMenuSoundOne()
                }
            )
        }
        composable("campaign_selection") {
            CampaignSelectionScreen(
                campaigns = gameViewModel.loadAvailableCampaigns(),
                onCampaignSelected = { campaign ->
                    gameViewModel.setCurrentCampaign(campaign)
                    navController.navigate("campaign_level_selection")
                    gameViewModel.playMenuSoundTwo()
                },
                onBackPressed = {
                    navController.popBackStack()
                    gameViewModel.playMenuSoundOne()
                }
            )
        }

        composable("campaign_level_selection") {
            val campaign = gameViewModel.currentCampaign.value
            if (campaign != null) {
                LevelSelectionScreen(
                    campaign = campaign,
                    playerDecks = gameViewModel.availableDecks.value,
                    selectedDeck = gameViewModel.selectedPlayerDeck.value,
                    onDeckSelected = { deckName ->
                        gameViewModel.setPlayerDeck(deckName)
                        gameViewModel.playMenuSoundOne()
                    },
                    onLevelSelected = { level ->
                        gameViewModel.startCampaignLevel(campaign.id, level.id)
                        navController.navigate("game")
                        gameViewModel.playMenuSoundTwo()
                    }, onLevelScroll = {
                        gameViewModel.playMenuScrollSound()
                    },
                    onBackPressed = {
                        navController.popBackStack()
                        gameViewModel.playMenuSoundOne()
                    }
                )
            } else {
                // Handle missing campaign
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable("deck_selection") {
            DeckSelectionScreen(
                viewModel = gameViewModel,
                onStartGame = { navController.navigate("game")
                    gameViewModel.playMenuSoundTwo()}
            )
        }

        composable("game") {
            GameScreen(
                viewModel = gameViewModel,
                onNavigateToMain = {
                    if (gameViewModel.isInCampaign.value) {
                        gameViewModel.exitCampaignLevel()
                        navController.navigate("campaign_level_selection") {
                            popUpTo("main_menu") {
                                saveState = true
                            }
                        }
                    } else {
                        navController.popBackStack(
                            route = "main_menu",
                            inclusive = false
                        )
                    }
                    gameViewModel.playMenuSoundTwo()
                }
            )
        }

        composable("deck_builder") {
            // Deck builder screen would go here
            // Text("Deck Builder - Not Implemented Yet")
        }

        composable("options") {
            // Options screen would go here
            // Text("Options - Not Implemented Yet")
        }
    }
}