package coinche7.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import coinche7.game.GameManager;
import coinche7.repository.UserRepository;
import coinche7.repository.GameArchiveRepository;

@Service
public class WaitingRoomService {

    private final Map<Integer, GameManager> waitingGames = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final WebSocketService wss;
    private final GameArchiveRepository archiveRepository;
    private int id_index;

    public WaitingRoomService(UserRepository userRepository, WebSocketService wss, GameArchiveRepository archiveRepository) {
        this.userRepository = userRepository;
        this.wss = wss;
        this.archiveRepository = archiveRepository;
    }

    public GameManager createGame() {
        GameManager gm = new GameManager(id_index, wss, userRepository, archiveRepository);
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