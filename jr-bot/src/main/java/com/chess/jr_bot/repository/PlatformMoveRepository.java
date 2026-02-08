package com.chess.jr_bot.repository;

import com.chess.jr_bot.entity.PlatformMoveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlatformMoveRepository extends JpaRepository<PlatformMoveEntity, Integer> {
    List<PlatformMoveEntity> findByGameIdOrderByMoveNumberAsc(Integer gameId);
}