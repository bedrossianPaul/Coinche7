package coinche7.game;

import coinche7.game.enums.CardColor;
import coinche7.game.enums.CardValue;

public class Card {

    private CardValue v;
    private CardColor c;

    public Card(CardValue v, CardColor c) {
        this.v = v;
        this.c = c;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public CardValue getValue() { return v; }
    public void setValue(CardValue v) { this.v = v; }

    public CardColor getColor() { return c; }
    public void setColor(CardColor c) { this.c = c; }

    // ── Code de carte (ne pas modifier : utilisé dans serialize des autres classes) ──

    public String getCode() {

        String label = "";

        if (v == CardValue.SEVEN)       { label = "7"; }
        else if (v == CardValue.EIGHT)  { label = "8"; }
        else if (v == CardValue.NINE)   { label = "9"; }
        else if (v == CardValue.TEN)    { label = "10"; }
        else if (v == CardValue.JACK)   { label = "J"; }
        else if (v == CardValue.QUEEN)  { label = "Q"; }
        else if (v == CardValue.KING)   { label = "K"; }
        else if (v == CardValue.ACE)    { label = "A"; }
        else { System.err.println("Valeur de la carte pas bonne"); }

        if (c == CardColor.HEART)        { label += "H"; }
        else if (c == CardColor.CLUB)    { label += "C"; }
        else if (c == CardColor.SPADE)   { label += "S"; }
        else if (c == CardColor.DIAMOND) { label += "D"; }
        else { System.err.println("Couleur de la carte pas bonne"); }

        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return v == card.v && c == card.c;
    }

    @Override
    public int hashCode() {
        return 31 * (v != null ? v.hashCode() : 0) + (c != null ? c.hashCode() : 0);
    }
}
