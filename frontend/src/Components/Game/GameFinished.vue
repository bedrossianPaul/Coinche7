<template>
  <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
    <div class="rounded-3xl border border-slate-200 bg-white shadow-2xl w-full max-w-md mx-4">
      <div class="px-6 py-8">
        <!-- Header avec gagnant -->
        <div class="text-center mb-6">
          <div v-if="playerWon" class="text-6xl mb-4">🏆</div>
          <div v-else class="text-6xl mb-4">💔</div>
          
          <h1 v-if="playerWon" class="text-3xl font-black text-emerald-600 mb-2">
            Vous avez gagné !
          </h1>
          <h1 v-else class="text-3xl font-black text-slate-700 mb-2">
            Défaite
          </h1>
        </div>

        <!-- Score -->
        <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 mb-6">
          <div class="text-center">
            <div class="text-sm text-slate-500 uppercase tracking-wider mb-2">Score final</div>
            <div class="text-3xl font-black">
              <span :class="playerWon ? 'text-emerald-600' : 'text-slate-700'">
                {{ gameManager?.gameStatus?.score?.scoreTeam1 || 0 }}
              </span>
              <span class="text-slate-400 mx-3">-</span>
              <span :class="!playerWon && gameManager?.gameStatus?.score?.scoreTeam2 >= (gameManager?.gameStatus?.score?.targetScore || 250) ? 'text-slate-700' : 'text-slate-400'">
                {{ gameManager?.gameStatus?.score?.scoreTeam2 || 0 }}
              </span>
            </div>
          </div>
        </div>

        <!-- Changement ELO -->
        <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 mb-6">
          <div class="text-center">
            <div class="text-sm text-slate-500 uppercase tracking-wider mb-3">Changement ELO</div>
            <div class="flex items-center justify-center gap-4">
              <div>
                <div class="text-xs text-slate-500 mb-1">Avant</div>
                <div class="text-2xl font-bold text-slate-700">{{ eloBeforeFinal }}</div>
              </div>
              <div v-if="eloDelta >= 0" class="text-emerald-600 font-bold text-xl">
                +{{ eloDelta }}
              </div>
              <div v-else class="text-rose-600 font-bold text-xl">
                {{ eloDelta }}
              </div>
              <div>
                <div class="text-xs text-slate-500 mb-1">Après</div>
                <div class="text-2xl font-bold" :class="eloDelta >= 0 ? 'text-emerald-600' : 'text-rose-600'">
                  {{ eloAfterFinal }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Message résumé -->
        <div class="text-center mb-6">
          <p v-if="playerWon" class="text-slate-600">
            Bravo ! Vous avez remporté cette partie avec une équipe performante.
          </p>
          <p v-else class="text-slate-600">
            Bonne chance pour la prochaine ! Vous progresserez avec l'expérience.
          </p>
        </div>

        <!-- Bouton retour -->
        <button
          @click="goHome"
          class="w-full rounded-xl bg-cyan-600 px-4 py-3 text-sm font-bold text-white transition hover:bg-cyan-700 active:scale-95"
        >
          Retour à l'accueil
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuth } from '../../services/useAuth';

export default {
  name: 'GameFinished',
  props: {
    gameManager: {
      type: Object,
      default: null
    },
    show: {
      type: Boolean,
      default: false
    }

  },
  emits: ['go-home'],
  data() {
    return {
      eloBeforeFinal: useAuth().user?.elo,
      eloAfterFinal: 0,
      eloDelta: 0,
      playerWon: false
    }
  },
    watch: {
        gameManager: {
        handler() {
            this.calculateEloChange()
        },
        immediate: true
        },
        show(newValue) {
        if (newValue) {
            this.calculateEloChange()
        }
        }
    },
  methods: {
    calculateEloChange() {
      // L'ELO final est stocké dans Player.elo après applyGameEloRewards
      // On calcule le delta
      const myId = this.gameManager?.me?.id
      console.log(this.gameManager.me)
      this.eloBeforeFinal = this.gameManager?.me?.elo
      const playersObj = this.gameManager?.gameStatus?.players || {}
      const players = Object.values(playersObj)

      let myPlayer = players.find(p => p.id === myId)
      if (!myPlayer) return
      // Récupérer l'ELO actuel du joueur
      this.eloAfterFinal = myPlayer.elo
      
      
      this.eloDelta = this.eloAfterFinal - this.eloBeforeFinal
      
      // Déterminer si le joueur a gagné
      const winnerTeam = this.gameManager?.gameStatus?.score?.winnerTeam
      const myTeam = myPlayer.team
      this.playerWon = this.eloDelta > 0
    },
    goHome() {
      this.$emit('go-home')
    }
  }
}
</script>
