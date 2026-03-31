<script>
const RANK_LABELS = {
  '7': '7',
  '8': '8',
  '9': '9',
  T: '10',
  J: 'V',
  Q: 'D',
  K: 'R',
  A: 'A'
}

const SUIT_MAP = {
  S: { symbol: '♠', color: 'text-slate-800', accent: 'from-slate-100 to-slate-50' },
  H: { symbol: '♥', color: 'text-rose-600', accent: 'from-rose-50 to-white' },
  D: { symbol: '♦', color: 'text-red-500', accent: 'from-red-50 to-white' },
  C: { symbol: '♣', color: 'text-emerald-700', accent: 'from-emerald-50 to-white' }
}

export default {
  name: 'PlayingCard',
  emits: ['click', 'drag-start', 'drag-end'],
  props: {
    code: {
      type: String,
      required: true
    },
    selected: {
      type: Boolean,
      default: false
    },
    expanded: {
      type: Boolean,
      default: false
    },
    size: {
      type: String,
      default: 'hand'
    },
    draggable: {
      type: Boolean,
      default: false
    },
    dragging: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    onDragStart(event) {
      if (!this.draggable) {
        event.preventDefault()
        return
      }

      event.dataTransfer.effectAllowed = 'move'
      event.dataTransfer.setData('text/plain', this.code)
      this.$emit('drag-start', this.code)
    },
    onDragEnd() {
      this.$emit('drag-end')
    }
  },
  computed: {
    rank() {
      return this.code.slice(0, -1)
    },
    suit() {
      return this.code.slice(-1)
    },
    displayRank() {
      return RANK_LABELS[this.rank] || this.rank
    },
    suitData() {
      return SUIT_MAP[this.suit] || SUIT_MAP.S
    },
    cardClasses() {
      return [
        'bg-linear-to-br',
        this.suitData.accent,
        this.suitData.color
      ]
    },
    sizeClasses() {
      if (this.size === 'trick') {
        return 'h-24 w-16 rounded-2xl p-2.5 sm:h-32 sm:w-24 sm:p-3'
      }

      if (this.expanded) {
        return 'h-28 w-20 rounded-2xl p-3 sm:h-36 sm:w-24 sm:p-4'
      }

      return 'h-20 w-14 rounded-xl p-2 sm:h-28 sm:w-20 sm:rounded-2xl sm:p-3'
    },
    stateClasses() {
      if (this.dragging) {
        return 'z-20 -translate-y-5 scale-105 opacity-55 ring-2 ring-emerald-400 shadow-2xl shadow-emerald-950/30'
      }

      return this.selected
        ? 'z-10 -translate-y-3 ring-2 ring-amber-400 shadow-xl shadow-amber-950/20'
        : 'hover:-translate-y-1'
    },
    interactionClasses() {
      return this.draggable ? 'cursor-grab active:cursor-grabbing' : 'cursor-pointer'
    }
  }
}
</script>

<template>
  <div
    :class="[
      cardClasses,
      sizeClasses,
      stateClasses,
      interactionClasses,
      'flex flex-col justify-between border border-slate-200 text-left shadow-md shadow-slate-900/10 transition-all duration-150 select-none'
    ]"
    :draggable="draggable"
    @click="$emit('click', code)"
    @dragstart="onDragStart"
    @dragend="onDragEnd"
  >
    <div class="flex items-center gap-1 leading-none">
      <div class="text-[11px] font-black sm:text-sm">{{ displayRank }}</div>
      <div class="text-sm sm:text-base">{{ suitData.symbol }}</div>
    </div>

    <div class="flex items-center justify-center text-xl font-black opacity-80 sm:text-3xl">
      {{ suitData.symbol }}
    </div>

    <div class="flex items-center justify-end gap-1 leading-none">
      <span class="text-[11px] font-black sm:text-sm">{{ displayRank }}</span>
      <span class="text-sm sm:text-base">{{ suitData.symbol }}</span>
    </div>
  </div>
</template>
