package com.chess.jr_bot.controller;

import com.chess.jr_bot.entity.MoveEntity;
import com.chess.jr_bot.entity.MoveClassification;
import com.chess.jr_bot.repository.HistoricalMoveRepository;
import com.chess.jr_bot.service.GameReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur avancé pour l'analyse et la revue de parties.
 * <p>
 * Ce composant orchestre la classification individuelle des coups, la détection 
 * des coups théoriques (Book moves) et le calcul du score de précision (Accuracy)
 * pour les deux camps.
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
     * Effectue une revue exhaustive de la partie et génère un rapport de précision.
     * <p>
     * Le processus inclut :
     * 1. La normalisation des scores selon le trait (Blanc ou Noir).
     * 2. Le nettoyage des suggestions Stockfish (gestion du ponder).
     * 3. La classification de chaque coup via le GameReviewService.
     * 4. Le calcul d'un score de précision global sur 100 pour chaque joueur.
     * </p>
     * * @param gameId L'identifiant de la partie à analyser.
     * @return Une Map contenant les précisions respectives et la liste des coups classifiés.
     */
    @GetMapping("/review/{gameId}")
    public ResponseEntity<Map<String, Object>> reviewGame(@PathVariable String gameId) {
        
        List<MoveEntity> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);

        if (moves.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        double previousScore = 20.0; 
        
        List<MoveClassification> whiteMovesClassif = new ArrayList<>();
        List<MoveClassification> blackMovesClassif = new ArrayList<>();

        for (MoveEntity move : moves) {
            
            if (move.getEvalScore() == null) {
                MoveClassification bookCheck = reviewService.classifyMove(0, 0, false, move.getFen());
                if (bookCheck == MoveClassification.BOOK) {
                    move.setClassification(MoveClassification.BOOK);
                }
                continue; 
            }

            double currentScore = move.getEvalScore();
            boolean isWhiteTurn = "w".equalsIgnoreCase(move.getTurn()) || "White".equalsIgnoreCase(move.getTurn());

            double scoreForPlayer_Prev;
            double scoreForPlayer_Curr;

            if (isWhiteTurn) {
                scoreForPlayer_Prev = previousScore;
                scoreForPlayer_Curr = currentScore;
            } else {
                scoreForPlayer_Prev = -previousScore;
                scoreForPlayer_Curr = -currentScore;
            }

            String played = move.getPlayedMove();
            String best = move.getStockfishBestMove();
            boolean isBestMove = false;

            if (played != null && best != null) {
                String cleanPlayed = played.trim().toLowerCase();
                String cleanBest = best.trim().toLowerCase();
                if (cleanBest.contains(" ")) cleanBest = cleanBest.split(" ")[0]; 
                isBestMove = cleanPlayed.equals(cleanBest);
            }

            MoveClassification classification = reviewService.classifyMove(
                    scoreForPlayer_Prev, 
                    scoreForPlayer_Curr, 
                    isBestMove,
                    move.getFen()
            );

            move.setClassification(classification);

            if (isWhiteTurn) {
                whiteMovesClassif.add(classification);
            } else {
                blackMovesClassif.add(classification);
            }

            previousScore = currentScore;
        }

        double whiteAcc = reviewService.calculateGameAccuracy(whiteMovesClassif);
        double blackAcc = reviewService.calculateGameAccuracy(blackMovesClassif);

        moveRepository.saveAll(moves);

        Map<String, Object> response = new HashMap<>();
        response.put("whiteAccuracy", whiteAcc);
        response.put("blackAccuracy", blackAcc);
        response.put("moves", moves);

        return ResponseEntity.ok(response);
    }
}