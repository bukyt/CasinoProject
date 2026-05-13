<template>
  <div class="settings-container">
    <div class="card">
      <h1>User settings</h1>

      <div v-if="loadError" class="error">
        <p><strong>Could not load your profile:</strong> {{ loadError }}</p>
        <button type="button" class="btn primary" @click="loadProfile">Try again</button>
      </div>

      <template v-else-if="profile">
        <p class="lead">
          Editing profile <strong>#{{ profile.playerProfileId }}</strong>
          (account <code>{{ profile.accountId }}</code>)
        </p>

        <!-- PATCH /profiles/{playerProfileId}/contact -------------------------------- -->
        <section class="api-block">
          <header>
            <h2>Update contact details</h2>
            <code class="endpoint">PATCH /profiles/{playerProfileId}/contact</code>
          </header>
          <p class="note">Empty fields are ignored — only filled values are sent.</p>

          <form class="form" @submit.prevent="submitContactPatch">
            <label>
              Email
              <input v-model="contactForm.email" type="email" />
            </label>
            <label>
              Phone
              <input v-model="contactForm.phone" type="tel" />
            </label>
            <label>
              Address
              <input v-model="contactForm.address" type="text" />
            </label>

            <p v-if="contactError" class="error compact">{{ contactError }}</p>
            <p v-if="contactSuccess" class="success compact">{{ contactSuccess }}</p>

            <button class="btn primary" type="submit" :disabled="contactBusy">
              {{ contactBusy ? 'Saving…' : 'PATCH contact' }}
            </button>
          </form>
        </section>

        <!-- PATCH /profiles/{playerProfileId}/preferences ---------------------------- -->
        <section class="api-block">
          <header>
            <h2>Update preferences</h2>
            <code class="endpoint">PATCH /profiles/{playerProfileId}/preferences</code>
          </header>
          <p class="note">Empty fields are ignored — only filled values are sent.</p>

          <form class="form" @submit.prevent="submitPreferencesPatch">
            <label>
              Language
              <input v-model="prefsForm.language" type="text" maxlength="8" />
            </label>
            <label>
              Currency
              <input v-model="prefsForm.currency" type="text" maxlength="8" />
            </label>

            <p v-if="prefsError" class="error compact">{{ prefsError }}</p>
            <p v-if="prefsSuccess" class="success compact">{{ prefsSuccess }}</p>

            <button class="btn primary" type="submit" :disabled="prefsBusy">
              {{ prefsBusy ? 'Saving…' : 'PATCH preferences' }}
            </button>
          </form>
        </section>
      </template>

      <div v-else class="loading-state">
        <p>Loading your profile…</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { getAccountIdFromToken } from '../auth.js';
import {
  fetchProfileByAccountId,
  fetchProfileById,
  updateContactDetails,
  updatePreferences,
} from '../services/profileApi.js';

const profile = ref(null);
const loadError = ref('');

const contactForm = reactive({ email: '', phone: '', address: '' });
const contactBusy = ref(false);
const contactError = ref('');
const contactSuccess = ref('');

const prefsForm = reactive({ language: '', currency: '' });
const prefsBusy = ref(false);
const prefsError = ref('');
const prefsSuccess = ref('');

function applyProfile(loaded) {
  profile.value = loaded;
  contactForm.email = loaded.email || '';
  contactForm.phone = loaded.phone || '';
  contactForm.address = loaded.address || '';
  prefsForm.language = loaded.language || '';
  prefsForm.currency = loaded.currency || '';
}

async function loadProfile() {
  loadError.value = '';
  const accountId = getAccountIdFromToken();
  if (!accountId) {
    loadError.value = 'Not logged in.';
    return;
  }
  try {
    const loaded = await fetchProfileByAccountId(accountId);
    if (!loaded) {
      loadError.value = 'No profile linked to your account yet.';
      return;
    }
    applyProfile(loaded);
  } catch (e) {
    loadError.value = e.message || 'Could not load profile';
  }
}

async function reloadCurrentProfile() {
  if (!profile.value) return;
  const fresh = await fetchProfileById(profile.value.playerProfileId);
  applyProfile(fresh);
}

async function submitContactPatch() {
  if (!profile.value) return;
  contactError.value = '';
  contactSuccess.value = '';

  const body = {};
  if (contactForm.email) body.email = contactForm.email;
  if (contactForm.phone) body.phone = contactForm.phone;
  if (contactForm.address) body.address = contactForm.address;

  if (Object.keys(body).length === 0) {
    contactError.value = 'Fill at least one field to PATCH.';
    return;
  }

  contactBusy.value = true;
  try {
    await updateContactDetails(profile.value.playerProfileId, body);
    await reloadCurrentProfile();
    contactSuccess.value = 'Contact details updated.';
  } catch (e) {
    contactError.value = e.message || 'Update failed';
  } finally {
    contactBusy.value = false;
  }
}

async function submitPreferencesPatch() {
  if (!profile.value) return;
  prefsError.value = '';
  prefsSuccess.value = '';

  const body = {};
  if (prefsForm.language) body.language = prefsForm.language;
  if (prefsForm.currency) body.currency = prefsForm.currency;

  if (Object.keys(body).length === 0) {
    prefsError.value = 'Fill at least one field to PATCH.';
    return;
  }

  prefsBusy.value = true;
  try {
    await updatePreferences(profile.value.playerProfileId, body);
    await reloadCurrentProfile();
    prefsSuccess.value = 'Preferences updated.';
  } catch (e) {
    prefsError.value = e.message || 'Update failed';
  } finally {
    prefsBusy.value = false;
  }
}

onMounted(loadProfile);
</script>

<style scoped>
.settings-container {
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

.lead {
  margin: 0 0 1rem;
  color: var(--muted);
  font-size: 0.9rem;
}

.lead code,
.endpoint {
  background: #121a24;
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  font-size: 0.8rem;
  color: var(--accent);
}

.api-block {
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 1rem 1.25rem 1.25rem;
  margin-top: 1.25rem;
  background: #121a24;
}

.api-block header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.35rem;
  flex-wrap: wrap;
}

.note {
  margin: 0 0 1rem;
  color: var(--muted);
  font-size: 0.85rem;
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

.loading-state {
  text-align: center;
  padding: 2rem;
  color: var(--muted);
}
</style>
