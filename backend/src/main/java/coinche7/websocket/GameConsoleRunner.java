package coinche7.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class GameConsoleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GameConsoleRunner.class);

    private final GameWebSocketHandler gameWebSocketHandler;

    public GameConsoleRunner(GameWebSocketHandler gameWebSocketHandler) {
        this.gameWebSocketHandler = gameWebSocketHandler;
    }

    @Override
    public void run(String... args) {
        Thread consoleThread = new Thread(this::readConsoleLoop, "game-server-console");
        consoleThread.setDaemon(true);
        consoleThread.start();

        log.info("Console WebSocket démarrée. Tape un message ici pour l'envoyer aux clients connectés.");
        log.info("Commandes: /clients pour voir le nombre de connexions, /help pour l'aide.");
    }

    private void readConsoleLoop() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                gameWebSocketHandler.broadcastFromConsole(line);
            }
        } catch (IOException e) {
            log.error("Erreur dans la console serveur WebSocket", e);
        }
    }
}