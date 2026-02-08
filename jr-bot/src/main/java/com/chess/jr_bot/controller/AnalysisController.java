package com.chess.jr_bot.controller;

import com.chess.jr_bot.entity.MoveEntity;
import com.chess.jr_bot.entity.MoveClassification;
import com.chess.jr_bot.repository.HistoricalMoveRepository;
import com.chess.jr_bot.service.GameReviewService;
import com.chess.jr_bot.service.EvaluationService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur maître pour l'analyse et la revue de parties d'échecs.
 * <p>
 * Gère l'importation des évaluations externes et orchestre la classification 
 * contextuelle des coups (incluant la détection des coups brillants/supers).
 * </p>
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private HistoricalMoveRepository moveRepository;

    @Autowired
    private GameReviewService reviewService;

    @Autowired
    private EvaluationService importService;

    /**
     * Déclenche l'importation des évaluations depuis le fichier JSON.
     * @return Statut de l'import (succès ou erreur).
     */
    @GetMapping("/import-json")
    public String triggerImport() {
        return importService.importEvaluations();
    }

    /**
     * Effectue une revue exhaustive d'une partie.
     * <p>
     * Cette méthode parcourt chronologiquement les coups et maintient un état 
     * des classifications précédentes pour permettre au service de détecter 
     * les coups dits "Great" (réfutation immédiate d'une gaffe adverse).
     * </p>
     * @param gameId Identifiant de la partie.
     * @return Map contenant les scores de précision (Accuracy) et les coups classifiés.
     */
    @GetMapping("/review/{gameId}")
    public ResponseEntity<Map<String, Object>> reviewGame(@PathVariable String gameId) {
        
        List<MoveEntity> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);

        if (moves.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Score de départ (avantage théorique blanc de 0.20)
        double previousScore = 20.0; 
        
        // Suivi de l'état précédent pour la logique contextuelle
        MoveClassification lastWhiteClassif = MoveClassification.BOOK;
        MoveClassification lastBlackClassif = MoveClassification.BOOK;
        
        List<MoveClassification> whiteMovesClassif = new ArrayList<>();
        List<MoveClassification> blackMovesClassif = new ArrayList<>();

        for (MoveEntity move : moves) {
            
            // Gestion des coups sans analyse Stockfish (fallback sur la théorie)
            if (move.getEvalScore() == null) {
                MoveClassification bookCheck = reviewService.classifyMove(
                    0.0, 0.0, null, false, move.getFen(), MoveClassification.BOOK
                );
                
                if (bookCheck == MoveClassification.BOOK) {
                    move.setClassification(MoveClassification.BOOK);
                }
                continue; 
            }

            // Préparation des scores relatifs au joueur actif
            double currentScore = move.getEvalScore();
            boolean isWhiteTurn = "w".equalsIgnoreCase(move.getTurn()) || "White".equalsIgnoreCase(move.getTurn());

            double scoreForPlayer_Prev;
            double scoreForPlayer_Curr;
            Integer rawSecondBestInt = move.getSecondBestEval();
            Double rawSecondBest = (rawSecondBestInt != null) ? rawSecondBestInt.doubleValue() : null;
            Double secondBestForPlayer = null;

            if (isWhiteTurn) {
                scoreForPlayer_Prev = previousScore;
                scoreForPlayer_Curr = currentScore;
                if (rawSecondBest != null) secondBestForPlayer = rawSecondBest;
            } else {
                scoreForPlayer_Prev = -previousScore;
                scoreForPlayer_Curr = -currentScore;
                if (rawSecondBest != null) secondBestForPlayer = -rawSecondBest;
            }

            // Détection de la correspondance avec le meilleur coup suggéré
            String played = move.getPlayedMove();
            String best = move.getStockfishBestMove();
            boolean isBestMove = false;

            if (played != null && best != null) {
                String cleanPlayed = played.trim().toLowerCase();
                String cleanBest = best.trim().toLowerCase();
                if (cleanBest.contains(" ")) cleanBest = cleanBest.split(" ")[0];
                isBestMove = cleanPlayed.equals(cleanBest);
            }

            // Identification de la qualité du dernier coup de l'adversaire
            MoveClassification lastOpponentClassif = isWhiteTurn ? lastBlackClassif : lastWhiteClassif;

            // Appel de l'algorithme de classification multicritères
            MoveClassification classification = reviewService.classifyMove(
                    scoreForPlayer_Prev, 
                    scoreForPlayer_Curr,
                    secondBestForPlayer,
                    isBestMove,
                    move.getFen(),
                    lastOpponentClassif
            );

            move.setClassification(classification);

            // Mise à jour de l'historique de classification pour le tour suivant
            if (isWhiteTurn) {
                whiteMovesClassif.add(classification);
                lastWhiteClassif = classification;
            } else {
                blackMovesClassif.add(classification);
                lastBlackClassif = classification;
            }

            previousScore = currentScore;
        }

        // Calcul final de l'Accuracy pour chaque joueur
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