import {useAuth} from "./useAuth";

class GameManager {
    constructor(url) {
        this.gameStatus = null;
        this.tunnel_url = url;
        this.me = useAuth().user.value;
        this.connected = false;
        this.action = null;
    }

    get_my_card() {
        for (const player of Object.values(this.gameStatus.players)) {
            if (player && player.id === this.me.id) {
                return player.hand;
            }
        }
        return [];
    }

    messageSerializer(type, payload) {
        return JSON.stringify({ type, payload });
    }

    messageDeserializer(message) {
        return JSON.parse(message);
    }

    _updateState_(newState) {
        this.gameStatus = newState;
        console.log(`State updated: ${JSON.stringify(this.gameStatus)}`);
    }

    _handle_request_(request) {
        console.log(`Received request from server: ${JSON.stringify(request)}`);
        switch (request.action) {
            case "BID":
                this.action = "BID";
                break;
            default:
                console.warn(`Unknown request action: ${request.action}`);
        }
        return;
    }

    _handle_info_(payload) {
        
        console.log(`Received info from server: ${payload}`);
        return;
    }

    _handle_error_(payload) {        
        console.error(`Error from server: ${payload}`);
        return;
    }

    async connect() {
        this.socket = new WebSocket(this.tunnel_url);
        this.isConnected = true;

        // Ask the server to connect us as soon as the socket is open
        this.socket.onopen = () => {
            this.socket.send(this.sendConnect());
            console.log("WebSocket connection established and CONNECT message sent.");
        };

        // Handle incoming messages from the server
        this.socket.onmessage = (event) => {
            const {type, payload} = this.messageDeserializer(event.data);
            switch (type) {
                case "STATE":
                    this._updateState_(payload);
                    break;
                case "REQUEST":
                    this._handle_request_(payload);
                    break;
                case "INFO":
                    this._handle_info_(payload);
                    break;
                case "ERROR":
                    this._handle_error_(payload);
                    break;
                default:
                    console.warn(`Unknown message type: ${type}`);
            }
        };
    }

    sendAction(card_code) {
        this.socket.send(this.messageSerializer("ACTION", { card: card_code }));
    }

    sendBid(bid) {
        this.socket.send(this.messageSerializer("BID", bid));
    }

    sendConnect() {
        return this.messageSerializer("CONNECT", { user: this.me });
    }
}

export default GameManager;