package com.chess.jr_bot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Objet de transfert de données pour le dictionnaire d'ouvertures.
 * <p>
 * Utilisé pour désérialiser les fichiers JSON contenant la théorie des 
 * débuts de partie, permettant au système d'identifier les coups et les 
 * positions célèbres.
 * </p>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Openings {

    /** * Code ECO (Encyclopédie des ouvertures d'échecs).
     * Ex: "C42" pour la Défense Petrov.
     */
    private String eco;

    /** * Nom complet de l'ouverture ou de la variante.
     * Ex: "Sicilian Defense: Najdorf Variation".
     */
    private String name;

    /** * Position de l'échiquier au format FEN correspondant à cette ouverture.
     */
    private String fen;

    /** * Liste des coups au format SAN ayant mené à cette position.
     * Ex: "1. e4 e5 2. Nf3 Nf6".
     */
    private String moves;
}