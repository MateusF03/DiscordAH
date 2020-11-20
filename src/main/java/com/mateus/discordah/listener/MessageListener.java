package com.mateus.discordah.listener;

import com.mateus.discordah.cards.Card;
import com.mateus.discordah.cards.PlayedCards;
import com.mateus.discordah.game.Game;
import com.mateus.discordah.game.GameManager;
import com.mateus.discordah.game.Player;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class MessageListener extends ListenerAdapter {



    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!event.isFromType(ChannelType.PRIVATE)) return;
        User user = event.getAuthor();
        PrivateChannel channel = event.getPrivateChannel();
        GameManager gameManager = GameManager.getInstance();
        String contentRaw = event.getMessage().getContentRaw();

        Game game = gameManager.getUserGame(user);
        if (game != null) {
            Player player = game.getPlayer(user);
            if (game.isCzar(player)) {
                return;
            }
            Card blackCard = game.getBlackCard();
            if (contentRaw.toLowerCase().startsWith("dh!play")) {
                if (game.getVotes().containsKey(user)) {
                    PlayedCards playedCards = game.getVotes().get(user);
                    if (playedCards.getPlayedCards().size() == blackCard.getSlots()) {
                        channel.sendMessage("**You have already played**").queue();
                        return;
                    }
                }
                String[] args = contentRaw.split("\\s+");
                try  {

                    int votedCardIdx = Integer.parseInt(args[1]);
                    Card votedCard = player.getCards().get(votedCardIdx - 1);
                    PlayedCards votes;
                    if (!game.getVotes().containsKey(user)) {
                        votes = new PlayedCards();
                    } else {
                        votes = game.getVotes().get(user);
                        if (votes.getPlayedCards().contains(votedCard)) return;
                    }

                    votes.addCard(votedCard);
                    game.getVotes().put(user, votes);
                    if (votes.getPlayedCards().size() == blackCard.getSlots()) {
                        channel.sendMessage("**You played the card!**").queue();
                    } else if (votes.getPlayedCards().size() < blackCard.getSlots()) {
                        channel.sendMessage("**The card was played, remains " + (blackCard.getSlots() - votes.getPlayedCards().size()) + " card picks**").queue();
                    }
                    if (game.everyonePlayed()) {
                        game.czarVoting(event.getJDA());
                    }
                } catch (NumberFormatException e) {
                    channel.sendMessage("**You need to select the card number**").queue();
                }
            }
        }
    }
}
