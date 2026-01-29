let board = null;
let game = new Chess();
let stockfish = null;
let currentMode = 'winning';
let bestMoveFoundByStockfish = null;
let puzzleActive = false;

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
        if (e.data.includes(" pv ")) {
            const parts = e.data.split(" pv ");
            const moves = parts[1].split(" ");
            bestMoveFoundByStockfish = moves[0];
            console.log("Solution Stockfish:", bestMoveFoundByStockfish);
        }
    };
    stockfish.postMessage("uci");
}

async function loadNewPuzzle() {
    const feedback = document.getElementById("feedback");
    feedback.innerText = "Chargement...";
    feedback.className = "feedback";
    puzzleActive = false;

    try {
        const res = await fetch(`/api/training/puzzle?type=${currentMode}`);
        if (!res.ok) throw new Error("Plus de puzzles !");
        
        const puzzleData = await res.json();

        game.load(puzzleData.fen);
        board.position(game.fen());
        
        let turn = game.turn();
        let orientation = (turn === 'w') ? 'white' : 'black';
        board.orientation(orientation);

        document.getElementById("game-source").innerText = puzzleData.gameId || "Inconnue";
        feedback.innerText = (turn === 'w' ? "Les Blancs" : "Les Noirs") + " jouent et gagnent !";

        bestMoveFoundByStockfish = null;
        stockfish.postMessage("stop");
        stockfish.postMessage(`position fen ${game.fen()}`);
        stockfish.postMessage("go depth 15"); 

        puzzleActive = true;

    } catch (e) {
        console.error(e);
        feedback.innerText = "Erreur chargement puzzle.";
    }
}

// --- LOGIQUE DE JEU ---

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

    checkSolution(playedMoveUCI);
}

function checkSolution(playedMove) {
    const feedback = document.getElementById("feedback");
    
    
    if (bestMoveFoundByStockfish && playedMove === bestMoveFoundByStockfish) {
        feedback.innerText = "EXCELLENT ! C'est le meilleur coup.";
        feedback.className = "feedback success";
        puzzleActive = false;

        setTimeout(() => {
             loadNewPuzzle(); 
        }, 1500);
        
    } else {
        feedback.innerText = "Ce n'est pas le meilleur coup. Réessaie !";
        feedback.className = "feedback error";
        
        setTimeout(() => {
            game.undo();
            board.position(game.fen());
        }, 500);
    }
}