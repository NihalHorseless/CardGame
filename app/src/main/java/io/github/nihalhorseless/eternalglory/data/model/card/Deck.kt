package io.github.nihalhorseless.eternalglory.data.model.card

data class Deck(
    val id: String,
    val name: String,
    val description: String,
    val isPlayerOwned: Boolean = true,
    val cards: MutableList<Card> = mutableListOf()
) {
    /**
     * Shuffle the deck
     */
    fun shuffle() {
        cards.shuffle()
    }

    /**
     * Draw a card from the top of the deck
     * @return The drawn card, or null if the deck is empty
     */
    fun drawCard(): Card? {
        if (cards.isEmpty()) return null
        return cards.removeAt(0)
    }



    /**
     * Add a card to the deck
     */
    fun addCard(card: Card) {
        cards.add(card)
    }

    /**
     * Get the current size of the deck
     */
    fun size(): Int = cards.size

    /**
     * Check if the deck is empty
     */
    fun isEmpty(): Boolean = cards.isEmpty()

    /**
     * Create a copy of this deck
     */
    fun copy(): Deck {
        return Deck(
            id = id,
            name = name,
            description = description,
            cards = cards.toMutableList()
        )
    }
}
