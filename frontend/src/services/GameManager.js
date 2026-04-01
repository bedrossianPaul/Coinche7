import {useAuth} from "../useAuth";

class GameManager {
    constructor(url) {
        this.metastate = null;
        this.players = {
            NORTH: null,
            EAST: null,
            SOUTH: null,
            WEST: null,
        };
        this.state = {
            current_trick: null,
            last_trick: null,
            player_turn: null,
            bid: null,
            score: (0, 0),
        }
        this.tunnel_url = url;
        this.me = useAuth().user;
    }

    messageSerializer(type, payload) {
        return JSON.stringify({ type, payload });
    }

    messageDeserializer(message) {
        return JSON.parse(message);
    }

    _updateState_(newState) {
        this.state = newState;
    }

    _handle_request_(request) {
        console.log(`Received request from server: ${request}`);
        return;
    }

    _handle_info_(payload) {
        (info, time) = payload;
        console.log(`Received info from server: ${info} at ${time}`);
        return;
    }

    _handle_error_(payload) {        
        console.error(`Error from server: ${payload}`);
        return;
    }

    async connect() {
        this.socket = new WebSocket(this.tunnel_url);

        // Ask the server to connect us as soon as the socket is open
        this.socket.onopen = () => {
            this.socket.send(this.sendConnect());
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
        this.socket.send(this.messageSerializer("BID", { bid }));
    }

    sendConnect() {
        this.socket.send(this.messageSerializer("CONNECT", { user: this.me }));
    }
}