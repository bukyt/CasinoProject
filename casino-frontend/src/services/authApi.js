import { clearToken, getToken, setToken } from '../auth.js';
import { readApiError } from './httpError.js';

async function fail(res, context) {
  throw new Error(await readApiError(res, context));
}

function authHeaders(extra = {}) {
  const token = getToken();
  const headers = { ...extra };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

export async function login(username, password) {
  const res = await fetch('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) await fail(res, 'login');
  const data = await res.json();
  if (!data.token) throw new Error('No token in response.');
  setToken(data.token);
  return data.token;
}

export async function register(username, password) {
  const res = await fetch('/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) await fail(res, 'register');
  const data = await res.json();
  if (!data.token) throw new Error('No token in response.');
  setToken(data.token);
  return data.token;
}

export async function logoutRemote() {
  const token = getToken();
  if (token) {
    try {
      await fetch('/auth/logout', {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
      });
    } catch {
      /* ignore network errors on logout */
    }
  }
  clearToken();
}

/** GET /auth/me — authoritative view of the currently logged-in account. */
export async function fetchCurrentAccount() {
  const res = await fetch('/auth/me', { headers: authHeaders() });
  if (!res.ok) await fail(res, 'load your account');
  return res.json();
}

/** GET /auth/accounts/{accountId} */
export async function fetchAccount(accountId) {
  const res = await fetch(`/auth/accounts/${encodeURIComponent(accountId)}`, {
    headers: authHeaders(),
  });
  if (!res.ok) await fail(res, 'load account');
  return res.json();
}

/** PATCH /auth/accounts/{accountId}/status — ADMIN only. Accepts "ACTIVE" or "SUSPENDED". */
export async function updateAccountStatus(accountId, status) {
  const res = await fetch(`/auth/accounts/${encodeURIComponent(accountId)}/status`, {
    method: 'PATCH',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify({ status }),
  });
  if (!res.ok) await fail(res, 'update account status');
  return res.json();
}
