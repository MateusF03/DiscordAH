package com.mateus.discordah.game;

import com.mateus.discordah.cards.Card;
import com.mateus.discordah.cards.CardComposite;
import com.mateus.discordah.cards.PlayedCards;
import com.mateus.discordah.listener.CzarListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.*;
import java.util.List;

public class Game {

    private final List<Player> players;
    private final TextChannel channel;
    private final long creatorId;
    private final Set<Card> playedBlackCards = new HashSet<>();
    private final Map<User, PlayedCards> votes = new HashMap<>();
    private final Map<Integer, User> choose = new HashMap<>();
    private final Map<User, Integer> points = new HashMap<>();
    private Player czar;
    private int czarIdx = -1;
    private Card blackCard;

    public Game(List<User> players, TextChannel channel, long creatorId) {
        this.channel = channel;
        this.creatorId = creatorId;
        List<Card> decks = CardComposite.makeDecks(players.size());
        int idx = 0;
        List<Player> playerList = new ArrayList<>();
        for (User player : players) {
            List<Card> playerCards = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                playerCards.add(decks.get(idx));
                idx++;
            }
            playerList.add(new Player(player, playerCards));
        }
        this.players = playerList;
        //System.out.println("legal 1");
    }

    public void newRound() {
        //System.out.println("legal 2");
        Collections.shuffle(players);
        nextRound();
    }

    public void nextRound() {
        for (Map.Entry<User, Integer> userIntegerEntry : points.entrySet()) {
            int points = userIntegerEntry.getValue();
            if (points >= 3) {
                channel.sendMessage("**The winner is: " + userIntegerEntry.getKey().getAsMention() +"!**").queue();
                stop();
                return;
            }
        }
        czarIdx++;
        if (czarIdx >= players.size()) {
            czarIdx = 0;
        }
        czar = players.get(czarIdx);
        blackCard = CardComposite.getRandomBlackCard();

        while (playedBlackCards.contains(blackCard)) {
            blackCard = CardComposite.getRandomBlackCard();
        }
        playedBlackCards.add(blackCard);

        for (Player player : players) {
            User user = player.getUser();
            Card bc = blackCard;
            if (player.getId() == czar.getId()) {
                user.openPrivateChannel().queue(p -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setAuthor("You are the Czar!");
                    embedBuilder.setDescription("**The black card for this round is:**\n" + bc.getContent().replace("_", "\\_"));
                    embedBuilder.setFooter("This card has " + bc.getSlots() + " pick(s).");
                    embedBuilder.setColor(0x1CBF1E);
                    p.sendMessage(embedBuilder.build()).queue();
                });
            } else {
                user.openPrivateChannel().queue(p -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setAuthor("Discord Against humanity");
                    embedBuilder.addField("The black card:", bc.getContent().replace("_", "\\_"), false);

                    int c = 0;

                    for (Card card : player.getCards()) {
                        c++;
                        embedBuilder.addField("Card "+ c +":", card.getContent(), true);
                    }
                    embedBuilder.setColor(0x1CBF1E);
                    embedBuilder.setFooter("This card has " + bc.getSlots() + " pick(s). Use `dh!play <number of the card>` to play it!");
                    p.sendMessage(embedBuilder.build()).queue();
                });
            }
        }
    }

    public void czarVoting(JDA jda) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("Discord against humanity");
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        embedBuilder.addField("The black card:", blackCard.getContent().replace("_", "\\_"), false);
        List<User> keys = new ArrayList<>(votes.keySet());
        Collections.shuffle(keys);
        for (User key : keys) {
            i++;
            for (Card playedCard : votes.get(key).getPlayedCards()) {
                stringBuilder.append(playedCard.getContent());
                stringBuilder.append(" | ");
            }
            embedBuilder.addField("Card(s) " + i + ":", stringBuilder.substring(0, stringBuilder.length() - 2), true);
            choose.put(i, key);
            stringBuilder.setLength(0);
        }
        embedBuilder.setColor(0x1CBF1E);
        embedBuilder.setFooter("Use `dh!vote <the number of your chosen card>`");
        channel.sendMessage(embedBuilder.build()).queue();

        channel.sendMessage(czar.getUser().getAsMention() + " choose your favorite card(s)").queue();

        jda.addEventListener(new CzarListener(choose, czar.getUser()));
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getPlayer(User user) {
        return players.stream().filter(p -> p.getId() == user.getIdLong()).findFirst().orElse(null);
    }

    public TextChannel getChannel() {
        return channel;
    }

    public Map<User, PlayedCards> getVotes() {
        return votes;
    }

    public Card getBlackCard() {
        return blackCard;
    }
    
    public boolean everyonePlayed() {
        if (blackCard == null) return false;
        int i = 0;
        for (Map.Entry<User, PlayedCards> userEntry : votes.entrySet()) {
            PlayedCards cards = userEntry.getValue();
            if (cards.getPlayedCards().size() == blackCard.getSlots()) {
                i++;
            }
        }
        return i == players.size() - 1;
    }

    public boolean isCzar(Player player) {
        if (czar == null) return false;
        return player.getId() == czar.getId();
    }

    public Map<Integer, User> getChoose() {
        return choose;
    }

    public Map<User, Integer> getPoints() {
        return points;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void stop() {
        players.clear();
        playedBlackCards.clear();
        GameManager.getInstance().removeGame(this);
    }

}
