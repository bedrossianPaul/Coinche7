<script>
export default {
  name: 'PlayerSeat',
  props: {
    player: {
      type: Object,
      required: true
    },
    seatClass: {
      type: String,
      default: 'pi pi-user'
    }
  }
}
</script>

<template>
  <div
    class="inline-flex h-10 w-10 items-center justify-center rounded-full border border-white/35 bg-white/90 text-slate-700 shadow-lg shadow-emerald-950/15 backdrop-blur-sm sm:h-12 sm:w-12"
    :title="player.name || 'Joueur'"
    :aria-label="player.name || 'Joueur'"
  >
    <i :class="seatClass" class="text-base sm:text-lg"></i>
  </div>
</template>
