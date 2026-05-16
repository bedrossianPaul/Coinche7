package coinche7.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.net.URI;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import coinche7.game.GameManager;
import coinche7.game.enums.PlacementPlayer;
import coinche7.websocket.PlayerWebSocket;

@Service
public class WebSocketService extends TextWebSocketHandler {
    
    private final Map<String, PlayerWebSocket> dynamicHandlers = new ConcurrentHashMap<>();
    private final Map<String, PlayerWebSocket> sessionHandlers = new ConcurrentHashMap<>();


    public Map<PlacementPlayer, PlayerWebSocket> createGameWebSockets(int gameId, GameManager gm){
        Map<PlacementPlayer, PlayerWebSocket> rep = new ConcurrentHashMap<>();
        for (PlacementPlayer position : PlacementPlayer.values()) {
            String path = "/game/" + gameId + "/player/" + position.name().toLowerCase();
            PlayerWebSocket handler = new PlayerWebSocket(gm, position, path);
            dynamicHandlers.put(path,handler);
            rep.put(position,handler);
        }
        return rep;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        String path = uri != null ? uri.getPath() : null;
        PlayerWebSocket delegate = path != null ? dynamicHandlers.get(path) : null;

        if (delegate == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unknown game websocket path"));
            return;
        }

        sessionHandlers.put(session.getId(), delegate);
        delegate.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        PlayerWebSocket delegate = sessionHandlers.get(session.getId());
        if (delegate == null) {
            return;
        }
        delegate.onTextMessage(session, message);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        PlayerWebSocket delegate = sessionHandlers.remove(session.getId());
        if (delegate != null) {
            delegate.afterConnectionClosed(session, status);
        }
    }


}
