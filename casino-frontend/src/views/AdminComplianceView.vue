<template>
  <main class="admin-page">
    <header class="hero-card compliance-hero">
      <div>
        <p class="eyebrow">Admin / Compliance</p>
        <h1>Compliance</h1>
        <p class="subtitle">
          Manage age verification, risk, self-exclusion, flags, limits, and
          eligibility with structured admin controls.
        </p>
      </div>

      <aside class="player-focus-card" aria-label="Selected player">
        <span class="shield-icon" aria-hidden="true">✓</span>
        <div>
          <p class="method-label">Selected player</p>
          <label>
            Player profile ID <span class="optional">optional override</span>
            <input
              v-model.number="selectedPlayerOverride"
              type="number"
              min="1"
              :placeholder="playerPlaceholder"
            />
          </label>
          <p class="profile-meta">
            Active player: #{{ activePlayerId || "—" }}
          </p>
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
          Compliance actions use this player unless an override is entered in
          the selected-player box.
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
            <p class="method-label">Profile</p>
            <h2>Create compliance profile</h2>
          </div>
          <code class="endpoint">POST /compliance</code>
        </header>
        <form @submit.prevent="runCreateProfile">
          <div class="field-row">
            <label>
              Player profile ID <span class="optional">optional override</span>
              <input
                v-model.number="createProfileForm.playerProfileId"
                type="number"
                min="1"
                :placeholder="playerPlaceholder"
              />
            </label>
            <label>
              Risk level
              <select v-model="createProfileForm.riskLevel">
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </label>
          </div>

          <div class="toggle-row">
            <label class="check-option">
              <input v-model="createProfileForm.ageVerified" type="checkbox" />
              <span>Age verified</span>
            </label>
            <label class="check-option danger-option">
              <input v-model="createProfileForm.selfExcluded" type="checkbox" />
              <span>Self-excluded</span>
            </label>
          </div>

          <button
            class="btn primary"
            type="submit"
            :disabled="
              createProfileState.busy ||
              !resolvedPlayerId(createProfileForm.playerProfileId)
            "
          >
            {{
              createProfileState.busy ? "Creating profile…" : "Create profile"
            }}
          </button>
        </form>
        <OperationFeedback :state="createProfileState" />
      </article>

      <article class="card form-card compact-card">
        <header class="card-header">
          <div>
            <p class="method-label">Profile</p>
            <h2>Load compliance profile</h2>
          </div>
          <code class="endpoint">GET /compliance/{playerProfileId}</code>
        </header>
        <form @submit.prevent="runGetProfile">
          <p class="helper-text">
            Uses active player #{{ activePlayerId || "—" }}.
          </p>
          <button
            class="btn secondary"
            type="submit"
            :disabled="getProfileState.busy || !activePlayerId"
          >
            {{ getProfileState.busy ? "Loading profile…" : "Load profile" }}
          </button>
        </form>
        <OperationFeedback :state="getProfileState" />
      </article>

      <article class="card form-card">
        <header class="card-header">
          <div>
            <p class="method-label">Profile</p>
            <h2>Update player compliance</h2>
          </div>
          <code class="endpoint">PATCH /compliance/{playerProfileId}</code>
        </header>
        <form @submit.prevent="runModifyProfile">
          <div class="field-row three-columns">
            <label>
              Risk level
              <select v-model="profilePatchForm.riskLevel">
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </label>
            <label class="check-option inline-check">
              <input v-model="profilePatchForm.ageVerified" type="checkbox" />
              <span>Age verified</span>
            </label>
            <label class="check-option inline-check danger-option">
              <input v-model="profilePatchForm.selfExcluded" type="checkbox" />
              <span>Self-excluded</span>
            </label>
          </div>

          <div
            class="risk-preview"
            :class="profilePatchForm.riskLevel.toLowerCase()"
          >
            <span class="risk-dot"></span>
            <div>
              <strong>{{ riskTitle(profilePatchForm.riskLevel) }}</strong>
              <span>
                Update compliance status for active player #{{
                  activePlayerId || "—"
                }}.
              </span>
            </div>
          </div>

          <button
            class="btn primary"
            type="submit"
            :disabled="modifyProfileState.busy || !activePlayerId"
          >
            {{
              modifyProfileState.busy
                ? "Saving changes…"
                : "Save compliance changes"
            }}
          </button>
        </form>
        <OperationFeedback :state="modifyProfileState" />
      </article>

      <article class="card form-card compact-card">
        <header class="card-header">
          <div>
            <p class="method-label">Eligibility</p>
            <h2>Check eligibility</h2>
          </div>
          <code class="endpoint">
            GET /compliance/{playerProfileId}/eligibility
          </code>
        </header>
        <form @submit.prevent="runEligibility">
          <p class="helper-text">
            Runs the service eligibility check for active player #{{
              activePlayerId || "—"
            }}.
          </p>
          <button
            class="btn secondary"
            type="submit"
            :disabled="eligibilityState.busy || !activePlayerId"
          >
            {{ eligibilityState.busy ? "Checking…" : "Check eligibility" }}
          </button>
        </form>
        <OperationFeedback :state="eligibilityState" />
      </article>

      <article class="card form-card">
        <header class="card-header">
          <div>
            <p class="method-label">Flags</p>
            <h2>Create flag</h2>
          </div>
          <code class="endpoint">POST /compliance/{playerProfileId}/flag</code>
        </header>
        <form @submit.prevent="runCreateFlag">
          <div class="field-row">
            <label>
              Flag type
              <select v-model="flagCreateForm.type">
                <option value="NO_AGE_VERIFICATION">No age verification</option>
                <option value="SELF_EXCLUSION">Self exclusion</option>
                <option value="MANUAL_RISK_LEVEL">Manual risk level</option>
              </select>
            </label>

            <label>
              Severity
              <select v-model="flagCreateForm.severity">
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="RESOLVED">RESOLVED</option>
                <option value="RESOLVED_ADMIN">RESOLVED ADMIN</option>
              </select>
            </label>
          </div>

          <button
            class="btn primary"
            type="submit"
            :disabled="createFlagState.busy || !activePlayerId"
          >
            {{ createFlagState.busy ? "Creating flag…" : "Create flag" }}
          </button>
        </form>
        <OperationFeedback :state="createFlagState" />
      </article>

      <article class="card form-card">
        <header class="card-header">
          <div>
            <p class="method-label">Flags</p>
            <h2>Resolve or edit flag</h2>
          </div>
          <code class="endpoint">
            PATCH /compliance/{playerProfileId}/flag/{flagId}
          </code>
        </header>
        <form @submit.prevent="runModifyFlag">
          <label>
            Flag ID
            <input
              v-model.number="flagPatchForm.flagId"
              type="number"
              min="1"
              placeholder="1"
              required
            />
          </label>

          <div class="field-row">
            <label>
              New flag type <span class="optional">optional</span>
              <select v-model="flagPatchForm.type">
                <option value="">No change</option>
                <option value="NO_AGE_VERIFICATION">No age verification</option>
                <option value="SELF_EXCLUSION">Self exclusion</option>
                <option value="MANUAL_RISK_LEVEL">Manual risk level</option>
              </select>
            </label>

            <label>
              New severity <span class="optional">optional</span>
              <select v-model="flagPatchForm.severity">
                <option value="">No change</option>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="RESOLVED">RESOLVED</option>
                <option value="RESOLVED ADMIN">RESOLVED ADMIN</option>
              </select>
            </label>
          </div>

          <label>
            Resolved date <span class="optional">optional</span>
            <input v-model="flagPatchForm.resolvedDate" type="datetime-local" />
          </label>

          <button
            class="btn primary"
            type="submit"
            :disabled="
              modifyFlagState.busy ||
              !activePlayerId ||
              !flagPatchForm.flagId ||
              !hasFlagPatchChanges
            "
          >
            {{ modifyFlagState.busy ? "Saving flag…" : "Save flag update" }}
          </button>
        </form>
        <OperationFeedback :state="modifyFlagState" />
      </article>

      <article class="card form-card">
        <header class="card-header">
          <div>
            <p class="method-label">Limits</p>
            <h2>Create gambling limit</h2>
          </div>
          <code class="endpoint"
            >POST /compliance/{playerProfileId}/limits</code
          >
        </header>
        <form @submit.prevent="runCreateLimit">
          <div class="field-row">
            <label>
              Limit type
              <select v-model="limitCreateForm.type">
                <option value="BET">Bet</option>
                <option value="WITHDRAWAL">Withdrawal</option>
              </select>
            </label>
            <label>
              Period
              <select v-model="limitCreateForm.period">
                <option value="DAILY">Daily</option>
                <option value="WEEKLY">Weekly</option>
                <option value="MONTHLY">Monthly</option>
              </select>
            </label>
          </div>

          <label>
            Amount
            <div class="money-input">
              <span>€</span>
              <input
                v-model.number="limitCreateForm.amount"
                type="number"
                min="1"
                step="1"
                required
              />
            </div>
          </label>

          <div class="field-row">
            <label>
              Start date
              <input
                v-model="limitCreateForm.startDate"
                type="datetime-local"
                :min="minimumDateTimeLocal"
                required
              />
            </label>

            <label>
              End date <span class="optional">optional</span>
              <input
                v-model="limitCreateForm.endDate"
                type="datetime-local"
                :min="minimumDateTimeLocal"
              />
            </label>
          </div>

          <button
            class="btn primary"
            type="submit"
            :disabled="
              createLimitState.busy ||
              !activePlayerId ||
              !limitCreateForm.amount ||
              !limitCreateForm.startDate
            "
          >
            {{ createLimitState.busy ? "Creating limit…" : "Create limit" }}
          </button>
        </form>
        <OperationFeedback :state="createLimitState" />
      </article>

      <article class="card form-card compact-card">
        <header class="card-header">
          <div>
            <p class="method-label">Limits</p>
            <h2>Load gambling limits</h2>
          </div>
          <code class="endpoint">GET /compliance/{playerProfileId}/limits</code>
        </header>
        <form @submit.prevent="runGetLimits">
          <p class="helper-text">
            Displays configured limits for active player #{{
              activePlayerId || "—"
            }}.
          </p>
          <button
            class="btn secondary"
            type="submit"
            :disabled="getLimitsState.busy || !activePlayerId"
          >
            {{ getLimitsState.busy ? "Loading limits…" : "Load limits" }}
          </button>
        </form>
        <OperationFeedback :state="getLimitsState" />
      </article>

      <article class="card form-card">
        <header class="card-header">
          <div>
            <p class="method-label">Limits</p>
            <h2>Update gambling limit</h2>
          </div>
          <code class="endpoint">
            PATCH /compliance/{playerProfileId}/limits/{limitId}
          </code>
        </header>
        <form @submit.prevent="runModifyLimit">
          <div class="field-row">
            <label>
              Limit ID
              <input
                v-model.number="limitPatchForm.limitId"
                type="number"
                min="1"
                placeholder="1"
                required
              />
            </label>

            <label>
              New amount <span class="optional">optional</span>
              <div class="money-input">
                <span>€</span>
                <input
                  v-model.number="limitPatchForm.amount"
                  type="number"
                  min="1"
                  step="1"
                />
              </div>
            </label>
          </div>

          <div class="field-row">
            <label>
              New type <span class="optional">optional</span>
              <select v-model="limitPatchForm.type">
                <option value="">No change</option>
                <option value="BET">Bet</option>
                <option value="WITHDRAWAL">Withdrawal</option>
              </select>
            </label>

            <label>
              New period <span class="optional">optional</span>
              <select v-model="limitPatchForm.period">
                <option value="">No change</option>
                <option value="DAILY">Daily</option>
                <option value="WEEKLY">Weekly</option>
                <option value="MONTHLY">Monthly</option>
              </select>
            </label>
          </div>

          <div class="field-row">
            <label>
              New start date <span class="optional">optional</span>
              <input
                v-model="limitPatchForm.startDate"
                type="datetime-local"
                :min="minimumDateTimeLocal"
              />
            </label>

            <label>
              New end date <span class="optional">optional</span>
              <input
                v-model="limitPatchForm.endDate"
                type="datetime-local"
                :min="minimumDateTimeLocal"
              />
            </label>
          </div>

          <label>
            Revoked date <span class="optional">optional</span>
            <input v-model="limitPatchForm.revokedDate" type="datetime-local" />
          </label>

          <button
            class="btn primary"
            type="submit"
            :disabled="
              modifyLimitState.busy ||
              !activePlayerId ||
              !limitPatchForm.limitId ||
              !hasLimitPatchChanges
            "
          >
            {{ modifyLimitState.busy ? "Saving limit…" : "Save limit update" }}
          </button>
        </form>
        <OperationFeedback :state="modifyLimitState" />
      </article>
    </section>
  </main>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref } from "vue";
import { getAccountIdFromToken } from "../auth.js";
import { fetchProfileByAccountId } from "../services/profileApi.js";
import {
  createComplianceProfile,
  fetchComplianceProfile,
  modifyComplianceProfile,
  fetchPlayerEligibility,
  createComplianceFlag,
  modifyComplianceFlag,
  createComplianceLimit,
  fetchComplianceLimits,
  modifyComplianceLimit,
} from "../services/complianceApi.js";

const complianceResultKeys = [
  "complianceProfileId",
  "complianceId",
  "id",
  "playerProfileId",
  "eligible",
  "allowed",
  "ageVerified",
  "selfExcluded",
  "riskLevel",
  "flagId",
  "limitId",
  "type",
  "severity",
  "resolvedDate",
  "period",
  "amount",
  "startDate",
  "endDate",
  "revokedDate",
  "status",
  "createdDate",
  "createdAt",
  "updatedAt",
];

const currentPlayerId = ref(null);
const selectedPlayerOverride = ref(null);
const currentProfile = ref(null);
const minimumDateTimeLocal = ref(futureDateTimeLocal(0));

const profileState = reactive(makeOperationState());
const createProfileState = reactive(makeOperationState());
const getProfileState = reactive(makeOperationState());
const modifyProfileState = reactive(makeOperationState());
const eligibilityState = reactive(makeOperationState());
const createFlagState = reactive(makeOperationState());
const modifyFlagState = reactive(makeOperationState());
const createLimitState = reactive(makeOperationState());
const getLimitsState = reactive(makeOperationState());
const modifyLimitState = reactive(makeOperationState());

const createProfileForm = reactive({
  playerProfileId: null,
  ageVerified: true,
  selfExcluded: false,
  riskLevel: "LOW",
});

const profilePatchForm = reactive({
  ageVerified: true,
  selfExcluded: false,
  riskLevel: "LOW",
});

const flagCreateForm = reactive({
  type: "AML_REVIEW",
  severity: "MEDIUM",
});

const flagPatchForm = reactive({
  flagId: null,
  type: "",
  severity: "",
  resolvedDate: "",
});

const limitCreateForm = reactive({
  type: "DEPOSIT",
  period: "DAILY",
  amount: 100,
  startDate: futureDateTimeLocal(5),
  endDate: "",
});

const limitPatchForm = reactive({
  limitId: null,
  type: "",
  period: "",
  amount: null,
  startDate: "",
  endDate: "",
  revokedDate: "",
});

const activePlayerId = computed(() =>
  resolvedPlayerId(selectedPlayerOverride.value)
);

const hasFlagPatchChanges = computed(() =>
  Boolean(
    flagPatchForm.type || flagPatchForm.severity || flagPatchForm.resolvedDate
  )
);

const hasLimitPatchChanges = computed(() =>
  Boolean(
    limitPatchForm.type ||
      limitPatchForm.period ||
      limitPatchForm.amount ||
      limitPatchForm.startDate ||
      limitPatchForm.endDate ||
      limitPatchForm.revokedDate
  )
);

const playerPlaceholder = computed(() =>
  currentPlayerId.value
    ? `Using current player #${currentPlayerId.value}`
    : "Enter player profile ID"
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
          renderRows(resultRows(state.result, complianceResultKeys)),
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
    code:
      error?.code || error?.payload?.code || error?.payload?.errorCode || "",
    data:
      error?.data ||
      error?.payload?.data ||
      error?.payload?.errors ||
      error?.payload?.fieldErrors ||
      error?.payload?.violations ||
      null,
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

function riskTitle(riskLevel) {
  return (
    {
      LOW: "Low risk profile",
      MEDIUM: "Medium risk profile",
      HIGH: "High risk profile",
    }[riskLevel] || riskLevel
  );
}

function compactPayload(payload) {
  return Object.fromEntries(
    Object.entries(payload).filter(
      ([, value]) => value !== null && value !== undefined && value !== ""
    )
  );
}

function futureDateTimeLocal(minutesAhead = 5) {
  const date = new Date(Date.now() + minutesAhead * 60_000);
  date.setSeconds(0, 0);

  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
  return local.toISOString().slice(0, 16);
}

function toOffsetDateTime(value) {
  if (!value) return null;

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return null;
  }

  const offsetMinutes = -date.getTimezoneOffset();
  const sign = offsetMinutes >= 0 ? "+" : "-";
  const absoluteOffset = Math.abs(offsetMinutes);
  const pad = (number) => String(number).padStart(2, "0");
  const offsetHours = pad(Math.floor(absoluteOffset / 60));
  const offsetMins = pad(absoluteOffset % 60);
  const normalizedValue =
    value.length === 16 ? `${value}:00` : value.slice(0, 19);

  return `${normalizedValue}${sign}${offsetHours}:${offsetMins}`;
}

function profileCreatePayload() {
  return {
    playerProfileId: resolvedPlayerId(createProfileForm.playerProfileId),
    ageVerified: Boolean(createProfileForm.ageVerified),
    selfExcluded: Boolean(createProfileForm.selfExcluded),
    riskLevel: createProfileForm.riskLevel,
  };
}

function profilePatchPayload() {
  return {
    ageVerified: Boolean(profilePatchForm.ageVerified),
    selfExcluded: Boolean(profilePatchForm.selfExcluded),
    riskLevel: profilePatchForm.riskLevel,
  };
}

function flagCreatePayload() {
  return {
    type: flagCreateForm.type,
    severity: flagCreateForm.severity,
  };
}

function flagPatchPayload() {
  return compactPayload({
    type: flagPatchForm.type,
    severity: flagPatchForm.severity,
    resolvedDate: toOffsetDateTime(flagPatchForm.resolvedDate),
  });
}

function limitCreatePayload() {
  return {
    type: limitCreateForm.type,
    period: limitCreateForm.period,
    amount: Number.parseInt(limitCreateForm.amount, 10),
    startDate: toOffsetDateTime(limitCreateForm.startDate),
    endDate: toOffsetDateTime(limitCreateForm.endDate),
  };
}

function limitPatchPayload() {
  return compactPayload({
    type: limitPatchForm.type,
    period: limitPatchForm.period,
    amount:
      limitPatchForm.amount == null || limitPatchForm.amount === ""
        ? null
        : Number.parseInt(limitPatchForm.amount, 10),
    startDate: toOffsetDateTime(limitPatchForm.startDate),
    endDate: toOffsetDateTime(limitPatchForm.endDate),
    revokedDate: toOffsetDateTime(limitPatchForm.revokedDate),
  });
}

async function loadCurrentPlayerDefaults() {
  await runAction(profileState, {
    successTitle: "Current player loaded",
    failureTitle: "Could not load current player",
    actionLabel: "Loaded player profile for default compliance actions.",
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

function runCreateProfile() {
  return runAction(createProfileState, {
    successTitle: "Compliance profile created",
    failureTitle: "Create profile failed",
    actionLabel: `Created compliance profile for player #${resolvedPlayerId(
      createProfileForm.playerProfileId
    )}.`,
    action: () => createComplianceProfile(profileCreatePayload()),
  });
}

function runGetProfile() {
  return runAction(getProfileState, {
    successTitle: "Compliance profile loaded",
    failureTitle: "Load profile failed",
    actionLabel: `Loaded compliance profile for player #${activePlayerId.value}.`,
    action: () => fetchComplianceProfile(activePlayerId.value),
  });
}

function runModifyProfile() {
  return runAction(modifyProfileState, {
    successTitle: "Compliance profile updated",
    failureTitle: "Update profile failed",
    actionLabel: `Updated compliance profile for player #${activePlayerId.value}.`,
    action: () =>
      modifyComplianceProfile(activePlayerId.value, profilePatchPayload()),
  });
}

function runEligibility() {
  return runAction(eligibilityState, {
    successTitle: "Eligibility checked",
    failureTitle: "Eligibility check failed",
    actionLabel: `Checked eligibility for player #${activePlayerId.value}.`,
    action: () => fetchPlayerEligibility(activePlayerId.value),
  });
}

function runCreateFlag() {
  return runAction(createFlagState, {
    successTitle: "Compliance flag created",
    failureTitle: "Create flag failed",
    actionLabel: `Created ${flagCreateForm.severity.toLowerCase()} compliance flag for player #${
      activePlayerId.value
    }.`,
    action: () =>
      createComplianceFlag(activePlayerId.value, flagCreatePayload()),
  });
}

function runModifyFlag() {
  return runAction(modifyFlagState, {
    successTitle: "Compliance flag updated",
    failureTitle: "Update flag failed",
    actionLabel: `Updated flag #${flagPatchForm.flagId} for player #${activePlayerId.value}.`,
    action: () =>
      modifyComplianceFlag(
        activePlayerId.value,
        flagPatchForm.flagId,
        flagPatchPayload()
      ),
  });
}

function runCreateLimit() {
  return runAction(createLimitState, {
    successTitle: "Compliance limit created",
    failureTitle: "Create limit failed",
    actionLabel: `Created ${limitCreateForm.period.toLowerCase()} ${limitCreateForm.type.toLowerCase()} limit for player #${
      activePlayerId.value
    }.`,
    action: () =>
      createComplianceLimit(activePlayerId.value, limitCreatePayload()),
  });
}

function runGetLimits() {
  return runAction(getLimitsState, {
    successTitle: "Compliance limits loaded",
    failureTitle: "Load limits failed",
    actionLabel: `Loaded limits for player #${activePlayerId.value}.`,
    action: () => fetchComplianceLimits(activePlayerId.value),
  });
}

function runModifyLimit() {
  return runAction(modifyLimitState, {
    successTitle: "Compliance limit updated",
    failureTitle: "Update limit failed",
    actionLabel: `Updated limit #${limitPatchForm.limitId} for player #${activePlayerId.value}.`,
    action: () =>
      modifyComplianceLimit(
        activePlayerId.value,
        limitPatchForm.limitId,
        limitPatchPayload()
      ),
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

.player-focus-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 0.8rem;
  align-items: center;
  border-radius: 20px;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.shield-icon {
  display: inline-grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: var(--accent, #3dd68c);
  color: #06140d;
  font-weight: 950;
  font-size: 1.5rem;
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

.toggle-row {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.check-option {
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 0.8rem;
  border: 1px solid var(--border, rgba(255, 255, 255, 0.12));
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.035);
  color: var(--text, #f5f7fb);
}

.check-option input {
  width: 18px;
  height: 18px;
  min-height: auto;
  accent-color: var(--accent, #3dd68c);
}

.inline-check {
  justify-content: center;
  min-height: 42px;
}

.danger-option input {
  accent-color: var(--danger, #ff6b6b);
}

.risk-preview {
  display: flex;
  gap: 0.8rem;
  align-items: center;
  padding: 0.85rem;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.035);
  border: 1px solid var(--border, rgba(255, 255, 255, 0.12));
}

.risk-preview strong,
.risk-preview span:not(.risk-dot) {
  display: block;
}

.risk-preview span:not(.risk-dot) {
  color: var(--muted, #a7b0bf);
  font-size: 0.85rem;
  margin-top: 0.12rem;
}

.risk-dot {
  width: 13px;
  height: 13px;
  border-radius: 999px;
  background: var(--accent, #3dd68c);
  box-shadow: 0 0 0 6px rgba(61, 214, 140, 0.12);
}

.risk-preview.medium .risk-dot {
  background: #facc15;
  box-shadow: 0 0 0 6px rgba(250, 204, 21, 0.12);
}

.risk-preview.high .risk-dot {
  background: var(--danger, #ff6b6b);
  box-shadow: 0 0 0 6px rgba(255, 107, 107, 0.12);
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
