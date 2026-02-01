package com.chess.jr_bot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chess.jr_bot.entity.MoveEntity;
import com.chess.jr_bot.repository.HistoricalMoveRepository;

/**
 * Contrôleur dédié aux modules d'entraînement et d'apprentissage.
 * <p>
 * Ce composant permet d'extraire des situations de jeu spécifiques
 * à partir de la base de données historique pour créer des puzzles interactifs.
 * </p>
 */
@RestController
@RequestMapping("/api/training")
public class TrainingController {

    private final HistoricalMoveRepository moveRepository;

    /**
     * Initialise le contrôleur avec l'accès aux données de coups historiques.
     * * @param repository Repository contenant la logique de sélection aléatoire des coups.
     */
    public TrainingController(HistoricalMoveRepository moveRepository) {
        this.moveRepository = moveRepository;
    }

    /**
     * Génère un puzzle aléatoire basé sur une thématique de jeu choisie.
     * <p>
     * Le type de puzzle influence la requête SQL :
     * - "opening" : Se concentre sur les coups de début de partie (tours 3 à 15).
     * - "endgame" : Cible les positions de fins de parties (sans dames).
     * - "winning" : Sélectionne des positions avec un avantage matériel ou positionnel fort.
     * </p>
     * * @param type La catégorie de l'entraînement ("opening", "endgame", "winning").
     * @return Une {@link ResponseEntity} contenant un {@link MoveEntity} ou un statut 404.
     */
    @GetMapping("/puzzle")
    public ResponseEntity<MoveEntity> getPuzzle(@RequestParam String type) {
        MoveEntity puzzle = moveRepository.findRandomPuzzle(type);
        
        if (puzzle == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(puzzle);
    }
}