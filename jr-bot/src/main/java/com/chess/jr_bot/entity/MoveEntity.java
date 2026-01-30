package com.chess.jr_bot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Représente un coup enregistré dans la base de données.
 * <p>
 * Cette entité fait le lien entre une position donnée sur l'échiquier (FEN)
 * et les métadonnées de performance associées, permettant au bot d'apprendre
 * ou de rejouer des coups historiques.
 * </p>
 */
@Entity
@Table(name = "moves", schema = "chess_bot")
@Data
public class MoveEntity {

    /**
     * Identifiant unique auto-incrémenté en base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Identifiant de la partie en cours (UUID ou chaîne unique).
     */
    @Column(name = "game_id")
    private String gameId;

    /**
     * État complet du plateau au moment du coup (Notation FEN).
     * Ce champ est obligatoire pour la recherche en mémoire.
     */
    @Column(nullable = false)
    private String fen;

    /**
     * Indique quel camp devait jouer ('w' pour blancs, 'b' pour noirs).
     */
    private String turn;

    /**
     * Numéro du coup dans la séquence de la partie.
     */
    @Column(name = "move_number")
    private Integer moveNumber;

    /**
     * Coup effectivement joué par l'utilisateur ou le bot (Notation UCI).
     */
    @Column(name = "played_move")
    private String playedMove;

    /**
     * Le coup optimal suggéré par Stockfish pour cette position précise.
     */
    @Column(name = "stockfish_best_move")
    private String stockfishBestMove;

    /**
     * Score d'évaluation de la position après le coup joué.
     * <p>
     * Une valeur positive favorise les blancs, une valeur négative les noirs.
     * Est exprimé en centipions (100 = 1 pion d'avantage).
     * </p>
     */
    @Column(name = "eval_score")
    private Integer evalScore;
}