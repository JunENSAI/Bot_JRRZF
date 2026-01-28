package com.chess.jr_bot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chess.jr_bot.entity.HistoricalGameEntity;

public interface HistoricalGameRepository extends JpaRepository<HistoricalGameEntity, String> {

    @Query("SELECT g FROM HistoricalGameEntity g WHERE " +
           "(g.whitePlayer = :player OR g.blackPlayer = :player) AND " +
           "LOWER(g.pgnEvent) LIKE LOWER(CONCAT('%', :type, '%')) " +
           "ORDER BY g.datePlayed DESC")
    Page<HistoricalGameEntity> findGamesByType(@Param("player") String player, 
                                               @Param("type") String type, 
                                               Pageable pageable);

    // Récupère TOUTES les parties si pas de type
    @Query("SELECT g FROM HistoricalGameEntity g WHERE " +
           "g.whitePlayer = :player OR g.blackPlayer = :player " +
           "ORDER BY g.datePlayed DESC")
    Page<HistoricalGameEntity> findAllGames(@Param("player") String player, Pageable pageable);
}