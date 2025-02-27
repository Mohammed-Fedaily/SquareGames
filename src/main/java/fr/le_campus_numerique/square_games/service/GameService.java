package fr.le_campus_numerique.square_games.service;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface GameService {
    Game createGame(String gameId, int numberOfPlayers, int boardSize);
    Game getGame(UUID gameId);
    Game playMove(UUID gameId, UUID playerId, CellPosition position) throws InvalidPositionException;
    Collection<Game> getAllGames();
    Collection<CellPosition> getAvailableMoves(UUID gameId, UUID playerId);
    List<String> getGameHistory(UUID gameId);
    String getGameName(String gameType, Locale locale);

}
