let board = null;
let game = new Chess();
let dbMovesData = [];
let currentMoveIndex = -1; 
let stockfish = null;
let currentPage = 0;
let currentSearch = "";
let activeGameId = null;

document.addEventListener("DOMContentLoaded", () => {
    initBoard();
    initStockfish();
    loadGamesList();

    const urlParams = new URLSearchParams(window.location.search);
    const urlId = urlParams.get('id');
    if(urlId) {
        activeGameId = urlId;
    }

    document.getElementById("opponent-search").addEventListener("keypress", function(event) {
        if (event.key === "Enter") searchOpponent();
    });
});


// Cherche adversaire
function searchOpponent() {
    const input = document.getElementById("opponent-search").value;
    currentSearch = input.trim();
    currentPage = 0; 
    loadGamesList();
}

// charger la liste des parties
async function loadGamesList() {
    const container = document.getElementById("games-container");
    container.innerHTML = "<div style='text-align:center; color:#888;'>Chargement...</div>";
    
    try {
        const url = `/api/historical/games?page=${currentPage}&search=${encodeURIComponent(currentSearch)}`;
        const res = await fetch(url);
        const pageData = await res.json();
        
        container.innerHTML = "";
        
        if(!pageData.content || pageData.content.length === 0) {
            container.innerHTML = "<div style='padding:10px; text-align:center'>Aucune partie trouvée.</div>";
            return;
        }

        pageData.content.forEach(g => {
            const div = document.createElement("div");
            div.className = "game-item";
            
            const wName = g.whitePlayer || "?";
            const bName = g.blackPlayer || "?";
            const result = g.result || "*";
            
            let colorClass = '#e57373'; 
            if ((wName.toLowerCase() === 'jrrzf' && result === '1-0') || 
                (bName.toLowerCase() === 'jrrzf' && result === '0-1')) colorClass = '#4caf50';
            if (result === '1/2-1/2') colorClass = '#999';

            div.innerHTML = `
                <div style="display:flex; align-items:center; gap:5px;">
                    <span style="color:${colorClass}; font-size:1.2em;">●</span> 
                    <div>
                        <strong>${wName}</strong> <span style="color:#666">vs</span> <strong>${bName}</strong>
                    </div>
                </div>
                <div style="font-size:0.8em; color:#888; margin-left:15px;">${g.datePlayed || ""}</div>
            `;
            
            div.onclick = () => loadGame(g);
            container.appendChild(div);
        });

        document.getElementById("page-indicator").innerText = currentPage + 1;

    } catch (e) { 
        console.error(e); 
        container.innerHTML = "<div style='color:red; text-align:center'>Erreur connexion</div>";
    }
}

function nextPage() { currentPage++; loadGamesList(); }
function prevPage() { if (currentPage > 0) currentPage--; loadGamesList(); }


// initialise stockfish
function initStockfish() {
    stockfish = new Worker('js/stockfish.js');
    stockfish.onmessage = (e) => {
        const line = e.data;
        if (typeof line === 'string') {
            if (line.includes("score cp") || line.includes("score mate")) {
                parseStockfishScore(line);
            }
            if (line.includes(" pv ")) {
                const bestMove = line.split(" pv ")[1].split(" ")[0];
                document.getElementById("best-move-display").innerHTML = 
                    `Meilleur coup : <strong style="color:#4caf50">${bestMove}</strong>`;

                $('.square-55d63').removeClass('highlight-best');
                const from = bestMove.substring(0, 2);
                const to = bestMove.substring(2, 4);
                $('#board').find('.square-' + from).addClass('highlight-best');
                $('#board').find('.square-' + to).addClass('highlight-best');
            }
        }
    };
    stockfish.postMessage("uci");
}

// colle le score de la part de Stockfish
function parseStockfishScore(line) {
    if (line.includes("score mate")) {
        const parts = line.split("score mate ")[1].split(" ")[0];
        document.getElementById("eval-score").innerText = `M${parts}`;
    } else if (line.includes("score cp")) {
        const parts = line.split("score cp ")[1].split(" ")[0];
        let cp = parseInt(parts);
        let displayScore = cp / 100.0;
        
        if (game.turn() === 'b') displayScore = -displayScore;
        const sign = displayScore > 0 ? "+" : "";
        document.getElementById("eval-score").innerText = sign + displayScore.toFixed(2);
        
        // Barre
        let absCp = (game.turn() === 'w') ? cp : -cp;
        if (absCp > 800) absCp = 800;   
        if (absCp < -800) absCp = -800; 
        let percent = 50 + (absCp / 16); 
        if (percent > 100) percent = 100; if (percent < 0) percent = 0;
        document.getElementById("eval-white").style.height = `${percent}%`;
        document.getElementById("eval-black").style.height = `${100 - percent}%`;
    }
}

async function loadGame(g) {
    document.getElementById("game-title").innerText = `${g.whitePlayer} vs ${g.blackPlayer}`;

    activeGameId = g.gameId; 

    $('#review-summary').hide(); 
    $('#btn-analyze').show().text("🔍 Lancer le Bilan").prop('disabled', false);

    try {
        const res = await fetch(`/api/historical/game-moves?gameId=${g.gameId}`);
        dbMovesData = await res.json();
        if (!dbMovesData || dbMovesData.length === 0) { alert("Pas de coups dispos"); return; }
        
        game.reset();
        currentMoveIndex = -1;
        updateUI(); 
    } catch (e) { console.error(e); }
}

function initBoard() {
    board = Chessboard('board', { position: 'start', pieceTheme: 'https://chessboardjs.com/img/chesspieces/wikipedia/{piece}.png' });
}

function nextMove() { if (currentMoveIndex < dbMovesData.length - 1) { currentMoveIndex++; updateUI(); } }
function prevMove() { if (currentMoveIndex >= 0) { currentMoveIndex--; updateUI(); } else { currentMoveIndex = -1; updateUI(); } }
function lastMove() { currentMoveIndex = dbMovesData.length - 1; updateUI(); }
function firstMove() { currentMoveIndex = -1; updateUI(); }
function flipBoard() { board.flip(); updateUI(); }

function createMoveHtml(move, index) {
    if (!move) return '';

    let iconHtml = '';
    if (move.classification) {
        let iconName = move.classification.toLowerCase() + ".png";
        iconHtml = `<img src="/images/${iconName}" class="move-classification-icon" style="height:16px; margin-left:4px;" title="${move.classification}">`;
    }

    const activeClass = (index === currentMoveIndex) ? 'active-move' : '';

    return `<span class="move-item ${activeClass}" onclick="jumpToMove(${index})" style="cursor:pointer; padding:2px 5px;">
                ${move.playedMove} ${iconHtml}
            </span>`;
}

/**
 * Fonction principale qui dessine toute la liste (1. e4 e5 ...)
 */
function updateMoveList() {
    const listContainer = document.getElementById('moves-list');
    if (!listContainer) return;
    
    if (!dbMovesData || dbMovesData.length === 0) {
        listContainer.innerHTML = '';
        return;
    }

    let html = '';
    for (let i = 0; i < dbMovesData.length; i += 2) {
        const moveWhite = dbMovesData[i];
        const moveBlack = dbMovesData[i+1];
        const moveNum = Math.floor(i / 2) + 1;

        html += `
            <div class="move-row" style="display:flex; border-bottom:1px solid #444; padding:3px;">
                <span class="move-num" style="width:30px; color:#888;">${moveNum}.</span>
                <div style="flex:1;">${createMoveHtml(moveWhite, i)}</div>
                <div style="flex:1;">${createMoveHtml(moveBlack, i+1)}</div>
            </div>
        `;
    }
    listContainer.innerHTML = html;
}

async function triggerGameReview() {
    const urlParams = new URLSearchParams(window.location.search);
    const gameId = activeGameId || urlParams.get('id');

    if (!gameId) {
        alert("Veuillez sélectionner une partie d'abord !");
        return;
    }

    const btn = $('#btn-analyze');
    btn.text("Analyse en cours...").prop('disabled', true);

    try {
        const response = await fetch(`/api/analysis/review/${gameId}`);
        if (!response.ok) throw new Error("Erreur lors de l'analyse backend");

        const data = await response.json(); 
        console.log("Réponse du serveur :", data); 


        if (data.moves) {
            dbMovesData = data.moves;
        } else if (Array.isArray(data)) {
            console.warn("Le backend AnalysisController n'est pas à jour !");
            dbMovesData = data; 
        }
        let rawAccW = data.whiteAccuracy;
        let rawAccB = data.blackAccuracy;

        if (rawAccW === undefined || rawAccW === null) rawAccW = 0;
        if (rawAccB === undefined || rawAccB === null) rawAccB = 0;

        const accW = Number(rawAccW).toFixed(1); // "94.1"
        const accB = Number(rawAccB).toFixed(1); // "88.0"

        $('#white-accuracy').text(accW + '%').css('color', getColorForAccuracy(rawAccW));
        $('#black-accuracy').text(rawAccB > 0 ? accB + '%' : '-').css('color', getColorForAccuracy(rawAccB));

        updateMoveList(); 

        btn.hide();
        $('#review-summary').fadeIn();

    } catch (e) {
        console.error(e);
        alert("Erreur: " + e.message);
        btn.text("Réessayer").prop('disabled', false);
    }
}

function getColorForAccuracy(score) {
    if (score >= 90) return '#2ecc71';
    if (score >= 80) return '#27ae60';
    if (score >= 70) return '#f1c40f';
    return '#e74c3c';
}

function updateUI() {
    if (currentMoveIndex === -1) {
        game.reset();
        board.position(game.fen());
        document.getElementById("eval-score").innerText = "0.0";
        document.getElementById("best-move-display").innerText = "Stockfish: Prêt";
        $('.square-55d63').removeClass('highlight-move');
        $('.square-55d63').removeClass('highlight-best');
        updateMoveList();
        return;
    }

    const moveData = dbMovesData[currentMoveIndex];
    if (moveData && moveData.fen) {
        game.load(moveData.fen); 
        board.position(moveData.fen);

        $('.square-55d63').removeClass('highlight-move');
        $('.square-55d63').removeClass('highlight-best'); 
        
        if (moveData.playedMove && moveData.playedMove.length >= 4) {
            const from = moveData.playedMove.substring(0, 2);
            const to = moveData.playedMove.substring(2, 4);
            $('#board').find('.square-' + from).addClass('highlight-move');
            $('#board').find('.square-' + to).addClass('highlight-move');
        }
        
        if (stockfish) {
            document.getElementById("best-move-display").innerText = "Calcul...";
            stockfish.postMessage("stop");
            stockfish.postMessage(`position fen ${moveData.fen}`);
            stockfish.postMessage("go depth 25");
        }
    }

    updateMoveList();
}

function jumpToMove(index) {
    currentMoveIndex = index;
    updateUI();
}