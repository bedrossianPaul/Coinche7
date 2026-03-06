<script setup>
import { onMounted, ref } from 'vue'

const backendMessage = ref('Chargement...')

onMounted(async () => {
  try {
    const response = await fetch('/api/hello')
    const data = await response.json()
    backendMessage.value = data.message
  } catch (error) {
    backendMessage.value = 'Impossible de contacter le backend.'
  }
})
</script>

<template>
  <main class="container">
    <h1>Coinche7</h1>
    <p>Frontend Vue.js OK</p>
    <p>{{ backendMessage }}</p>
  </main>
</template>