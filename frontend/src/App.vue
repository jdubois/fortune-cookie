<script setup>
import { ref, onMounted } from 'vue'

const roast = ref('')
const title = ref('')
const url = ref('')
const lang = ref('fr')
const total = ref(0)
const loading = ref(false)
const fade = ref(false)
const error = ref('')

async function fetchRoast() {
  error.value = ''
  loading.value = true
  fade.value = true
  try {
    await new Promise((r) => setTimeout(r, 200))
    const res = await fetch('/api/roasts/random')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    roast.value = data.roast
    title.value = data.title
    url.value = data.url
    lang.value = data.lang || 'fr'
  } catch (e) {
    error.value = "Impossible de charger un roast. Réessayez."
    roast.value = ''
    title.value = ''
    url.value = ''
  } finally {
    loading.value = false
    fade.value = false
  }
}

async function fetchCount() {
  try {
    const res = await fetch('/api/roasts/count')
    if (!res.ok) return
    const data = await res.json()
    total.value = data.total
  } catch (e) { /* ignore */ }
}

onMounted(() => {
  fetchCount()
  fetchRoast()
})
</script>

<template>
  <div class="app-shell">
    <div class="card roaster-card">
      <div class="roaster-header">
        <div class="brand-row">
          <span class="brand-x">X</span>
          <span class="brand-text">Devoxx France 2026</span>
        </div>
        <h1 class="roaster-title">🔥 Devoxx Roaster</h1>
        <p class="roaster-subtitle">
          On grille (gentiment) les talks<span v-if="total"> &mdash; {{ total }} au menu</span>
        </p>
      </div>

      <div class="card-body roaster-body">
        <blockquote class="roast" :class="{ fade, en: lang === 'en' }">
          <span v-if="error" class="roast-error">{{ error }}</span>
          <template v-else-if="roast">
            <span class="roast-mark" aria-hidden="true">“</span>
            <span class="roast-text">{{ roast }}</span>
            <span class="roast-mark closing" aria-hidden="true">”</span>
          </template>
          <span v-else class="roast-loading">Préparation du feu&hellip;</span>
        </blockquote>

        <div v-if="title && !error" class="reference" :class="{ fade }">
          <div class="reference-label">
            <i class="bi bi-mic me-1"></i>
            {{ lang === 'en' ? 'About this talk' : 'À propos de ce talk' }}
          </div>
          <a
            v-if="url"
            class="reference-link"
            :href="url"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ title }}
            <i class="bi bi-box-arrow-up-right ms-2"></i>
          </a>
          <span v-else class="reference-link no-link">{{ title }}</span>
        </div>

        <button
          class="btn roast-btn mt-4"
          @click="fetchRoast"
          :disabled="loading"
        >
          <i class="bi bi-fire me-2"></i>
          <span v-if="loading">Attisons les braises&hellip;</span>
          <span v-else>Griller un autre talk</span>
        </button>
      </div>

      <div class="roaster-footer">
        <span>🔥 Généré par GitHub Copilot CLI avec Claude Opus 4.7.</span>
        <span class="sep">·</span>
        <a href="https://www.devoxx.fr/" target="_blank" rel="noopener noreferrer">devoxx.fr</a>
        <span class="sep">·</span>
        <a href="https://m.devoxx.com/events/devoxxfr2026/talks" target="_blank" rel="noopener noreferrer">Agenda complet</a>
      </div>
    </div>
  </div>
</template>
