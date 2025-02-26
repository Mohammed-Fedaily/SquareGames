package fr.le_campus_numerique.square_games.Dao;

import fr.le_campus_numerique.square_games.Entity.GameEntity;
import fr.le_campus_numerique.square_games.Entity.GameTokenEntity;
import fr.le_campus_numerique.square_games.Repository.GameEntityRepository;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.Token;
import fr.le_campus_numerique.square_games.engine.TokenPosition;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("jpaGameDao")
public class JpaGameDao implements GameDao {
    private final GameEntityRepository repository;
    private final TicTacToeGameFactory gameFactory;

    @Autowired
    public JpaGameDao(GameEntityRepository repository, TicTacToeGameFactory gameFactory) {
        this.repository = repository;
        this.gameFactory = gameFactory;
    }

    @Override
    public Stream<Game> findAll() {
        return repository.findAll().stream()
                .map(this::ToGame);
    }

    @Override
    public Optional<Game> findById(String gameId) {
        return repository.findById(gameId)
                .map(this::ToGame);
    }

    @Override
    public Game upsert(Game game) {
        GameEntity entity = ToEntity(game);
        repository.save(entity);
        return game;
    }

    @Override
    public void delete(String gameId) {
        repository.deleteById(gameId);
    }

    private GameEntity ToEntity(Game game) {
        GameEntity entity = new GameEntity();

        entity.id = game.getId().toString();
        entity.factoryId = game.getFactoryId();
        entity.boardSize = game.getBoardSize();
        entity.status = game.getStatus().toString();
        entity.currentPlayerId = game.getCurrentPlayerId() != null ?
                game.getCurrentPlayerId().toString() : null;

        entity.playerIds = game.getPlayerIds().stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));

        entity.tokens = new ArrayList<>();

        game.getBoard().forEach((position, token) -> {
            GameTokenEntity tokenEntity = createTokenEntity(token);
            tokenEntity.x = position.x();
            tokenEntity.y = position.y();
            entity.tokens.add(tokenEntity);
        });


        game.getRemainingTokens().forEach(token -> {
            GameTokenEntity tokenEntity = createTokenEntity(token);
            if (token.getPosition() != null) {
                tokenEntity.x = token.getPosition().x();
                tokenEntity.y = token.getPosition().y();
            }
            entity.tokens.add(tokenEntity);
        });

        game.getRemovedTokens().forEach(token -> {
            GameTokenEntity tokenEntity = createTokenEntity(token);
            tokenEntity.removed = true;
            entity.tokens.add(tokenEntity);
        });

        return entity;
    }

    private GameTokenEntity createTokenEntity(Token token) {
        GameTokenEntity entity = new GameTokenEntity();
        entity.name = token.getName();
        entity.removed = false;

        if (token.getOwnerId().isPresent()) {
            entity.ownerId = token.getOwnerId().get().toString();
        }

        return entity;
    }

    private Game ToGame(GameEntity entity) {
        try {
            List<UUID> playerIds = Arrays.stream(entity.playerIds.split(","))
                    .filter(s -> !s.isEmpty())
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

            List<TokenPosition<UUID>> boardTokens = new ArrayList<>();
            List<TokenPosition<UUID>> removedTokens = new ArrayList<>();

            for (GameTokenEntity tokenEntity : entity.tokens) {
                UUID ownerId = tokenEntity.ownerId != null ?
                        UUID.fromString(tokenEntity.ownerId) : null;

                if (tokenEntity.removed) {
                    removedTokens.add(new TokenPosition<>(
                            ownerId, tokenEntity.name, 0, 0
                    ));
                } else if (tokenEntity.x != null && tokenEntity.y != null) {
                    boardTokens.add(new TokenPosition<>(
                            ownerId, tokenEntity.name, tokenEntity.x, tokenEntity.y
                    ));
                }
            }

            return gameFactory.createGameWithIds(
                    UUID.fromString(entity.id),
                    entity.boardSize,
                    playerIds,
                    boardTokens,
                    removedTokens
            );
        } catch (InconsistentGameDefinitionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error loading game: " + e.getMessage()
            );
        }
    }
}