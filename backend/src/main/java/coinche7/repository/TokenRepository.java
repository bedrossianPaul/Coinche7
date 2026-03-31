package coinche7.repository;

import coinche7.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    boolean existsByUserId(Long userId);

    Optional<Token> findByUserId(Long userId);

	Optional<Token> findByToken(String token);
}
