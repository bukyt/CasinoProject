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

// FIX 1: ALWAYS derive playerId from backend-compatible session first
const playerId = ref(null)

const spinning = ref(false)
const lastResult = ref(null)
const credits = ref(0)
const hasActiveBonus = ref(false)

// FIX 2: safe number formatting
const formattedCredits = computed(() => {
  return Number(credits.value ?? 0).toFixed(2)
})

// FIX 3: load session properly to get real playerId
const fetchSession = async () => {
  try {
    const res = await fetch(`/games/sessions/${sessionId}`)
    if (!res.ok) return

    const data = await res.json()
    playerId.value = String(data.playerProfileId) // CRITICAL FIX
  } catch (e) {
    console.error("Failed to fetch session", e)
  }
}

const syncState = async () => {
  if (!playerId.value) return

  try {
    const cRes = await fetch(`/bonuses/players/${playerId.value}/credits`)
    if (cRes.ok) {
      const data = await cRes.json()
      credits.value = Number(data.balance ?? 0)
    }

    const bRes = await fetch(`/bonuses/players/${playerId.value}`)
    if (bRes.ok) {
      const data = await bRes.json()
      hasActiveBonus.value =
        Array.isArray(data) &&
        data.some(b => b.status === 'active')
    }
  } catch (e) {
    console.error("syncState failed", e)
  }
}

const roll = async () => {
  if (!sessionId) return

  spinning.value = true
  lastResult.value = null

  // FIX 4: backend ALWAYS gets real bet (never 0)
  const betAmount = 10.0

  try {
    const res = await fetch(`/games/sessions/${sessionId}/bets`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: betAmount })
    })

    const result = await res.json()

    setTimeout(() => {
      lastResult.value = result
      spinning.value = false
      syncState()
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
  await fetchSession()
  await syncState()
})
</script>

<style scoped>
.game-view {
  min-height: 80vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
}

.header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  padding: 20px;
  position: absolute;
  top: 0;
}

.machine {
  background: #1a1a1a;
  padding: 3rem;
  border-radius: 20px;
  border: 4px solid #333;
  text-align: center;
}

.display {
  background: #000;
  height: 120px;
  width: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  margin-bottom: 2rem;
  border-radius: 10px;
  border: 2px solid #444;
}

.roll-btn {
  padding: 1rem 3rem;
  font-size: 1.2rem;
  border-radius: 50px;
  border: none;
  background: #e91e63;
  color: white;
  cursor: pointer;
}

.roll-btn.free {
  background: #ff9800;
  animation: pulse 1s infinite;
}

.roll-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.outcome.WIN { color: #4caf50; }
.outcome.LOSE { color: #f44336; }

.exit-btn {
  background: #333;
  border: none;
  color: white;
  padding: 8px 16px;
  border-radius: 4px;
}

.bonus-status {
  color: #ffeb3b;
  font-weight: bold;
}

.footer-stats {
  margin-top: 20px;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}
</style>