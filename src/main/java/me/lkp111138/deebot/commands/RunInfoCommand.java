package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.game.Game;

public class RunInfoCommand implements Command {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        Game.RunInfo runInfo = Game.runInfo();
        StringBuilder text = new StringBuilder(String.format("Total games: %d\nRunning games: %d\nPlayers: %d\nGames:", runInfo.gameCount, runInfo.runningCount, runInfo.playerCount));
        if (msg.from().id() == Main.BOT_OWNER) {
            for (Long key : runInfo.games.keySet()) {
                Game game = runInfo.games.get(key);
                text.append(String.format("Group ID: %s\nRunning: %s\nPlayers:\n", key, game.started()));
                for (User player : game.getPlayers()) {
                    text.append(String.format("<a href=\"tg://user?id=%s\">%s</a> (%s)", player.id(), player.firstName(), player.id()));
                }
            }
        }
        bot.execute(new SendMessage(msg.chat().id(), text.toString()));
    }
}
