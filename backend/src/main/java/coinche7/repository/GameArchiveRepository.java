package coinche7.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import coinche7.model.GameArchive;
import coinche7.model.User;

public interface GameArchiveRepository extends JpaRepository<GameArchive, Long> {
    @Query("SELECT a FROM GameArchive a WHERE :user MEMBER OF a.team1Players OR :user MEMBER OF a.team2Players ORDER BY a.finishedAt DESC")
    List<GameArchive> findByPlayersContainingOrderByFinishedAtDesc(@Param("user") User user);
}
