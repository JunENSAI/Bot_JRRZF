package com.chess.jr_bot.dto;

/**
 * Conteneur de statistiques agrégées pour un joueur ou une ouverture.
 * <p>
 * Cette classe permet de compiler les résultats des parties (victoires, défaites, nuls)
 * et de calculer automatiquement l'issue en fonction de la couleur des pièces jouées.
 * </p>
 */
public class Stats {

    /** Nombre de victoires accumulées. */
    public int wins = 0;

    /** Nombre de défaites accumulées. */
    public int losses = 0;

    /** Nombre de parties nulles accumulées. */
    public int draws = 0;

    /** Nombre total de parties traitées. */
    public int total = 0;

    /**
     * Incrémente les compteurs en fonction du résultat de la partie et de la couleur.
     * * @param result Le score de la partie (ex: "1-0", "0-1", "1/2-1/2").
     * @param isPlayerWhite Indique si le joueur suivi jouait les Blancs.
     */
    public void addResult(String result, boolean isPlayerWhite) {
        total++;
        if ("1/2-1/2".equals(result)) {
            draws++;
        } else if ((isPlayerWhite && "1-0".equals(result)) || (!isPlayerWhite && "0-1".equals(result))) {
            wins++;
        } else {
            losses++;
        }
    }
}