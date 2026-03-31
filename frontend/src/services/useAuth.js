import axios from 'axios'
import { computed, ref } from 'vue'

const TOKEN_STORAGE_KEY = 'coinche7_token'
const USER_STORAGE_KEY = 'coinche7_user'

export const api = axios.create({
  baseURL: '/api'
})

const token = ref(localStorage.getItem(TOKEN_STORAGE_KEY) || '')
const user = ref(parseStoredUser())

let interceptorInstalled = false

function parseStoredUser() {
  const rawUser = localStorage.getItem(USER_STORAGE_KEY)

  if (!rawUser) {
    return null
  }

  try {
    return JSON.parse(rawUser)
  } catch {
    localStorage.removeItem(USER_STORAGE_KEY)
    return null
  }
}

function installAuthInterceptor() {
  if (interceptorInstalled) {
    return
  }

  api.interceptors.request.use((config) => {
    if (token.value) {
      config.headers.Authorization = token.value
    } else if (config.headers?.Authorization) {
      delete config.headers.Authorization
    }

    return config
  })

  interceptorInstalled = true
}

function setSession(sessionToken, sessionUser = null) {
  token.value = sessionToken
  localStorage.setItem(TOKEN_STORAGE_KEY, sessionToken)

  if (sessionUser) {
    user.value = sessionUser
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(sessionUser))
  }
}

function clearSession() {
  token.value = ''
  user.value = null
  localStorage.removeItem(TOKEN_STORAGE_KEY)
  localStorage.removeItem(USER_STORAGE_KEY)
}

export function useAuth() {
  installAuthInterceptor()

  const login = async (pseudo, password) => {
    const { data } = await api.post('/users/login', { pseudo, password })

    setSession(data.token, {
      id: data.id,
      pseudo: data.pseudo
    })

    return data
  }

  const register = async (pseudo, password) => {
    await api.post('/users/register', { pseudo, password })
    return login(pseudo, password)
  }

  const logout = () => {
    clearSession()
  }

  const hasToken = () => Boolean(token.value)

  const check = async () => {
    if (!token.value) {
      return false
    }

    try {
      const { data, status } = await api.get('/private/auth/check')

      if (status === 200 && data) {
        user.value = {
          id: data.id,
          pseudo: data.pseudo
        }
        localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user.value))
        return true
      }

      clearSession()
      return false
    } catch {
      clearSession()
      return false
    }
  }

  return {
    api,
    check,
    hasToken,
    isAuthenticated: computed(() => Boolean(token.value)),
    login,
    logout,
    register,
    setSession,
    token: computed(() => token.value),
    user: computed(() => user.value)
  }
}