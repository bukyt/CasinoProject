import { getToken } from '../auth.js';

function authHeaders() {
  const token = getToken();
  const h = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return h;
}

/**
 * GET /wallet/{playerProfileId}
 * Fetches the current wallet state and funds for a player.
 */
export async function fetchWallet(playerProfileId) {
  const res = await fetch(`/wallet/${playerProfileId}`, {
    method: 'GET',
    headers: authHeaders(),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Fetch wallet HTTP ${res.status}`);
  }
  return res.json();
}

/**
 * POST /wallet/debit/{playerProfileId}
 * Manually deposit funds or handle manual adjustments.
 */
export async function depositFunds(playerProfileId, amount) {
  const res = await fetch(`/wallet/debit/${playerProfileId}`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ amount }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Deposit HTTP ${res.status}`);
  }
  return res.json();
}