<template>
  <div class="h-full w-full flex justify-center">
    <div class="w-full rounded-xl bg-slate-900/20 p-3">
      <div class="relative z-0 flex justify-center">
        <div class="flex -space-x-2">
          <div
            v-for="(card, index) in topHandCards"
            :key="`top-${card.value}-${index}`"
            class="transition-all duration-200"
            :class="{ 'z-20': hoveredCard.row === 'top' && hoveredCard.index === index, 'z-6': hoveredCard.row === 'top' && Math.abs(hoveredCard.index - index) === 1 }"
            :style="handCardStyle('top', index)"
            @mouseenter="setHoveredCard('top', index)"
            @mouseleave="clearHoveredCard"
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
            :class="{ 'z-5': hoveredCard.row === 'bottom' && hoveredCard.index === index }"
            :style="handCardStyle('bottom', index)"
            @mouseenter="setHoveredCard('bottom', index)"
            @mouseleave="clearHoveredCard"
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
    const { getCardImage, getCardSuit, getSuitSymbol, getSuitColor, setupTrump, sortCards } = useCard()
    this.$cardImage = getCardImage
    this.$cardSuit = getCardSuit
    this.$suitSymbol = getSuitSymbol
    this.$suitColor = getSuitColor
    this.$setupTrump = setupTrump
    this.$sortCards = sortCards
  },
  props: {
    cards: {
      type: Array,
      default: () => []
    },
    trump: {
      type: String,
      default: 'SA'
    }
  },
  computed: {
    resolvedCards() {
      this.$setupTrump(this.trump)
      const orderedCards = this.$sortCards(this.cards)

      return orderedCards.map((value) => {
        const suit = this.$cardSuit(value)
        return {
          value,
          image: this.$cardImage(value),
          suit,
          color: this.$suitColor(suit),
          suitSymbol: this.$suitSymbol(suit),
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
