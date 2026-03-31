<script>
import Card from 'primevue/card'
import { useAuth } from '../services/useAuth'

export default {
  name: 'Home',
  components: {
    Card
  },
  data() {
    return {
      auth: useAuth()
    }
  },
  computed: {
    user() {
      return this.auth.user.value
    }
  }
}
</script>

<template>
  <div class="grid min-h-[calc(100vh-80px)] place-items-center px-6 py-8">
    <Card
      class="w-full max-w-2xl rounded-3xl border border-slate-200 bg-white shadow-lg shadow-slate-900/5"
      :pt="{
        body: { class: 'p-6 sm:p-8' },
        title: { class: 'mb-4 text-2xl font-bold text-[#1e3a8a]' },
        content: { class: 'space-y-3 p-0 text-base text-slate-600' }
      }"
    >
      <template #title>Bienvenue sur Coinche7</template>
      <template #content>
        <p>
          Authentification réussie<span v-if="user">, {{ user.pseudo }}</span>.
        </p>
        <p>Ton token est stocké côté client et envoyé automatiquement aux routes protégées.</p>
      </template>
    </Card>
  </div>
</template>