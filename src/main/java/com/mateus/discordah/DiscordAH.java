package com.mateus.discordah;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mateus.discordah.cards.Card;
import com.mateus.discordah.cards.CardComposite;
import com.mateus.discordah.listener.MessageListener;
import com.mateus.discordah.listener.StartListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Logger;

public class DiscordAH {

    private static final Logger BOT_LOGGER = Logger.getLogger("DiscordAH");

    public static void main(String[] args) throws IOException, LoginException {
        File tokenFile = new File("token.txt");
        if (!tokenFile.createNewFile()) {
            BOT_LOGGER.info("Token file found!");
        }
        File cards = new File("cards.json");
        if (!cards.exists()) {
            BOT_LOGGER.severe("The cards file doesn't exist");
            return;
        }
        String token = Files.readAllLines(Paths.get(tokenFile.getPath())).get(0);

        if (!setupCards()) {
            BOT_LOGGER.severe("Error while loading cards");
            return;
        }
        JDA jda = JDABuilder.createDefault(token)
                .setActivity(Activity.playing("dh!start"))
                .build();
        jda.addEventListener(new MessageListener());
        jda.addEventListener(new StartListener());
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String cmd = scanner.next();
                if (cmd.equalsIgnoreCase("shutdown")) {
                    jda.shutdown();
                    System.exit(0);
                } else if (cmd.equalsIgnoreCase("guilds")) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Guild guild : jda.getGuilds()) {
                        stringBuilder.append("- ").append(guild.getName()).append('\n');
                    }
                    System.out.println(stringBuilder.toString());
                }
            }
        });
        consoleThread.start();
    }

    private static boolean setupCards() throws IOException {
        File cardsFile = new File("cards.json");
        if (!cardsFile.exists()) {
            return false;
        }
        String content = new String(Files.readAllBytes(Paths.get(cardsFile.getPath())));

        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();

        JsonArray whiteCards = jsonObject.get("white").getAsJsonArray();
        JsonArray blackCards = jsonObject.get("black").getAsJsonArray();

        for (JsonElement whiteCard : whiteCards) {
            String text = whiteCard.getAsString();
            CardComposite.addCard(new Card(text));
        }

        for (JsonElement blackCard : blackCards) {
            JsonObject blackCardObject = blackCard.getAsJsonObject();
            String text = blackCardObject.get("text").getAsString();
            int slots = blackCardObject.get("pick").getAsInt();

            CardComposite.addCard(new Card(Card.CardType.BLACK, text, slots));
        }

        return true;
    }
}
