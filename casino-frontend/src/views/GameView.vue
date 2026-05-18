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
// 1. FIX: Import getToken to securely authorize requests
import { getToken } from '../auth.js'

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

// 2. FIX: Dynamic helper function to generate authorization payload
const getHeaders = (extra = {}) => {
  const token = getToken()
  return {
    ...extra,
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  }
}

/**
 * SINGLE SOURCE OF TRUTH: GAME SESSION + WALLET STATE SYNC
 */
const fetchSession = async () => {
  try {
    // 3. FIX: Add authorization headers here
    const res = await fetch(`/games/sessions/${sessionId}`, {
      headers: getHeaders()
    })
    
    if (!res.ok) {
      if (res.status === 401) {
        console.error("Game session unauthorized. Bailing to login.");
        router.push('/login')
      }
      return
    }

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
    // 1. Fire the bet request to the backend
    const res = await fetch(`/games/sessions/${sessionId}/bets`, {
      method: 'POST',
      headers: getHeaders({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({ amount: betAmount })
    })

    if (!res.ok) throw new Error(`Bet failed: ${res.status}`)

    const result = await res.json()

    // 2. FIX: Fetch the fresh state from the server IMMEDIATELY.
    // This turns off 'hasActiveBonus' and updates balances instantly in the background data layer.
    const stateSyncPromise = syncState()

    // 3. Keep the visual spin delay separate for layout purposes
    setTimeout(async () => {
      // 4. FIX: Ensure the network sync has completed before we drop the spinning screen flags
      await stateSyncPromise 

      lastResult.value = result
      spinning.value = false
    }, 800)

  } catch (err) {
    console.error(err)
    spinning.value = false
  }
}

const exit = async () => {
  if (sessionId) {
    // 5. FIX: Secure the close session patch notification
    await fetch(`/games/sessions/${sessionId}/close`, {
      method: 'PATCH',
      headers: getHeaders()
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