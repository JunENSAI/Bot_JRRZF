import React from "react";

const GameControl = ({ onResign, onDraw, gameStatus }) => {
  return (
    <div className="game-controls">
      {/* Affichage du statut (ex: "Trait aux Blancs") */}
      <div className="status-display">
        {gameStatus}
      </div>

      <div className="buttons-row">
        <button className="btn btn-resign" onClick={onResign}>
            Abandonner
        </button>
        <button className="btn btn-draw" onClick={onDraw}>
            Match Nul
        </button>
      </div>
    </div>
  );
};

export default GameControl;