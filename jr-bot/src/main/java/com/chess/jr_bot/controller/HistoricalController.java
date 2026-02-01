package com.chess.jr_bot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chess.jr_bot.dto.OpeningStats;
import com.chess.jr_bot.entity.HistoricalGameEntity;
import com.chess.jr_bot.entity.MoveEntity;
import com.chess.jr_bot.repository.HistoricalGameRepository;
import com.chess.jr_bot.repository.HistoricalMoveRepository;

/**
 * Contrôleur gérant l'accès aux données historiques et statistiques.
 * <p>
 * Permet de récupérer les performances sur les ouvertures, de lister les parties
 * archivées avec recherche paginée et d'extraire les coups détaillés d'une partie.
 * </p>
 */
@RestController
@RequestMapping("/api/historical")
public class HistoricalController {

    private static final Logger logger = LoggerFactory.getLogger(HistoricalController.class);


    private final HistoricalMoveRepository moveRepository;
    private final HistoricalGameRepository gameRepository;

    /**
     * Initialise le contrôleur avec les repositories de données historiques.
     * * @param moveRepository Repository pour les coups et statistiques d'ouvertures.
     * @param gameRepository Repository pour la recherche de parties archivées.
     */
    public HistoricalController(HistoricalMoveRepository moveRepository, HistoricalGameRepository gameRepository) {
        this.moveRepository = moveRepository;
        this.gameRepository = gameRepository;
    }

    /**
     * Récupère les statistiques d'ouvertures pour le profil de référence.
     * * @return Une réponse contenant deux listes : "white" et "black" avec les stats par coup.
     */
    @GetMapping("/openings")
    public ResponseEntity<?> getOpenings() {
        String historicPlayerName = "JRRZF";

        List<OpeningStats> whiteStats = moveRepository.getWhiteOpeningStats(historicPlayerName);
        List<OpeningStats> blackStats = moveRepository.getBlackOpeningStats(historicPlayerName);

        return ResponseEntity.ok(Map.of(
            "white", enrichStats(whiteStats, true),
            "black", enrichStats(blackStats, false)
        ));
    }
    /**
     * Recherche des parties historiques avec support de la pagination.
     * * @param page Numéro de la page (0 par défaut).
     * @param size Nombre d'éléments par page (30 par défaut).
     * @param search Filtre textuel pour le nom des joueurs.
     * @return Un objet {@link Page} contenant les résultats de la recherche.
     */
    @GetMapping("/games")
    public ResponseEntity<?> getHistoricalGames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "") String search
        ) {
        
        String player = "JRRZF";
        
        try {
            System.out.println("Tentative de récupération des parties pour : " + player);
            Page<HistoricalGameEntity> result = gameRepository.searchGames(search, PageRequest.of(page, size));
            System.out.println("Succès ! Nombre de parties trouvées : " + result.getTotalElements());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("erreur :", e);
            return ResponseEntity.internalServerError().body("Une erreur s'est produite");
        }
    }

    /**
     * Récupère la liste ordonnée des coups pour une partie donnée.
     * * @param gameId Identifiant unique de la partie.
     * @return Liste de {@link MoveEntity} classée par numéro de coup.
     */
    @GetMapping("/game-moves")
    public ResponseEntity<List<MoveEntity>> getGameMoves(@RequestParam String gameId) {
        return ResponseEntity.ok(moveRepository.findByGameIdOrderByMoveNumberAsc(gameId));
    }

    /**
     * Méthode utilitaire pour transformer les projections en Map (si nécessaire).
     * * @param rawList Liste des statistiques brutes issues du repository.
     * @param isWhite Précise la couleur analysée.
     * @return Liste de Maps formatées pour le JSON.
     */
    private List<Map<String, Object>> enrichStats(List<OpeningStats> rawList, boolean isWhite) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (OpeningStats stat : rawList) {
            Map<String, Object> map = new HashMap<>();
            map.put("move", stat.getMove());
            map.put("fen", stat.getFen());
            map.put("total", stat.getTotal());
            map.put("wins", stat.getWins());
            map.put("draws", stat.getDraws());
            map.put("losses", stat.getLosses());
            map.put("color", isWhite ? "white" : "black");
            
            result.add(map);
        }
        return result;
    }
}