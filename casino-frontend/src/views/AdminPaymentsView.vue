<template>
  <main class="admin-page">
    <header class="hero-card payments-hero">
      <div class="hero-copy">
        <p class="eyebrow">Admin / Payments</p>
        <h1>Payments</h1>
        <p class="subtitle">
          Create deposits, record withdrawals, inspect payment records, and
          simulate provider callbacks with normal form controls.
        </p>
      </div>

      <aside class="mock-provider-card" aria-label="Mock payment provider">
        <div class="mock-card-topline">
          <span class="mock-logo" aria-hidden="true">MP</span>
          <div><strong>MockPay</strong><span>Sandbox gateway</span></div>
        </div>
        <div class="mock-card-number">4242 • 1888 • MOCK</div>
        <div class="mock-card-badges">
          <span>Visa test</span><span>Instant</span><span>EUR</span>
        </div>
      </aside>

      <nav class="admin-nav" aria-label="Admin sections">
        <RouterLink to="/admin">Overview</RouterLink>
        <RouterLink to="/payments">Payments</RouterLink>
        <RouterLink to="/admin/compliance">Compliance</RouterLink>
      </nav>
    </header>

    <section class="profile-default-card" aria-live="polite">
      <div>
        <p class="method-label">Default player</p>
        <h2>
          {{
            currentPlayerId
              ? `Current player #${currentPlayerId}`
              : "No current player loaded"
          }}
        </h2>
        <p class="profile-meta">
          Payment forms use this player by default. Enter a player ID inside a
          form only when you want to override it.
        </p>
      </div>
      <button
        class="btn secondary"
        type="button"
        :disabled="profileState.busy"
        @click="loadCurrentPlayerDefaults"
      >
        {{ profileState.busy ? "Loading profile…" : "Reload current player" }}
      </button>
      <OperationFeedback class="profile-error" :state="profileState" />
    </section>

    <section class="layout-grid">
      <article class="card form-card highlight-card">
        <header class="card-header">
          <div>
            <p class="method-label">Deposit</p>
            <h2>Create player deposit</h2>
          </div>
          <code class="endpoint">POST /payments/deposits</code>
        </header>
        <form @submit.prevent="runDeposit">
          <div class="field-row">
            <label>
              Player profile ID <span class="optional">optional override</span>
              <input
                v-model.number="depositForm.playerProfileId"
                type="number"
                min="1"
                :placeholder="playerPlaceholder"
              />
            </label>
            <label>
              Amount
              <div class="money-input">
                <span>€</span>
                <input
                  v-model.number="depositForm.amount"
                  type="number"
                  min="0.01"
                  step="0.01"
                  required
                />
              </div>
            </label>
          </div>

          <label>
            Provider
            <select v-model="depositForm.provider">
              <option value="MOCK">MockPay sandbox</option>
              <option value="CARD">Card test provider</option>
              <option value="BANK_TRANSFER">Bank transfer mock</option>
            </select>
          </label>

          <div class="payment-preview">
            <span class="brand-chip">
              {{ providerInitials(depositForm.provider) }}
            </span>
            <div>
              <strong>{{ providerLabel(depositForm.provider) }}</strong>
              <span>
                Deposit {{ formatCurrency(depositForm.amount) }} to player #{{
                  playerDisplay(depositForm.playerProfileId)
                }}
              </span>
            </div>
          </div>

          <button
            class="btn primary"
            type="submit"
            :disabled="depositState.busy || !canSubmitMoneyForm(depositForm)"
          >
            {{ depositState.busy ? "Creating deposit…" : "Create deposit" }}
          </button>
        </form>
        <OperationFeedback :state="depositState" />
      </article>

      <article class="card form-card">
        <header class="card-header">
          <div>
            <p class="method-label">Withdrawal</p>
            <h2>Request withdrawal</h2>
          </div>
          <code class="endpoint">POST /payments/withdrawals</code>
        </header>
        <form @submit.prevent="runWithdrawal">
          <div class="field-row">
            <label>
              Player profile ID <span class="optional">optional override</span>
              <input
                v-model.number="withdrawalForm.playerProfileId"
                type="number"
                min="1"
                :placeholder="playerPlaceholder"
              />
            </label>
            <label>
              Amount
              <div class="money-input">
                <span>€</span>
                <input
                  v-model.number="withdrawalForm.amount"
                  type="number"
                  min="0.01"
                  step="0.01"
                  required
                />
              </div>
            </label>
          </div>

          <label>
            Provider
            <select v-model="withdrawalForm.provider">
              <option value="MOCK">MockPay sandbox</option>
              <option value="CARD">Card test provider</option>
              <option value="BANK_TRANSFER">Bank transfer mock</option>
            </select>
          </label>

          <div class="payment-preview muted-preview">
            <span class="brand-chip secondary">
              {{ providerInitials(withdrawalForm.provider) }}
            </span>
            <div>
              <strong>{{ providerLabel(withdrawalForm.provider) }}</strong>
              <span>
                Withdraw {{ formatCurrency(withdrawalForm.amount) }} from player
                #{{ playerDisplay(withdrawalForm.playerProfileId) }}
              </span>
            </div>
          </div>

          <button
            class="btn primary"
            type="submit"
            :disabled="
              withdrawalState.busy || !canSubmitMoneyForm(withdrawalForm)
            "
          >
            {{
              withdrawalState.busy
                ? "Requesting withdrawal…"
                : "Request withdrawal"
            }}
          </button>
        </form>
        <OperationFeedback :state="withdrawalState" />
      </article>

      <article class="card form-card compact-card">
        <header class="card-header">
          <div>
            <p class="method-label">Lookup</p>
            <h2>Find payment</h2>
          </div>
          <code class="endpoint">GET /payments/{paymentId}</code>
        </header>
        <form @submit.prevent="runPaymentLookup">
          <label>
            Payment ID
            <input
              v-model.number="paymentLookupId"
              type="number"
              min="1"
              placeholder="1"
              required
            />
          </label>
          <button
            class="btn secondary"
            type="submit"
            :disabled="paymentLookupState.busy || !paymentLookupId"
          >
            {{ paymentLookupState.busy ? "Loading payment…" : "Load payment" }}
          </button>
        </form>
        <OperationFeedback :state="paymentLookupState" />
      </article>

      <article class="card form-card compact-card">
        <header class="card-header">
          <div>
            <p class="method-label">History</p>
            <h2>Player payments</h2>
          </div>
          <code class="endpoint">GET /payments/player/{playerProfileId}</code>
        </header>
        <form @submit.prevent="runPlayerPayments">
          <label>
            Player profile ID <span class="optional">optional override</span>
            <input
              v-model.number="playerPaymentsId"
              type="number"
              min="1"
              :placeholder="playerPlaceholder"
            />
          </label>
          <button
            class="btn secondary"
            type="submit"
            :disabled="
              playerPaymentsState.busy || !resolvedPlayerId(playerPaymentsId)
            "
          >
            {{
              playerPaymentsState.busy
                ? "Loading history…"
                : "Load player history"
            }}
          </button>
        </form>
        <OperationFeedback :state="playerPaymentsState" />
      </article>

      <article class="card form-card webhook-card">
        <header class="card-header">
          <div>
            <p class="method-label">Provider callback</p>
            <h2>Simulate payment status</h2>
          </div>
          <code class="endpoint">POST /payments/provider/webhook</code>
        </header>
        <form @submit.prevent="runWebhook">
          <div class="field-row three-columns">
            <label>
              Payment ID
              <input
                v-model.number="webhookForm.paymentId"
                type="number"
                min="1"
                required
              />
            </label>
            <label>
              Status
              <select v-model="webhookForm.status">
                <option value="COMPLETED">Completed</option>
                <option value="PENDING">Pending</option>
                <option value="FAILED">Failed</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
            </label>
            <label>
              Provider reference <span class="optional">optional</span>
              <input
                v-model.trim="webhookForm.providerReference"
                type="text"
                placeholder="mock-txn-1001"
              />
            </label>
          </div>

          <div class="status-preview" :class="webhookForm.status.toLowerCase()">
            <span class="status-dot"></span>
            <div>
              <strong>{{ webhookStatusTitle }}</strong>
              <span>
                MockPay will update payment #{{ webhookForm.paymentId || "—" }}
                through the provider webhook endpoint.
              </span>
            </div>
          </div>

          <button
            class="btn primary"
            type="submit"
            :disabled="webhookState.busy || !webhookForm.paymentId"
          >
            {{
              webhookState.busy ? "Updating status…" : "Update payment status"
            }}
          </button>
        </form>
        <OperationFeedback :state="webhookState" />
      </article>
    </section>
  </main>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref } from "vue";
import { getAccountIdFromToken } from "../auth.js";
import { fetchProfileByAccountId } from "../services/profileApi.js";
import {
  createDepositPayment,
  createWithdrawalPayment,
  fetchPayment,
  fetchPlayerPayments,
  submitPaymentProviderWebhook,
} from "../services/paymentApi.js";

const paymentResultKeys = [
  "paymentId",
  "id",
  "status",
  "amount",
  "playerProfileId",
  "provider",
  "providerReference",
  "createdAt",
  "updatedAt",
];

const currentPlayerId = ref(null);
const currentProfile = ref(null);
const profileState = reactive(makeOperationState());
const depositState = reactive(makeOperationState());
const withdrawalState = reactive(makeOperationState());
const paymentLookupState = reactive(makeOperationState());
const playerPaymentsState = reactive(makeOperationState());
const webhookState = reactive(makeOperationState());

const depositForm = reactive({
  playerProfileId: null,
  amount: 25,
  provider: "MOCK",
});

const withdrawalForm = reactive({
  playerProfileId: null,
  amount: 10,
  provider: "MOCK",
});

const paymentLookupId = ref(null);
const playerPaymentsId = ref(null);

const webhookForm = reactive({
  paymentId: 1,
  status: "COMPLETED",
  providerReference: "",
});

const providerNames = {
  MOCK: "MockPay sandbox",
  CARD: "Card test provider",
  BANK_TRANSFER: "Bank transfer mock",
};

const playerPlaceholder = computed(() =>
  currentPlayerId.value
    ? `Using current player #${currentPlayerId.value}`
    : "Enter player profile ID"
);

const webhookStatusTitle = computed(
  () =>
    ({
      COMPLETED: "Mark as completed",
      PENDING: "Keep payment pending",
      FAILED: "Mark as failed",
      CANCELLED: "Cancel payment",
    }[webhookForm.status] || "Update payment")
);

const OperationFeedback = defineComponent({
  name: "OperationFeedback",
  props: { state: { type: Object, required: true } },
  setup(props) {
    return () => {
      const state = props.state;

      if (!state.error && !state.result) return null;

      if (state.error) {
        return h(
          "section",
          { class: "inline-result error", "aria-live": "polite" },
          [
            h("div", { class: "result-title" }, [
              h("span", state.error.title || "Action failed"),
              state.error.code ? h("code", state.error.code) : null,
            ]),
            h("p", { class: "result-message" }, state.error.message),
            renderRows(errorRows(state.error)),
            renderDetails(
              "Backend error payload",
              state.error.payload || state.error
            ),
          ]
        );
      }

      return h(
        "section",
        { class: "inline-result success", "aria-live": "polite" },
        [
          h("div", { class: "result-title" }, [
            h("span", state.successTitle || "Action completed"),
          ]),
          state.lastAction
            ? h("p", { class: "result-message" }, state.lastAction)
            : null,
          renderRows(resultRows(state.result, paymentResultKeys)),
          renderDetails("Technical response", state.result),
        ]
      );
    };
  },
});

function renderRows(rows) {
  if (!rows.length) return null;

  return h(
    "dl",
    { class: "result-rows" },
    rows.map((row) =>
      h("div", { key: row.key }, [h("dt", row.label), h("dd", row.value)])
    )
  );
}

function renderDetails(label, payload) {
  if (!payload) return null;

  return h("details", { class: "technical-details" }, [
    h("summary", label),
    h("pre", { class: "api-result" }, formatJson(payload)),
  ]);
}

function makeOperationState() {
  return {
    busy: false,
    result: null,
    error: null,
    lastAction: "",
    successTitle: "",
  };
}

function normaliseError(error, fallbackTitle) {
  return {
    title: fallbackTitle,
    message: error?.message || "Action failed.",
    code: error?.code || error?.payload?.code || "",
    data: error?.data || error?.payload?.data || null,
    status: error?.status || null,
    payload: error?.payload || null,
  };
}

function errorRows(error) {
  const rows = [];

  if (error.status) {
    rows.push({ key: "status", label: "Status", value: String(error.status) });
  }

  if (error.code) {
    rows.push({ key: "code", label: "Code", value: error.code });
  }

  return rows.concat(objectRows(error.data));
}

function objectRows(value) {
  if (!value || typeof value !== "object" || Array.isArray(value)) return [];

  return Object.entries(value).map(([key, rowValue]) => ({
    key,
    label: sentenceCase(key),
    value: displayValue(rowValue),
  }));
}

function resultRows(value, preferred = []) {
  if (Array.isArray(value)) {
    return [{ key: "items", label: "Items", value: String(value.length) }];
  }

  if (!value || typeof value !== "object") {
    return value == null
      ? []
      : [{ key: "value", label: "Value", value: displayValue(value) }];
  }

  const rows = [];

  for (const key of preferred) {
    if (value[key] !== undefined && isDisplayable(value[key])) {
      rows.push({
        key,
        label: sentenceCase(key),
        value: displayValue(value[key]),
      });
    }
  }

  if (!rows.length) return objectRows(value).slice(0, 6);

  return rows;
}

function isDisplayable(value) {
  return (
    ["string", "number", "boolean"].includes(typeof value) || value == null
  );
}

function displayValue(value) {
  if (value === null || value === undefined || value === "") return "—";
  if (typeof value === "boolean") return value ? "Yes" : "No";
  return String(value);
}

function sentenceCase(value) {
  return String(value)
    .replace(/([A-Z])/g, " $1")
    .replace(/[_-]+/g, " ")
    .replace(/^./, (char) => char.toUpperCase())
    .trim();
}

function formatJson(value) {
  return JSON.stringify(value ?? { ok: true }, null, 2);
}

function numericOrNull(value) {
  const numeric = Number(value);
  return Number.isFinite(numeric) && numeric > 0 ? numeric : null;
}

async function runAction(
  state,
  { successTitle, failureTitle, actionLabel, action }
) {
  state.result = null;
  state.error = null;
  state.lastAction = actionLabel;
  state.successTitle = successTitle;
  state.busy = true;

  try {
    state.result = (await action()) ?? { ok: true };
  } catch (error) {
    state.error = normaliseError(error, failureTitle);
  } finally {
    state.busy = false;
  }
}

function resolvedPlayerId(overrideValue) {
  return numericOrNull(overrideValue) ?? numericOrNull(currentPlayerId.value);
}

function playerDisplay(overrideValue) {
  return resolvedPlayerId(overrideValue) || "—";
}

function providerLabel(provider) {
  return providerNames[provider] || provider;
}

function providerInitials(provider) {
  return provider === "BANK_TRANSFER" ? "BT" : provider.slice(0, 2);
}

function formatCurrency(value) {
  return new Intl.NumberFormat("en-EE", {
    style: "currency",
    currency: "EUR",
  }).format(Number(value || 0));
}

function canSubmitMoneyForm(form) {
  return (
    Boolean(resolvedPlayerId(form.playerProfileId)) && Number(form.amount) > 0
  );
}

function moneyPayload(form) {
  return {
    playerProfileId: resolvedPlayerId(form.playerProfileId),
    amount: Number(form.amount),
    provider: form.provider,
  };
}

function webhookPayload() {
  const payload = {
    paymentId: Number(webhookForm.paymentId),
    status: webhookForm.status,
  };

  if (webhookForm.providerReference) {
    payload.providerReference = webhookForm.providerReference;
  }

  return payload;
}

async function loadCurrentPlayerDefaults() {
  await runAction(profileState, {
    successTitle: "Current player loaded",
    failureTitle: "Could not load current player",
    actionLabel: "Loaded player profile for default payment actions.",
    action: async () => {
      const accountId = getAccountIdFromToken();

      if (!accountId) {
        throw new Error("No account ID was found in the current token.");
      }

      const profile = await fetchProfileByAccountId(accountId);

      if (!profile || profile.playerProfileId == null) {
        throw new Error(
          "The current account does not have a player profile ID."
        );
      }

      currentProfile.value = profile;
      currentPlayerId.value = Number(profile.playerProfileId);

      return profile;
    },
  });
}

function runDeposit() {
  return runAction(depositState, {
    successTitle: "Deposit created",
    failureTitle: "Deposit failed",
    actionLabel: `Created ${formatCurrency(
      depositForm.amount
    )} deposit for player #${playerDisplay(depositForm.playerProfileId)}.`,
    action: () => createDepositPayment(moneyPayload(depositForm)),
  });
}

function runWithdrawal() {
  return runAction(withdrawalState, {
    successTitle: "Withdrawal requested",
    failureTitle: "Withdrawal failed",
    actionLabel: `Requested ${formatCurrency(
      withdrawalForm.amount
    )} withdrawal for player #${playerDisplay(
      withdrawalForm.playerProfileId
    )}.`,
    action: () => createWithdrawalPayment(moneyPayload(withdrawalForm)),
  });
}

function runPaymentLookup() {
  return runAction(paymentLookupState, {
    successTitle: "Payment loaded",
    failureTitle: "Payment lookup failed",
    actionLabel: `Loaded payment #${paymentLookupId.value}.`,
    action: () => fetchPayment(paymentLookupId.value),
  });
}

function runPlayerPayments() {
  const playerId = resolvedPlayerId(playerPaymentsId.value);

  return runAction(playerPaymentsState, {
    successTitle: "Payment history loaded",
    failureTitle: "Payment history failed",
    actionLabel: `Loaded payment history for player #${playerId}.`,
    action: () => fetchPlayerPayments(playerId),
  });
}

function runWebhook() {
  return runAction(webhookState, {
    successTitle: "Payment status updated",
    failureTitle: "Provider webhook failed",
    actionLabel: `${webhookStatusTitle.value} for payment #${webhookForm.paymentId}.`,
    action: () => submitPaymentProviderWebhook(webhookPayload()),
  });
}

onMounted(loadCurrentPlayerDefaults);
</script>

<style scoped>
.admin-page {
  width: min(1180px, calc(100% - 2rem));
  margin: 0 auto;
  padding: 1.5rem 0 2.5rem;
}

.hero-card,
.card {
  background: var(--panel, #101923);
  border: 1px solid var(--border, rgba(255, 255, 255, 0.12));
  border-radius: 22px;
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.18);
}

.hero-card {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(260px, 340px);
  gap: 1rem;
  align-items: stretch;
  padding: 1.35rem;
  margin-bottom: 1rem;
}

.hero-card:before {
  content: "";
  position: absolute;
  inset: -35% -10% auto auto;
  width: 420px;
  height: 420px;
  background: radial-gradient(
    circle,
    rgba(61, 214, 140, 0.18),
    transparent 62%
  );
  pointer-events: none;
}

.hero-card > * {
  position: relative;
  z-index: 1;
}

.eyebrow,
.method-label {
  margin: 0 0 0.35rem;
  color: var(--accent, #3dd68c);
  font-size: 0.73rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-weight: 800;
}

h1,
h2,
h3 {
  color: var(--text, #f5f7fb);
  margin: 0;
}

h1 {
  font-size: clamp(1.75rem, 4vw, 2.45rem);
  line-height: 1.05;
}

h2 {
  font-size: 1.05rem;
}

.subtitle {
  color: var(--muted, #a7b0bf);
  margin: 0.65rem 0 0;
  max-width: 690px;
  line-height: 1.55;
}

.admin-nav {
  grid-column: 1/-1;
  display: flex;
  gap: 0.55rem;
  flex-wrap: wrap;
}

.admin-nav a {
  color: var(--text, #f5f7fb);
  text-decoration: none;
  border: 1px solid var(--border, rgba(255, 255, 255, 0.12));
  border-radius: 999px;
  padding: 0.5rem 0.85rem;
  font-size: 0.88rem;
  background: rgba(255, 255, 255, 0.035);
}

.admin-nav a.router-link-exact-active {
  color: #06140d;
  border-color: var(--accent, #3dd68c);
  background: var(--accent, #3dd68c);
}

.layout-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

.card {
  padding: 1.15rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  gap: 0.8rem;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.endpoint {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  overflow: auto;
  background: rgba(61, 214, 140, 0.1);
  color: var(--accent, #3dd68c);
  border: 1px solid rgba(61, 214, 140, 0.24);
  border-radius: 999px;
  padding: 0.28rem 0.55rem;
  font-size: 0.76rem;
  white-space: nowrap;
}

form,
label {
  display: flex;
  flex-direction: column;
}

form {
  gap: 0.9rem;
}

label {
  gap: 0.38rem;
  color: var(--muted, #a7b0bf);
  font-size: 0.86rem;
  font-weight: 650;
}

.field-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.three-columns {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

input,
select,
textarea {
  width: 100%;
  min-height: 42px;
  padding: 0.64rem 0.72rem;
  border-radius: 12px;
  border: 1px solid var(--border, rgba(255, 255, 255, 0.12));
  background: rgba(7, 13, 20, 0.68);
  color: var(--text, #f5f7fb);
  outline: none;
}

textarea {
  resize: vertical;
  line-height: 1.45;
  font-family: inherit;
}

input:focus,
select:focus,
textarea:focus {
  border-color: var(--accent, #3dd68c);
  box-shadow: 0 0 0 3px rgba(61, 214, 140, 0.14);
}

.money-input {
  display: flex;
  align-items: center;
  border: 1px solid var(--border, rgba(255, 255, 255, 0.12));
  background: rgba(7, 13, 20, 0.68);
  border-radius: 12px;
  overflow: hidden;
}

.money-input span {
  padding-left: 0.72rem;
  color: var(--muted, #a7b0bf);
  font-weight: 800;
}

.money-input input {
  border: 0;
  background: transparent;
  box-shadow: none;
}

.highlight-card {
  border-color: rgba(61, 214, 140, 0.35);
}

.compact-card {
  min-height: 236px;
}

.optional,
.helper-text,
.profile-meta,
.response-action {
  color: var(--muted, #a7b0bf);
}

.optional {
  font-weight: 500;
  opacity: 0.8;
}

.helper-text {
  line-height: 1.5;
  margin: 0;
}

.btn {
  border: 1px solid transparent;
  border-radius: 13px;
  min-height: 44px;
  padding: 0.72rem 1rem;
  font-weight: 850;
  cursor: pointer;
  transition: transform 0.16s ease, opacity 0.16s ease, border-color 0.16s ease;
}

.btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn.primary {
  background: var(--accent, #3dd68c);
  color: #06140d;
}

.btn.secondary {
  background: rgba(255, 255, 255, 0.06);
  color: var(--text, #f5f7fb);
  border-color: var(--border, rgba(255, 255, 255, 0.12));
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.inline-result {
  margin-top: 0.95rem;
  padding: 0.9rem;
  border-radius: 16px;
  border: 1px solid var(--border, rgba(255, 255, 255, 0.12));
  background: rgba(255, 255, 255, 0.035);
}

.inline-result.success {
  border-color: rgba(61, 214, 140, 0.28);
  background: rgba(61, 214, 140, 0.065);
}

.inline-result.error {
  border-color: rgba(255, 107, 107, 0.35);
  background: rgba(255, 107, 107, 0.075);
}

.result-title {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  align-items: center;
  color: var(--text, #f5f7fb);
  font-weight: 850;
}

.result-title code {
  color: var(--danger, #ff6b6b);
  font-size: 0.75rem;
}

.result-message {
  margin: 0.4rem 0 0;
  color: var(--muted, #a7b0bf);
  line-height: 1.45;
}

.result-rows {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 0.55rem;
  margin: 0.75rem 0 0;
}

.result-rows div {
  border-radius: 12px;
  background: rgba(7, 13, 20, 0.45);
  padding: 0.6rem;
}

.result-rows dt {
  color: var(--muted, #a7b0bf);
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 0.2rem;
}

.result-rows dd {
  margin: 0;
  color: var(--text, #f5f7fb);
  font-weight: 750;
  overflow-wrap: anywhere;
}

.technical-details {
  margin-top: 0.75rem;
  color: var(--muted, #a7b0bf);
}

.technical-details summary {
  cursor: pointer;
  font-weight: 750;
}

.api-result {
  white-space: pre-wrap;
  word-break: break-word;
  background: rgba(7, 13, 20, 0.82);
  border: 1px solid var(--border, rgba(255, 255, 255, 0.12));
  border-radius: 14px;
  padding: 1rem;
  color: var(--text, #f5f7fb);
  font-size: 0.86rem;
  margin: 0.7rem 0 0;
}

.mock-provider-card {
  border-radius: 20px;
  padding: 1rem;
  color: #f8fafc;
  background: linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.14),
      rgba(255, 255, 255, 0.04)
    ),
    linear-gradient(135deg, #19283a, #0b111b 70%);
  border: 1px solid rgba(255, 255, 255, 0.15);
  min-height: 190px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.mock-card-topline,
.mock-card-badges,
.payment-preview,
.status-preview {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.mock-card-topline strong,
.mock-card-topline span,
.payment-preview strong,
.payment-preview span:not(.brand-chip),
.status-preview strong,
.status-preview span:not(.status-dot) {
  display: block;
}

.mock-card-topline span,
.payment-preview span:not(.brand-chip),
.status-preview span:not(.status-dot) {
  color: var(--muted, #a7b0bf);
  font-size: 0.85rem;
  margin-top: 0.12rem;
}

.mock-logo,
.brand-chip {
  display: inline-grid;
  place-items: center;
  flex: 0 0 auto;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: var(--accent, #3dd68c);
  color: #06140d;
  font-weight: 950;
  letter-spacing: -0.04em;
}

.mock-card-number {
  font-size: 1.2rem;
  font-weight: 850;
  letter-spacing: 0.08em;
}

.mock-card-badges {
  flex-wrap: wrap;
}

.mock-card-badges span {
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 999px;
  padding: 0.25rem 0.55rem;
  font-size: 0.75rem;
  color: #dbeafe;
}

.profile-default-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 0.8rem;
  align-items: center;
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 18px;
  border: 1px solid rgba(61, 214, 140, 0.2);
  background: rgba(61, 214, 140, 0.055);
}

.profile-default-card .profile-error {
  grid-column: 1/-1;
  margin-top: 0;
}

.profile-meta {
  margin: 0.3rem 0 0;
  line-height: 1.45;
}

.payment-preview,
.status-preview {
  padding: 0.85rem;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.035);
  border: 1px solid var(--border, rgba(255, 255, 255, 0.12));
}

.brand-chip.secondary {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text, #f5f7fb);
}

.status-dot {
  width: 13px;
  height: 13px;
  border-radius: 999px;
  background: var(--accent, #3dd68c);
  box-shadow: 0 0 0 6px rgba(61, 214, 140, 0.12);
}

.status-preview.failed .status-dot,
.status-preview.cancelled .status-dot {
  background: var(--danger, #ff6b6b);
  box-shadow: 0 0 0 6px rgba(255, 107, 107, 0.12);
}

.status-preview.pending .status-dot {
  background: #facc15;
  box-shadow: 0 0 0 6px rgba(250, 204, 21, 0.12);
}

@media (max-width: 900px) {
  .hero-card,
  .field-row,
  .three-columns,
  .profile-default-card {
    grid-template-columns: 1fr;
  }

  .card-header,
  .result-title {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
