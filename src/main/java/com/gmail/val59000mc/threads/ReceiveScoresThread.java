package com.gmail.val59000mc.threads;

import java.io.IOException;
import java.util.Scanner;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayerStats;

import org.bukkit.Bukkit;

import org.bukkit.command.CommandException;

public class ReceiveScoresThread implements Runnable {
    public void run() {
        try (Scanner in = new Scanner(GameManager.getGameManager().getLobbySocket().getInputStream())) {
            while (in.hasNextLine() && UhcCore.getPlugin().isEnabled()) {
                String[] input = in.nextLine().split(":");
                switch (input[0]) {
                case "STATS":
                    // update player rating
                    PlayerStats ps = PlayerStats.newFromParse(input);
                    GameManager.getGameManager().getPlayersManager().getScoreKeeper().storePlayer(ps);
                    continue;
                case "MSG":
                    GameManager.getGameManager().broadcastInfoMessage(input[1]);
                    continue;
                case "RAWMSG":
                    GameManager.getGameManager().broadcastMessage(input[1]);
                    continue;
                case "GROUP":
                    // Bukkit.getLogger().info(String.join(":", input));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(),
                            () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                                    "lp user " + input[1] + " parent set " + input[2]));
                    continue;
                }

                Bukkit.getLogger().info(input + " was not recognized!");
            }
        } catch (IOException e) {
        }
    }

}
