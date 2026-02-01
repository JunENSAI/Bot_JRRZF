let board = null;
let game = new Chess();
let userColor = 'white';
let selectedTime = 300;
let whiteTime = 300;
let blackTime = 300;
let timerInterval = null;
let gameActive = false;
let botPhrases = {};
let isBotThinking = false; 

const username = localStorage.getItem("username") || "Invité";

// --- 1. SETUP ET LANCEMENT ---

document.addEventListener("DOMContentLoaded", async () => {
    document.getElementById("playerName").innerText = username;
    try {
        const res = await fetch('json/bot_phrases.json');
        botPhrases = await res.json();
    } catch (e) { console.error("Pas de phrases trouvées"); }
});

function selectColor(color) {
    userColor = color;
    document.getElementById('btnW').className = color === 'white' ? 'selected' : '';
    document.getElementById('btnB').className = color === 'black' ? 'selected' : '';
}

function startGame() {
    const timeVal = document.getElementById("timeControlSelect").value;
    if (timeVal === "no-limit") {
        selectedTime = null;
        document.getElementById("clock-white").innerText = "∞";
        document.getElementById("clock-black").innerText = "∞";
    } else {
        selectedTime = parseInt(timeVal);
        whiteTime = selectedTime;
        blackTime = selectedTime;
        updateClockDisplay();
    }

    document.querySelector(".game-container").style.filter = "none";
    document.getElementById("setupModal").style.display = "none";
    
    initBoard();
    gameActive = true;
    speak("intro");
    
    if (selectedTime) startTimer();
}

function initBoard() {
    game = new Chess();
    const config = {
        draggable: true,
        position: 'start',
        orientation: userColor,
        onDragStart: onDragStart,
        onDrop: onDrop,
        onSnapEnd: onSnapEnd,
        pieceTheme: 'https://chessboardjs.com/img/chesspieces/wikipedia/{piece}.png'
    };
    board = Chessboard('board', config);

    if (userColor === 'black') {
        setTimeout(makeBotMove, 500);
    }
}

// --- 2. LOGIQUE DU TIMER ---

function startTimer() {
    if (timerInterval) clearInterval(timerInterval);
    
    timerInterval = setInterval(() => {
        if (!gameActive) return;

        // Décrémenter le temps du joueur dont c'est le tour
        if (game.turn() === 'w') {
            whiteTime--;
            if (whiteTime <= 0) timeOut('white');
        } else {
            blackTime--;
            if (blackTime <= 0) timeOut('black');
        }
        updateClockDisplay();
    }, 1000);
}

function stopTimer() {
    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
    }
}

function updateClockDisplay() {
    if (!selectedTime) return;

    const wDiv = document.getElementById("clock-white");
    const bDiv = document.getElementById("clock-black");

    wDiv.innerText = formatTime(whiteTime);
    bDiv.innerText = formatTime(blackTime);

    // Indication visuelle de qui joue
    if (game.turn() === 'w') {
        wDiv.classList.add("active-clock");
        bDiv.classList.remove("active-clock");
    } else {
        bDiv.classList.add("active-clock");
        wDiv.classList.remove("active-clock");
    }
}

function formatTime(seconds) {
    if (seconds < 0) seconds = 0;
    const m = Math.floor(seconds / 60).toString().padStart(2, '0');
    const s = (seconds % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
}

function timeOut(color) {
    gameActive = false;
    stopTimer();
    const loser = color === 'white' ? "Blancs" : "Noirs";
    alert(`Temps écoulé ! Les ${loser} ont perdu.`);
    speak("timeout");
    
    let result = (color === 'white') ? "0-1" : "1-0"; 
    saveGame(result);
}

// --- 3. JEU & CHAT ---

function onDragStart(source, piece) {
    if (game.game_over() || !gameActive || isBotThinking) return false;

    if ((userColor === 'white' && piece.search(/^b/) !== -1) ||
        (userColor === 'black' && piece.search(/^w/) !== -1)) {
        return false;
    }
}

function onDrop(source, target) {
    const move = game.move({ from: source, to: target, promotion: 'q' });
    if (move === null) return 'snapback';

    if (move.captured) speak("capture");
    if (game.in_check()) speak("check");

    updateStatus();
    
    makeBotMove();
}

function onSnapEnd() { board.position(game.fen()); }

async function makeBotMove() {
    if (game.game_over() || !gameActive) return checkGameOver();

    isBotThinking = true; 

    try {
        const url = `/api/bot/move?fen=${encodeURIComponent(game.fen())}`;
        const res = await fetch(url);
        
        if (!res.ok) throw new Error("Erreur serveur Bot");

        const botMove = await res.text();
        
        game.move(botMove, { sloppy: true });
        board.position(game.fen());
        
        if (game.in_check()) speak("check");
        checkGameOver();
        
    } catch (e) { 
        console.error("Bot Error", e); 
    } finally {
        isBotThinking = false;
        updateStatus();
        
    }
}

function checkGameOver() {
    if (game.game_over()) {
        gameActive = false;
        stopTimer();
        isBotThinking = false;
        
        let result = "1/2-1/2";
        if (game.in_checkmate()) {
            const winner = game.turn() === 'w' ? "Noirs" : "Blancs";
            result = winner === "Blancs" ? "1-0" : "0-1";
            
            if ((userColor === 'white' && winner === 'Noirs') || (userColor === 'black' && winner === 'Blancs')) {
                speak("win");
            } else {
                speak("draw");
            }
        } else if (game.in_draw() || game.in_stalemate() || game.in_threefold_repetition()) {
             speak("draw");
        } else {
            speak("loss");
        }
        
        saveGame(result);
        return true;
    }
    return false;
}

// --- 4. SYSTEME DE CHAT ---
let bubbleTimeout;
function speak(category) {
    if (!botPhrases[category]) return;

    // Moins de spam pour les coups normaux
    if (category !== 'intro' && category !== 'win' && category !== 'loss' && category !== 'timeout') {
        if (Math.random() > 0.4) return; 
    }

    const phrases = botPhrases[category];
    const text = phrases[Math.floor(Math.random() * phrases.length)];
    
    const bubble = document.getElementById("bot-bubble");
    bubble.innerText = text;
    bubble.style.display = "block";

    clearTimeout(bubbleTimeout);
    bubbleTimeout = setTimeout(() => {
        bubble.style.display = "none";
    }, 4000);
}

function endGame(action) {
    if (!gameActive) return;
    let result = "1/2-1/2";
    if (action === 'resign') {
        result = userColor === 'white' ? "0-1" : "1-0";
        speak("win");
    } else if (action === 'draw') {
        speak("draw");
    }
    gameActive = false;
    stopTimer();
    saveGame(result);
}

async function saveGame(result) {
    let timeLabel = "Standard";
    if (selectedTime <= 120) timeLabel = "Bullet";
    else if (selectedTime <= 300) timeLabel = "Blitz";
    else if (selectedTime <= 900) timeLabel = "Rapide";

    try {
        await fetch('/api/platform/save', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                whitePlayer: userColor === 'white' ? username : "Bot-JRRZF",
                blackPlayer: userColor === 'black' ? username : "Bot-JRRZF",
                result: result,
                pgn: game.pgn(),
                timeControl: timeLabel 
            })
        });
        setTimeout(() => window.location.href = "dashboard.html", 2500);
    } catch (e) { console.error(e); }
}

function updateStatus() {
    let status = "";
    let moveColor = game.turn() === 'b' ? 'Noirs' : 'Blancs';

    if (game.in_check()) {
        status += moveColor + " sont en échec ! ";
    }
    if (game.game_over()) {
        status = "Partie terminée.";
    }
    document.getElementById("status").innerText = status;
}