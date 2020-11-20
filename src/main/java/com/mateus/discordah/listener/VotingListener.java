package com.mateus.discordah.listener;

import com.mateus.discordah.game.GameManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.List;

public class VotingListener extends ListenerAdapter {

    private final long messageID;
    private final int minVotes;
    private final List<User> usersToVote;
    private final long creatorId;
    private int votes = 0;


    public VotingListener(long messageID, int minVotes, List<User> usersToVote, long creatorId) {
        this.messageID = messageID;
        this.minVotes = minVotes;
        this.usersToVote = usersToVote;
        this.creatorId = creatorId;
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        long id = event.getMessageIdLong();
        if (id == messageID) {
            if (usersToVote.stream().noneMatch(u -> event.getUserIdLong() == u.getIdLong())) return;
            MessageReaction.ReactionEmote emote = event.getReactionEmote();
            if (emote.isEmoji()) {
                String unicode = emote.getEmoji();
                if (unicode.equals("\uD83C\uDDFD")) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setAuthor("Discord against humanity");
                    embedBuilder.setColor(0x1CBF1E);
                    embedBuilder.setDescription("Voting cancelled...");
                    event.getChannel().editMessageById(messageID, embedBuilder.build()).queue(m -> m.clearReactions().queue());
                    event.getJDA().removeEventListener(this);
                } else if (unicode.equals("✅")) {
                    votes++;
                    if (votes >= minVotes) {
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setAuthor("Discord against humanity");
                        embedBuilder.setColor(0x1CBF1E);
                        embedBuilder.setDescription("The game will soon start!");
                        event.getChannel().editMessageById(messageID, embedBuilder.build()).queue(m -> m.clearReactions().queue());
                        event.getJDA().removeEventListener(this);
                        GameManager.getInstance().createGame(usersToVote, event.getChannel(), creatorId);
                    }
                }
            }
        }
    }
}
