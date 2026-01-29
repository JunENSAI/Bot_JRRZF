package com.chess.jr_bot.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "games", schema = "chess_bot")
@Data
public class HistoricalGameEntity {

    @Id
    @Column(name = "game_id")
    private String gameId;

    @Column(name = "white_player")
    private String whitePlayer;

    @Column(name = "black_player")
    private String blackPlayer;

    @Column(name = "white_elo")
    private Integer whiteElo;

    @Column(name = "black_elo")
    private Integer blackElo;

    @Column(name = "date_played")
    private Date datePlayed; 

    @Column(name = "result")
    private String result;

    @Column(name = "pgn_event")
    private String pgnEvent; 
}