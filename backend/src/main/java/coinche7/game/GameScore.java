package coinche7.game;

public class GameScore {

    /** Score cumulé sur toutes les manches (équipe 1 = NORTH+SOUTH, équipe 2 = EAST+WEST) */
    private int scoreTeam1;
    private int scoreTeam2;

    /** Score de la manche en cours */
    private int roundScoreTeam1;
    private int roundScoreTeam2;

    /** Nombre de manches gagnées */
    private int winsTeam1;
    private int winsTeam2;

    /** Score cible pour remporter la partie (ex: 2000 points) */
    private int targetScore;

    public GameScore() {
        this.scoreTeam1 = 0;
        this.scoreTeam2 = 0;
        this.roundScoreTeam1 = 0;
        this.roundScoreTeam2 = 0;
        this.winsTeam1 = 0;
        this.winsTeam2 = 0;
        this.targetScore = 2000;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getScoreTeam1() { return scoreTeam1; }
    public void setScoreTeam1(int scoreTeam1) { this.scoreTeam1 = scoreTeam1; }

    public int getScoreTeam2() { return scoreTeam2; }
    public void setScoreTeam2(int scoreTeam2) { this.scoreTeam2 = scoreTeam2; }

    public int getRoundScoreTeam1() { return roundScoreTeam1; }
    public void setRoundScoreTeam1(int roundScoreTeam1) { this.roundScoreTeam1 = roundScoreTeam1; }

    public int getRoundScoreTeam2() { return roundScoreTeam2; }
    public void setRoundScoreTeam2(int roundScoreTeam2) { this.roundScoreTeam2 = roundScoreTeam2; }

    public int getWinsTeam1() { return winsTeam1; }
    public void setWinsTeam1(int winsTeam1) { this.winsTeam1 = winsTeam1; }

    public int getWinsTeam2() { return winsTeam2; }
    public void setWinsTeam2(int winsTeam2) { this.winsTeam2 = winsTeam2; }

    public int getTargetScore() { return targetScore; }
    public void setTargetScore(int targetScore) { this.targetScore = targetScore; }

    // ── Méthodes utilitaires ───────────────────────────────────────────────────

    public void addToTeam1(int points) { this.scoreTeam1 += points; }
    public void addToTeam2(int points) { this.scoreTeam2 += points; }

    public void addRoundToTeam1(int points) { this.roundScoreTeam1 += points; }
    public void addRoundToTeam2(int points) { this.roundScoreTeam2 += points; }

    /** Valide la manche : transfère les scores de manche dans le score global. */
    public void commitRound(int winnerTeam) {
        this.scoreTeam1 += this.roundScoreTeam1;
        this.scoreTeam2 += this.roundScoreTeam2;
        if (winnerTeam == 1) this.winsTeam1++;
        else this.winsTeam2++;
        this.roundScoreTeam1 = 0;
        this.roundScoreTeam2 = 0;
    }

    public boolean isGameOver() {
        return scoreTeam1 >= targetScore || scoreTeam2 >= targetScore;
    }

    /** Renvoie l'équipe gagnante (1 ou 2), ou 0 si pas encore terminé. */
    public int getWinnerTeam() {
        if (scoreTeam1 >= targetScore && scoreTeam1 > scoreTeam2) return 1;
        if (scoreTeam2 >= targetScore && scoreTeam2 > scoreTeam1) return 2;
        return 0;
    }

    // ── Serialize (ne pas modifier) ────────────────────────────────────────────

    public String serialize() {
        return "{"
                + "\"scoreTeam1\": " + scoreTeam1 + ", "
                + "\"scoreTeam2\": " + scoreTeam2 + ", "
                + "\"roundScoreTeam1\": " + roundScoreTeam1 + ", "
                + "\"roundScoreTeam2\": " + roundScoreTeam2 + ", "
                + "\"winsTeam1\": " + winsTeam1 + ", "
                + "\"winsTeam2\": " + winsTeam2
                + "}";
    }
}
