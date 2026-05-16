package coinche7.controller;

import java.util.HashMap;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import coinche7.game.GameManager;
import coinche7.game.enums.PlacementPlayer;
import coinche7.services.WebSocketService;


@RestController
@RequestMapping("/api/waiting-room")
public class WaitingRoomController {

    private final HashMap<Integer, GameManager> waiting_games = new HashMap<>();
    private final WebSocketService wss;
    private int id_index;

    public WaitingRoomController(WebSocketService wss) {
        this.wss = wss;
        this.id_index = 0;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGame() {
        
        GameManager gameManager = new GameManager(id_index, wss);
        waiting_games.put(gameManager.getGameId(), gameManager);

        HashMap<String, Object> response = new HashMap<>();
        response.put("id", id_index);
        id_index++;

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGame(@RequestParam int gameId) {
        
        GameManager gameManager = waiting_games.get(gameId);
        if (gameManager == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        }
        Set<PlacementPlayer> available_seats = gameManager.getGame().getGame_seats().getFreeSeats();
        if (available_seats.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Game is full");
        }
        PlacementPlayer reserved_seat = available_seats.iterator().next();
        String ws_url = gameManager.getGame().getGame_seats().getPlayerWebSocket(reserved_seat).getUrl();

        HashMap<String, Object> response = new HashMap<>();
        response.put("ws_url", ws_url);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/list")
    public ResponseEntity<?> listGames() {
        HashMap<Integer, Integer> gamesInfo = new HashMap<>();

        // Clean started games
        waiting_games.entrySet().removeIf(entry -> entry.getValue().getGame().getGame_seats().getFreeSeats().isEmpty());

        for (GameManager gm : waiting_games.values()) {
            gamesInfo.put(
                gm.getGameId(),
                gm.getGame().getGame_seats().getFreeSeats().size()
            );
    }

    HashMap<String, Object> response = new HashMap<>();
    response.put("games", gamesInfo);

    return ResponseEntity.ok(response);
    }
}
       
