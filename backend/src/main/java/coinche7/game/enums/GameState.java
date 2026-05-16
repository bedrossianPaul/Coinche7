package coinche7.game.enums;

public enum GameState {
    WAITING,   // waiting for 4 players to connect
    DEALING,   // server distributes cards (instant, no player input)
    BIDDING,   // waiting for each player's bid in turn
    PLAYING,   // waiting for the current player to play a card
    SCORING,   // server computes round score (instant)
    FINISHED   // game over
}