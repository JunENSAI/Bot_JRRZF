package com.chess.jr_bot.dto;

import lombok.Data;

/**
 * Objet de transfert de données (DTO) utilisé pour l'enregistrement d'une partie.
 * <p>
 * Ce conteneur regroupe les informations finales d'une confrontation afin de
 * persister l'historique dans la base de données via le contrôleur dédié.
 * </p>
 */
@Data
public class GameSave {

    /**
     * Pseudonyme du joueur contrôlant les pièces blanches.
     */
    private String whitePlayer;

    /**
     * Pseudonyme du joueur contrôlant les pièces noires.
     */
    private String blackPlayer;

    /**
     * Issue de la rencontre (ex: "1-0", "0-1", "1/2-1/2").
     */
    private String result;

    /**
     * Transcription complète de la partie en notation PGN.
     */
    private String pgn;

    /**
     * Format de temps utilisé durant la partie (ex: "5+0", "Bullet").
     */
    private String timeControl;
}