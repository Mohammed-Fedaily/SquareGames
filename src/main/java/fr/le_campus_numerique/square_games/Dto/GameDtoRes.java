package fr.le_campus_numerique.square_games.dto;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Token;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameDtoRes {
    private UUID gameId;
    private String factoryId;
    private Set<UUID> playerIds;
    private String status;
    private UUID currentPlayerId;
    private int boardSize;
    private Map<CellPosition, Token> board;
    private Collection<Token> remainingTokens;
    private Collection<Token> removedTokens;

    // Getters and Setters
    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    public Set<UUID> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(Set<UUID> playerIds) {
        this.playerIds = playerIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(UUID currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public Map<CellPosition, Token> getBoard() {
        return board;
    }

    public void setBoard(Map<CellPosition, Token> board) {
        this.board = board;
    }

    public Collection<Token> getRemainingTokens() {
        return remainingTokens;
    }

    public void setRemainingTokens(Collection<Token> remainingTokens) {
        this.remainingTokens = remainingTokens;
    }

    public Collection<Token> getRemovedTokens() {
        return removedTokens;
    }

    public void setRemovedTokens(Collection<Token> removedTokens) {
        this.removedTokens = removedTokens;
    }
}