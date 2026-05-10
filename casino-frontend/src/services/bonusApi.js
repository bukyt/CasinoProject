import { getToken } from '../auth.js';

function authHeaders() {
  const token = getToken();
  const h = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return h;
}

/**
 * GET /bonuses/players/{playerId}/credits
 * Returns the accumulated bonus credits (Double) for a player.
 * playerId is the playerProfileId (Integer) from the profile service.
 */
export async function fetchPlayerCredits(playerId) {
  const res = await fetch(`/bonuses/players/${encodeURIComponent(playerId)}/credits`, {
    headers: authHeaders(),
  });
  if (!res.ok) {
    throw new Error(`Bonus credits HTTP ${res.status}`);
  }
  return res.json(); // returns a number
}

/**
 * GET /bonuses/players/{playerId}
 * Returns list of PlayerBonus objects assigned to a player.
 */
export async function fetchPlayerBonuses(playerId) {
  const res = await fetch(`/bonuses/players/${encodeURIComponent(playerId)}`, {
    headers: authHeaders(),
  });
  if (!res.ok) {
    throw new Error(`Player bonuses HTTP ${res.status}`);
  }
  return res.json();
}

/**
 * GET /bonuses
 * Returns all available bonus definitions.
 */
export async function fetchBonuses() {
  const res = await fetch('/bonuses', {
    headers: authHeaders(),
  });
  if (!res.ok) {
    throw new Error(`Bonuses list HTTP ${res.status}`);
  }
  return res.json();
}