package com.chess.jr_bot.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class StockfishService {

    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;

    private static final String ENGINE_PATH = "/usr/games/stockfish"; 

    public void startEngine() {
        try {
            ProcessBuilder pb = new ProcessBuilder(ENGINE_PATH);
            this.process = pb.start();
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            
            sendCommand("uci");
        } catch (IOException e) {
            System.err.println("❌ Impossible de démarrer Stockfish : " + e.getMessage());
        }
    }

    public void stopEngine() {
        if (process != null) {
            sendCommand("quit");
            process.destroy();
        }
    }

    private void sendCommand(String command) {
        try {
            writer.write(command + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Analyse une position FEN donnée et retourne un objet résultat contenant le score et le meilleur coup.
     * @param fen La notation FEN de la position.
     * @param depth La profondeur d'analyse (ex: 15 ou 20).
     */
    public StockfishResult analyze(String fen, int depth) {
        if (process == null || !process.isAlive()) {
            startEngine();
        }

        sendCommand("position fen " + fen);
        sendCommand("go depth " + depth);

        String bestMove = null;
        Integer evalScore = null;
        Integer secondBestEval = null; // Pour le futur (MultiPV)

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                // Analyse de la ligne d'info (score)
                if (line.contains("score")) {
                    evalScore = parseScore(line);
                }
                
                // Fin de l'analyse
                if (line.startsWith("bestmove")) {
                    bestMove = line.split(" ")[1];
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new StockfishResult(evalScore, bestMove, secondBestEval);
    }

    private Integer parseScore(String line) {
        try {
            String[] parts = line.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("score")) {
                    String type = parts[i + 1]; // "cp" ou "mate"
                    int val = Integer.parseInt(parts[i + 2]);
                    
                    // Si c'est un mat, on convertit en grand score
                    if (type.equals("mate")) {
                        return val > 0 ? 10000 - val : -10000 + val;
                    }
                    // Si c'est le tour des noirs, Stockfish donne le score du point de vue blanc
                    // mais pour MoveEntity on veut souvent le score absolu ou relatif selon ta convention.
                    // Ici on retourne brut, le contrôleur gère l'inversion si besoin.
                    return val;
                }
            }
        } catch (Exception e) { }
        return null;
    }

    // Classe interne simple pour transporter le résultat
    public static class StockfishResult {
        public Integer eval;
        public String bestMove;
        public Integer secondEval;

        public StockfishResult(Integer eval, String bestMove, Integer secondEval) {
            this.eval = eval;
            this.bestMove = bestMove;
            this.secondEval = secondEval;
        }
    }
}