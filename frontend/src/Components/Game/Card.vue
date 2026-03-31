<template>
    <div
        class="h-28 w-20 overflow-hidden rounded-lg border-2 bg-white shadow-md"
        :style="color ? { borderColor: color } : {}"
    >
        <img
            :src="cardImage"
            :alt="`${value} ${suitSymbol}`"
            class="h-full w-full object-cover"
        >
    </div>
</template>

<script>
export default {
    name: 'Card',
    props: {
        value: {
            type: String,
            required: true
        },
        // Props optionnelles pré-résolues par le parent via useCard
        cardImage: {
            type: String,
            default: null
        },
        cardSuit: {
            type: String,
            default: null
        },
        suitSymbol: {
            type: String,
            default: null
        },
        color: {
            type: String,
            default: null
        }
    },
}
</script>