<template>
  <div class="game-view">
    <div class="header">
      <button @click="exit" class="exit-btn">← Back to Lobby</button>

      <div class="wallet-group">
        <span class="wallet-display">
          💳 Real Wallet: ${{ formattedWalletBalance }}
        </span>
        <!-- DEBUG BUTTON -->
        <button 
          @click="addDebugFunds" 
          :disabled="!playerId" 
          class="debug-btn"
        >
          ⚡ Debug: Add $100
        </button>
      </div>

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
      <p>Session ID: {{ sessionId }}</p>
      <p v-if="playerId">Player ID: {{ playerId }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchWallet, depositFunds } from '../services/wallet.js' 

const route = useRoute()
const router = useRouter()

const sessionId = route.params.sessionId || null

const playerId = ref(null)
const spinning = ref(false)
const lastResult = ref(null)
const walletBalance = ref(0) 
const hasActiveBonus = ref(false)

const formattedWalletBalance = computed(() => {
  return Number(walletBalance.value ?? 0).toFixed(2)
})

/**
 * SINGLE SOURCE OF TRUTH: GAME SESSION + WALLET STATE SYNC
 */
const fetchSession = async () => {
  try {
    const res = await fetch(`/games/sessions/${sessionId}`)
    if (!res.ok) return

    const data = await res.json()

    playerId.value = String(data.playerProfileId)
    hasActiveBonus.value = Boolean(data.hasActiveBonus ?? false)

    if (data.playerProfileId) {
      await syncWalletState(data.playerProfileId)
    }

  } catch (e) {
    console.error("Failed to fetch session", e)
  }
}

const syncWalletState = async (id) => {
  try {
    const walletData = await fetchWallet(id)
    // FIX: Changed .balance to .availableBalance to match Spring's serialized Record
    walletBalance.value = Number(walletData.availableBalance ?? 0)
  } catch (err) {
    console.error("Could not synchronize remote wallet data assets", err)
  }
}

const syncState = async () => {
  await fetchSession()
}

/**
 * DEBUG FEATURE: Call wallet microservice debit endpoint directly 
 */
const addDebugFunds = async () => {
  if (!playerId.value) return
  
  try {
    const updatedWallet = await depositFunds(playerId.value, 100.00)
    // FIX: Changed .balance to .availableBalance here as well
    walletBalance.value = Number(updatedWallet.availableBalance ?? 0)
    console.log("Debug funds added successfully!", updatedWallet)
  } catch (err) {
    console.error("Failed to add debug funds", err)
  }
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

<style scoped>
.wallet-group {
  display: flex;
  align-items: center;
  gap: 12px;
}
.debug-btn {
  background-color: #e67e22;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
.debug-btn:disabled {
  background-color: #7f8c8d;
  cursor: not-allowed;
}
.debug-btn:hover:not(:disabled) {
  background-color: #d35400;
}
</style>