package com.gmail.val59000mc.commands;

import java.util.stream.Collectors;

import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.listeners.InventoryGUIListener;
import com.gmail.val59000mc.players.UhcPlayer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            UhcPlayer u = GameManager.getGameManager().getPlayersManager().getUhcPlayer((Player) sender);
            ((Player) sender).openInventory(GameManager.getGameManager().getListInventoryHandler().getListInventory(u));
            return true;
        }
        String message = "\nPlayers Online: " + Bukkit.getOnlinePlayers().size();
        if (Bukkit.getOnlinePlayers().size() > 0) message += "\n  "
                + Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.joining(","));
        sender.sendMessage(message);
        return true;
    }
}
