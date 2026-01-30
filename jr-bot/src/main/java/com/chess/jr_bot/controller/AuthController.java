package com.chess.jr_bot.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.jr_bot.dto.Login;
import com.chess.jr_bot.entity.UserEntity;
import com.chess.jr_bot.repository.UserRepository;

/**
 * Contrôleur REST gérant les flux d'authentification des utilisateurs.
 * <p>
 * Fournit les points d'entrée pour la validation des identifiants et la gestion
 * des sessions au sein de l'écosystème JR Bot.
 * </p>
 * * @author JunENSAI
 * @version 1.0
 */

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    /**
     * Initialise le contrôleur avec les accès aux données nécessaires.
     * * @param userRepository Interface d'accès à la table des utilisateurs.
     */
    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Traite la tentative de connexion d'un utilisateur.
     * <p>
     * Vérifie l'existence du pseudonyme et la correspondance du mot de passe.
     * Cette méthode retourne un succès uniquement si les deux critères sont validés.
     * </p>
     * * <pre>
     * Exemple de retour (200 OK) :
     * {
     * "message": "Login success",
     * "username": "joueur1",
     * "userId": 1
     * }
     * </pre>
     * * @param request Objet DTO contenant les informations de connexion.
     * @return Une {@link ResponseEntity} contenant les données utilisateur en cas de succès,
     * ou un statut 401 (Unauthorized) avec un message d'erreur.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login request) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            if (user.getPassword().equals(request.getPassword())) {
                return ResponseEntity.ok(Map.of(
                    "message", "Login success",
                    "username", user.getUsername(),
                    "userId", user.getId()
                ));
            }
        }
        
        return ResponseEntity.status(401).body(Map.of("message", "Mauvais identifiants"));
    }
}