package coinche7.game;

import coinche7.game.enums.PlacementPlayer;
import coinche7.model.User;
import coinche7.repository.UserRepository;

public class Player {

    private int id;
    private String name;
    private int elo = 1000;
    private String avatar;
    private Hand hand;
    private boolean is_first;
    private PlacementPlayer position;
    private int team;
    private User user;

    /**
     * Session WebSocket propre à ce joueur.
     * Assignée lors de la connexion WS dans GameWebSocketHandler.
     * Non sérialisée — usage interne uniquement.
     */

    /*
    A ETE DEPLACE DANS GAME SEATS
    private transient WebSocketSession session;
    */

    public Player() {
        this.hand = new Hand();
    }

    public Player(int userId, UserRepository userRepository) {
        this.id = userId;
        // Cherche les infos du joueur en base de données
        if (userRepository != null) {
            var userOpt = userRepository.findById((long) userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                this.name = user.getPseudo();
                this.elo = user.getElo() != null ? user.getElo() : 1000;
                this.avatar = user.getAvatarUrl();
                this.user = user;
            }
        }
        this.hand = new Hand();
        this.is_first = false;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Hand getHand() { return hand; }
    public void setHand(Hand hand) { this.hand = hand; }

    public boolean isFirst() { return is_first; }
    public void setFirst(boolean is_first) { this.is_first = is_first; }

    public PlacementPlayer getPosition() { return position; }
    public void setPosition(PlacementPlayer position) { this.position = position; }

    public int getTeam() { return team; }
    public void setTeam(int team) { this.team = team; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    /*
    public WebSocketSession getSession() { return session; }
    public void setSession(WebSocketSession session) { this.session = session; }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }
    */

    // ── Serialize (ne pas modifier) ────────────────────────────────────────────

    /**
     * Sérialise le joueur pour un observateur donné.
     * @param showHand true = révèle les cartes réelles ; false = masque la main (adversaires).
     */
    public String serialize(boolean showHand) {
        String handJson = (hand != null)
                ? (showHand ? hand.serialize() : hand.serializeHidden())
                : "[]";
        return "{ \"id\": " + id
                + ", \"name\": \"" + name + "\""
                + ", \"elo\": " + elo
                + ", \"avatar\": \"" + avatar + "\""
                + ", \"hand\": " + handJson
                + ", \"is_first\": " + is_first
                + " }";
    }

    /**
     * Serialize sans argument — révèle la main (usage interne / debug uniquement).
     * Ne pas appeler depuis broadcastGame.
     */
    public String serialize() {
        return serialize(true);
    }
}
