package com.chess.jr_bot.dto;

/**
 * Projection utilisée pour récupérer les statistiques de performance d'un coup spécifique.
 * <p>
 * Cette interface permet à Spring Data JPA de mapper les résultats des agrégations
 * SQL (requêtes de statistiques d'ouvertures) en objets exploitables par le Frontend.
 * </p>
 */
public interface OpeningStats {

    /**
     * @return Le coup joué en notation UCI (ex: "e2e4").
     */
    String getMove();

    /**
     * @return La position FEN associée à ce stade de l'ouverture.
     */
    String getFen();

    /**
     * @return Le nombre total de fois où ce coup a été joué.
     */
    Long getTotal();

    /**
     * @return Le nombre de victoires obtenues avec ce coup.
     */
    Long getWins();

    /**
     * @return Le nombre de parties nulles obtenues avec ce coup.
     */
    Long getDraws();

    /**
     * @return Le nombre de défaites subies avec ce coup.
     */
    Long getLosses();
}