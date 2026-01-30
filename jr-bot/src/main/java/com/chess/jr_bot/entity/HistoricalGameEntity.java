package com.chess.jr_bot.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Représente une partie issue d'une base de données historique externe.
 * <p>
 * Contrairement aux parties jouées sur la plateforme, cette entité est conçue 
 * pour stocker des parties de référence (ex: bases Lichess ou tournois pro) 
 * afin d'enrichir les connaissances du bot sur les niveaux de jeu et l'Elo.
 * </p>
 */
@Entity
@Table(name = "games", schema = "chess_bot")
@Data
public class HistoricalGameEntity {

    /**
     * Identifiant unique de la partie, souvent une clé externe (ex: ID Lichess).
     */
    @Id
    @Column(name = "game_id")
    private String gameId;

    /**
     * Nom du joueur ayant conduit les pièces blanches.
     */
    @Column(name = "white_player")
    private String whitePlayer;

    /**
     * Nom du joueur ayant conduit les pièces noires.
     */
    @Column(name = "black_player")
    private String blackPlayer;

    /**
     * Classement Elo du joueur blanc au moment de la partie.
     */
    @Column(name = "white_elo")
    private Integer whiteElo;

    /**
     * Classement Elo du joueur noir au moment de la partie.
     */
    @Column(name = "black_elo")
    private Integer blackElo;

    /**
     * Date de l'événement au format SQL Date.
     */
    @Column(name = "date_played")
    private Date datePlayed; 

    /**
     * Résultat de la confrontation (ex: "1-0", "0-1", "1/2-1/2").
     */
    @Column(name = "result")
    private String result;

    /**
     * Nom de l'événement ou du tournoi associé à cette partie.
     */
    @Column(name = "pgn_event")
    private String pgnEvent;
}