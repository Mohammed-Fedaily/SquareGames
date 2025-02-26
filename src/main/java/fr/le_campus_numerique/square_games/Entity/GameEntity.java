package fr.le_campus_numerique.square_games.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Entity
@Table(name = "games")
public class GameEntity {
    @Id
    public String id;

    @NotNull
    @Column(name = "factory_id")
    public String factoryId;

    @Positive
    @Column(name = "board_size")
    public int boardSize;

    @NotNull
    @Column(name = "player_ids")
    public String playerIds;

    @Column(name = "status")
    public String status;

    @Column(name = "current_player_id")
    public String currentPlayerId;

    @Column(name = "board")
    public String board;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "game_id", referencedColumnName = "id")
    public List<GameTokenEntity> tokens;;
}
