package coinche7.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_archives")
public class GameArchive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long finishedAt;

    private Integer winnerTeam;

    private Integer team1Score;
    private Integer team2Score;

    private Integer targetScore;

    @ManyToMany
    @JoinTable(name = "game_archive_team1_players",
        joinColumns = @JoinColumn(name = "archive_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> team1Players = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "game_archive_team2_players",
        joinColumns = @JoinColumn(name = "archive_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> team2Players = new ArrayList<>();

    public GameArchive() {}

    public Long getId() { return id; }
    public Long getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Long finishedAt) { this.finishedAt = finishedAt; }

    public Integer getWinnerTeam() { return winnerTeam; }
    public void setWinnerTeam(Integer winnerTeam) { this.winnerTeam = winnerTeam; }

    public Integer getTeam1Score() { return team1Score; }
    public void setTeam1Score(Integer team1Score) { this.team1Score = team1Score; }

    public Integer getTeam2Score() { return team2Score; }
    public void setTeam2Score(Integer team2Score) { this.team2Score = team2Score; }

    public Integer getTargetScore() { return targetScore; }
    public void setTargetScore(Integer targetScore) { this.targetScore = targetScore; }

    public List<User> getTeam1Players() { return team1Players; }
    public void setTeam1Players(List<User> team1Players) { this.team1Players = team1Players; }

    public List<User> getTeam2Players() { return team2Players; }
    public void setTeam2Players(List<User> team2Players) { this.team2Players = team2Players; }
}
