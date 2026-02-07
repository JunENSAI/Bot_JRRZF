package com.chess.jr_bot.service;

import com.chess.jr_bot.dto.Openings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpeningService {

    private final Map<String, String> bookMoves = new HashMap<>();

    @PostConstruct
    public void loadOpenings() {
        try {
            System.out.println(" Chargement du livre d'ouvertures...");
            
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("json/openings.json");
            InputStream inputStream = resource.getInputStream();

            List<Openings> openings = mapper.readValue(inputStream, new TypeReference<List<Openings>>(){});

            for (Openings entry : openings) {
                bookMoves.put(entry.getFen(), entry.getName());
            }

            System.out.println(" Livre chargé : " + bookMoves.size() + " positions connues.");

        } catch (IOException e) {
            System.err.println(" Erreur lors du chargement de openings.json : " + e.getMessage());
        }
    }

    /**
     * Vérifie si la position actuelle est une ouverture connue ("Book").
     * @param fen Le FEN de la position APRÈS le coup joué.
     * @return Le nom de l'ouverture si trouvé, sinon null.
     */
    public String getOpeningName(String fen) {
        if (fen == null) return null;
        return bookMoves.get(fen);
    }

    /**
     * Version simplifiée qui ignore les compteurs de coups (plus robuste).
     */
    public boolean isBookMove(String fen) {
        return bookMoves.containsKey(fen);
    }
    
}
