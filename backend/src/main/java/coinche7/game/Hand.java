package coinche7.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hand {

    private List<Card> cards;

    public Hand() {
        this.cards = new ArrayList<>();
    }

    public Hand(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public List<Card> getCards() { return Collections.unmodifiableList(cards); }
    public void setCards(List<Card> cards) { this.cards = new ArrayList<>(cards); }

    // ── Méthodes utilitaires ───────────────────────────────────────────────────

    public void addCard(Card card) { this.cards.add(card); }

    public boolean playCard(Card card) { return this.cards.remove(card); }

    public int size() { return this.cards.size(); }

    public boolean isEmpty() { return this.cards.isEmpty(); }

    public boolean contains(Card card) { return this.cards.contains(card); }

    // ── Serialize (ne pas modifier) ────────────────────────────────────────────

    /** Sérialise la main complète (cartes visibles). */
    public String serialize() {
        if (cards.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            sb.append("\"").append(card.getCode()).append("\"").append(", ");
        }
        sb.setLength(sb.length() - 2);
        return "[" + sb + "]";
    }

    /**
     * Sérialise la main masquée : envoie uniquement le nombre de cartes,
     * sans révéler lesquelles (pour les adversaires).
     * Format : {"hidden": true, "count": N}
     */
    public String serializeHidden() {
        return "{\"hidden\": true, \"count\": " + cards.size() + "}";
    }
}
