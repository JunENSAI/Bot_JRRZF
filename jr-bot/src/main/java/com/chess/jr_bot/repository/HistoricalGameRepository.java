package com.chess.jr_bot.repository;

import com.chess.jr_bot.entity.HistoricalGameEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoricalGameRepository extends JpaRepository<HistoricalGameEntity, String> {

    @Query(value = """
        SELECT * FROM chess_bot.games 
        WHERE (LOWER(white_player) = 'jrrzf' OR LOWER(black_player) = 'jrrzf')
        AND (
            LOWER(white_player) LIKE LOWER(CONCAT('%', :search, '%')) 
            OR 
            LOWER(black_player) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY date_played DESC
    """, 
    countQuery = """
        SELECT count(*) FROM chess_bot.games 
        WHERE (LOWER(white_player) = 'jrrzf' OR LOWER(black_player) = 'jrrzf')
        AND (
            LOWER(white_player) LIKE LOWER(CONCAT('%', :search, '%')) 
            OR 
            LOWER(black_player) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """,
    nativeQuery = true)
    Page<HistoricalGameEntity> searchGames(@Param("search") String search, Pageable pageable);
}