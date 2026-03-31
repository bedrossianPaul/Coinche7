package coinche7.services;

import java.security.SecureRandom;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import coinche7.model.User;
import coinche7.model.Token;
import coinche7.repository.TokenRepository;

@Service
public class TokenService {
    private final TokenRepository tokenRepository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String CHARSET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789" +
            "!@#$%^&*()-_=+[]{};:,.?/";

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Generate a token for an user
     * @return token string
     */
    public String generateToken(User user, Long expirationMs) {
        tokenRepository.findByUserId(user.getId()).ifPresent(tokenRepository::delete);
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            int index = SECURE_RANDOM.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }
        String token = sb.toString();
        String hashedToken = passwordEncoder.encode(token);

        Token tokenEntity = new Token();
        tokenEntity.setToken(hashedToken);
        tokenEntity.setUser(user);
        if (expirationMs != null) {
            tokenEntity.setExpiration(System.currentTimeMillis() + expirationMs);
        } else {
            tokenEntity.setExpiration(null);
        }
        tokenEntity = tokenRepository.save(tokenEntity);
        return token;
    }

    /**
     * Validate a token and return the associated user if valid, null otherwise
     * @param token
     * @return User associated with the token if valid, null otherwise
     */
    public User validateToken(String token) {
        long now = System.currentTimeMillis();

        return tokenRepository.findAll().stream()
                .filter(t -> passwordEncoder.matches(token, t.getToken()))
                .filter(t -> t.getExpiration() == null || t.getExpiration() > now)
                .findFirst()
                .map(validToken -> {
                    return validToken.getUser();
                })
                .orElse(null);
    }

    /**
    * Invalidate a token (delete it from the database)
    */
   public void invalidateToken(String token) {
       tokenRepository.findAll().stream()
               .filter(t -> passwordEncoder.matches(token, t.getToken()))
               .findFirst()
               .ifPresent(tokenRepository::delete);
        return;
   }
}
