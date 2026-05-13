<template>
  <div class="admin-container">
    <div class="card">
      <h1>Admin</h1>

      <nav class="tabs" role="tablist">
        <button
          type="button"
          class="tab"
          :class="{ active: activeTab === 'accounts' }"
          role="tab"
          :aria-selected="activeTab === 'accounts'"
          @click="activeTab = 'accounts'"
        >
          Accounts
        </button>
        <button
          type="button"
          class="tab"
          :class="{ active: activeTab === 'profiles' }"
          role="tab"
          :aria-selected="activeTab === 'profiles'"
          @click="activeTab = 'profiles'"
        >
          Profiles
        </button>
      </nav>

      <!-- Accounts tab --------------------------------------------------------- -->
      <section v-if="activeTab === 'accounts'" class="tab-panel" role="tabpanel">
        <div class="api-block">
          <header>
            <h2>Load account</h2>
            <h2>for example accountId: 4dd56047-4754-4b95-9e72-717792f60a7a</h2>
            <code class="endpoint">GET /auth/accounts/{accountId}</code>
          </header>

          <div class="row">
            <input
              v-model="accountLookupId"
              type="text"
              placeholder="accountId (UUID)"
            />
            <button
              type="button"
              class="btn primary"
              :disabled="accountBusy || !accountLookupId"
              @click="loadAccountById"
            >
              {{ accountBusy ? 'Loading…' : 'Load' }}
            </button>
          </div>
          <p v-if="accountError" class="error compact">{{ accountError }}</p>
        </div>

        <div v-if="account" class="api-block">
          <header>
            <h2>Account</h2>
            <span
              class="status-badge"
              :class="account.status === 'ACTIVE' ? 'ok' : 'danger'"
            >
              {{ account.status }}
            </span>
          </header>

          <dl class="kv">
            <dt>Account ID</dt>
            <dd><code>{{ account.accountId }}</code></dd>

            <dt>Username</dt>
            <dd>{{ account.username }}</dd>

            <dt>Created</dt>
            <dd>{{ formatDate(account.createdDate) }}</dd>

            <dt>Roles</dt>
            <dd>{{ (account.roles || []).join(', ') || '—' }}</dd>
          </dl>

          <div class="api-sub">
            <header>
              <h3>Update account status</h3>
              <code class="endpoint">PATCH /auth/accounts/{accountId}/status</code>
            </header>

            <div class="row">
              <button
                type="button"
                class="btn primary"
                :disabled="statusBusy || account.status === 'SUSPENDED'"
                @click="changeStatus('SUSPENDED')"
              >
                {{ statusBusy && statusTarget === 'SUSPENDED' ? 'Suspending…' : 'Suspend' }}
              </button>
              <button
                type="button"
                class="btn ghost"
                :disabled="statusBusy || account.status === 'ACTIVE'"
                @click="changeStatus('ACTIVE')"
              >
                {{ statusBusy && statusTarget === 'ACTIVE' ? 'Reactivating…' : 'Reactivate' }}
              </button>
            </div>

            <p v-if="statusError" class="error compact">{{ statusError }}</p>
            <p v-if="statusSuccess" class="success compact">{{ statusSuccess }}</p>
          </div>
        </div>
      </section>

      <!-- Profiles tab --------------------------------------------------------- -->
      <section v-else class="tab-panel" role="tabpanel">
        <div class="api-block">
          <header>
            <h2>Load profile</h2>
            <code class="endpoint">GET /profiles/{playerProfileId}</code>
          </header>

          <div class="row">
            <input
              v-model.number="lookupId"
              type="number"
              min="1"
              placeholder="playerProfileId"
            />
            <button
              type="button"
              class="btn primary"
              :disabled="lookupBusy || !lookupId"
              @click="loadProfileById"
            >
              {{ lookupBusy ? 'Loading…' : 'Load' }}
            </button>
          </div>
          <p v-if="lookupError" class="error compact">{{ lookupError }}</p>
        </div>

        <div v-if="target" class="api-block">
          <header>
            <h2>Full update</h2>
            <code class="endpoint">PUT /profiles/{playerProfileId}</code>
          </header>
          <p class="note">
            Editing profile <strong>#{{ target.playerProfileId }}</strong>
            (account <code>{{ target.accountId }}</code>). All fields are sent.
          </p>

          <form class="form" @submit.prevent="submitFullUpdate">
            <label>
              Full name
              <input v-model="fullForm.fullName" type="text" required />
            </label>
            <label>
              Date of birth
              <input v-model="fullForm.dateOfBirth" type="date" required />
            </label>
            <label>
              Status
              <select v-model="fullForm.status" required>
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </label>
            <label>
              Email
              <input v-model="fullForm.email" type="email" />
            </label>
            <label>
              Phone
              <input v-model="fullForm.phone" type="tel" />
            </label>
            <label>
              Address
              <input v-model="fullForm.address" type="text" />
            </label>
            <label>
              Language
              <input v-model="fullForm.language" type="text" maxlength="8" />
            </label>
            <label>
              Currency
              <input v-model="fullForm.currency" type="text" maxlength="8" />
            </label>

            <p v-if="fullError" class="error compact">{{ fullError }}</p>
            <p v-if="fullSuccess" class="success compact">{{ fullSuccess }}</p>

            <button class="btn primary" type="submit" :disabled="fullBusy">
              {{ fullBusy ? 'Saving…' : 'PUT update' }}
            </button>
          </form>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import {
  fetchProfileById,
  updateProfile,
} from '../services/profileApi.js';
import {
  fetchAccount,
  updateAccountStatus,
} from '../services/authApi.js';

const activeTab = ref('accounts');

// --- Accounts tab state ------------------------------------------------------
const accountLookupId = ref('');
const accountBusy = ref(false);
const accountError = ref('');
const account = ref(null);

const statusBusy = ref(false);
const statusTarget = ref('');
const statusError = ref('');
const statusSuccess = ref('');

function formatDate(value) {
  if (!value) return '—';
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? value : d.toLocaleString();
}

async function loadAccountById() {
  if (!accountLookupId.value) return;
  accountError.value = '';
  statusError.value = '';
  statusSuccess.value = '';
  accountBusy.value = true;
  try {
    account.value = await fetchAccount(accountLookupId.value.trim());
  } catch (e) {
    account.value = null;
    accountError.value = e.message || 'Could not load account';
  } finally {
    accountBusy.value = false;
  }
}

async function changeStatus(nextStatus) {
  if (!account.value) return;
  statusError.value = '';
  statusSuccess.value = '';
  statusTarget.value = nextStatus;
  statusBusy.value = true;
  try {
    account.value = await updateAccountStatus(account.value.accountId, nextStatus);
    statusSuccess.value = `Account ${nextStatus === 'SUSPENDED' ? 'suspended' : 'reactivated'}.`;
  } catch (e) {
    statusError.value = e.message || 'Update failed';
  } finally {
    statusBusy.value = false;
    statusTarget.value = '';
  }
}

const lookupId = ref(null);
const lookupBusy = ref(false);
const lookupError = ref('');

const target = ref(null);

const fullForm = reactive({
  fullName: '',
  dateOfBirth: '',
  status: 'ACTIVE',
  email: '',
  phone: '',
  address: '',
  language: '',
  currency: '',
});
const fullBusy = ref(false);
const fullError = ref('');
const fullSuccess = ref('');

function applyTarget(loaded) {
  target.value = loaded;
  fullForm.fullName = loaded.fullName ?? '';
  fullForm.dateOfBirth = loaded.dateOfBirth ?? '';
  fullForm.status = loaded.status ?? 'ACTIVE';
  fullForm.email = loaded.email ?? '';
  fullForm.phone = loaded.phone ?? '';
  fullForm.address = loaded.address ?? '';
  fullForm.language = loaded.language ?? '';
  fullForm.currency = loaded.currency ?? '';
  fullError.value = '';
  fullSuccess.value = '';
}

async function loadProfileById() {
  if (!lookupId.value) return;
  lookupError.value = '';
  lookupBusy.value = true;
  try {
    const loaded = await fetchProfileById(lookupId.value);
    applyTarget(loaded);
  } catch (e) {
    target.value = null;
    lookupError.value = e.message || 'Could not load profile';
  } finally {
    lookupBusy.value = false;
  }
}

async function submitFullUpdate() {
  if (!target.value) return;
  fullError.value = '';
  fullSuccess.value = '';
  fullBusy.value = true;
  try {
    await updateProfile(target.value.playerProfileId, {
      accountId: target.value.accountId,
      fullName: fullForm.fullName,
      dateOfBirth: fullForm.dateOfBirth,
      status: fullForm.status,
      email: fullForm.email || null,
      phone: fullForm.phone || null,
      address: fullForm.address || null,
      language: fullForm.language || null,
      currency: fullForm.currency || null,
    });
    const fresh = await fetchProfileById(target.value.playerProfileId);
    applyTarget(fresh);
    fullSuccess.value = 'Profile updated.';
  } catch (e) {
    fullError.value = e.message || 'Update failed';
  } finally {
    fullBusy.value = false;
  }
}
</script>

<style scoped>
.admin-container {
  max-width: 820px;
  margin: 0 auto;
  padding: 1.25rem;
}

.card {
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 1.75rem;
}

h1 {
  margin: 0 0 1rem;
  font-size: 1.5rem;
}

h2 {
  margin: 0;
  font-size: 1.05rem;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  border-bottom: 1px solid var(--border);
  margin-bottom: 1rem;
}

.tab {
  background: transparent;
  color: var(--muted);
  border: none;
  border-bottom: 2px solid transparent;
  padding: 0.5rem 0.85rem;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.95rem;
}

.tab.active {
  color: var(--accent);
  border-bottom-color: var(--accent);
}

.tab-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.placeholder {
  color: var(--muted);
  text-align: center;
  padding: 2rem 0;
  margin: 0;
}

.api-block {
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 1rem 1.25rem 1.25rem;
  background: #121a24;
}

.api-block header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.endpoint,
.note code {
  background: #0f1620;
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  font-size: 0.8rem;
  color: var(--accent);
}

.note {
  margin: 0 0 1rem;
  color: var(--muted);
  font-size: 0.85rem;
}

.row {
  display: flex;
  gap: 0.6rem;
  align-items: center;
}

.row input {
  flex: 1;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

label {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.85rem;
  color: var(--muted);
}

input,
select {
  padding: 0.55rem 0.65rem;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: #0f1620;
  color: var(--text);
}

.error,
.success {
  margin: 0;
  font-size: 0.9rem;
}

.error {
  color: var(--danger);
}

.success {
  color: var(--accent);
}

.compact {
  margin: 0.25rem 0 0;
}

.kv {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 0.5rem 1rem;
  margin: 0 0 1rem;
  font-size: 0.9rem;
}

.kv dt {
  color: var(--muted);
}

.kv dd {
  margin: 0;
  color: var(--text);
  word-break: break-all;
}

.kv code {
  font-family: 'Courier New', Courier, monospace;
  font-size: 0.8rem;
  color: var(--muted);
}

.status-badge {
  padding: 0.15rem 0.6rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  border: 1px solid;
}

.status-badge.ok {
  color: var(--accent);
  border-color: var(--accent);
  background: rgba(61, 214, 140, 0.1);
}

.status-badge.danger {
  color: var(--danger);
  border-color: var(--danger);
  background: rgba(232, 93, 111, 0.1);
}

.api-sub {
  border-top: 1px solid var(--border);
  padding-top: 0.85rem;
  margin-top: 0.5rem;
}

.api-sub header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.6rem;
  flex-wrap: wrap;
}

.api-sub h3 {
  margin: 0;
  font-size: 0.95rem;
}
</style>
