package com.chess.jr_bot.entity;

public enum MoveClassification {
    BRILLIANT("Brillant", "brilliant.png", "‼️"),
    GREAT("Super", "great.png", "❗"),
    BEST("Meilleur coup", "best.png", "⭐"),
    EXCELLENT("Excellent", "excellent.png", "👍"),
    GOOD("Bon", "good.png", "✅"),
    BOOK("Théorie", "book.png", "📔"),
    INACCURACY("Imprécision", "inaccuracy.png", "⁉️"),
    MISTAKE("Erreur", "mistake.png", "❓"),
    BLUNDER("Gaffe", "blunder.png", "❓❓");

    private final String label;
    private final String imageName;
    private final String symbol;

    MoveClassification(String label, String imageName, String symbol) {
        this.label = label;
        this.imageName = imageName;
        this.symbol = symbol;
    }

    public String getLabel() { return label; }
    public String getImageName() { return imageName; }
    public String getSymbol() { return symbol; }
}