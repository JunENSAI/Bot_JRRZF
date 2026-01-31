package com.chess.jr_bot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.jr_bot.entity.UserEntity;

/**
 * Interface d'accès aux données pour les utilisateurs du système.
 * <p>
 * Fournit les méthodes standard de manipulation CRUD pour l'entité {@link UserEntity}
 * et permet la récupération des profils par identifiant unique.
 * </p>
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Recherche un utilisateur par son pseudonyme exact.
     * <p>
     * Cette méthode est principalement utilisée lors du processus d'authentification
     * pour valider l'existence d'un compte avant la vérification du mot de passe.
     * </p>
     * * @param username Le nom d'utilisateur recherché.
     * @return Un {@link Optional} contenant l'utilisateur s'il existe, ou vide dans le cas contraire.
     */
    Optional<UserEntity> findByUsername(String username);
}