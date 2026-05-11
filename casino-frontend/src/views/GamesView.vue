<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
// Import your auth helpers
import { getAccountIdFromToken } from '../auth.js';
import { fetchProfileByAccountId } from '../services/profileApi.js';

const router = useRouter();
const games = ref([]);
const bonusBalance = ref(0);
const playerId = ref(null); // Now reactive and starts null

const fetchData = async () => {
  try {
    // 1. Get the Account ID from the JWT token
    const accountId = getAccountIdFromToken();
    if (!accountId) {
      console.error("No account found in token");
      router.push('/login');
      return;
    }

    // 2. Fetch the actual Profile to get the internal ID (player ID)
    const profile = await fetchProfileByAccountId(accountId);
    if (!profile || profile.playerProfileId == null) {
      console.error("Profile not found or playerProfileId missing for account");
      return;
    }

    playerId.value = profile.playerProfileId;

    // 3. Fetch Games list (Proxy '/games' -> :8082)
    const gRes = await fetch('/games');
    games.value = await gRes.json();

    // 4. Fetch Credits using the real playerId (Proxy '/bonuses' -> :8084)
    const cRes = await fetch(`/bonuses/players/${playerId.value}/credits`);
    const cData = await cRes.json();
    bonusBalance.value = cData.balance;
  } catch (err) {
    console.error("Lobby load failed", err);
  }
};

const createSession = async (gameId) => {
  if (!playerId.value) {
    alert("User profile not loaded yet.");
    return;
  }

  try {
    const res = await fetch('/games/sessions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        gameId: gameId,
        initialBalance: 100.0,
        playerProfileId: playerId.value // Dynamic ID
      })
    });

    const session = await res.json();
    console.log("Session Response:", session);

    // Look for ID in various possible fields
    const sid = session.id || session.sessionId || (typeof session === 'string' ? session : null);

    if (sid) {
      router.push(`/game/${sid}/${playerId.value}`);
    } else {
      console.error("Could not find ID in session response", session);
      alert("Error: Backend did not return a valid Session ID");
    }
  } catch (err) {
    console.error("Session creation failed", err);
    alert("Session creation failed. Check console.");
  }
};

const addDebugCredits = async () => {
  if (!playerId.value) return;
  
  await fetch(`/bonuses/players/${playerId.value}/debug-add`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ amount: 50.0 })
  });
  fetchData(); // Refresh balance
};

onMounted(fetchData);
</script>

<template>
  <div class="lobby">
    <header class="lobby-header">
      <h1>Casino Floor</h1>
      <div v-if="playerId" class="user-info">
        <div class="balance-card">
          <span class="label">Bonus Credits:</span>
          <span class="amount">${{ bonusBalance.toFixed(2) }}</span>
        </div>
        <button @click="addDebugCredits" class="debug-btn">+ Add $50</button>
      </div>
      <div v-else>Loading profile...</div>
    </header>

    <div class="game-grid">
      <div v-for="game in games" :key="game.id" class="game-card">
        <div class="icon">🎰</div>
        <h3>{{ game.name }}</h3>
        <p>{{ game.description }}</p>
        <button @click="createSession(game.id)" class="play-btn">Play Now</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lobby { max-width: 1000px; margin: 0 auto; padding: 2rem; color: white; }
.lobby-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; min-height: 80px; }
.balance-card { background: #1a1a1a; padding: 1rem; border-radius: 8px; border: 1px solid #4caf50; display: inline-block; }
.amount { font-weight: bold; color: #4caf50; margin-left: 10px; }
.game-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 1.5rem; }
.game-card { background: #242424; padding: 1.5rem; border-radius: 12px; border: 1px solid #333; text-align: center; transition: transform 0.2s; }
.game-card:hover { transform: translateY(-5px); border-color: #4caf50; }
.play-btn { width: 100%; margin-top: 1rem; padding: 0.8rem; background: #4caf50; border: none; border-radius: 6px; color: white; font-weight: bold; cursor: pointer; }
.debug-btn { background: none; color: #888; border: 1px solid #444; padding: 5px 10px; border-radius: 4px; cursor: pointer; margin-left: 10px; }
</style>