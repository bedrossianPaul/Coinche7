<template>
  <div class="h-full w-full flex justify-center">
    <div class="w-full rounded-xl bg-slate-900/20 p-3">
      <div class="relative z-0 flex justify-center">
        <div class="flex -space-x-2">
          <div
              v-for="(card, index) in topHandCards"
              :key="`top-${card.value}-${index}`"
              class="transition-all duration-200"
              :class="[
                { 'z-20': hoveredCard.row === 'top' && hoveredCard.index === index, 'z-6': hoveredCard.row === 'top' && Math.abs(hoveredCard.index - index) === 1 },
                // RETRAIT DE GRAYSCALE ET SATURATE-0 : On garde uniquement l'opacité basse et le blocage du clic
                card.disabled ? 'opacity-35 cursor-not-allowed pointer-events-none' : 'cursor-pointer'
              ]"
              :style="handCardStyle('top', index)"
              @mouseenter="setHoveredCard('top', index)"
              @mouseleave="clearHoveredCard"
              @click="onCardClick(card)"
            >
            <Card
              :value="card.value"
              :card-image="card.image"
              :card-suit="card.suit"
              :suit-symbol="card.suitSymbol"
              :color="card.color"
            />
          </div>
        </div>
      </div>

      <div class="relative z-10 -mt-7 flex justify-center">
        <div class="flex -space-x-2">
          <div
              v-for="(card, index) in bottomHandCards"
              :key="`bottom-${card.value}-${index}`"
              class="transition-all duration-200"
              :class="[
                { 'z-5': hoveredCard.row === 'bottom' && hoveredCard.index === index },
                // IDEM ICI : La carte reste colorée mais semi-transparente
                card.disabled ? 'opacity-35 cursor-not-allowed pointer-events-none' : 'cursor-pointer'
              ]"
              :style="handCardStyle('bottom', index)"
              @mouseenter="setHoveredCard('bottom', index)"
              @mouseleave="clearHoveredCard"
              @click="onCardClick(card)"
            >
            <Card
              :value="card.value"
              :card-image="card.image"
              :card-suit="card.suit"
              :suit-symbol="card.suitSymbol"
              :color="card.color"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Card from './Card.vue';
import { useCard } from '../../services/useCard.js';

export default {
  name: 'PlayerHand',
  emits: ['play-card'],
  components: {
    Card
  },
  data() {
    return {
      hoveredCard: {
        row: null,
        index: null
      }
    }
  },
  created() {
    const { getCardImage, getCardSuit, getSuitColor, getSuitSymbol, setupTrump, sortCards, getPlayableCards } = useCard()
    this.$cardImage = getCardImage
    this.$cardSuit = getCardSuit
    this.$suitColor = getSuitColor
    this.$suitSymbol = getSuitSymbol
    this.$setupTrump = setupTrump
    this.$sortCards = sortCards
    this.$getPlayableCards = getPlayableCards

    // Configuration initiale lors de la création
    this.$setupTrump(this.trump)
  },
  props: {
    cards: { type: Array, default: () => [] },
    trump: { type: String, default: 'SA' },
    currentTrick: { type: Array, default: () => [] },
    players: { type: Object, default: () => ({}) },
    myId: { type: Number, default: null },
    playerTurnId: { type: Number, default: null },
    action: { type: String, default: null }
  },
  
  // LE REFUGE DE LA RÉACTIVITÉ : On écoute les changements de propriétés de l'atout
  watch: {
    trump(newTrump) {
      if (this.$setupTrump) {
        console.log(`[PlayerHand Watcher] L'atout a changé : ${newTrump}. Mise à jour du contexte d'atout.`);
        this.$setupTrump(newTrump)
        // Force la récomputation du computed en changeant une dépendance
        this.$forceUpdate()
      }
    }
  },
  
  computed: {
    isPlayTimeDisabled() {
      return this.action !== 'PLAY'
    },
    resolvedCards() {
      // S'assurer que le contexte de trump est à jour (setup s'effectue aussi dans le watcher)
      this.$setupTrump(this.trump)
      console.log(`[PlayerHand Computed] Résolution des cartes avec trump=${this.trump}, action=${this.action}, playerTurnId=${this.playerTurnId}`)

      // Détection des cartes interdites selon les règles officiales de Coinche
      let unplayableList = []
      if (this.isPlayTimeDisabled) {
        // Pendant l'enchère : toutes les cartes sont injouables
        unplayableList = [...this.cards]
      } else {
        // Pendant le jeu : selon les règles de couleur/atout
        // On récupère la liste des cartes jouables selon le contexte actuel (atout, cartes déjà jouées dans le pli, etc.)
        // La carte du partenaire est celle située dans le trick à la position opposée de la mienne
        // Ma position est contenue dans players : la clé de chaque joueur correspond à east, north, west ou south. En fonction de ma position, je peux déterminer où se trouve mon partenaire et donc quelle carte il a jouée (s'il a déjà joué)
        // Les cartes du trick sont dans l'ordre : [east, north, west, south]
        const partnerPositionIndex = this.getPartnerPositionIndex()
        let partnerCard = null
        if (partnerPositionIndex == null || partnerPositionIndex === -1) {
          console.warn(`[PlayerHand] Impossible de déterminer la position du partenaire pour le joueur ${this.myId}. Aucune carte partenaire considérée.`)
        } else {
          partnerCard = this.currentTrick[partnerPositionIndex]
        }

        // On peux récupérer la position du joueur qui a commencé le pli dans les attributs de players
        const firstPlayerPositionIndex = this.getFirstPlayerPositionIndex()
        let firstCard = null
        if (firstPlayerPositionIndex == null || firstPlayerPositionIndex === -1) {
          console.warn(`[PlayerHand] Impossible de déterminer la position du joueur qui a commencé le pli. Aucune carte de référence considérée pour la détermination des cartes jouables.`)
        } else {
          firstCard = this.currentTrick[firstPlayerPositionIndex]
        }
        let playableList = this.$getPlayableCards(this.cards, this.currentTrick, partnerCard, firstCard)
        unplayableList = this.cards.filter(card => !playableList.includes(card))
      }

      // Tri automatique des cartes (atout d'abord si SUIT, puis par couleur, puis par force décroissante)
      const orderedCards = this.$sortCards(this.cards)

      // Enrichissement avec métadonnées d'affichage
      return orderedCards.map((value) => {
        const suit = this.$cardSuit(value)
        return {
          value,
          image: this.$cardImage(value),
          suit,
          color: this.$suitColor(suit),
          suitSymbol: this.$suitSymbol(suit),
          disabled: unplayableList.includes(value)
        }
      })
    },
    topHandCards() {
      return this.resolvedCards.slice(0, 4)
    },
    bottomHandCards() {
      return this.resolvedCards.slice(4, 8)
    }
  },
  methods: {
    onCardClick(card) {
      if (!card || card.disabled) return
      this.$emit('play-card', card.value)
    },
    setHoveredCard(row, index) {
      this.hoveredCard = { row, index }
    },
    clearHoveredCard() {
      this.hoveredCard = { row: null, index: null }
    },
    handCardStyle(row, index) {
      if (this.hoveredCard.row !== row || this.hoveredCard.index === null) return {}
      const zIndex = row === 'top' ? 20 : 5
      if (index === this.hoveredCard.index) {
        return { transform: 'translateY(-16px)', zIndex }
      }
      if (index === this.hoveredCard.index - 1) return { transform: 'translateX(-2px)' }
      if (index === this.hoveredCard.index + 1) return { transform: 'translateX(2px)' }
      return {}
    },
    getPartnerPositionIndex() {
  const positions = ['east','north','west','south']
  // map positions -> index order used by currentTrick
  for (let i = 0; i < positions.length; i++) {
    const pos = positions[i]
    const playerObj = this.players[pos] ?? this.players[pos.toUpperCase()]
    const id = playerObj && (playerObj.id ?? playerObj)
    if (id === this.myId) return i
  }
  // fallback: scan entries (handles unusual shapes)
  for (const [key, val] of Object.entries(this.players || {})) {
    const id = val && (val.id ?? val)
    if (id === this.myId) {
      const pos = key.toLowerCase()
      const idx = positions.indexOf(pos)
      if (idx !== -1) return idx
    }
  }
  return null
},

getFirstPlayerPositionIndex() {
  const positions = ['east','north','west','south']
  for (let i = 0; i < positions.length; i++) {
    const pos = positions[i]
    const playerObj = this.players[pos] ?? this.players[pos.toUpperCase()]
    if (!playerObj) continue
    if (playerObj.is_first === true || playerObj.is_first_player === true) return i
  }
  // fallback: scan entries
  for (const [key, val] of Object.entries(this.players || {})) {
    if (val && (val.is_first === true || val.is_first_player === true)) {
      const idx = positions.indexOf(key.toLowerCase())
      if (idx !== -1) return idx
    }
  }
  return -1
}
  }
}
</script>