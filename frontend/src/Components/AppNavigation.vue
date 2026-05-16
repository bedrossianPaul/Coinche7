<script>
import Button from 'primevue/button'
import Drawer from 'primevue/drawer'
import Badge from 'primevue/badge'
import { api } from '../services/useAuth'

export default {
  name: 'AppNavigation',
  components: {
    Button,
    Drawer,
    Badge
  },
  props: {
    user: {
      type: Object,
      default: null
    }
  },
  emits: ['logout'],
  data() {
    return {
      menuVisible: false,
      pendingFriendRequests: 0,
      unreadMessagesCount: 0
    }
  },
  computed: {
    navigationItems() {
      const items = [...this.$router.options.routes]
        .filter((item) => item.meta?.requiresAuth && !item.meta?.hideInMenu)
        .sort((first, second) => (first.meta?.order || 0) - (second.meta?.order || 0))
        .map((item) => ({
          path: item.path,
          label: item.meta?.label || item.name,
          icon: item.meta?.icon || 'pi pi-circle'
        }))

      // Add profile shortcut (current user)
      if (this.user && this.user.id) {
        items.unshift({ path: `/profile/${this.user.id}`, label: 'Profil', icon: 'pi pi-user' })
      }
      return items
    },
    currentPath() {
      return this.$route.path
    },
    friendsBadgeCount() {
      return this.pendingFriendRequests
    },
    chatBadgeCount() {
      return this.unreadMessagesCount
    }
  },
  mounted() {
    this.refreshNotifications()
    this.notificationTimer = setInterval(() => {
      this.refreshNotifications()
    }, 30000)
  },
  beforeUnmount() {
    if (this.notificationTimer) {
      clearInterval(this.notificationTimer)
      this.notificationTimer = null
    }
  },
  methods: {
    openMenu() {
      this.menuVisible = true
      this.refreshNotifications()
    },
    async navigateTo(path) {
      this.menuVisible = false

      if (this.currentPath !== path) {
        await this.$router.push(path)
      }
    },
    handleLogout() {
      this.menuVisible = false
      this.$emit('logout')
    },
    async refreshNotifications() {
      try {
        const [friendsResponse, messagesResponse] = await Promise.all([
          api.get('/friends'),
          api.get('/messages/unread-counts')
        ])

        this.pendingFriendRequests = (friendsResponse.data.incomingRequests || []).length
        this.unreadMessagesCount = messagesResponse.data.totalUnreadCount || 0
      } catch {
        this.pendingFriendRequests = 0
        this.unreadMessagesCount = 0
      }
    }
  }
}
</script>

<template>
  <div classs="shadow-3xl">
    <div class="flex items-center gap-2">
      <Button
        icon="pi pi-bars"
        text
        rounded
        aria-label="Ouvrir le menu"
        class="text-slate-700"
        @click="openMenu"
      />
    </div>

    <Drawer
      v-model:visible="menuVisible"
      position="right"
      class="w-full max-w-sm"
      :pt="{
        root: { class: 'border-l border-slate-200 bg-white text-slate-900' },
        header: { class: 'border-b border-slate-200 bg-white px-5 py-4' },
        content: { class: 'bg-white px-5 py-4' },
        mask: { class: 'bg-slate-950/20 backdrop-blur-sm' }
      }"
    >
      <template #header>
        <div class="flex items-center gap-3">
          <img src="/logo-coinche7.svg" alt="Logo Coinche7" class="h-10 w-10" />
          <div v-if="user" class="flex flex-col text-slate-800">
            <strong>{{ user.pseudo }}</strong>
            <span class="text-sm text-slate-500">Elo {{ user.elo ?? 'N/A' }}</span>
          </div>
        </div>
      </template>

      <div class="flex flex-col gap-3">
        <div v-for="item in navigationItems" :key="item.path" class="relative">
          <Button
            :label="item.label"
            :icon="item.icon"
            :class="[
              'w-full justify-start rounded-xl',
              currentPath === item.path
                ? 'bg-blue-50 font-bold text-[#1e3a8a]'
                : 'text-slate-700'
            ]"
            text
            @click="navigateTo(item.path)"
          />

          <Badge
            v-if="item.path === '/friends' && friendsBadgeCount > 0"
            :value="friendsBadgeCount"
            severity="danger"
            class="absolute right-3 top-1/2 -translate-y-1/2"
          />

          <Badge
            v-if="item.path === '/chat' && chatBadgeCount > 0"
            :value="chatBadgeCount"
            severity="danger"
            class="absolute right-3 top-1/2 -translate-y-1/2"
          />
        </div>

        <Button
          label="Déconnexion"
          icon="pi pi-sign-out"
          severity="danger"
          outlined
          class="mt-2 w-full justify-start rounded-xl"
          @click="handleLogout"
        />
      </div>
    </Drawer>
  </div>
</template>
