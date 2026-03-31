<script>
import AppNavigation from './Components/AppNavigation.vue'
import { useAuth } from './services/useAuth'

export default {
  name: 'App',
  components: {
    AppNavigation
  },
  computed: {
    isAuthenticated() {
      return useAuth().isAuthenticated.value
    },
    user() {
      return useAuth().user.value
    }
  },
  methods: {
    handleLogout() {
      useAuth().logout()
      this.$router.push('/auth')
    }
  }
}
</script>

<template>
  <div id="app" class="flex min-h-screen flex-col">
    <nav
      v-if="isAuthenticated"
      class="sticky top-0 z-50 flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3 shadow-sm shadow-slate-900/5 sm:px-8"
    >
      <div
        class="flex cursor-pointer items-center gap-3 text-xl font-bold text-[#1e3a8a] transition-transform duration-200 hover:scale-105 sm:text-2xl"
        @click="$router.push('/')"
      >
        <img src="/logo-coinche7.svg" alt="Logo Coinche7" class="h-9 w-9 object-contain" />
        <span>Coinche7</span>
      </div>

      <AppNavigation :user="user" @logout="handleLogout" />
    </nav>

    <main class="flex-1">
      <router-view />
    </main>
  </div>
</template>
