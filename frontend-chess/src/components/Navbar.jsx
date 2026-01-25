import React from "react";
import { Link } from "react-router-dom";

const Navbar = ({ username, onLogout }) => {
  return (
    <nav className="navbar">
      <div className="navbar-logo">
        <span style={{ fontSize: "1.5rem" }}>♟️</span> JRRZF Bot
      </div>
      <div className="navbar-links">
        {username ? (
          <>
            <span className="user-greeting">Bonjour, <strong>{username}</strong></span>
            <button className="btn-logout" onClick={onLogout}>Déconnexion</button>
          </>
        ) : (
          <Link to="/login">Connexion</Link>
        )}
      </div>
    </nav>
  );
};

export default Navbar;