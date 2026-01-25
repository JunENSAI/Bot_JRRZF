import React from "react";

const Menu = ({ title, description, icon, onClick, className }) => {
  return (
    <div className={`menu-card ${className}`} onClick={onClick}>
      <div className="menu-icon">{icon}</div>
      <h2>{title}</h2>
      <p>{description}</p>
    </div>
  );
};

export default Menu;