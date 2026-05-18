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

async function readJsonResponse(response, fallbackMessage, options = {}) {
  if (options.nullOn404 && response.status === 404) {
    return null;
  }

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

    const error = new Error(message);

    error.status = response.status;
    error.payload = body;
    error.code = body?.code || body?.errorCode || "";
    error.data =
      body?.data ||
      body?.errors ||
      body?.fieldErrors ||
      body?.violations ||
      null;

    throw error;
  }

  return body;
}

export async function createComplianceProfile(body) {
  const response = await fetch("/compliance", {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(body),
  });

  return readJsonResponse(response, "Create compliance profile failed");
}

export async function fetchComplianceProfile(playerProfileId) {
  const response = await fetch(
    `/compliance/${encodeURIComponent(playerProfileId)}`,
    {
      method: "GET",
      headers: authHeaders(),
    }
  );

  return readJsonResponse(response, "Fetch compliance profile failed", {
    nullOn404: true,
  });
}

export async function modifyComplianceProfile(playerProfileId, body) {
  const response = await fetch(
    `/compliance/${encodeURIComponent(playerProfileId)}`,
    {
      method: "PATCH",
      headers: authHeaders(),
      body: JSON.stringify(body),
    }
  );

  return readJsonResponse(response, "Modify compliance profile failed");
}

export async function fetchPlayerEligibility(playerProfileId) {
  const response = await fetch(
    `/compliance/${encodeURIComponent(playerProfileId)}/eligibility`,
    {
      method: "GET",
      headers: authHeaders(),
    }
  );

  return readJsonResponse(response, "Fetch eligibility failed", {
    nullOn404: true,
  });
}

export async function createComplianceFlag(playerProfileId, body) {
  const response = await fetch(
    `/compliance/${encodeURIComponent(playerProfileId)}/flag`,
    {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify(body),
    }
  );

  return readJsonResponse(response, "Create compliance flag failed");
}

export async function modifyComplianceFlag(playerProfileId, flagId, body) {
  const response = await fetch(
    `/compliance/${encodeURIComponent(
      playerProfileId
    )}/flag/${encodeURIComponent(flagId)}`,
    {
      method: "PATCH",
      headers: authHeaders(),
      body: JSON.stringify(body),
    }
  );

  return readJsonResponse(response, "Modify compliance flag failed");
}

export async function createComplianceLimit(playerProfileId, body) {
  const response = await fetch(
    `/compliance/${encodeURIComponent(playerProfileId)}/limits`,
    {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify(body),
    }
  );

  return readJsonResponse(response, "Create compliance limit failed");
}

export async function fetchComplianceLimits(playerProfileId) {
  const response = await fetch(
    `/compliance/${encodeURIComponent(playerProfileId)}/limits`,
    {
      method: "GET",
      headers: authHeaders(),
    }
  );

  return readJsonResponse(response, "Fetch compliance limits failed", {
    nullOn404: true,
  });
}

export async function modifyComplianceLimit(playerProfileId, limitId, body) {
  const response = await fetch(
    `/compliance/${encodeURIComponent(
      playerProfileId
    )}/limits/${encodeURIComponent(limitId)}`,
    {
      method: "PATCH",
      headers: authHeaders(),
      body: JSON.stringify(body),
    }
  );

  return readJsonResponse(response, "Modify compliance limit failed");
}

// Optional local/dev helper.
export async function createComplianceProfileDevOnly(playerProfileId) {
  return createComplianceProfile({
    playerProfileId,
    ageVerified: true,
    selfExcluded: false,
    riskLevel: "LOW",
  });
}
