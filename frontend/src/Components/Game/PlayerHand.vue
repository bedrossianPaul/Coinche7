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
              card.disabled || this.isPlayTimeDisabled ? 'opacity-40  cursor-not-allowed pointer-events-none' : 'cursor-pointer'
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
              card.disabled ? 'opacity-40 grayscale saturate-0 cursor-not-allowed pointer-events-none' : 'cursor-pointer'
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
    const { getCardImage, getCardSuit, getSuitSymbol, getSuitColor, setupTrump, sortCards, getUnplayableCards } = useCard()
    this.$cardImage = getCardImage
    this.$cardSuit = getCardSuit
    this.$suitSymbol = getSuitSymbol
    this.$suitColor = getSuitColor
    this.$setupTrump = setupTrump
    this.$sortCards = sortCards
    this.$getUnplayableCards = getUnplayableCards
  },
  props: {
    cards: {
      type: Array,
      default: () => []
    },
    trump: {
      type: String,
      default: 'SA'
    },
    currentTrick: {
      type: Array,
      default: () => []
    },
    players: {
      type: Object,
      default: () => ({})
    },
    myId: {
      type: Number,
      default: null
    },
    playerTurnId: {
      type: Number,
      default: null
    },
    action: {
      type: String,
      default: null
    }
  },
  computed: {
    isPlayTimeDisabled() {
      const disabled = this.action !== 'PLAY'
      console.log(`[PlayerHand] action=${this.action}, isPlayTimeDisabled=${disabled}`)
      return disabled
    },
    unplayableCards() {
      if (this.isPlayTimeDisabled) {
        console.log(`[PlayerHand] Play phase disabled → all ${this.cards.length} cards unplayable`)
        return this.cards
      }

      return this.$getUnplayableCards(this.cards, this.currentTrick, {
        trumpType: this.trump,
        players: this.players,
        myPlayerId: this.myId,
        playerTurnId: this.playerTurnId
      })
    },
    resolvedCards() {
      this.$setupTrump(this.trump)
      const orderedCards = this.$sortCards(this.cards)

      console.log(`[PlayerHand] Resolving cards for hand:`, {
        originalCards: this.cards,
        orderedCards,
        trump: this.trump,
        currentTrick: this.currentTrick,
        players: this.players,
        myPlayerId: this.myId,
        playerTurnId: this.playerTurnId,
        unplayableCards: this.unplayableCards
      })

      return orderedCards.map((value) => {
        const suit = this.$cardSuit(value)
        return {
          value,
          image: this.$cardImage(value),
          suit,
          color: this.$suitColor(suit),
          suitSymbol: this.$suitSymbol(suit),
          disabled: this.unplayableCards.includes(value)
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
      if (!card || card.disabled || this.isPlayTimeDisabled) {
        return
      }

      this.$emit('play-card', card.value)
    },
    setHoveredCard(row, index) {
      this.hoveredCard = { row, index }
    },
    clearHoveredCard() {
      this.hoveredCard = { row: null, index: null }
    },
    handCardStyle(row, index) {
      if (this.hoveredCard.row !== row || this.hoveredCard.index === null) {
        return {}
      }

      const zIndex = row === 'top' ? 20 : 5

      if (index === this.hoveredCard.index) {
        return {
          transform: 'translateY(-16px)',
          zIndex
        }
      }

      if (index === this.hoveredCard.index - 1) {
        return {
          transform: 'translateX(-2px)'
        }
      }

      if (index === this.hoveredCard.index + 1) {
        return {
          transform: 'translateX(2px)'
        }
      }

      return {}
    }
  }
}
</script>
