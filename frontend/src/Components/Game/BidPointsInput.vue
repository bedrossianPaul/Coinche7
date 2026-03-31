<template>
  <label class="flex flex-col gap-1 text-sm font-semibold text-slate-700">
    Points
    <input
      v-model.number="localValue"
      type="number"
      :min="min"
      :max="max"
      :step="step"
      class="rounded-md border border-slate-300 bg-white px-2 py-2 text-sm font-medium"
      @blur="commitValue"
      @change="commitValue"
    >
    <span class="text-xs font-medium text-slate-500">Entre {{ min }} et {{ max }} (pas de {{ step }})</span>
  </label>
</template>

<script>
export default {
  name: 'BidPointsInput',
  props: {
    modelValue: {
      type: Number,
      default: 80
    },
    min: {
      type: Number,
      default: 80
    },
    max: {
      type: Number,
      default: 500
    },
    step: {
      type: Number,
      default: 10
    }
  },
  emits: ['update:modelValue'],
  data() {
    return {
      localValue: this.modelValue
    }
  },
  watch: {
    modelValue(newValue) {
      this.localValue = newValue
    }
  },
  methods: {
    commitValue() {
      const nextValue = this.normalizeValue(this.localValue)
      this.localValue = nextValue
      this.$emit('update:modelValue', nextValue)
    },
    normalizeValue(value) {
      const raw = Number(value)

      if (!Number.isFinite(raw)) {
        return this.min
      }

      const clamped = Math.max(this.min, Math.min(this.max, raw))
      const offset = clamped - this.min
      const roundedOffset = Math.round(offset / this.step) * this.step
      const stepped = this.min + roundedOffset

      return Math.max(this.min, Math.min(this.max, stepped))
    }
  }
}
</script>