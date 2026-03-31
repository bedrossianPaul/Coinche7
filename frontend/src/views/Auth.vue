<script>
import Button from 'primevue/button'
import Card from 'primevue/card'
import Divider from 'primevue/divider'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Password from 'primevue/password'
import { useAuth } from '../services/useAuth'

export default {
  name: 'Auth',
  components: {
    Button,
    Card,
    Divider,
    InputText,
    Message,
    Password
  },
  data() {
    return {
      auth: useAuth(),
      mode: 'login',
      loading: false,
      successMessage: '',
      errorMessage: '',
      loginForm: {
        pseudo: '',
        password: ''
      },
      registerForm: {
        pseudo: '',
        password: '',
        confirmPassword: ''
      }
    }
  },
  computed: {
    currentPseudo: {
      get() {
        return this.mode === 'login' ? this.loginForm.pseudo : this.registerForm.pseudo
      },
      set(value) {
        if (this.mode === 'login') {
          this.loginForm.pseudo = value
          return
        }

        this.registerForm.pseudo = value
      }
    },
    currentPassword: {
      get() {
        return this.mode === 'login' ? this.loginForm.password : this.registerForm.password
      },
      set(value) {
        if (this.mode === 'login') {
          this.loginForm.password = value
          return
        }

        this.registerForm.password = value
      }
    },
    submitLabel() {
      if (this.loading) {
        return this.mode === 'login' ? 'Connexion...' : 'Inscription...'
      }

      return this.mode === 'login' ? 'Se connecter' : "S'inscrire"
    }
  },
  methods: {
    resetMessages() {
      this.successMessage = ''
      this.errorMessage = ''
    },

    async redirectToHome() {
      const isValid = await this.auth.check()

      if (isValid) {
        await this.$router.replace('/')
      }
    },
    async submit() {
      this.resetMessages()
      this.loading = true

      try {
        if (this.mode === 'login') {
          await this.auth.login(this.loginForm.pseudo.trim(), this.loginForm.password)
          this.successMessage = 'Connexion réussie. Redirection en cours...'
        } else {
          if (!this.registerForm.pseudo.trim()) {
            throw new Error('Le pseudo est obligatoire.')
          }

          if (this.registerForm.password !== this.registerForm.confirmPassword) {
            throw new Error('Les mots de passe ne correspondent pas.')
          }

          await this.auth.register(this.registerForm.pseudo.trim(), this.registerForm.password)
          this.successMessage = 'Compte créé avec succès. Redirection en cours...'
        }

        await this.redirectToHome()
      } catch (error) {
        this.errorMessage =
          this.mode === 'login'
            ? 'Echec de la connexion.'
            : "Echec de la création du compte."
        
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<template>
  <div class="grid min-h-screen place-items-center bg-linear-to-b from-slate-50 to-slate-100 px-6 py-8">
    <Card
      class="w-full max-w-[460px] rounded-3xl border border-slate-200 bg-white shadow-xl shadow-slate-900/5"
      :pt="{
        body: { class: 'flex flex-col gap-5 p-6 sm:p-8' },
        title: { class: 'm-0' },
        content: { class: 'p-0' }
      }"
    >
      <template #title>
        <div class="flex items-center justify-center gap-4 text-center">
          <img src="/logo-coinche7.svg" alt="Logo Coinche7" class="h-[72px] w-[72px] object-contain" />
          <div class="flex flex-col items-start text-left">
            <span class="text-3xl font-extrabold text-[#1e3a8a]">Coinche7</span>
            <span class="text-sm text-slate-600">Connecte-toi ou crée ton compte.</span>
          </div>
        </div>
      </template>

      <template #content>
        <div class="grid grid-cols-2 gap-3">
          <Button
            label="Connexion"
            :severity="mode === 'login' ? 'primary' : 'secondary'"
            :outlined="mode !== 'login'"
            class="w-full"
            @click="mode = 'login'; resetMessages()"
          />
          <Button
            label="Inscription"
            :severity="mode === 'register' ? 'primary' : 'secondary'"
            :outlined="mode !== 'register'"
            class="w-full"
            @click="mode = 'register'; resetMessages()"
          />
        </div>

        <Divider />

        <form class="flex flex-col gap-4" @submit.prevent="submit">
          <Message v-if="errorMessage" severity="error">{{ errorMessage }}</Message>
          <Message v-if="successMessage" severity="success">{{ successMessage }}</Message>

          <div class="flex flex-col gap-2">
            <label for="pseudo" class="font-semibold text-slate-800">Pseudo</label>
            <InputText
              id="pseudo"
              v-model="currentPseudo"
              placeholder="Entre ton pseudo"
              autocomplete="username"
              fluid
              class="w-full"
            />
          </div>

          <div class="flex flex-col gap-2">
            <label for="password" class="font-semibold text-slate-800">Mot de passe</label>
            <Password
              id="password"
              v-model="currentPassword"
              placeholder="Entre ton mot de passe"
              :feedback="mode === 'register'"
              toggle-mask
              :inputProps="{ autocomplete: mode === 'login' ? 'current-password' : 'new-password' }"
              fluid
              class="w-full"
            />
          </div>

          <div v-if="mode === 'register'" class="flex flex-col gap-2">
            <label for="confirmPassword" class="font-semibold text-slate-800">Confirmer le mot de passe</label>
            <Password
              id="confirmPassword"
              v-model="registerForm.confirmPassword"
              placeholder="Confirme ton mot de passe"
              :feedback="false"
              toggle-mask
              :inputProps="{ autocomplete: 'new-password' }"
              fluid
              class="w-full"
            />
          </div>

          <Button
            type="submit"
            :label="submitLabel"
            :loading="loading"
            class="mt-2 w-full"
          />
        </form>
      </template>
    </Card>
  </div>
</template>