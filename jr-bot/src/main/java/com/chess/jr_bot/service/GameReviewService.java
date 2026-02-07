package com.chess.jr_bot.service;

import com.chess.jr_bot.entity.MoveClassification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameReviewService {

    @Autowired
    private OpeningService openingService;

    /**
     * Calcule la classification d'un coup basé sur l'évaluation avant et après le coup.
     *
     * @param prevCp Score en centipions AVANT le coup (du point de vue du joueur).
     * @param currentCp Score en centipions APRÈS le coup (du point de vue du joueur).
     * @param isBestMove Si Stockfish dit que c'était le meilleur coup absolu.
     * @return La classification (Blunder, Great, Best, etc.)
     */
    public MoveClassification classifyMove(double prevCp, double currentCp, boolean isBestMove, String resultingFen) {

        if (openingService.isBookMove(resultingFen)) {
            return MoveClassification.BOOK;
        }

        if (isBestMove) {
            return MoveClassification.BEST; 
        }

        double prevWinChance = toWinChance(prevCp);
        double currentWinChance = toWinChance(currentCp);
        double delta = prevWinChance - currentWinChance;

        if (delta <= 0.02) return MoveClassification.EXCELLENT; // Perte <= 2%
        if (delta <= 0.05) return MoveClassification.GOOD;      // Perte <= 5%
        if (delta <= 0.10) return MoveClassification.INACCURACY; // Perte <= 10%
        if (delta <= 0.20) return MoveClassification.MISTAKE;    // Perte <= 20%
        
        return MoveClassification.BLUNDER; // Perte > 20%
    }

    /**
     * Formule Sigmoid pour convertir les Centipions en Chance de Gain (0 à 1).
     * Utilisé par Lichess et inspiré de ce que fait Chess.com.
     */
    private double toWinChance(double cp) {
        if (cp > 1000) cp = 1000;
        if (cp < -1000) cp = -1000;

        return 0.5 + 0.5 * (2 / (1 + Math.exp(-0.00368208 * cp)) - 1);
    }
    
    /**
     * Calcule le score de précision global (Accuracy) de 0 à 100.
     */
    public double calculateGameAccuracy(java.util.List<Double> allMovesWinChanceLoss) {
        if (allMovesWinChanceLoss.isEmpty()) return 0.0;
        
        double sum = 0;
        for (double loss : allMovesWinChanceLoss) {
            sum += (1.0 - loss) * 100;
        }
        return sum / allMovesWinChanceLoss.size();
    }
}