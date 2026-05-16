package coinche7.controller;

import coinche7.model.User;
import coinche7.services.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/private/auth")
public class TokenController {
    TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkToken(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");

        return ResponseEntity.ok().body(Map.of(
                "message", "Token is valid",
                "id", user.getId(),
                "pseudo", user.getPseudo(),
                "elo", user.getElo()
        ));
    }
}
