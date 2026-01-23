import os
import chess.pgn
import sys

SOURCE_DIR = "/home/junior/Fine_Tuning_Stockfish/pgn_files"
OUTPUT_FILE = "/home/junior/Fine_Tuning_Stockfish/user_pgn.pgn"

def merge_pgns():
    seen_games = set()
    
    count_total = 0
    count_kept = 0

    files = [f for f in os.listdir(SOURCE_DIR) if f.lower().endswith(".pgn")]

    with open(OUTPUT_FILE, "w", encoding="utf-8") as out_f:
        print(f"Début de la fusion de {len(files)} fichiers...")

        for filename in sorted(files):
            if filename == os.path.basename(OUTPUT_FILE):
                continue

            file_path = os.path.join(SOURCE_DIR, filename)
            print(f"Traitement de {filename}...", end=" ")
            
            with open(file_path, "r", encoding="utf-8") as in_f:
                while True:
                    try:
                        game = chess.pgn.read_game(in_f)
                    except ValueError:
                        continue

                    if game is None:
                        break 

                    count_total += 1
                    headers = game.headers
                    unique_id = (
                        headers.get("Date", ""),
                        headers.get("White", ""),
                        headers.get("Black", ""),
                        headers.get("EndTime", "")
                    )

                    if unique_id in seen_games:
                        continue
                    
                    seen_games.add(unique_id)
                    count_kept += 1
                    print(game, file=out_f, end="\n\n")
            
            print("OK.")

    print("-" * 50)
    print(f"Terminé !")
    print(f"Parties lues au total : {count_total}")
    print(f"Parties uniques gardées : {count_kept}")
    print(f"Fichier généré : {OUTPUT_FILE}")

if __name__ == "__main__":
    merge_pgns()