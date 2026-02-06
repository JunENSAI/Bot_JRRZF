package com.chess.jr_bot.controller;

import com.chess.jr_bot.entity.MoveEntity;
import com.chess.jr_bot.entity.MoveClassification;
import com.chess.jr_bot.repository.HistoricalMoveRepository;
import com.chess.jr_bot.service.GameReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur responsable de l'analyse post-partie et de la revue de jeu.
 * <p>
 * Ce composant calcule la qualité des coups joués en comparant l'évolution 
 * du score d'évaluation entre chaque tour.
 * </p>
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private HistoricalMoveRepository moveRepository;

    @Autowired
    private GameReviewService reviewService;

    /**
     * Effectue une révision complète d'une partie et classifie chaque coup.
     * <p>
     * Le processus compare le score actuel au score précédent du point de vue 
     * du joueur dont c'est le tour. Il identifie également si le coup joué 
     * correspond à la suggestion optimale du moteur Stockfish.
     * </p>
     * * @param gameId Identifiant unique de la partie à analyser.
     * @return Liste des coups mis à jour avec leur classification (Great, Blunder, etc.).
     */
    @GetMapping("/review/{gameId}")
    public ResponseEntity<List<MoveEntity>> reviewGame(@PathVariable String gameId) {
        List<MoveEntity> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);

        if (moves.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Score initial standard : +20 centipions (avantage Blancs par défaut au coup 1)
        double previousScore = 20.0; 

        for (MoveEntity move : moves) {
            if (move.getEvalScore() == null) continue;

            double currentScore = move.getEvalScore();
            boolean isWhiteTurn = "w".equalsIgnoreCase(move.getTurn()) || "White".equalsIgnoreCase(move.getTurn());
            
            double scoreForPlayer_Prev;
            double scoreForPlayer_Curr;

            // Inversion du score pour l'analyse relative au joueur
            if (isWhiteTurn) {
                scoreForPlayer_Prev = previousScore;
                scoreForPlayer_Curr = currentScore;
            } else {
                scoreForPlayer_Prev = -previousScore;
                scoreForPlayer_Curr = -currentScore;
            }

            // Vérification si le coup joué correspond au meilleur coup suggéré
            boolean isBestMove = move.getStockfishBestMove() != null &&
                                 move.getPlayedMove() != null &&
                                 move.getStockfishBestMove().trim().startsWith(move.getPlayedMove().trim());

            // Appel au service de classification pour déterminer la qualité du coup
            MoveClassification classification = reviewService.classifyMove(
                    scoreForPlayer_Prev, 
                    scoreForPlayer_Curr, 
                    isBestMove
            );

            move.setClassification(classification);
            previousScore = currentScore;
        }

        moveRepository.saveAll(moves);

        return ResponseEntity.ok(moves);
    }
}