package com.chess.jr_bot.service;

import com.chess.jr_bot.entity.MoveClassification;
import com.chess.jr_bot.service.OpeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service d'analyse expert pour la classification des coups d'échecs.
 * <p>
 * Ce service implémente une logique de révision profonde incluant :
 * - La détection des opportunités de mat manquées.
 * - L'identification des coups forcés (quand le deuxième meilleur coup est nettement inférieur).
 * - La revalorisation des coups ("Great Move") après une gaffe adverse.
 * </p>
 */
@Service
public class GameReviewService {

    @Autowired
    private OpeningService openingService;

    /**
     * Algorithme de classification multicritères (Mat, Centipions, Contexte).
     * * @param prevEval        Évaluation avant le coup (en centipions).
     * @param currentEval     Évaluation après le coup.
     * @param secondBestEval  Évaluation du deuxième meilleur coup suggéré (pour détecter les coups forcés).
     * @param isBestMove      Le coup joué est-il le premier choix de l'IA ?
     * @param resultingFen    FEN après le coup pour la détection théorique.
     * @param lastClassif     Classification du coup précédent (pour détecter les "Great Moves").
     * @return La {@link MoveClassification} la plus appropriée.
     */
    public MoveClassification classifyMove(double prevEval, double currentEval, Double secondBestEval,
                                           boolean isBestMove, String resultingFen, MoveClassification lastClassif) {

        // 1. Priorité absolue : Théorie (Book)
        if (openingService.isBookMove(resultingFen)) return MoveClassification.BOOK;

        // 2. Gestion des séquences de Mat
        boolean prevIsMate = Math.abs(prevEval) > 9000;
        boolean currIsMate = Math.abs(currentEval) > 9000;

        if (prevIsMate && !currIsMate) {
            // Mat raté (Blunder ou Mistake selon la perte d'avantage restante)
            if (prevEval > 0 && currentEval > 0) {
                 if (currentEval < 500) return MoveClassification.BLUNDER;
                 return MoveClassification.MISTAKE;
            }
            return MoveClassification.BLUNDER;
        }

        if (prevIsMate && currIsMate) {
            // Mat maintenu ou accéléré
            if (currentEval >= prevEval) return MoveClassification.BEST; 
            return MoveClassification.GOOD;
        }

        // 3. Logique de perte de centipions (clamping à +/- 10.0 pions)
        double clampedPrev = Math.max(-1000, Math.min(1000, prevEval));
        double clampedCurr = Math.max(-1000, Math.min(1000, currentEval));
        double loss = Math.max(0, clampedPrev - clampedCurr);

        // 4. Détection des coups forcés
        if (secondBestEval != null) {
            double diffWithSecond = prevEval - secondBestEval;
            if (isBestMove && diffWithSecond > 200) return MoveClassification.FORCED;
        }

        // 5. Classification standard via seuils quadratiques
        MoveClassification classification = MoveClassification.BLUNDER;
        if (isBestMove) {
            classification = MoveClassification.BEST;
        } else {
            if (loss <= getThreshold(MoveClassification.BEST, clampedPrev)) classification = MoveClassification.BEST;
            else if (loss <= getThreshold(MoveClassification.EXCELLENT, clampedPrev)) classification = MoveClassification.EXCELLENT;
            else if (loss <= getThreshold(MoveClassification.GOOD, clampedPrev)) classification = MoveClassification.GOOD;
            else if (loss <= getThreshold(MoveClassification.INACCURACY, clampedPrev)) classification = MoveClassification.INACCURACY;
            else if (loss <= getThreshold(MoveClassification.MISTAKE, clampedPrev)) classification = MoveClassification.MISTAKE;
        }

        // 6. Logique "Great Move" (Super coup)
        // Se déclenche si on trouve un coup fort après une erreur adverse ou dans une position tendue
        if (classification == MoveClassification.BEST || classification == MoveClassification.EXCELLENT) {
             boolean noMate = Math.abs(currentEval) < 2000;
             if (noMate 
                 && lastClassif == MoveClassification.BLUNDER 
                 && secondBestEval != null
                 && (prevEval - secondBestEval) >= 150) {
                 return MoveClassification.GREAT;
             }
        }

        // 7. Sécurité pour les positions déjà décisives (+/- 6.0 pions)
        // On ne classifie pas comme gaffe un coup qui maintient un avantage gagnant
        if (classification == MoveClassification.BLUNDER && currentEval >= 600) return MoveClassification.GOOD;
        if (classification == MoveClassification.BLUNDER && prevEval <= -600) return MoveClassification.GOOD;

        return classification;
    }

    /**
     * Calcule le seuil de tolérance dynamique selon la phase de jeu.
     */
    private double getThreshold(MoveClassification expected, double prevEval) {
        double absEval = Math.abs(prevEval);
        switch (expected) {
            case BEST: return 0.0001 * Math.pow(absEval, 2) + (0.0236 * absEval) - 3.7143;
            case EXCELLENT: return 0.0002 * Math.pow(absEval, 2) + 0.1231 * absEval + 27.5455;
            case GOOD: return 0.0002 * Math.pow(absEval, 2) + 0.2643 * absEval + 60.5455;
            case INACCURACY: return 0.0002 * Math.pow(absEval, 2) + 0.3624 * absEval + 108.0909;
            case MISTAKE: return 0.0003 * Math.pow(absEval, 2) + 0.4027 * absEval + 225.8182;
            default: return 0.0;
        }
    }

    /**
     * Calcul de la précision moyenne de la partie.
     */
    public double calculateGameAccuracy(List<MoveClassification> classifications) {
        if (classifications.isEmpty()) return 0.0;
        double totalScore = 0.0;
        for (MoveClassification cls : classifications) {
            totalScore += cls.getAccuracyScore() * 100;
        }
        return totalScore / classifications.size();
    }
}