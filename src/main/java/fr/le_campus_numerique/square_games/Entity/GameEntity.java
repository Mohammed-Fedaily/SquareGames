package fr.le_campus_numerique.square_games.Entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Token;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameEntity {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String id;
    private String factoryId;
    private String playerIds;
    private String status;
    private String currentPlayerId;
    private int boardSize;
    private String board;
    private String remainingTokens;
    private String removedTokens;

    public GameEntity() {}

    public static String serializePlayerIds(List<String> playerIds) {
        try {
            return objectMapper.writeValueAsString(playerIds);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    public static String serializeBoard(Map<CellPosition, Token> boardMap) {
        try {
            return objectMapper.writeValueAsString(boardMap);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public static String serializeTokens(List<Token> tokens) {
        try {
            return objectMapper.writeValueAsString(tokens);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    // Deserialization methods
    public static List<String> deserializePlayerIds(String playerIdsJson) {
        try {
            return objectMapper.readValue(playerIdsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static Map<CellPosition, Token> deserializeBoard(String boardJson) {
        try {
            return objectMapper.readValue(boardJson,
                    objectMapper.getTypeFactory().constructMapType(Map.class, CellPosition.class, Token.class));
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    public static List<Token> deserializeTokens(String tokensJson) {
        try {
            return objectMapper.readValue(tokensJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Token.class));
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFactoryId() { return factoryId; }
    public void setFactoryId(String factoryId) { this.factoryId = factoryId; }

    public String getPlayerIds() { return playerIds; }
    public void setPlayerIds(String playerIds) { this.playerIds = playerIds; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentPlayerId() { return currentPlayerId; }
    public void setCurrentPlayerId(String currentPlayerId) { this.currentPlayerId = currentPlayerId; }

    public int getBoardSize() { return boardSize; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }

    public String getBoard() { return board; }
    public void setBoard(String board) { this.board = board; }

    public String getRemainingTokens() { return remainingTokens; }
    public void setRemainingTokens(String remainingTokens) { this.remainingTokens = remainingTokens; }

    public String getRemovedTokens() { return removedTokens; }
    public void setRemovedTokens(String removedTokens) { this.removedTokens = removedTokens; }
}