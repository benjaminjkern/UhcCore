package com.gmail.val59000mc.threads;

import java.util.Scanner;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayerStats;

import org.bukkit.Bukkit;

public class ReceiveScoresThread implements Runnable {
    public void run() {
        try (Scanner in = new Scanner(GameManager.getGameManager().getLobbySocket().getInputStream())) {
            while (in.hasNextLine() && UhcCore.getPlugin().isEnabled()) {
                String input = in.nextLine();
                if (input.startsWith("STATS:")) {
                    // update player rating
                    PlayerStats ps = PlayerStats.newFromParse(input.substring(6));
                    GameManager.getGameManager().getPlayersManager().getScoreKeeper().storePlayer(ps);
                    continue;
                }

                Bukkit.getLogger().info(input + " was not recognized!");
            }
        } catch (Exception e) {}
    }

}
