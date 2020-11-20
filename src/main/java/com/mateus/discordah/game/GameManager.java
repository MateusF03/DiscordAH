package com.mateus.discordah.game;

import com.mateus.discordah.runnable.VotingRunnable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameManager {

    private final List<Game> games = new ArrayList<>();
    
    private static GameManager instance;
    private GameManager() { }
    public static GameManager getInstance() {
        if (instance == null) {
            synchronized (GameManager.class) {
                if (instance == null) {
                    instance = new GameManager();
                }
            }
        }
        return instance;
    }
    
    public void startVoting(List<User> users, TextChannel channel, User author, JDA jda) {
        if (users.size() > 8) {
            channel.sendMessage("**You can't play with more than 8 users**").queue();
            return;
        }
        Stream<User> userStream = users.stream();
        if (userStream.anyMatch(u -> u.isBot() || u.getIdLong() == jda.getSelfUser().getIdLong())) {
            channel.sendMessage("**You can't play with bots**").queue();
            return;
        }
        if (userStream.anyMatch(u -> GameManager.getInstance().userIsInGame(u))) {
            channel.sendMessage("**A user you invited is already in a game**").queue();
            return;
        }
        if (userStream.anyMatch(u -> !u.hasPrivateChannel())) {
            channel.sendMessage("**One of the invited users doesn't have a open private channel**").queue();
            return;
        }
        if (channelHasGame(channel)) {
            channel.sendMessage("**This channel has already a game**").queue();
            return;
        }
        List<User> filteredUsers = users.stream().distinct().collect(Collectors.toList());
        filteredUsers.remove(author);
        if (filteredUsers.size() < 2) {
            channel.sendMessage("**You need the minimum of three users to play**").queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("Discord against humanity");
        embedBuilder.setDescription("Do you want to participate?");
        embedBuilder.setColor(0x1CBF1E);
        filteredUsers.add(author);
        channel.sendMessage(embedBuilder.build()).queue(m -> {
            m.addReaction("✅").queue();
            m.addReaction("\uD83C\uDDFD").queue();
            processVotes(m, jda, filteredUsers.size(), filteredUsers, author.getIdLong());
        });
    }

    public void createGame(List<User> players, TextChannel channel, long creatorId) {
        Game game = new Game(players, channel, creatorId);
        game.newRound();
        games.add(game);
    }

    public boolean channelHasGame(TextChannel textChannel) {
        for (Game game : games) {
            if (game.getChannel().getIdLong() == textChannel.getIdLong()) {
                return true;
            }
        }
        return false;
    }

    public Game getChannelGame(TextChannel textChannel) {
        for (Game game : games) {
            if (game.getChannel().getIdLong() == textChannel.getIdLong()) {
                return game;
            }
        }
        return null;
    }
    
    public boolean userIsInGame(User user) {
        for (Game game : games) {
            if (game.getPlayers().stream().anyMatch(p -> p.getId() == user.getIdLong())) {
                return true;
            }
        }
        return false;
    }
    
    public Game getUserGame(User user) {
        for (Game game : games) {
            if (game.getPlayers().stream().anyMatch(p -> p.getId() == user.getIdLong())) {
                return game;
            }
        }
        return null;
    }

    private void processVotes(Message message, JDA jda, int userSize, List<User> users, long creatorId) {
        Thread expireThread = new Thread(new VotingRunnable(message, jda,userSize - 1, users, creatorId));
        expireThread.start();
    }


    public void removeGame(Game game) {
        games.remove(game);
    }
}
