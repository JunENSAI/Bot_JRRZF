package com.chess.jr_bot.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chess.jr_bot.service.BotService;

/**
 * Interface de communication avec l'intelligence artificielle du JR Bot.
 * <p>
 * Ce contrôleur expose les fonctionnalités de calcul de coups et de gestion
 * des ouvertures selon l'état de la partie.
 * </p>
 */
@RestController
@RequestMapping("/api/bot")
@CrossOrigin(origins = "*")
public class BotController {

    private final BotService botService;

    /**
     * Constructeur injectant le moteur de décision du bot.
     * * @param botService Service contenant l'algorithme de calcul des coups.
     */
    public BotController(BotService botService) {
        this.botService = botService;
    }

    /**
     * Calcule le meilleur coup suivant à partir d'une position donnée.
     * * @param fen La position actuelle du plateau au format Forsyth-Edwards Notation (FEN).
     * @return Le coup choisi par l'IA (format notation algébrique standard).
     */
    @GetMapping("/move")
    public String getMove(@RequestParam String fen) {
        return botService.decideMove(fen);
    }

    /**
     * Initialise la logique de début de partie selon la couleur choisie par le joueur.
     * <p>
     * Si l'utilisateur joue les noirs, le bot (blancs) retourne son premier coup.
     * Sinon, le bot reste en attente de l'action du joueur.
     * </p>
     * * @param userColor Couleur sélectionnée par l'humain ("white" ou "black").
     * @return Le premier coup du bot ou la chaîne "WAITING_FOR_USER".
     */
    @GetMapping("/start")
    public String startGame(@RequestParam String userColor) {
        if ("black".equalsIgnoreCase(userColor)) {
            return botService.getOpeningMove();
        }
        return "WAITING_FOR_USER"; 
    }
}