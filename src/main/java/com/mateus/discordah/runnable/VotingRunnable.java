package com.mateus.discordah.runnable;

import com.mateus.discordah.listener.VotingListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class VotingRunnable implements Runnable {

    public static final Set<Long> messages = new HashSet<>();

    private final Message message;
    private final long id;
    private final int minVotes;
    private final JDA jda;
    private final List<User> users;
    private final long creatorId;

    public VotingRunnable(Message message, JDA jda, int minVotes, List<User> users, long creatorId) {
        this.message = message;
        this.jda = jda;
        this.id = message.getIdLong();
        this.minVotes = minVotes;
        this.users = users;
        this.creatorId = creatorId;
        messages.add(id);
    }

    @Override
    public void run() {
        VotingListener votingListener = new VotingListener(id, minVotes + 1, users, creatorId);
        jda.addEventListener(votingListener);
        try{
            wait(TimeUnit.MINUTES.toMillis(2L));
        } catch (InterruptedException e) {
            e.printStackTrace();
            jda.removeEventListener(votingListener);
        }
        if (messages.contains(id)) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setAuthor("Discord against humanity");
            embedBuilder.setDescription("Voting expired...");
            embedBuilder.setColor(0x1CBF1E);
            message.editMessage(embedBuilder.build()).queue(m -> m.clearReactions().queue());
            jda.removeEventListener(votingListener);
        }
    }


}
