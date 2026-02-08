-- Table pour les utilisateurs (Login)
CREATE TABLE chess_bot.app_users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table pour les parties jouées SUR LA PLATEFORME
CREATE TABLE chess_bot.platform_games (
    id SERIAL PRIMARY KEY,
    white_player VARCHAR(50),
    black_player VARCHAR(50),
    result VARCHAR(10),
    pgn_text TEXT,
    time_control VARCHAR(50),
    date_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- utilisateur test
INSERT INTO chess_bot.app_users (username, password) VALUES ('Martin', 'Ineverdo0209');

CREATE TABLE chess_bot.platform_moves (
    id SERIAL PRIMARY KEY,
    game_id INT REFERENCES chess_bot.platform_games(id) ON DELETE CASCADE,
    
    fen VARCHAR(255) NOT NULL,
    turn VARCHAR(10),
    move_number INT,
    played_move VARCHAR(10),
    stockfish_best_move VARCHAR(10),
    
    eval_score INT,
    second_best_eval INT,

    classification VARCHAR(50),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);