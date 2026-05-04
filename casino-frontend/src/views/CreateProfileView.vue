<template>
  <div class="card">
    <h1>Create player profile</h1>
    <p class="hint">
      Logged in as <strong>{{ usernameHint }}</strong>. Account id
      <code>{{ accountId }}</code> will be linked to this profile.
    </p>

    <form class="form" @submit.prevent="submit">
      <label>
        Full name *
        <input v-model="fullName" type="text" required />
      </label>
      <label>
        Date of birth *
        <input v-model="dateOfBirth" type="date" required />
      </label>
      <label>
        Profile status *
        <select v-model="status">
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
        </select>
      </label>
      <label>
        Email
        <input v-model="email" type="email" />
      </label>
      <label>
        Phone
        <input v-model="phone" type="tel" />
      </label>
      <label>
        Address
        <input v-model="address" type="text" />
      </label>
      <label>
        Language
        <input v-model="language" type="text" maxlength="8" />
      </label>
      <label>
        Currency
        <input v-model="currency" type="text" maxlength="8" />
      </label>

      <p v-if="error" class="error">{{ error }}</p>
      <button class="btn primary" type="submit" :disabled="loading">
        {{ loading ? 'Saving…' : 'Save profile' }}
      </button>
    </form>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getAccountIdFromToken, getUsernameFromToken } from '../auth.js';
import { createPlayerProfile } from '../services/profileApi.js';

const router = useRouter();

const accountId = computed(() => getAccountIdFromToken());
const usernameHint = computed(() => getUsernameFromToken() || '');

const fullName = ref('');
const dateOfBirth = ref('');
const status = ref('ACTIVE');
const email = ref('');
const phone = ref('');
const address = ref('');
const language = ref('en');
const currency = ref('EUR');

const error = ref('');
const loading = ref(false);

async function submit() {
  error.value = '';
  if (!accountId.value) {
    error.value = 'Not logged in';
    return;
  }
  loading.value = true;
  try {
    await createPlayerProfile({
      accountId: accountId.value,
      fullName: fullName.value,
      dateOfBirth: dateOfBirth.value,
      status: status.value,
      email: email.value || undefined,
      phone: phone.value || undefined,
      address: address.value || undefined,
      language: language.value || undefined,
      currency: currency.value || undefined,
    });
    router.push('/');
  } catch (e) {
    error.value = e.message || 'Could not create profile';
  } finally {
    loading.value = false;
  }
}
</script>


<style scoped>
.card {
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 1.75rem;
}

h1 {
  margin: 0 0 0.35rem;
  font-size: 1.35rem;
}

.hint {
  margin: 0 0 1.25rem;
  color: var(--muted);
  font-size: 0.9rem;
  line-height: 1.5;
}

code {
  font-size: 0.8rem;
  background: #121a24;
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

label {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.85rem;
  color: var(--muted);
}

input,
select {
  padding: 0.55rem 0.65rem;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: #121a24;
  color: var(--text);
}

.error {
  color: var(--danger);
  margin: 0;
  font-size: 0.9rem;
}
</style>
