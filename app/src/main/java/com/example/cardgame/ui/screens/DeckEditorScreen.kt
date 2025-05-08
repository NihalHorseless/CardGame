package com.example.cardgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.cardgame.R
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.FortificationCard
import com.example.cardgame.data.model.card.TacticCard
import com.example.cardgame.data.model.card.UnitCard
import com.example.cardgame.data.repository.DeckBuilderRepository
import com.example.cardgame.ui.components.board.FortificationTypeIcon
import com.example.cardgame.ui.components.board.TacticTypeIcon
import com.example.cardgame.ui.components.board.UnitTypeIcon
import com.example.cardgame.ui.theme.bloodDropShape
import com.example.cardgame.ui.theme.libreFont
import com.example.cardgame.ui.viewmodel.DeckBuilderViewModel
import kotlinx.coroutines.launch

@Composable
fun DeckEditorScreen(
    viewModel: DeckBuilderViewModel,
    deckId: String,
    onNavigateBack: () -> Unit
) {
    // State
    val availableCards by viewModel.filteredCards
    val currentDeckCards = viewModel.currentDeckCards
    val searchQuery by viewModel.searchQuery

    // Selected card for detail view
    var selectedCard by remember { mutableStateOf<Card?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Deck name and description for new decks
    var deckName by remember { mutableStateOf("") }
    var deckDescription by remember { mutableStateOf("") }

    // Track if we're creating a new deck
    var isCreatingNewDeck by remember { mutableStateOf(deckId == "new") }

    // If editing existing deck, load it
    LaunchedEffect(deckId) {
        viewModel.playEditorMusic()
        if (deckId != "new") {
            viewModel.editDeck(deckId)
        } else {
            // Start with new deck dialog
            viewModel.exitDeckEditor() // Clear any previous state
        }
    }

    // For new decks, show name/description dialog
    var showNewDeckDialog by remember { mutableStateOf(isCreatingNewDeck) }

    // Load available cards
    LaunchedEffect(Unit) {
        viewModel.loadAvailableCards()
    }

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.playEditorMusic()
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.stopMusic()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // Main layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1F2233),
                        Color(0xFF2D3250)
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.available_cards_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // Header with back button, title, and save button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                // Search bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .fillMaxWidth(0.5f)
                )

                // Save button
                Button(
                    onClick = {
                        if (isCreatingNewDeck) {
                            // For a new deck, show the dialog
                            showNewDeckDialog = true
                        } else {
                            // For an existing deck, just save
                            viewModel.saveCurrentDeck()
                        }
                    },
                    enabled = viewModel.isCurrentDeckValid() || isCreatingNewDeck,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8E6432),
                        disabledContainerColor = Color(0xFF422D1E).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .width(80.dp)
                        .border(
                            width = 4.dp,
                            color = Color(0xFF754311),
                            shape = RectangleShape
                        ),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.size(8.dp))

            // Card count indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Cards",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = libreFont,
                    color = Color(0xFF3A1E07)
                )

            }
            Spacer(modifier = Modifier.size(16.dp))


            // Card browser with cards and pagination
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // Left arrow (if needed)
                val gridState = rememberLazyGridState()
                var currentPage by remember { mutableIntStateOf(0) }
                val itemsPerPage = 12 // 3x3 grid
                val totalPages = (availableCards.size + itemsPerPage - 1) / itemsPerPage

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (currentPage > 0) {
                                currentPage--
                                // Scroll to top
                                gridState.scrollToItem(0)
                                viewModel.playMenuScrollSound()
                            }
                        }
                    },
                    enabled = currentPage > 0,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Page",
                        tint = if (currentPage > 0) Color.White else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Card grid
                CardGrid(
                    cards = availableCards.drop(currentPage * itemsPerPage).take(itemsPerPage),
                    onCardClick = { selectedCard = it },
                    gridState = gridState,
                    modifier = Modifier.weight(1f)
                )

                // Right arrow (if needed)
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (currentPage < totalPages - 1) {
                                currentPage++
                                // Scroll to top
                                gridState.scrollToItem(0)
                                viewModel.playMenuScrollSound()
                            }
                        }
                    },
                    enabled = currentPage < totalPages - 1,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Page",
                        tint = if (currentPage < totalPages - 1) Color.White else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                // Current deck cards at bottom
                Text(
                    text = "Your Deck: ${currentDeckCards.size}/${DeckBuilderRepository.DECK_SIZE}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = libreFont,
                    color = if (currentDeckCards.size != DeckBuilderRepository.DECK_SIZE)
                        Color(0xFF633E18) else Color.White
                )
            }


            DeckCards(
                cards = currentDeckCards,
                onCardClick = { _, index ->
                    // When clicking an added card, remove it
                    viewModel.removeCardFromDeck(index)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Card detail overlay
        if (selectedCard != null) {
            CardDetailOverlay(
                card = selectedCard!!,
                onDismiss = { selectedCard = null },
                onAddToDeck = {
                    viewModel.addCardToDeck(it)
                },
                isDeckFull = currentDeckCards.size == DeckBuilderRepository.DECK_SIZE
            )
        }
        // New deck dialog
        if (showNewDeckDialog) {
            NewDeckDialog(
                deckName = deckName,
                onDeckNameChange = { deckName = it },
                deckDescription = deckDescription,
                onDeckDescriptionChange = { deckDescription = it },
                onConfirm = {
                    viewModel.createNewDeck(deckName, deckDescription)
                    showNewDeckDialog = false
                    isCreatingNewDeck = false // Mark that we're no longer creating a new deck
                },
                onDismiss = {
                    if (deckId == "new" && !isCreatingNewDeck) {
                        // If this is a new deck and we've already created it once, just close the dialog
                        showNewDeckDialog = false
                    } else if (deckId == "new") {
                        // If this is a new deck and we haven't created it yet, go back
                        onNavigateBack()
                    } else {
                        showNewDeckDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier
            .height(48.dp)
            .background(
                color = Color(0xFFBF9A6A),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF8E6432),
                shape = RoundedCornerShape(8.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun CardGrid(
    cards: List<Card>,
    onCardClick: (Card) -> Unit,
    gridState: LazyGridState,
    modifier: Modifier = Modifier
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        modifier = modifier
    ) {
        items(cards) { card ->
            CompactCardItem(
                card = card,
                onClick = { onCardClick(card) },
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(0.7f)
            )
        }
    }
}

@Composable
fun CompactCardItem(
    card: Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(2.dp)
            )
            .clip(RoundedCornerShape(2.dp))
            .background(
                Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Card content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.size(12.dp))
            // Card icon
            when (card) {
                is UnitCard -> {
                    UnitTypeIcon(
                        unitType = card.unitType,
                        modifier = Modifier
                            .size(32.dp)
                            .alpha(0.75f)
                    )
                }

                is FortificationCard -> {
                    FortificationTypeIcon(
                        fortType = card.fortType,
                        modifier = Modifier
                            .size(32.dp)
                            .alpha(0.75f)
                    )
                }

                is TacticCard -> {
                    TacticTypeIcon(
                        tacticCardType = card.cardType,
                        modifier = Modifier
                            .size(32.dp)
                            .alpha(0.75f)
                    )
                }

                else -> {
                    // Fallback
                    Icon(
                        painter = painterResource(R.drawable.magic_effect_icon),
                        contentDescription = "Default Card Icon",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Card name
            Text(
                text = card.name,
                color = Color(0xFF3A1E07),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = libreFont,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

        }
    }
}

@Composable
fun DeckCards(
    cards: List<Card>,
    onCardClick: (Card, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFBF9A6A),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF8E6432),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        // Group identical cards and show count
        val cardGroups = cards.groupingBy { it.id }.eachCount()

        // Create a list of unique cards with their counts
        val uniqueCards = cards.distinctBy { it.id }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cards.isEmpty()) {
                Text(
                    text = "Your deck is empty. Add cards from above.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uniqueCards.forEachIndexed { _, card ->
                        val count = cardGroups[card.id] ?: 0

                        // Find the index of the first occurrence of this card
                        val firstIndex = cards.indexOfFirst { it.id == card.id }

                        DeckCardWithCount(
                            card = card,
                            count = count,
                            onClick = { onCardClick(card, firstIndex) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeckCardWithCount(
    card: Card,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .width(70.dp)
            .height(90.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Transparent)
                .clickable(onClick = onClick)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Card content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.size(12.dp))
                // Card name
                Text(
                    text = card.name,
                    color = Color(0xFF3A1E07),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.size(8.dp))

            }
        }
        // Mana cost
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(Color(0xFFC41E3A), bloodDropShape)
                .border(1.dp, Color.White, bloodDropShape)
                .align(Alignment.BottomEnd),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.manaCost.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }

        // Card count badge
        if (count > 1) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopStart)
                    .background(Color(0xFF5271FF), CircleShape)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "x$count",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun CardDetailOverlay(
    card: Card,
    onDismiss: () -> Unit,
    onAddToDeck: (Card) -> Unit,
    isDeckFull: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x88000000))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .clickable { /* Prevent clicks from passing through */ },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFB6803E)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // Card header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = libreFont,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                )

                // Card image/icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (card) {
                        is UnitCard -> {
                            UnitTypeIcon(
                                unitType = card.unitType,
                                modifier = Modifier.size(80.dp)
                            )
                        }

                        is FortificationCard -> {
                            FortificationTypeIcon(
                                fortType = card.fortType,
                                modifier = Modifier.size(80.dp)
                            )
                        }

                        is TacticCard -> {
                            TacticTypeIcon(
                                tacticCardType = card.cardType,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Card details
                Text(
                    text = card.description,
                    fontSize = 14.sp,
                    fontFamily = libreFont,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Card stats
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mana cost
                    StatBadge(
                        label = "Mana",
                        value = card.manaCost.toString(),
                        color = Color(0xFFC41E3A)
                    )

                    // For units and fortifications, show attack and health
                    when (card) {
                        is UnitCard -> {
                            StatBadge(
                                label = "Attack",
                                value = card.attack.toString(),
                                color = Color(0xFFFF9800)
                            )

                            StatBadge(
                                label = "Health",
                                value = card.health.toString(),
                                color = Color(0xFF4CAF50)
                            )

                            if (card.hasCharge) {
                                StatBadge(
                                    label = "Charge",
                                    value = "Yes",
                                    color = Color(0xFFFFC107)
                                )
                            }

                            if (card.hasTaunt) {
                                StatBadge(
                                    label = "Taunt",
                                    value = "Yes",
                                    color = Color(0xFF795548)
                                )
                            }
                        }

                        is FortificationCard -> {
                            StatBadge(
                                label = "Attack",
                                value = card.attack.toString(),
                                color = Color(0xFFFF9800)
                            )

                            StatBadge(
                                label = "Health",
                                value = card.health.toString(),
                                color = Color(0xFF4CAF50)
                            )
                        }

                        is TacticCard -> {
                            // None, it clearly says what it does in the description!
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Add to deck button
                Button(
                    onClick = { onAddToDeck(card) },
                    enabled = !isDeckFull,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDeckFull) "Deck Full" else "Add to Deck",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun NewDeckDialog(
    deckName: String,
    onDeckNameChange: (String) -> Unit,
    deckDescription: String,
    onDeckDescriptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF9D672A)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create New Deck",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Deck name field
                Text(
                    text = "Deck Name",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                TextField(
                    value = deckName,
                    onValueChange = onDeckNameChange,
                    placeholder = {
                        Text("Enter deck name", color = Color.White.copy(alpha = 0.5f))
                    },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF414978),
                        unfocusedContainerColor = Color(0xFF414978),
                        disabledContainerColor = Color(0xFF414978),
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0xFF5271FF),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Deck description field
                Text(
                    text = "Deck Description",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                TextField(
                    value = deckDescription,
                    onValueChange = onDeckDescriptionChange,
                    placeholder = {
                        Text("Enter deck description", color = Color.White.copy(alpha = 0.5f))
                    },
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF414978),
                        unfocusedContainerColor = Color(0xFF414978),
                        disabledContainerColor = Color(0xFF414978),
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF5271FF),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5E5E5E)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = onConfirm,
                        enabled = deckName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

