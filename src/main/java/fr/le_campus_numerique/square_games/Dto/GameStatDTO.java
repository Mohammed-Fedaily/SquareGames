package fr.le_campus_numerique.square_games.dto;


import java.time.LocalDateTime;

public class GameStatDTO {
    private String gameId;
    private String playerId;
    private String gameType;
    private String result;
    private int moves;
    private LocalDateTime timestamp;

    public GameStatDTO() {
    }

    public GameStatDTO(String gameId, String playerId, String gameType, String result, int moves) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.gameType = gameType;
        this.result = result;
        this.moves = moves;
        this.timestamp = LocalDateTime.now();
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return "GameStatDTO{" +
                "gameId='" + gameId + '\'' +
                ", playerId='" + playerId + '\'' +
                ", gameType='" + gameType + '\'' +
                ", result='" + result + '\'' +
                ", moves=" + moves +
                ", timestamp=" + timestamp +
                '}';
    }
}
