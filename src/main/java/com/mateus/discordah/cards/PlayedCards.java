package com.mateus.discordah.cards;

import java.util.ArrayList;
import java.util.List;

public class PlayedCards {

    private final List<Card> playedCards = new ArrayList<>();

    public void addCard(Card card) {
        playedCards.add(card);
    }

    public List<Card> getPlayedCards() {
        return playedCards;
    }
}
