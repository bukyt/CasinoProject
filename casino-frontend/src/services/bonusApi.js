import { getToken } from '../auth.js';

function authHeaders() {
  const token = getToken();
  const h = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return h;
}

/**
 * POST /bonuses/{bonusId}/assign
 * This is the call that gives the +10 credits based on your new Java logic
 */
export async function assignBonusToPlayer(bonusId, playerId) {
  const res = await fetch(`/bonuses/${bonusId}/assign`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ 
      playerId: String(playerId) // Matches AssignBonusRequest DTO
    })
  });

  if (!res.ok) throw new Error(`Assign bonus failed: ${res.status}`);
  return await res.json();
}

/**
 * DEBUG: Directly adds money to the Real Balance (Ledger)
 */
export async function debugAddCredits(playerId, amount = 50.0) {
  // Append the amount to the URL instead of the body
  const res = await fetch(`/bonuses/players/${playerId}/debug-add?amount=${amount}`, {
    method: 'POST',
    headers: authHeaders(),
    // body: JSON.stringify({ amount: amount }) // Remove this if using @RequestParam
  });
  
  if (!res.ok) throw new Error(`Debug credits failed: ${res.status}`);
  
  const data = await res.json();
  return data && data.balance !== undefined ? data.balance : 0;
}

/**
 * GET /bonuses
 * Lists all defined bonuses (Welcome Bonus, etc.)
 */
export async function fetchBonuses() {
  const res = await fetch('/bonuses', {
    headers: authHeaders(),
  });
  if (!res.ok) throw new Error(`Bonuses list HTTP ${res.status}`);
  return res.json();
}