<template>
  <div 
    class="relative text-center h-[calc(100vh-65px)] bg-cover bg-center bg-no-repeat lg:max-w-1/2 mx-auto rounded-t-2xl"
    style="background-image: url('/game_table.jpg');"
  >
    <GlobalMessage ref="globalMessage" />
    <Annonce ref="annonce" />

    <div class="h-2/3 w-full p-1">
      <Table :players="players" :score="score"/>
    </div>
    <div class="h-1/3 w-full p-1">
      <PlayerHand :cards="playerHand" trump="H"/>
    </div>
  </div>

</template>

<script>
import Table from '../Components/Game/Table.vue';
import PlayerHand from '../Components/Game/PlayerHand.vue';
import GlobalMessage from '../Components/GlobalMessage.vue';
import Annonce from '../Components/Game/Annonce.vue';
import { useCard } from '../services/useCard.js';

export default {
  name: 'Game',
  data() {
    return {
      players: [
        { pseudo: 'Player1', elo: 1500 },
        { pseudo: 'Player2', elo: 1400 },
        { pseudo: 'Player3', elo: 1300 },
        { pseudo: 'Player4', elo: 1200 }
      ],
      score: { us: 0, them: 0 },
      playerHand: ['AH', '10H', 'JH', 'QH', '10S', 'KS', 'QS'],
      annonce: {points: 100, type: 'H'}
    }
  },
  mounted() {
    this.$refs.globalMessage.show('Bienvenue !', 2000, '#ffcc00');
    this.$refs.globalMessage.show('Annonces !', 2000, '#00ccff');
    this.openAnnonceModal(this.annonce)
    useCard().setupTrump('H');

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