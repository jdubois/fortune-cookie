<script setup>
import { ref, onMounted } from 'vue'

const message = ref('')
const url = ref('')
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
    url.value = data.url || ''
  } catch (e) {
    error.value = "Impossible de tirer une fortune. Veuillez réessayer."
    message.value = ''
    url.value = ''
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
        <div class="brand-row">
          <span class="brand-x">X</span>
          <span class="brand-text">Devoxx France 2026</span>
        </div>
        <div class="cookie-emoji">🥠</div>
        <h1 class="h3 mt-2 mb-0">Fortune Cookie</h1>
        <p class="mb-0 opacity-75">Une pépite à chaque clic &mdash; 22&ndash;24 avril, Palais des Congrès, Paris</p>
      </div>
      <div class="card-body cookie-body">
        <p class="fortune-message" :class="{ fade }">
          <span v-if="error" class="text-danger">{{ error }}</span>
          <span v-else-if="message">« {{ message }} »</span>
          <span v-else class="text-muted">Ouverture de votre fortune&hellip;</span>
        </p>
        <p v-if="url && !loading" class="fortune-link">
          <a :href="url" target="_blank" rel="noopener noreferrer">
            <i class="bi bi-box-arrow-up-right me-1"></i>
            Voir cette session
          </a>
        </p>
        <button
          class="btn cookie-btn mt-3"
          @click="fetchFortune"
          :disabled="loading"
        >
          <i class="bi bi-arrow-clockwise me-2"></i>
          <span v-if="loading">Ouverture&hellip;</span>
          <span v-else>Une autre fortune</span>
        </button>
      </div>
      <div class="cookie-footer">
        <a href="https://www.devoxx.fr/" target="_blank" rel="noopener noreferrer">devoxx.fr</a>
        <span class="sep">·</span>
        <a href="https://m.devoxx.com/events/devoxxfr2026/talks" target="_blank" rel="noopener noreferrer">Agenda complet</a>
      </div>
    </div>
  </div>
</template>
