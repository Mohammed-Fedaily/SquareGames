package fr.le_campus_numerique.square_games.controller;

import fr.le_campus_numerique.square_games.dto.GameDtoReq;
import fr.le_campus_numerique.square_games.dto.GameDtoRes;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.service.GameService;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public Collection<GameDtoRes> getAllGames() {
        return gameService.getAllGames().stream()
                .map(this::convertToGameDtoRes)
                .collect(Collectors.toList());
    }

    @PostMapping
    public GameDtoRes createGame(
            @RequestBody GameDtoReq gameDtoReq) {
        Game game = gameService.createGame(
                gameDtoReq.getGameType(),
                gameDtoReq.getNumberOfPlayers(),
                gameDtoReq.getBoardSize()
        );
        return convertToGameDtoRes(game);
    }

    @GetMapping("/name")
    public String getGameName(
            @RequestParam("type") String gameType,
            @RequestHeader(name = "Accept-Language", defaultValue = "en") String languageHeader) {
        Locale locale = Locale.forLanguageTag(languageHeader);
        return gameService.getGameName(gameType, locale);
    }

    @GetMapping("/{gameId}")
    public GameDtoRes getGame(@PathVariable UUID gameId) {
        Game game = gameService.getGame(gameId);
        return convertToGameDtoRes(game);
    }

    @PostMapping("/{gameId}/moves")
    public GameDtoRes playMove(
            @PathVariable UUID gameId,
            @RequestHeader("X-UserId") UUID playerId,
            @RequestBody CellPosition position) throws InvalidPositionException {
        Game game = gameService.playMove(gameId, playerId, position);
        return convertToGameDtoRes(game);
    }

    @GetMapping("/{gameId}/available-moves")
    public Collection<CellPosition> getAvailableMoves(
            @PathVariable UUID gameId,
            @RequestHeader("X-UserId") UUID playerId
    ) {
        Game game = gameService.getGame(gameId);
        if (!playerId.equals(game.getCurrentPlayerId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Yoo what's u doing?? - is it ur turn??"
            );
        }
        return gameService.getAvailableMoves(gameId, playerId);
    }

    @GetMapping("/{gameId}/history")
    public List<String> getGameHistory(@PathVariable UUID gameId) {
        return gameService.getGameHistory(gameId);
    }

    private GameDtoRes convertToGameDtoRes(Game game) {
        GameDtoRes dto = new GameDtoRes();
        dto.setGameId(game.getId());
        dto.setFactoryId(game.getFactoryId());
        dto.setPlayerIds(game.getPlayerIds());
        dto.setStatus(game.getStatus().toString());
        dto.setCurrentPlayerId(game.getCurrentPlayerId());
        dto.setBoardSize(game.getBoardSize());
        dto.setBoard(game.getBoard());
        dto.setRemainingTokens(game.getRemainingTokens());
        dto.setRemovedTokens(game.getRemovedTokens());
        return dto;
    }
}