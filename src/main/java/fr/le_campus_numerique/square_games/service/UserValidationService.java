package fr.le_campus_numerique.square_games.service;


import fr.le_campus_numerique.square_games.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class UserValidationService {
    private static final Logger logger = LoggerFactory.getLogger(UserValidationService.class);

    private final RestClient restClient;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public UserValidationService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public boolean validateUser(UUID userId) {
        try {
            logger.debug("Validating user: {}", userId);

            UserDto user = restClient.get()
                    .uri(userServiceUrl + "/users/{userId}", userId)
                    .retrieve()
                    .body(UserDto.class);

            boolean valid = user != null;

            logger.debug("User {} validation result: {}", userId, valid);
            return valid;
        } catch (Exception e) {
            logger.error("Error validating user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
}
