package coinche7.controller;

import coinche7.model.User;
import coinche7.repository.UserRepository;
import coinche7.services.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    private static final long TOKEN_EXPIRATION_MS = 86_400_000L;

    public UserController(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User userPayload) {
        if (userPayload.getPseudo() == null || userPayload.getPseudo().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pseudo is required"));
        }

        if (userPayload.getPassword() == null || userPayload.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }

        if (userRepository.existsByPseudo(userPayload.getPseudo())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "This username is already taken"));
        }

        User user = new User();
        user.setPseudo(userPayload.getPseudo());
        user.setPassword(passwordEncoder.encode(userPayload.getPassword()));

        User savedUser = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("pseudo", savedUser.getPseudo());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginPayload) {
        if (loginPayload.getPseudo() == null || loginPayload.getPseudo().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pseudo is required"));
        }
        if (loginPayload.getPassword() == null || loginPayload.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }

        return userRepository.findByPseudo(loginPayload.getPseudo())
                .map(user -> {
                    String storedPassword = user.getPassword();
                    if (storedPassword != null && passwordEncoder.matches(loginPayload.getPassword(), storedPassword)) {
                        Map<String, Object> response = new HashMap<>();
                        String token = tokenService.generateToken(user, TOKEN_EXPIRATION_MS);

                        response.put("id", user.getId());
                        response.put("pseudo", user.getPseudo());
                        response.put("token", token);
                        return ResponseEntity.ok(response);
                    }

                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Password incorrect"));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found")));
    }

}
