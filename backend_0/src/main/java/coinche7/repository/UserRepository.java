package coinche7.repository;

import coinche7.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByPseudo(String pseudo);

	Optional<User> findByPseudo(String pseudo);
}
