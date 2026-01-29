package com.chess.jr_bot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chess.jr_bot.entity.HistoricalMoveEntity;
import com.chess.jr_bot.repository.HistoricalRepository;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    private final HistoricalRepository repository;

    public TrainingController(HistoricalRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/puzzle")
    public ResponseEntity<HistoricalMoveEntity> getPuzzle(@RequestParam String type) {
        // type peut être : "opening", "endgame", "winning"
        HistoricalMoveEntity puzzle = repository.findRandomPuzzle(type);
        
        if (puzzle == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(puzzle);
    }
}