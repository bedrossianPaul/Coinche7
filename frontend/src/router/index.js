import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../services/useAuth'
import Auth from '../views/Auth.vue'
import Chat from '../views/Chat.vue'
import Friends from '../views/Friends.vue'
import Profile from '../views/Profile.vue'
import Game from '../views/Game.vue'
import Home from '../views/Home.vue'
import WaitingRoom from '../views/WaitingRoom.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      requiresAuth: true,
      label: 'Accueil',
      icon: 'pi pi-home',
      order: 1
    }
  },
  {
    path: '/waiting-room',
    name: 'Salle d\'attente',
    component: WaitingRoom,
    meta: {
      requiresAuth: true,
      label: 'Salle d\'attente',
      icon: 'pi pi-play-circle',
      order: 1
    }
  },
  {
    path: '/friends',
    name: 'Amis',
    component: Friends,
    meta: {
      requiresAuth: true,
      label: 'Amis',
      icon: 'pi pi-users',
      order: 2
    }
  },
  {
    path: '/game',
    name: 'Game',
    component: Game,
    meta: { requiresAuth: true, hideInMenu: true }
  },
  {
    path: '/profile/:id',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true, hideInMenu: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: Chat,
    meta: {
      requiresAuth: true,
      label: 'Chat',
      icon: 'pi pi-comment',
      order: 3
    }
  },
  
  {
    path: '/auth',
    name: 'Auth',
    component: Auth,
    meta: {
      hideInMenu: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const middleware = async (to) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const auth = useAuth()

  if (requiresAuth) {
    const isConnected = await auth.check()

    if (!isConnected) {
      return '/auth'
    }
  }

  if (to.path === '/auth' && auth.hasToken()) {
    const isConnected = await auth.check()

    if (isConnected) {
      return '/'
    }
  }
}

router.beforeEach(middleware)

export default router
