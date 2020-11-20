package com.mateus.discordah.game;

import com.mateus.discordah.cards.Card;
import com.mateus.discordah.cards.CardComposite;
import net.dv8tion.jda.api.entities.User;

import java.security.InvalidParameterException;
import java.util.List;

public class Player {

    private final User user;
    private final long id;
    private final List<Card> cards;

    public Player(User user, List<Card> cards) {
        this.user = user;
        this.id = user.getIdLong();
        this.cards = cards;
    }

    public long getId() {
        return id;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void dumpCards(List<Card> dumpedCards) {
        cards.removeAll(dumpedCards);
        List<Card> newCards = CardComposite.recicleCards(dumpedCards);
        if ((cards.size() + newCards.size()) > 7) {
            throw new InvalidParameterException("New cards size are too big");
        }
        cards.addAll(newCards);
    }

    public User getUser() {
        return user;
    }
}
