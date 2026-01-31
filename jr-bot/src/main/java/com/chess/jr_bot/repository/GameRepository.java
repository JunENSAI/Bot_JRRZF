package com.chess.jr_bot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chess.jr_bot.entity.GameEntity;

/**
 * Interface d'accès aux données pour l'historique des parties de la plateforme.
 * <p>
 * Fournit des méthodes pour extraire les performances des joueurs et 
 * gérer l'affichage des listes de parties terminées.
 * </p>
 */
public interface GameRepository extends JpaRepository<GameEntity, Long> {

    /**
     * Récupère toutes les parties impliquant un joueur, qu'il soit blanc ou noir.
     * * @param whitePlayer Nom du joueur (côté blancs).
     * @param blackPlayer Nom du joueur (côté noirs).
     * @return Liste non triée des parties correspondantes.
     */
    List<GameEntity> findByWhitePlayerOrBlackPlayer(String whitePlayer, String blackPlayer);

    /**
     * Récupère l'historique complet d'un joueur, trié par date décroissante.
     * * @param whitePlayer Nom du joueur.
     * @param blackPlayer Nom du joueur.
     * @return Liste des parties de la plus récente à la plus ancienne.
     */
    List<GameEntity> findByWhitePlayerOrBlackPlayerOrderByDatePlayedDesc(String whitePlayer, String blackPlayer);

    /**
     * Récupère les 50 dernières parties jouées par un utilisateur spécifique.
     * <p>Utilisé pour l'affichage de l'historique récent sur le tableau de bord.</p>
     * * @param whitePlayer Nom du joueur.
     * @param blackPlayer Nom du joueur.
     * @return Liste limitée aux 50 derniers enregistrements.
     */
    List<GameEntity> findTop50ByWhitePlayerOrBlackPlayerOrderByDatePlayedDesc(String whitePlayer, String blackPlayer);

    /**
     * Calcule le nombre total de victoires d'un joueur.
     * <p>
     * Une victoire est comptabilisée si le joueur gagne avec les blancs ("1-0")
     * ou avec les noirs ("0-1").
     * </p>
     * * @param player Nom du joueur à analyser.
     * @return Nombre total de victoires.
     */
    @Query("SELECT COUNT(g) FROM GameEntity g WHERE " +
           "(g.whitePlayer = :player AND g.result = '1-0') OR " +
           "(g.blackPlayer = :player AND g.result = '0-1')")
    long countWins(@Param("player") String player);

    /**
     * Calcule le volume total de parties disputées par un utilisateur.
     * * @param player Nom du joueur.
     * @return Nombre total de parties (tous résultats confondus).
     */
    @Query("SELECT COUNT(g) FROM GameEntity g WHERE g.whitePlayer = :player OR g.blackPlayer = :player")
    long countTotalGames(@Param("player") String player);
}