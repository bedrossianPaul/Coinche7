package coinche7.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import coinche7.game.GameManager;
import coinche7.game.enums.PlacementPlayer;
import coinche7.websocket.PlayerWebSocket;

@Service
public class WebSocketService {
    
    private WebSocketHandlerRegistry registry;
    private final Map<String, PlayerWebSocket> dynamicHandlers = new ConcurrentHashMap<>();

    public void setRegistry(WebSocketHandlerRegistry registry) {
        this.registry = registry;
    }


    public Map<PlacementPlayer, PlayerWebSocket> createGameWebSockets(int gameId, GameManager gm){

        if (registry == null) {
            throw new IllegalStateException("WebSocketHandlerRegistry non initialisé");
        }

        Map<PlacementPlayer, PlayerWebSocket> rep = new ConcurrentHashMap<>();
        for (PlacementPlayer position : PlacementPlayer.values()) {
            String path = "/ws/game/" + gameId + "/player/" + position.name().toLowerCase();
            PlayerWebSocket handler = new PlayerWebSocket(gm, position, path);
            dynamicHandlers.put(path,handler);
            registry.addHandler(handler, path);
            rep.put(position,handler);
        }
        return rep;
    }


}
