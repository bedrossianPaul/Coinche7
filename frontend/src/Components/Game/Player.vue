<template>
  <div class="flex flex-col gap-1 w-fit" :class="isLocalPlayer ? 'scale-[1.03]' : ''">
    <!-- Badge Équipe -->
    <div v-if="playerTeam" class="flex justify-center mb-1">
      <span :class="['rounded px-2 py-1 text-xs font-bold', teamBadgeColor]">
        {{ playerTeam }}
      </span>
    </div>
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
            <div class="w-26 h-12 bg-[rgba(255,255,255,0.3)] flex flex-col gap-1 justify-end rounded-xl px-2 py-1 relative -left-6" :class="isLocalPlayer ? 'bg-cyan-100/60 ring-2 ring-cyan-300 shadow-lg' : ''">
                <!--Player name-->
                <div class="flex items-center justify-end gap-1 w-full">
                  <span v-if="isLocalPlayer" class="rounded-full bg-cyan-600 px-1.5 py-0.5 text-[10px] font-extrabold uppercase tracking-wide text-white shadow-sm">
                    Vous
                  </span>
                  <div class="text-sm font-bold text-right truncate" :class="isLocalPlayer ? 'text-cyan-950' : ''">{{ player.name }}</div>
                </div>
                <!--Player score-->
                <div class="text-xs font-bold w-full text-right" :class="isLocalPlayer ? 'text-cyan-950' : ''">{{ player.elo }}</div>
        
            </div>

        </div>
        <div class="relative flex justify-between items-center -left-6">
          <div class="relative">
            <div v-if="player.is_first" class="relative left-16 rounded-full w-6 h-6 shadow-md">
                <img src="/icons/first_player.png" alt="First" class="h-full w-full object-cover">
            </div>
          </div>
            <div v-if="game_manager.gameStatus.bid && game_manager.gameStatus.bid.bidder == player.id" class="relative mr-0">
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
      timerProgress: 0,
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
    isLocalPlayer() {
      return this.game_manager?.me?.id != null && this.player?.id === this.game_manager.me.id
    },
    playerPosition() {
      // Cherche la position du joueur en parcourant les clés de players
      const players = this.game_manager?.gameStatus?.players || {}
      for (const [position, playerData] of Object.entries(players)) {
        if (playerData?.id === this.player?.id) {
          return position.toUpperCase()
        }
      }
      return null
    },
    playerTeam() {
      // Team 1: NORTH, SOUTH | Team 2: EAST, WEST
      if (this.playerPosition === 'NORTH' || this.playerPosition === 'SOUTH') {
        return 'Team 1'
      }
      if (this.playerPosition === 'EAST' || this.playerPosition === 'WEST') {
        return 'Team 2'
      }
      return null
    },
    teamBadgeColor() {
      if (this.playerPosition === 'NORTH' || this.playerPosition === 'SOUTH') {
        return 'bg-blue-500 text-white'
      }
      if (this.playerPosition === 'EAST' || this.playerPosition === 'WEST') {
        return 'bg-red-500 text-white'
      }
      return 'bg-slate-400 text-white'
    },
    annonceSuitKey() {
      const rawType = this.game_manager.gameStatus.bid?.suit

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

      return this.annonceSuitKey ? symbols[this.annonceSuitKey] : String(this.annonce?.suit || '')
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
    // Met à jour la progression du timer en fonction du remaining (en secondes)
    updateTimerFromRemaining(initialSeconds, remainingSeconds) {
      if (this.timerInterval) {
        clearInterval(this.timerInterval)
        this.timerInterval = null
      }

      const initial = Math.max(1, Number(initialSeconds) || 1)
      const remaining = Math.max(0, Number(remainingSeconds) || 0)

      const progress = (remaining / initial) * 100
      this.timerProgress = Math.max(0, Math.min(100, progress))
    }
  },
  watch: {
  'game_manager.gameStatus'(new_gm, old_gm) {
      console.log('action changée', old_gm, '→', new_gm)

      if (new_gm != null && this.player.id == new_gm.player_turn) {
        console.log('Time remaining:', new_gm.time_remaining)
        // Détecte la phase pour connaître le timeout initial (doit correspondre au backend)
        const phase = (new_gm.metadata || '').toString()
        const initial = (phase === 'BIDDING') ? 30 : (phase === 'PLAYING') ? 20 : 20
        this.updateTimerFromRemaining(initial, new_gm.time_remaining)
      } else {
        this.timerProgress = 0
      }
    },
    'game_manager.action'(newAction, oldAction) {
      console.log(`Action changed: ${oldAction} → ${newAction}`)
      clearInterval(this.timerInterval)
      this.timerProgress = 100
    }
  },
  beforeUnmount() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval)
    }
  },
}
</script>