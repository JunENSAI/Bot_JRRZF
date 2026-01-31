package com.chess.jr_bot.dto;

import lombok.Data;

/**
 * Objet de transfert de données (DTO) pour les tentatives de connexion.
 * <p>
 * Utilisé par le contrôleur d'authentification pour encapsuler les identifiants
 * envoyés par le client lors d'une requête de type login.
 * </p>
 */
@Data
public class Login {

    /**
     * Nom d'utilisateur ou pseudonyme du compte à authentifier.
     */
    private String username;

    /**
     * Mot de passe associé au compte, transmis en texte brut 
     * pour vérification côté serveur.
     */
    private String password;
}