package coinche7.game;

import coinche7.game.enums.PlacementPlayer;
import coinche7.game.enums.Suit;

public class Bid {

    /** Atout choisi (null si passe) */
    private Suit suit;

    /** Nombre de points annoncés (80, 90, 100 ... 160, 250 = capot, 0 = passe) */
    private int points;

    /** Position du joueur qui a fait l'annonce */
    private PlacementPlayer bidder;

    /** Coinche ou surcoinche */
    private boolean coinched;
    private boolean surcoinched;

    public Bid() {
        this.points = 0;
        this.coinched = false;
        this.surcoinched = false;
    }

    public Bid(Suit suit, int points, PlacementPlayer bidder) {
        this.suit = suit;
        this.points = points;
        this.bidder = bidder;
        this.coinched = false;
        this.surcoinched = false;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public Suit getSuit() { return suit; }
    public void setSuit(Suit suit) { this.suit = suit; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public PlacementPlayer getBidder() { return bidder; }
    public void setBidder(PlacementPlayer bidder) { this.bidder = bidder; }

    public boolean isCoinched() { return coinched; }
    public void setCoinched(boolean coinched) { this.coinched = coinched; }

    public boolean isSurcoinched() { return surcoinched; }
    public void setSurcoinched(boolean surcoinched) { this.surcoinched = surcoinched; }

    // ── Méthodes utilitaires ───────────────────────────────────────────────────

    /** Renvoie true si une annonce a été faite (pas tous passe). */
    public boolean hasBid() {
        return suit != null && points > 0;
    }

    /** Multiplicateur lié à la coinche (x1, x2, x4). */
    public int getMultiplier() {
        if (surcoinched) return 4;
        if (coinched) return 2;
        return 1;
    }

    // ── Serialize (ne pas modifier) ────────────────────────────────────────────

    public String serialize() {
        return "{"
                + "\"suit\": " + (suit != null ? "\"" + suit.name() + "\"" : "null") + ", "
                + "\"points\": " + points + ", "
                + "\"bidder\": " + (bidder != null ? "\"" + bidder.name() + "\"" : "null") + ", "
                + "\"coinched\": " + coinched + ", "
                + "\"surcoinched\": " + surcoinched
                + "}";
    }
}
