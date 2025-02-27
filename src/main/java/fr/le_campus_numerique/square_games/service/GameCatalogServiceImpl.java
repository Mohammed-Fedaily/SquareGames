package fr.le_campus_numerique.square_games.service;

import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

@Service
public class GameCatalogServiceImpl implements GameCatalogService {
    private final TicTacToeGameFactory ticTacToeFactory;
    private final TaquinGameFactory taquinFactory;

    public GameCatalogServiceImpl(TicTacToeGameFactory ticTacToeFactory,
                                  TaquinGameFactory taquinFactory) {
        this.ticTacToeFactory = ticTacToeFactory;
        this.taquinFactory = taquinFactory;
    }

    @Override
    public Collection<String> getGameIdentifiers() {
        return Arrays.asList(
                ticTacToeFactory.getGameFactoryId(),
                taquinFactory.getGameFactoryId()
        );
    }
}