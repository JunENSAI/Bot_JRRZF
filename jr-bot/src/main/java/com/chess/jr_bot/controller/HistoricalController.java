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
import com.chess.jr_bot.entity.HistoricalMoveEntity;
import com.chess.jr_bot.repository.HistoricalGameRepository;
import com.chess.jr_bot.repository.HistoricalRepository;

@RestController
@RequestMapping("/api/historical")
public class HistoricalController {

    private static final Logger logger = LoggerFactory.getLogger(HistoricalController.class);


    private final HistoricalRepository repository;
    private final HistoricalGameRepository gameRepository;

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

    @GetMapping("/game-moves")
    public ResponseEntity<List<HistoricalMoveEntity>> getGameMoves(@RequestParam String gameId) {
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