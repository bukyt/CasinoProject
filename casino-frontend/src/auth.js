import { jwtDecode } from 'jwt-decode';

const TOKEN_KEY = 'casino_jwt';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function isAuthenticated() {
  const token = getToken();
  if (!token) return false;
  try {
    const { exp } = jwtDecode(token);
    return typeof exp === 'number' && exp * 1000 > Date.now();
  } catch {
    return false;
  }
}

export function getAccountIdFromToken() {
  const token = getToken();
  if (!token || !isAuthenticated()) return null;
  try {
    return jwtDecode(token).accountId ?? null;
  } catch {
    return null;
  }
}

export function getUsernameFromToken() {
  const token = getToken();
  if (!token || !isAuthenticated()) return null;
  try {
    return jwtDecode(token).sub ?? null;
  } catch {
    return null;
  }
}
