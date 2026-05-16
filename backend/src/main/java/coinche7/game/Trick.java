package coinche7.game;

import java.util.HashMap;

import coinche7.game.enums.PlacementPlayer;

public class Trick {

    private HashMap<PlacementPlayer, Card> cards;
    /** Position du joueur qui a ouvert ce pli (premier à jouer) */
    private PlacementPlayer leader;

    public Trick() {
        this.cards = new HashMap<>();
    }

    public Trick(PlacementPlayer leader) {
        this.cards = new HashMap<>();
        this.leader = leader;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public HashMap<PlacementPlayer, Card> getCards() { return cards; }
    public void setCards(HashMap<PlacementPlayer, Card> cards) { this.cards = cards; }

    public PlacementPlayer getLeader() { return leader; }
    public void setLeader(PlacementPlayer leader) { this.leader = leader; }

    // ── Méthodes utilitaires ───────────────────────────────────────────────────

    /** Ajoute la carte jouée par un joueur dans ce pli. */
    public void addCard(PlacementPlayer position, Card card) {
        this.cards.put(position, card);
    }

    /** Nombre de cartes déjà jouées dans ce pli (0-4). */
    public int size() {
        return this.cards.size();
    }

    /** Le pli est complet quand les 4 joueurs ont joué. */
    public boolean isComplete() {
        return this.cards.size() == 4;
    }

    /** Renvoie la carte jouée par un joueur donné (null si pas encore joué). */
    public Card getCardOf(PlacementPlayer position) {
        return this.cards.get(position);
    }

    /** Renvoie la couleur de la première carte jouée (couleur demandée). */
    public coinche7.game.enums.CardColor getLeadColor() {
        if (leader == null || !cards.containsKey(leader)) return null;
        return cards.get(leader).getColor();
    }

    // ── Serialize (ne pas modifier) ────────────────────────────────────────────

    public String serialize() {
        Card east  = this.cards.get(PlacementPlayer.EAST);
        Card north = this.cards.get(PlacementPlayer.NORTH);
        Card west  = this.cards.get(PlacementPlayer.WEST);
        Card south = this.cards.get(PlacementPlayer.SOUTH);

        return "["
                + (east  != null ? east.getCode()  : "null") + ","
                + (north != null ? north.getCode() : "null") + ","
                + (west  != null ? west.getCode()  : "null") + ","
                + (south != null ? south.getCode() : "null")
                + "]";
    }
}
