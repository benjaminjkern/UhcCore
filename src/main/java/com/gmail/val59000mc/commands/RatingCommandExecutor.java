package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.exceptions.UhcPlayerDoesntExistException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.ScoreKeeper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RatingCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        GameManager gm = GameManager.getGameManager();
        ScoreKeeper sk = gm.getPlayersManager().getScoreKeeper();

        switch (args.length) {
            case 0:
                try {
                    sender.sendMessage(
                            "Your Player Rating is: \u00a76" + String.format("%.2f", sk.getScore(sender.getName())));
                } catch (UhcPlayerDoesntExistException e) {
                    sender.sendMessage("Something went wrong. I think you don't exist.");
                }
                return true;
            case 1:
                if (args[0].equals("top")) { return showTopRatings(sender, 10); }
                if (!sender.hasPermission("uhccore.commands.rating.seeothers")) return noPerms(sender);
                try {
                    sender.sendMessage(
                            args[0] + "'s Player Rating is: \u00a76" + String.format("%.2f", sk.getScore(args[0])));
                } catch (UhcPlayerDoesntExistException e) {
                    sender.sendMessage("That player doesn't exist!");
                }
                return true;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "set":
                        if (!sender.hasPermission("uhccore.commands.rating.set")) return noPerms(sender);
                        try {
                            sender.sendMessage("Your Player Rating has been set to: \u00a76" + String.format("%.2f",
                                    sk.setScore(sender.getName(), Double.parseDouble(args[1]))));
                            gm.sendInfoToServer("RATING:" + sender.getName() + ":" + sk.getScore(sender.getName()),
                                    false);
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("Something went wrong. I think you don't exist.");
                        }
                        return true;
                    case "add":
                        if (!sender.hasPermission("uhccore.commands.rating.set")) return noPerms(sender);
                        try {
                            sender.sendMessage("Your Player Rating has been set to: \u00a76" + String.format("%.2f",
                                    sk.addScore(sender.getName(), Double.parseDouble(args[1]))));
                            gm.sendInfoToServer("RATING:" + sender.getName() + ":" + sk.getScore(sender.getName()),
                                    false);
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("Something went wrong. I think you don't exist.");
                        }
                        return true;
                    case "setelo":
                        if (!sender.hasPermission("uhccore.commands.rating.set")) return noPerms(sender);
                        try {
                            sender.sendMessage("Your Player Rating has been set to: \u00a76" + String.format("%.2f",
                                    sk.setScoreI(sender.getName(), Double.parseDouble(args[1]))));
                            gm.sendInfoToServer("RATING:" + sender.getName() + ":" + sk.getScore(sender.getName()),
                                    false);
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("Something went wrong. I think you don't exist.");
                        }
                        return true;
                    case "addelo":
                        if (!sender.hasPermission("uhccore.commands.rating.set")) return noPerms(sender);
                        try {
                            sender.sendMessage("Your Player Rating has been set to: \u00a76" + String.format("%.2f",
                                    sk.addScoreI(sender.getName(), Double.parseDouble(args[1]))));
                            gm.sendInfoToServer("RATING:" + sender.getName() + ":" + sk.getScore(sender.getName()),
                                    false);
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("Something went wrong. I think you don't exist.");
                        }
                        return true;
                    case "top":
                        if (Integer.parseInt(args[1]) > 50) { sender.sendMessage("\u00a7cCan only show the top 50!"); }
                        return showTopRatings(sender, Math.min(Integer.parseInt(args[1]), 50));
                    default:
                        if (args[1].equals("elo")) {
                            if (!sender.hasPermission("uhccore.commands.rating.seeothers")) return noPerms(sender);
                            try {
                                sender.sendMessage(args[0] + "'s Elo Player Rating is: \u00a76"
                                        + String.format("%.2f", sk.getScoreI(args[0])));
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
                                    + String.format("%.2f", sk.setScore(args[1], Double.parseDouble(args[2]))));
                            gm.sendInfoToServer("RATING:" + args[1] + ":" + sk.getScore(args[1]), false);
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("That player doesn't exist!");
                        }
                        return true;
                    case "add":
                        try {
                            sender.sendMessage(args[1] + "'s Player Rating has been set to: \u00a76"
                                    + String.format("%.2f", sk.addScore(args[1], Double.parseDouble(args[2]))));
                            gm.sendInfoToServer("RATING:" + args[1] + ":" + sk.getScore(args[1]), false);
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("That player doesn't exist!");
                        }
                        return true;
                    case "setelo":
                        try {
                            sender.sendMessage(args[1] + "'s Player Rating has been set to: \u00a76"
                                    + String.format("%.2f", sk.setScoreI(args[1], Double.parseDouble(args[2]))));
                            gm.sendInfoToServer("RATING:" + args[1] + ":" + sk.getScore(args[1]), false);
                        } catch (UhcPlayerDoesntExistException e) {
                            sender.sendMessage("That player doesn't exist!");
                        }
                        return true;
                    case "addelo":
                        try {
                            sender.sendMessage(args[1] + "'s Player Rating has been set to: \u00a76"
                                    + String.format("%.2f", sk.addScoreI(args[1], Double.parseDouble(args[2]))));
                            gm.sendInfoToServer("RATING:" + args[1] + ":" + sk.getScore(args[1]), false);
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

    private boolean showTopRatings(CommandSender sender, int amount) {
        sender.sendMessage("\u00a7cYou don't have permission to do that!");
        return true;
    }

}