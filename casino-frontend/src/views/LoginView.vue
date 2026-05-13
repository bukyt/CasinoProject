<template>
  <div class="card">
    <h1>Login</h1>
    <p class="hint">You must sign in before using the app.</p>

    <form class="form" @submit.prevent="submit">
      <label>
        Username
        <input v-model="username" type="text" autocomplete="username" required />
      </label>
      <label>
        Password
        <input v-model="password" type="password" autocomplete="current-password" required />
      </label>

      <div v-if="suspended" class="warning" role="alert">
        <strong>Account suspended</strong>
        <p>{{ error }}</p>
      </div>
      <p v-else-if="error" class="error">{{ error }}</p>

      <button class="btn primary" type="submit" :disabled="loading">
        {{ loading ? 'Signing in…' : 'Login' }}
      </button>
    </form>

    <p class="footer">
      No account?
      <RouterLink to="/register">Register</RouterLink>
    </p>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { login } from '../services/authApi.js';

const router = useRouter();
const route = useRoute();

const username = ref('');
const password = ref('');
const error = ref('');
const loading = ref(false);

const suspended = computed(() => /suspend/i.test(error.value));

async function submit() {
  error.value = '';
  loading.value = true;
  try {
    await login(username.value, password.value);
    const redirect = route.query.redirect;
    router.push(typeof redirect === 'string' ? redirect : '/');
  } catch (e) {
    error.value = e.message || 'Login failed';
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

.warning {
  background: rgba(255, 152, 0, 0.1);
  border: 1px solid #ff9800;
  color: #ffb74d;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.9rem;
}

.warning strong {
  display: block;
  color: #ffcc80;
  margin-bottom: 0.25rem;
}

.warning p {
  margin: 0;
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
