package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.ScoreKeeper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RatingCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        GameManager gm = GameManager.getGameManager();
        ScoreKeeper sk = gm.getPlayersManager().getScoreKeeper();

        sender.sendMessage(
                "Your player rating is: \u00a76" + String.format("%.2f", sk.getStats(sender.getName()).rating));
        return true;
    }

}