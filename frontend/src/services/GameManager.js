import { useAuth } from "./useAuth";

const PHASES = {
    JOINING: 'JOINING',
    GAME_START: 'GAME_START',
    SHUFFLE_CARDS: 'SHUFFLE_CARDS',
    DEALING_CARDS: 'DEALING_CARDS',
    AUCTIONS: 'AUCTIONS',
};

class GameManager {
    phase = PHASES.JOINING;
    currentPlayer = null;
    players = [];
    cards = [];
    remainingTime = null;
    currentAuction = null;

    #iam;
    #token;
    #webSocketService;
    #phaseChangeListeners = new Set();
    #requestListeners = new Set();

    constructor(url) {
        this.phase = PHASES.JOINING;
        this.#iam = useAuth().user.value;
        this.#token = useAuth().token.value;
        this.#webSocketService = new WebSocket(`ws://${url}`);
        this.#webSocketService.send(JSON.stringify({
            type: 'JOIN',
            data: {
                token: this.#token,
                user: this.#iam,
            }
        }));
    }

    init() {
        this.#webSocketService.onmessage = (event) => {
            const message = JSON.parse(event.data);
            this.#handleWSMessage(message);
        };
    }

    onPhaseChange(callback) {
        if (typeof callback !== 'function') {
            return () => {};
        }

        this.#phaseChangeListeners.add(callback);
        return () => {
            this.#phaseChangeListeners.delete(callback);
        };
    }

    onRequest(callback) {
        if (typeof callback !== 'function') {
            return () => {};
        }

        this.#requestListeners.add(callback);
        return () => {
            this.#requestListeners.delete(callback);
        };
    }

    sendAuction(auction){
        this.#webSocketService.send(JSON.stringify({
            type: 'AUCTION',
            data: {
                auction,
                token: this.#token,
                user: this.#iam,
            }
        }));
    }

    #notifyPhaseChange() {
        this.#phaseChangeListeners.forEach((listener) => listener());
    }

    #notifyRequest() {
        this.#requestListeners.forEach((listener) => listener());
    }

    #handleWSMessage(message) {
        switch (message.type) {
            case 'INFO':
                this.#handleInfoMessage(message.data);
                break;
            case 'REQUEST':
                this.#handleRequestMessage(message.data);
                break;
            case 'ERROR':
                console.error(message.data);
                break;
            default:
                console.warn('Unknown message type:', message.type);
        }
    }

    #handleInfoMessage(data) {
        let hasPhaseChanged = false;

        switch (data.infoType) {
            // Serveur is shuffling the cards
            case 'GAME_START':
                this.phase = PHASES.GAME_START;
                this.users = data.body.users;
                hasPhaseChanged = true;
                break;
            case 'SHUFFLE_CARDS':
                this.phase = PHASES.SHUFFLE_CARDS;
                hasPhaseChanged = true;
                break;
            // Serveur is dealing the cards
            case 'DEALING_CARDS':
                this.phase = PHASES.DEALING_CARDS;
                this.cards = data.body.cards;
                hasPhaseChanged = true;
                break;
            case 'AUCTIONS':
                this.phase = PHASES.AUCTIONS;
                this.user = data.body.user;
                this.remainingTime = data.body.remainingTime;
                hasPhaseChanged = true;
                break;
            case 'AUCTION_UPDATE':
                this.currentAuction = data.body.auction;
                break;
            default:
                console.warn('Unknown info type:', data.infoType);
        }

        if (hasPhaseChanged) {
            this.#notifyPhaseChange();
        }
    }

    #handleRequestMessage(data){
        switch (data.requestType) {
            case 'AUCTION':
                this.currentAuction = data.body.auction;
                this.#notifyRequest();
                break;
            default:
                console.warn('Unknown request type:', data.requestType);
        }
    }
}