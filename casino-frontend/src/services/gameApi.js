import { getToken } from '../auth.js';

function authHeaders() {
  const token = getToken();
  const h = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return h;
}

/**
 * POST /games/sessions
 * Creates a new game session. For a free roll, pass initialBalance = 10
 * (represents the bonus credit value for this spin).
 *
 * @param {string} gameId         - e.g. "bonus-roll"
 * @param {number} initialBalance - balance for the session (10 per free roll)
 * @param {number} playerProfileId
 * @returns {Promise<GameSession>} - { id, gameId, playerProfileId, balance, status, bets: [] }
 */
export async function createGameSession(gameId, initialBalance, playerProfileId) {
  const res = await fetch('/games/sessions', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ gameId, initialBalance, playerProfileId }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Create session HTTP ${res.status}`);
  }
  return res.json();
}

/**
 * POST /games/sessions/{sessionId}/bets
 * Places a bet in the session. Game service decides WIN/LOSE randomly.
 *
 * @param {string} sessionId
 * @param {number} amount     - must be <= session balance
 * @returns {Promise<Bet>}    - { id, amount, payout, outcome: "WIN"|"LOSE" }
 */
export async function placeBet(sessionId, amount) {
  const res = await fetch(`/games/sessions/${encodeURIComponent(sessionId)}/bets`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ amount }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Place bet HTTP ${res.status}`);
  }
  return res.json();
}

/**
 * PATCH /games/sessions/{sessionId}/close
 * Closes the session after the free roll is done.
 *
 * @param {string} sessionId
 * @returns {Promise<GameSession>}
 */
export async function closeGameSession(sessionId) {
  const res = await fetch(`/games/sessions/${encodeURIComponent(sessionId)}/close`, {
    method: 'PATCH',
    headers: authHeaders(),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Close session HTTP ${res.status}`);
  }
  return res.json();
}