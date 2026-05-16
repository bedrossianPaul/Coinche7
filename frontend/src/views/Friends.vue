<template>
  <div class="min-h-screen bg-slate-50 text-slate-900">
    <div class="mx-auto flex min-h-screen max-w-6xl flex-col gap-6 px-4 py-8 sm:px-6 lg:px-8">
      <header class="rounded-3xl border border-slate-200 bg-white px-6 py-6 shadow-sm">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <h1 class="mt-2 text-3xl font-black tracking-tight text-slate-900 sm:text-4xl">Mes amis</h1>

          </div>

        </div>
      </header>

      <div class="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
        <section class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="mb-4 flex items-center justify-between gap-3">
            <div>
              <h2 class="text-lg font-semibold text-slate-900">Amis</h2>
              <p class="text-sm text-slate-500">{{ friends.length }} ami{{ friends.length > 1 ? 's' : '' }}</p>
            </div>
            <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">Liste actuelle</span>
          </div>

          <div v-if="friends.length === 0" class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-6 py-10 text-center text-slate-500">
            <p class="text-lg font-semibold text-slate-800">Aucun ami pour le moment</p>
            <p class="mt-2 text-sm">Il faut qu'une demande soit acceptée avant d'être amis.</p>
          </div>

          <div v-else class="flex flex-col gap-3">
            <article
              v-for="friend in friends"
              :key="friend.id"
              class="flex items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3"
            >
              <router-link :to="`/profile/${friend.id}`" class="flex items-center gap-3 no-underline text-inherit">
                <div class="flex h-12 w-12 items-center justify-center overflow-hidden rounded-full bg-cyan-100 text-sm font-black text-cyan-900">
                  <img
                    v-if="friend.avatarUrl"
                    :src="friend.avatarUrl"
                    :alt="friend.pseudo"
                    class="h-full w-full object-cover"
                  >
                  <span v-else>{{ initials(friend.pseudo) }}</span>
                </div>

                <div>
                  <p class="font-bold text-slate-900">{{ friend.pseudo }}</p>
                  <p class="text-sm text-slate-500">ID #{{ friend.id }}</p>
                </div>
              </router-link>

              <div class="flex items-center gap-3">
                <router-link
                  :to="`/profile/${friend.id}`"
                  class="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-cyan-200 hover:bg-cyan-50 hover:text-cyan-700 no-underline"
                >
                  Profil
                </router-link>
                <button
                  @click="removeFriend(friend.id)"
                  class="rounded-xl border border-slate-200 px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-rose-200 hover:bg-rose-50 hover:text-rose-700"
                >
                  Retirer
                </button>
              </div>
            </article>
          </div>

          <div class="mt-6 grid gap-4 sm:grid-cols-2">
            <div>
              <div class="mb-3 flex items-center justify-between">
                <h3 class="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">Demandes reçues</h3>
                <span class="text-xs font-semibold text-slate-400">{{ incomingRequests.length }}</span>
              </div>

              <div v-if="incomingRequests.length === 0" class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-6 text-sm text-slate-500">
                Aucune demande reçue.
              </div>

              <div v-else class="flex flex-col gap-3">
                <article v-for="request in incomingRequests" :key="request.requestId" class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                  <div class="flex items-center gap-3">
                    <div class="flex h-11 w-11 items-center justify-center rounded-full bg-cyan-100 text-xs font-black text-cyan-900">
                      {{ initials(request.pseudo) }}
                    </div>
                    <div class="min-w-0 flex-1">
                      <p class="truncate font-semibold text-slate-900">{{ request.pseudo }}</p>
                      <p class="text-xs text-slate-500">Demande reçue</p>
                    </div>
                  </div>

                  <div class="mt-3 flex justify-end gap-2">
                    <button class="rounded-xl border border-slate-200 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100" @click="rejectRequest(request.id)">Refuser</button>
                    <button class="rounded-xl bg-cyan-600 px-3 py-2 text-sm font-semibold text-white hover:bg-cyan-700" @click="acceptRequest(request.id)">Accepter</button>
                  </div>
                </article>
              </div>
            </div>

            <div>
              <div class="mb-3 flex items-center justify-between">
                <h3 class="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">Demandes envoyées</h3>
                <span class="text-xs font-semibold text-slate-400">{{ outgoingRequests.length }}</span>
              </div>

              <div v-if="outgoingRequests.length === 0" class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-6 text-sm text-slate-500">
                Aucune demande en attente.
              </div>

              <div v-else class="flex flex-col gap-3">
                <article v-for="request in outgoingRequests" :key="request.requestId" class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                  <div class="flex items-center gap-3">
                    <div class="flex h-11 w-11 items-center justify-center rounded-full bg-slate-200 text-xs font-black text-slate-700">
                      {{ initials(request.pseudo) }}
                    </div>
                    <div class="min-w-0 flex-1">
                      <p class="truncate font-semibold text-slate-900">{{ request.pseudo }}</p>
                      <p class="text-xs text-slate-500">Demande envoyée</p>
                    </div>
                  </div>
                </article>
              </div>
            </div>
          </div>
        </section>

        <section class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="mb-4">
            <h2 class="text-lg font-semibold text-slate-900">Rechercher un joueur</h2>
            <p class="text-sm text-slate-500">Tape un pseudo pour filtrer les utilisateurs.</p>
          </div>

          <form class="flex gap-3" @submit.prevent="searchUsers">
            <InputText
              v-model="searchQuery"
              placeholder="Pseudo à rechercher"
              class="w-full"
              @keyup.enter="searchUsers"
            />
            <Button label="Rechercher" type="submit" class="whitespace-nowrap" />
          </form>

          <div class="mt-4 flex items-center gap-2 text-sm text-slate-500">
            <span class="h-2 w-2 rounded-full bg-emerald-400"></span>
            {{ results.length }} résultat{{ results.length > 1 ? 's' : '' }}
          </div>

          <div v-if="loading" class="mt-4 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-6 text-center text-slate-500">
            Recherche en cours...
          </div>

          <div v-else class="mt-4 flex flex-col gap-3">
            <article
              v-for="user in results"
              :key="user.id"
              class="flex items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3"
            >
              <div class="flex items-center gap-3">
                <div class="flex h-12 w-12 items-center justify-center overflow-hidden rounded-full bg-slate-200 text-sm font-black text-slate-700">
                  <img
                    v-if="user.avatarUrl"
                    :src="user.avatarUrl"
                    :alt="user.pseudo"
                    class="h-full w-full object-cover"
                  >
                  <span v-else>{{ initials(user.pseudo) }}</span>
                </div>

                <div>
                  <p class="font-bold text-slate-900">{{ user.pseudo }}</p>
                  <p class="text-sm text-slate-500">ID #{{ user.id }}</p>
                </div>
              </div>

              <div class="flex items-center gap-2">
                <span v-if="user.status === 'FRIEND'" class="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">Ami</span>
                <span v-else-if="user.status === 'PENDING_OUT'" class="rounded-full bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-700">En attente</span>
                <span v-else-if="user.status === 'PENDING_IN'" class="rounded-full bg-sky-50 px-3 py-1 text-xs font-semibold text-sky-700">Demande reçue</span>
                <Button
                  v-else
                  label="Demander"
                  severity="primary"
                  @click="addFriend(user.id)"
                />
              </div>
            </article>

            <div v-if="results.length === 0" class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-6 py-10 text-center text-slate-500">
              Aucun utilisateur trouvé.
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script>
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import { api, useAuth } from '../services/useAuth'

export default {
  name: 'Friends',
  components: {
    Button,
    InputText
  },
  data() {
    return {
      auth: useAuth(),
      friends: [],
      incomingRequests: [],
      outgoingRequests: [],
      results: [],
      searchQuery: '',
      loading: false
    }
  },
  computed: {
    currentUser() {
      return this.auth.user || null
    },
    friendIds() {
      return new Set(this.friends.map((friend) => friend.id))
    }
  },
  mounted() {
    this.loadFriends()
    this.searchUsers()
  },
  methods: {
    initials(pseudo) {
      const value = String(pseudo || '').trim()
      if (!value) return '?'
      return value.slice(0, 2).toUpperCase()
    },
    isFriend(userId) {
      return this.friendIds.has(userId)
    },
    async loadFriends() {
      try {
        const { data } = await api.get('/friends')
        this.friends = data.friends || []
        this.incomingRequests = data.incomingRequests || []
        this.outgoingRequests = data.outgoingRequests || []
      } catch (error) {
        console.error('Error loading friends:', error)
      }
    },
    async searchUsers() {
      this.loading = true

      try {
        

            const { data } = await api.get('/friends/search', {
              params: {
                q: this.searchQuery.trim()
              }
            })
            this.results = data.results || []
        

      } catch (error) {
        console.error('Error searching users:', error)
        this.results = []
      } finally {
        this.loading = false
      }
    },
    async addFriend(friendId) {
      try {
        const { data } = await api.post('/friends/request', null, {
          params: { friendId }
        })

        if (data.friends) this.friends = data.friends || []
        await this.searchUsers()
        await this.loadFriends()
      } catch (error) {
        console.error('Error adding friend:', error)
      }
    },
    async acceptRequest(friendId) {
      try {
        const { data } = await api.post('/friends/accept', null, {
          params: { friendId }
        })

        this.friends = data.friends || []
        await this.loadFriends()
        await this.searchUsers()
      } catch (error) {
        console.error('Error accepting friend request:', error)
      }
    },
    async rejectRequest(friendId) {
      try {
        await api.delete('/friends/reject', {
          params: { friendId }
        })

        await this.loadFriends()
        await this.searchUsers()
      } catch (error) {
        console.error('Error rejecting friend request:', error)
      }
    },
    async removeFriend(friendId) {
      try {
        const { data } = await api.delete('/friends', {
          params: { friendId }
        })

        this.friends = data.friends || []
        await this.loadFriends()
        await this.searchUsers()
      } catch (error) {
        console.error('Error removing friend:', error)
      }
    }
  }
}
</script>