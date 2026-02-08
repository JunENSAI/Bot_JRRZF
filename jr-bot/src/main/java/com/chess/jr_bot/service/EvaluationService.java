package com.chess.jr_bot.service;

import com.chess.jr_bot.dto.Evaluation;
import com.chess.jr_bot.entity.MoveEntity;
import com.chess.jr_bot.repository.HistoricalMoveRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

/**
 * Service d'importation massive des analyses Stockfish.
 * <p>
 * Ce service lit un fichier JSON contenant des évaluations multi-lignes (Top Lines)
 * et met à jour les entrées correspondantes dans la base de données. Il est 
 * essentiel pour activer les fonctionnalités avancées de revue de partie (Game Review).
 * </p>
 */
@Service
public class EvaluationService {

    @Autowired
    private HistoricalMoveRepository moveRepository;

    /**
     * Importe et synchronise les évaluations JSON avec la base de données.
     * <p>
     * Le processus suit les étapes suivantes :
     * 1. Désérialisation d'une structure JSON complexe (Liste de Listes).
     * 2. Correspondance par FEN (Position unique sur l'échiquier).
     * 3. Extraction du meilleur coup (ID 1) et du score d'évaluation.
     * 4. Extraction du second meilleur coup (ID 2) pour l'analyse comparative.
     * 5. Gestion spécifique des scores de "Mat" vs "Centipions" (CP).
     * </p>
     * * @return Un message de statut indiquant le succès ou l'échec de l'opération.
     */
    @Transactional
    public String importEvaluations() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("static/json/evaluations.json");

            if (!resource.exists()) {
                return " Fichier evaluations.json introuvable !";
            }

            InputStream inputStream = resource.getInputStream();
            // Structure : Liste de parties, chaque partie contenant une liste de positions évaluées
            List<List<Evaluation>> allGames = mapper.readValue(inputStream, new TypeReference<List<List<Evaluation>>>(){});

            int updatedCount = 0;

            for (List<Evaluation> gamePositions : allGames) {
                for (Evaluation entry : gamePositions) {
                    
                    // Récupération des entités correspondantes à la position FEN
                    List<MoveEntity> moves = moveRepository.findByFen(entry.getFen());
                    if (moves.isEmpty()) continue;

                    var topLines = entry.getTopLines();
                    if (topLines == null || topLines.isEmpty()) continue;

                    // Identification des deux meilleures lignes d'analyse
                    var bestLine = topLines.stream().filter(l -> l.getId() == 1).findFirst().orElse(null);
                    var secondLine = topLines.stream().filter(l -> l.getId() == 2).findFirst().orElse(null);

                    for (MoveEntity moveDb : moves) {
                        boolean modified = false;

                        // Traitement de la ligne principale (Best Move)
                        if (bestLine != null && bestLine.getEvaluation() != null) {
                            moveDb.setStockfishBestMove(bestLine.getMoveUCI());
                            
                            // Normalisation des scores de Mat (Mate) pour le calcul mathématique
                            if ("mate".equals(bestLine.getEvaluation().getType())) {
                                int mateScore = bestLine.getEvaluation().getValue() > 0 ? 10000 : -10000;
                                moveDb.setEvalScore(mateScore);
                            } else {
                                moveDb.setEvalScore(bestLine.getEvaluation().getValue());
                            }
                            modified = true;
                        }

                        // Traitement de la ligne secondaire (Crucial pour détecter les coups "Forcés")
                        if (secondLine != null && secondLine.getEvaluation() != null) {
                            if ("cp".equals(secondLine.getEvaluation().getType())) {
                                moveDb.setSecondBestEval(secondLine.getEvaluation().getValue());
                                modified = true;
                            }
                        }

                        if (modified) {
                            moveRepository.save(moveDb);
                            updatedCount++;
                        }
                    }
                }
            }

            return "✅ Import terminé ! " + updatedCount + " coups mis à jour.";

        } catch (Exception e) {
            return " Erreur critique : " + e.getMessage();
        }
    }
}