<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { isAuthenticated } from './auth.js';
import { logoutRemote } from './services/authApi.js';

const route = useRoute();
const router = useRouter();

const authed = computed(() => {
  route.path;
  return isAuthenticated();
});

async function logout() {
  await logoutRemote();
  router.push({ name: 'Login' });
}
</script>

<template>
  <div class="app-root">
    <header class="top-bar">
      <span class="brand">Casino Player</span>
      <nav v-if="authed" class="nav-links">
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/bonus-game" class="nav-bonus">🎰 Free Roll</RouterLink>
        <button type="button" class="btn ghost" @click="logout">Logout</button>
      </nav>
      <nav v-else class="nav-links">
        <RouterLink to="/login">Login</RouterLink>
        <RouterLink to="/register">Register</RouterLink>
      </nav>
    </header>

    <main class="main">
      <RouterView />
    </main>
  </div>
</template>

<style>
:root {
  --bg: #0f1419;
  --panel: #1a2332;
  --text: #e8eef7;
  --muted: #8b9bb4;
  --accent: #3dd68c;
  --accent-dim: #2a9d66;
  --danger: #e85d6f;
  --border: #2c3a50;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
  background: var(--bg);
  color: var(--text);
}

#app {
  min-height: 100vh;
}

.app-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.85rem 1.5rem;
  background: var(--panel);
  border-bottom: 1px solid var(--border);
}

.brand {
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--accent);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.nav-links a {
  color: var(--muted);
  text-decoration: none;
  font-weight: 500;
}

.nav-links a.router-link-active {
  color: var(--accent);
}

/* Bonus link has a subtle highlight even when inactive */
.nav-links a.nav-bonus {
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 0.25rem 0.65rem;
  font-size: 0.88rem;
  transition: border-color 0.15s, color 0.15s;
}
.nav-links a.nav-bonus:hover,
.nav-links a.nav-bonus.router-link-active {
  border-color: var(--accent);
  color: var(--accent);
}

.main {
  flex: 1;
  padding: 1.5rem;
  max-width: 720px;
  margin: 0 auto;
  width: 100%;
}

.btn {
  cursor: pointer;
  border: none;
  border-radius: 8px;
  padding: 0.55rem 1rem;
  font-weight: 600;
  font-size: 0.9rem;
}

.btn.ghost {
  background: transparent;
  color: var(--danger);
  border: 1px solid var(--danger);
}

.btn.primary {
  background: var(--accent);
  color: #0a0f14;
}

.btn.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>