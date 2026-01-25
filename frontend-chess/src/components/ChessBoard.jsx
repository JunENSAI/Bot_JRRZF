import React from "react";
import Chessboard from "chessboardjsx";
import "./Components.css";

const ChessBoard = ({ fen, onDrop, orientation = "white", width = 500 }) => {
  return (
    <div className="board-container">
      <Chessboard
        width={width}
        position={fen}
        onDrop={onDrop}
        orientation={orientation}
        id="basicBoard"

        darkSquareStyle={{ backgroundColor: "#779556" }}
        lightSquareStyle={{ backgroundColor: "#ebecd0" }}
        calcWidth={({ screenWidth }) => (screenWidth < 500 ? screenWidth - 40 : 500)}
        pieces={{
           wK: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/4/42/Chess_klt45.svg" alt="" />,
           wQ: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/1/15/Chess_qlt45.svg" alt="" />,
           wR: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/7/72/Chess_rlt45.svg" alt="" />,
           wB: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/b/b1/Chess_blt45.svg" alt="" />,
           wN: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/7/70/Chess_nlt45.svg" alt="" />,
           wP: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/4/45/Chess_plt45.svg" alt="" />,
           bK: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/f/f0/Chess_kdt45.svg" alt="" />,
           bQ: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/4/47/Chess_qdt45.svg" alt="" />,
           bR: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/f/ff/Chess_rdt45.svg" alt="" />,
           bB: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/9/98/Chess_bdt45.svg" alt="" />,
           bN: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/e/ef/Chess_ndt45.svg" alt="" />,
           bP: ({ squareWidth }) => <img style={{ width: squareWidth, height: squareWidth }} src="https://upload.wikimedia.org/wikipedia/commons/c/c7/Chess_pdt45.svg" alt="" />,
        }}
      />
    </div>
  );
};

export default ChessBoard;