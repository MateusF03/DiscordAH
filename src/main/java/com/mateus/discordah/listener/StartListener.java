package com.mateus.discordah.listener;

import com.mateus.discordah.game.Game;
import com.mateus.discordah.game.GameManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class StartListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();
        if (content.toLowerCase().startsWith("dh!start")) {
            Message message = event.getMessage();
            if (GameManager.getInstance().userIsInGame(event.getAuthor())) {
                event.getChannel().sendMessage("**You are already in a game**").queue();
                return;
            }
            GameManager.getInstance().startVoting(message.getMentionedUsers(), event.getChannel(), event.getAuthor(), event.getJDA());
        } else if (content.toLowerCase().startsWith("dh!stop")) {
            User author = event.getAuthor();
            TextChannel channel = event.getChannel();
            Game game = GameManager.getInstance().getChannelGame(channel);
            if (game == null) {
                channel.sendMessage("This channel doesn't have a game running...").queue();
            } else {
                Guild guild = event.getGuild();
                Member authorMember = guild.getMember(author);
                Member selfMember = guild.getMember(event.getJDA().getSelfUser());
                if ((authorMember != null &&  selfMember != null && authorMember.canInteract(selfMember)) || game.getCreatorId() == author.getIdLong()) {
                    game.stop();
                    channel.sendMessage("**The game has been stopped**").queue();
                }
            }
        }
    }
}
