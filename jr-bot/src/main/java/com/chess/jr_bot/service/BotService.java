package com.chess.jr_bot.service;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.chess.jr_bot.entity.MoveEntity;
import com.chess.jr_bot.repository.HistoricalMoveRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service de logique de décision pour l'IA du JR Bot.
 * <p>
 * Ce service implémente un système hybride de choix de coups :
 * 1. Recherche dans une base de données de coups historiques (mémoire).
 * 2. Analyse via un microservice externe utilisant Stockfish en cas d'inconnu
 * ou de coup mémorisé jugé sous-optimal.
 * </p>
 */
@Service
public class BotService {

    private static final Logger logger = Logger.getLogger(BotService.class.getName());

    private final HistoricalMoveRepository HistoricalMoveRepository;
    private final RestTemplate restTemplate;
    private final Random random = new Random();
    
    private final String API_SERVICE = "http://localhost:5000/analyze?fen=";

    /**
     * Constructeur pour l'initialisation du service avec ses dépendances.
     * * @param HistoricalMoveRepository Accès à la base de données des coups.
     * @param restTemplate Client HTTP pour interroger le service Python/Stockfish.
     */
    public BotService(HistoricalMoveRepository HistoricalMoveRepository, RestTemplate restTemplate) {
        this.HistoricalMoveRepository = HistoricalMoveRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Détermine le meilleur coup à jouer pour une position FEN donnée.
     * <p>
     * Le processus suit une hiérarchie de décision :
     * - Extraction de la structure du plateau (sans les compteurs de coups).
     * - Sélection aléatoire parmi les coups connus si l'évaluation est supérieure à -200.
     * - Recours automatique à Stockfish si la position est nouvelle ou la mémoire est mauvaise.
     * </p>
     * * @param currentFen La position actuelle au format FEN.
     * @return Le coup sélectionné en notation UCI (ex: "e2e4").
     */
    public String decideMove(String currentFen) {
        String fenKey = currentFen.split(" ")[0];
        
        // 1. Recherche en memoire (Mon style)
        List<MoveEntity> knownMoves = HistoricalMoveRepository.findByFenStartingWith(fenKey);

        if (!knownMoves.isEmpty()) {
            MoveEntity memoryMove = knownMoves.get(random.nextInt(knownMoves.size()));

            if (memoryMove.getEvalScore() != null && memoryMove.getEvalScore() > -200) {
                System.out.println("Coup en mémoire : " + memoryMove.getPlayedMove());
                return memoryMove.getPlayedMove();
            }
            System.out.println("Correction : Coup historique jugé mauvais. Appel à l'IA.");
        } else {
            System.out.println("Nouveau coup : Appel à l'IA.");
        }

        return askStockfish(currentFen);
    }

    /**
     * Interroge le microservice Python pour obtenir une analyse de Stockfish.
     * * @param fen La position à analyser.
     * @return Le meilleur coup suggéré par l'IA ou "0000" en cas d'erreur réseau/parsing.
     */
    private String askStockfish(String fen) {
        try {
            String response = restTemplate.getForObject(API_SERVICE + fen, String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            
            String bestMove = root.path("best_move").asText();
            System.out.println("STOCKFISH SUGGÈRE : " + bestMove);
            
            return bestMove;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur", e);
            return "0000";
        }
    }

    /**
     * Sélectionne un coup d'ouverture parmi les positions de départ enregistrées.
     * <p>
     * Cette méthode permet au bot de varier ses débuts de partie lorsqu'il joue les blancs.
     * Si aucune ouverture n'est enregistrée, le bot joue par défaut "d2d4" (Pion Dame).
     * </p>
     * * @return Un coup d'ouverture en notation UCI.
     */
    public String getOpeningMove() {
        String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"; 
        
        List<MoveEntity> openingMoves = HistoricalMoveRepository.findByFenStartingWith(startFen);

        if (openingMoves.isEmpty()) {
            return "d2d4"; 
        }
        
        MoveEntity selected = openingMoves.get(random.nextInt(openingMoves.size()));
        System.out.println("BOT (avec les blancs) : " + selected.getPlayedMove());
        
        return selected.getPlayedMove();
    }
}