package com.chess.jr_bot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * Objet de transfert de données pour les analyses approfondies de positions.
 * <p>
 * Cette classe mappe la structure JSON issue du moteur d'analyse. Elle inclut
 * la position FEN, les détails du coup joué et les meilleures lignes alternatives
 * calculées par Stockfish.
 * </p>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Evaluation {

    /** Position de l'échiquier au format FEN. */
    private String fen;
    
    /** Détails du coup spécifique associé à cette évaluation. */
    private MoveDto move; 
    
    /** Liste des meilleures lignes d'analyse (Top Lines) trouvées par le moteur. */
    private List<TopLineDto> topLines;

    /**
     * Représentation d'un coup dans différents formats de notation.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MoveDto {
        /** Notation algébrique standard (ex: "Nf3"). */
        private String san; 
        /** Notation UCI (ex: "g1f3"). */
        private String uci; 
    }

    /**
     * Représentation d'une ligne d'analyse suggérée par le moteur.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopLineDto {
        /** Rang de la suggestion (1 = meilleur coup). */
        private int id;
        /** Coup suggéré en notation UCI. */
        private String moveUCI;
        /** Détails du score d'évaluation pour cette ligne. */
        private EvalValueDto evaluation;
    }

    /**
     * Valeur numérique et type de l'évaluation.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EvalValueDto {
        /** Type de score : "cp" (centipions) ou "mate" (mat forcé). */
        private String type;
        /** Valeur de l'évaluation (positive pour l'avantage blanc, négative pour noir). */
        private Integer value;
    }
}