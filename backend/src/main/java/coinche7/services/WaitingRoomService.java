package coinche7.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import coinche7.game.GameManager;

@Service
public class WaitingRoomService {

    private final Map<Integer, GameManager> waitingGames = new ConcurrentHashMap<>();
    private WebSocketService wss;
    private int id_index;

    public GameManager createGame() {
        GameManager gm = new GameManager(id_index, wss);
        waitingGames.put(id_index, gm);
        id_index++;
        return gm;
    }

    public GameManager getGame(int id) {
        return waitingGames.get(id);
    }

    public Map<Integer, GameManager> getGames() {
        return waitingGames;
    }
}