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
        <PlayerHand :cards="gameManager.get_my_card()" :trump="gameManager.gameStatus.bid.trump"/>
      </div>
    </div>
    <div v-else class="flex items-center justify-center h-screen">
      <p class="text-2xl text-gray-500">Connexion au serveur de jeu...</p>
    </div>
  </div>

</template>

<script>
import Table from '../Components/Game/Table.vue';
import PlayerHand from '../Components/Game/PlayerHand.vue';
import GlobalMessage from '../Components/GlobalMessage.vue';
import Annonce from '../Components/Game/Annonce.vue';
import { useCard } from '../services/useCard.js';
import GameManager from '../services/GameManager.js';

export default {
  name: 'Game',
  data() {
    return {
      gameManager: null,
    }
  },
  mounted() {
    this.gameManager = new GameManager('ws://localhost:8080/ws/game');
    this.gameManager.connect();

  },
  methods: {
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
  },
  components: {
    Table,
    PlayerHand,
    GlobalMessage,
    Annonce
  }
}
</script>