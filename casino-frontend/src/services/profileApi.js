import { getToken } from '../auth.js';

/**
 * GET /profiles/account/{accountId}?optional=true — profile-service returns 204 when no profile (success in Network),
 * 200 + JSON when found. Same path as your API doc; query flag is additive.
 */
export async function fetchProfileByAccountId(accountId) {
  const qs = new URLSearchParams({ optional: 'true' });
  const res = await fetch(`/profiles/account/${encodeURIComponent(accountId)}?${qs}`);
  if (res.status === 204) {
    return null;
  }
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Profile HTTP ${res.status}`);
  }
  return res.json();
}

export async function createPlayerProfile(payload) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const res = await fetch('/profiles', {
    method: 'POST',
    headers,
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Create profile HTTP ${res.status}`);
  }
  return res.json();
}
