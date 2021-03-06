package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import me.lkp111138.deebot.DeeBot;
import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.game.ConcurrentGameException;
import me.lkp111138.deebot.game.Game;
import me.lkp111138.deebot.game.GroupInfo;
import me.lkp111138.deebot.translation.Translation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayCommand implements Command {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        // ono LOGIC :EYES:
        if (msg.chat().type() == Chat.Type.Private) {
            // private chat cya
            bot.execute(new SendMessage(msg.chat().id(), "You cannot play in private! Try using /" + args[0] + " in a group."));
        } else {
            Game g = Game.byGroup(msg.chat().id());
            if (g == null) {
                try (Connection conn = Main.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("SELECT chips_per_card, wait_time, turn_wait_time, lang, fry, collect_place, protest_mode FROM `groups` WHERE gid=?");
                    stmt.setLong(1, msg.chat().id());
                    ResultSet rs = stmt.executeQuery();
                    GroupInfo info;
                    int turn_wait = 45;
                    int wait = 120;
                    int chips = 1;
                    boolean fry = false;
                    boolean collect_place = false;
                    if (rs.next()) {
                        turn_wait = rs.getInt(3);
                        wait = rs.getInt(2);
                        chips = rs.getInt(1);
                        fry = rs.getBoolean(5);
                        collect_place = rs.getBoolean(6);
                        if (rs.getInt(7) > 0) {
                            bot.execute(new SendMessage(msg.chat().id(), Translation.get(DeeBot.lang(msg.chat().id())).JOIN_69_PROTEST()).replyToMessageId(msg.messageId()));
//                            return;
                        }
                    } else {
                        // insert
                        PreparedStatement stmt1 = conn.prepareStatement("INSERT INTO `groups` (gid) VALUES (?)");
                        stmt1.setLong(1, msg.chat().id());
                        stmt1.execute();
                        stmt1.close();
                    }
                    info = new GroupInfo(turn_wait, collect_place, fry);
                    try {
                        new Game(msg, chips, wait, info);
                    } catch (ConcurrentGameException e) {
                        if (e.getGame().started()) {
                            bot.execute(new SendMessage(msg.chat().id(), Translation.get(DeeBot.lang(msg.chat().id())).GAME_STARTED()).replyToMessageId(msg.messageId()));
                        } else {
                            bot.execute(new SendMessage(msg.chat().id(), Translation.get(DeeBot.lang(msg.chat().id())).GAME_STARTING()).replyToMessageId(msg.messageId()));
                        }
                    }
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    bot.execute(new SendMessage(msg.chat().id(), Translation.get(DeeBot.lang(msg.chat().id())).ERROR() + e.getMessage()).replyToMessageId(msg.messageId()));
                }
            } else {
                if (g.started()) {
                    bot.execute(new SendMessage(msg.chat().id(), Translation.get(DeeBot.lang(msg.chat().id())).GAME_STARTED()).replyToMessageId(msg.messageId()));
                } else {
//                    bot.execute(new SendMessage(msg.chat().id(), Translation.get(DeeBot.lang(msg.chat().id())).GAME_STARTING()).replyToMessageId(msg.messageId()));
                    // as if it's a join
                    new JoinCommand().respond(bot, msg, args);
                }
            }
        }
    }
}
