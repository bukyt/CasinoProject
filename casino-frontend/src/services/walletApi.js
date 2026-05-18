import { getToken } from '../auth.js';
import { readApiError } from './httpError.js';

function authHeaders() {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
}

/**
 * GET /wallet/{playerProfileId}
 */
export async function fetchWallet(playerProfileId) {
  const res = await fetch(`/wallet/${encodeURIComponent(playerProfileId)}`, {
    headers: authHeaders(),
  });

  if (res.status === 404) {
    return createWallet(playerProfileId);
  }

  if (!res.ok) {
    throw new Error(await readApiError(res, 'load wallet funds'));
  }

  return res.json();
}

/**
 * POST /wallet/debit/{playerProfileId}
 * Wallet service uses "debit" for adding funds into a wallet.
 */
export async function addWalletFunds(playerProfileId, amount) {
  const res = await fetch(`/wallet/debit/${encodeURIComponent(playerProfileId)}`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ amount }),
  });

  if (!res.ok) {
    throw new Error(await readApiError(res, 'add wallet funds'));
  }

  return res.json();
}

/**
 * POST /wallet/create/{playerProfileId}
 */
async function createWallet(playerProfileId) {
  const res = await fetch(`/wallet/create/${encodeURIComponent(playerProfileId)}`, {
    method: 'POST',
    headers: authHeaders(),
  });

  if (!res.ok) {
    throw new Error(await readApiError(res, 'create wallet'));
  }

  return res.json();
}
