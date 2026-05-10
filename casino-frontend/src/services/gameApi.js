import { getToken } from '../auth.js';

function authHeaders() {
  const token = getToken();
  const h = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return h;
}

/**
 * POST /games/sessions
 * Creates a new game session.
 */
export async function createGameSession({ gameId, initialBalance, playerProfileId }) {
  // Added leading slash to ensure it hits http://localhost:5173/games (proxy root)
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
 */
export async function placeBet(sessionId, amount) {
  // Ensure the URL is absolute relative to the domain root
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

/**
 * GET /games
 * Returns a list of available games.
 */
export async function fetchGames() {
  const res = await fetch('/games', {
    headers: authHeaders(),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Fetch games HTTP ${res.status}`);
  }
  return res.json();
}