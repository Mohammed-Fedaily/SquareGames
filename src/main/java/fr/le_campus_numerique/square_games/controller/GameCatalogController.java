package fr.le_campus_numerique.square_games.Controller;
import fr.le_campus_numerique.square_games.Service.GameCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/catalog")
public class GameCatalogController {
    private final GameCatalogService gameCatalog;

    @Autowired
    public GameCatalogController(GameCatalogService gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    @GetMapping
    public Collection<String> getAvailableGames() {
        return gameCatalog.getGameIdentifiers();
    }
}