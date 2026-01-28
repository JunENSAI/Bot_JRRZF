package com.chess.jr_bot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chess.jr_bot.dto.OpeningStats;
import com.chess.jr_bot.entity.HistoricalGameEntity;
import com.chess.jr_bot.entity.HistoricalMoveEntity;
import com.chess.jr_bot.repository.HistoricalGameRepository;
import com.chess.jr_bot.repository.HistoricalRepository;

@RestController
@RequestMapping("/api/historical")
public class HistoricalController {

    private final HistoricalRepository repository;         // Gère les Moves & Stats
    private final HistoricalGameRepository gameRepository; // Gère la liste des Games

    // Injection des deux repositories via le constructeur
    public HistoricalController(HistoricalRepository repository, HistoricalGameRepository gameRepository) {
        this.repository = repository;
        this.gameRepository = gameRepository;
    }

    @GetMapping("/openings")
    public ResponseEntity<?> getOpenings() {
        String historicPlayerName = "JRRZF";

        List<OpeningStats> whiteStats = repository.getWhiteOpeningStats(historicPlayerName);
        List<OpeningStats> blackStats = repository.getBlackOpeningStats(historicPlayerName);

        return ResponseEntity.ok(Map.of(
            "white", whiteStats,
            "black", blackStats
        ));
    }

    // --- LISTE DES PARTIES (Paginée & Filtrée) ---
    @GetMapping("/games")
    public ResponseEntity<Page<HistoricalGameEntity>> getHistoricalGames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "") String category) {
        
        String player = "JRRZF";
        Page<HistoricalGameEntity> result;

        // Si la catégorie est vide ou "All", on prend tout
        if (category == null || category.isEmpty() || category.equals("All")) {
            result = gameRepository.findAllGames(player, PageRequest.of(page, size));
        } else {
            // Sinon on filtre (Blitz, Bullet, etc.)
            result = gameRepository.findGamesByType(player, category, PageRequest.of(page, size));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/game-moves")
    public ResponseEntity<List<HistoricalMoveEntity>> getGameMoves(@RequestParam String gameId) {
        // Récupère tous les coups de la partie, triés par ordre chronologique
        return ResponseEntity.ok(repository.findByGameIdOrderByMoveNumberAsc(gameId));
    }

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
            
            result.add(map);
        }
        return result;
    }


}