package fr.le_campus_numerique.square_games.config;

import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameFactoryConfig {
    @Bean
    public TicTacToeGameFactory ticTacToeGameFactory() {
        return new TicTacToeGameFactory();
    }
}