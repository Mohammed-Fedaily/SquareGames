package fr.le_campus_numerique.square_games.Service;

import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class GameCatalogServiceImpl implements GameCatalogService {
    private final TicTacToeGameFactory ticTacToeFactory;

    public GameCatalogServiceImpl() {
        this.ticTacToeFactory = new TicTacToeGameFactory();

    }

    @Override
    public Collection<String> getGameIdentifiers() {
        return Collections.singletonList(ticTacToeFactory.getGameFactoryId());
    }
}
