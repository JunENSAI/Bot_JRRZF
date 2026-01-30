package com.chess.jr_bot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chess.jr_bot.dto.OpeningStats;
import com.chess.jr_bot.entity.MoveEntity;

/**
 * Repository pour l'accès aux données historiques des coups.
 * <p>
 * Gère les requêtes de recherche par position (FEN) et calcule des statistiques
 * avancées sur les ouvertures et la performance des joueurs.
 * </p>
 */
public interface HistoricalMoveRepository extends JpaRepository<MoveEntity, Long> {

    /**
     * Récupère tous les coups associés à une position FEN exacte.
     * * @param fen La chaîne FEN complète.
     * @return Liste des coups enregistrés pour cette position.
     */
    List<MoveEntity> findByFen(String fen);

    /**
     * Recherche des coups en ignorant les compteurs de fin de FEN.
     * * @param fenPart La partie initiale du FEN (disposition des pièces).
     * @return Liste des coups correspondant à la structure du plateau.
     */
    @Query("SELECT m FROM MoveEntity m WHERE m.fen LIKE :fenPart%")
    List<MoveEntity> findByFenStartingWith(@Param("fenPart") String fenPart);

    /**
     * Reconstitue la séquence chronologique des coups d'une partie spécifique.
     * * @param gameId L'identifiant de la partie.
     * @return Liste ordonnée des coups du premier au dernier.
     */
    List<MoveEntity> findByGameIdOrderByMoveNumberAsc(String gameId);

    /**
     * Calcule les statistiques de performance des ouvertures pour les Blancs.
     * <p>
     * Agrège les résultats (victoires, nuls, défaites) pour le premier coup joué 
     * par un joueur spécifique avec les pièces blanches.
     * </p>
     * * @param playerName Nom du joueur à analyser.
     * @return Liste d'objets {@link OpeningStats} triée par fréquence d'utilisation.
     */
    @Query(value = """
        SELECT m.played_move as move,
               COUNT(*) as total,
               SUM(CASE WHEN g.result = '1-0' THEN 1 ELSE 0 END) as wins,
               SUM(CASE WHEN g.result = '1/2-1/2' THEN 1 ELSE 0 END) as draws,
               SUM(CASE WHEN g.result = '0-1' THEN 1 ELSE 0 END) as losses
        FROM chess_bot.moves m
        JOIN chess_bot.games g ON m.game_id = g.game_id
        WHERE m.move_number = 1 
          AND (m.turn = 'White' OR m.turn = 'w') 
          AND g.white_player = :player
        GROUP BY m.played_move
        ORDER BY total DESC
    """, nativeQuery = true)
    List<OpeningStats> getWhiteOpeningStats(@Param("player") String playerName);

    /**
     * Calcule les statistiques de performance des ouvertures pour les Noirs.
     * <p>
     * Agrège les résultats pour les réponses jouées par un joueur spécifique
     * avec les pièces noires au premier coup.
     * </p>
     * * @param playerName Nom du joueur à analyser.
     * @return Liste d'objets {@link OpeningStats} incluant le FEN et le coup joué.
     */
    @Query(value = """
        SELECT mb.fen as fen, 
               mb.played_move as move,
               COUNT(*) as total,
               SUM(CASE WHEN g.result = '0-1' THEN 1 ELSE 0 END) as wins,
               SUM(CASE WHEN g.result = '1/2-1/2' THEN 1 ELSE 0 END) as draws,
               SUM(CASE WHEN g.result = '1-0' THEN 1 ELSE 0 END) as losses
        FROM chess_bot.moves mb
        JOIN chess_bot.games g ON mb.game_id = g.game_id
        WHERE mb.move_number = 1 
          AND LOWER(mb.turn) = 'black' 
          AND LOWER(g.black_player) = LOWER(:player)
        GROUP BY mb.fen, mb.played_move
        ORDER BY total DESC
    """, nativeQuery = true)
    List<OpeningStats> getBlackOpeningStats(@Param("player") String playerName);

    /**
     * Sélectionne aléatoirement un coup selon une thématique spécifique (Puzzle).
     * <p>
     * Types supportés :
     * - 'opening' : Coups joués entre le tour 3 et 15.
     * - 'endgame' : Positions sans reines sur le plateau.
     * - 'winning' : Positions avec un avantage significatif (eval > 200).
     * </p>
     * * @param type Le type de situation recherchée.
     * @return Une instance de {@link MoveEntity} choisie au hasard.
     */
    @Query(value = """
        SELECT * FROM chess_bot.moves m
        WHERE 
        (:type = 'opening' AND m.move_number >= 3 AND m.move_number <= 15)
        OR
        (:type = 'endgame' AND m.fen NOT LIKE '%Q%' AND m.fen NOT LIKE '%q%')
        OR
        (:type = 'winning' AND ABS(m.eval_score) > 200)
        AND m.fen IS NOT NULL
        ORDER BY RANDOM()
        LIMIT 1
    """, nativeQuery = true)
    MoveEntity findRandomPuzzle(@Param("type") String type);
}