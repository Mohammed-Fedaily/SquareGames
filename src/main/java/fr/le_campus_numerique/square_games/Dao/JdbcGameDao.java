package fr.le_campus_numerique.square_games.Dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.le_campus_numerique.square_games.engine.*;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.apache.commons.dbutils.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JdbcGameDao implements GameDao {
    private final QueryRunner queryRunner;
    private final ObjectMapper objectMapper;
    private final TicTacToeGameFactory gameFactory;

    @Autowired
    public JdbcGameDao(DataSource dataSource, TicTacToeGameFactory gameFactory) {
        this.queryRunner = new QueryRunner(dataSource);
        this.objectMapper = new ObjectMapper();
        this.gameFactory = gameFactory;
    }

    @Override
    public Stream<Game> findAll() {
        try {
            String sql = "SELECT * FROM games";

            return queryRunner.query(sql, rs -> {
                List<Game> games = new ArrayList<>();
                while (rs.next()) {
                    UUID gameId = UUID.fromString(rs.getString("id"));
                    int boardSize = rs.getInt("board_size");

                    List<UUID> playerIds = parsePlayerIds(rs.getString("player_ids"));

                    Collection<TokenPosition<UUID>> boardTokens =
                            deserializeBoardTokens(rs.getString("board"));
                    Collection<TokenPosition<UUID>> removedTokens =
                            deserializeRemovedTokens(rs.getString("removed_tokens"));

                    Game game = null;
                    try {
                        game = gameFactory.createGameWithIds(
                                gameId,
                                boardSize,
                                playerIds,
                                boardTokens,
                                removedTokens
                        );
                    } catch (InconsistentGameDefinitionException e) {
                        throw new RuntimeException(e);
                    }

                    games.add(game);
                }
                return games.stream();
            });
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching games", e);
        }
    }

    @Override
    public Optional<Game> findById(String gameId) {
        try {
            String sql = "SELECT * FROM games WHERE id = ?";

            return queryRunner.query(sql, rs -> {
                if (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    int boardSize = rs.getInt("board_size");

                    List<UUID> playerIds = parsePlayerIds(rs.getString("player_ids"));

                    Collection<TokenPosition<UUID>> boardTokens =
                            deserializeBoardTokens(rs.getString("board"));
                    Collection<TokenPosition<UUID>> removedTokens =
                            deserializeRemovedTokens(rs.getString("removed_tokens"));

                    Game game = null;
                    try {
                        game = gameFactory.createGameWithIds(
                                id,
                                boardSize,
                                playerIds,
                                boardTokens,
                                removedTokens
                        );
                    } catch (InconsistentGameDefinitionException e) {
                        throw new RuntimeException(e);
                    }

                    return Optional.of(game);
                }
                return Optional.empty();
            }, gameId);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching game", e);
        }
    }

    @Override
    public Game upsert(Game game) {
        try {
            String playerIds = game.getPlayerIds().stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));

            String boardTokensJson = serializeBoardTokens(game);

            String remainingTokensJson = serializeTokens(game.getRemainingTokens());

            String removedTokensJson = serializeTokens(game.getRemovedTokens());

            String sql = "REPLACE INTO games " +
                    "(id, factory_id, player_ids, status, current_player_id, board_size, " +
                    "board, remaining_tokens, removed_tokens) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            queryRunner.update(sql,
                    game.getId().toString(),
                    game.getFactoryId(),
                    playerIds,
                    game.getStatus().toString(),
                    game.getCurrentPlayerId().toString(),
                    game.getBoardSize(),
                    boardTokensJson,
                    remainingTokensJson,
                    removedTokensJson
            );

            return game;
        } catch (SQLException e) {
            throw new RuntimeException("Error upserting game", e);
        }
    }

    @Override
    public void delete(String gameId) {
        try {
            queryRunner.update("DELETE FROM games WHERE id = ?", gameId);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting game", e);
        }
    }

    private String serializeTokens(Collection<Token> tokens) {
        try {
            return objectMapper.writeValueAsString(
                    tokens.stream()
                            .map(this::serializeToken)
                            .collect(Collectors.toList())
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing tokens", e);
        }
    }

    private Map<String, Object> serializeToken(Token token) {
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("ownerId", token.getOwnerId().orElse(null));
        tokenMap.put("name", token.getName());
        tokenMap.put("position", token.getPosition());
        tokenMap.put("allowedMoves", token.getAllowedMoves());
        return tokenMap;
    }

    private String serializeBoardTokens(Game game) {
        try {
            return objectMapper.writeValueAsString(
                    game.getBoard().entrySet().stream()
                            .map(entry -> {
                                Map<String, Object> boardEntry = new HashMap<>();
                                boardEntry.put("position", entry.getKey());
                                boardEntry.put("token", serializeToken(entry.getValue()));
                                return boardEntry;
                            })
                            .collect(Collectors.toList())
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing board tokens", e);
        }
    }

    private List<UUID> parsePlayerIds(String playerIdsJson) {
        try {
            if (playerIdsJson == null || playerIdsJson.trim().isEmpty()) {
                return Collections.emptyList();
            }

            try {
                List<String> playerIdStrings = objectMapper.readValue(playerIdsJson, new TypeReference<List<String>>() {});
                return playerIdStrings.stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList());
            } catch (JsonProcessingException e) {
                return Arrays.stream(playerIdsJson.split(","))
                        .map(String::trim)
                        .map(UUID::fromString)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error parsing player IDs: " + playerIdsJson);
            return Collections.emptyList();
        }
    }

    private Collection<TokenPosition<UUID>> deserializeBoardTokens(String boardTokensJson) {
        try {
            if (boardTokensJson == null || boardTokensJson.trim().isEmpty() || boardTokensJson.equals("{}")) {
                return Collections.emptyList();
            }

            try {

                List<Map<String, Object>> boardEntries = objectMapper.readValue(boardTokensJson,
                        new TypeReference<List<Map<String, Object>>>() {});

                return boardEntries.stream()
                        .map(entry -> {
                            Map<String, Object> positionMap = entry.containsKey("position")
                                    ? (Map<String, Object>) entry.get("position")
                                    : entry;

                            int x = extractIntValue(positionMap, "x");
                            int y = extractIntValue(positionMap, "y");

                            Map<String, Object> tokenMap = entry.containsKey("token")
                                    ? (Map<String, Object>) entry.get("token")
                                    : entry;

                            String ownerIdStr = tokenMap.containsKey("ownerId")
                                    ? (String) tokenMap.get("ownerId")
                                    : null;

                            UUID ownerId = ownerIdStr != null
                                    ? UUID.fromString(ownerIdStr)
                                    : null;

                            String tokenName = tokenMap.containsKey("name")
                                    ? (String) tokenMap.get("name")
                                    : "Unknown";

                            return new TokenPosition<>(
                                    ownerId,
                                    tokenName,
                                    x,
                                    y
                            );
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
                Map<String, Object> boardEntry = objectMapper.readValue(boardTokensJson,
                        new TypeReference<Map<String, Object>>() {});

                return Collections.singletonList(
                        new TokenPosition<>(
                                null,
                                "Unknown",
                                extractIntValue(boardEntry, "x"),
                                extractIntValue(boardEntry, "y")
                        )
                );
            }
        } catch (Exception e) {
            System.err.println("Error deserializing board tokens: " + boardTokensJson);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private int extractIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private Collection<TokenPosition<UUID>> deserializeRemovedTokens(String removedTokensJson) {
        try {
            List<Map<String, Object>> tokenEntries = objectMapper.readValue(removedTokensJson, new TypeReference<List<Map<String, Object>>>() {
            });

            return tokenEntries.stream()
                    .map(tokenMap -> {
                        String ownerIdStr = (String) tokenMap.get("ownerId");
                        UUID ownerId = ownerIdStr != null ? UUID.fromString(ownerIdStr) : null;
                        String tokenName = (String) tokenMap.get("name");

                        return new TokenPosition<>(
                                ownerId,
                                tokenName,
                                0,
                                0
                        );
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing removed tokens", e);
        }
    }
}