package fr.le_campus_numerique.square_games.Service;

import fr.le_campus_numerique.square_games.Dao.GameDao;
import fr.le_campus_numerique.square_games.Plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
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
    private final GamePlugin gamePlugin;
    private final Map<UUID, List<String>> gameHistories = new HashMap<>();

    @Autowired
    public GameServiceImpl(@Qualifier("jdbcGameDao") GameDao gameDao, GamePlugin gamePlugin) {
        this.gameDao = gameDao;
        this.gamePlugin = gamePlugin;
    }

    @Override
    public Collection<Game> getAllGames() {
        return gameDao.findAll().collect(Collectors.toList());
    }

    @Override
    public Game createGame(String gameType, int numberOfPlayers, int boardSize) {
        Game game = gamePlugin.createGame(
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
        Game game = getGame(gameId);

        return game.getRemainingTokens().stream()
                .filter(token -> token.getOwnerId().isPresent() &&
                        token.getOwnerId().get().equals(playerId))
                .findFirst()
                .map(Token::getAllowedMoves)
                .orElse(Collections.emptySet());
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
    public String getGameName(Locale locale) {
        return gamePlugin.getName(locale);
    }
}