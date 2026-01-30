package com.chess.jr_bot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Représente un compte utilisateur au sein du système JR Bot.
 * <p>
 * Cette entité stocke les informations d'identification nécessaires à
 * l'accès sécurisé à l'application et au suivi des parties.
 * </p>
 */
@Entity
@Table(name = "app_users", schema = "chess_bot")
@Data
public class UserEntity {
    
    /**
     * Identifiant unique de l'utilisateur.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nom d'utilisateur unique utilisé pour la connexion.
     * Ce champ ne peut pas être nul et garantit l'unicité des comptes.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Mot de passe de l'utilisateur.
     * <p>Note : Dans un environnement de production, ce champ devrait stocker
     * une version hachée du mot de passe.</p>
     */
    @Column(nullable = false)
    private String password;
}