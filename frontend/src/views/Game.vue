<template>
  <div>
    <div v-if="gameManager && gameManager.isConnected && gameManager.gameStatus"
      class="relative text-center h-[calc(100vh-65px)] bg-cover bg-center bg-no-repeat lg:max-w-1/2 mx-auto rounded-t-2xl"
      style="background-image: url('/game_table.jpg');"
    >
      <GlobalMessage ref="globalMessage" />
      <Annonce ref="annonce" :game_manager="gameManager" />
  
      <div class="h-2/3 w-full p-1">
        <Table :game-manager="gameManager"/>
      </div>
      <div class="h-1/3 w-full p-1" v-if="gameManager.get_my_card().length > 0 && gameManager.gameStatus.bid">
        <PlayerHand
          :cards="gameManager.get_my_card()"
          :trump="gameManager.gameStatus.bid.trump"
          :current-trick="gameManager.gameStatus.current_trick"
          :players="gameManager.gameStatus.players"
          :my-id="gameManager.me.id"
          :player-turn-id="gameManager.gameStatus.player_turn"
          :action="gameManager.action"
          @play-card="handlePlayCard"
        />
      </div>
    </div>
    <div v-else class="flex items-center justify-center h-screen">
      <p class="text-2xl text-gray-500">Connexion au serveur de jeu...</p>
    </div>
    <GameFinished
      v-if="gameManager && gameManager.gameStatus && gameManager.gameStatus.metadata === 'FINISHED'"
      :show="gameManager.gameStatus.metadata === 'FINISHED'"
      :game-manager="gameManager"
      @go-home="$router.push('/')"
    />
  </div>

</template>

<script>
import Table from '../Components/Game/Table.vue';
import PlayerHand from '../Components/Game/PlayerHand.vue';
import GlobalMessage from '../Components/GlobalMessage.vue';
import Annonce from '../Components/Game/Annonce.vue';
import GameManager from '../services/GameManager.js';
import GameFinished from '../Components/Game/GameFinished.vue';

export default {
  name: 'Game',
  components: {
    Table,
    PlayerHand,
    GlobalMessage,
    Annonce,
    GameFinished
  },
  data() {
    return {
      gameManager: null,
      waitingMessageInterval: null
    }
  },
  mounted() {
    const wsUrl = this.$route.query.wsUrl ;
    console.log("Connecting to game with wsUrl:", wsUrl);
    this.gameManager = new GameManager(wsUrl);
    this.gameManager.connect();

  },
  watch: {
    'gameManager.gameStatus.metadata'(newState, oldState) {
      if (newState === oldState) return

      console.log(`[GameState] ${oldState} → ${newState}`)

      // Arrête le message WAITING permanent s'il y en a un
      if (this.waitingMessageInterval) {
        clearInterval(this.waitingMessageInterval)
        this.waitingMessageInterval = null
      }

      // Attendre que le DOM soit à jour pour utiliser globalMessage
      this.$nextTick(() => {
        if (!this.$refs.globalMessage) return

        switch (newState) {
          case 'WAITING':
            // Message permanent = réaffichage toutes les 3s
            this.$refs.globalMessage.show('EN attente d\'autres joueurs...', 2800, '#79f8f6')
            this.waitingMessageInterval = setInterval(() => {
              if (this.$refs.globalMessage) {
                this.$refs.globalMessage.show('EN attente d\'autres joueurs...', 2800, '#79f8f6')
              }
            }, 3000)
            break

          case 'DEALING':
            this.$refs.globalMessage.show('📦 Distribution des cartes', 1200, '#a78bfa')
            break

          case 'PLAYING':
            this.$refs.globalMessage.show('▶ Début du round', 1200, '#34d399')
            break

          case 'SCORING':
            this.$refs.globalMessage.show('📊 Calcul des points', 1200, '#fbbf24')
            break

          case 'FINISHED':
            this.$refs.globalMessage.show('🏁 Partie terminée', 2000, '#f87171')
            break
        }
      })
    }
  },
  beforeUnmount() {
    if (this.waitingMessageInterval) {
      clearInterval(this.waitingMessageInterval)
    }
  },
  methods: {
    handlePlayCard(cardCode) {
      if (!this.gameManager || !this.gameManager.gameStatus || !cardCode) {
        return
      }

      if (this.gameManager.gameStatus.player_turn !== this.gameManager.me.id) {
        return
      }

      this.gameManager.sendAction(cardCode)
    },
    async openAnnonceModal(annonce = null) {
      const result = await this.$refs.annonce.open('DEPART', {
        previousAnnonce: annonce,
        minPoints: 80,
        maxPoints: 500
      })

      if (result.action === 'PASS') {
        this.$refs.globalMessage.show('Passe', 1400, '#cbd5e1')
        return result
      }

      if (result.mode === 'DEPART') {
        if (result.type) {
          this.$refs.globalMessage.show(`${result.type}`, 1800, '#67e8f9')
          return result
        }

        this.annonce = {
          points: result.points,
          type: result.type
        }

        this.$refs.globalMessage.show(`${result.points} ${result.type}`, 1800, '#ffcc00')
        return result
      }

      this.$refs.globalMessage.show(`${result.type}`, 1800, '#67e8f9')
      return result
    }
  }
}
</script>