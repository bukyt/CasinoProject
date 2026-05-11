import { getToken } from '../auth.js';

function authHeaders() {
  const token = getToken();
  const h = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return h;
}

/**
 * GET /ledger/player/{playerId}/balance
 */
export async function fetchAccountBalance(playerId) {
  const res = await fetch(`/api/ledger/player/${playerId}/balance`, {
    headers: authHeaders(),
  });

  if (!res.ok) {
    throw new Error(`Ledger Error: ${res.status}`);
  }
  return res.json();
}

/**
 * POST /ledger/player/{playerId}/deposit
 * This is the missing function causing your SyntaxError
 */
export async function depositDebugFunds(playerId, amount) {
  const res = await fetch(`/api/ledger/player/${playerId}/deposit`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ amount: parseFloat(amount) })
  });

  if (!res.ok) {
    throw new Error(`Deposit failed: ${res.status}`);
  }
  
  const data = await res.json();
  // Return the new balance so the UI updates immediately
  return data.balance; 
}