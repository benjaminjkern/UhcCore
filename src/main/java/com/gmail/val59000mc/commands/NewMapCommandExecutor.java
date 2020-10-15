package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.PlayersManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class NewMapCommandExecutor implements CommandExecutor {

    // - /newmap command force reloads and starts a new map
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("uhccore.commands.newmap")) return noPerms(sender);
        GameManager gm = GameManager.getGameManager();
        GameState gs = gm.getGameState();
        if (gs != GameState.WAITING && gs != GameState.STARTING
                && !sender.hasPermission("uhccore.commands.newmap.force")) {
            sender.sendMessage("\u00a7cThe game is already running! Cannot get a new map now!");
            return true;
        }

        PlayersManager pm = gm.getPlayersManager();
        gm.sendInfoToServer("SHUTTINGDOWN", false);

        if (gm.getConfiguration().getEnableBungeeSupport()) {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> { pm.sendPlayerToBungeeServer(player); });
        } else Bukkit.getServer().getOnlinePlayers()
                .forEach(player -> { player.kickPlayer(sender.getName() + " has requested a new map!"); });
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload confirm");
        return true;
    }

    private boolean noPerms(CommandSender sender) {
        sender.sendMessage("\u00a7cYou don't have permission to do that!");
        return true;
    }
}
