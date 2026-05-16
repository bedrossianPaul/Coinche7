package coinche7.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import coinche7.game.Card;
import coinche7.game.GameManager;
import coinche7.game.Player;
import coinche7.game.enums.PlacementPlayer;
import coinche7.game.enums.Suit;
import io.micrometer.common.lang.NonNull;

public class PlayerWebSocket extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    private GameManager gm;

    private Player player;

    private PlacementPlayer pp;

    private String url;

    public PlayerWebSocket(GameManager gm, PlacementPlayer pp, String url){
        this.gm = gm;
        this.pp = pp;
        this.url = url;
    }

    @SuppressWarnings("null")
    public void broadcast(@NonNull String message) throws IOException {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    @SuppressWarnings("null")
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
        System.out.println("New connection: " + session.getId() + " for player " + pp);
    }

    @SuppressWarnings("null")
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        sessions.remove(session);
    }

    @SuppressWarnings("null")
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        String payload = message.getPayload().trim();
        System.out.println("Received message from player " + pp + ": " + payload);
        try {
            String type = extractString(payload, "type");
            if (type == null) {
                //sendError(session, "Champ 'action' manquant");
                return;
            }
            switch (type.toUpperCase()) {

                case "CONNECT" ->   {
                                    String userJson = extractObject(payload, "user");
                                    int playerId = extractInt(userJson, "id", -1);
                                    player = new Player(playerId); 
                                    gm.joinGame(player,pp);

                }

                case "BID" ->       {
                                    String bidPayload = extractObject(payload, "payload");
                                    String action = extractString(bidPayload, "action");

                                    if (action == null) {
                                        System.out.println("BID message missing action: " + payload);
                                        return;
                                    }

                                    switch(action.toUpperCase()){

                                        case "PASS" -> gm.placeBid(player, null, 0);

                                        case "COINCHE","SURCOINCHE" -> gm.coinche(player);

                                        case "ANNOUNCE" -> {
                                            String suit = extractString(bidPayload, "type");

                                            Suit s = null;

                                            if (suit == null) {
                                                return;
                                            }

                                            if ("S".equals(suit)){
                                                s = Suit.SPADES;
                                            } else if ("H".equals(suit)){
                                                s = Suit.HEARTS;
                                            } else if ("C".equals(suit)){
                                                s = Suit.CLUBS;
                                            } else if ("D".equals(suit)){
                                                s = Suit.DIAMONDS;
                                            } else if ("SA".equals(suit)){
                                                s = Suit.NO_TRUMP;
                                            } else if ("TA".equals(suit)){
                                                s = Suit.ALL_TRUMP;
                                            } else {
                                                return;
                                            }

                                            int points = extractInt(bidPayload, "points", -1);

                                            gm.placeBid(player, s, points);
                                            
                                        }

                                    }

                }

                case "ACTION" -> {
                                    String code = extractString(payload, "card");
                                    Card card = parseCard(code);
                                    gm.playCard(player, card);
                }

            }



        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void onTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        handleTextMessage(session, message);
    }


        // ── Parsers JSON minimalistes (sans dépendance externe) ───────────────────

    /**
     * Extrait la valeur d'une clé string dans un JSON plat :
     *   {"action": "JOIN", "gameId": "abc"} → extractString(json, "gameId") = "abc"
     */
    private String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int start = json.indexOf('"', colon + 1);
        if (start < 0) return null;
        int end = json.indexOf('"', start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }

    /**
     * Extrait la valeur d'une clé numérique dans un JSON plat.
     */
    private int extractInt(String json, String key, int defaultValue) {
        if (json == null) return defaultValue;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return defaultValue;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return defaultValue;
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Extrait un objet JSON enfant (non-string), par ex:
     *   extractObject('{"payload":{"user":{"id":4}}}', "user") -> '{"id":4}'
     */
    private String extractObject(String json, String key) {
        if (json == null) return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;

        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length() || json.charAt(start) != '{') return null;

        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return json.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    /**
     * Parse un code de carte (ex: "9H", "JS", "10D") en Card_v2.
     * Format : valeur (7 8 9 10 J Q K A) + couleur (H C S D).
     */
    private coinche7.game.Card parseCard(String code) {
        if (code == null || code.length() < 2) return null;
        try {
            String valueStr;
            String colorStr;
            if (code.startsWith("10")) {
                valueStr = "10";
                colorStr = code.substring(2);
            } else {
                valueStr = code.substring(0, 1);
                colorStr = code.substring(1);
            }

            coinche7.game.enums.CardValue value = switch (valueStr) {
                case "7"  -> coinche7.game.enums.CardValue.SEVEN;
                case "8"  -> coinche7.game.enums.CardValue.EIGHT;
                case "9"  -> coinche7.game.enums.CardValue.NINE;
                case "10" -> coinche7.game.enums.CardValue.TEN;
                case "J"  -> coinche7.game.enums.CardValue.JACK;
                case "Q"  -> coinche7.game.enums.CardValue.QUEEN;
                case "K"  -> coinche7.game.enums.CardValue.KING;
                case "A"  -> coinche7.game.enums.CardValue.ACE;
                default   -> null;
            };
            coinche7.game.enums.CardColor color = switch (colorStr.toUpperCase()) {
                case "H" -> coinche7.game.enums.CardColor.HEART;
                case "C" -> coinche7.game.enums.CardColor.CLUB;
                case "S" -> coinche7.game.enums.CardColor.SPADE;
                case "D" -> coinche7.game.enums.CardColor.DIAMOND;
                default  -> null;
            };
            if (value == null || color == null) return null;
            return new coinche7.game.Card(value, color);
        } catch (Exception e) {
            return null;
        }
    }

    public String getUrl() {
        return url;
    }
}
