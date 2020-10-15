package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.threads.LobbyPingThread;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PingCommandExecutor implements CommandExecutor {

    private final GameManager gameManager;

    public PingCommandExecutor(GameManager gameManager) { this.gameManager = gameManager; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("uhc-core.commands.ping")) return true;
        switch (args.length) {
            case 0:
                if (gameManager.sendInfoToServer("CURRENTSIZE:" + Bukkit.getOnlinePlayers().size(), false))
                    sender.sendMessage("Pinged Lobby Server.");
                else sender.sendMessage("\u00a7cFailed to ping lobby server, try \u00a7l/ping on");
                return true;
            case 1:
                switch (args[0]) {
                    case "off":
                        sender.sendMessage("Unregistering this server from lobby.");
                        gameManager.sendInfoToServer("SHUTTINGDOWN", false);
                        LobbyPingThread.stop();
                        return true;
                    case "on":
                        sender.sendMessage("Registering this server with lobby.");
                        gameManager.sendInfoToServer("CURRENTSIZE:" + Bukkit.getOnlinePlayers().size(), true);
                        return true;
                }
        }
        return false;
    }

}
