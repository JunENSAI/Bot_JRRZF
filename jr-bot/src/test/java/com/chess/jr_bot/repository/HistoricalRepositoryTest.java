package com.chess.jr_bot.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.chess.jr_bot.entity.MoveEntity;

@SpringBootTest
@ActiveProfiles("test")
public class HistoricalRepositoryTest {

    @Autowired
    private HistoricalMoveRepository repository;

    @Test
    void shouldFindWinningPuzzle() {
        createAndSaveMove("game1", 10, 300, "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
        createAndSaveMove("game1", 11, 10, "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");

        MoveEntity puzzle = repository.findRandomPuzzle("winning");

        assertThat(puzzle).isNotNull();
        assertThat(puzzle.getEvalScore()).isGreaterThan(200);
    }


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