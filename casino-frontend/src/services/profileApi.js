/**
 * Profile service (no JWT on backend in current setup).
 * Uses accountId from auth JWT — must match the logged-in account.
 *
 * Lookup uses `/api/profile-by-account/:id` so the Vite dev/preview server can map
 * backend 404 to 200 + JSON null (no "failed" request in the Network panel).
 */
export async function fetchProfileByAccountId(accountId) {
  const res = await fetch(`/api/profile-by-account/${encodeURIComponent(accountId)}`);
  if (res.status === 404) return null;
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Profile HTTP ${res.status}`);
  }
  return res.json();
}

export async function createPlayerProfile(payload) {
  const res = await fetch('/profiles', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Create profile HTTP ${res.status}`);
  }
  return res.json();
}
