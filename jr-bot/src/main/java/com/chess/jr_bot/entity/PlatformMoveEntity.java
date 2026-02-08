package com.chess.jr_bot.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "platform_moves", schema = "chess_bot")
@Data
public class PlatformMoveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "game_id")
    private Integer gameId;

    private String fen;
    private String turn;

    @Column(name = "move_number")
    private Integer moveNumber;

    @Column(name = "played_move")
    private String playedMove;

    @Column(name = "stockfish_best_move")
    private String stockfishBestMove;

    @Column(name = "eval_score")
    private Integer evalScore;

    @Column(name = "second_best_eval")
    private Integer secondBestEval;

    @Enumerated(EnumType.STRING)
    private MoveClassification classification;
}