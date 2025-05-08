package com.example.cardgame.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardgame.audio.MusicManager
import com.example.cardgame.audio.MusicTrack
import com.example.cardgame.audio.SoundManager
import com.example.cardgame.audio.SoundType
import com.example.cardgame.data.model.card.Card
import com.example.cardgame.data.model.card.Deck
import com.example.cardgame.data.repository.DeckBuilderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeckBuilderViewModel(
    private val deckBuilderRepository: DeckBuilderRepository,
    private val soundManager: SoundManager? = null,
    private val musicManager: MusicManager? = null
) : ViewModel() {

    // State for card collection and filtering
    private val _availableCards = mutableStateOf<List<Card>>(emptyList())
    val availableCards: State<List<Card>> = _availableCards

    private val _filteredCards = mutableStateOf<List<Card>>(emptyList())
    val filteredCards: State<List<Card>> = _filteredCards

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    // State for deck list
    private val _playerDecks = mutableStateOf<List<Deck>>(emptyList())
    val playerDecks: State<List<Deck>> = _playerDecks

    // State for currently edited deck
    private val _currentDeck = mutableStateOf<Deck?>(null)
    val currentDeck: State<Deck?> = _currentDeck

    // Cards in current deck (observable list for add/remove operations)
    private val _currentDeckCards = mutableStateListOf<Card>()
    val currentDeckCards: List<Card> = _currentDeckCards

    // Loading state
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Status messages
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    // Initialize with available cards and player decks
    init {
        loadAvailableCards()
        loadPlayerDecks()
    }

    /**
     * Load all available cards
     */
    fun loadAvailableCards() {
        viewModelScope.launch {
            _isLoading.value = true
            val cards = deckBuilderRepository.getAllAvailableCards()
            _availableCards.value = cards
            _filteredCards.value = cards
            _isLoading.value = false
        }
    }

    /**
     * Load all player decks
     */
    fun loadPlayerDecks() {
        viewModelScope.launch {
            _isLoading.value = true
            _playerDecks.value = deckBuilderRepository.getCustomDecks()
            _isLoading.value = false
        }
    }

    /**
     * Set search query and filter cards
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterCards()
    }

    /**
     * Filter cards based on current query and filters
     */
    private fun filterCards() {
        viewModelScope.launch {
            _filteredCards.value = deckBuilderRepository.searchCards(_searchQuery.value)
        }
    }

    /**
     * Start building a new deck
     */
    fun createNewDeck(name: String, description: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val newDeck = deckBuilderRepository.createDeck(name, description)

            if (newDeck != null) {
                setCurrentDeck(newDeck)
                playMenuSoundOne()
                _statusMessage.value = "Created new deck: ${newDeck.name}"
            } else {
                _statusMessage.value = "Cannot create more decks (maximum ${DeckBuilderRepository.MAX_CUSTOM_DECKS})"
            }
            _isLoading.value = false
        }
    }

    /**
     * Load a deck for editing
     */
    fun editDeck(deckId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val deck = deckBuilderRepository.getDeck(deckId)

            if (deck != null) {
                setCurrentDeck(deck)
                _statusMessage.value = "Editing deck: ${deck.name}"
            } else {
                _statusMessage.value = "Deck not found"
            }
            _isLoading.value = false
        }
    }

    /**
     * Set the current deck being edited
     */
    private fun setCurrentDeck(deck: Deck) {
        _currentDeck.value = deck
        _currentDeckCards.clear()
        _currentDeckCards.addAll(deck.cards)
    }

    /**
     * Add a card to the current deck
     */
    fun addCardToDeck(card: Card) {
        if (_currentDeck.value == null) {
            _statusMessage.value = "No deck selected for editing"
            return
        }

        if (_currentDeckCards.size == DeckBuilderRepository.DECK_SIZE) {
            _statusMessage.value = "Deck is already at maximum size"
            return
        }

        playCardAddedSound()
        _currentDeckCards.add(card)
        _statusMessage.value = "Added ${card.name} to deck"
    }

    /**
     * Remove a card from the current deck
     */
    fun removeCardFromDeck(index: Int) {
        if (index < 0 || index >= _currentDeckCards.size) {
            return
        }

        val removedCard = _currentDeckCards.removeAt(index)
        playCardRemovedSound()
        _statusMessage.value = "Removed ${removedCard.name} from deck"
    }

    /**
     * Save the current deck
     */
    fun saveCurrentDeck() {
        val currentDeck = _currentDeck.value ?: return

        // Update deck with current cards
        val updatedDeck = currentDeck.copy(cards = _currentDeckCards.toMutableList())

        // Validate deck
        val validationResult = deckBuilderRepository.validateDeck(updatedDeck)

        if (!validationResult.isValid) {
            _statusMessage.value = validationResult.message
            return
        }

        // Save deck
        viewModelScope.launch {
            _isLoading.value = true
            val success = deckBuilderRepository.saveDeck(updatedDeck)

            if (success) {
                playMenuSoundTwo()
                _statusMessage.value = "Deck saved successfully"
                // Refresh player decks list
                loadPlayerDecks()
            } else {
                _statusMessage.value = "Failed to save deck"
            }
            _isLoading.value = false
        }
    }

    /**
     * Delete a deck
     */
    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = deckBuilderRepository.deleteDeck(deckId)

            if (success) {
                playMenuSoundOne()
                _statusMessage.value = "Deck deleted"

                // Clear current deck if it was the one being edited
                if (_currentDeck.value?.id == deckId) {
                    _currentDeck.value = null
                    _currentDeckCards.clear()
                }

                // Refresh player decks list
                loadPlayerDecks()
            } else {
                _statusMessage.value = "Failed to delete deck"
            }
            _isLoading.value = false
        }
    }

    /**
     * Exit the deck editor without saving
     */
    fun exitDeckEditor() {
        _currentDeck.value = null
        _currentDeckCards.clear()
    }

    /**
     * Update deck name and description
     */
    fun updateDeckInfo(name: String, description: String) {
        val currentDeck = _currentDeck.value ?: return

        _currentDeck.value = currentDeck.copy(
            name = name,
            description = description
        )
    }

    /**
     * Check if the current deck is valid
     */
    fun isCurrentDeckValid(): Boolean {
        if (_currentDeck.value == null) return false

        // Just check card count for basic validation
        return _currentDeckCards.size == DeckBuilderRepository.DECK_SIZE
    }


    /**
     * Sound effects for menu actions
     */
    private fun playMenuSoundOne() {
        soundManager?.playSound(SoundType.MENU_TAP)
    }

    private fun playMenuSoundTwo() {
        soundManager?.playSound(SoundType.MENU_TAP_TWO)
    }

    fun playMenuScrollSound() {
        soundManager?.playSound(SoundType.MENU_SCROLL)
    }

    private fun playCardAddedSound() {
        soundManager?.playSound(SoundType.CARD_PICK)
    }

    private fun playCardRemovedSound() {
        soundManager?.playSound(SoundType.FOOT_UNIT_TAP)
    }
    fun playEditorMusic() {
        musicManager?.playMusic(MusicTrack.DECK_EDITOR,true)
    }
    fun stopMusic() {
        musicManager?.stopMusic()
    }
    override fun onCleared() {
        super.onCleared()
        musicManager?.release()
    }

}