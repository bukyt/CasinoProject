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
          {{ lastResult.outcome === 'WIN' ? 'WIN! +$' + lastResult.payout : 'LOSE' }}
        </div>
        <div v-else class="idle">Ready to Roll?</div>
      </div>

      <!-- Disable button if spinning OR if we don't have a valid session yet -->
      <button 
        @click="roll" 
        :disabled="spinning || !sessionId || sessionId === 'undefined'" 
        class="roll-btn" 
        :class="{ 'free': hasActiveBonus }"
      >
        {{ hasActiveBonus ? 'FREE SPIN!' : 'SPIN ($10)' }}
      </button>
    </div>

    <div class="footer-stats">
      <p>Credits: ${{ credits.toFixed(2) }}</p>
      <p>Session ID: {{ sessionId }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

/** 
 * FIX: Your router.js defines this as path: '/game/:sessionId'
 * Therefore, we must use route.params.sessionId.
 */
const sessionId = route.params.sessionId;
const playerId = route.params.playerId;

const spinning = ref(false);
const lastResult = ref(null);
const credits = ref(0);
const hasActiveBonus = ref(false);

const syncState = async () => {
  // Guard clause to prevent "undefined" API calls
  if (!sessionId || sessionId === 'undefined') {
    console.warn("Session ID is missing or undefined. Navigation might be broken.");
    return;
  }

  try {
    // Refresh credits from Bonus Service
    const cRes = await fetch(`/bonuses/players/${playerId}/credits`);
    if (cRes.ok) {
        const cData = await cRes.json();
        credits.value = cData.balance;
    }

    // Check for active bonuses in the player's list
    const bRes = await fetch(`/bonuses/players/${playerId}`);
    if (bRes.ok) {
        const bData = await bRes.json();
        // If any bonus is 'active', the next spin will be free
        hasActiveBonus.value = bData.some(b => b.status === 'active');
    }
  } catch (e) {
    console.error("Sync failed", e);
  }
};

const roll = async () => {
  spinning.value = true;
  lastResult.value = null;

  const betAmount = hasActiveBonus.value ? 0 : 10.0;

  try {
    const res = await fetch(`/games/sessions/${sessionId}/bets`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: betAmount })
    });
    
    const result = await res.json();

    // 1. Show the spinning animation for 1 second
    setTimeout(async () => {
      lastResult.value = result;
      spinning.value = false;
      
      // 2. Wait an extra 200ms to ensure the Bonus Service 
      //    has finished processing the Kafka event
      setTimeout(async () => {
        await syncState(); 
      }, 200);
      
    }, 1000);
  } catch (err) {
    console.error(err);
    spinning.value = false;
  }
};

const exit = async () => {
  if (sessionId && sessionId !== 'undefined') {
    try {
        await fetch(`/games/sessions/${sessionId}/close`, { method: 'PATCH' });
    } catch (e) {
        console.error("Failed to close session", e);
    }
  }
  router.push('/games');
};

onMounted(() => {
    console.log("GameView mounted with sessionId:", sessionId);
    syncState();
});
</script>

<style scoped>
.game-view { min-height: 80vh; display: flex; flex-direction: column; align-items: center; justify-content: center; color: white; }
.header { width: 100%; display: flex; justify-content: space-between; padding: 20px; position: absolute; top: 0; }
.machine { background: #1a1a1a; padding: 3rem; border-radius: 20px; border: 4px solid #333; text-align: center; }
.display { background: #000; height: 120px; width: 250px; display: flex; align-items: center; justify-content: center; font-size: 2rem; margin-bottom: 2rem; border-radius: 10px; border: 2px solid #444; }
.roll-btn { padding: 1rem 3rem; font-size: 1.2rem; border-radius: 50px; border: none; background: #e91e63; color: white; cursor: pointer; }
.roll-btn.free { background: #ff9800; animation: pulse 1s infinite; }
.roll-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.outcome.WIN { color: #4caf50; }
.outcome.LOSE { color: #f44336; }
.exit-btn { background: #333; border: none; color: white; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
.bonus-status { color: #ffeb3b; font-weight: bold; }

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}
</style>