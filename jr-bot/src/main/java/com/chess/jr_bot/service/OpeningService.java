package com.chess.jr_bot.service;

import com.chess.jr_bot.dto.Openings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de reconnaissance des ouvertures d'échecs.
 * <p>
 * Charge un dictionnaire d'ouvertures au démarrage et permet d'identifier
 * si une position donnée correspond à une variante théorique répertoriée.
 * </p>
 */
@Service
public class OpeningService {

    /** Table de hachage associant un FEN normalisé au nom de son ouverture. */
    private final Map<String, String> bookMoves = new HashMap<>();

    /**
     * Charge le fichier JSON des ouvertures dès l'initialisation du service.
     * <p>
     * Le service tente de lire le fichier dans le dossier statique ou à la racine
     * des ressources, puis normalise chaque entrée pour une recherche efficace.
     * </p>
     */
    @PostConstruct
    public void loadOpenings() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("static/json/openings.json");
            
            if (!resource.exists()) {
                resource = new ClassPathResource("openings.json");
            }

            if (resource.exists()) {
                InputStream inputStream = resource.getInputStream();
                List<Openings> openings = mapper.readValue(inputStream, new TypeReference<List<Openings>>(){});

                for (Openings entry : openings) {
                    String cleanFen = normalizeFen(entry.getFen());
                    bookMoves.put(cleanFen, entry.getName());
                }
            }
        } catch (IOException e) {
            // Log d'erreur simplifié
        }
    }

    /**
     * Vérifie si une position appartient à la "théorie" (Book moves).
     * * @param fen La position actuelle au format FEN.
     * @return true si la position est reconnue dans le dictionnaire.
     */
    public boolean isBookMove(String fen) {
        if (fen == null) return false;
        String cleanFen = normalizeFen(fen);
        return bookMoves.containsKey(cleanFen);
    }

    /**
     * Récupère le nom usuel de l'ouverture pour une position donnée.
     * * @param fen La position actuelle au format FEN.
     * @return Le nom de l'ouverture (ex: "Sicilian Defense") ou null si inconnu.
     */
    public String getOpeningName(String fen) {
        if (fen == null) return null;
        return bookMoves.get(normalizeFen(fen));
    }

    /**
     * Normalise une chaîne FEN pour faciliter la comparaison.
     * <p>
     * Un FEN d'échecs contient 6 segments. Cette méthode ne conserve que les 4 premiers 
     * (placement, trait, droits de roque, case en passant) et ignore les compteurs 
     * de demi-coups et de coups complets qui n'altèrent pas l'identité de l'ouverture.
     * </p>
     * * @param fen Le FEN brut à traiter.
     * @return Le FEN normalisé (théorique).
     */
    private String normalizeFen(String fen) {
        if (fen == null) return "";
        String[] parts = fen.trim().split(" ");
        if (parts.length >= 4) {
            return parts[0] + " " + parts[1] + " " + parts[2] + " " + parts[3];
        }
        return fen;
    }
}