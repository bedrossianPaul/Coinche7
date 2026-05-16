package coinche7.game;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import coinche7.game.enums.PlacementPlayer;
import coinche7.services.WebSocketService;
import coinche7.websocket.PlayerWebSocket;

public class GameSeat {

    private HashMap<PlacementPlayer, Player> players;

    private HashMap<PlacementPlayer, PlayerWebSocket> sockets;

    public GameSeat(int gameId, WebSocketService wss, GameManager gm) {
        this.players = new HashMap<>();
        this.sockets = new HashMap<>();

        Map<PlacementPlayer, PlayerWebSocket> a = wss.createGameWebSockets(gameId, gm);
        sockets.putAll(a);

        

    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public HashMap<PlacementPlayer, Player> getPlayers() { return players; }
    public void setPlayers(HashMap<PlacementPlayer, Player> players) { this.players = players; }

    // ── Méthodes utilitaires ───────────────────────────────────────────────────

    public void add_player(Player player, PlacementPlayer pos) {
        players.put(pos, player);
    }

    public void removePlayer(PlacementPlayer pos) {
        players.remove(pos);
    }

    public PlayerWebSocket getPlayerWebSocket (PlacementPlayer pos) {
        return sockets.get(pos);
    }
    
    public Player getPlayer(PlacementPlayer pos) {
        return players.get(pos);
    }

    public PlacementPlayer getPositionOf(Player player) {
        for (Map.Entry<PlacementPlayer, Player> entry : players.entrySet()) {
            if (entry.getValue().getId() == player.getId()) return entry.getKey();
        }
        return null;
    }

    public boolean isFull() {
        return players.size() == PlacementPlayer.values().length;
    }

    public int getPlayerCount() { return players.size(); }

    public Set<PlacementPlayer> getFreeSeats() {
        Set<PlacementPlayer> freeSeats = EnumSet.allOf(PlacementPlayer.class);
        freeSeats.removeAll(players.keySet());
        return freeSeats;
    }

    public Set<PlacementPlayer> getOccupiedSeats() { return players.keySet(); }

    /** Ordre : EAST → NORTH → WEST → SOUTH → EAST. */
    public PlacementPlayer getNextPosition(PlacementPlayer current) {
        PlacementPlayer[] order = PlacementPlayer.values();
        for (int i = 0; i < order.length; i++) {
            if (order[i] == current) return order[(i + 1) % order.length];
        }
        return current;
    }

    // ── Serialize (ne pas modifier) ────────────────────────────────────────────

    /**
     * Sérialise les 4 sièges du point de vue d'un joueur donné.
     * Le viewer voit sa propre main ; les adversaires voient une main masquée.
     */
    public String serialize(PlacementPlayer viewer) {
        StringBuilder sb = new StringBuilder("{");
        PlacementPlayer[] positions = PlacementPlayer.values();
        for (int i = 0; i < positions.length; i++) {
            PlacementPlayer pos = positions[i];
            Player p = players.get(pos);
            boolean showHand = (pos == viewer);
            sb.append("\"").append(pos.name()).append("\": ")
              .append(p != null ? p.serialize(showHand) : "null");
            if (i < positions.length - 1) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    /** Serialize sans viewer — révèle toutes les mains (debug). */
    public String serialize() {
        Player east  = players.get(PlacementPlayer.EAST);
        Player north = players.get(PlacementPlayer.NORTH);
        Player west  = players.get(PlacementPlayer.WEST);
        Player south = players.get(PlacementPlayer.SOUTH);
        return "{ \"EAST\": "  + (east  != null ? east.serialize()  : "null")
             + ", \"NORTH\": " + (north != null ? north.serialize() : "null")
             + ", \"WEST\": "  + (west  != null ? west.serialize()  : "null")
             + ", \"SOUTH\": " + (south != null ? south.serialize() : "null")
             + "}";
    }
}
