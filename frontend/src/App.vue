<script setup>
import { ref, onMounted } from 'vue'

const message = ref('')
const loading = ref(false)
const fade = ref(false)
const error = ref('')

async function fetchFortune() {
  error.value = ''
  loading.value = true
  fade.value = true
  try {
    await new Promise((r) => setTimeout(r, 200))
    const res = await fetch('/api/fortunes/random')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    message.value = data.message
  } catch (e) {
    error.value = 'Could not fetch a fortune. Please try again.'
    message.value = ''
  } finally {
    loading.value = false
    fade.value = false
  }
}

onMounted(fetchFortune)
</script>

<template>
  <div class="app-shell">
    <div class="card cookie-card">
      <div class="cookie-header">
        <div class="cookie-emoji">🥠</div>
        <h1 class="h3 mt-2 mb-0">Fortune Cookie</h1>
        <p class="mb-0 opacity-75">A little wisdom, just for you</p>
      </div>
      <div class="card-body cookie-body">
        <p class="fortune-message" :class="{ fade }">
          <span v-if="error" class="text-danger">{{ error }}</span>
          <span v-else-if="message">“{{ message }}”</span>
          <span v-else class="text-muted">Cracking your cookie…</span>
        </p>
        <button
          class="btn cookie-btn mt-3"
          @click="fetchFortune"
          :disabled="loading"
        >
          <i class="bi bi-arrow-clockwise me-2"></i>
          <span v-if="loading">Cracking…</span>
          <span v-else>Crack another cookie</span>
        </button>
      </div>
    </div>
  </div>
</template>
