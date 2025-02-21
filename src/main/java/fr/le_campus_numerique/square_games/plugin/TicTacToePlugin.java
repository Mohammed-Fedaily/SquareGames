package fr.le_campus_numerique.square_games.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class TicTacToePlugin implements GamePlugin {
    private final TicTacToeGameFactory gameFactory;
    private final MessageSource messageSource;

    @Value("${game.tictactoe.default-player-count:2}")
    private int defaultPlayerCount;

    @Value("${game.tictactoe.default-board-size:3}")
    private int defaultBoardSize;

    @Autowired
    public TicTacToePlugin(MessageSource messageSource) {
        this.gameFactory = new TicTacToeGameFactory();
        this.messageSource = messageSource;
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.tictactoe.name", null, locale);
    }

    @Override
    public Game createGame(Optional<Integer> playerCount, Optional<Integer> boardSize) {
        return gameFactory.createGame(
                playerCount.orElse(defaultPlayerCount),
                boardSize.orElse(defaultBoardSize)
        );
    }
}