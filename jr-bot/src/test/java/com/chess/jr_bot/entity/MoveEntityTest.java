package com.chess.jr_bot.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour les méthodes utilitaires de l'entité MoveEntity.
 * <p>
 * Ces tests vérifient la logique de détection des propriétés spécifiques 
 * aux coups d'échecs, comme les promotions de pions, basées sur la notation UCI.
 * </p>
 */
public class MoveEntityTest {

    /**
     * Valide qu'un coup de 5 caractères est correctement identifié comme une promotion.
     */
    @Test
    void shouldDetectPromotion() {
        MoveEntity promoMove = new MoveEntity();
        promoMove.setPlayedMove("a7a8q"); 

        assertThat(promoMove.isPromotion()).isTrue();
    }

    /**
     * Valide qu'un coup standard de 4 caractères n'est pas considéré comme une promotion.
     */
    @Test
    void shouldNotDetectPromotionForStandardMove() {
        MoveEntity normalMove = new MoveEntity();
        normalMove.setPlayedMove("e2e4");

        assertThat(normalMove.isPromotion()).isFalse();
    }

    /**
     * Valide la robustesse de la méthode face à un coup non renseigné (null).
     */
    @Test
    void shouldHandleNullMove() {
        MoveEntity emptyMove = new MoveEntity();
        emptyMove.setPlayedMove(null);

        assertThat(emptyMove.isPromotion()).isFalse();
    }
}