import { getToken } from "../auth.js";

function authHeaders() {
  const token = getToken();

  const headers = {
    Accept: "application/json",
    "Content-Type": "application/json",
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return headers;
}

async function readJsonResponse(response, fallbackMessage) {
  if (response.status === 204) {
    return null;
  }

  let body = null;

  try {
    body = await response.json();
  } catch {
    // Ignore non-JSON body.
  }

  if (!response.ok) {
    const message =
      body?.message ||
      body?.error ||
      `${fallbackMessage}. Status: ${response.status}`;

    throw new Error(message);
  }

  return body;
}

export async function createDepositPayment(body) {
  const response = await fetch("/payments/deposits", {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(body),
  });

  return readJsonResponse(response, "Deposit request failed");
}

export async function createWithdrawalPayment(body) {
  const response = await fetch("/payments/withdrawals", {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(body),
  });

  return readJsonResponse(response, "Withdrawal request failed");
}

export async function fetchPayment(paymentId) {
  const response = await fetch(`/payments/${encodeURIComponent(paymentId)}`, {
    method: "GET",
    headers: authHeaders(),
  });

  return readJsonResponse(response, "Fetching payment failed");
}

export async function fetchPlayerPayments(playerProfileId) {
  const response = await fetch(
    `/payments/player/${encodeURIComponent(playerProfileId)}`,
    {
      method: "GET",
      headers: authHeaders(),
    }
  );

  return readJsonResponse(response, "Fetching player payments failed");
}

export async function submitPaymentProviderWebhook(body) {
  const response = await fetch("/payments/provider/webhook", {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(body),
  });

  return readJsonResponse(response, "Provider webhook request failed");
}