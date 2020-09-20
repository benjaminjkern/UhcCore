package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.exceptions.UhcPlayerDoesntExistException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayersManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RatingCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        PlayersManager pm = GameManager.getGameManager().getPlayersManager();

        switch (args.length) {
            case 0:
                try {
                    sender.sendMessage(
                            "Your Player Rating is: \u00a76" + pm.getScoreKeeper().getScore(sender.getName()));
                } catch (UhcPlayerDoesntExistException e) {
                    sender.sendMessage("Something went wrong. I think you don't exist.");
                }
                return true;
            case 1:
                if (!sender.hasPermission("uhccore.commands.rating.seeothers")) return noPerms(sender);
                try {
                    sender.sendMessage(
                            args[0] + "'s Player Rating is: \u00a76" + pm.getScoreKeeper().getScore(args[0]));
                } catch (UhcPlayerDoesntExistException e) {
                    sender.sendMessage("That player doesn't exist!");
                }
                return true;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "set":
                        if (!sender.hasPermission("uhccore.commands.rating.set")) return noPerms(sender);
                        try {
                            sender.sendMessage("Your Player Rating has been set to: \u00a76"
                                    + pm.getScoreKeeper().setScore(sender.getName(), Double.parseDouble(args[1])));
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("Something went wrong. I think you don't exist.");
                        }
                        return true;
                    case "add":
                        if (!sender.hasPermission("uhccore.commands.rating.set")) return noPerms(sender);
                        try {
                            sender.sendMessage("Your Player Rating has been set to: \u00a76"
                                    + pm.getScoreKeeper().addScore(sender.getName(), Double.parseDouble(args[1])));
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("Something went wrong. I think you don't exist.");
                        }
                        return true;
                    case "setelo":
                        if (!sender.hasPermission("uhccore.commands.rating.set")) return noPerms(sender);
                        try {
                            sender.sendMessage("Your Player Rating has been set to: \u00a76"
                                    + pm.getScoreKeeper().setScoreI(sender.getName(), Double.parseDouble(args[1])));
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("Something went wrong. I think you don't exist.");
                        }
                        return true;
                    case "addelo":
                        if (!sender.hasPermission("uhccore.commands.rating.set")) return noPerms(sender);
                        try {
                            sender.sendMessage("Your Player Rating has been set to: \u00a76"
                                    + pm.getScoreKeeper().addScoreI(sender.getName(), Double.parseDouble(args[1])));
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("Something went wrong. I think you don't exist.");
                        }
                        return true;
                    default:
                        if (args[1].equals("elo")) {
                            if (!sender.hasPermission("uhccore.commands.rating.seeothers")) return noPerms(sender);
                            try {
                                sender.sendMessage(args[0] + "'s Elo Player Rating is: \u00a76"
                                        + pm.getScoreKeeper().getScoreI(args[0]));
                            } catch (UhcPlayerDoesntExistException e) {
                                sender.sendMessage("That player doesn't exist!");
                            }
                            return true;
                        }
                        sender.sendMessage("Usage: /rating [set|add|setelo|addelo] (Number)");
                }
                return true;
            case 3:
                if (!sender.hasPermission("uhccore.commands.rating.set")) return noPerms(sender);
                switch (args[0].toLowerCase()) {
                    case "set":
                        try {
                            sender.sendMessage(args[1] + "'s Player Rating has been set to: \u00a76"
                                    + pm.getScoreKeeper().setScore(args[1], Double.parseDouble(args[2])));
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("That player doesn't exist!");
                        }
                        return true;
                    case "add":
                        try {
                            sender.sendMessage(args[1] + "'s Player Rating has been set to: \u00a76"
                                    + pm.getScoreKeeper().addScore(args[1], Double.parseDouble(args[2])));
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("That player doesn't exist!");
                        }
                        return true;
                    case "setelo":
                        try {
                            sender.sendMessage(args[1] + "'s Player Rating has been set to: \u00a76"
                                    + pm.getScoreKeeper().setScoreI(args[1], Double.parseDouble(args[2])));
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("That player doesn't exist!");
                        }
                        return true;
                    case "addelo":
                        try {
                            sender.sendMessage(args[1] + "'s Player Rating has been set to: \u00a76"
                                    + pm.getScoreKeeper().addScoreI(args[1], Double.parseDouble(args[2])));
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("That player doesn't exist!");
                        }
                        return true;
                    default:
                        sender.sendMessage("Usage: /rating [set|add|setelo|addelo] (Player) (Number)");
                }
                return true;
            default:
                sender.sendMessage("Usage: /rating [(Player)|set|add|setelo|addelo]");

        }
        return true;
    }

    private boolean noPerms(CommandSender sender) {
        sender.sendMessage("\u00a7cYou don't have permission to do that!");
        return true;
    }

}