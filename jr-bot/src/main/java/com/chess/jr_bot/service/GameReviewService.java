package com.chess.jr_bot.service;

import com.chess.jr_bot.entity.MoveClassification;
import com.chess.jr_bot.service.OpeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service d'analyse qualitative des coups et de calcul de performance.
 * <p>
 * Ce service utilise des modèles mathématiques quadratiques pour définir des
 * seuils de perte (centipions) dynamiques, permettant de classifier la qualité
 * des coups selon le contexte de la partie.
 * </p>
 */
@Service
public class GameReviewService {

    @Autowired
    private OpeningService openingService;

    /**
     * Classifie un coup en fonction de la perte d'avantage (loss) subie.
     * <p>
     * L'algorithme applique une logique de seuils dynamiques. Plus la position est 
     * déséquilibrée (eval élevée), plus le moteur est "tolérant" envers une perte 
     * de points, car l'issue de la partie est déjà presque décidée.
     * </p>
     * * @param prevEval      Évaluation avant le coup (du point de vue du joueur).
     * @param currentEval   Évaluation après le coup (du point de vue du joueur).
     * @param isBestMove    Indique si le coup correspond à la suggestion Stockfish.
     * @param resultingFen  Position résultante pour la vérification théorique.
     * @return La {@link MoveClassification} correspondante.
     */
    public MoveClassification classifyMove(double prevEval, double currentEval, boolean isBestMove, String resultingFen) {
        
        // Priorité 1 : Coup théorique
        if (openingService.isBookMove(resultingFen)) {
            return MoveClassification.BOOK;
        }

        // Limitation de l'analyse aux bornes de [-10.0, +10.0] pions
        double clampedPrev = Math.max(-1000, Math.min(1000, prevEval));
        double clampedCurr = Math.max(-1000, Math.min(1000, currentEval));

        // Calcul de la perte d'avantage (une perte positive signifie une erreur)
        double loss = clampedPrev - clampedCurr;
        if (loss < 0) loss = 0;

        // Priorité 2 : Coup optimal
        if (isBestMove) {
            return MoveClassification.BEST;
        }

        double absPrevEval = Math.abs(clampedPrev);

        // Algorithme de seuils dynamiques (Modèle quadratique)
        // Les constantes permettent d'ajuster la sévérité de l'analyse
        double thresholdExcellent = 0.0002 * Math.pow(absPrevEval, 2) + 0.1231 * absPrevEval + 27.5455;
        double thresholdGood      = 0.0002 * Math.pow(absPrevEval, 2) + 0.2643 * absPrevEval + 60.5455;
        double thresholdInaccuracy= 0.0002 * Math.pow(absPrevEval, 2) + 0.3624 * absPrevEval + 108.0909;
        double thresholdMistake   = 0.0002 * Math.pow(absPrevEval, 2) + 0.4027 * absPrevEval + 225.8182;

        if (loss <= thresholdExcellent) return MoveClassification.EXCELLENT;
        if (loss <= thresholdGood)      return MoveClassification.GOOD;
        if (loss <= thresholdInaccuracy)return MoveClassification.INACCURACY;
        if (loss <= thresholdMistake)   return MoveClassification.MISTAKE;

        return MoveClassification.BLUNDER;
    }

    /**
     * Calcule la précision globale d'une partie (Weighted Accuracy).
     * <p>
     * Utilise les coefficients définis dans l'énumération {@link MoveClassification}
     * pour produire une note sur 100 reflétant la qualité globale du jeu.
     * </p>
     * * @param classifications Liste des classifications de la partie.
     * @return Score de précision (0.0 à 100.0).
     */
    public double calculateGameAccuracy(List<MoveClassification> classifications) {
        if (classifications.isEmpty()) return 0.0;

        double totalScore = 0.0;
        double count = 0.0;

        for (MoveClassification cls : classifications) {
            totalScore += cls.getAccuracyScore() * 100;
            count++;
        }

        return totalScore / count;
    }
}