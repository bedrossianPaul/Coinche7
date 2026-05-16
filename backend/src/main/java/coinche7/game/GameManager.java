package coinche7.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


import coinche7.game.enums.*;
import coinche7.model.User;
import coinche7.repository.UserRepository;
import coinche7.services.WebSocketService;

/**
 * GameManager_v2 — Gestionnaire d'une partie de coinche.
 *
 * Cycle de vie :
 *   WAITING → DEALING → BIDDING → PLAYING → SCORING → (nouvelle manche ou) FINISHED
 *
 * Orchestration complète :
 *   - joinGame()    : un joueur se connecte ; dès que 4 joueurs sont assis → startGame()
 *   - startGame()   : DEALING → startRound()
 *   - startRound()  : vide les mains, dealCards(), fixe le 1er joueur, passe en BIDDING
 *   - placeBid()    : gère les enchères tour par tour ;
 *                     3 passes consécutives après une annonce → endBidding()
 *                     4 passes d'affilée (aucune annonce)     → startRound() (redistribution)
 *   - endBidding()  : fixe l'attaquant, passe en PLAYING
 *   - playCard()    : le joueur courant pose une carte ;
 *                     pli complet → resolveTrick()
 *   - resolveTrick(): désigne le gagnant du pli, prépare le suivant ;
 *                     8 plis joués → endRound()
 *   - endRound()    : calcule le score, vérifie la fin de partie ou relance startRound()
 *
 * À chaque changement d'état, broadcastGame() envoie à chaque joueur
 * game.serialize(viewerPosition) : sa propre main est visible, les autres masquées.
 */
public class GameManager {


    // ── État de la partie ──────────────────────────────────────────────────────

    private final Game game;

    /** Nombre de plis joués dans la manche courante (max 8). */
    private int tricksPlayedInRound;

    private static final int TRICKS_PER_ROUND = 8;

    /**
     * Compteur de passes consécutives pendant la phase d'enchères.
     * Remis à 0 quand un joueur fait une vraie annonce.
     * → 3 passes après une annonce = fin des enchères
     * → 4 passes d'affilée = redistribution
     */
    private int consecutivePasses;

    // Outils pour la gestion du Timer
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> currentTurnTimer;
    private ScheduledFuture<?> waitingBroadcastTimer;
    // Horodatage du début du timer d'attente (ms)
    private long waitingStartMillis = 0L;
    
    // Temps alloué pour jouer (en secondes)
    private static final int TURN_TIMEOUT_SECONDS = 20;
    // Durées spécifiques par phase
    private static final int BIDDING_TIMEOUT_SECONDS = 30;
    private static final int PLAY_TIMEOUT_SECONDS = 20;

    private final String ACK = "{\"type\": \"ACK\", \"payload\": {\"action\": \"PLAY\"}}";

    // Compteurs de points accumulés pendant la manche en cours
    private int team1RoundPoints = 0;
    private int team2RoundPoints = 0;

    private final UserRepository userRepository;
    private final coinche7.repository.GameArchiveRepository archiveRepository;
    private boolean eloApplied = false;

    // ── Constructeur ───────────────────────────────────────────────────────────

    public GameManager(int gameId, WebSocketService wss, UserRepository userRepository, coinche7.repository.GameArchiveRepository archiveRepository) {
        this.game = new Game(gameId, wss,this);
        this.game.getScore().setTargetScore(80);
        this.tricksPlayedInRound = 0;
        this.consecutivePasses = 0;
        this.userRepository = userRepository;
        this.archiveRepository = archiveRepository;




    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Game getGame() { return game; }
    public int getGameId() { return game.getGameId(); }
    public GameState getState() { return game.getMetadata(); }

    public void setTargetScore(int targetScore) {
        this.game.getScore().setTargetScore(targetScore);
    }



    // ══════════════════════════════════════════════════════════════════════════
    // CONNEXION / DÉCONNEXION DES JOUEURS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Accueille un joueur, lui attribue la première place libre et attache
     * sa session WebSocket.
     *
     * Dès que les 4 places sont remplies, startGame() est appelé automatiquement.
     *
     * @return la position attribuée, ou null si la partie est pleine / déjà lancée.
     */
    public PlacementPlayer joinGame(Player player, PlacementPlayer seat) {
        if (game.getMetadata() != GameState.WAITING) return null;

        GameSeat seats = game.getGame_seats();
        if (seats.isFull()) return null;

        if (!seats.getFreeSeats().contains(seat)) {
            return null;
        }

        int team = (seat == PlacementPlayer.NORTH || seat == PlacementPlayer.SOUTH) ? 1 : 2;
        System.out.println("Player " + player.getId() + " joins as " + seat + " (Team " + team + ")");
        player.setTeam(team);
        seats.add_player(player, seat);

        send_ACK(player);

        broadcastGame(); // informe les joueurs déjà connectés qu'un nouveau vient d'arriver

        if (seats.isFull()) {
            startRound();
        }

        return seat;
    }



    // ══════════════════════════════════════════════════════════════════════════
    // LANCEMENT DE LA PARTIE
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Lance la partie.
     * Automatiquement appelé quand le 4e joueur rejoint.
     */
    /*private void startGame() {
        startRound();
    }*/

    // ══════════════════════════════════════════════════════════════════════════
    // BOUCLE DES MANCHES
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Initialise une nouvelle manche :
     *   1. Remet les compteurs à zéro
     *   2. Vide les mains
     *   3. Distribue 8 cartes à chaque joueur
     *   4. Fixe le premier joueur (SOUTH à la première manche, sinon conservé)
     *   5. Passe en BIDDING
     */
    private void startRound() {
        cancelWaitingBroadcastTimer();
        game.setMetadata(GameState.DEALING);
        broadcastGame();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        team1RoundPoints = 0;
        team2RoundPoints = 0;
        tricksPlayedInRound = 0;
        consecutivePasses = 0;

        game.setBid(new Bid());
        game.setCurrent_trick(new Trick());
        game.setLast_trick(new Trick());

        // Vide les mains
        for (PlacementPlayer pos : PlacementPlayer.values()) {
            Player p = game.getGame_seats().getPlayer(pos);
            if (p != null) p.setHand(new Hand());
        }

        // Distribution
        dealCards();

        // Premier joueur : SOUTH par défaut, sinon le gagnant du dernier pli (conservé dans player_turn)
        if (game.getPlayer_turn() == null) {
            Player south = game.getGame_seats().getPlayer(PlacementPlayer.SOUTH);
            game.setPlayer_turn(south);
        }
        // Réinitialise le drapeau is_first
        for (PlacementPlayer pos : PlacementPlayer.values()) {
            Player p = game.getGame_seats().getPlayer(pos);
            if (p != null) p.setFirst(p.getId() == game.getPlayer_turn().getId());
        }

        game.setMetadata(GameState.BIDDING);
        broadcastGame();

        Player p = game.getPlayer_turn();
        ask_bid(p);

    }

    // ══════════════════════════════════════════════════════════════════════════
    // DISTRIBUTION DES CARTES
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Crée un jeu de 32 cartes, le mélange et distribue 8 cartes par joueur.
     * TODO : distribution réelle avec coupe (3+2+3 ou 3+3+2).
     */
    private void dealCards() {
        List<Card> deck = buildDeck();
        Collections.shuffle(deck);

        int cardIndex = 0;
        for (PlacementPlayer pos : PlacementPlayer.values()) {
            Player player = game.getGame_seats().getPlayer(pos);
            if (player == null) continue;
            for (int i = 0; i < 8; i++) {
                player.getHand().addCard(deck.get(cardIndex++));
            }
        }
        // Pas de broadcastGame() ici : appelé juste après dans startRound()
    }

    private List<Card> buildDeck() {
        List<Card> deck = new ArrayList<>();
        for (CardColor color : CardColor.values()) {
            for (CardValue value : CardValue.values()) {
                deck.add(new Card(value, color));
            }
        }
        return deck;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ENCHÈRES
    // ══════════════════════════════════════════════════════════════════════════



    private void ask_bid(Player p){

        scheduleWaitingBroadcast(GameState.BIDDING, p);

        String json = "{\"type\": \"REQUEST\", \"payload\": {\"action\": \"BID\"}}";
        try {
            GameSeat seats = game.getGame_seats();
            seats.getPlayerWebSocket(p.getPosition()).broadcast(json);                
        } catch (Exception e) {
                System.out.println(e);
        }
        currentTurnTimer = scheduler.schedule(() -> {
        // Le mot-clé synchronized évite que le timer et une action joueur simultanée n'entrent en conflit
        synchronized (this) {
            // Sécurité : On vérifie que c'est toujours la phase d'enchères et le tour de ce joueur
            if (game.getMetadata() == GameState.BIDDING && isPlayerTurn(p)) {
                System.out.println("Time out ! Le joueur " + p.getPosition() + " passe automatiquement.");
                placeBid(p, null, 0); // Force la passe
                }
            }
        }, BIDDING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }





    /**
     * Reçoit l'action d'un joueur pendant les enchères (annonce ou passe).
     *
     * Règles de fin d'enchères :
     *   • 4 passes d'affilée dès le départ         → redistribution (startRound)
     *   • 3 passes consécutives après une annonce  → endBidding()
     *
     * @param player  le joueur qui parle
     * @param suit    l'atout choisi (null = passe)
     * @param points  la mise (0 = passe ; 80-250 sinon)
     * @return true si l'action est acceptée
     */
    public synchronized boolean placeBid(Player player, Suit suit, int points) {
        if (game.getMetadata() != GameState.BIDDING) return false;
        if (!isPlayerTurn(player)) return false;

        cancelWaitingBroadcastTimer();

        Bid currentBid = game.getBid();
        boolean isPass = (suit == null || points == 0);

        if (!isPass) {
            // Validation de l'annonce
            if (points < 80 || points > 250 || points % 10 != 0) return false;
            if (currentBid.hasBid() && points <= currentBid.getPoints()) return false;

            cancelCurrentTimer();

            // Enregistre la nouvelle meilleure enchère
            currentBid.setSuit(suit);
            currentBid.setPoints(points);
            currentBid.setBidder(player);
            consecutivePasses = 0; // remise à zéro : le compteur repart de cette annonce

        } else {
            // Passe
            consecutivePasses++;
            cancelCurrentTimer();
        }

        send_ACK(player);

        advanceTurn();
        broadcastGame();

        // ── Décision de fin d'enchères ─────────────────────────────────────
        if (!currentBid.hasBid()) {
            // Aucune annonce encore faite
            if (consecutivePasses >= 4) {
                // Tout le monde a passé → redistribution
                startRound();
            } else {
                ask_bid(game.getPlayer_turn());
            }
            // sinon on continue le tour
        } else {
            // Une annonce a été faite
            if (consecutivePasses >= 3) {
                // 3 passes après la dernière annonce → l'enchère est validée
                endBidding();
            } else {
                ask_bid(game.getPlayer_turn());
            }
            // sinon on continue le tour
        }

        return true;
    }

    /**
     * Coinche ou surcoinche l'annonce en cours.
     * Seul un adversaire de l'annonceur peut coincher.
     * La coinche ne fait pas avancer le tour ; le joueur courant joue ensuite normalement.
     */
    public boolean coinche(Player player) {
        if (game.getMetadata() != GameState.BIDDING) return false;
        Bid bid = game.getBid();
        if (!bid.hasBid()) return false;

        PlacementPlayer bidderPos = bid.getBidder().getPosition();
        PlacementPlayer playerPos = game.getGame_seats().getPositionOf(player);
        if (sameTeam(bidderPos, playerPos)) return false;

        if (!bid.isCoinched()) {
            bid.setCoinched(true);
        } else if (!bid.isSurcoinched()) {
            bid.setSurcoinched(true);
        } else {
            return false;
        }

        broadcastGame();
        return true;
    }

    /**
     * Clôture les enchères et lance la phase de jeu.
     * Le premier joueur du jeu est l'annonceur.
     */
    private void endBidding() {
        game.setMetadata(GameState.PLAYING);
        broadcastGame();
        cancelWaitingBroadcastTimer();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Bid bid = game.getBid();
        // (hasBid() est garanti vrai ici — appelé uniquement depuis placeBid après 3 passes)

        PlacementPlayer bidderPos = bid.getBidder().getPosition();
        Player bidder = game.getGame_seats().getPlayer(bidderPos);
        game.setPlayer_turn(bidder);

        game.setCurrent_trick(new Trick(bidderPos));
        broadcastGame();

        ask_card(bidder);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // JEU DES PLIS
    // ══════════════════════════════════════════════════════════════════════════


    private void ask_card(Player p){

        scheduleWaitingBroadcast(GameState.PLAYING, p);

        cancelCurrentTimer();

        String json = "{\"type\": \"REQUEST\", \"payload\": {\"action\": \"PLAY\"}}";
        try {
            GameSeat seats = game.getGame_seats();
            seats.getPlayerWebSocket(p.getPosition()).broadcast(json);                
        } catch (Exception e) {
                System.out.println(e);
        }
        currentTurnTimer = scheduler.schedule(() -> {
        // Le mot-clé synchronized évite que le timer et une action joueur simultanée n'entrent en conflit
        synchronized (this) {
            // Sécurité : On vérifie que c'est toujours la phase d'enchères et le tour de ce joueur
            if (game.getMetadata() == GameState.PLAYING && isPlayerTurn(p)) {
                System.out.println("Time out ! Le joueur " + p.getPosition() + " passe automatiquement.");
                for (Card card : p.getHand().getCards()){
                    if (isCardPlayable(p, card)){
                        playCard(p,card); // Force la passe
                        break;
                        }
                    }
                }
            }
        }, PLAY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }








    /**
     * Le joueur courant pose une carte.
     *
     * @return true si le coup est valide et a été joué.
     */
    public synchronized boolean playCard(Player player, Card card) {
        if (game.getMetadata() != GameState.PLAYING) return false;
        if (!isPlayerTurn(player)) return false;
        if (!player.getHand().contains(card)) return false;

        cancelWaitingBroadcastTimer();

        if (!isCardPlayable(player, card)) return false;

        cancelCurrentTimer();

        player.getHand().playCard(card);
        PlacementPlayer pos = game.getGame_seats().getPositionOf(player);
        game.getCurrent_trick().addCard(pos, card);

        send_ACK(player);

        if (game.getCurrent_trick().isComplete()) {
            broadcastGame(); // montre le pli complet avant de le résoudre
            resolveTrick();
        } else {
            advanceTurn();
            broadcastGame();
            ask_card(game.getPlayer_turn());
        }

        return true;
    }


    /**
     * Validation complète des règles de la coinche pour la jouabilité d'une carte.
     */
    private boolean isCardPlayable(Player player, Card card) {

        if (!player.getHand().contains(card)){
            return false;
        }

        Trick trick = game.getCurrent_trick();
        Suit contract = game.getBid().getSuit();

        // 1. Le premier joueur du pli peut jouer ce qu'il veut
        if (trick.getCards().isEmpty()) {
            return true;
        }

        CardColor leadColor = trick.getLeadColor();
        CardColor trumpColor = getTrumpColorFromSuit(contract);

        // Vérification de la présence de la couleur demandée dans la main
        boolean hasLeadColor = player.getHand().getCards().stream()
                .anyMatch(c -> c.getColor() == leadColor);

        // ── CAS 1 : Le joueur a la couleur demandée (COL) ────────────────────────
        if (hasLeadColor) {
            // Il doit obligatoirement fournir la couleur
            if (card.getColor() != leadColor) return false;

            // Règle du "monter à l'atout" : s'applique si COL est l'atout OU si on est en Tout Atout
            boolean mustRise = (contract == Suit.ALL_TRUMP) || (contract != Suit.NO_TRUMP && leadColor == trumpColor);
            if (mustRise) {
                int highestLeadInTrick = trick.getCards().values().stream()
                        .filter(c -> c.getColor() == leadColor)
                        .mapToInt(c -> getTrumpPower(c.getValue()))
                        .max().orElse(-1);

                boolean canRise = player.getHand().getCards().stream()
                        .filter(c -> c.getColor() == leadColor)
                        .anyMatch(c -> getTrumpPower(c.getValue()) > highestLeadInTrick);

                // S'il peut monter, il doit obligatoirement le faire
                if (canRise) {
                    return getTrumpPower(card.getValue()) > highestLeadInTrick;
                }
            }
            return true;
        }

        // ── CAS 2 : Le joueur n'a PAS la couleur demandée (COL) ──────────────────
        else {
            PlacementPlayer masterPos = determineTrickWinner(trick);
            boolean partnerIsMaster = sameTeam(game.getGame_seats().getPositionOf(player), masterPos);

            // Si le partenaire est maître, on pisse/défausse ce qu'on veut (sauf en Sans/Tout Atout où on n'a pas le choix de toute façon)
            if (partnerIsMaster || contract == Suit.NO_TRUMP || contract == Suit.ALL_TRUMP) {
                return true;
            }

            // Si l'adversaire est maître, on doit couper à l'atout si on en a
            boolean hasTrump = player.getHand().getCards().stream()
                    .anyMatch(c -> c.getColor() == trumpColor);

            if (hasTrump) {
                // Obligation de jouer un atout
                if (card.getColor() != trumpColor) return false;

                // S'il y a déjà de l'atout dans le pli, on doit monter si on peut
                int highestTrumpInTrick = trick.getCards().values().stream()
                        .filter(c -> c.getColor() == trumpColor)
                        .mapToInt(c -> getTrumpPower(c.getValue()))
                        .max().orElse(-1);

                boolean canOvertrump = player.getHand().getCards().stream()
                        .filter(c -> c.getColor() == trumpColor)
                        .anyMatch(c -> getTrumpPower(c.getValue()) > highestTrumpInTrick);

                if (canOvertrump) {
                    return getTrumpPower(card.getValue()) > highestTrumpInTrick;
                }
                return true; // Sous-couper ("pisser") est autorisé si on ne peut pas monter
            }

            // Pas de couleur demandée, pas d'atout -> Défausse libre
            return true;
        }
    }

    // ── MÉTHODES LOGIQUES ET PUISSANCES ──────────────────────────────────────────

    private int getTrumpPower(CardValue value) {
        switch (value) {
            case JACK:  return 7;
            case NINE:  return 6;
            case ACE:   return 5;
            case TEN:   return 4;
            case KING:  return 3;
            case QUEEN: return 2;
            case EIGHT: return 1;
            case SEVEN: return 0;
            default: return -1;
        }
    }

    private int getNonTrumpPower(CardValue value) {
        switch (value) {
            case ACE:   return 7;
            case TEN:   return 6;
            case KING:  return 5;
            case QUEEN: return 4;
            case JACK:  return 3;
            case NINE:  return 2;
            case EIGHT: return 1;
            case SEVEN: return 0;
            default: return -1;
        }
    }

    /**
     * Calcule la force absolue d'une carte dans le pli pour désigner le maître.
     */
    private int getCardAbsoluteStrength(Card card, CardColor leadColor, Suit contract) {
        CardColor trumpColor = getTrumpColorFromSuit(contract);

        if (contract == Suit.NO_TRUMP) {
            if (card.getColor() != leadColor) return -1;
            return getNonTrumpPower(card.getValue());
        }
        if (contract == Suit.ALL_TRUMP) {
            if (card.getColor() != leadColor) return -1;
            return getTrumpPower(card.getValue());
        }
        // Contrat à la couleur classique
        if (card.getColor() == trumpColor) {
            return 100 + getTrumpPower(card.getValue()); // L'atout l'emporte toujours sur le reste
        } else if (card.getColor() == leadColor) {
            return getNonTrumpPower(card.getValue());
        }
        return -1; // Défausse hors couleur
    }

    private CardColor getTrumpColorFromSuit(Suit suit) {
        if (suit == null) return null;
        switch (suit) {
            case HEARTS:   return CardColor.HEART;
            case DIAMONDS: return CardColor.DIAMOND;
            case SPADES:   return CardColor.SPADE;
            case CLUBS:    return CardColor.CLUB;
            default:      return null; // SANS_ATOUT ou TOUT_ATOUT
        }
    }














    /**
     * Résout le pli complet :
     *   1. Détermine le gagnant
     *   2. Archive le pli dans last_trick
     *   3. Prépare current_trick pour le pli suivant
     *   4. Si 8 plis joués → endRound()
     */
    private void resolveTrick() {
        Trick trick = game.getCurrent_trick();
        PlacementPlayer winner = determineTrickWinner(trick);

        // ── CALCUL DES POINTS DU PLI ──────────────────────────────────────────
        int trickPoints = 0;
        for (Card card : trick.getCards().values()) {
            trickPoints += getCardPoints(card, game.getBid().getSuit());
        }

        tricksPlayedInRound++;

        // Règle du "Dix de Der" : Si c'est le 8ème et dernier pli
        if (tricksPlayedInRound >= TRICKS_PER_ROUND) {
            trickPoints += 10;
        }

        // Attribution des points à la bonne équipe
        boolean isTeam1 = (winner == PlacementPlayer.NORTH || winner == PlacementPlayer.SOUTH);
        if (isTeam1) {
            team1RoundPoints += trickPoints;
        } else {
            team2RoundPoints += trickPoints;
        }



        // Le gagnant du pli ouvre le suivant
        Player winnerPlayer = game.getGame_seats().getPlayer(winner);
        for (PlacementPlayer pos : PlacementPlayer.values()) {
            Player p = game.getGame_seats().getPlayer(pos);
            if (p != null) p.setFirst(p.getId() == winnerPlayer.getId());
        }
        game.setPlayer_turn(winnerPlayer);

        game.setLast_trick(trick);
        game.setCurrent_trick(new Trick(winner));

        broadcastGame();

        if (tricksPlayedInRound >= TRICKS_PER_ROUND) {
            endRound();
        } else {
            ask_card(winnerPlayer);
        }   
    }



    private PlacementPlayer determineTrickWinner(Trick trick) {
    CardColor leadColor = trick.getLeadColor();
    Suit contract = game.getBid().getSuit();
    PlacementPlayer winner = null;
    int highestStrength = -2;

    for (Map.Entry<PlacementPlayer, Card> entry : trick.getCards().entrySet()) {
        int strength = getCardAbsoluteStrength(entry.getValue(), leadColor, contract);
        if (strength > highestStrength) {
            highestStrength = strength;
            winner = entry.getKey();
        }
    }
    trick.setLeader(winner);
    return winner;
}

    // ══════════════════════════════════════════════════════════════════════════
    // FIN DE MANCHE ET SCORE
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Calcule le score de la manche, met à jour GameScore_v2.
     * Lance une nouvelle manche ou termine la partie.
     */
    private void endRound() {
        game.setMetadata(GameState.SCORING);
        broadcastGame();

        computeRoundScore();

        if (game.getScore().isGameOver()) {
            applyGameEloRewards();
            game.setMetadata(GameState.FINISHED);
            broadcastGame();
            return;
        }

        startRound();
    }


    private int getCardPoints(Card card, Suit contract) {
    CardValue value = card.getValue();
    CardColor trumpColor = getTrumpColorFromSuit(contract);

    if (contract == Suit.NO_TRUMP) {
        switch (value) {
            case ACE:   return 19;
            case TEN:   return 10;
            case KING:  return 4;
            case QUEEN: return 3;
            case JACK:  return 2;
            default:    return 0;
        }
    }
    if (contract == Suit.ALL_TRUMP) {
        switch (value) {
            case JACK:  return 14;
            case NINE:  return 9;
            case ACE:   return 6;
            case TEN:   return 5;
            case KING:  return 3;
            case QUEEN: return 2;
            default:    return 0;
        }
    }
    // Contrat classique
    if (card.getColor() == trumpColor) {
        switch (value) {
            case JACK:  return 20;
            case NINE:  return 14;
            case ACE:   return 11;
            case TEN:   return 10;
            case KING:  return 4;
            case QUEEN: return 3;
            default:    return 0;
        }
    } else {
        switch (value) {
            case ACE:   return 11;
            case TEN:   return 10;
            case KING:  return 4;
            case QUEEN: return 3;
            case JACK:  return 2;
            default:    return 0;
        }
    }
}
    
    private void computeRoundScore() {
        // Soumission des scores calculés dynamiquement au gestionnaire de score global
        int score_bid = game.getBid().getPoints();
        boolean isContractFulfilled = false;
        boolean team1IsAttacking = (game.getBid().getBidder().getPosition() == PlacementPlayer.NORTH || game.getBid().getBidder().getPosition() == PlacementPlayer.SOUTH);
        // Bonus de coinche/surcoinche
        if (game.getBid().isCoinched()) {
            score_bid *= 2;
        }
        if (game.getBid().isSurcoinched()) {
                score_bid *= 2;
        }

        if (team1IsAttacking) {
            isContractFulfilled = (team1RoundPoints >= score_bid);
            if (isContractFulfilled) {
                game.getScore().addRoundToTeam1(score_bid); // L'équipe attaquante marque ses points + la valeur du contrat
            } else {
                game.getScore().addRoundToTeam2(score_bid); // L'équipe défenseur marque la valeur du contrat en cas d'échec de l'attaque
            }
        } else {
            isContractFulfilled = (team2RoundPoints >= score_bid);
            if (isContractFulfilled) {
                game.getScore().addRoundToTeam2(score_bid); // L'équipe attaquante marque ses points + la valeur du contrat
            } else {
                game.getScore().addRoundToTeam1(score_bid); // L'équipe défenseur marque la valeur du contrat en cas d'échec de l'attaque
            }
        }
        game.getScore().commitRound(1); 
    }

    private void applyGameEloRewards() {
        if (eloApplied || userRepository == null) {
            return;
        }

        int winnerTeam = game.getScore().getWinnerTeam();
        if (winnerTeam == 0) {
            return;
        }

        List<Player> team1Players = new ArrayList<>();
        List<Player> team2Players = new ArrayList<>();
        for (PlacementPlayer pos : PlacementPlayer.values()) {
            Player player = game.getGame_seats().getPlayer(pos);
            if (player == null || player.getUser() == null) {
                continue;
            }
            if (pos == PlacementPlayer.NORTH || pos == PlacementPlayer.SOUTH) {
                team1Players.add(player);
            } else {
                team2Players.add(player);
            }
        }

        if (team1Players.isEmpty() || team2Players.isEmpty()) {
            return;
        }

        int avgTeam1 = averageElo(team1Players);
        int avgTeam2 = averageElo(team2Players);
        int gap = Math.abs(avgTeam1 - avgTeam2);
        int delta = Math.max(1, 10 - (gap / 100));

        List<User> usersToSave = new ArrayList<>();

        if (winnerTeam == 1) {
            adjustTeamElo(team1Players, delta, usersToSave);
            adjustTeamElo(team2Players, -delta, usersToSave);
        } else {
            adjustTeamElo(team2Players, delta, usersToSave);
            adjustTeamElo(team1Players, -delta, usersToSave);
        }

        if (!usersToSave.isEmpty()) {
            userRepository.saveAll(usersToSave);
        }

        eloApplied = true;
        // Après application des ELO, archive la partie
        try {
            coinche7.model.GameArchive archive = new coinche7.model.GameArchive();
            archive.setFinishedAt(System.currentTimeMillis());
            archive.setWinnerTeam(winnerTeam);
            archive.setTeam1Score(game.getScore().getScoreTeam1());
            archive.setTeam2Score(game.getScore().getScoreTeam2());
            archive.setTargetScore(game.getScore().getTargetScore());

            java.util.List<coinche7.model.User> t1Players = new java.util.ArrayList<>();
            java.util.List<coinche7.model.User> t2Players = new java.util.ArrayList<>();
            for (Player player : team1Players) {
                if (player != null && player.getUser() != null) {
                    t1Players.add(player.getUser());
                }
            }
            for (Player player : team2Players) {
                if (player != null && player.getUser() != null) {
                    t2Players.add(player.getUser());
                }
            }
            archive.setTeam1Players(t1Players);
            archive.setTeam2Players(t2Players);
            if (archiveRepository != null) {
                archiveRepository.save(archive);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int averageElo(List<Player> players) {
        int sum = 0;
        for (Player player : players) {
            User user = player.getUser();
            int elo = user.getElo() != null ? user.getElo() : 1000;
            sum += elo;
        }
        return sum / players.size();
    }

    private void adjustTeamElo(List<Player> players, int delta, List<User> usersToSave) {
        for (Player player : players) {
            User user = player.getUser();
            if (user == null) {
                continue;
            }

            int currentElo = user.getElo() != null ? user.getElo() : 1000;
            int updatedElo = Math.max(900, currentElo + delta);
            user.setElo(updatedElo);
            player.setElo(updatedElo);
            usersToSave.add(user);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BROADCAST
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Envoie à chaque joueur connecté le JSON de la partie depuis son point de vue :
     *   - sa propre main est visible
     *   - les mains des adversaires sont masquées (count seulement)
     *
     * Méthode publique pour que GameWebSocketHandler puisse l'appeler (broadcastFromConsole).
     */
    public void broadcastGame() {
        GameSeat seats = game.getGame_seats();
        for (PlacementPlayer pos : PlacementPlayer.values()) {
            String json = "{\"type\": \"STATE\", \"payload\":" + game.serialize(pos) + "}";
            try {
                seats.getPlayerWebSocket(pos).broadcast(json);                
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
    

    // ══════════════════════════════════════════════════════════════════════════
    // UTILITAIRES INTERNES
    // ══════════════════════════════════════════════════════════════════════════
    
    private boolean isPlayerTurn(Player player) {
        Player current = game.getPlayer_turn();
        return current != null && current.getId() == player.getId();
    }
    
    private void advanceTurn() {
        PlacementPlayer currentPos = game.getGame_seats().getPositionOf(game.getPlayer_turn());
        if (currentPos == null) return;
        PlacementPlayer nextPos = game.getGame_seats().getNextPosition(currentPos);
        game.setPlayer_turn(game.getGame_seats().getPlayer(nextPos));
    }
    
    private boolean sameTeam(PlacementPlayer a, PlacementPlayer b) {
        boolean team1a = (a == PlacementPlayer.NORTH || a == PlacementPlayer.SOUTH);
        boolean team1b = (b == PlacementPlayer.NORTH || b == PlacementPlayer.SOUTH);
        return team1a == team1b;
    }

    private void cancelCurrentTimer() {
    if (currentTurnTimer != null && !currentTurnTimer.isDone()) {
        currentTurnTimer.cancel(false); // false pour ne pas interrompre brutalement si la tâche a déjà commencé
    }
    }

    private void cancelWaitingBroadcastTimer() {
        if (waitingBroadcastTimer != null && !waitingBroadcastTimer.isDone()) {
            waitingBroadcastTimer.cancel(false);
        }
        // Réinitialise l'horodatage et le compteur affiché
        waitingStartMillis = 0L;
        game.setTime_remaining(0);
        
    }

    private void scheduleWaitingBroadcast(GameState expectedState, Player expectedPlayer) {
        cancelWaitingBroadcastTimer();
        int initialRemaining = (expectedState == GameState.BIDDING) ? BIDDING_TIMEOUT_SECONDS
                : (expectedState == GameState.PLAYING) ? PLAY_TIMEOUT_SECONDS
                : TURN_TIMEOUT_SECONDS;

        // Mémorise le début du timer et envoie la première valeur
        waitingStartMillis = System.currentTimeMillis();
        game.setTime_remaining(initialRemaining);
        broadcastGame();

        waitingBroadcastTimer = scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                if (game.getMetadata() != expectedState || !isPlayerTurn(expectedPlayer)) {
                    cancelWaitingBroadcastTimer();
                    return;
                }

                long elapsedSec = (System.currentTimeMillis() - waitingStartMillis) / 1000L;
                int remaining = (int) Math.max(0, initialRemaining - elapsedSec);
                game.setTime_remaining(remaining);

                // Si temps écoulé, annule le timer local (le currentTurnTimer traitera l'action)
                if (remaining <= 0) {
                    cancelWaitingBroadcastTimer();
                }

                broadcastGame();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void send_ACK(Player p){
        GameSeat seats = game.getGame_seats();
        try {
            seats.getPlayerWebSocket(p.getPosition()).broadcast(ACK);                
        } catch (Exception e) {
            System.out.println(e);
            }
        }
}