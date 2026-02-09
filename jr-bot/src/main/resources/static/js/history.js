let board = null;
let game = new Chess();
let currentPlatformGame = null;
let analysisData = null;
let fullGameHistory = [];
let currentMoveIndex = -1;

// Configuration des icônes de classification
const CLASSIFICATION_ICONS = {
    "BRILLIANT": "‼️", "GREAT": "❗", "BEST": "⭐", "EXCELLENT": "👍",
    "GOOD": "✅", "INACCURACY": "⁉️", "MISTAKE": "❓", "BLUNDER": "❓❓", "BOOK": "📔"
};

document.addEventListener("DOMContentLoaded", () => {
    initBoard();
    loadHistoryList();

    // Navigation au clavier
    document.addEventListener('keydown', (e) => {
        if (e.key === 'ArrowRight') nextMove();
        if (e.key === 'ArrowLeft') prevMove();
    });
});

function initBoard() {
    board = Chessboard('board', { position: 'start', pieceTheme: 'https://chessboardjs.com/img/chesspieces/wikipedia/{piece}.png' });
}


// --- 1. CHARGEMENT DE LA LISTE GAUCHE ---
async function loadHistoryList() {
    const user = localStorage.getItem("username");
    if (!user) return;

    try {
        const response = await fetch(`/api/platform/history?username=${user}`);
        const games = await response.json();
        const tableBody = document.getElementById("historyBody");
        tableBody.innerHTML = "";

        if (games.length === 0) {
            tableBody.innerHTML = "<tr><td style='padding:10px; text-align:center'>Aucune partie trouvée.</td></tr>";
            return;
        }

        games.forEach(g => {
            const row = document.createElement("tr");
            row.style.cursor = "pointer";
            row.style.borderBottom = "1px solid #333";
            row.onmouseover = () => row.style.background = "#333";
            row.onmouseout = () => row.style.background = "transparent";
            
            // Calcul couleur résultat
            let color = "#aaa";
            let resText = g.result;
            if(g.result === '1-0') color = (g.whitePlayer === user) ? '#4caf50' : '#e57373';
            if(g.result === '0-1') color = (g.blackPlayer === user) ? '#4caf50' : '#e57373';
            if(g.result === '1/2-1/2') color = '#aaa';

            row.innerHTML = `
                <td style="padding:15px;">
                    <div style="font-weight:bold; color:#eee; font-size:1.1em; margin-bottom:5px;">
                        ${g.whitePlayer} vs ${g.blackPlayer}
                    </div>
                    <div style="display:flex; justify-content:space-between; font-size:0.9em;">
                        <span style="color:${color}; font-weight:bold;">${resText}</span>
                        <span style="color:#666;">${new Date(g.datePlayed).toLocaleDateString()}</span>
                    </div>
                </td>
            `;
            
            row.onclick = () => loadGameOnBoard(g);
            tableBody.appendChild(row);
        });

    } catch (e) { console.error(e); }
}

// --- 2. CHARGEMENT D'UNE PARTIE ---
function loadGameOnBoard(g) {
    currentPlatformGame = g;
    analysisData = null; // Reset analyse précédente
    document.getElementById("game-title").innerText = `${g.whitePlayer} vs ${g.blackPlayer}`;
    
    // Charger le PGN
    game.load_pgn(g.pgnText);
    
    // ✅ CORRECTION: Sauvegarder l'historique complet des coups
    fullGameHistory = game.history();
    
    // Reset complet
    firstMove();
    
    // UI Reset
    $('#btn-analyze').show().text("🔍 Lancer l'Analyse (Stockfish)").prop('disabled', false);
    $('#moves-analysis-panel').hide();
    $('#moves-list').empty();
    $('#white-accuracy').text('Blancs: -');
    $('#black-accuracy').text('Noirs: -');
    resetEvalBar();
}

// --- 3. ANALYSE (APPEL BACKEND) ---
async function analyzeCurrentGame() {
    if(!currentPlatformGame) return;
    
    const btn = $('#btn-analyze');
    btn.text("⏳ Analyse en cours... (Patientez)").prop('disabled', true);

    try {
        const res = await fetch(`/api/platform/analyze/${currentPlatformGame.id}`);
        const data = await res.json();

        if (data.error) {
            alert("Erreur: " + data.error);
            btn.text("Erreur").prop('disabled', false);
            return;
        }

        console.log("Analyse reçue :", data);
        analysisData = data.moves; 
        
        // Affichage Précision
        if(data.whiteAccuracy) $('#white-accuracy').text(`Blancs: ${data.whiteAccuracy.toFixed(1)}%`);
        if(data.blackAccuracy) $('#black-accuracy').text(`Noirs: ${data.blackAccuracy.toFixed(1)}%`);

        // Affichage Panneau Latéral
        $('#moves-analysis-panel').show();
        btn.hide(); // On cache le bouton, l'analyse est faite

        // Génération de la liste des coups colorés
        renderAnalysisList();
        
        // Mise à jour immédiate de la barre d'éval
        updateEvalFromAnalysis();

    } catch(e) {
        console.error(e);
        alert("Erreur lors de l'analyse.");
        btn.text("Réessayer").prop('disabled', false);
    }
}

// --- 4. AFFICHAGE LISTE COUPS (Logique Analysis.js) ---
function renderAnalysisList() {
    const list = $('#moves-list');
    list.empty();

    if (!analysisData) return;

    // On groupe par 2 (Blanc + Noir) pour l'affichage
    for (let i = 0; i < analysisData.length; i += 2) {
        const moveW = analysisData[i];
        const moveB = analysisData[i+1];
        const moveNum = Math.floor(i / 2) + 1;

        const div = $('<div>').addClass('move-row').attr('id', `move-row-${i}`);
        
        // Numéro
        div.append($('<span>').text(`${moveNum}.`).css('color', '#666').css('width', '20px'));

        // Coup Blanc
        if (moveW) {
            const spanW = $('<span>').text(moveW.playedMove).addClass(`classif-${moveW.classification}`);
            const iconW = CLASSIFICATION_ICONS[moveW.classification] || "";
            spanW.append($('<span>').text(iconW).addClass('classification-icon'));
            spanW.click((e) => { e.stopPropagation(); jumpToMove(i); });
            div.append(spanW);
        }

        // Coup Noir
        if (moveB) {
            const spanB = $('<span>').text(moveB.playedMove).addClass(`classif-${moveB.classification}`);
            const iconB = CLASSIFICATION_ICONS[moveB.classification] || "";
            spanB.append($('<span>').text(iconB).addClass('classification-icon'));
            spanB.click((e) => { e.stopPropagation(); jumpToMove(i+1); });
            div.append(spanB);
        }

        list.append(div);
    }
}

// --- 5. NAVIGATION ---
function updateBoardUI() {
    board.position(game.fen());
    
    // Surbrillance dans la liste
    $('.move-row').removeClass('active');
    if (currentMoveIndex >= 0) {
        const rowIndex = Math.floor(currentMoveIndex / 2) * 2; 
        $(`#move-row-${rowIndex}`).addClass('active');
    }

    // Mise à jour barre d'éval
    updateEvalFromAnalysis();
}

function updateEvalFromAnalysis() {
    if (!analysisData || currentMoveIndex < 0) {
        resetEvalBar();
        return;
    }

    const currentAnalysis = analysisData[currentMoveIndex];
    if (currentAnalysis && currentAnalysis.evalScore !== null) {
        let score = currentAnalysis.evalScore;
        let absScore = score;
        if (currentAnalysis.turn === 'BLACK' || currentAnalysis.turn === 'b') {
            absScore = -score;
        } else {
             absScore = score;
        }
        
        setEvalBar(absScore);
    }
}

// ✅ CORRECTION: nextMove() joue maintenant réellement le coup suivant
function nextMove() {
    if (currentMoveIndex < fullGameHistory.length - 1) {
        currentMoveIndex++;
        game.move(fullGameHistory[currentMoveIndex]); // ← AJOUT: Jouer le coup
        updateBoardUI();
    }
}

function prevMove() {
    if (currentMoveIndex >= 0) {
        game.undo();
        currentMoveIndex--;
        updateBoardUI();
    }
}

function firstMove() {
    game.reset();
    currentMoveIndex = -1;
    updateBoardUI();
}

function lastMove() {
    game.load_pgn(currentPlatformGame.pgnText);
    const history = game.history();
    currentMoveIndex = history.length - 1;
    updateBoardUI();
}

function jumpToMove(index) {
    game.reset();
    const history = game.history({verbose: true}); // Récupérer tout l'historique PGN
    // Charger le PGN complet dans une instance temporaire pour extraire les coups
    let tempGame = new Chess();
    tempGame.load_pgn(currentPlatformGame.pgnText);
    let allMoves = tempGame.history();

    // Rejouer jusqu'à index
    for (let i = 0; i <= index; i++) {
        game.move(allMoves[i]);
    }
    currentMoveIndex = index;
    updateBoardUI();
}

function flipBoard() {
    board.flip();
    // Inverser aussi la barre d'éval visuellement si tu veux
}

// --- BARRE D'EVALUATION ---
function setEvalBar(cp) {
    // Formule sigmoïde pour convertir centipions en % (pour la barre)
    // cp = +100 (avantage blanc) -> barre blanche monte
    const chance = 1 / (1 + Math.pow(10, -cp / 400));
    const percentWhite = chance * 100;
    
    $('#eval-white').css('height', `${percentWhite}%`);
    $('#eval-black').css('height', `${100 - percentWhite}%`);
}

function resetEvalBar() {
    $('#eval-white').css('height', '50%');
    $('#eval-black').css('height', '50%');
}