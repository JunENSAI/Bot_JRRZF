package com.chess.jr_bot.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.chess.jr_bot.entity.MoveEntity;

/**
 * Tests d'intégration pour le repository HistoricalMoveRepository.
 * <p>
 * Cette classe valide le bon fonctionnement des requêtes SQL natives, 
 * en particulier la logique de sélection aléatoire des puzzles selon 
 * des critères spécifiques (score d'évaluation, phase de jeu, matériel).
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
public class HistoricalRepositoryTest {

    @Autowired
    private HistoricalMoveRepository repository;

    /**
     * Vérifie la capacité à extraire un puzzle de type "winning".
     * <p>Le test s'assure que le coup retourné possède un score d'évaluation 
     * strictement supérieur à 200 centipions.</p>
     */
    @Test
    void shouldFindWinningPuzzle() {
        createAndSaveMove("game1", 10, 300, "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
        createAndSaveMove("game1", 11, 10, "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");

        MoveEntity puzzle = repository.findRandomPuzzle("winning");

        assertThat(puzzle).isNotNull();
        assertThat(puzzle.getEvalScore()).isGreaterThan(200);
    }

    /**
     * Vérifie la capacité à extraire un puzzle de type "endgame".
     * <p>Le test valide que la position FEN sélectionnée ne contient aucune Dame 
     * (caractères 'Q' ou 'q'), respectant ainsi la définition d'une finale.</p>
     */
    @Test
    void shouldFindEndgamePuzzle() {
        String endgameFen = "8/8/8/8/8/4k3/4P3/4K3 w - - 0 1"; 
        createAndSaveMove("game2", 40, 50, endgameFen);

        MoveEntity puzzle = repository.findRandomPuzzle("endgame");

        assertThat(puzzle).isNotNull();
        assertThat(puzzle.getFen()).doesNotContain("Q");
        assertThat(puzzle.getFen()).doesNotContain("q");
    }

    /**
     * Vérifie la capacité à extraire un puzzle de type "opening".
     * <p>Le test confirme que le numéro du coup sélectionné se situe 
     * bien dans la plage définie pour l'ouverture (entre le coup 3 et 15).</p>
     */
    @Test
    void shouldFindOpeningPuzzle() {
        createAndSaveMove("game3", 5, 20, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        createAndSaveMove("game3", 20, 20, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        MoveEntity puzzle = repository.findRandomPuzzle("opening");

        assertThat(puzzle).isNotNull();
        assertThat(puzzle.getMoveNumber()).isBetween(3, 15);
    }

    /**
     * Méthode utilitaire pour persister un coup en base de test.
     * * @param gameId Identifiant de la partie.
     * @param moveNumber Rang du coup.
     * @param eval Score d'analyse.
     * @param fen Configuration du plateau.
     */
    private void createAndSaveMove(String gameId, int moveNumber, int eval, String fen) {
        MoveEntity move = new MoveEntity();
        move.setGameId(gameId);
        move.setMoveNumber(moveNumber);
        move.setEvalScore(eval);
        move.setFen(fen);
        move.setPlayedMove("e2e4");
        move.setTurn("White");
        repository.save(move);
    }
}