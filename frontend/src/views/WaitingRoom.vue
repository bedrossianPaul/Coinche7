<template>
  <div class="min-h-screen bg-slate-50 text-slate-900">
    <div class="mx-auto flex min-h-screen max-w-5xl flex-col px-4 py-8 sm:px-6 lg:px-8">
      <header class="mb-8 rounded-3xl border border-slate-200 bg-white px-6 py-5 shadow-sm">
        <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h1 class="mt-2 text-3xl font-black tracking-tight text-slate-900 sm:text-4xl">Salle d’attente</h1>
            <p class="mt-2 max-w-2xl text-sm leading-relaxed text-slate-600">
              Crée une nouvelle partie ou rejoins une table déjà ouverte. L’affichage se met à jour automatiquement.
            </p>
          </div>

          <button
            @click="createGame"
            class="inline-flex items-center justify-center rounded-xl bg-cyan-600 px-5 py-3 text-sm font-bold text-white shadow-sm transition hover:bg-cyan-700 active:scale-[0.99]"
          >
            Créer une partie
          </button>
        </div>
      </header>

      <main class="flex-1">
        <section class="mb-4 flex items-center justify-between">
          <div>
            <h2 class="text-lg font-semibold text-slate-900">Parties disponibles</h2>
          </div>
          <div class="rounded-full border border-slate-200 bg-white px-3 py-1 text-xs font-semibold text-slate-600 shadow-sm">
            {{ gamesCount }} partie{{ gamesCount > 1 ? 's' : '' }}
          </div>
        </section>

        <div v-if="gamesCount === 0" class="rounded-3xl border border-dashed border-slate-200 bg-white px-6 py-16 text-center text-slate-500 shadow-sm">
          <div class="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-full bg-cyan-50 text-2xl">🎴</div>
          <p class="text-lg font-semibold text-slate-900">Aucune partie disponible</p>
          <p class="mt-2 text-sm text-slate-500">Crée une partie pour commencer une nouvelle table.</p>
        </div>

        <div v-else class="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          <article
            v-for="(remaining_seats, id) in games"
            :key="id"
            class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:border-cyan-300"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.25em] text-slate-400">Partie #{{ id }}</p>
                <p class="mt-2 text-2xl font-black text-slate-900">{{ remaining_seats }}</p>
                <p class="text-sm text-slate-500">place{{ remaining_seats > 1 ? 's' : '' }} restante{{ remaining_seats > 1 ? 's' : '' }}</p>
              </div>

              <span
                class="rounded-full px-3 py-1 text-xs font-bold"
                :class="remaining_seats === 0 ? 'bg-slate-100 text-slate-500' : 'bg-emerald-50 text-emerald-700'"
              >
                {{ remaining_seats === 0 ? 'Complète' : 'Ouverte' }}
              </span>
            </div>

            <div class="mt-4 h-2 overflow-hidden rounded-full bg-slate-100">
              <div
                class="h-full rounded-full transition-all"
                :class="remaining_seats === 0 ? 'bg-slate-300' : 'bg-cyan-500'"
                :style="{ width: `${Math.max(0, Math.min(4, 4 - remaining_seats)) * 25}%` }"
              ></div>
            </div>

            <div class="mt-4 flex justify-end">
              <button
                @click="joinGame(id)"
                :disabled="remaining_seats === 0"
                class="rounded-xl px-4 py-2 text-sm font-semibold transition"
                :class="remaining_seats === 0
                  ? 'cursor-not-allowed bg-slate-100 text-slate-400'
                  : 'bg-slate-900 text-white hover:bg-cyan-700'"
              >
                Rejoindre
              </button>
            </div>
          </article>
        </div>
      </main>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'WaitingRoom',
  data() {
    return {
      games: {},
      refreshTimer: null
    }
  },
  methods: {
    async createGame() {
      try {
        const response = await axios.post('/api/waiting-room/create');
        console.log(response.data);
        this.joinGame(response.data.id)
      } catch (error) {
        console.error('Error creating game:', error);
      }
    },
    async joinGame(gameId) {
      try {
        const response = await axios.post(`/api/waiting-room/join?gameId=${gameId}`);
        const wsPath = response.data.ws_url;
        const protocol = 'ws';
        const host = "localhost:8080";
        const ws_url = `${protocol}://${host}${wsPath}`;
        console.log('WebSocket URL:', ws_url);
        this.$router.push({ name: 'Game', query: { wsUrl: ws_url } });
      } catch (error) {
        console.error('Error joining game:', error);
      }
    },
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
    this.refreshTimer = setInterval(this.fetchGames, 1000);
  },
  beforeUnmount() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
      this.refreshTimer = null
    }
  },
  computed: {
    gamesCount() {
      return Object.keys(this.games || {}).length
    }
  }
}
</script>
