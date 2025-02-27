package fr.le_campus_numerique.square_games.repository;


import fr.le_campus_numerique.square_games.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameEntityRepository extends JpaRepository<GameEntity, String> {
}
