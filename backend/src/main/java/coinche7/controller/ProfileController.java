package coinche7.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import coinche7.model.GameArchive;
import coinche7.model.User;
import coinche7.repository.GameArchiveRepository;
import coinche7.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final GameArchiveRepository archiveRepository;

    public ProfileController(UserRepository userRepository, GameArchiveRepository archiveRepository) {
        this.userRepository = userRepository;
        this.archiveRepository = archiveRepository;
    }

    private User getCurrentUser(HttpServletRequest request) {
        Object obj = request.getAttribute("authenticatedUser");
        if (obj instanceof User) return (User) obj;
        return null;
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<?> myProfile(HttpServletRequest request) {
        User me = getCurrentUser(request);
        if (me == null) return ResponseEntity.status(401).body("Not authenticated");
        return profileForUser(me.getId());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> profileForUser(@PathVariable long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.status(404).body("User not found");

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("pseudo", user.getPseudo());
        response.put("elo", user.getElo() != null ? user.getElo() : 1000);

        List<GameArchive> archives = archiveRepository.findByPlayersContainingOrderByFinishedAtDesc(user);

        // Limit to last 10
        List<GameArchive> last10 = archives.stream().limit(10).collect(java.util.stream.Collectors.toList());

        List<Map<String, Object>> games = last10.stream().map(a -> {
            Map<String, Object> g = new HashMap<>();
            g.put("id", a.getId());
            g.put("finishedAt", a.getFinishedAt());
            g.put("winnerTeam", a.getWinnerTeam());
            g.put("team1Score", a.getTeam1Score());
            g.put("team2Score", a.getTeam2Score());
            g.put("targetScore", a.getTargetScore());
            
            // Déterminer l'équipe du joueur et s'il a gagné
            boolean playerInTeam1 = a.getTeam1Players().stream().anyMatch(p -> p.getId().equals(user.getId()));
            boolean playerInTeam2 = a.getTeam2Players().stream().anyMatch(p -> p.getId().equals(user.getId()));
            int playerTeam = playerInTeam1 ? 1 : (playerInTeam2 ? 2 : 0);
            boolean playerWon = a.getWinnerTeam() != null && playerTeam > 0 && playerTeam == a.getWinnerTeam();
            g.put("playerWon", playerWon);
            
            // Lister tous les joueurs
            java.util.List<Map<String, Object>> players = new java.util.ArrayList<>();
            for (User p : a.getTeam1Players()) {
                Map<String, Object> pu = new HashMap<>();
                pu.put("id", p.getId());
                pu.put("pseudo", p.getPseudo());
                pu.put("elo", p.getElo());
                players.add(pu);
            }
            for (User p : a.getTeam2Players()) {
                Map<String, Object> pu = new HashMap<>();
                pu.put("id", p.getId());
                pu.put("pseudo", p.getPseudo());
                pu.put("elo", p.getElo());
                players.add(pu);
            }
            g.put("players", players);
            return g;
        }).collect(Collectors.toList());

        response.put("games", games);
        // Stats over all games and recent 10
        int total = archives.size();
        long wins = 0;
        for (GameArchive a : archives) {
            if (a.getWinnerTeam() != null && a.getWinnerTeam() > 0) {
                boolean userInTeam1 = a.getTeam1Players() != null && a.getTeam1Players().stream().anyMatch(p -> p.getId().equals(user.getId()));
                boolean userInTeam2 = a.getTeam2Players() != null && a.getTeam2Players().stream().anyMatch(p -> p.getId().equals(user.getId()));
                int userTeam = userInTeam1 ? 1 : (userInTeam2 ? 2 : 0);
                if (userTeam == a.getWinnerTeam()) wins++;
            }
        }
        long losses = total - wins;
        double winRate = total > 0 ? (wins * 100.0 / total) : 0.0;

        long recentWins = 0;
        for (GameArchive a : last10) {
            if (a.getWinnerTeam() != null && a.getWinnerTeam() > 0) {
                boolean userInTeam1 = a.getTeam1Players() != null && a.getTeam1Players().stream().anyMatch(p -> p.getId().equals(user.getId()));
                boolean userInTeam2 = a.getTeam2Players() != null && a.getTeam2Players().stream().anyMatch(p -> p.getId().equals(user.getId()));
                int userTeam = userInTeam1 ? 1 : (userInTeam2 ? 2 : 0);
                if (userTeam == a.getWinnerTeam()) recentWins++;
            }
        }
        double recentWinRate = last10.size() > 0 ? (recentWins * 100.0 / last10.size()) : 0.0;

        // average score difference (team - opponent) over last10
        double avgScoreDiff = 0.0;
        if (!last10.isEmpty()) {
            double sum = 0.0;
            for (GameArchive a : last10) {
                int team1 = a.getTeam1Score() != null ? a.getTeam1Score() : 0;
                int team2 = a.getTeam2Score() != null ? a.getTeam2Score() : 0;
                boolean userInTeam1 = a.getTeam1Players().stream().anyMatch(p -> p.getId().equals(user.getId()));
                int diff = userInTeam1 ? (team1 - team2) : (team2 - team1);
                sum += diff;
            }
            avgScoreDiff = sum / last10.size();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGames", total);
        stats.put("wins", wins);
        stats.put("losses", losses);
        stats.put("winRate", winRate);
        stats.put("recentGames", last10.size());
        stats.put("recentWins", recentWins);
        stats.put("recentWinRate", recentWinRate);
        stats.put("avgScoreDiffLast10", avgScoreDiff);

        response.put("stats", stats);

        return ResponseEntity.ok(response);
    }
}
