package com.mateus.discordah.cards;

import java.util.*;
import java.util.stream.Collectors;

public class CardComposite {

    private static final List<Card> cardList = new ArrayList<>();

    public static void addCard(Card card) {
        cardList.add(card);
    }

    public static Card getRandomWhiteCard() {
        Random random = new Random();
        List<Card> whiteCards = cardList.stream().filter(c -> c.getCardType() == Card.CardType.WHITE).collect(Collectors.toList());
        return whiteCards.get(random.nextInt(whiteCards.size()));
    }

    public static List<Card> makeDecks(int playerCount) {
        int size = 7 * playerCount;
        Set<Card> cardSet = new HashSet<>();
        while (cardSet.size() < size) {
            Card card = getRandomWhiteCard();
            if (cardSet.contains(card)) continue;
            cardSet.add(card);
        }
        return new ArrayList<>(cardSet);
    }

    public static List<Card> recicleCards(List<Card> dumpedCards) {
        int size = dumpedCards.size();
        Set<Card> newCards = new HashSet<>();

        while (newCards.size() < size) {
            Card card = getRandomWhiteCard();
            if (newCards.contains(card) || dumpedCards.contains(card)) continue;
            newCards.add(card);
        }

        return new ArrayList<>(newCards);
    }

    public static Card getRandomBlackCard() {
        Random random = new Random();
        List<Card> blackCards = cardList.stream().filter(c -> c.getCardType() == Card.CardType.BLACK).collect(Collectors.toList());
        return blackCards.get(random.nextInt(blackCards.size()));
    }
}
