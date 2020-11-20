package com.mateus.discordah.listener;

import com.mateus.discordah.cards.PlayedCards;
import com.mateus.discordah.game.Game;
import com.mateus.discordah.game.GameManager;
import com.mateus.discordah.game.Player;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Map;

public class CzarListener extends ListenerAdapter {

    private final Map<Integer, User> choose;
    private final User czar;

    public CzarListener(Map<Integer, User> choose, User czar) {

        this.choose = choose;
        this.czar = czar;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        User author = event.getAuthor();
        if (author.getIdLong() != czar.getIdLong()) return;
        String content = event.getMessage().getContentRaw();
        if (!content.toLowerCase().startsWith("dh!vote")) return;
        String[] args = content.split("\\s+");
        if (args.length == 1) {
            event.getChannel().sendMessage("You need to pick a card...").queue();
            return;
        }
        try {
            int chooseCards = Integer.parseInt(args[1]);
            if (!choose.containsKey(chooseCards)) {
                event.getChannel().sendMessage("This number is invalid").queue();
                return;
            }
            User winner = choose.get(chooseCards);

            Game game = GameManager.getInstance().getUserGame(winner);
            if (game == null) {
                event.getJDA().removeEventListener(this);
                return;
            }
            event.getChannel().sendMessage("**" + winner.getAsMention() + " won the round!**").queue();
            Map<User, PlayedCards> votes = game.getVotes();

            for (Player player : game.getPlayers()) {
                if (votes.containsKey(player.getUser())) {
                    PlayedCards playedCards = votes.get(player.getUser());
                    player.dumpCards(playedCards.getPlayedCards());
                    votes.remove(player.getUser());
                }
            }
            int points = 0;
            if (game.getPoints().containsKey(winner)) {
                points = game.getPoints().get(winner);
            }
            game.getPoints().put(winner, points + 1);
            game.getChoose().clear();
            game.nextRound();
            event.getJDA().removeEventListener(this);
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("This isn't a number").queue();
        }
    }
}
