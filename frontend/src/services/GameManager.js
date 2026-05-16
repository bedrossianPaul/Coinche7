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

    get_timeRemaining(position){
        return this.gameStatus.players[position].timeRemaining;
    }

    messageSerializer(type, payload) {
        return JSON.stringify({ 'type': type, 'payload': payload });
    }

    messageDeserializer(message) {
        try {
            return JSON.parse(message);
        } catch (error) {
            console.error("Invalid WS JSON message:", message, error);
            return null;
        }
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
            case "PLAY":
                this.action = "PLAY";
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

    _handle_ack_(payload) {
        this.action = null;
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
            const parsed = this.messageDeserializer(event.data);
            if (!parsed) {
                return;
            }
            const {type, payload} = parsed;
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
                case "ACK":
                    this._handle_ack_(payload);
                    break;
                case "ERROR":
                    this._handle_error_(payload);
                    break;
                default:
                    console.warn(`Unknown message type: ${type}`);
                    console.log(`Full message: ${event.data}`);
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