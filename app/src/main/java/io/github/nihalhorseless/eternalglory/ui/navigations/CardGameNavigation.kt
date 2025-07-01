package io.github.nihalhorseless.eternalglory.ui.navigations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.nihalhorseless.eternalglory.ui.screens.CampaignSelectionScreen
import io.github.nihalhorseless.eternalglory.ui.screens.CreditsScreen
import io.github.nihalhorseless.eternalglory.ui.screens.DeckBuilderScreen
import io.github.nihalhorseless.eternalglory.ui.screens.DeckEditorScreen
import io.github.nihalhorseless.eternalglory.ui.screens.DeckSelectionScreen
import io.github.nihalhorseless.eternalglory.ui.screens.GameScreen
import io.github.nihalhorseless.eternalglory.ui.screens.GuideScreen
import io.github.nihalhorseless.eternalglory.ui.screens.LevelSelectionScreen
import io.github.nihalhorseless.eternalglory.ui.screens.MainMenuScreen
import io.github.nihalhorseless.eternalglory.ui.viewmodel.DeckBuilderViewModel
import io.github.nihalhorseless.eternalglory.ui.viewmodel.GameViewModel
import io.github.nihalhorseless.eternalglory.ui.viewmodel.GameViewModelFactory

@Composable
fun CardGameNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Create ViewModel factory using the companion object method
    val factory = remember { GameViewModelFactory.create(context) }

    // Create ViewModels using the factory
    val gameViewModel: GameViewModel = viewModel(factory = factory)
    val deckBuilderViewModel: DeckBuilderViewModel = viewModel(factory = factory)

    // Get the music mute state from the MusicManager
    val isMusicMuted by gameViewModel.isMusicMuted

    // Function to toggle mute state
    val toggleMusicMute = {
        gameViewModel.toggleMusicMute()
    }

    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(
                onIntro = {
                    gameViewModel.playScreenMusic("main_menu")
                },
                onLeaveGame = {
                    gameViewModel.stopMusic()
                },
                onStartGame = {
                    navController.navigate("deck_selection")
                    gameViewModel.stopMusic()
                    gameViewModel.playMenuSoundOne()
                    gameViewModel.resetWin()
                },
                onShowDeckBuilder = {
                    navController.navigate("deck_builder")
                    gameViewModel.stopMusic()
                    gameViewModel.playMenuSoundOne()
                },
                onShowGuide = {
                    navController.navigate("guide")
                    gameViewModel.playMenuSoundOne()
                    gameViewModel.stopMusic()
                },
                onShowCredits = {
                    navController.navigate("credits")
                    gameViewModel.playMenuSoundOne()
                    gameViewModel.stopMusic()
                },
                onShowCampaign = {
                    navController.navigate("campaign_selection")
                    gameViewModel.stopMusic()
                    gameViewModel.playMenuSoundOne()
                    gameViewModel.resetWin()
                },
                isMusicMuted = isMusicMuted,
                onToggleMusicMute = toggleMusicMute
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
                    playerDeckNames =  gameViewModel.availableDeckNames.value,
                    selectedDeck = gameViewModel.selectedPlayerDeck.value,
                    onDeckSelected = { deckName ->
                        gameViewModel.setPlayerDeck(deckName)
                        gameViewModel.playMenuSoundOne()
                    },
                    onLevelSelected = { level ->
                        gameViewModel.startCampaignLevel(campaign.id, level.id)
                        gameViewModel.stopMusic()
                        gameViewModel.playStartBattleSound()
                        navController.navigate("game")
                    }, onLevelScroll = {
                        gameViewModel.playMenuScrollSound()
                    },
                    onBackPressed = {
                        navController.popBackStack()
                        gameViewModel.stopMusic()
                        gameViewModel.playMenuSoundOne()
                    },
                    onInitial = {
                        gameViewModel.playScreenMusic("level_selection")
                        gameViewModel.loadAvailableDecks()
                    },
                    onLeaveGame = {
                        gameViewModel.stopMusic()
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
                onStartGame = {
                    navController.navigate("game")
                    gameViewModel.playMenuSoundTwo()
                }
            )
        }

        composable("game") {
            GameScreen(
                viewModel = gameViewModel,
                onNavigateToMain = {
                    if (gameViewModel.isInCampaign.value) {
                        gameViewModel.exitCampaignLevel()
                        navController.navigate("main_menu") {
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
            DeckBuilderScreen(
                viewModel = deckBuilderViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = { deckId ->
                    // Navigate to editor screen with or without a deck ID
                    if (deckId != null) {
                        navController.navigate("deck_editor/$deckId")
                    } else {
                        navController.navigate("deck_editor/new")
                    }
                    gameViewModel.playMenuSoundTwo()
                }
            )
        }
        composable(
            "deck_editor/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: "new"

            DeckEditorScreen(
                viewModel = deckBuilderViewModel,
                deckId = deckId,
                onNavigateBack = {
                    navController.popBackStack()
                    gameViewModel.playMenuSoundOne()
                    gameViewModel.stopMusic()
                }
            )
        }
        composable("guide") {
            GuideScreen(
                onBackPressed = {
                    navController.popBackStack()
                    gameViewModel.playMenuSoundOne()
                }
            )
        }
        composable("credits") {
            CreditsScreen(
                onBackPressed = {
                    navController.popBackStack()
                    gameViewModel.playMenuSoundOne()
                }
            )
        }
    }
}