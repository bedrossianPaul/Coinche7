<template>
  <Dialog
    v-model:visible="isOpen"
    modal
    :closable="false"
    :draggable="false"
    :style="{ width: '28rem', maxWidth: '95vw' }"
    :header="dialogTitle"
  >
    <div class="flex flex-col gap-4">
      <p class="text-sm text-slate-600">{{ dialogDescription }}</p>

      <template v-if="mode === 'DEPART'">
        <div v-if="previousAnnonce" class="rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-left">
          <div class="text-xs font-semibold uppercase tracking-wide text-slate-500">Annonce précédente</div>
          <div class="flex items-center gap-2 text-sm font-bold text-slate-800">
            <template v-if="typeInfo(previousAnnonce.type)">
              <span>{{ previousAnnonce.points }}</span>
              <span
                class="flex items-center gap-1 rounded px-1.5 py-0.5 text-base font-black"
                :class="typeInfo(previousAnnonce.type).colorClass"
              >
                {{ typeInfo(previousAnnonce.type).symbol }}
                {{ typeInfo(previousAnnonce.type).short }}
              </span>
            </template>
            <template v-else>{{ previousAnnonceText }}</template>
          </div>
        </div>

        <div class="grid grid-cols-3 gap-2">
          <button
            type="button"
            class="rounded-md border px-2 py-2 text-sm font-semibold"
            :class="depart.actionType === 'ANNOUNCE' ? 'border-slate-900 bg-slate-900 text-white' : 'border-slate-300 bg-white text-slate-700 hover:bg-slate-100'"
            @click="depart.actionType = 'ANNOUNCE'"
          >
            Annoncer
          </button>

          <button
            type="button"
            class="rounded-md border px-2 py-2 text-sm font-semibold"
            :class="[
              depart.actionType === 'COINCHE' ? 'border-slate-900 bg-slate-900 text-white' : 'border-slate-300 bg-white text-slate-700',
              !canCoinche ? 'cursor-not-allowed opacity-40' : 'hover:bg-slate-100'
            ]"
            :disabled="!canCoinche"
            @click="depart.actionType = 'COINCHE'"
          >
            Coincher
          </button>

          <button
            type="button"
            class="rounded-md border px-2 py-2 text-sm font-semibold"
            :class="[
              depart.actionType === 'SURCOINCHE' ? 'border-slate-900 bg-slate-900 text-white' : 'border-slate-300 bg-white text-slate-700',
              !canSurcoinche ? 'cursor-not-allowed opacity-40' : 'hover:bg-slate-100'
            ]"
            :disabled="!canSurcoinche"
            @click="depart.actionType = 'SURCOINCHE'"
          >
            Surcoincher
          </button>
        </div>

        <div :class="depart.actionType !== 'ANNOUNCE' ? 'opacity-40 pointer-events-none' : ''" class="flex flex-col gap-3">
          <BidPointsInput
            v-model="depart.points"
            :min="minBidPoints"
            :max="maxBidPoints"
            :step="10"
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
                  depart.type === t.value
                    ? 'border-slate-900 bg-slate-900 text-white'
                    : `border-slate-200 bg-white hover:bg-slate-50 ${t.colorClass}`
                ]"
                @click="depart.type = t.value"
              >
                <span>{{ t.symbol }}</span>
                <span class="text-xs font-semibold">{{ t.short }}</span>
              </button>
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="mode === 'IN_GAME'">
        <label class="flex flex-col gap-1 text-sm font-semibold text-slate-700">
          Annonce en jeu
          <select v-model="inGame.annonce" class="rounded-md border border-slate-300 bg-white px-2 py-2 text-sm font-medium">
            <option v-for="value in availableInGameAnnouncements" :key="value" :value="value">{{ value }}</option>
          </select>
        </label>
      </template>

      <template v-else-if="mode === 'COUNTER'">
        <label class="flex flex-col gap-1 text-sm font-semibold text-slate-700">
          Type de contre
          <select v-model="counter.type" class="rounded-md border border-slate-300 bg-white px-2 py-2 text-sm font-medium">
            <option value="COINCHE">Coinche</option>
            <option value="SURCOINCHE">Surcoinche</option>
          </select>
        </label>
      </template>
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
  data() {
    return {
      isOpen: false,
      mode: 'DEPART',
      resolver: null,
      depart: {
        points: 80,
        type: 'S',
        actionType: 'ANNOUNCE'
      },
      inGame: {
        annonce: 'Belote'
      },
      counter: {
        type: 'COINCHE'
      },
      minBidPoints: 80,
      maxBidPoints: 500,
      canCoinche: false,
      canSurcoinche: false,
      previousAnnonce: null,
      availableTypes: [
        { value: 'S',  symbol: '♠', short: 'Pique',     colorClass: 'text-slate-800' },
        { value: 'H',  symbol: '♥', short: 'Cœur',      colorClass: 'text-red-600'   },
        { value: 'D',  symbol: '♦', short: 'Carreau',   colorClass: 'text-red-600'   },
        { value: 'C',  symbol: '♣', short: 'Trèfle',    colorClass: 'text-slate-800' },
        { value: 'SA', symbol: '—', short: 'Sans-atout', colorClass: 'text-emerald-700' },
        { value: 'TA', symbol: '★', short: 'Tout-atout', colorClass: 'text-amber-600'  }
      ],
      availableInGameAnnouncements: ['Belote', 'Rebelote']
    }
  },
  computed: {
    dialogTitle() {
      if (this.mode === 'DEPART') {
        return 'Annonce de départ'
      }

      if (this.mode === 'COUNTER') {
        return 'Coinche / Surcoinche'
      }

      return 'Annonce en jeu'
    },
    dialogDescription() {
      if (this.mode === 'DEPART') {
        return "Choisis ton contrat (points + atout) ou passe ton tour."
      }

      if (this.mode === 'COUNTER') {
        return 'Tu peux coincher, surcoincher, ou passer.'
      }

      return 'Choisis ton annonce en jeu, ou passe.'
    },
    previousAnnonceText() {
      if (!this.previousAnnonce) {
        return ''
      }

      const points = Number(this.previousAnnonce.points)
      const rawType = this.previousAnnonce.type || ''
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
  methods: {
    typeInfo(value) {
      return this.availableTypes.find((t) => t.value === String(value ?? '').toUpperCase()) || null
    },
    open(type = 'DEPART', options = {}) {
      this.mode = this.normalizeMode(type)
      this.previousAnnonce = null
      this.canCoinche = false
      this.canSurcoinche = false

      if (Array.isArray(options.bidPoints) && options.bidPoints.length) {
        const numericValues = options.bidPoints
          .map((value) => Number(value))
          .filter((value) => Number.isFinite(value))

        if (numericValues.length) {
          this.minBidPoints = Math.min(...numericValues)
          this.maxBidPoints = Math.max(...numericValues)
        }
      }

      if (Number.isFinite(Number(options.minPoints))) {
        this.minBidPoints = Number(options.minPoints)
      }

      if (Number.isFinite(Number(options.maxPoints))) {
        this.maxBidPoints = Number(options.maxPoints)
      }

      if (this.maxBidPoints < this.minBidPoints) {
        this.maxBidPoints = this.minBidPoints
      }

      if (Array.isArray(options.availableTypes) && options.availableTypes.length) {
        this.availableTypes = options.availableTypes
      }

      if (Array.isArray(options.inGameAnnouncements) && options.inGameAnnouncements.length) {
        this.availableInGameAnnouncements = options.inGameAnnouncements
      }

      if (this.mode === 'DEPART') {
        this.previousAnnonce = options.previousAnnonce || null

        const hasAnnonce = Boolean(this.previousAnnonce)
        const previousPoints = Number(this.previousAnnonce?.points)
        const computedMin = Number.isFinite(previousPoints)
          ? Math.min(this.maxBidPoints, previousPoints + 10)
          : this.minBidPoints

        if (Number.isFinite(previousPoints)) {
          this.minBidPoints = computedMin
        }

        this.canCoinche = Boolean(options.canCoinche) || hasAnnonce
        this.canSurcoinche = Boolean(options.canSurcoinche) || hasAnnonce
        this.depart.points = this.minBidPoints
        this.depart.type = this.availableTypes[0]?.value || 'S'
        this.depart.actionType = 'ANNOUNCE'
      }

      if (this.mode === 'IN_GAME') {
        this.inGame.annonce = this.availableInGameAnnouncements[0] || 'Belote'
      }

      if (this.mode === 'COUNTER') {
        this.counter.type = 'COINCHE'
      }

      this.isOpen = true

      return new Promise((resolve) => {
        this.resolver = resolve
      })
    },
    normalizeMode(type) {
      const key = String(type || '').trim().toUpperCase()

      if (['DEPART', 'BID_START', 'START_BID'].includes(key)) {
        return 'DEPART'
      }

      if (['COUNTER', 'COINCHE', 'SURCOINCHE'].includes(key)) {
        return 'COUNTER'
      }

      return 'IN_GAME'
    },
    submitPass() {
      this.resolveAndClose({ action: 'PASS', mode: this.mode })
    },
    submitAnnonce() {
      if (this.mode === 'DEPART') {
        if (this.depart.actionType === 'COINCHE' && this.canCoinche) {
          this.resolveAndClose({
            action: 'ANNOUNCE',
            mode: this.mode,
            type: 'COINCHE'
          })
          return
        }

        if (this.depart.actionType === 'SURCOINCHE' && this.canSurcoinche) {
          this.resolveAndClose({
            action: 'ANNOUNCE',
            mode: this.mode,
            type: 'SURCOINCHE'
          })
          return
        }

        this.resolveAndClose({
          action: 'ANNOUNCE',
          mode: this.mode,
          points: this.depart.points,
          type: this.depart.type
        })
        return
      }

      if (this.mode === 'COUNTER') {
        this.resolveAndClose({
          action: 'ANNOUNCE',
          mode: this.mode,
          type: this.counter.type
        })
        return
      }

      this.resolveAndClose({
        action: 'ANNOUNCE',
        mode: this.mode,
        type: this.inGame.annonce
      })
    },
    resolveAndClose(payload) {
      const resolver = this.resolver
      this.resolver = null
      this.isOpen = false

      if (resolver) {
        resolver(payload)
      }

      this.$emit('submit', payload)
    }
  }
}
</script>
