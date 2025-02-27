package fr.le_campus_numerique.square_games.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "game_tokens")
public class GameTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "owner_id")
    public String ownerId;

    @NotNull
    @Column(name = "name")
    public String name;

    @Column(name = "removed")
    public boolean removed;

    @Column(name = "x")
    public Integer x;

    @Column(name = "y")
    public Integer y;

    @Column(name = "game_id")
    public String gameId;
}
