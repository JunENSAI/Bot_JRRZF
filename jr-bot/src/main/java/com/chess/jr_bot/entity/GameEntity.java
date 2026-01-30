package com.chess.jr_bot.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Représente l'archive d'une partie terminée sur la plateforme.
 * <p>
 * Cette entité stocke le résumé historique, incluant les joueurs,
 * le résultat final et l'intégralité des coups au format PGN.
 * </p>
 */
@Entity
@Table(name = "platform_games", schema = "chess_bot")
@Data
public class GameEntity {
    
    /**
     * Identifiant unique de la partie archivée.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nom d'utilisateur du joueur ayant conduit les pièces blanches.
     */
    @Column(name = "white_player")
    private String whitePlayer;

    /**
     * Nom d'utilisateur du joueur ayant conduit les pièces noires.
     */
    @Column(name = "black_player")
    private String blackPlayer;

    /**
     * Résultat de la partie.
     * <p>Valeurs standards : "1-0" (Blancs gagnent), "0-1" (Noirs gagnent), "1/2-1/2" (Nul).</p>
     */
    private String result;

    /**
     * Historique complet de la partie en notation PGN (Portable Game Notation).
     * Stocké sous forme de texte brut pour permettre la relecture de la partie.
     */
    @Column(name = "pgn_text", columnDefinition = "TEXT")
    private String pgnText;

    /**
     * Cadence de jeu appliquée (ex: "Blitz 3 min", "Rapid 10 min").
     */
    @Column(name = "time_control")
    private String timeControl;

    /**
     * Date et heure précises de l'enregistrement de la partie.
     * Initialisé par défaut au moment de la création de l'instance.
     */
    @Column(name = "date_played")
    private LocalDateTime datePlayed = LocalDateTime.now();
}