package fr.le_campus_numerique.square_games.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class TaquinPlugin implements GamePlugin {
    private final TaquinGameFactory gameFactory;
    private final MessageSource messageSource;

    @Value("${game.taquin.default-player-count:1}")
    private int defaultPlayerCount;

    @Value("${game.taquin.default-board-size:4}")
    private int defaultBoardSize;

    @Autowired
    public TaquinPlugin(MessageSource messageSource) {
        this.gameFactory = new TaquinGameFactory();
        this.messageSource = messageSource;
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.taquin.name", null, locale);
    }

    @Override
    public Game createGame(Optional<Integer> playerCount, Optional<Integer> boardSize) {
        return gameFactory.createGame(
                playerCount.orElse(defaultPlayerCount),
                boardSize.orElse(defaultBoardSize)
        );
    }
}