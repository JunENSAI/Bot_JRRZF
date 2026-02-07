package com.chess.jr_bot.entity;

/**
 * Définit les catégories de qualité pour chaque coup joué.
 * <p>
 * Chaque constante associe une étiquette textuelle, un symbole visuel et un 
 * score d'exactitude (accuracyScore) utilisé pour le calcul de la précision 
 * globale d'une partie.
 * </p>
 */
public enum MoveClassification {
    
    /** Coup exceptionnel, souvent difficile à trouver pour le moteur à faible profondeur. */
    BRILLIANT("Brillant", "brilliant.png", "‼️", 1.0),
    
    /** Coup très fort qui améliore significativement la position. */
    GREAT("Super", "great.png", "❗", 1.0),
    
    /** Le coup recommandé par le moteur Stockfish. */
    BEST("Meilleur coup", "best.png", "⭐", 1.0),
    
    /** Un coup très solide, proche du meilleur coup. */
    EXCELLENT("Excellent", "excellent.png", "👍", 0.90),
    
    /** Un coup correct qui maintient l'équilibre de la position. */
    GOOD("Bon", "good.png", "✅", 0.65),
    
    /** Coup reconnu dans la théorie des ouvertures. */
    BOOK("Théorie", "book.png", "📔", 1.0),
    
    /** Une imprécision qui dégrade légèrement l'avantage. */
    INACCURACY("Imprécision", "inaccuracy.png", "⁉️", 0.40),
    
    /** Une erreur notable qui change l'évaluation de la partie. */
    MISTAKE("Erreur", "mistake.png", "❓", 0.20),
    
    /** Une erreur fatale perdant un avantage décisif ou du matériel. */
    BLUNDER("Gaffe", "blund0r.png", "❓❓", 0.0),
    
    /** Le seul coup légal ou raisonnablement jouable dans la position. */
    FORCED("Force", "forced.png", "➡️", 1.0);

    private final String label;
    private final String imageName;
    private final String symbol;
    private final double accuracyScore;

    /**
     * Constructeur interne pour initialiser les propriétés de classification.
     * * @param label Nom affiché dans l'interface.
     * @param imageName Nom du fichier image/icône associé.
     * @param symbol Caractère emoji représentant le coup sur l'échiquier.
     * @param accuracyScore Poids (de 0.0 à 1.0) utilisé pour la moyenne de précision.
     */
    MoveClassification(String label, String imageName, String symbol, double accuracyScore) {
        this.label = label;
        this.imageName = imageName;
        this.symbol = symbol;
        this.accuracyScore = accuracyScore;
    }

    public String getLabel() { return label; }
    public String getImageName() { return imageName; }
    public String getSymbol() { return symbol; }
    public double getAccuracyScore() { return accuracyScore; }
}