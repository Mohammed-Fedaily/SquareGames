package fr.le_campus_numerique.square_games.service;

import fr.le_campus_numerique.square_games.dao.GameDao;
import fr.le_campus_numerique.square_games.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {
    private final GameDao gameDao;
    private final Map<String, GamePlugin> gamePlugins;
    private final Map<UUID, List<String>> gameHistories = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);


    @Autowired
    public GameServiceImpl(@Qualifier("jpaGameDao") GameDao gameDao, List<GamePlugin> plugins) {
        this.gameDao = gameDao;
        this.gamePlugins = plugins.stream()
                .collect(Collectors.toMap(
                        plugin -> {
                            Game sampleGame = plugin.createGame(Optional.empty(), Optional.empty());
                            return sampleGame.getFactoryId();
                        },
                        plugin -> plugin
                ));

    }

    @Override
    public Collection<Game> getAllGames() {
        return gameDao.findAll().collect(Collectors.toList());
    }

    @Override
    public Game createGame(String gameType, int numberOfPlayers, int boardSize) {
        GamePlugin plugin = gamePlugins.get(gameType);
        if (plugin == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unknown game type: " + gameType);
        }

        Game game = plugin.createGame(
                Optional.of(numberOfPlayers),
                Optional.of(boardSize)
        );

        gameDao.upsert(game);
        gameHistories.put(game.getId(), new ArrayList<>());
        return game;

    }

    @Override
    public Game getGame(UUID gameId) {
        return gameDao.findById(gameId.toString())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No Game find"));
    }

    @Override
    public Collection<CellPosition> getAvailableMoves(UUID gameId, UUID playerId) {
        logger.debug("Getting available moves for game: {}, player: {}", gameId, playerId);

        Game game = getGame(gameId);

        logger.debug("Current player: {}", game.getCurrentPlayerId());
        logger.debug("Remaining tokens : {}", game.getRemainingTokens().size());

        game.getRemainingTokens().forEach(token -> {
            logger.debug("Token: {}, Owner: {}, Allowed moves: {}",
                    token.getName(),
                    token.getOwnerId().orElse(null),
                    token.getAllowedMoves().size());


        });

        if (!playerId.equals(game.getCurrentPlayerId())) {
            logger.debug("Not player's turn. Current player is: {}", game.getCurrentPlayerId());
            return Collections.emptySet();
        }

        Collection<CellPosition> availableMoves = game.getRemainingTokens().stream()
                .filter(token -> {
                    boolean hasOwner = token.getOwnerId().isPresent();
                    boolean isOwner = hasOwner && token.getOwnerId().get().equals(playerId);
                    logger.debug("Token: {}, hasOwner: {}, isOwner: {}",
                            token.getName(), hasOwner, isOwner);
                    return isOwner;
                })
                .findFirst()
                .map(token -> {
                    logger.debug("Found token for player, allowed moves: {}",
                            token.getAllowedMoves().size());
                    return token.getAllowedMoves();
                })
                .orElse(Collections.emptySet());

        logger.debug("Final available moves count: {}", availableMoves.size());
        return availableMoves;
    }

    @Override
    public List<String> getGameHistory(UUID gameId) {
        if (!gameHistories.containsKey(gameId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "You Have No History Here N****");
        }
        return gameHistories.get(gameId);
    }

    private void recordMove(UUID gameId, UUID playerId, CellPosition position) {
        String moveRecord = String.format("Player %s moved to position (%d,%d)",
                playerId, position.x(), position.y());

        if (!gameHistories.containsKey(gameId)) {
            gameHistories.put(gameId, new ArrayList<>());
        }
        gameHistories.get(gameId).add(moveRecord);
    }

    @Override
    public Game playMove(UUID gameId, UUID playerId, CellPosition position) throws InvalidPositionException {
        Game game = getGame(gameId);

        if (!playerId.equals(game.getCurrentPlayerId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not your turn DumbAss");
        }


        Token tokenToPlay = game.getRemainingTokens().stream()
                .filter(token -> token.getOwnerId().isPresent() &&
                        token.getOwnerId().get().equals(playerId) &&
                        token.getAllowedMoves().contains(position))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "U Can't Use This Token !! From Where You Got It!??"));

            tokenToPlay.moveTo(position);
            recordMove(gameId, playerId, position);

            gameDao.upsert(game);

            return game;
    }

    @Override
    public String getGameName(String gameType, Locale locale) {
        GamePlugin plugin = gamePlugins.get(gameType);
        if (plugin == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported game type: " + gameType
            );
        }
        return plugin.getName(locale);
    }
}