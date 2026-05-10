<template>
  <div class="home-container">
    <div class="card">
      <h1>Welcome{{ username ? `, ${username}` : '' }}</h1>
      
      <div v-if="loadError" class="error">
        <p><strong>⚠️ Sync Error:</strong> {{ loadError }}</p>
        <button @click="refreshData" class="retry-btn">Try Re-sync</button>
      </div>

      <template v-else-if="profile">
        <p class="lead">Your player profile is active and synced with game services.</p>

        <div class="bonus-summary">
          <div class="balance-info">
            <span class="label">Available Bonus Credits</span>
            <span class="amount">{{ credits }}</span>
          </div>
          <router-link to="/games" class="play-link">
            Go to Casino Lobby →
          </router-link>
        </div>

        <hr class="divider" />

        <dl class="grid">
          <dt>Database ID (Numeric)</dt>
          <dd>
            <strong :class="{ 'warning-text': !profile.id }">
              {{ profile.id || 'Pending Backend Update...' }}
            </strong>
          </dd>
          
          <dt>Internal Profile UUID</dt>
          <dd class="uuid-text">{{ profile.playerProfileId }}</dd>
          
          <dt>Account Auth UUID</dt>
          <dd class="uuid-text">{{ profile.accountId }}</dd>
          
          <dt>Full name</dt>
          <dd>{{ profile.fullName }}</dd>
          
          <dt>Date of birth</dt>
          <dd>{{ profile.dateOfBirth }}</dd>
          
          <dt>Status</dt>
          <dd><span class="status-badge" :class="profile.status">{{ profile.status }}</span></dd>
          
          <dt>Email</dt>
          <dd>{{ profile.email || '—' }}</dd>
          
          <dt>Phone</dt>
          <dd>{{ profile.phone || '—' }}</dd>
          
          <dt>Language / Currency</dt>
          <dd>{{ profile.language }} / {{ profile.currency }}</dd>
        </dl>
      </template>
      
      <div v-else class="loading-state">
        <div class="spinner"></div>
        <p>Syncing player identity across services...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { getAccountIdFromToken, getUsernameFromToken } from '../auth.js';
import { fetchProfileByAccountId } from '../services/profileApi.js';
import { fetchPlayerCredits } from '../services/bonusApi.js';

const username = ref(getUsernameFromToken() || '');
const profile = ref(null);
const credits = ref(0);
const loadError = ref('');

/**
 * Fetches profile and resolves Integer ID for financial services
 */
const refreshData = async () => {
  loadError.value = '';
  const aid = getAccountIdFromToken();
  
  if (!aid) {
    loadError.value = "Session expired. Please log in again.";
    return;
  }

  try {
    // 1. Fetch Profile using the Account UUID
    const profileData = await fetchProfileByAccountId(aid);
    console.log("Profile Data received:", profileData);
    
    if (!profileData) {
      throw new Error("No player profile associated with this account.");
    }
    
    profile.value = profileData;

    // 2. Extract the Numeric Internal ID
    // We check .id (the new Long field) first. 
    const internalNumericId = profileData.id; 

    if (internalNumericId !== undefined && internalNumericId !== null) {
      // 3. Fetch Credits using the Integer ID (Matches Ledger/Game Service expectations)
      credits.value = await fetchPlayerCredits(internalNumericId);
    } else {
      console.error("Numeric ID is still missing from JSON response.");
      loadError.value = "Profile synced, but numeric Internal ID is missing. Check Java entity and DB columns.";
    }

  } catch (e) {
    console.error("HomeView Sync Error:", e);
    loadError.value = e.message || "Failed to communicate with profile service.";
  }
};

onMounted(refreshData);
</script>

<style scoped>
.home-container { max-width: 800px; margin: 0 auto; padding: 20px; }
.card { background: #1e1e1e; border: 1px solid #333; border-radius: 12px; padding: 1.75rem; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3); color: #eee; }

h1 { margin: 0 0 0.5rem; font-size: 1.6rem; color: #fff; }
.lead { color: #888; margin-bottom: 1.5rem; font-size: 0.95rem; }

.bonus-summary {
  background: rgba(76, 175, 80, 0.1);
  border: 1px solid #4caf50;
  border-radius: 10px;
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.amount { font-size: 2rem; font-weight: bold; color: #4caf50; }
.label { font-size: 0.75rem; text-transform: uppercase; color: #aaa; letter-spacing: 1px; }

.play-link {
  background: #4caf50;
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: bold;
  transition: transform 0.2s;
}
.play-link:hover { transform: scale(1.05); }

.grid { display: grid; grid-template-columns: 180px 1fr; gap: 0.8rem 1rem; margin: 1rem 0; font-size: 0.9rem; }
dt { color: #777; }
dd { margin: 0; color: #ddd; word-break: break-all; }

.uuid-text { font-family: 'Courier New', Courier, monospace; font-size: 0.8rem; color: #999; }
.warning-text { color: #ff9800; }

.status-badge { 
  background: #2e7d32; 
  color: #fff; 
  padding: 2px 10px; 
  border-radius: 20px; 
  font-size: 0.7rem; 
  font-weight: bold;
}

.error { 
  color: #ff5252; 
  background: rgba(255, 82, 82, 0.1); 
  padding: 1.5rem; 
  border-radius: 8px; 
  border: 1px solid #ff5252;
  margin-bottom: 1rem;
}

.retry-btn {
  margin-top: 10px;
  padding: 5px 15px;
  background: #ff5252;
  border: none;
  color: white;
  border-radius: 4px;
  cursor: pointer;
}

.divider { border: 0; border-top: 1px solid #333; margin: 1.5rem 0; }
.loading-state { text-align: center; padding: 3rem; color: #666; }
</style>