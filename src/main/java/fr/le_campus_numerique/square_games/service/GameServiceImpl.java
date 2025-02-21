package fr.le_campus_numerique.square_games.service;

import fr.le_campus_numerique.square_games.engine.*;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameServiceImpl implements GameService {
    private final Map<UUID, Game> games = new ConcurrentHashMap<>();
    private final TicTacToeGameFactory gameFactory;
    private final Map<UUID, List<String>> gameHistories = new ConcurrentHashMap<>();

    public GameServiceImpl() {
        this.gameFactory = new TicTacToeGameFactory();
    }

    @Override
    public Collection<Game> getAllGames() {
        return games.values();
    }

    @Override
    public Game createGame(String gameId, int numberOfPlayers, int boardSize) {
        if (!gameFactory.getGameFactoryId().equals(gameId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid game type");
        }

        IntRange playerCountRange = gameFactory.getPlayerCountRange();
        if (numberOfPlayers < playerCountRange.min() || numberOfPlayers > playerCountRange.max()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Player count must be between " + playerCountRange.min() + " and " + playerCountRange.max());
        }

        IntRange boardSizeRange = gameFactory.getBoardSizeRange(numberOfPlayers);
        if (boardSize < boardSizeRange.min() || boardSize > boardSizeRange.max()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Board size must be between " + boardSizeRange.min() + " and " + boardSizeRange.max());
        }

        Game game = gameFactory.createGame(numberOfPlayers, boardSize);
        games.put(game.getId(), game);
        return game;
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
    public Game getGame(UUID gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        return game;
    }

    @Override
    public List<String> getGameHistory(UUID gameId) {
        if (!gameHistories.containsKey(gameId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game history not found");
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
    public Game playMove(UUID gameId, UUID playerId, CellPosition position) {
        Game game = getGame(gameId);

        if (!playerId.equals(game.getCurrentPlayerId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not your turn");
        }

        Token tokenToPlay = game.getRemainingTokens().stream()
                .filter(token -> token.getOwnerId().isPresent() &&
                        token.getOwnerId().get().equals(playerId) &&
                        token.getAllowedMoves().contains(position))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No valid token available for this move"));

        try {
            tokenToPlay.moveTo(position);
            recordMove(gameId, playerId, position);
            return game;
        } catch (InvalidPositionException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid move: " + e.getMessage());
        }
    }
}