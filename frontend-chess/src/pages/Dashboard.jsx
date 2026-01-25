import React from "react";
import { useNavigate } from "react-router-dom";
import Menu from "../components/Menu";
import "./Pages.css";

const Dashboard = () => {
  const navigate = useNavigate();

  return (
    <div className="dashboard-container">
      <h1>Tableau de Bord</h1>
      <div className="dashboard-grid">
        
        <Menu 
          title="Jouer" 
          description="Affronter le Bot (Blanc ou Noir)" 
          icon="⚔️"
          className="card-play"
          onClick={() => navigate("/play")}
        />

        <Menu 
          title="Mon Profil" 
          description="Statistiques et Progression" 
          icon="📊"
          className="card-profile"
          onClick={() => navigate("/profile")}
        />

        <Menu 
          title="Ouvertures" 
          description="Mon répertoire (À venir)" 
          icon="📖"
          className="card-openings"
          onClick={() => alert("Module en cours de construction...")}
        />

        <Menu 
          title="Historique" 
          description="Revoir mes parties" 
          icon="📜"
          className="card-history"
          onClick={() => alert("Module en cours de construction...")}
        />

      </div>
    </div>
  );
};

export default Dashboard;

