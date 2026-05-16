<template>
    <div>
        <h1>Waiting Room</h1>
        <button @click="createGame">Create New Game</button>
        <div v-if="Object.keys(games).length === 0">
            No games available. Please create a new game.
        </div>
        <div v-else>
          <div v-for="(remaining_seats, id) in games" :key="id">
              Game {{ id }} - {{ remaining_seats }} seats remaining
              <button @click="joinGame(id)" :disabled="remaining_seats === 0">Join</button>
            </div>
        </div>
    </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'WaitingRoom',
  data() {
    return {
      games: {}
    }
  },
  methods: {
    async createGame() {
      try {
        const response = await axios.post('/api/waiting-room/create');
        console.log(response.data);
      } catch (error) {
        console.error('Error creating game:', error);
      }
    },
    // async joinGame(gameId) {
    //   try {
    //     const response = await axios.post(`/api/waiting-room/join?gameId=${gameId}`);
    //     const ws_url = response.data.ws_url;
    //     this.$router.push({ name: 'Game', params: { wsUrl: ws_url } });
    //   } catch (error) {
    //     console.error('Error joining game:', error);
    //   }
    // },
    async fetchGames() {
      try {
        const response = await axios.get('/api/waiting-room/list');
        this.games = response.data.games;
      } catch (error) {
        console.error('Error fetching games:', error);
      }
    }
  },
  mounted() {
    this.fetchGames();
    setInterval(this.fetchGames, 1000);
  }
}
</script>
