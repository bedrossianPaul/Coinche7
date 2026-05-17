<template>
  <Dialog
    :visible="isVisible"
    modal
    :closable="false"
    :draggable="false"
    :style="{ width: '28rem', maxWidth: '95vw' , position: 'absolute', top: '40%', left: '50%', transform: 'translate(-50%, -50%)' }"
    header="Annonce"
  >
    <div class="flex flex-col gap-4">
      <p class="text-sm text-slate-600">Fais ton annonce pour ce tour (contrat, coinche/surcoinche ou passe).</p>

      <div v-if="currentBid" class="rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-left">
          <div class="text-xs font-semibold uppercase tracking-wide text-slate-500">Dernier contrat le plus haut</div>
          <div class="flex items-center gap-2 text-sm font-bold text-slate-800">
            <template v-if="typeInfo(currentBidType)">
              <span>{{ currentBidPoints }}</span>
              <span
                class="flex items-center gap-1 rounded px-1.5 py-0.5 text-base font-black"
                :class="typeInfo(currentBidType).colorClass"
              >
                {{ typeInfo(currentBidType).symbol }}
                {{ typeInfo(currentBidType).short }}
              </span>
            </template>
            <template v-else>{{ previousAnnonceText }}</template>
          </div>
      </div>

      <div class="grid grid-cols-3 gap-2">
        <button
          type="button"
          class="rounded-md border px-2 py-2 text-sm font-semibold"
          :class="[
            form.actionType === 'ANNOUNCE' ? 'border-slate-900 bg-slate-900 text-white' : 'border-slate-300 bg-white text-slate-700',
            !canAnnounce ? 'cursor-not-allowed opacity-40' : 'hover:bg-slate-100'
          ]"
          :disabled="!canAnnounce"
          @click="selectAction('ANNOUNCE')"
        >
          Annoncer
        </button>

        <button
          type="button"
          class="rounded-md border px-2 py-2 text-sm font-semibold"
          :class="[
            form.actionType === 'COINCHE' ? 'border-slate-900 bg-slate-900 text-white' : 'border-slate-300 bg-white text-slate-700',
            !canCoinche ? 'cursor-not-allowed opacity-40' : 'hover:bg-slate-100'
          ]"
          :disabled="!canCoinche"
          @click="selectAction('COINCHE')"
        >
          Coincher
        </button>

        <button
          type="button"
          class="rounded-md border px-2 py-2 text-sm font-semibold"
          :class="[
            form.actionType === 'SURCOINCHE' ? 'border-slate-900 bg-slate-900 text-white' : 'border-slate-300 bg-white text-slate-700',
            !canSurcoinche ? 'cursor-not-allowed opacity-40' : 'hover:bg-slate-100'
          ]"
          :disabled="!canSurcoinche"
          @click="selectAction('SURCOINCHE')"
        >
          Surcoincher
        </button>
      </div>

      <div :class="form.actionType !== 'ANNOUNCE' || !canAnnounce ? 'opacity-40 pointer-events-none' : ''" class="flex flex-col gap-3">
        <BidPointsInput
          v-model="form.points"
        />

        <div class="flex flex-col gap-1">
          <span class="text-sm font-semibold text-slate-700">Atout</span>
          <div class="grid grid-cols-3 gap-2">
            <button
              v-for="t in availableTypes"
              :key="t.value"
              type="button"
              class="flex items-center justify-center gap-1 rounded-md border px-2 py-2 text-sm font-bold transition-colors"
              :class="[
                form.type === t.value
                  ? 'border-slate-900 bg-slate-900 text-white'
                  : `border-slate-200 bg-white hover:bg-slate-50 ${t.colorClass}`
              ]"
              @click="form.type = t.value"
            >
              <span>{{ t.symbol }}</span>
              <span class="text-xs font-semibold">{{ t.short }}</span>
            </button>
          </div>
        </div>
      </div>

    </div>

    <template #footer>
      <div class="flex justify-end gap-2">
        <button
          type="button"
          class="rounded-md border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
          @click="submitPass"
        >
          Passer
        </button>
        <button
          type="button"
          class="rounded-md bg-slate-900 px-3 py-2 text-sm font-semibold text-white hover:bg-slate-700"
          :disabled="!canSubmit"
          :class="!canSubmit ? 'cursor-not-allowed opacity-40' : ''"
          @click="submitAnnonce"
        >
          Valider l'annonce
        </button>
      </div>
    </template>
  </Dialog>
</template>

<script>
import Dialog from 'primevue/dialog'
import BidPointsInput from './BidPointsInput.vue'

export default {
  name: 'Annonce',
  components: {
    Dialog,
    BidPointsInput
  },
  props: {
    game_manager: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      form: {
        points: this.game_manager.gameStatus?.bid?.points +10 || 80,
        type: 'S',
        actionType: 'ANNOUNCE'
      },
      availableTypes: [
        { value: 'S',  symbol: '♠', short: 'Pique',     colorClass: 'text-slate-800' },
        { value: 'H',  symbol: '♥', short: 'Cœur',      colorClass: 'text-red-600'   },
        { value: 'D',  symbol: '♦', short: 'Carreau',   colorClass: 'text-red-600'   },
        { value: 'C',  symbol: '♣', short: 'Trèfle',    colorClass: 'text-slate-800' },
        { value: 'SA', symbol: '—', short: 'Sans-atout', colorClass: 'text-emerald-700' },
        { value: 'TA', symbol: '★', short: 'Tout-atout', colorClass: 'text-amber-600'  }
      ]
    }
  },
  computed: {
    gameStatus() {
      return this.game_manager?.gameStatus || {}
    },
    isVisible() {
      return this.game_manager.action === 'BID'
    },
    currentBid() {
      return this.gameStatus.bid || null
    },
    currentBidPoints() {
      const value = Number(this.currentBid?.points)
      return Number.isFinite(value) ? value : 0
    },
    currentBidType() {
      return String(this.currentBid?.type ?? this.currentBid?.trump ?? '').toUpperCase()
    },
    maxBidPoints() {
      return 250
    },
    minBidPoints() {
      if (this.currentBidPoints > 0) {
        return this.currentBidPoints + 10
      }
      return 80
    },
    possibleActions() {
      const explicit = this.extractAvailableActions(this.gameStatus)

      if (explicit.length) {
        return explicit
      }

      return this.deriveActionsFromBid(this.currentBid)
    },
    canAnnounce() {
      return this.possibleActions.includes('ANNOUNCE')
    },
    canCoinche() {
      return this.possibleActions.includes('COINCHE')
    },
    canSurcoinche() {
      return this.possibleActions.includes('SURCOINCHE')
    },
    canSubmit() {
      if (this.form.actionType === 'ANNOUNCE') {
        return this.canAnnounce
      }

      if (this.form.actionType === 'COINCHE') {
        return this.canCoinche
      }

      if (this.form.actionType === 'SURCOINCHE') {
        return this.canSurcoinche
      }

      return false
    },
    previousAnnonceText() {
      if (!this.currentBid) {
        return ''
      }

      const points = Number(this.currentBid.points)
      const rawType = this.currentBidType
      const info = this.availableTypes.find((t) => t.value === rawType.toUpperCase())
      const typeDisplay = info ? `${info.symbol} ${info.short}` : rawType

      if (Number.isFinite(points) && typeDisplay) {
        return `${points} – ${typeDisplay}`
      }

      if (Number.isFinite(points)) {
        return `${points}`
      }

      return typeDisplay || 'Annonce en cours'
    }
  },
  watch: {
    isVisible(next) {
      if (next) {
        this.resetForTurn()
      }
    },
    possibleActions() {
      this.syncActionSelection()
    },
    minBidPoints(nextMin) {
      if (this.form.points < nextMin) {
        this.form.points = nextMin
      }
    }
  },
  methods: {
    typeInfo(value) {
      return this.availableTypes.find((t) => t.value === String(value ?? '').toUpperCase()) || null
    },
    normalizeAction(value) {
      return String(value ?? '').trim().toUpperCase()
    },
    extractAvailableActions(status) {
      const raw = status?.availableActions || status?.possibleActions || status?.actions

      if (!Array.isArray(raw)) {
        return []
      }

      const allowed = ['ANNOUNCE', 'COINCHE', 'SURCOINCHE', 'PASS']
      const normalized = raw
        .map((action) => this.normalizeAction(action))
        .filter((action) => allowed.includes(action))

      return [...new Set(normalized)]
    },
    deriveActionsFromBid(bid) {
      const actions = ['PASS']

      // S'il n'y a pas de bid ou si le bid n'a pas de points, seul PASS et ANNOUNCE sont possibles
      if (!bid || !bid.points || bid.points === 0) {
        actions.push('ANNOUNCE')
        return actions
      }

      const bidType = this.normalizeAction(bid.type || bid.trump)
      const isCoinched = Boolean(bid.coinched || bid.coinche || bid.isCoinched)
      const isSurcoinched = Boolean(bid.surcoinched || bid.surcoinche || bid.isSurcoinched)

      if (bidType === 'SURCOINCHE' || isSurcoinched) {
        return actions
      }

      if (bidType === 'COINCHE' || isCoinched) {
        actions.push('SURCOINCHE')
        return actions
      }

      actions.push('ANNOUNCE', 'COINCHE')
      return actions
    },
    firstAllowedAction() {
      if (this.canAnnounce) {
        return 'ANNOUNCE'
      }

      if (this.canCoinche) {
        return 'COINCHE'
      }

      if (this.canSurcoinche) {
        return 'SURCOINCHE'
      }

      return 'ANNOUNCE'
    },
    syncActionSelection() {
      const current = this.form.actionType

      if (
        (current === 'ANNOUNCE' && this.canAnnounce)
        || (current === 'COINCHE' && this.canCoinche)
        || (current === 'SURCOINCHE' && this.canSurcoinche)
      ) {
        return
      }

      this.form.actionType = this.firstAllowedAction()
    },
    resetForTurn() {
      this.form.points = this.minBidPoints
      this.form.type = this.availableTypes[0]?.value || 'S'
      this.form.actionType = this.firstAllowedAction()
    },
    selectAction(actionType) {
      const next = this.normalizeAction(actionType)

      if (next === 'ANNOUNCE' && this.canAnnounce) {
        this.form.actionType = 'ANNOUNCE'
      }

      if (next === 'COINCHE' && this.canCoinche) {
        this.form.actionType = 'COINCHE'
      }

      if (next === 'SURCOINCHE' && this.canSurcoinche) {
        this.form.actionType = 'SURCOINCHE'
      }
    },
    sendBid(payload) {
      if (this.game_manager?.sendBid) {
        this.game_manager.sendBid(payload)
      }

      this.$emit('submit', payload)
    },
    submitPass() {
      this.sendBid({ action: 'PASS', type: 'PASS' })
    },
    submitAnnonce() {
      if (!this.canSubmit) {
        return
      }

      if (this.form.actionType === 'COINCHE' || this.form.actionType === 'SURCOINCHE') {
        this.sendBid({
          action: this.form.actionType,
          type: this.form.actionType
        })
        return
      }

      this.sendBid({
        action: 'ANNOUNCE',
        id: this.game_manager.me.id ,
        points: this.form.points,
        type: this.form.type,
        trump: this.form.type
      })
    }
  }
}
</script>
