async function getJson(path) {
  const response = await fetch(`${path}`, {
    method: "GET",
    headers: {
      Accept: "application/json",
    },
  });

  // Missing compliance profile should not necessarily break the whole HomeView.
  if (response.status === 404) {
    return null;
  }

  if (!response.ok) {
    let message = `Compliance API request failed with status ${response.status}`;

    try {
      const body = await response.json();
      message = body.message || body.error || message;
    } catch {
      // Ignore non-JSON error body.
    }

    throw new Error(message);
  }

  return response.json();
}

export function fetchComplianceProfile(playerProfileId) {
  if (playerProfileId === undefined || playerProfileId === null) {
    throw new Error("playerProfileId is required to fetch compliance profile.");
  }

  return getJson(`/compliance/${encodeURIComponent(playerProfileId)}`);
}

export function fetchPlayerEligibility(playerProfileId) {
  if (playerProfileId === undefined || playerProfileId === null) {
    throw new Error("playerProfileId is required to fetch eligibility.");
  }

  return getJson(
    `/compliance/${encodeURIComponent(playerProfileId)}/eligibility`
  );
}

export async function createComplianceProfileDevOnly(playerProfileId) {
  if (playerProfileId === undefined || playerProfileId === null) {
    throw new Error(
      "playerProfileId is required to create compliance profile."
    );
  }

  // TODO REMOVE: Dev-only fallback for local testing.
  // Do not create compliance profiles from the HomeView in production.
  const response = await fetch(`/compliance`, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      playerProfileId,

      // These defaults assume your CreateComplianceProfileDTO accepts these fields.
      // Adjust if your DTO has a different shape.
      ageVerified: true,
      selfExcluded: false,
      riskLevel: "LOW",
    }),
  });

  if (response.status === 409) {
    // Profile was created elsewhere between GET and POST.
    return fetchComplianceProfile(playerProfileId);
  }

  if (!response.ok) {
    let message = `Failed to create dev compliance profile. Status: ${response.status}`;

    try {
      const body = await response.json();
      message = body.message || body.error || message;
    } catch {
      // Ignore non-JSON error body.
    }

    throw new Error(message);
  }

  return response.json();
}
