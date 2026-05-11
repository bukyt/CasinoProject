<template>
  <div class="game-view">
    <div class="header">
      <button @click="exit" class="exit-btn">← Back to Lobby</button>

      <div class="bonus-status" v-if="hasActiveBonus">
        🎁 FREE SPIN READY
      </div>
    </div>

    <div class="machine">
      <div class="display">
        <div v-if="spinning" class="spinning">🎰</div>

        <div v-else-if="lastResult" :class="['outcome', lastResult.outcome]">
          {{ lastResult.outcome === 'WIN'
            ? 'WIN! +' + lastResult.payout
            : 'LOSE' }}
        </div>

        <div v-else class="idle">Ready to Roll?</div>
      </div>

      <button
        @click="roll"
        :disabled="spinning || !sessionId"
        class="roll-btn"
        :class="{ free: hasActiveBonus }"
      >
        {{ hasActiveBonus ? 'FREE SPIN!' : 'SPIN ($10)' }}
      </button>
    </div>

    <div class="footer-stats">
      <p>Credits: ${{ formattedCredits }}</p>
      <p>Session ID: {{ sessionId }}</p>
      <p v-if="playerId">Player ID: {{ playerId }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const sessionId = route.params.sessionId || null

const playerId = ref(null)
const spinning = ref(false)
const lastResult = ref(null)
const credits = ref(0)
const hasActiveBonus = ref(false)

const formattedCredits = computed(() => {
  return Number(credits.value ?? 0).toFixed(2)
})

/**
 * SINGLE SOURCE OF TRUTH: GAME SESSION
 */
const fetchSession = async () => {
  try {
    const res = await fetch(`/games/sessions/${sessionId}`)
    if (!res.ok) return

    const data = await res.json()

    playerId.value = String(data.playerProfileId)

    // IMPORTANT: backend must provide these fields
    credits.value = Number(data.balance ?? 0)
    hasActiveBonus.value = Boolean(data.hasActiveBonus ?? false)

  } catch (e) {
    console.error("Failed to fetch session", e)
  }
}

const syncState = async () => {
  await fetchSession()
}

const roll = async () => {
  if (!sessionId) return

  spinning.value = true
  lastResult.value = null

  const betAmount = 10.0

  try {
    const res = await fetch(`/games/sessions/${sessionId}/bets`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: betAmount })
    })

    const result = await res.json()

    setTimeout(async () => {
      lastResult.value = result
      spinning.value = false

      await syncState()
    }, 800)

  } catch (err) {
    console.error(err)
    spinning.value = false
  }
}

const exit = async () => {
  if (sessionId) {
    await fetch(`/games/sessions/${sessionId}/close`, {
      method: 'PATCH'
    })
  }
  router.push('/games')
}

onMounted(async () => {
  await syncState()
})
</script>