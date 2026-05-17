//==========================================================================
// Constants
//==========================================================================

// Base url for card images
const BASE_CARD_URL = '/PNG-cards-1.3/'

// Suit constants
const SUITS = {
    HEARTS: 'H',
    DIAMONDS: 'D',
    CLUBS: 'C',
    SPADES: 'S',
    NO_TRUMP: 'SA', // No trump suit in the current mode
    ALL_TRUMP: 'TA' // All suits are trump in the current mode
}

// Mapping of card strings to their rank, suit, and image filename
const CARDS = {
    '7H': { rank: 7, trump_rank: 7, suit: SUITS.HEARTS, suit_key: 'H', img: '7_of_hearts.png' },
    '8H': { rank: 8, trump_rank: 8, suit: SUITS.HEARTS, suit_key: 'H', img: '8_of_hearts.png' },
    '9H': { rank: 9, trump_rank: 13, suit: SUITS.HEARTS, suit_key: 'H', img: '9_of_hearts.png' },
    '10H': { rank: 10, trump_rank: 11, suit: SUITS.HEARTS, suit_key: 'H', img: '10_of_hearts.png' },
    'JH': { rank: 11, trump_rank: 14, suit: SUITS.HEARTS, suit_key: 'H', img: 'jack_of_hearts.png' },
    'QH': { rank: 12, trump_rank: 9, suit: SUITS.HEARTS, suit_key: 'H', img: 'queen_of_hearts.png' },
    'KH': { rank: 13, trump_rank: 10, suit: SUITS.HEARTS, suit_key: 'H', img: 'king_of_hearts.png' },
    'AH': { rank: 14, trump_rank: 12, suit: SUITS.HEARTS, suit_key: 'H', img: 'ace_of_hearts.png' },
    '7D': { rank: 7, trump_rank: 7, suit: SUITS.DIAMONDS, suit_key: 'D', img: '7_of_diamonds.png' },
    '8D': { rank: 8, trump_rank: 8, suit: SUITS.DIAMONDS, suit_key: 'D', img: '8_of_diamonds.png' },
    '9D': { rank: 9, trump_rank: 13, suit: SUITS.DIAMONDS, suit_key: 'D', img: '9_of_diamonds.png' },
    '10D': { rank: 10, trump_rank: 11, suit: SUITS.DIAMONDS, suit_key: 'D', img: '10_of_diamonds.png' },
    'JD': { rank: 11, trump_rank: 14, suit: SUITS.DIAMONDS, suit_key: 'D', img: 'jack_of_diamonds.png' },
    'QD': { rank: 12, trump_rank: 9, suit: SUITS.DIAMONDS, suit_key: 'D', img: 'queen_of_diamonds.png' },
    'KD': { rank: 13, trump_rank: 10, suit: SUITS.DIAMONDS, suit_key: 'D', img: 'king_of_diamonds.png' },
    'AD': { rank: 14, trump_rank: 12, suit: SUITS.DIAMONDS, suit_key: 'D', img: 'ace_of_diamonds.png' },
    '7C': { rank: 7, trump_rank: 7, suit: SUITS.CLUBS, suit_key: 'C', img: '7_of_clubs.png' },
    '8C': { rank: 8, trump_rank: 8, suit: SUITS.CLUBS, suit_key: 'C', img: '8_of_clubs.png' },
    '9C': { rank: 9, trump_rank: 13, suit: SUITS.CLUBS, suit_key: 'C', img: '9_of_clubs.png' },
    '10C': { rank: 10, trump_rank: 11, suit: SUITS.CLUBS, suit_key: 'C', img: '10_of_clubs.png' },
    'JC': { rank: 11, trump_rank: 14, suit: SUITS.CLUBS, suit_key: 'C', img: 'jack_of_clubs.png' },
    'QC': { rank: 12, trump_rank: 9, suit: SUITS.CLUBS, suit_key: 'C', img: 'queen_of_clubs.png' },
    'KC': { rank: 13, trump_rank: 10, suit: SUITS.CLUBS, suit_key: 'C', img: 'king_of_clubs.png' },
    'AC': { rank:14, trump_rank:12, suit:SUITS.CLUBS, suit_key:'C', img:'ace_of_clubs.png' },
    '7S': { rank: 7, trump_rank: 7, suit: SUITS.SPADES, suit_key: 'S', img: '7_of_spades.png' },
    '8S': { rank: 8, trump_rank: 8, suit: SUITS.SPADES, suit_key: 'S', img: '8_of_spades.png' },
    '9S': { rank: 9, trump_rank: 13, suit: SUITS.SPADES, suit_key: 'S', img: '9_of_spades.png' },
    '10S': { rank: 10, trump_rank: 11, suit: SUITS.SPADES, suit_key: 'S', img: '10_of_spades.png' },
    'JS': { rank: 11, trump_rank: 14, suit: SUITS.SPADES, suit_key: 'S', img: 'jack_of_spades.png' },
    'QS': { rank: 12, trump_rank: 9, suit: SUITS.SPADES, suit_key: 'S', img: 'queen_of_spades.png' },
    'KS': { rank: 13, trump_rank: 10, suit: SUITS.SPADES, suit_key: 'S', img: 'king_of_spades.png' },
    'AS': { rank: 14, trump_rank: 12, suit: SUITS.SPADES, suit_key: 'S', img: 'ace_of_spades.png' }
}


/**
 * Card service providing utility functions for handling card-related operations such as getting card images, suits, and colors based on the current trump context.
 * @returns {Object} Object containing the utility functions
 */
export function useCard() {

    //==========================================================================
    // Initialization
    //==========================================================================

    // Current trump context (mode and suit), by default no trump suit and "SA" mode
    let trump =  SUITS.ALL_TRUMP; // 'SA' for no trump, 'TA' for all trump, or H,S,C,D if specific trump suit

    //==========================================================================
    // Utility functions
    //==========================================================================

    /**
     * Normalize a card string to a standard format (e.g., "10H" -> "10H", "  jh " -> "JH")
     * @param {string} card 
     * @returns {string}
     */
    function normalizeCard(card) { 
        return String(card || '').trim().toUpperCase() 
    }

    /**
     * Get the winning card of the current trick based on the cards played, the asked suit, and the current trump context
     * @param {array[Object]} currentTrick Array of card objects representing the cards played in the current trick, in order of play
     * @param {string | null} askedSuit Suit that was asked for in the current trick (e.g., "H", "D", "C", "S"), or null if no suit was asked (first card of the trick)
     * @returns {Object} Card info object representing the winning card of the trick
     */
    function getWinningCard(currentTrick, askedSuit) {
        const winning_card = currentTrick ? currentTrick.reduce((bestCard, card) => {
            // First card is the best card by default
            if (!bestCard) return card;

            // Card to compare
            let cardRank = 0;
            const isCardTrump = card.suit_key === trump || trump === SUITS.ALL_TRUMP;

            if (isCardTrump) {
                cardRank = card.trump_rank + 100; // Trump cards get a big boost to ensure they beat non-trump cards
            } else if (card.suit_key === askedSuit) {
                cardRank = card.rank; // Non-trump cards of the asked suit are ranked by their normal rank
            } //  Otherwise, the card can not win and gets a rank of 0

            // Best card rank - same logic as above
            let bestCardRank = 0;
            const isBestCardTrump = bestCard.suit_key === trump || trump === SUITS.ALL_TRUMP;

            if (isBestCardTrump) {
                bestCardRank = bestCard.trump_rank + 100;
            } else if (bestCard.suit_key === askedSuit) {
                bestCardRank = bestCard.rank;
            }

            // The highest score wins
            return cardRank > bestCardRank ? card : bestCard;
        }) : null;
        return winning_card;
    }


    //==========================================================================
    // Exposed functions definitions
    //==========================================================================

    /**
     * Set up the current trump context (mode and trump suit)
     * @param {string|null} trumpSuit // 'HEARTS', 'DIAMONDS', 'CLUBS', 'SPADES' for specific trump suit, 'NO_TRUMP' for no trump mode, 'ALL_TRUMP' for all trump mode
     */
    function setupTrump(trumpSuit) {
          // Accepts:
          // - null/undefined -> NO_TRUMP mode
          // - full keys like 'SPADES','HEARTS' -> maps to 'S','H' via SUITS
          // - suit letters like 'S','H','C','D' -> used directly
          // - special values 'SA' (NO_TRUMP) or 'TA' (ALL_TRUMP)
          if (trumpSuit == null) {
              trump = SUITS.NO_TRUMP
              return
          }

          let v = String(trumpSuit).toUpperCase()

          // If passed a letter/value that's already one of SUITS values
          if (Object.values(SUITS).includes(v)) {
              trump = v
              return
          }

          // If passed a key like 'SPADES' or 'HEARTS'
          if (SUITS[v]) {
              trump = SUITS[v]
              return
          }

          console.warn(`Invalid trump suit: "${trumpSuit}". Defaulting to ALL_TRUMP mode.`)
          trump = SUITS.ALL_TRUMP
    }

    /**
     * Get the image URL for a given card
     * @param {string} card Card string (e.g., "10H", "JH", "AS")
     * @returns {string|null} URL of the card image, or null if invalid card string
     */
    function getCardImage(card) { 
        const info = CARDS[normalizeCard(card)]; 
        if (!info) console.warn(`Invalid card string: "${card}"`)
        return info ? BASE_CARD_URL + info.img : null 
    }

    /**
     * Get the suit key for a given card (e.g., "H" for hearts, "D" for diamonds)
     * @param {string} card Card string (e.g., "10H", "JH", "AS")
     * @returns {string|null} Letter representing the suit, or null if invalid card string
     */
    function getCardSuit(card) {
        const info = CARDS[normalizeCard(card)]
        if (!info) console.warn(`Invalid card string: "${card}"`)
        return info ? info.suit_key : null
    }

    /**
     * Get the color hex code for a given suit key
     * @param {string} suitKey Letter representing the suit (e.g., "H", "D", "C", "S")
     * @returns {string} color hex code for the suit (red for hearts/diamonds, black for clubs/spades, gold for trump)
     */
    function getSuitColor(suitKey) {
        if (trump === suitKey || trump === SUITS.ALL_TRUMP) return '#eab308' // Gold color for trump suit
        return ['H', 'D'].includes(suitKey) ? '#dc2626' : '#0f172a'
    }

    /**
     * Get the suit symbol for a given suit key
     * @param {string} suitKey Letter representing the suit (e.g., "H", "D", "C", "S")
     * @returns {string} Symbol representing the suit (♥, ♦, ♣, ♠)
     */
    function getSuitSymbol(suitKey) {
        const symbols = {
            'H': '♥',
            'D': '♦',
            'C': '♣',
            'S': '♠',
            'SA': 'Sans Atout',
            'TA': 'Tous Atout'
        }
        return symbols[suitKey] || ''
    }

    /**
     * Sort an array of card strings first by suit (trump suit first, then in order H, D, C, S) and then by rank within each suit
     * @param {array[string]} cards Array of card strings (e.g., ["10H", "JH", "AS", "7D"])
     * @returns {array[string]} New array of card strings sorted by suit and rank
     */
    function sortCards(cards) {
        //Determine suit order based on current trump context
        const suitOrder = (trump === SUITS.NO_TRUMP || trump === SUITS.ALL_TRUMP)
            ? ['H', 'D', 'C', 'S'] 
            : [trump, ...['H', 'D', 'C', 'S'].filter(s => s !== trump)] // Trump suit first, then the rest

        // Then sort cards by suit order and rank (given the fact they are of the trump suit or not)
        return [...cards].sort((a, b) => {
            const cardA = CARDS[normalizeCard(a)]
            const cardB = CARDS[normalizeCard(b)]
            
            let rankA = cardA.suit_key === trump || trump === SUITS.ALL_TRUMP ? cardA.trump_rank : cardA.rank
            let rankB = cardB.suit_key === trump || trump === SUITS.ALL_TRUMP ? cardB.trump_rank : cardB.rank
            
            const suitComparison = suitOrder.indexOf(cardA.suit_key) - suitOrder.indexOf(cardB.suit_key)
            if (suitComparison !== 0) return suitComparison
            return rankB - rankA // Higher rank first
        })
    }

    /**
     * Get the list of playable cards from the player's hand based on the current trick state, partner's card, and asked suit
     * @param {array[string]} hand // Array of card strings representing the player's hand (e.g., ["10H", "JH", "AS", "7D"])
     * @param {array} currentTrick // Array of card code like {null, "10H", "AS", null} representing the current trick state, with null for players who haven't played yet the 1st is the card placed by east player and then north west south
     * @param {string|null} partnerCard // Card played by the partner, or null if not played yet
     * @param {string|null} askedSuit // Suit that was asked for, or null if not applicable
     */
    function getPlayableCards(hand, currentTrick, partnerCard, firstCard) {
        // Normalize hand & current trick and transform to card info objects
        hand = hand.map(normalizeCard).map(card => CARDS[card])
        currentTrick = currentTrick.map(normalizeCard).filter(card => CARDS[card]).map(card => CARDS[card])
        // Normalize partner card and first card
        partnerCard = partnerCard ? CARDS[normalizeCard(partnerCard)] : null
        firstCard = firstCard ? CARDS[normalizeCard(firstCard)] : null
        // Determine the asked suit based on the first card played in the trick
        const askedSuit = firstCard ? firstCard.suit_key : null

        let playableCards = [];

        // ====== CORE LOGIC ======
        // The first card played can be any card
        if (askedSuit === null) {
            playableCards = hand
        } else {

            // If the player has cards of the asked suit, they must play one of them
            const hasAskedSuit = hand.some(card => card.suit_key === askedSuit)
            if (hasAskedSuit) {
                // If the asked suit is trump, the player must play a trump card with an higher rank of the winning card
                if (askedSuit === trump || trump === SUITS.ALL_TRUMP) {
                    // Determine the winning card with the rank of all the cards played in the trick (considering the trump context)
                    const winningCard = getWinningCard(currentTrick, askedSuit)
                    // Check if the player has a trump card of the asked suit that has a higher rank than the winning card
                    const hasHigherCards = hand.filter(card => card.suit_key === askedSuit && card.trump_rank > winningCard.trump_rank).length > 0
                    if (hasHigherCards) {
                        // Filter the player's hand to only include cards of the asked suit that have a higher rank than the winning card
                        playableCards = hand.filter(card => card.suit_key === askedSuit && card.trump_rank > winningCard.trump_rank)
                    } else {
                        // If the player does not have a higher trump card, they can play any card of the asked suit
                        playableCards = hand.filter(card => card.suit_key === askedSuit)
                    }
                } else {
                    // If the player has the asked suit and it's not trump, they must play a card of that suit
                    playableCards = hand.filter(card => card.suit_key === askedSuit)
                }
            } else {
                // If the player does not have cards of the asked suit
                // Determine the winning card with the rank of all the cards played in the trick (considering the trump context)
                const winningCard = getWinningCard(currentTrick, askedSuit)
                const partnerIsWinning = partnerCard && (partnerCard === winningCard)
                // If the partner is currently winning the trick, the player can play any card (no need to try to win)
                if (partnerIsWinning) {
                    playableCards = hand
                } else {
                    // If the partner is not winning, the player must try to win if they can by playing a trump card that beats the current winning card
                    const winningRank = (winningCard.suit_key === trump || trump === SUITS.ALL_TRUMP) ? winningCard.trump_rank + 100 : winningCard.rank
                    const canWin = hand.some(card => (card.suit_key === trump || trump === SUITS.ALL_TRUMP) && card.trump_rank + 100 > winningRank)
                    if (canWin) {
                        playableCards = hand.filter(card => (card.suit_key === trump || trump === SUITS.ALL_TRUMP) && card.trump_rank + 100 > winningRank)
                    } else {
                        playableCards = hand
                    }
                }
            }
        }
        return playableCards.map(cardObject => {
            return Object.keys(CARDS).find(key => CARDS[key] === cardObject);
        });// Convert back to card strings

    }


    //==========================================================================
    // Exposed functions
    //==========================================================================

    return {
        getCardImage,
        getCardSuit,
        getSuitColor,
        getSuitSymbol,
        setupTrump,
        sortCards,
        getPlayableCards
    }
}