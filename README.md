Stockfish est le bot ultime en terme de jeux d'echecs. Il est opensource donc quoi de mieux que de l'entraîner sur mes propres donnés pour apprendre avec lui les bons coups.

# Configuration minimale

## Python 

- Créer un environnement virtuel pour ne pas avoir de problème de dépendances

```bash
python3 -m venv bot_jr
```
- Activer l'environnement virtuel que vous venez de créer :

```bash
source ./Fine_Tuning_Stockfish/jr_bot/bin/activate
```

- Installez les librairies nécessaires pour le bon fonctionnement des fichiers python

```bash
cd Fine_Tuning_Stockfish
pip install -r requirements.txt
```

## Stockfish

- Disponible en téléchargement via les dépots `apt` avec les commandes :

```bash
sudo apt-get update

sudo apt-get install stockfish
```

- Verification de l'installation :

```bash
which stockfish
```

### Petit historique : Les Echecs

Je ne suis que joueur ammateur donc je vais expliquer ce que j'ai compris en général.

Les echecs c'est un jeu de plateau de 8 cases par 8 cases dont :

- en colonnes on trouve des lettres allant de `a` à `h`

- en lignes ce sont les chiffres allant de `1` à `8`

![plateau](img/chessboard.png)


Le but du jeu est de `mater` le roi adverse. Un roi est dit échec et mat lorsqu’il est en échec et qu’aucun coup légal, ni du roi ni d’une autre pièce, ne permet de lever cet échec.

Cette situation est souvent confondue avec le `pat`, où le roi n’est ni en échec ni mat, mais où le joueur n’a aucun coup légal à jouer alors que c’est à lui de jouer ; la partie est alors déclarée **nulle**.

### Les notations chesscom

Les notations du jeu sont en anglais et ça porte confusion si on ne sait pas.

- K : King (le roi)

- Q : Queen (la reine)

- R : Rook (la tour)

- N : Knight (le cavalier)

- B : Bishop (le fou)

Les pions sont directement représentés selon la ligne et la colonne où ils se trouvent.

### La position de départ

Vous avez :

- 8 pions : seconde rangée (pour les blancs) et septième rangée (pour les noirs)

- 1 Roi : placé en e1 (pour les blancs) et e8 (pour les noirs)

- 1 Reine : placé en d1 (pour les blancs) et d8 (pour les noirs)

- 2 Tours : en a1 et h1 (pour les blancs) et a8 et h8 (pour les noirs)

- 2 cavaliers : en b1 et g1 (pour les blancs) et b8 et g8 (pour les noirs)

- 2 fous : en c1 et f1 (pour les blancs) et c8 et f8 (pour les noirs)

![position_depart](img/initial_pos.png)


## Structure du répo

### data_pgn

Contient les **5675** parties que j'ai eu à jouer sur mon compte personnel sur la plateforme `chess.com`.

Le fichier est enregistré sous un format `.pgn` (**P**ortable **G**ame **N**otation) dont la structure est comme ceci :

```pgn
[Event "Live Chess"]  <-- partie en temps réel
[Site "Chess.com"]    <-- site du jeu
[Date "2025.07.22"]   <-- date à laquelle la partie a été jouée
[Round "-"]   <-- pas de valeur parce que c'était pas en tournoi
[White "user_name"]  <-- utilisateur avec les pièces blanches
[Black "user_name"]  <-- utilisateur avec les pièces noires
[Result "0-1"]   <-- résultats de la partie
[WhiteElo "1850"]  <-- le classement elo du joueur aux blancs
[BlackElo "1844"]  <-- le classement elo du joueur aux noirs
[TimeControl "180"]   <-- la cadence de la partie (180 secondes ici)
[EndTime "17:22:21 GMT+0000"]  <-- l'heure à laquelle la partie s'est terminée
[Termination "user_name won by checkmate"]   <-- Comment la partie s'est terminée

1. d4 e6 2. Nf3 d5 3. Bf4 Nf6 4. e3 c5 5. Be2 Nc6 6. dxc5 Bxc5 7. c3 Qb6 8. Qc2
Bd7 9. Be5 Nxe5 10. Nxe5 Bd6 11. Nxd7 Nxd7 12. O-O O-O 13. Nd2 Rac8 14. Nf3 h6
15. h3 Ne5 16. Rfd1 Nxf3+ 17. Bxf3 a6 18. Rd4 Be5 19. Rb4 Qc7 20. Qb3 b5 21. a4
Bd6 22. Rd4 Rb8 23. axb5 Rxb5 24. Qa4 Rxb2 25. Qxa6 Rfb8 26. Rb4 Bxb4 27. cxb4
R2xb4 28. Rf1 Rb1 29. Be2 Rxf1+ 30. Bxf1 Rc8 31. h4 Qc1 32. g3 Rc2 33. Qa8+ Kh7
34. Qf8 Qe1 35. Qxf7 Rc1 36. Kg2 Qxf1+ 37. Kf3 Qh1+ 38. Kf4 Qe4# 0-1
```

Explication des notations (pas en entier vu que ça prendrait trop de lignes) :

- 1. d4 e6 : le 1 représente le premier coup (comment la partie a debuté). d4 représente la ligne **4** et la colonne **d** (coup joué par les blancs). e6 représente la ligne **6** et la colonne **e** (coup répondu par les noirs).

- Les symbôles :

    - \+ : c'est pour dire que le roi est en echec (le roi est menacé sur sa position)

    - x : ça veut dire capture (par exemple `Qxf7` : Queen captures a pawn on line 7 and column f).

    - O-O : veut dire petit roque (le roi part se cacher), l'action se fait avec la tour (noté R).

    - \# : c'est le signe de l'echec et mat (le roi est en echec mais n'a plus de case de fuite alors que c'est à son tour de jouer).

## Java

### Configuration et Installation

Sous Linux pour installer java il suffit de taper les commandes suivantes :

```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

Pour verifier que java a été bien installé sur votre ordinateur :
```bash
java --version
```

Pour initialiser un projet Springboot java vous pouvez générer un zip contenant directement le fichier `pom.xml` ,l'arborescence et les dépendances dont vous aurez besoin via le site : https://start.spring.io/ .

Un exemple de configuration serait :

```properties
Project : maven
Language : Java
Spring Boot : 4.0.2
Project Metadata : {
    Group : com.chess
    Artifact : jr-bot
    Name : jr-bot
    Description : Bot stockfish avec mes parties
    Package name : com.chess.jr_bot
    Packaging : Jar
    Configuration : Properties
    Java : 21
}
Dependencies : {
    Spring Web : Permet de créer des applications web et des API REST
    Spring Data JPA : Simplifie l'accès aux données avec JPA/Hibernate
    ProgreSQL Driver : Driver JDBC pour se connecter à PostgreSQL
    Lombok : Réduit le code boilerplate avec des annotations
}
```

Une fois généré dezippez le dossier directement à la racine de votre projet git. Ensuite installez `maven` :

```bash
sudo apt install maven
```

Vous pouvez lancer l'application telle qu'elle est :
```bash
cd jr-bot
mvn spring-boot:run
```

Pour lancer l'application avec de la donnée sur Postgres il faut rajouter dans `src/main/resources/application.properties` les configurations suivantes :

```properties
server.port=8080 
spring.datasource.url=jdbc:postgresql://localhost:5432/bot_db
spring.datasource.username=jr_bot
spring.datasource.password=Ton_mdp
spring.jpa.properties.hibernate.default_schema=chess_bot
```

Le port 8080 doit être changé si vous avez par exemple `Jenkins` présent sur ce port.

- `server.port = 8080` : port de lancement de l'application

- `spring.datasource.url=jdbc:postgresql://localhost:5432/bot_db` : bot_db a été crée dans l console progres (doit être crée en amont)

- `spring.datasource.username` : ma logique est chaque utilisateur postgres sa base (donc on crée un user avec un username et un mot de passe. Par la suite on lui attribue une base de donnée bot_db).

- `spring.jpa.properties.hibernate.default_schema=chess_bot` : le schema est celui crée dans DBeaver a l'aide du fichier `init_db.sql`.

#### Pourquoi Java (Spring Boot) au lieu de tout faire en Python ?

On pourrait créer les notions orientées objets directement à travers Python mais je trouve que Java est mieux pour gérer tout ce qui est attrait à la POO.

- **Côté performance** :  capabilité de gérer plusieurs réquêtes à la fois grâce à son modèle de Threading robuste.

- **Typage fort** : en essayant d'envoyer un Texte là où le code attend un Nombre, Java t'arrête avant même de lancer l'application (à la compilation).

- **Architecture "Microservices"**

    - `Python` : se concentre sur la communication avec stockfish et realise les calculs et recueille les données envoyés par celui ci

    - `Java` : se concentre sur la geestion des utilisateur, des parties, de la base de donnée. Communique avec python pour les coups pensé par stockfish pour le côté bot.

### Classes 

1. **Controller (Points d'entrée API)** : Ces classes exposent les endpoints HTTP accessibles par le Frontend (React).

    - **`BotController.java`** : 
        - Gère l'intelligence du Bot.
        - `/api/bot/move` : Reçoit un FEN et renvoie le meilleur coup.
        - `/api/bot/start` : Gère l'initialisation si le Bot joue les Blancs.

    - **`AuthController.java`** : 
        - Gère la sécurité.
        - `/api/auth/login` : Vérifie le pseudo/mot de passe de l'utilisateur.

    - **`GameController.java`** : 
        - Gère les données de la plateforme.
        - `/api/platform/save` : Sauvegarde une partie terminée.
        - `/api/platform/stats` : Calcule et renvoie les statistiques agrégées (Historique + Récent).

2. **Service (Logique Métier)**

    - **`BotService.java`** : **La classe la plus importante.**
        - Contient l'algorithme de décision :
            1. Regarde dans PostgreSQL si une position est connue.
            2. Si connue et jugée "bonne", renvoie le coup historique (mimétisme).
            3. Si inconnue ou erreur historique, appelle l'API Python (Stockfish).
        - Gère aussi l'ouverture variée via pondération (Weighted Random).

3. Entity (Modèle de Données) :Ces classes représentent les tables de la base de données PostgreSQL (ORM Hibernate).

    - **`MoveEntity.java`** : 
        - Mappe la table `moves`. Contient un FEN, le coup joué, et le score Stockfish.

    - **`UserEntity.java`** : 
        - Mappe la table `app_users`. Stocke les identifiants (username/password).

    - **`GameEntity.java`** : 
        - Mappe la table `platform_games`. Stocke les parties jouées sur le site (PGN complet, résultat, date).
    
4. **Repository (Accès Données)** : Interfaces gérées par Spring Data JPA pour interagir avec SQL sans écrire de requêtes manuelles.

    - **`MoveRepository.java`** : Permet de trouver des coups par FEN (`findByFenStartingWith`).

    - **`AppUserRepository.java`** : Permet de trouver un user par son pseudo.

    - **`PlatformGameRepository.java`** : Contient des requêtes personnalisées (`@Query`) pour compter les victoires et le total de parties.

5. **DTO (Data Transfer Objects)** : Objets utilisés pour transporter les données proprement entre le Frontend et le Backend.

    - **`Login.java`** : Structure JSON reçue lors du login `{username, password}`.

    - **`GameSave.java`** : Structure JSON reçue à la fin d'une partie `{pgn, result, white, black...}`.


## React JS
