import { createRouter, createWebHistory } from 'vue-router';
import { isAuthenticated, getAccountIdFromToken, clearToken } from '../auth.js';
import { fetchProfileByAccountId } from '../services/profileApi.js';

import HomeView from '../views/HomeView.vue';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import CreateProfileView from '../views/CreateProfileView.vue';
import BonusGameView from '../views/BonusGameView.vue';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView,
    meta: { requiresAuth: true, requiresProfile: true },
  },
  {
    path: '/complete-profile',
    name: 'CompleteProfile',
    component: CreateProfileView,
    meta: { requiresAuth: true, requiresProfile: false },
  },
  {
    path: '/bonus-game',
    name: 'BonusGame',
    component: BonusGameView,
    meta: { requiresAuth: true, requiresProfile: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { public: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView,
    meta: { public: true },
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

router.beforeEach(async (to, _from, next) => {
  if (to.meta.public) {
    if (isAuthenticated() && (to.name === 'Login' || to.name === 'Register')) {
      return next({ name: 'Home' });
    }
    return next();
  }

  if (!isAuthenticated()) {
    return next({ name: 'Login', query: { redirect: to.fullPath } });
  }

  const accountId = getAccountIdFromToken();
  if (!accountId) {
    clearToken();
    return next({ name: 'Login', query: { redirect: to.fullPath } });
  }

  let profile = null;
  try {
    profile = await fetchProfileByAccountId(accountId);
  } catch (e) {
    console.error(e);
    return next(false);
  }

  if (to.name === 'CompleteProfile') {
    if (profile) return next({ name: 'Home' });
    return next();
  }

  if (to.meta.requiresProfile && !profile) {
    return next({ name: 'CompleteProfile', query: { redirect: to.fullPath } });
  }

  return next();
});

export default router;