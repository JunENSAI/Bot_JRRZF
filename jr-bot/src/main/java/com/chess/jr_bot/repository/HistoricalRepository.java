package com.chess.jr_bot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chess.jr_bot.dto.OpeningStats;
import com.chess.jr_bot.entity.HistoricalMoveEntity;

public interface HistoricalRepository extends JpaRepository<HistoricalMoveEntity, Long> {

  List<HistoricalMoveEntity> findByGameIdOrderByMoveNumberAsc(String gameId);

    // Stats pour les Blancs (Move 1, Turn White)
    @Query(value = """
        SELECT m.played_move as move,
               COUNT(*) as total,
               SUM(CASE WHEN g.result = '1-0' THEN 1 ELSE 0 END) as wins,
               SUM(CASE WHEN g.result = '1/2-1/2' THEN 1 ELSE 0 END) as draws,
               SUM(CASE WHEN g.result = '0-1' THEN 1 ELSE 0 END) as losses
        FROM chess_bot.moves m
        JOIN chess_bot.games g ON m.game_id = g.game_id
        WHERE m.move_number = 1 
          AND (m.turn = 'White' OR m.turn = 'w') 
          AND g.white_player = :player
        GROUP BY m.played_move
        ORDER BY total DESC
    """, nativeQuery = true)
    List<OpeningStats> getWhiteOpeningStats(@Param("player") String playerName);

    // Stats pour les Noirs (Move 1, Turn Black)
    @Query(value = """
        SELECT mb.fen as fen, 
               mb.played_move as move,
               COUNT(*) as total,
               SUM(CASE WHEN g.result = '0-1' THEN 1 ELSE 0 END) as wins,
               SUM(CASE WHEN g.result = '1/2-1/2' THEN 1 ELSE 0 END) as draws,
               SUM(CASE WHEN g.result = '1-0' THEN 1 ELSE 0 END) as losses
        FROM chess_bot.moves mb
        JOIN chess_bot.games g ON mb.game_id = g.game_id
        WHERE mb.move_number = 1 
          AND LOWER(mb.turn) = 'black' 
          AND LOWER(g.black_player) = LOWER(:player)
        GROUP BY mb.fen, mb.played_move
        ORDER BY total DESC
    """, nativeQuery = true)
    List<OpeningStats> getBlackOpeningStats(@Param("player") String playerName);

    @Query(value = """
        SELECT * FROM chess_bot.moves m
        WHERE 
        (:type = 'opening' AND m.move_number >= 3 AND m.move_number <= 15)
        OR
        (:type = 'endgame' AND m.fen NOT LIKE '%Q%' AND m.fen NOT LIKE '%q%')
        OR
        (:type = 'winning' AND ABS(m.eval_score) > 200)
        AND m.fen IS NOT NULL
        ORDER BY RANDOM()
        LIMIT 1
    """, nativeQuery = true)
    HistoricalMoveEntity findRandomPuzzle(@Param("type") String type);
}