package com.chess.jr_bot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chess.jr_bot.entity.HistoricalGameEntity;

/**
 * Repository dédié à la consultation des archives de parties historiques.
 * <p>
 * Permet d'effectuer des recherches textuelles complexes sur les joueurs
 * avec un support natif pour la pagination et le tri.
 * </p>
 */
public interface HistoricalGameRepository extends JpaRepository<HistoricalGameEntity, String> {

    /**
     * Recherche paginée des parties impliquant un joueur spécifique et un critère de recherche.
     * <p>
     * Cette requête native filtre les parties où l'utilisateur principal ('jrrzf') 
     * apparaît, tout en permettant une recherche partielle (LIKE) sur les noms des adversaires.
     * </p>
     * * @param search   Chaîne de caractères pour filtrer les noms des joueurs.
     * @param pageable Objet contenant les informations de pagination (page, taille, tri).
     * @return Une {@link Page} de {@link HistoricalGameEntity} correspondant aux critères.
     */
    @Query(value = """
        SELECT * FROM chess_bot.games 
        WHERE (LOWER(white_player) = 'jrrzf' OR LOWER(black_player) = 'jrrzf')
        AND (
            LOWER(white_player) LIKE LOWER(CONCAT('%', :search, '%')) 
            OR 
            LOWER(black_player) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY date_played DESC
    """, 
    countQuery = """
        SELECT count(*) FROM chess_bot.games 
        WHERE (LOWER(white_player) = 'jrrzf' OR LOWER(black_player) = 'jrrzf')
        AND (
            LOWER(white_player) LIKE LOWER(CONCAT('%', :search, '%')) 
            OR 
            LOWER(black_player) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """,
    nativeQuery = true)
    Page<HistoricalGameEntity> searchGames(@Param("search") String search, Pageable pageable);
}