<template>
    <div class="flex flex-col gap-1 w-fit">
        <div class="flex items-center">
            <!--Avatar | Default avatar-->
        <div class="relative flex h-16 w-16 items-center justify-center z-10">
          <div
            class="absolute inset-0 rounded-full"
            :style="timerStyle"
          ></div>
          <div class="relative h-14 w-14 rounded-full bg-white shadow-2xl">
            <img src="/icons/avatar.png" alt="Avatar" class="h-full w-full rounded-full object-cover">
          </div>
            </div>
            <div class="w-26 h-12 bg-[rgba(255,255,255,0.3)] flex flex-col gap-1 justify-end rounded-xl px-2 py-1 relative -left-6">
                <!--Player name-->
                <div class="text-sm font-bold w-full text-right truncate">{{ player.name }}</div>
                <!--Player score-->
                <div class="text-xs font-bold w-full text-right">{{ player.elo }}</div>
        
            </div>

        </div>
        <div class="relative flex justify-between items-center -left-6">
          <div class="relative">
            <div v-if="player.is_first" class="relative left-16 rounded-full w-6 h-6 shadow-md">
                <img src="/icons/first_player.png" alt="First" class="h-full w-full object-cover">
            </div>
          </div>
            <div v-if="game_manager.gameStatus.bid" class="relative mr-0">
              <div class="pointer-events-none absolute -inset-1 rounded-md bg-white/20 blur-sm ring-1 ring-white/70 shadow-lg animate-pulse"></div>
              <div class="relative rounded-md bg-gray-200/40 px-2 py-1 text-sm font-bold" :class="annonceTextColorClass">
                {{ game_manager.gameStatus.bid.points }} {{ annonceTypeSymbol }}
              </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
  name: 'Player',
  data() {
    return {
      timerProgress: 100,
      timerInterval: null
    }
  },
  props: {
    player: {
      type: Object,
      required: true
    },
    game_manager: {
      type: Object,
      required: true
    }
  },
  computed: {
    annonceSuitKey() {
      const rawType = this.game_manager.gameStatus.bid?.type

      if (!rawType) {
        return null
      }

      const type = String(rawType).trim().toLowerCase()
      const suitMap = {
        s: 'S',
        spade: 'S',
        spades: 'S',
        pique: 'S',
        p: 'S',

        h: 'H',
        heart: 'H',
        hearts: 'H',
        coeur: 'H',
        cœurs: 'H',

        d: 'D',
        diamond: 'D',
        diamonds: 'D',
        carreau: 'D',
        carreaux: 'D',

        c: 'C',
        club: 'C',
        clubs: 'C',
        trefle: 'C',
        trèfle: 'C',
        trefles: 'C',
        trèfles: 'C'
      }

      return suitMap[type] || null
    },
    annonceTypeSymbol() {
      const symbols = {
        S: '♠',
        H: '♥',
        D: '♦',
        C: '♣'
      }

      return this.annonceSuitKey ? symbols[this.annonceSuitKey] : String(this.annonce?.type || '')
    },
    annonceTextColorClass() {
      if (this.annonceSuitKey === 'H' || this.annonceSuitKey === 'D') {
        return 'text-red-600'
      }

      if (this.annonceSuitKey === 'S' || this.annonceSuitKey === 'C') {
        return 'text-slate-900'
      }

      return 'text-slate-700'
    },
    timerStyle() {
      const elapsedAngle = (1 - this.timerProgress / 100) * 360
      const p = this.timerProgress / 100

      // Interpolation HSL : vert néon (115°) → jaune → orange → rouge (0°)
      const hue = Math.round(115 * p)
      const solid = `hsl(${hue}, 100%, 50%)`

      return {
        background: `conic-gradient(transparent 0deg, transparent ${elapsedAngle}deg, ${solid} ${elapsedAngle}deg, ${solid} 360deg)`
      }
    }
  },
  methods: {
    playTimer(duration = 10000) {
      if (this.timerInterval) {
        clearInterval(this.timerInterval)
      }

      const startTime = Date.now()
      this.timerProgress = 100

      this.timerInterval = setInterval(() => {
        const elapsed = Date.now() - startTime
        const remaining = Math.max(0, duration - elapsed)

        this.timerProgress = (remaining / duration) * 100

        if (remaining <= 0) {
          clearInterval(this.timerInterval)
          this.timerInterval = null
        }
      }, 50)
    }
  },
  beforeUnmount() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval)
    }
  },
  mounted(){
    this.playTimer(50000)
  }
}
</script>