import os
import chess
import chess.pgn
import chess.engine
import pandas as pd
from dotenv import load_dotenv
import sys

load_dotenv()

PGN_FILE = "/home/junior/Fine_Tuning_Stockfish/data_pgn/user_pgn.pgn" 

MY_USERNAME = os.getenv("user_name")

STOCKFISH_PATH = "/usr/games/stockfish" 


DEPTH = 14 

def analyze_pgn():
    print(f"Chargement du moteur Stockfish depuis : {STOCKFISH_PATH}")
    try:
        engine = chess.engine.SimpleEngine.popen_uci(STOCKFISH_PATH)
    except FileNotFoundError:
        print("ERREUR : Stockfish introuvable. Vérifie le chemin STOCKFISH_PATH.")
        return

    data_rows = []
    games_processed = 0
    
    print(f"Lecture du fichier : {PGN_FILE}...")
    
    games_data = [] # Liste pour la table 'games'
    moves_data = [] # Liste pour la table 'moves'

    seen_game_ids = set()
    
    with open(PGN_FILE) as pgn:
        while True:
            game = chess.pgn.read_game(pgn)
            if game is None: break

            headers = game.headers
            white = headers.get("White", "?")
            black = headers.get("Black", "?")
            date_played = headers.get("Date", "1970.01.01").replace(".", "-")
            
            # ID unique pour la partie
            game_id = f"{date_played}_{white}_{black}".replace(" ", "_")

            if game_id in seen_game_ids:
                continue 

            seen_game_ids.add(game_id)

            games_data.append({
                "game_id": game_id,
                "white_player": white,
                "black_player": black,
                "white_elo": headers.get("WhiteElo", 0),
                "black_elo": headers.get("BlackElo", 0),
                "date_played": date_played,
                "result": headers.get("Result", "*"),
                "opening_code": headers.get("ECO", ""),
                "pgn_event": headers.get("Event", "")
            })

            # Identification du joueur 
            if white == MY_USERNAME:
                my_color = chess.WHITE
            elif black == MY_USERNAME:
                my_color = chess.BLACK
            else:
                continue 

            board = game.board()
            
            for node in game.mainline():
                if board.turn == my_color:
                    
                    info = engine.analyse(board, chess.engine.Limit(depth=DEPTH))
                    best_move = info["pv"][0].uci() if "pv" in info else ""
                    
                    score_obj = info["score"].white() if board.turn == chess.WHITE else info["score"].black()
                    score_val = score_obj.score(mate_score=10000)
                    
                    moves_data.append({
                        "game_id": game_id,
                        "fen": board.fen(),
                        "turn": "white" if board.turn == chess.WHITE else "black",
                        "move_number": board.fullmove_number,
                        "played_move": node.move.uci(),
                        "stockfish_best_move": best_move,
                        "eval_score": score_val
                    })

                board.push(node.move)

    engine.quit()

    pd.DataFrame(games_data).to_csv("games.csv", index=False)
    pd.DataFrame(moves_data).to_csv("moves.csv", index=False)
    print("Export terminé : games.csv et moves.csv générés.")

if __name__ == "__main__":
    analyze_pgn()