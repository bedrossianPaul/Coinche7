<script>
import Button from 'primevue/button'
import { api, useAuth } from '../services/useAuth'

export default {
  name: 'Chat',
  components: {
    Button
  },
  data() {
    return {
      auth: useAuth(),
      friends: [],
      unreadByFriend: {},
      totalUnreadCount: 0,
      messages: [],
      newMessage: '',
      selectedFriendId: null,
      friendsPanelOpen: false,
      lastTimestamp: 0,
      pollingInterval: null,
      loading: false,
      loadingFriends: false
    }
  },
  computed: {
    user() {
      return this.auth.user || null
    },
    selectedFriend() {
      return this.friends.find(f => f.id === this.selectedFriendId)
    },
    friendsWithUnread() {
      return this.friends.map((friend) => ({
        ...friend,
        unreadCount: this.unreadByFriend[friend.id] || 0
      }))
    }
  },
  methods: {
    async loadFriends() {
      try {
        this.loadingFriends = true
        const response = await api.get('/friends')
        this.friends = response.data.friends || []
        await this.loadUnreadCounts()
      } catch (error) {
        console.error('Erreur lors du chargement des amis:', error)
      } finally {
        this.loadingFriends = false
      }
    },
    async selectFriend(friendId) {
      this.selectedFriendId = friendId
      this.messages = []
      this.lastTimestamp = 0
      this.friendsPanelOpen = false
      await this.loadMessages()
    },
    toggleFriendsPanel() {
      this.friendsPanelOpen = !this.friendsPanelOpen
    },
    async loadMessages() {
      if (!this.selectedFriendId) return
      
      try {
        const response = await api.get(`/messages/conversation/${this.selectedFriendId}`)
        this.messages = response.data
        if (this.messages.length > 0) {
          this.lastTimestamp = this.messages[this.messages.length - 1].createdAt
        }
        await this.loadUnreadCounts()
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      } catch (error) {
        console.error('Erreur lors du chargement des messages:', error)
      }
    },
    async loadNewMessages() {
      if (!this.selectedFriendId || this.lastTimestamp === 0) return

      try {
        const response = await api.get(`/messages/conversation/${this.selectedFriendId}/since?timestamp=${this.lastTimestamp}`)
        if (response.data.length > 0) {
          this.messages.push(...response.data)
          this.lastTimestamp = response.data[response.data.length - 1].createdAt
          await this.loadUnreadCounts()
          this.$nextTick(() => {
            this.scrollToBottom()
          })
        }
      } catch (error) {
        console.error('Erreur lors du chargement des nouveaux messages:', error)
      }
    },
    async loadUnreadCounts() {
      try {
        const response = await api.get('/messages/unread-counts')
        const counts = response.data.counts || []

        const unreadMap = {}
        counts.forEach((entry) => {
          unreadMap[entry.friendId] = entry.unreadCount
        })

        this.unreadByFriend = unreadMap
        this.totalUnreadCount = response.data.totalUnreadCount || 0
      } catch (error) {
        console.error('Erreur lors du chargement des messages non lus:', error)
        this.unreadByFriend = {}
        this.totalUnreadCount = 0
      }
    },
    async sendMessage() {
      if (!this.newMessage.trim() || !this.selectedFriendId) return

      try {
        this.loading = true
        const response = await api.post(`/messages/conversation/${this.selectedFriendId}`, {
          content: this.newMessage
        })
        
        this.newMessage = ''
        
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      } catch (error) {
        console.error('Erreur lors de l\'envoi du message:', error)
      } finally {
        this.loading = false
      }
    },
    scrollToBottom() {
      const messagesContainer = this.$refs.messagesContainer
      if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight
      }
    },
    formatDate(timestamp) {
      const date = new Date(timestamp)
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${hours}:${minutes}`
    },
    isCurrentUser(sender) {
      return sender.pseudo === this.user?.pseudo
    },
    getInitials(pseudo) {
      const value = String(pseudo || '').trim()
      return value ? value.slice(0, 2).toUpperCase() : '?'
    }
  },
  mounted() {
    this.loadFriends()
    
    // Polling toutes les secondes
    this.pollingInterval = setInterval(() => {
      this.loadUnreadCounts()
      this.loadNewMessages()
    }, 1000)
  },
  beforeUnmount() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval)
    }
  }
}
</script>

<template>
  <div class="flex h-[calc(100vh-80px)] flex-col bg-slate-50 lg:flex-row">
    <!-- Friends List -->
    <div class="flex w-full flex-col border-b border-slate-200 bg-white shadow-sm lg:w-80 lg:border-b-0 lg:border-r lg:shadow-none">
      <!-- Header -->
      <div class="border-b border-slate-200 px-4 py-3 sm:px-5 sm:py-4">
        <div class="flex items-center justify-between gap-3">
          <div class="min-w-0">
            <h2 class="truncate text-base font-semibold text-slate-900 sm:text-lg">Messages</h2>
            <p class="text-xs text-slate-500">{{ friends.length }} ami{{ friends.length > 1 ? 's' : '' }}</p>
          </div>

          <button
            type="button"
            @click="toggleFriendsPanel"
            class="relative inline-flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-100"
          >
            <i :class="friendsPanelOpen ? 'pi pi-chevron-down' : 'pi pi-users'" class="text-xs"></i>
            <span class="hidden sm:inline">{{ friendsPanelOpen ? 'Réduire' : 'Choisir conv' }}</span>
            <span v-if="!friendsPanelOpen && totalUnreadCount > 0" class="ml-1 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-rose-500 px-1.5 text-[11px] font-bold leading-none text-white">
              {{ totalUnreadCount }}
            </span>
          </button>
        </div>
      </div>

      <!-- Friends Scroll -->
      <div v-if="friendsPanelOpen" class="max-h-[38vh] flex-1 overflow-y-auto lg:max-h-none">
        <div v-if="friends.length === 0" class="flex h-28 items-center justify-center px-4 text-center sm:h-32">
          <p class="text-sm text-slate-500">Tu dois avoir des amis pour discuter</p>
        </div>

        <button
          v-for="friend in friendsWithUnread"
          :key="friend.id"
          @click="selectFriend(friend.id)"
          :class="{
            'flex w-full items-center gap-3 border-l-4 border-blue-500 bg-blue-50 px-3 py-3 sm:px-4': selectedFriendId === friend.id,
            'flex w-full items-center gap-3 border-l-4 border-transparent px-3 py-3 hover:bg-slate-50 sm:px-4': selectedFriendId !== friend.id
          }"
        >
          <div class="flex h-9 w-9 shrink-0 items-center justify-center overflow-hidden rounded-full bg-cyan-100 text-xs font-black text-cyan-900 sm:h-10 sm:w-10">
            <img
              v-if="friend.avatarUrl"
              :src="friend.avatarUrl"
              :alt="friend.pseudo"
              class="h-full w-full object-cover"
            />
            <span v-else>{{ getInitials(friend.pseudo) }}</span>
          </div>
          <div class="min-w-0 flex-1 text-left">
            <p class="truncate text-sm font-medium text-slate-900 sm:text-base">{{ friend.pseudo }}</p>
            <p class="text-xs text-slate-500">ID #{{ friend.id }}</p>
          </div>

          <span
            v-if="friend.unreadCount > 0"
            class="inline-flex h-6 min-w-6 items-center justify-center rounded-full bg-rose-500 px-2 text-xs font-bold text-white"
          >
            {{ friend.unreadCount }}
          </span>
        </button>
      </div>

      
    </div>

    <!-- Chat Area -->
    <div v-if="selectedFriendId" class="flex min-h-0 flex-1 flex-col">
      <!-- Header -->
      <div class="border-b border-slate-200 bg-white px-4 py-3 shadow-sm sm:px-6 sm:py-4">
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-full bg-cyan-100 text-xs font-black text-cyan-900 sm:h-11 sm:w-11">
            <img
              v-if="selectedFriend?.avatarUrl"
              :src="selectedFriend.avatarUrl"
              :alt="selectedFriend?.pseudo"
              class="h-full w-full object-cover"
            />
            <span v-else>{{ getInitials(selectedFriend?.pseudo) }}</span>
          </div>
          <div>
            <h3 class="text-sm font-semibold text-slate-900 sm:text-base">{{ selectedFriend?.pseudo }}</h3>

          </div>
        </div>
      </div>

      <!-- Messages Container -->
      <div ref="messagesContainer" class="flex-1 overflow-y-auto px-4 py-4 sm:px-6 sm:py-6">
        <div v-if="messages.length === 0" class="flex h-full items-center justify-center">
          <p class="px-4 text-center text-slate-500">Aucun message pour l'instant. Dis hello! 👋</p>
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="message in messages"
            :key="message.id"
            :class="{
              'flex justify-end': isCurrentUser(message.sender),
              'flex justify-start': !isCurrentUser(message.sender)
            }"
          >
            <div
              :class="{
                'max-w-[85%] rounded-2xl rounded-br-none bg-blue-500 px-4 py-3 text-white sm:max-w-xs': isCurrentUser(message.sender),
                'max-w-[85%] rounded-2xl rounded-bl-none bg-slate-200 px-4 py-3 text-slate-100 sm:max-w-xs': !isCurrentUser(message.sender)
              }"
            >
              <p
                :class="[
                  'break-all text-sm leading-relaxed',
                  isCurrentUser(message.sender) ? 'text-white' : 'text-slate-700'
                ]"
              >
                {{ message.content }}
              </p>
              <p
                :class="{
                  'mt-2 text-xs opacity-75': true,
                  'text-white': isCurrentUser(message.sender),
                  'text-slate-600': !isCurrentUser(message.sender)
                }"
              >
                {{ formatDate(message.createdAt) }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Container -->
      <div class="border-t border-slate-200 bg-white px-4 py-4 shadow-lg sm:px-6">
        <div class="flex gap-2 sm:gap-3">
          <textarea
            v-model="newMessage"
            placeholder="Écris un message..."
            class="flex-1 resize-none rounded-2xl border border-slate-200 p-3 text-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
            rows="1"
            @keydown.enter.ctrl="sendMessage"
            :disabled="loading"
            style="max-height: 120px"
          />
          <Button
            icon="pi pi-send"
            @click="sendMessage"
            :loading="loading"
            class="rounded-2xl"
            aria-label="Envoyer"
          />
        </div>
        <p class="mt-2 text-xs text-slate-500">Appuie sur Ctrl+Entrée pour envoyer</p>
      </div>
    </div>

    <!-- No Selection -->
    <div v-else class="hidden flex-1 items-center justify-center lg:flex">
      <div class="text-center">
        <i class="pi pi-comment text-6xl text-slate-300"></i>
        <p class="mt-4 text-lg font-medium text-slate-600">Sélectionne un ami pour discuter</p>
      </div>
    </div>
  </div>
</template>
