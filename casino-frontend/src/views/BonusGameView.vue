<template>
  <div class="bonus-game">

    <!-- ══════════════════════════════════════════
         PHASE 1 — LOBBY (no active session)
    ══════════════════════════════════════════ -->
    <template v-if="!session">

      <div class="lobby-header">
        <h1 class="page-title">🎰 Bonus Free Roll</h1>
        <p class="page-sub">Use your bonus credits to play the slot machine</p>
      </div>

      <div class="credits-card">
        <span class="credits-label">YOUR BONUS CREDITS</span>
        <span class="credits-amount">{{ credits }}</span>
        <span class="credits-sub">
          {{ freeRollsAvailable }} free roll{{ freeRollsAvailable !== 1 ? 's' : '' }} available
          <span class="credits-hint">(10 credits = 1 roll)</span>
        </span>
      </div>

      <p v-if="loadError" class="msg error">⚠ {{ loadError }}</p>
      <p v-if="loading"   class="msg muted">Loading credits…</p>

      <div v-if="!loading" class="lobby-form">
        <label class="field-label">Starting balance for this session</label>
        <input
          v-model.number="startBalance"
          type="number"
          min="10"
          :max="credits || 100"
          step="10"
          class="field-input"
          :disabled="credits <= 0"
        />
        <p class="field-hint">
          Each bet will cost the amount you choose below.<br>
          Session ends when you run out or click "Quit".
        </p>
        <button
          class="btn primary large"
          :disabled="credits <= 0 || starting"
          @click="startSession"
        >
          {{ starting ? 'Starting…' : '🎰 Start Session' }}
        </button>
        <p v-if="credits <= 0" class="msg muted no-credits-hint">
          No bonus credits yet. Place bets in real games — every 50 wagered gives 10 credits.
        </p>
        <p v-if="startError" class="msg error">⚠ {{ startError }}</p>
      </div>

    </template>

    <!-- ══════════════════════════════════════════
         PHASE 2 — ACTIVE GAME SESSION
    ══════════════════════════════════════════ -->
    <template v-else>

      <!-- Top bar: session info + quit -->
      <div class="session-bar">
        <div class="session-info">
          <span class="session-label">SESSION</span>
          <span class="session-id">{{ session.id }}</span>
        </div>
        <button class="btn danger" :disabled="closing" @click="quitSession">
          {{ closing ? 'Closing…' : '🚪 Quit' }}
        </button>
      </div>

      <!-- Balance display -->
      <div class="balance-display" :class="{ flash: balanceFlashed }">
        <span class="balance-label">BALANCE</span>
        <span class="balance-value">{{ balance.toFixed(2) }}</span>
      </div>

      <!-- Slot reels -->
      <div class="machine">
        <div class="machine-rail"></div>
        <div class="reels-wrapper">
          <div
            v-for="(sym, i) in reels"
            :key="i"
            class="reel"
            :class="{ spinning: betting }"
            :style="{ animationDelay: `${i * 55}ms` }"
          >
            <span class="reel-symbol" :class="{ landed: !betting && lastBet }">{{ sym }}</span>
          </div>
        </div>
        <div class="machine-rail"></div>

        <!-- Result pill -->
        <transition name="pill-fade">
          <div v-if="lastBet && !betting" class="result-pill" :class="lastBet.outcome === 'WIN' ? 'win' : 'lose'">
            <span v-if="lastBet.outcome === 'WIN'">🎉 WIN  +{{ (lastBet.payout - lastBet.amount).toFixed(2) }}</span>
            <span v-else>😔 LOSE  −{{ lastBet.amount.toFixed(2) }}</span>
          </div>
        </transition>
      </div>

      <!-- Win burst -->
      <div v-if="showParticles" class="particles" aria-hidden="true">
        <span v-for="n in 20" :key="n" class="particle" :style="particleStyle(n)">★</span>
      </div>

      <!-- Bet controls -->
      <div class="bet-controls">
        <label class="field-label">Bet amount</label>
        <div class="bet-row">
          <button class="chip" @click="betAmount = Math.max(1, betAmount - 5)" :disabled="betting">−5</button>
          <input
            v-model.number="betAmount"
            type="number"
            min="1"
            :max="balance"
            class="field-input bet-input"
            :disabled="betting"
          />
          <button class="chip" @click="betAmount = Math.min(balance, betAmount + 5)" :disabled="betting">+5</button>
        </div>

        <button
          class="btn primary large"
          :disabled="betting || balance <= 0 || betAmount <= 0 || betAmount > balance"
          @click="placeBetClick"
        >
          {{ betting ? 'Spinning…' : '🎲 Place Bet' }}
        </button>

        <p v-if="betError" class="msg error">⚠ {{ betError }}</p>

        <p v-if="balance <= 0" class="msg muted">
          Balance is 0 — click Quit to end the session.
        </p>
      </div>

      <!-- Bet history -->
      <div v-if="betHistory.length" class="history">
        <h3 class="history-title">Bet history</h3>
        <table class="history-table">
          <thead>
            <tr><th>#</th><th>Bet</th><th>Outcome</th><th>Payout</th><th>Balance after</th></tr>
          </thead>
          <tbody>
            <tr
              v-for="(h, i) in betHistory"
              :key="i"
              :class="h.outcome === 'WIN' ? 'row-win' : 'row-lose'"
            >
              <td>{{ betHistory.length - i }}</td>
              <td>{{ h.amount.toFixed(2) }}</td>
              <td>{{ h.outcome }}</td>
              <td>{{ h.payout.toFixed(2) }}</td>
              <td>{{ h.balanceAfter.toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

    </template>

    <!-- Session closed banner -->
    <div v-if="closedSession" class="closed-banner">
      <p>Session <strong>{{ closedSession.id }}</strong> closed.</p>
      <p>Final balance: <strong>{{ closedSession.balance.toFixed(2) }}</strong></p>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getAccountIdFromToken } from '../auth.js';
import { fetchProfileByAccountId } from '../services/profileApi.js';
import { fetchPlayerCredits } from '../services/bonusApi.js';
import { createGameSession, placeBet, closeGameSession } from '../services/gameApi.js';

// ─── Symbols ──────────────────────────────────────────────────────────────────
const ALL     = ['🍒', '🍋', '🍊', '⭐', '💎', '🎰'];
const WINNERS = ['🍒', '🍋', '🍊', '⭐', '💎', '🎰'];
const LOSERS  = ['🍒', '🍋', '🍊', '⭐', '💎'];

const rand  = (arr) => arr[Math.floor(Math.random() * arr.length)];
const winReels  = () => { const s = rand(WINNERS); return [s, s, s]; };
const loseReels = () => {
  let a, b, c;
  do { a = rand(LOSERS); b = rand(LOSERS); c = rand(LOSERS); } while (a === b && b === c);
  return [a, b, c];
};

// ─── State ────────────────────────────────────────────────────────────────────
const loading       = ref(true);
const loadError     = ref('');
const credits       = ref(0);
const startBalance  = ref(50);
const starting      = ref(false);
const startError    = ref('');
const session       = ref(null);   // active GameSession
const balance       = ref(0);
const betAmount     = ref(10);
const betting       = ref(false);
const betError      = ref('');
const lastBet       = ref(null);
const betHistory    = ref([]);
const reels         = ref(['🎰', '🎰', '🎰']);
const closing       = ref(false);
const closedSession = ref(null);
const showParticles = ref(false);
const balanceFlashed = ref(false);

let playerProfileId = null;

const freeRollsAvailable = computed(() => Math.floor(credits.value / 10));

// ─── Load credits ─────────────────────────────────────────────────────────────
onMounted(async () => {
  try {
    const accountId = getAccountIdFromToken();
    if (!accountId) throw new Error('Not authenticated');

    const profile = await fetchProfileByAccountId(accountId);
    if (!profile) throw new Error('Complete your profile first');

    // parseInt to ensure no float like 1.0 is sent as path variable
    playerProfileId = parseInt(profile.playerProfileId, 10);

    try {
      credits.value = await fetchPlayerCredits(playerProfileId);
      startBalance.value = Math.max(10, credits.value);
    } catch {
      // Bonus service may return 400 if player has no record yet — treat as 0
      credits.value = 0;
      loadError.value = 'Could not fetch bonus credits (no bets placed yet?). You can still start a demo session.';
    }
  } catch (e) {
    loadError.value = e.message || 'Could not load profile';
  } finally {
    loading.value = false;
  }
});

// ─── Start session ────────────────────────────────────────────────────────────
async function startSession() {
  startError.value = '';
  starting.value   = true;
  closedSession.value = null;
  try {
    const s = await createGameSession('slot-machine', startBalance.value, playerProfileId);
    session.value  = s;
    balance.value  = s.balance;
    betAmount.value = Math.min(10, s.balance);
    betHistory.value = [];
    lastBet.value  = null;
    reels.value    = ['🎰', '🎰', '🎰'];
  } catch (e) {
    startError.value = e.message || 'Failed to create session. Is the game service running on port 8082?';
  } finally {
    starting.value = false;
  }
}

// ─── Place bet ────────────────────────────────────────────────────────────────
async function placeBetClick() {
  betError.value      = '';
  showParticles.value = false;
  betting.value       = true;
  lastBet.value       = null;

  // Animate reels while request is in flight
  const anim = setInterval(() => { reels.value = [rand(ALL), rand(ALL), rand(ALL)]; }, 90);

  try {
    const bet = await placeBet(session.value.id, betAmount.value);
    clearInterval(anim);

    const isWin   = bet.outcome === 'WIN';
    reels.value   = isWin ? winReels() : loseReels();
    lastBet.value = bet;

    // Update balance locally (game service: win → +amount, lose → -amount)
    balance.value = isWin
      ? balance.value + betAmount.value
      : balance.value - betAmount.value;

    // Clamp betAmount so it never exceeds new balance
    if (betAmount.value > balance.value) betAmount.value = Math.max(1, Math.floor(balance.value));

    betHistory.value.unshift({
      amount: bet.amount,
      outcome: bet.outcome,
      payout: bet.payout,
      balanceAfter: balance.value,
    });

    // Flash balance badge
    balanceFlashed.value = true;
    setTimeout(() => { balanceFlashed.value = false; }, 500);

    if (isWin) {
      showParticles.value = true;
      setTimeout(() => { showParticles.value = false; }, 2000);
    }
  } catch (e) {
    clearInterval(anim);
    reels.value = ['🎰', '🎰', '🎰'];
    betError.value = e.message || 'Bet failed';
  } finally {
    betting.value = false;
  }
}

// ─── Quit / close session ────────────────────────────────────────────────────
async function quitSession() {
  closing.value = true;
  try {
    const closed = await closeGameSession(session.value.id);
    closedSession.value = { id: closed.id, balance: balance.value };
  } catch {
    // Even if close fails, reset UI
    closedSession.value = { id: session.value.id, balance: balance.value };
  } finally {
    session.value  = null;
    closing.value  = false;
  }
}

function particleStyle(n) {
  return {
    '--angle': `${(n / 20) * 360}deg`,
    '--dist':  `${90 + Math.random() * 60}px`,
    animationDelay: `${(n * 0.06).toFixed(2)}s`,
  };
}
</script>

<style scoped>
/* ── Page wrapper ── */
.bonus-game {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.4rem;
  padding-bottom: 3rem;
}

/* ── Lobby ── */
.lobby-header { text-align: center; }
.page-title   { margin: 0; font-size: 1.7rem; color: var(--accent); }
.page-sub     { margin: 0.25rem 0 0; color: var(--muted); font-size: 0.9rem; }

.credits-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--panel);
  border: 1px solid var(--accent);
  border-radius: 14px;
  padding: 1.2rem 2rem;
  width: 100%;
  max-width: 360px;
}
.credits-label  { font-size: 0.7rem; letter-spacing: 0.1em; color: var(--muted); }
.credits-amount { font-size: 3rem; font-weight: 800; color: var(--accent); line-height: 1.1; }
.credits-sub    { font-size: 0.85rem; color: var(--text); margin-top: 0.25rem; }
.credits-hint   { color: var(--muted); font-size: 0.78rem; margin-left: 0.25rem; }

.lobby-form {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  max-width: 340px;
}

/* ── Session bar ── */
.session-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 0.65rem 1rem;
}
.session-info   { display: flex; flex-direction: column; }
.session-label  { font-size: 0.65rem; letter-spacing: 0.1em; color: var(--muted); }
.session-id     { font-size: 0.9rem; font-weight: 600; color: var(--text); font-family: monospace; }

/* ── Balance ── */
.balance-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 0.75rem 2rem;
  transition: border-color 0.25s;
}
.balance-display.flash { border-color: var(--accent); }
.balance-label { font-size: 0.65rem; letter-spacing: 0.1em; color: var(--muted); }
.balance-value {
  font-size: 2.4rem;
  font-weight: 800;
  color: var(--accent);
  font-variant-numeric: tabular-nums;
}

/* ── Machine ── */
.machine {
  position: relative;
  background: linear-gradient(160deg, #1e2f45 0%, #12202f 100%);
  border: 2px solid var(--border);
  border-radius: 20px;
  padding: 0 1.5rem;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.5);
}
.machine-rail {
  height: 5px;
  background: linear-gradient(90deg, #2a3f5c, var(--accent), #2a3f5c);
  border-radius: 3px;
  margin: 1.1rem 0;
}

.reels-wrapper { display: flex; gap: 0.75rem; justify-content: center; }
.reel {
  flex: 1;
  background: #0a1520;
  border: 1px solid var(--border);
  border-radius: 12px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: inset 0 2px 8px rgba(0,0,0,0.6);
}
.reel.spinning { animation: shake 0.11s linear infinite; }
.reel.spinning .reel-symbol { animation: blur-sym 0.09s linear infinite alternate; }

@keyframes shake {
  0%,100% { transform: translateY(0); }
  50%      { transform: translateY(-3px); }
}
@keyframes blur-sym {
  0%   { filter: blur(3px); opacity: 0.6; }
  100% { filter: blur(0);   opacity: 1; }
}

.reel-symbol {
  font-size: 3rem;
  user-select: none;
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.reel-symbol.landed { transform: scale(1.1); }

/* ── Result pill ── */
.result-pill {
  text-align: center;
  padding: 0.4rem 1.2rem;
  border-radius: 20px;
  font-weight: 700;
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
}
.result-pill.win  { background: var(--accent); color: #0a0f14; }
.result-pill.lose { background: #2c3a50;        color: var(--muted); }

.pill-fade-enter-active { transition: all 0.3s ease; }
.pill-fade-leave-active { transition: all 0.15s ease; }
.pill-fade-enter-from   { opacity: 0; transform: translateY(6px); }
.pill-fade-leave-to     { opacity: 0; }

/* ── Bet controls ── */
.bet-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.65rem;
  width: 100%;
  max-width: 340px;
}
.bet-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
}
.chip {
  background: var(--panel);
  border: 1px solid var(--border);
  color: var(--text);
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
  font-weight: 700;
  cursor: pointer;
  transition: border-color 0.15s;
  flex-shrink: 0;
}
.chip:hover:not(:disabled) { border-color: var(--accent); }
.chip:disabled { opacity: 0.4; cursor: not-allowed; }

.bet-input { text-align: center; flex: 1; }

/* ── Shared fields ── */
.field-label {
  font-size: 0.78rem;
  letter-spacing: 0.06em;
  color: var(--muted);
  text-transform: uppercase;
  align-self: flex-start;
}
.field-input {
  width: 100%;
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text);
  padding: 0.6rem 0.85rem;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.15s;
}
.field-input:focus { border-color: var(--accent); }
.field-input:disabled { opacity: 0.5; }

.field-hint {
  font-size: 0.78rem;
  color: var(--muted);
  margin: 0;
  text-align: center;
  line-height: 1.5;
}

/* ── Buttons ── */
.btn {
  border: none;
  border-radius: 50px;
  padding: 0.6rem 1.4rem;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  transition: opacity 0.15s, transform 0.15s, box-shadow 0.15s;
}
.btn:disabled { opacity: 0.4; cursor: not-allowed; }
.btn.primary {
  background: linear-gradient(135deg, var(--accent), var(--accent-dim));
  color: #0a0f14;
  box-shadow: 0 4px 14px rgba(61,214,140,0.3);
}
.btn.primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(61,214,140,0.4);
}
.btn.danger {
  background: transparent;
  color: var(--danger);
  border: 1px solid var(--danger);
}
.btn.danger:hover:not(:disabled) { background: rgba(232,93,111,0.1); }
.btn.large { padding: 0.8rem 2rem; font-size: 1rem; width: 100%; border-radius: 10px; }

/* ── Messages ── */
.msg             { font-size: 0.88rem; margin: 0; text-align: center; }
.msg.error       { color: var(--danger); }
.msg.muted       { color: var(--muted); }
.no-credits-hint { max-width: 320px; line-height: 1.5; }

/* ── History table ── */
.history { width: 100%; max-width: 480px; }
.history-title {
  font-size: 0.75rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--muted);
  margin: 0 0 0.5rem;
}
.history-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}
.history-table th {
  text-align: left;
  padding: 0.3rem 0.5rem;
  color: var(--muted);
  font-size: 0.75rem;
  border-bottom: 1px solid var(--border);
}
.history-table td {
  padding: 0.35rem 0.5rem;
  border-bottom: 1px solid var(--border);
}
.row-win td  { color: var(--accent); }
.row-lose td { color: var(--muted); }

/* ── Closed banner ── */
.closed-banner {
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 1rem 1.5rem;
  text-align: center;
  color: var(--muted);
  font-size: 0.9rem;
}
.closed-banner strong { color: var(--text); }

/* ── Win particles ── */
.particles { position: fixed; top: 50%; left: 50%; pointer-events: none; z-index: 100; }
.particle {
  position: absolute;
  color: var(--accent);
  font-size: 1.1rem;
  animation: burst 1.8s ease-out forwards;
}
@keyframes burst {
  0%   { transform: rotate(var(--angle)) translateX(0) scale(1); opacity: 1; }
  80%  { opacity: 1; }
  100% { transform: rotate(var(--angle)) translateX(var(--dist)) scale(0.2); opacity: 0; }
}
</style>