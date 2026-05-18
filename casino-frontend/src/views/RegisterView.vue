<template>
  <div class="card">
    <h1>Register</h1>
    <p class="hint">Create an account. You will set up your player profile next.</p>

    <form class="form" @submit.prevent="submit">
      <label>
        Username
        <input v-model="username" type="text" autocomplete="username" required />
      </label>
      <label>
        Password
        <input v-model="password" type="password" autocomplete="new-password" required minlength="6" />
      </label>
      <label>
        Confirm password
        <input v-model="password2" type="password" autocomplete="new-password" required minlength="6" />
      </label>
      <p v-if="error" class="error">{{ error }}</p>
      <button class="btn primary" type="submit" :disabled="loading">
        {{ loading ? 'Creating…' : 'Create account' }}
      </button>
    </form>

    <p class="footer">
      Already have an account?
      <RouterLink to="/login">Login</RouterLink>
    </p>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { register } from '../services/authApi.js';

const router = useRouter();

const username = ref('');
const password = ref('');
const password2 = ref('');
const error = ref('');
const loading = ref(false);

async function submit() {
  error.value = '';
  if (password.value !== password2.value) {
    error.value = 'Passwords do not match';
    return;
  }
  loading.value = true;
  try {
    await register(username.value, password.value);
  } catch (e) {
    error.value = e.message || 'Registration failed';
    loading.value = false;
    return;
  }

  try {
    await router.push('/complete-profile');
  } catch (e) {
    console.error('Post-registration navigation failed:', e);
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
  font-size: 1.5rem;
}

.hint {
  margin: 0 0 1.25rem;
  color: var(--muted);
  font-size: 0.9rem;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

label {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.85rem;
  color: var(--muted);
}

input {
  padding: 0.6rem 0.75rem;
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

.footer {
  margin-top: 1.25rem;
  font-size: 0.9rem;
  color: var(--muted);
}

.footer a {
  color: var(--accent);
}
</style>
