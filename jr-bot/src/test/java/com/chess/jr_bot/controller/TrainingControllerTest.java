package com.chess.jr_bot.controller;

import com.chess.jr_bot.entity.MoveEntity;
import com.chess.jr_bot.repository.HistoricalMoveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le contrôleur d'entraînement.
 * <p>
 * Valide le comportement des points d'entrée de génération de puzzles
 * en simulant des requêtes HTTP et en vérifiant les réponses JSON
 * basées sur les données réelles de la base de test.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HistoricalMoveRepository repository;

    /**
     * Nettoyage de la base de données H2 avant chaque exécution de test.
     */
    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    /**
     * Valide le retour d'un puzzle avec un code 200 lorsqu'une position correspondante existe.
     */
    @Test
    void retournePuzzleSiPositionExistante() throws Exception {
        MoveEntity puzzle = new MoveEntity();
        puzzle.setGameId("game_test_1");
        puzzle.setFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
        puzzle.setEvalScore(300);
        puzzle.setMoveNumber(10);
        puzzle.setPlayedMove("e2e4");
        puzzle.setTurn("White");
        repository.save(puzzle);

        mockMvc.perform(get("/api/training/puzzle")
                .param("type", "winning")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("game_test_1"))
                .andExpect(jsonPath("$.evalScore").value(300));
    }

    /**
     * Valide le retour d'une erreur 404 lorsque la base de données est vide
     * ou qu'aucune position ne correspond au type demandé.
     */
    @Test
    void retourneErreur404SiAucunPuzzleTrouve() throws Exception {
        mockMvc.perform(get("/api/training/puzzle")
                .param("type", "winning"))
                .andExpect(status().isNotFound());
    }
}