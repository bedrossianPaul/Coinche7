<template>
    <div class="relative w-full h-full">
        <div class="pointer-events-none absolute left-1/2 top-1/2 h-44 w-44 -translate-x-1/3 -translate-y-1/3" v-if="gameManager.gameStatus.current_trick">
            <div
                v-for="(trickCard, index) in displayedTrickCards"
                :key="`${trickCard.playerIndex}-${trickCard.value}-${index}`"
                class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2"
                :style="cardPositionStyle(trickCard.playerIndex, index)"
            >
                <Card v-bind="resolveCard(trickCard.value)" />
            </div>
        </div>
        <div class="absolute top-2 left-2">
            <div class="flex gap-3 items-end">
                <div v-if="gameManager.gameStatus.score" class=" bg-gray-200/40 px-4 py-2 rounded-md text-sm font-bold">
                    <div class="flex items-center gap-3">
                        <div class="flex flex-col items-start">
                            <div class="text-sm">Team 1: {{ gameManager.gameStatus.score.scoreTeam1 }} </div>
                            <div class="text-sm">Team 2: {{ gameManager.gameStatus.score.scoreTeam2 }} </div>
                        </div>
        
                    </div>
                </div>
                <!-- Last Trick Button -->
                <div class="relative" v-if="gameManager.gameStatus.last_trick">
                    <button
                        type="button"
                        class="h-9 w-9 cursor-pointer"
                        title="Voir le dernier pli"
                        @click="openLastTrickModal"
                    >
                        <span class="absolute left-2 top-0 h-6 w-4 rounded-sm border border-gray-800 bg-white/90"></span>
                        <span class="absolute left-1 top-1 h-6 w-4 rounded-sm border border-gray-800 bg-white/90"></span>
                        <span class="absolute left-0 top-2 h-6 w-4 rounded-sm border border-gray-800 bg-white"></span>
                    </button>
                </div>
            </div>
        </div>

        <!-- Last Trick Modal -->
        <Dialog v-model:visible="isLastTrickModalOpen" modal header="Dernier pli" :style="{ width: '28rem' }">
            <div v-if="displayedLastTrickCards.length" class="mx-auto h-64 w-64">
                <div class="pointer-events-none relative h-full w-full">
                    <div
                        v-for="(trickCard, index) in displayedLastTrickCards"
                        :key="`last-${trickCard.playerIndex}-${trickCard.value}-${index}`"
                        class="absolute left-1/2 top-1/2"
                        :style="cardPositionStyle(trickCard.playerIndex, index)"
                    >
                        <Card v-bind="resolveCard(trickCard.value)" />
                    </div>
                </div>
            </div>
            <div v-else class="py-6 text-center text-sm text-slate-600">
                Aucun dernier pli disponible.
            </div>

            <template #footer>
                <button
                    type="button"
                    class="rounded px-2 py-1 text-sm font-semibold text-slate-700 hover:bg-slate-100"
                    @click="closeLastTrickModal"
                >
                    Fermer
                </button>
            </template>
        </Dialog>

        <!-- Player Positions -->
        <div v-if="gameManager.gameStatus.players.NORTH" class="absolute top-2 left-3/4 -translate-x-1/2">
            <Player :player="gameManager.gameStatus.players.NORTH" :game_manager="gameManager"/>
        </div>

        <div v-if="gameManager.gameStatus.players.EAST" class="absolute top-3/4 right-1 -translate-y-1/2">
            <Player :player="gameManager.gameStatus.players.EAST" :game_manager="gameManager"/>
        </div>

        <div v-if="gameManager.gameStatus.players.SOUTH" class="absolute bottom-2 left-1/4 -translate-x-1/2">
            <Player :player="gameManager.gameStatus.players.SOUTH" :game_manager="gameManager"/>
        </div>

        <div v-if="gameManager.gameStatus.players.WEST" class="absolute top-1/4 left-1 -translate-y-1/2">
            <Player :player="gameManager.gameStatus.players.WEST" :game_manager="gameManager"/>
        </div>
    </div>
</template>

<script>
import Player from './Player.vue';
import Card from './Card.vue';
import Dialog from 'primevue/dialog';
import { useCard } from '../../services/useCard.js';

export default {
    name: 'Table',
    components: {
        Player,
        Card,
        Dialog
    },
    created() {
        const { getCardImage, getCardSuit, getSuitSymbol, getSuitColor } = useCard()
        this.$cardImage = getCardImage
        this.$cardSuit = getCardSuit
        this.$suitSymbol = getSuitSymbol
        this.$suitColor = getSuitColor
    },
    data() {
        return {
            isLastTrickModalOpen: false
        }
    },
    props: {
        gameManager: {
            type: Object,
            required: true
        },
    },
    computed: {
        displayedTrickCards() {
            return this.normalizeTrickCards(this.gameManager.gameStatus.current_trick)
        },
        displayedLastTrickCards() {
            return this.normalizeTrickCards(this.gameManager.gameStatus.last_trick)
        }
    },
    methods: {
        normalizeTrickCards(trick) {
            if (!Array.isArray(trick)) {
                return []
            }

            return trick
                .map((card, index) => {
                    if (typeof card === 'string') {
                        return { value: card, playerIndex: index }
                    }

                    if (card && typeof card.value === 'string') {
                        return {
                            value: card.value,
                            playerIndex: Number.isInteger(card.playerIndex) ? card.playerIndex : index
                        }
                    }

                    return null
                })
                .filter(Boolean)
        },
        openLastTrickModal() {
            this.isLastTrickModalOpen = true
        },
        closeLastTrickModal() {
            this.isLastTrickModalOpen = false
        },
        resolveCard(value) {
            const suit = this.$cardSuit(value)
            return {
                value,
                cardImage: this.$cardImage(value),
                cardSuit: suit,
                suitSymbol: this.$suitSymbol(suit),
                color: this.$suitColor(suit)
            }
        },
        cardPositionStyle(playerIndex, order) {
            const offsets = {
                0: { x: 0, y: -48, r: -4 },
                1: { x: 48, y: 0, r: 4 },
                2: { x: 0, y: 48, r: 3 },
                3: { x: -48, y: 0, r: -3 }
            }

            const base = offsets[playerIndex] || { x: 0, y: 0, r: 0 }
            const overlapShift = order * 4

            return {
                transform: `translate(-50%, -50%) translate(${base.x + overlapShift}px, ${base.y + overlapShift}px) rotate(${base.r}deg)`,
                zIndex: 10 + order
            }
        }
    }
}
</script>