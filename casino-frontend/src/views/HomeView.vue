<template>
  <div class="home-container">
    <div class="card">
      <h1>Welcome{{ username ? `, ${username}` : "" }}</h1>

      <div v-if="loadError" class="error">
        <p><strong>⚠️ Sync Error:</strong> {{ loadError }}</p>
        <button @click="refreshData" class="retry-btn">Try Re-sync</button>
      </div>

      <template v-else-if="profile">
        <p class="lead">
          Your player profile is active and synced with game services.
        </p>

        <div class="funds-summary">
          <div class="balance-info">
            <span class="label">Wallet Funds</span>
            <span class="amount">{{ formattedWalletFunds }}</span>
            <span v-if="walletError" class="balance-warning">
              {{ walletError }}
            </span>
          </div>

          <router-link to="/games" class="play-link">
            Go to Casino Lobby →
          </router-link>
        </div>

        <section class="wallet-actions">
          <div>
            <h2>Add funds</h2>
            <p>Add money to your wallet balance before joining a game.</p>
          </div>

          <form class="wallet-add-form" @submit.prevent="handleAddFunds">
            <label for="wallet-add-amount">Amount</label>
            <div class="wallet-add-controls">
              <input
                id="wallet-add-amount"
                v-model="walletAddAmount"
                type="number"
                min="0.01"
                step="0.01"
                inputmode="decimal"
                placeholder="25.00"
                :disabled="walletAddLoading"
              />
              <button
                type="submit"
                class="wallet-add-button"
                :disabled="walletAddLoading || !canAddWalletFunds"
              >
                {{ walletAddLoading ? "Adding..." : "Add funds" }}
              </button>
            </div>
            <span v-if="walletAddMessage" class="wallet-add-message success">
              {{ walletAddMessage }}
            </span>
            <span v-if="walletAddError" class="wallet-add-message error-text">
              {{ walletAddError }}
            </span>
          </form>
        </section>

        <hr class="divider" />

        <section v-if="account" class="account-card">
          <code class="endpoint">GET /auth/me</code>
          <header>
            <h2>Account</h2>
            <span
              class="status-badge"
              :class="account.status === 'ACTIVE' ? 'active' : 'suspended'"
            >
              {{ account.status }}
            </span>
          </header>

          <dl class="grid account-grid">
            <dt>Username</dt>
            <dd>{{ account.username }}</dd>

            <dt>Account ID</dt>
            <dd class="uuid-text">{{ account.accountId }}</dd>

            <dt>Roles</dt>
            <dd>{{ (account.roles || []).join(", ") || "—" }}</dd>

            <dt>Member since</dt>
            <dd>{{ formatDateTime(account.createdDate) }}</dd>
          </dl>
        </section>

        <hr v-if="account" class="divider" />
        <code class="endpoint">GET /profiles/account/{accountId}</code>
        <dl class="grid">
          <dt>Profile ID (numeric PK)</dt>
          
          <dd>
            <strong
              :class="{ 'warning-text': profile.playerProfileId == null }"
            >
              {{ profile.playerProfileId ?? "—" }}
            </strong>
          </dd>

          <dt>Account ID (auth)</dt>
          <dd class="uuid-text">{{ profile.accountId }}</dd>

          <dt>Full name</dt>
          <dd>{{ profile.fullName }}</dd>

          <dt>Date of birth</dt>
          <dd>{{ profile.dateOfBirth }}</dd>

          <dt>Status</dt>
          <dd>
            <span class="status-badge" :class="profile.status">
              {{ profile.status }}
            </span>
          </dd>

          <dt>Email</dt>
          <dd>{{ profile.email || "—" }}</dd>

          <dt>Phone</dt>
          <dd>{{ profile.phone || "—" }}</dd>

          <dt>Language / Currency</dt>
          <dd>{{ profile.language }} / {{ profile.currency }}</dd>
        </dl>

        <hr class="divider" />

        <details class="compliance-collapse" @toggle="handleComplianceToggle">
          <summary>
            <span>Compliance</span>
            <span
              v-if="complianceProfile || eligibility"
              class="compliance-badge"
              :class="complianceStatusClass"
            >
              {{
                eligibility?.riskLevel ||
                complianceProfile?.riskLevel ||
                "UNKNOWN"
              }}
            </span>
          </summary>

          <div class="compliance-body">
            <div v-if="complianceLoading" class="compliance-loading">
              Loading compliance details...
            </div>

            <div v-else-if="complianceError" class="compliance-warning">
              <p>{{ complianceError }}</p>
              <button
                type="button"
                class="retry-btn compliance-retry"
                @click="loadComplianceData(true)"
              >
                Try again
              </button>
            </div>

            <template v-else-if="complianceProfile || eligibility">
              <dl class="grid compliance-grid">
                <dt>Compliance ID</dt>
                <dd>{{ complianceProfile?.complianceId ?? "—" }}</dd>

                <dt>Age verified</dt>
                <dd>
                  {{
                    formatBoolean(
                      eligibility?.ageVerified ?? complianceProfile?.ageVerified
                    )
                  }}
                </dd>

                <dt>Self-excluded</dt>
                <dd>
                  <strong
                    :class="{
                      'danger-text':
                        eligibility?.selfExcluded ||
                        complianceProfile?.selfExcluded,
                    }"
                  >
                    {{
                      formatBoolean(
                        eligibility?.selfExcluded ??
                          complianceProfile?.selfExcluded
                      )
                    }}
                  </strong>
                </dd>

                <dt>Risk level</dt>
                <dd>
                  {{
                    eligibility?.riskLevel ||
                    complianceProfile?.riskLevel ||
                    "—"
                  }}
                </dd>

                <dt>May bet</dt>
                <dd>
                  <span class="pill" :class="mayBet ? 'ok' : 'danger'">
                    {{ formatBoolean(eligibility?.mayBet) }}
                  </span>
                </dd>

                <dt>May withdraw</dt>
                <dd>
                  <span class="pill" :class="mayWithdraw ? 'ok' : 'danger'">
                    {{ formatBoolean(eligibility?.mayWithdraw) }}
                  </span>
                </dd>

                <dt>Active bet limit</dt>
                <dd>{{ formatLimit(eligibility?.activeBetLimit) }}</dd>

                <dt>Active withdrawal limit</dt>
                <dd>{{ formatLimit(eligibility?.activeWithdrawalLimit) }}</dd>

                <dt>Last review</dt>
                <dd>{{ formatDateTime(complianceProfile?.lastReviewDate) }}</dd>

                <dt>Checked at</dt>
                <dd>{{ formatDateTime(eligibility?.checkedAt) }}</dd>
              </dl>

              <div
                v-if="eligibility?.blockReasons?.length"
                class="block-reasons"
              >
                <h3>Blocking reasons</h3>
                <ul>
                  <li v-for="reason in eligibility.blockReasons" :key="reason">
                    {{ reason }}
                  </li>
                </ul>
              </div>

              <div
                v-if="complianceProfile?.limits?.length"
                class="compliance-list"
              >
                <h3>Configured limits</h3>

                <div
                  v-for="limit in complianceProfile.limits"
                  :key="limit.limitId"
                  class="list-row"
                >
                  <span>{{ limit.type }}</span>
                  <strong>{{ limit.amount }} / {{ limit.period }}</strong>
                </div>
              </div>

              <div
                v-if="complianceProfile?.flags?.length"
                class="compliance-list"
              >
                <h3>Compliance flags</h3>

                <div
                  v-for="flag in complianceProfile.flags"
                  :key="flag.flagId"
                  class="list-row"
                >
                  <span>{{ flag.type }}</span>
                  <strong>{{ flag.severity }}</strong>
                </div>
              </div>
            </template>

            <div v-else-if="complianceLoaded" class="compliance-warning">
              Could not access compliance profile details.
            </div>

            <div v-else class="compliance-muted">
              Open this section to load compliance profile details.
            </div>
          </div>
        </details>
      </template>

      <div v-else class="loading-state">
        <div class="spinner"></div>
        <p>Syncing player identity across services...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { getAccountIdFromToken, getUsernameFromToken } from "../auth.js";
import { fetchProfileByAccountId } from "../services/profileApi.js";
import { fetchCurrentAccount } from "../services/authApi.js";
import { addWalletFunds, fetchWallet } from "../services/walletApi.js";
import {
  fetchComplianceProfile,
  fetchPlayerEligibility,
  createComplianceProfileDevOnly,
} from "../services/complianceApi.js";

const username = ref(getUsernameFromToken() || "");

const profile = ref(null);
const account = ref(null);
const walletFunds = ref(null);
const walletError = ref("");
const walletAddAmount = ref("");
const walletAddLoading = ref(false);
const walletAddError = ref("");
const walletAddMessage = ref("");
const loadError = ref("");

const complianceProfile = ref(null);
const eligibility = ref(null);
const complianceError = ref("");
const complianceLoading = ref(false);
const complianceLoaded = ref(false);

const mayBet = computed(() => eligibility.value?.mayBet === true);
const mayWithdraw = computed(() => eligibility.value?.mayWithdraw === true);

const formattedWalletFunds = computed(() => {
  if (walletFunds.value === null || walletFunds.value === undefined) {
    return "—";
  }

  return formatMoney(walletFunds.value);
});

const canAddWalletFunds = computed(() => {
  const amount = Number(walletAddAmount.value);
  return Number.isFinite(amount) && amount > 0 && profile.value?.playerProfileId != null;
});

const complianceStatusClass = computed(() => {
  const risk =
    eligibility.value?.riskLevel || complianceProfile.value?.riskLevel;

  if (!eligibility.value && !complianceProfile.value) return "unknown";
  if (risk === "CRITICAL" || risk === "HIGH") return "danger";
  if (risk === "MEDIUM") return "warning";
  if (mayBet.value && mayWithdraw.value) return "ok";

  return "warning";
});

const resetComplianceState = () => {
  complianceProfile.value = null;
  eligibility.value = null;
  complianceError.value = "";
  complianceLoading.value = false;
  complianceLoaded.value = false;
};

const formatBoolean = (value) => {
  if (value === true) return "Yes";
  if (value === false) return "No";
  return "—";
};

const formatDateTime = (value) => {
  if (!value) return "—";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("et-EE", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
};

const formatLimit = (limit) => {
  if (!limit) return "—";
  return `${limit.amount} / ${limit.period}`;
};

const formatMoney = (value) => {
  const amount = Number(value ?? 0);

  if (Number.isNaN(amount)) {
    return "—";
  }

  try {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: profile.value?.currency || "USD",
    }).format(amount);
  } catch {
    return amount.toFixed(2);
  }
};

const loadWalletFunds = async (playerProfileId) => {
  walletFunds.value = null;

  if (playerProfileId === undefined || playerProfileId === null) {
    walletError.value = "Player profile ID is missing.";
    return;
  }

  try {
    const wallet = await fetchWallet(playerProfileId);
    walletFunds.value = wallet.availableBalance;
    walletError.value = "";
  } catch (e) {
    console.error("Wallet fetch failed:", e);
    walletError.value = e.message || "Could not load wallet funds.";
  }
};

const refreshData = async () => {
  loadError.value = "";
  walletError.value = "";
  walletAddError.value = "";
  walletAddMessage.value = "";
  walletFunds.value = null;
  resetComplianceState();
  account.value = null;

  const aid = getAccountIdFromToken();

  if (!aid) {
    loadError.value = "Session expired. Please log in again.";
    return;
  }

  try {
    const [accountResult, profileData] = await Promise.all([
      fetchCurrentAccount().catch((e) => {
        console.warn("/auth/me failed:", e);
        return null;
      }),
      fetchProfileByAccountId(aid),
    ]);

    if (accountResult) {
      account.value = accountResult;
      if (accountResult.username) {
        username.value = accountResult.username;
      }
    }

    console.log("Profile Data received:", profileData);

    if (!profileData) {
      throw new Error("No player profile associated with this account.");
    }

    profile.value = profileData;

    await loadWalletFunds(profileData.playerProfileId);
  } catch (e) {
    console.error("HomeView Sync Error:", e);
    loadError.value =
      e.message || "Failed to communicate with profile service.";
  }
};

const handleAddFunds = async () => {
  walletAddError.value = "";
  walletAddMessage.value = "";

  const playerProfileId = profile.value?.playerProfileId;
  const amount = Number(walletAddAmount.value);

  if (playerProfileId === undefined || playerProfileId === null) {
    walletAddError.value = "Player profile ID is missing.";
    return;
  }

  if (!Number.isFinite(amount) || amount <= 0) {
    walletAddError.value = "Enter an amount greater than 0.";
    return;
  }

  walletAddLoading.value = true;
  try {
    const wallet = await addWalletFunds(playerProfileId, amount.toFixed(2));
    walletFunds.value = wallet.availableBalance;
    walletAddAmount.value = "";
    walletAddMessage.value = "Funds added";
    walletError.value = "";
  } catch (e) {
    console.error("Add wallet funds failed:", e);
    walletAddError.value = e.message || "Could not add wallet funds.";
  } finally {
    walletAddLoading.value = false;
  }
};

const handleComplianceToggle = (event) => {
  if (event.target.open) {
    loadComplianceData();
  }
};

const loadComplianceData = async (force = false) => {
  if (complianceLoading.value) return;
  if (complianceLoaded.value && !force) return;

  complianceError.value = "";
  complianceLoading.value = true;

  if (force) {
    complianceProfile.value = null;
    eligibility.value = null;
    complianceLoaded.value = false;
  }

  const playerProfileId = profile.value?.playerProfileId;

  if (playerProfileId === undefined || playerProfileId === null) {
    complianceLoading.value = false;
    complianceLoaded.value = true;
    complianceError.value =
      "Could not access compliance profile details because the player profile ID is missing.";
    return;
  }

  try {
    let createdProfileInDev = false;

    const [complianceResult, eligibilityResult] = await Promise.allSettled([
      fetchComplianceProfile(playerProfileId),
      fetchPlayerEligibility(playerProfileId),
    ]);

    if (complianceResult.status === "fulfilled") {
      complianceProfile.value = complianceResult.value;
    } else {
      console.error(
        "Compliance profile fetch failed:",
        complianceResult.reason
      );
    }

    if (eligibilityResult.status === "fulfilled") {
      eligibility.value = eligibilityResult.value;
    } else {
      console.error("Eligibility fetch failed:", eligibilityResult.reason);
    }

    if (!complianceProfile.value && !eligibility.value) {
      // TODO REMOVE: Dev-only fallback for local testing.
      // This creates a default compliance profile if it is missing.
      if (import.meta.env.DEV) {
        try {
          complianceProfile.value = await createComplianceProfileDevOnly(
            playerProfileId
          );

          createdProfileInDev = true;

          try {
            eligibility.value = await fetchPlayerEligibility(playerProfileId);
          } catch (eligibilityError) {
            console.error(
              "Eligibility fetch after dev compliance creation failed:",
              eligibilityError
            );
          }
        } catch (createError) {
          console.error("Dev compliance profile creation failed:", createError);
        }
      }
    }

    if (!complianceProfile.value && !eligibility.value) {
      complianceError.value = "Could not access compliance profile details.";
    }

    if (createdProfileInDev) {
      console.warn(
        "TODO REMOVE: Dev-only compliance profile was created from HomeView."
      );
    }
  } catch (e) {
    console.error("Compliance section error:", e);
    complianceError.value = "Could not access compliance profile details.";
  } finally {
    complianceLoading.value = false;
    complianceLoaded.value = true;
  }
};

onMounted(refreshData);
</script>

<style scoped>
.home-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.card {
  background: #1e1e1e;
  border: 1px solid #333;
  border-radius: 12px;
  padding: 1.75rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  color: #eee;
}

h1 {
  margin: 0 0 0.5rem;
  font-size: 1.6rem;
  color: #fff;
}

.lead {
  color: #888;
  margin-bottom: 1.5rem;
  font-size: 0.95rem;
}

.funds-summary {
  background: rgba(76, 175, 80, 0.1);
  border: 1px solid #4caf50;
  border-radius: 10px;
  padding: 1.5rem;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) auto;
  gap: 1.25rem;
  align-items: center;
  margin-bottom: 1rem;
}

.balance-info {
  min-width: 0;
}

.amount {
  font-size: 2rem;
  font-weight: bold;
  color: #4caf50;
}

.label {
  font-size: 0.75rem;
  text-transform: uppercase;
  color: #aaa;
  letter-spacing: 1px;
}

.balance-warning {
  display: block;
  margin-top: 0.25rem;
  color: #ffb74d;
  font-size: 0.8rem;
}

.wallet-actions {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(260px, 340px);
  gap: 1.25rem;
  align-items: end;
  background: #181818;
  border: 1px solid #333;
  border-radius: 10px;
  padding: 1rem;
  margin-bottom: 2rem;
}

.wallet-actions h2 {
  margin: 0 0 0.35rem;
  color: #fff;
  font-size: 1rem;
}

.wallet-actions p {
  margin: 0;
  color: #888;
  font-size: 0.9rem;
  line-height: 1.4;
}

.wallet-add-form {
  min-width: 0;
}

.wallet-add-form label {
  display: block;
  margin-bottom: 0.4rem;
  color: #aaa;
  font-size: 0.75rem;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.wallet-add-controls {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 0.5rem;
}

.wallet-add-controls input {
  min-width: 0;
  width: 100%;
  border: 1px solid #3f6641;
  border-radius: 8px;
  background: #151f17;
  color: #eee;
  padding: 0.65rem 0.75rem;
}

.wallet-add-controls input:disabled {
  opacity: 0.7;
}

.wallet-add-button {
  border: none;
  border-radius: 8px;
  background: #4caf50;
  color: #fff;
  cursor: pointer;
  font-weight: bold;
  padding: 0.65rem 0.9rem;
  white-space: nowrap;
}

.wallet-add-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.wallet-add-message {
  display: block;
  margin-top: 0.35rem;
  font-size: 0.8rem;
}

.wallet-add-message.success {
  color: #81c784;
}

.wallet-add-message.error-text {
  color: #ff8a80;
}

@media (max-width: 680px) {
  .funds-summary,
  .wallet-actions {
    grid-template-columns: 1fr;
  }

  .play-link {
    text-align: center;
  }
}

.play-link {
  background: #4caf50;
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: bold;
  transition: transform 0.2s;
}

.play-link:hover {
  transform: scale(1.05);
}

.grid {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 0.8rem 1rem;
  margin: 1rem 0;
  font-size: 0.9rem;
}

dt {
  color: #777;
}

dd {
  margin: 0;
  color: #ddd;
  word-break: break-all;
}

.uuid-text {
  font-family: "Courier New", Courier, monospace;
  font-size: 0.8rem;
  color: #999;
}

.warning-text {
  color: #ff9800;
}

.status-badge {
  background: #2e7d32;
  color: #fff;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 0.7rem;
  font-weight: bold;
}

.error {
  color: #ff5252;
  background: rgba(255, 82, 82, 0.1);
  padding: 1.5rem;
  border-radius: 8px;
  border: 1px solid #ff5252;
  margin-bottom: 1rem;
}

.retry-btn {
  margin-top: 10px;
  padding: 5px 15px;
  background: #ff5252;
  border: none;
  color: white;
  border-radius: 4px;
  cursor: pointer;
}

.divider {
  border: 0;
  border-top: 1px solid #333;
  margin: 1.5rem 0;
}

.loading-state {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.account-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.account-card h2 {
  margin: 0;
  font-size: 1.05rem;
  color: #fff;
}

.account-grid {
  margin: 0;
}

.account-card .status-badge {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.7rem;
  font-weight: bold;
  border: 1px solid;
}

.account-card .status-badge.active {
  background: rgba(76, 175, 80, 0.15);
  color: #81c784;
  border-color: #4caf50;
}

.account-card .status-badge.suspended {
  background: rgba(255, 82, 82, 0.15);
  color: #ff8a80;
  border-color: #ff5252;
}

.compliance-collapse {
  background: #181818;
  border: 1px solid #333;
  border-radius: 10px;
  overflow: hidden;
}

.compliance-collapse summary {
  cursor: pointer;
  list-style: none;
  padding: 1rem;
  color: #eee;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.compliance-collapse summary::-webkit-details-marker {
  display: none;
}

.compliance-collapse summary::after {
  content: "Expand";
  color: #888;
  font-size: 0.75rem;
  font-weight: normal;
}

.compliance-collapse[open] summary::after {
  content: "Collapse";
}

.compliance-body {
  border-top: 1px solid #333;
  padding: 1rem;
}

.compliance-loading,
.compliance-muted {
  color: #888;
  font-size: 0.9rem;
}

.compliance-warning {
  color: #ffb74d;
  background: rgba(255, 152, 0, 0.1);
  border: 1px solid #ff9800;
  padding: 1rem;
  border-radius: 8px;
}

.compliance-warning p {
  margin: 0;
}

.compliance-retry {
  background: #ff9800;
  margin-top: 0.75rem;
}

.compliance-grid {
  margin-top: 0;
}

.compliance-badge {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.7rem;
  font-weight: bold;
  border: 1px solid #555;
  color: #ddd;
  background: #2b2b2b;
}

.compliance-badge.ok {
  background: rgba(76, 175, 80, 0.15);
  border-color: #4caf50;
  color: #81c784;
}

.compliance-badge.warning {
  background: rgba(255, 152, 0, 0.15);
  border-color: #ff9800;
  color: #ffb74d;
}

.compliance-badge.danger {
  background: rgba(255, 82, 82, 0.15);
  border-color: #ff5252;
  color: #ff8a80;
}

.compliance-badge.unknown {
  background: rgba(158, 158, 158, 0.12);
  border-color: #777;
  color: #aaa;
}

.pill {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: bold;
}

.pill.ok {
  background: rgba(76, 175, 80, 0.15);
  color: #81c784;
  border: 1px solid #4caf50;
}

.pill.danger {
  background: rgba(255, 82, 82, 0.15);
  color: #ff8a80;
  border: 1px solid #ff5252;
}

.danger-text {
  color: #ff5252;
}

.block-reasons,
.compliance-list {
  margin-top: 1.25rem;
  background: #141414;
  border: 1px solid #333;
  border-radius: 10px;
  padding: 1rem;
}

.block-reasons h3,
.compliance-list h3 {
  margin: 0 0 0.75rem;
  font-size: 0.95rem;
  color: #fff;
}

.block-reasons ul {
  margin: 0;
  padding-left: 1.25rem;
  color: #ffb74d;
}

.list-row {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.5rem 0;
  border-top: 1px solid #2a2a2a;
  font-size: 0.85rem;
}

.list-row:first-of-type {
  border-top: 0;
}

.list-row span {
  color: #aaa;
}

.list-row strong {
  color: #eee;
}
.endpoint {
  background: #121a24;
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  font-size: 0.8rem;
  color: var(--accent);
}

</style>
