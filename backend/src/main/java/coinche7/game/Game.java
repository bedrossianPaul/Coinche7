package coinche7.game;

import coinche7.game.enums.GameState;
import coinche7.game.enums.PlacementPlayer;
import coinche7.services.WebSocketService;

public class Game {

    private int gameId;
    private GameState metadata;
    private GameSeat game_seats;
    private Trick current_trick;
    private Trick last_trick;
    private Player player_turn;
    private int time_remaining;
    private Bid bid;
    private GameScore score;

    public Game(int gameId, WebSocketService wss, GameManager gm) {
        this.gameId = gameId;
        this.metadata = GameState.WAITING;
        this.game_seats = new GameSeat(gameId,wss,gm);
        this.current_trick = new Trick();
        this.last_trick = new Trick();
        this.bid = new Bid();
        this.score = new GameScore();
        this.time_remaining = 0;


    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public GameState getMetadata() { return metadata; }
    public void setMetadata(GameState metadata) { this.metadata = metadata; }

    public GameSeat getGame_seats() { return game_seats; }
    public void setGame_seats(GameSeat game_seats) { this.game_seats = game_seats; }

    public Trick getCurrent_trick() { return current_trick; }
    public void setCurrent_trick(Trick current_trick) { this.current_trick = current_trick; }

    public Trick getLast_trick() { return last_trick; }
    public void setLast_trick(Trick last_trick) { this.last_trick = last_trick; }

    public Player getPlayer_turn() { return player_turn; }
    public void setPlayer_turn(Player player_turn) { this.player_turn = player_turn; }

    public int getTime_remaining() { return time_remaining; }
    public void setTime_remaining(int time_remaining) { this.time_remaining = time_remaining; }

    public Bid getBid() { return bid; }
    public void setBid(Bid bid) { this.bid = bid; }

    public GameScore getScore() { return score; }
    public void setScore(GameScore score) { this.score = score; }

    // ── Serialize (ne pas modifier) ────────────────────────────────────────────

    /**
     * Sérialise l'état du jeu du point de vue d'un joueur précis.
     * Sa main est révélée ; celles des autres sont masquées.
     */
    public String serialize(PlacementPlayer viewer) {
        return "{\"metadata\": \"" + metadata.name() + "\""
                + ", \"players\": " + game_seats.serialize(viewer)
                + ", \"current_trick\": " + current_trick.serialize()
                + ", \"last_trick\": " + last_trick.serialize()
                + ", \"player_turn\": " + (player_turn != null ? player_turn.getId() : "null")
                + ", \"time_remaining\": " + time_remaining
                + ", \"bid\": " + bid.serialize()
                + ", \"score\": " + score.serialize()
                + "}";
    }

    /** Serialize sans viewer — révèle toutes les mains (debug/console). */
    public String serialize() {
        return "{\"metadata\": \"" + metadata.name() + "\""
                + ", \"players\": " + game_seats.serialize()
                + ", \"current_trick\": " + current_trick.serialize()
                + ", \"last_trick\": " + last_trick.serialize()
                + ", \"player_turn\": " + (player_turn != null ? player_turn.getId() : "null")
                + ", \"time_remaining\": " + time_remaining
                + ", \"bid\": " + bid.serialize()
                + ", \"score\": " + score.serialize()
                + "}";
    }
}
