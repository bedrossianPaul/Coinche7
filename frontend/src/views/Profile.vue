<template>
  <div class="min-h-screen bg-slate-50 text-slate-900">
    <div class="mx-auto max-w-4xl px-4 py-8">
      <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold">Profil de {{ profile.pseudo || '...' }}</h1>
            <p class="text-sm text-slate-500">ID #{{ profile.id }}</p>
          </div>
          <div class="text-right">
            <div class="text-sm text-slate-500">ELO</div>
            <div class="text-3xl font-black">{{ profile.elo }}</div>
          </div>
        </div>

        <div class="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div class="rounded-lg border border-slate-100 bg-slate-50 p-4">
            <div class="text-sm text-slate-500">Parties jouées</div>
            <div class="text-xl font-semibold">{{ profile.stats.totalGames || 0 }}</div>
          </div>
          <div class="rounded-lg border border-slate-100 bg-slate-50 p-4">
            <div class="text-sm text-slate-500">Victoires</div>
            <div class="text-xl font-semibold">{{ profile.stats.wins || 0 }}</div>
          </div>
          <div class="rounded-lg border border-slate-100 bg-slate-50 p-4">
            <div class="text-sm text-slate-500">Taux de victoire</div>
            <div class="text-xl font-semibold">{{ Math.round((profile.stats.winRate || 0) * 10) / 10 }}%</div>
          </div>
        </div>

        <div class="mt-6">
          <h2 class="text-lg font-semibold">Historique des parties</h2>
          <div v-if="profile.games.length === 0" class="mt-4 text-sm text-slate-500">Aucune partie trouvée.</div>

          <ul v-else class="mt-4 space-y-3">
            <li v-for="game in profile.games" :key="game.id" :class="['rounded-2xl border p-4', game.playerWon ? 'border-emerald-200 bg-emerald-50' : 'border-slate-200 bg-slate-50']">
              <div class="flex items-center justify-between">
                <div>
                  <div class="flex items-center gap-2">
                    <div class="font-semibold">Partie #{{ game.id }}</div>
                    <span v-if="game.playerWon" class="rounded-full bg-emerald-600 px-2 py-1 text-xs font-bold text-white">Victoire</span>
                    <span v-else class="rounded-full bg-slate-400 px-2 py-1 text-xs font-bold text-white">Défaite</span>
                  </div>
                  <div class="text-xs text-slate-500">Terminée le {{ formatDate(game.finishedAt) }}</div>
                </div>
                <div class="text-right">
                  <div class="text-sm text-slate-500">Score</div>
                  <div class="font-bold">{{ game.team1Score }} - {{ game.team2Score }}</div>
                </div>
              </div>

              <div class="mt-3 flex gap-3">
                <div v-for="p in game.players" :key="p.id" class="rounded-lg border border-slate-100 bg-white px-3 py-1 text-sm">
                  <router-link :to="`/profile/${p.id}`" class="font-semibold text-slate-900">{{ p.pseudo }}</router-link>
                  <div class="text-xs text-slate-500">ELO: {{ p.elo || '-' }}</div>
                </div>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { api, useAuth } from '../services/useAuth'

export default {
  name: 'Profile',
  data() {
    return {
      profile: {
        id: null,
        pseudo: '',
        elo: 1000,
        games: [],
        stats: {}
      },
      auth: useAuth()
    }
  },
  async created() {
    const id = this.$route.params.id
    try {
      let res
      if (!id || id === 'me') {
        res = await api.get('/profile/me')
      } else {
        res = await api.get(`/profile/${id}`)
      }
      this.profile = res.data || this.profile
    } catch (e) {
      console.error('Error loading profile', e)
    }
  },
  methods: {
    formatDate(ts) {
      if (!ts) return ''
      const d = new Date(ts)
      return d.toLocaleString()
    }
  }
}
</script>
