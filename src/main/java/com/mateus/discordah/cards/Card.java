package com.mateus.discordah.cards;

import java.security.InvalidParameterException;

public class Card {

    private final CardType cardType;
    private final String content;
    private final int slots;

    public Card(String content) {
        this(CardType.WHITE, content, 0);
    }

    public Card(CardType cardType, String content, int slots) {
        if (cardType == CardType.WHITE && slots > 0) {
            throw new InvalidParameterException("White cards can't have slots");
        }
        this.cardType = cardType;
        this.content = content;
        this.slots = slots;
    }

    public CardType getCardType() {
        return cardType;
    }

    public String getContent() {
        return content;
    }

    public int getSlots() {
        return slots;
    }

    public enum CardType {
        BLACK, WHITE
    }
}
