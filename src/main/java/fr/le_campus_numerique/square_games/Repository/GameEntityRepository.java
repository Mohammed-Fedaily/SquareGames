package fr.le_campus_numerique.square_games.Repository;


import fr.le_campus_numerique.square_games.Entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameEntityRepository extends JpaRepository<GameEntity, String> {
}
