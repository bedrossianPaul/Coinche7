<template>
  <Transition name="msg">
    <div
      v-if="visible"
      class="pointer-events-none absolute inset-0 z-50 flex items-center justify-center w-full h-full"
    >
      <div
        class="msg-text select-none text-center text-5xl font-black tracking-tight drop-shadow-[0_4px_24px_rgba(0,0,0,0.7)]" style="font-family: 'Cinzel Decorative', serif;"
        :style="textStyle"
      >
        {{ current.text }}
      </div>
    </div>
  </Transition>
</template>

<script>
export default {
  name: 'GlobalMessage',
  data() {
    return {
      visible: false,
      current: { text: '', color: '#ffffff' },
      queue: [],
      running: false
    }
  },
  computed: {
    textStyle() {
      return {
        color: this.current.color,
        WebkitTextStroke: '3px rgba(0,0,0,0.5)',
        textShadow: `0 0 40px ${this.current.color}88, 0 4px 8px rgba(0,0,0,0.8)`
      }
    }
  },
  methods: {
    /**
     * Affiche un message pendant une durée donnée.
     * @param {string} text      - Texte à afficher
     * @param {number} duration  - Durée en ms (défaut : 900)
     * @param {string} color     - Couleur CSS (défaut : '#ffffff')
     */
    show(text, duration = 900, color = '#ffffff') {
      this.queue.push({ text, duration, color })
      if (!this.running) this._next()
    },
    _next() {
      if (!this.queue.length) {
        this.running = false
        return
      }

      this.running = true
      this.current = this.queue.shift()
      this.visible = true

      setTimeout(() => {
        this.visible = false
        setTimeout(() => this._next(), 250)
      }, this.current.duration)
    }
  }
}
</script>

<style scoped>
.msg-enter-active {
  animation: msg-pop 0.22s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}
.msg-leave-active {
  animation: msg-pop 0.18s cubic-bezier(0.55, 0, 1, 0.45) reverse forwards;
}

@keyframes msg-pop {
  from {
    opacity: 0;
    transform: scale(0.4);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
