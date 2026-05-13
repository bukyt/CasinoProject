import { getToken } from '../auth.js';
import { readApiError } from './httpError.js';

function authHeaders(extra = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...extra };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

async function fail(res, context) {
  throw new Error(await readApiError(res, context));
}

/**
 * GET /profiles/account/{accountId}?optional=true — profile-service returns 204 when no profile
 * (success in Network), 200 + JSON when found. Same path as the API doc; query flag is additive.
 */
export async function fetchProfileByAccountId(accountId) {
  const qs = new URLSearchParams({ optional: 'true' });
  const res = await fetch(`/profiles/account/${encodeURIComponent(accountId)}?${qs}`, {
    headers: authHeaders(),
  });
  if (res.status === 204) {
    return null;
  }
  if (!res.ok) await fail(res, 'load your profile');
  return res.json();
}

/** GET /profiles/{playerProfileId} */
export async function fetchProfileById(playerProfileId) {
  const res = await fetch(`/profiles/${encodeURIComponent(playerProfileId)}`, {
    headers: authHeaders(),
  });
  if (!res.ok) await fail(res, 'load profile');
  return res.json();
}

/** POST /profiles */
export async function createPlayerProfile(payload) {
  const res = await fetch('/profiles', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });
  if (!res.ok) await fail(res, 'create profile');
  return res.json();
}

/** PUT /profiles/{playerProfileId} — full replace; body must contain all required fields. */
export async function updateProfile(playerProfileId, payload) {
  const res = await fetch(`/profiles/${encodeURIComponent(playerProfileId)}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });
  if (!res.ok) await fail(res, 'update profile');
  return res.json();
}

/** PATCH /profiles/{playerProfileId}/contact — partial update; only non-null fields are applied. */
export async function updateContactDetails(playerProfileId, payload) {
  const res = await fetch(`/profiles/${encodeURIComponent(playerProfileId)}/contact`, {
    method: 'PATCH',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });
  if (!res.ok) await fail(res, 'update contact details');
  return res.json();
}

/** PATCH /profiles/{playerProfileId}/preferences — partial update; only non-null fields are applied. */
export async function updatePreferences(playerProfileId, payload) {
  const res = await fetch(`/profiles/${encodeURIComponent(playerProfileId)}/preferences`, {
    method: 'PATCH',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });
  if (!res.ok) await fail(res, 'update preferences');
  return res.json();
}
