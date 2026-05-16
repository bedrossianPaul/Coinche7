<script>
import Card from 'primevue/card'
import Button from 'primevue/button'
import { useAuth } from '../services/useAuth'

export default {
  name: 'Home',
  components: {
    Card,
    Button
  },
  data() {
    return {
      auth: useAuth()
    }
  },
  computed: {
    user() {
      return this.auth.user || null
    },
    quickActions() {
      return [
        {
          label: 'Mes amis',
          description: 'Gérer tes demandes et ta liste.',
          icon: 'pi pi-users',
          path: '/friends',
          tone: 'bg-sky-50 text-sky-700 border-sky-200'
        },
        {
          label: 'Jouer',
          description: 'Rejoindre ou créer une partie.',
          icon: 'pi pi-play-circle',
          path: '/waiting-room',
          tone: 'bg-emerald-50 text-emerald-700 border-emerald-200'
        },
        {
          label: 'Chat',
          description: 'Discute avec les autres joueurs.',
          icon: 'pi pi-comment',
          path: '/chat',
          tone: 'bg-purple-50 text-purple-700 border-purple-200'
        }
      ]
    }
  },
  methods: {
    openPath(path) {
      this.$router.push(path)
    },
    initials(pseudo) {
      const value = String(pseudo || '').trim()
      return value ? value.slice(0, 2).toUpperCase() : '?'
    }
  }
}
</script>

<template>
  <div class="min-h-[calc(100vh-80px)] bg-slate-50 px-4 py-8 sm:px-6 lg:px-8">
    <div class="mx-auto flex max-w-6xl flex-col gap-6">
      <section class="overflow-hidden rounded-4xl border border-slate-200 bg-white shadow-sm">
        <div class="grid gap-6 p-6 lg:grid-cols-[1.1fr_0.9fr] lg:p-8">
          <div class="flex flex-col justify-between gap-6">
            <div>
              <h1 class="mt-3 text-4xl font-black tracking-tight text-slate-900 sm:text-5xl">
                Bienvenue<span v-if="user">, {{ user.pseudo }}</span>
              </h1>
              <p>Dashboard</p>
            </div>
          </div>

          
        </div>
      </section>

      <section class="grid gap-4 md:grid-cols-3">
        <Card
          v-for="item in quickActions"
          :key="item.label"
          class="rounded-3xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
          :pt="{ body: { class: 'p-6' }, content: { class: 'p-0' } }"
        >
          <template #content>
            <div class="flex h-full flex-col gap-4">
              <div class="flex items-start justify-between gap-3">
                <div class="flex h-12 w-12 items-center justify-center rounded-2xl border" :class="item.tone">
                  <i :class="item.icon"></i>
                </div>
                <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-500">Accès rapide</span>
              </div>

              <div>
                <h2 class="text-xl font-bold text-slate-900">{{ item.label }}</h2>
                <p class="mt-2 text-sm leading-relaxed text-slate-600">{{ item.description }}</p>
              </div>

              <div class="mt-auto flex justify-end">
                <Button :label="item.label" class="rounded-xl" @click="openPath(item.path)" />
              </div>
            </div>
          </template>
        </Card>
      </section>

      
    </div>
  </div>
</template>