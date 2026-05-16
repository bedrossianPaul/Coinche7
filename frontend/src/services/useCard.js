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

// Dictionnaire des points selon le contrat
const CARD_POINTS = {
    SA: { A: 19, '10': 10, K: 4, Q: 3, J: 2, '9': 0, '8': 0, '7': 0 },
    TA: { J: 14, '9': 9, A: 6, '10': 5, K: 3, Q: 2, '8': 0, '7': 0 },
    TRUMP: { J: 20, '9': 14, A: 11, '10': 10, K: 4, Q: 3, '8': 0, '7': 0 },
    NON_TRUMP: { A: 11, '10': 10, K: 4, Q: 3, J: 2, '9': 0, '8': 0, '7': 0 }

}

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
const PLAYER_ORDER = ['NORTH', 'EAST', 'SOUTH', 'WEST']

export function useCard() {
    let trumpContext = {
        mode: 'SA',
        trumpSuit: null
    }

    function normalizeCard(card) { return String(card || '').trim().toUpperCase() }
    function getRankLabel(card) {
        const norm = normalizeCard(card)
        return norm.startsWith('10') ? '10' : (norm[0] || '')
    }
    function getSuitKeyFromValue(suitValue) { return SUIT_KEYS.find((key) => SUITS[key] === suitValue) ?? null }
    function getSuitKey(suitValue) { return getSuitKeyFromValue(suitValue) }
    function getCardSuit(card) {
        const info = CARDS[normalizeCard(card)]
        return info ? getSuitKey(info.suit) : null
    }

    function parseTrumpType(trumpType) {
        if (trumpType && typeof trumpType === 'object') return parseTrumpType(trumpType.type ?? trumpType.trump ?? null)
        const raw = String(trumpType ?? '').trim().toUpperCase().replace(/[-_\s]/g, '')
        if (!raw || raw === 'NONE' || raw === 'NULL' || raw === 'SA' || raw === 'SANSATOUT') return { mode: 'SA', trumpSuit: null }
        if (raw === 'TA' || raw === 'TOUTATOUT') return { mode: 'TA', trumpSuit: null }
        if (SUIT_KEYS.includes(raw)) return { mode: 'SUIT', trumpSuit: raw }
        const suitKey = getSuitKeyFromValue(raw)
        return suitKey ? { mode: 'SUIT', trumpSuit: suitKey } : { mode: 'SA', trumpSuit: null }
    }

    function setupTrump(trumpType = 'SA') {
        trumpContext = parseTrumpType(trumpType)
        return { ...trumpContext }
    }

    function getTrumpContext() { return { ...trumpContext } }
    function isTrumpSuit(suitKey) {
        if (!suitKey) return false
        if (trumpContext.mode === 'TA') return true
        if (trumpContext.mode === 'SA') return false
        return suitKey === trumpContext.trumpSuit
    }
    function isTrumpCard(card) { return isTrumpSuit(getCardSuit(card)) }

    function getTrumpStrength(card) { return TRUMP_RANK_ORDER[getRankLabel(card)] || 0 }
    function getNonTrumpStrength(card) { return NON_TRUMP_RANK_ORDER[getRankLabel(card)] || 0 }
    function getCardStrength(card) { return isTrumpCard(card) ? getTrumpStrength(card) : getNonTrumpStrength(card) }

    // ── NOUVEAU : CALCUL DE LA VALEUR EN POINTS D'UNE CARTE ──────────────────
    function getCardPoints(card) {
        const rank = getRankLabel(card)
        if (trumpContext.mode === 'SA') return CARD_POINTS.SA[rank] || 0
        if (trumpContext.mode === 'TA') return CARD_POINTS.TA[rank] || 0
        if (isTrumpCard(card)) return CARD_POINTS.TRUMP[rank] || 0
        return CARD_POINTS.NON_TRUMP[rank] || 0
    }

    // ── NOUVEAU : CALCUL ABSOLU DE LA FORCE DANS LE PLI ──────────────────────
    function getCardAbsoluteStrength(card, leadSuit) {
        const suit = getCardSuit(card)
        if (trumpContext.mode === 'SA') {
            return suit === leadSuit ? getNonTrumpStrength(card) : -1
        }
        if (trumpContext.mode === 'TA') {
            return suit === leadSuit ? getTrumpStrength(card) : -1
        }
        // Mode couleur classique
        if (suit === trumpContext.trumpSuit) {
            return 100 + getTrumpStrength(card) // L'atout bat tout le reste
        }
        if (suit === leadSuit) {
            return getNonTrumpStrength(card)
        }
        return -1 // Défausse hors couleur
    }

    function compareCardsForTrick(leftCard, rightCard, leadSuit) {
        return getCardAbsoluteStrength(leftCard, leadSuit) - getCardAbsoluteStrength(rightCard, leadSuit)
    }

    function getTrickWinner(trickCards = [], leadSuit = null) {
        if (!Array.isArray(trickCards) || !trickCards.length || !leadSuit) return null
        return trickCards.reduce((winner, current) => {
            if (!winner) return current
            return compareCardsForTrick(current.value, winner.value, leadSuit) > 0 ? current : winner
        }, null)
    }

    // ── CORRECTION COMPLÈTE DE LA LOGIQUE DE JOUABILITÉ ──────────────────────
    function getUnplayableCards(handCards = [], currentTrick = [], options = {}) {
        const normalizedHand = handCards
            .map((card) => normalizeCard(card))
            .filter((card) => Boolean(card) && Boolean(CARDS[card]))

        if (!normalizedHand.length || !Array.isArray(currentTrick) || !currentTrick.length) return []
        if (options?.trumpType !== undefined) setupTrump(options.trumpType)

        const normalizedTrick = assignTrickPositions(normalizeTrickCards(currentTrick), options)
        const leadCard = normalizedTrick[0]
        if (!leadCard) return []

        const leadSuit = getCardSuit(leadCard.value)
        if (!leadSuit) return []

        const hasLeadSuit = normalizedHand.some((card) => getCardSuit(card) === leadSuit)

        // ── CAS 1 : On a la couleur demandée
        if (hasLeadSuit) {
            const leadCardsInHand = normalizedHand.filter((card) => getCardSuit(card) === leadSuit)
           
            // Règle du "monter à l'atout" (Atout classique ou Tout Atout)
            const mustRise = (trumpContext.mode === 'TA') || (trumpContext.mode === 'SUIT' && leadSuit === trumpContext.trumpSuit)
           
            if (mustRise) {
                const leadCardsInTrick = normalizedTrick.filter((c) => getCardSuit(c.value) === leadSuit)
                const highestLeadStrength = leadCardsInTrick.reduce((max, c) => {
                    const str = getTrumpStrength(c.value)
                    return str > max ? str : max
                }, -1)

                const overCards = leadCardsInHand.filter((card) => getTrumpStrength(card) > highestLeadStrength)
               
                // Si on peut monter, on doit obligatoirement jouer une de ces cartes supérieures
                if (overCards.length > 0) {
                    return normalizedHand.filter((card) => !overCards.includes(card))
                }
            }

            // Sinon suit classique obligatoire
            return normalizedHand.filter((card) => getCardSuit(card) !== leadSuit)
        }

        // ── CAS 2 : On n'a pas la couleur demandée (Défausse / Coupe)
        if (trumpContext.mode !== 'SUIT' || !trumpContext.trumpSuit) return [] // SA ou TA hors couleur = défausse libre

        const trumpSuit = trumpContext.trumpSuit
        const handTrumps = normalizedHand.filter((card) => getCardSuit(card) === trumpSuit)
        if (!handTrumps.length) return [] // Pas d'atout = défausse libre

        const winner = getTrickWinner(normalizedTrick, leadSuit)
        const myPosition = resolvePositionFromPlayers(options?.players ?? {}, options?.myPlayerId ?? null)
        const partnerPosition = myPosition ? getPartnerPosition(myPosition) : null
        const partnerWinning = Boolean(winner && partnerPosition && winner.playerPosition === partnerPosition)

        if (partnerWinning) return [] // Le partenaire est maître = défausse libre

        // L'adversaire est maître : Obligation de couper
        const trickTrumps = normalizedTrick.filter((card) => getCardSuit(card.value) === trumpSuit)
       
        if (!trickTrumps.length) {
            // Pas encore d'atout dans le pli : n'importe quel atout est valide (on coupe)
            return normalizedHand.filter((card) => getCardSuit(card) !== trumpSuit)
        }

        // Il y a déjà de l'atout : obligation de surcouper si possible
        const highestTrumpInTrick = trickTrumps.reduce((best, current) => {
            return getTrumpStrength(current.value) > getTrumpStrength(best) ? current.value : best
        }, trickTrumps[0].value)

        const overTrumps = handTrumps.filter((card) => getTrumpStrength(card) > getTrumpStrength(highestTrumpInTrick))
       
        if (overTrumps.length > 0) {
            return normalizedHand.filter((card) => !overTrumps.includes(card)) // Obligation de surcouper
        }

        return normalizedHand.filter((card) => getCardSuit(card) !== trumpSuit) // Sous-coupe autorisée si on ne peut pas monter
    }

    // ── NOUVEAU : CALCULATEUR DE SCORE DE FIN DE MANCHE ──────────────────────
    function computeRoundScore(team1Tricks = [], team2Tricks = [], lastTrickWinnerTeam) {
        let team1Points = 0
        let team2Points = 0

        // Points de l'équipe 1
        team1Tricks.forEach(trick => {
            trick.forEach(card => { team1Points += getCardPoints(card) })
        })

        // Points de l'équipe 2
        team2Tricks.forEach(trick => {
            trick.forEach(card => { team2Points += getCardPoints(card) })
        })

        // Règle du Dix de Der
        if (lastTrickWinnerTeam === 1) team1Points += 10
        if (lastTrickWinnerTeam === 2) team2Points += 10

        return { team1Points, team2Points }
    }

    // Fonctions de rotation existantes conservées...
    function rotateBack(position, steps) {
        const idx = PLAYER_ORDER.indexOf(position)
        if (idx < 0) return null
        return PLAYER_ORDER[(idx - (((steps % 4) + 4) % 4) + 4) % 4]
    }
    function rotateForward(position, steps) {
        const idx = PLAYER_ORDER.indexOf(position)
        if (idx < 0) return null
        return PLAYER_ORDER[(idx + (((steps % 4) + 4) % 4)) % 4]
    }
    function getPartnerPosition(position) { return rotateForward(position, 2) }
    function resolvePositionFromPlayers(players = {}, playerId = null) {
        if (!playerId || !players || typeof players !== 'object') return null
        for (const position of PLAYER_ORDER) {
            if (players[position] && players[position].id === playerId) return position
        }
        return null
    }
    function assignTrickPositions(trickCards = [], context = {}) {
        const { players = {}, playerTurnId = null } = context
        const playedCount = trickCards.length
        if (!playedCount) return trickCards
        const currentTurnPosition = resolvePositionFromPlayers(players, playerTurnId)
        const inferredLeader = currentTurnPosition ? rotateBack(currentTurnPosition, playedCount) : null
        return trickCards.map((card, order) => {
            if (card.playerPosition) return card
            if (inferredLeader) return { ...card, playerPosition: rotateForward(inferredLeader, order) }
            return card
        })
    }
    function normalizeTrickCards(currentTrick = []) {
        if (!Array.isArray(currentTrick)) return []
        return currentTrick.map((card, index) => {
            if (typeof card === 'string') {
                const value = normalizeCard(card)
                return CARDS[value] ? { value, playerPosition: null, playerId: null, index } : null
            }
            if (!card || typeof card.value !== 'string') return null
            const value = normalizeCard(card.value)
            return CARDS[value] ? {
                value,
                playerPosition: typeof card.playerPosition === 'string' ? card.playerPosition.toUpperCase() : null,
                playerId: Number.isInteger(card.playerId) ? card.playerId : null,
                index
            } : null
        }).filter(Boolean)
    }
    function getCardImage(card) { const info = CARDS[normalizeCard(card)]; return info ? BASE_CARD_URL + info.img : null }
    function getCardRank(card) { const info = CARDS[normalizeCard(card)]; return info ? info.rank : null }
    function getSuitSymbol(suitKey) { return '' }
    function getSuitColor(suitKey) {
        if (isTrumpSuit(suitKey)) return '#eab308'
        return ['HEART', 'DIAMOND'].includes(suitKey) ? '#dc2626' : '#0f172a'
    }
    function getSuitBucket(suitKey) { return trumpContext.mode === 'SUIT' && suitKey === trumpContext.trumpSuit ? 0 : 1 }
    function sortCards(cards = []) {
        return [...cards.map(normalizeCard).filter(c => Boolean(CARDS[c]))].sort((left, right) => {
            const leftSuit = getCardSuit(left), rightSuit = getCardSuit(right)
            const bLeft = getSuitBucket(leftSuit), bRight = getSuitBucket(rightSuit)
            if (bLeft !== bRight) return bLeft - bRight
            const oLeft = SUIT_ORDER.indexOf(leftSuit), oRight = SUIT_ORDER.indexOf(rightSuit)
            if (oLeft !== oRight) return oLeft - oRight
            return getCardStrength(right) - getCardStrength(left)
        })
    }

    return {
        getCardImage, getCardRank, getCardSuit, getSuitKey, getSuitSymbol, getSuitColor,
        setupTrump, getTrumpContext, isTrumpSuit, isTrumpCard, sortCards, getUnplayableCards,
        getCardPoints, computeRoundScore
    }
}