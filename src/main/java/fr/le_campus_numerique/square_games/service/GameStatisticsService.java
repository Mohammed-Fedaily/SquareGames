package fr.le_campus_numerique.square_games.service;


import fr.le_campus_numerique.square_games.dto.GameStatDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.HashMap;

@Service
public class GameStatisticsService {
    private static final Logger logger = LoggerFactory.getLogger(GameStatisticsService.class);

    private final RestClient restClient;

    @Value("${stats.service.url}")
    private String statsServiceUrl;

    public GameStatisticsService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public void recordGameResult(String gameId, String playerId, String gameType, String result, int moves) {
        try {
            logger.debug("Recording game result for game: {}, player: {}", gameId, playerId);

            GameStatDTO statDto = new GameStatDTO(gameId, playerId, gameType, result, moves);

            restClient.post()
                    .uri(statsServiceUrl + "/api/stats/record")
                    .body(statDto)
                    .retrieve()
                    .toBodilessEntity();

            logger.debug("Successfully recorded game result");
        } catch (Exception e) {
            logger.error("Error recording game statistics: {}", e.getMessage(), e);
        }
    }


}
