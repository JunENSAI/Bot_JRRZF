package com.chess.jr_bot.controller;

import com.chess.jr_bot.entity.GameEntity;
import com.chess.jr_bot.entity.MoveClassification;
import com.chess.jr_bot.entity.PlatformMoveEntity; // Nouvelle entité
import com.chess.jr_bot.repository.GameRepository;
import com.chess.jr_bot.repository.PlatformMoveRepository; // Nouveau repo
import com.chess.jr_bot.service.GameReviewService;
import com.chess.jr_bot.service.StockfishService;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/platform")
public class PlatformController {

    @Autowired private GameRepository gameRepository;
    @Autowired private PlatformMoveRepository platformMoveRepository; // Injection du bon repo
    @Autowired private GameReviewService reviewService;
    @Autowired private StockfishService stockfishService;

    @GetMapping("/analyze/{gameId}")
    public ResponseEntity<Map<String, Object>> analyzePlatformGame(@PathVariable Integer gameId) {
        
        Optional<GameEntity> gameOpt = gameRepository.findById(Long.valueOf(gameId));
        if (gameOpt.isEmpty()) return ResponseEntity.notFound().build();
        GameEntity game = gameOpt.get();

        // 1. Vérifie si l'analyse existe déjà dans la table platform_moves
        List<PlatformMoveEntity> existingMoves = platformMoveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
        
        // 2. SI PAS D'ANALYSE : On lance le calcul (PGN -> Stockfish)
        if (existingMoves.isEmpty()) {
            try {
                Board board = new Board();
                MoveList pgnMoves = new MoveList();
                pgnMoves.loadFromSan(game.getPgnText()); 

                List<PlatformMoveEntity> newMoves = new ArrayList<>();
                int moveCount = 1;

                for (Move move : pgnMoves) {
                    PlatformMoveEntity entity = new PlatformMoveEntity();
                    entity.setGameId(gameId); // Liaison directe par l'ID entier
                    entity.setMoveNumber(moveCount++);
                    
                    String turn = board.getSideToMove().toString(); 
                    entity.setTurn(turn);
                    
                    board.doMove(move);
                    String fen = board.getFen();
                    entity.setFen(fen);
                    entity.setPlayedMove(move.toString());

                    // Analyse Stockfish
                    StockfishService.StockfishResult analysis = stockfishService.analyze(fen, 15);
                    
                    entity.setEvalScore(analysis.eval);
                    entity.setStockfishBestMove(analysis.bestMove);
                    // entity.setSecondBestEval(analysis.secondEval); // Si dispo

                    newMoves.add(entity);
                }

                platformMoveRepository.saveAll(newMoves);
                existingMoves = newMoves;

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body(Map.of("error", "Erreur analyse: " + e.getMessage()));
            }
        }

        // 3. Appel de la logique de classification
        return runReviewLogic(existingMoves);
    }

    private ResponseEntity<Map<String, Object>> runReviewLogic(List<PlatformMoveEntity> moves) {
        
        double previousScore = 20.0; 
        MoveClassification lastWhiteClassif = MoveClassification.BOOK;
        MoveClassification lastBlackClassif = MoveClassification.BOOK;
        
        List<MoveClassification> whiteMovesClassif = new ArrayList<>();
        List<MoveClassification> blackMovesClassif = new ArrayList<>();

        for (PlatformMoveEntity move : moves) {
            
            if (move.getEvalScore() == null) {
                move.setClassification(MoveClassification.BOOK);
                continue; 
            }

            double currentScore = move.getEvalScore();
            boolean isWhiteTurn = "WHITE".equalsIgnoreCase(move.getTurn()) || "w".equalsIgnoreCase(move.getTurn());

            double scoreForPlayer_Prev;
            double scoreForPlayer_Curr;
            
            // Gestion du 2ème meilleur coup
            Integer rawSecondBestInt = move.getSecondBestEval();
            Double secondBestForPlayer = (rawSecondBestInt != null) 
                ? (isWhiteTurn ? rawSecondBestInt.doubleValue() : -rawSecondBestInt.doubleValue()) 
                : null;

            if (isWhiteTurn) {
                scoreForPlayer_Prev = previousScore;
                scoreForPlayer_Curr = currentScore;
            } else {
                scoreForPlayer_Prev = -previousScore;
                scoreForPlayer_Curr = -currentScore;
            }

            // Détection Meilleur Coup
            boolean isBestMove = false;
            if (move.getPlayedMove() != null && move.getStockfishBestMove() != null) {
                String cleanPlayed = move.getPlayedMove().trim().toLowerCase();
                String cleanBest = move.getStockfishBestMove().trim().toLowerCase();
                if (cleanBest.contains(" ")) cleanBest = cleanBest.split(" ")[0];
                isBestMove = cleanPlayed.equals(cleanBest);
            }

            MoveClassification lastOpponentClassif = isWhiteTurn ? lastBlackClassif : lastWhiteClassif;

            MoveClassification classification = reviewService.classifyMove(
                    scoreForPlayer_Prev, 
                    scoreForPlayer_Curr,
                    secondBestForPlayer,
                    isBestMove,
                    move.getFen(),
                    lastOpponentClassif
            );

            move.setClassification(classification);

            if (isWhiteTurn) {
                whiteMovesClassif.add(classification);
                lastWhiteClassif = classification;
            } else {
                blackMovesClassif.add(classification);
                lastBlackClassif = classification;
            }

            previousScore = currentScore;
        }

        double whiteAcc = reviewService.calculateGameAccuracy(whiteMovesClassif);
        double blackAcc = reviewService.calculateGameAccuracy(blackMovesClassif);

        // Sauvegarde dans la table platform_moves
        platformMoveRepository.saveAll(moves);

        Map<String, Object> response = new HashMap<>();
        response.put("whiteAccuracy", whiteAcc);
        response.put("blackAccuracy", blackAcc);
        response.put("moves", moves);

        return ResponseEntity.ok(response);
    }
}