let board = null;
let game = new Chess();
let stockfish = null;
let currentMode = 'winning';
let bestMove = null;
let puzzleActive = false;
let moveSequenceCount = 0; 

document.addEventListener("DOMContentLoaded", () => {
    initBoard();
    initStockfish();
    loadNewPuzzle();
});

function setMode(mode) {
    currentMode = mode;
    document.querySelectorAll('.mode-btn').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');
    loadNewPuzzle();
}

function initBoard() {
    board = Chessboard('board', {
        draggable: true,
        position: 'start',
        onDragStart: onDragStart,
        onDrop: onDrop,
        pieceTheme: 'https://chessboardjs.com/img/chesspieces/wikipedia/{piece}.png'
    });
}

function initStockfish() {
    stockfish = new Worker('js/stockfish.js');
    stockfish.onmessage = (e) => {
        if (typeof e.data === 'string' && e.data.includes(" pv ")) {
            const parts = e.data.split(" pv ");
            const moves = parts[1].split(" ");
            const foundMove = moves[0];

            if (puzzleActive) {
                bestMove = foundMove;
                console.log("Solution attendue (Toi):", bestMove);
            } 
            else {
                console.log("Réponse Ordi:", foundMove);
                makeComputerMove(foundMove);
            }
        }
    };
    stockfish.postMessage("uci");
}

async function loadNewPuzzle() {
    updateFeedback("Chargement...", "");
    puzzleActive = false;
    moveSequenceCount = 0;

    try {
        const res = await fetch(`/api/training/puzzle?type=${currentMode}`);
        if (!res.ok) throw new Error("Erreur API");
        
        const puzzleData = await res.json();
        
        game.load(puzzleData.fen);
        board.position(game.fen());
        
        let turn = game.turn();
        board.orientation(turn === 'w' ? 'white' : 'black');

        document.getElementById("game-source").innerText = puzzleData.gameId || "Inconnue";

        updateFeedback((turn === 'w' ? "Les Blancs" : "Les Noirs") + " jouent !", "");
        askStockfishForBestMove();
        puzzleActive = true;

    } catch (e) {
        console.error(e);
        updateFeedback("Erreur chargement.", "error");
    }
}

function askStockfishForBestMove() {
    stockfish.postMessage("stop");
    stockfish.postMessage(`position fen ${game.fen()}`);
    stockfish.postMessage("go depth 15"); 
}

// Logique de jeu

function onDragStart(source, piece) {
    if (!puzzleActive) return false;
    if ((game.turn() === 'w' && piece.search(/^b/) !== -1) ||
        (game.turn() === 'b' && piece.search(/^w/) !== -1)) {
        return false;
    }
}

function onDrop(source, target) {
    if (!puzzleActive) return 'snapback';

    const move = game.move({
        from: source,
        to: target,
        promotion: 'q'
    });

    if (move === null) return 'snapback';

    const playedMoveUCI = source + target;
    
    if (bestMove && playedMoveUCI === bestMove) {
        handleCorrectMove();
    } else {
        handleWrongMove();
        return 'snapback';
    }
}

function handleCorrectMove() {
    moveSequenceCount++;
    board.position(game.fen());
    
    if (game.in_checkmate()) {
        puzzleSuccess("✨ ECHEC ET MAT ! Bien joué !");
        return;
    }
    
    let maxMoves = (currentMode === 'endgame') ? 4 : 2;

    if (moveSequenceCount >= maxMoves) {
        puzzleSuccess("✅ Excellent calcul ! Séquence terminée.");
    } else {
        puzzleActive = false;
        updateFeedback("Bien joué ! L'adversaire répond...", "success");

        setTimeout(() => {
            askStockfishForBestMove(); 
        }, 500);
    }
}

function handleWrongMove() {
    updateFeedback("Mauvais coup. Réessaie !", "error");
    game.undo();
}

function makeComputerMove(computerBestMove) {
    const from = computerBestMove.substring(0, 2);
    const to = computerBestMove.substring(2, 4);
    
    game.move({ from: from, to: to, promotion: 'q' });
    board.position(game.fen());
    
    if (game.in_checkmate()) {
        updateFeedback("Tu as été maté... Oups.", "error");
        return;
    }

    updateFeedback("À toi ! Trouve la suite.", "");
    puzzleActive = true;
    askStockfishForBestMove();
}

function puzzleSuccess(msg) {
    puzzleActive = false;
    updateFeedback(msg, "success");
}

function updateFeedback(text, className) {
    const el = document.getElementById("feedback");
    el.innerText = text;
    el.className = "feedback " + className;
}