<template>
  <div class="card">
    <h1>Welcome{{ username ? `, ${username}` : '' }}</h1>
    <p v-if="loadError" class="error">{{ loadError }}</p>
    <template v-else-if="profile">
      <p class="lead">Your player profile is active.</p>
      <dl class="grid">
        <dt>Profile id</dt>
        <dd>{{ profile.playerProfileId }}</dd>
        <dt>Account id</dt>
        <dd>{{ profile.accountId }}</dd>
        <dt>Full name</dt>
        <dd>{{ profile.fullName }}</dd>
        <dt>Date of birth</dt>
        <dd>{{ profile.dateOfBirth }}</dd>
        <dt>Status</dt>
        <dd>{{ profile.status }}</dd>
        <dt>Email</dt>
        <dd>{{ profile.email || '—' }}</dd>
        <dt>Phone</dt>
        <dd>{{ profile.phone || '—' }}</dd>
        <dt>Language / currency</dt>
        <dd>{{ profile.language }} / {{ profile.currency }}</dd>
      </dl>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { getAccountIdFromToken, getUsernameFromToken } from '../auth.js';
import { fetchProfileByAccountId } from '../services/profileApi.js';

const username = ref(getUsernameFromToken() || '');
const profile = ref(null);
const loadError = ref('');

onMounted(async () => {
  const aid = getAccountIdFromToken();
  if (!aid) return;
  try {
    profile.value = await fetchProfileByAccountId(aid);
  } catch (e) {
    loadError.value = e.message || 'Failed to load profile';
  }
});
</script>

<style scoped>
.card {
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 1.75rem;
}

h1 {
  margin: 0 0 0.5rem;
  font-size: 1.45rem;
}

.lead {
  color: var(--muted);
  margin-top: 0;
}

.grid {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 0.35rem 1rem;
  margin: 1rem 0;
}

dt {
  color: var(--muted);
  font-size: 0.85rem;
}

dd {
  margin: 0;
}

.error {
  color: var(--danger);
}
</style>
