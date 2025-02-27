package fr.le_campus_numerique.square_games.filter;

import fr.le_campus_numerique.square_games.service.UserValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class UserValidationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(UserValidationFilter.class);

    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/catalog",
            "/api/games/name"

    );

    private final UserValidationService userValidationService;

    public UserValidationFilter(UserValidationService userValidationService) {
        this.userValidationService = userValidationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (isPublicEndpoint(path)) {
            logger.debug("Skipping validation for public endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String userIdHeader = request.getHeader("X-UserId");

        if (userIdHeader == null || userIdHeader.isEmpty()) {
            logger.debug("No X-UserId header provided for protected endpoint: {}", path);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "X-UserId header is required");
            return;
        }

        try {
            UUID userId = UUID.fromString(userIdHeader);

            if (userValidationService.validateUser(userId)) {
                logger.debug("User {} validated successfully", userId);
                filterChain.doFilter(request, response);
            } else {
                logger.debug("User {} validation failed", userId);
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid user ID");
            }
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid UUID format in X-UserId header: {}", userIdHeader);
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid user ID format");
        }
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::contains);
    }
}
