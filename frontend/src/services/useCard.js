export const SUITS = {
    HEART: 'H',
    DIAMOND: 'D',
    CLUB: 'C',
    SPADE: 'S'
}

const SUIT_KEYS = Object.keys(SUITS)
const SUIT_ORDER = ['SPADE', 'HEART', 'DIAMOND', 'CLUB']
const NON_TRUMP_RANK_ORDER = { A: 8, '10': 7, K: 6, Q: 5, J: 4, '9': 3, '8': 2, '7': 1 }
const TRUMP_RANK_ORDER = { J: 8, '9': 7, A: 6, '10': 5, K: 4, Q: 3, '8': 2, '7': 1 }

const CARDS = {
    '7H': { rank: 7, suit: SUITS.HEART, img: '7_of_hearts.png' },
    '8H': { rank: 8, suit: SUITS.HEART, img: '8_of_hearts.png' },
    '9H': { rank: 9, suit: SUITS.HEART, img: '9_of_hearts.png' },
    '10H': { rank: 10, suit: SUITS.HEART, img: '10_of_hearts.png' },
    'JH': { rank: 11, suit: SUITS.HEART, img: 'jack_of_hearts.png' },
    'QH': { rank: 12, suit: SUITS.HEART, img: 'queen_of_hearts.png' },
    'KH': { rank: 13, suit: SUITS.HEART, img: 'king_of_hearts.png' },
    'AH': { rank: 14, suit: SUITS.HEART, img: 'ace_of_hearts.png' },
    '7D': { rank: 7, suit: SUITS.DIAMOND, img: '7_of_diamonds.png' },
    '8D': { rank: 8, suit: SUITS.DIAMOND, img: '8_of_diamonds.png' },
    '9D': { rank: 9, suit: SUITS.DIAMOND, img: '9_of_diamonds.png' },
    '10D': { rank: 10, suit: SUITS.DIAMOND, img: '10_of_diamonds.png' },
    'JD': { rank: 11, suit: SUITS.DIAMOND, img: 'jack_of_diamonds.png' },
    'QD': { rank: 12, suit: SUITS.DIAMOND, img: 'queen_of_diamonds.png' },
    'KD': { rank: 13, suit: SUITS.DIAMOND, img: 'king_of_diamonds.png' },
    'AD': { rank: 14, suit: SUITS.DIAMOND, img: 'ace_of_diamonds.png' },
    '7C': { rank: 7, suit: SUITS.CLUB, img: '7_of_clubs.png' },
    '8C': { rank: 8, suit: SUITS.CLUB, img: '8_of_clubs.png' },
    '9C': { rank: 9, suit: SUITS.CLUB, img: '9_of_clubs.png' },
    '10C': { rank: 10, suit: SUITS.CLUB, img: '10_of_clubs.png' },
    'JC': { rank: 11, suit: SUITS.CLUB, img: 'jack_of_clubs.png' },
    'QC': { rank: 12, suit: SUITS.CLUB, img: 'queen_of_clubs.png' },
    'KC': { rank: 13, suit: SUITS.CLUB, img: 'king_of_clubs.png' },
    'AC': { rank: 14, suit: SUITS.CLUB, img: 'ace_of_clubs.png' },
    '7S': { rank: 7, suit: SUITS.SPADE, img: '7_of_spades.png' },
    '8S': { rank: 8, suit: SUITS.SPADE, img: '8_of_spades.png' },
    '9S': { rank: 9, suit: SUITS.SPADE, img: '9_of_spades.png' },
    '10S': { rank: 10, suit: SUITS.SPADE, img: '10_of_spades.png' },
    'JS': { rank: 11, suit: SUITS.SPADE, img: 'jack_of_spades.png' },
    'QS': { rank: 12, suit: SUITS.SPADE, img: 'queen_of_spades.png' },
    'KS': { rank: 13, suit: SUITS.SPADE, img: 'king_of_spades.png' },
    'AS': { rank: 14, suit: SUITS.SPADE, img: 'ace_of_spades.png' }
}

const BASE_CARD_URL = '/PNG-cards-1.3/'

export function useCard() {
    let trumpContext = {
        mode: 'SA',
        trumpSuit: null
    }

    function normalizeCard(card) {
        return String(card || '').trim().toUpperCase()
    }

    function getRankLabel(card) {
        const normalizedCard = normalizeCard(card)
        if (normalizedCard.startsWith('10')) {
            return '10'
        }

        return normalizedCard[0] || ''
    }

    function getSuitKeyFromValue(suitValue) {
        return SUIT_KEYS.find((key) => SUITS[key] === suitValue) ?? null
    }

    function parseTrumpType(trumpType) {
        if (trumpType && typeof trumpType === 'object') {
            return parseTrumpType(trumpType.type ?? trumpType.trump ?? null)
        }

        const raw = String(trumpType ?? '')
            .trim()
            .toUpperCase()
            .replace(/[-_\s]/g, '')

        if (!raw || raw === 'NONE' || raw === 'NULL') {
            return { mode: 'SA', trumpSuit: null }
        }

        if (raw === 'SA' || raw === 'SANSATOUT') {
            return { mode: 'SA', trumpSuit: null }
        }

        if (raw === 'TA' || raw === 'TOUTATOUT') {
            return { mode: 'TA', trumpSuit: null }
        }

        if (SUIT_KEYS.includes(raw)) {
            return { mode: 'SUIT', trumpSuit: raw }
        }

        const suitKey = getSuitKeyFromValue(raw)
        if (suitKey) {
            return { mode: 'SUIT', trumpSuit: suitKey }
        }

        return { mode: 'SA', trumpSuit: null }
    }

    function setupTrump(trumpType = 'SA') {
        trumpContext = parseTrumpType(trumpType)
        return { ...trumpContext }
    }

    function getTrumpContext() {
        return { ...trumpContext }
    }

    function getCardImage(card) {
        const cardInfo = CARDS[normalizeCard(card)];
        if (!cardInfo) {
            console.warn(`Unknown card: ${card}`);
            return null;
        }
        return BASE_CARD_URL + cardInfo.img;
    }

    function getCardRank(card) {
        const cardInfo = CARDS[normalizeCard(card)];
        if (!cardInfo) {
            console.warn(`Unknown card: ${card}`);
            return null;
        }
        return cardInfo.rank;
    }

    function getSuitKey(suitValue) {
        return getSuitKeyFromValue(suitValue)
    }

    function getCardSuit(card) {
        const cardInfo = CARDS[normalizeCard(card)];
        if (!cardInfo) {
            console.warn(`Unknown card: ${card}`);
            return null;
        }
        return getSuitKey(cardInfo.suit);
    }

    function isTrumpSuit(suitKey) {
        if (!suitKey) {
            return false
        }

        if (trumpContext.mode === 'TA') {
            return true
        }

        if (trumpContext.mode === 'SA') {
            return false
        }

        return suitKey === trumpContext.trumpSuit
    }

    function isTrumpCard(card) {
        return isTrumpSuit(getCardSuit(card))
    }

    function getSuitSymbol(suitKey) {
        switch (suitKey) {
            case 'HEART':   return '♥';
            case 'DIAMOND': return '♦';
            case 'CLUB':    return '♣';
            case 'SPADE':   return '♠';
            default:        return '';
        }
    }

    function getSuitColor(suitKey) {
        if (isTrumpSuit(suitKey)) {
            return '#eab308';
        }

        switch (suitKey) {
            case 'HEART':
            case 'DIAMOND':
                return '#dc2626'; // red-600
            case 'CLUB':
            case 'SPADE':
            default:
                return '#0f172a'; // slate-900
        }
    }

    function getCardStrength(card) {
        const rankLabel = getRankLabel(card)
        const useTrumpOrder = isTrumpCard(card)
        const order = useTrumpOrder ? TRUMP_RANK_ORDER : NON_TRUMP_RANK_ORDER

        return order[rankLabel] || 0
    }

    function getSuitBucket(suitKey) {
        if (trumpContext.mode === 'SUIT') {
            return suitKey === trumpContext.trumpSuit ? 0 : 1
        }

        return 1
    }

    function sortCards(cards = []) {
        const normalizedCards = cards
            .map((card) => normalizeCard(card))
            .filter((card) => Boolean(card))

        return [...normalizedCards].sort((left, right) => {
            const leftSuit = getCardSuit(left)
            const rightSuit = getCardSuit(right)

            const leftBucket = getSuitBucket(leftSuit)
            const rightBucket = getSuitBucket(rightSuit)
            if (leftBucket !== rightBucket) {
                return leftBucket - rightBucket
            }

            const leftSuitOrder = SUIT_ORDER.indexOf(leftSuit)
            const rightSuitOrder = SUIT_ORDER.indexOf(rightSuit)
            if (leftSuitOrder !== rightSuitOrder) {
                return leftSuitOrder - rightSuitOrder
            }

            const leftStrength = getCardStrength(left)
            const rightStrength = getCardStrength(right)
            if (leftStrength !== rightStrength) {
                return rightStrength - leftStrength
            }

            return left.localeCompare(right)
        })
    }

    return {
        getCardImage,
        getCardRank,
        getCardSuit,
        getSuitKey,
        getSuitSymbol,
        getSuitColor,
        setupTrump,
        getTrumpContext,
        isTrumpSuit,
        isTrumpCard,
        sortCards
    }

}