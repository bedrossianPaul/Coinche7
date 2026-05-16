package coinche7.model;
import jakarta.persistence.*;

@Entity
@Table(name = "waiting_games")
public class Game {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
