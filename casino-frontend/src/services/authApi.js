import { clearToken, getToken, setToken } from '../auth.js';

async function readErrorMessage(res) {
  const text = await res.text();
  try {
    const j = JSON.parse(text);
    return j.message || j.detail || j.title || text || res.statusText;
  } catch {
    return text || res.statusText;
  }
}

export async function login(username, password) {
  const res = await fetch('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) {
    throw new Error(await readErrorMessage(res));
  }
  const data = await res.json();
  if (!data.token) throw new Error('No token in response');
  setToken(data.token);
  return data.token;
}

export async function register(username, password) {
  const res = await fetch('/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) {
    throw new Error(await readErrorMessage(res));
  }
  const data = await res.json();
  if (!data.token) throw new Error('No token in response');
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
