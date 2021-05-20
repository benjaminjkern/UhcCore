package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.threads.PreStartDeathmatchThread;
import com.gmail.val59000mc.threads.WorldBorderShrinkThread;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathmatchListener extends ScenarioListener {
    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        new BukkitRunnable() {
            public void run() {
                if (getGameManager().getGameState() != GameState.DEATHMATCH) {
                    Bukkit.getScheduler().runTask(UhcCore.getPlugin(), new PreStartDeathmatchThread(30));
                    WorldBorderShrinkThread.enabled = false;
                }
            }
        }.runTaskLater(UhcCore.getPlugin(), 20 * (int) (Math.random() * 60 * 25 + 5 * 60) - 20 * 30);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        new BukkitRunnable() {
            public void run() {
                if (getPlayersManager().getAllPlayingPlayers().size() == 2
                        && getGameManager().getGameState() != GameState.DEATHMATCH) {
                    Bukkit.getScheduler().runTask(UhcCore.getPlugin(), new PreStartDeathmatchThread(10));
                    WorldBorderShrinkThread.enabled = false;
                }
            }
        }.runTaskLater(UhcCore.getPlugin(), 20);
    }

    @EventHandler
    public void onDebugChat(PlayerChatEvent e) {
        if (!e.getMessage().equalsIgnoreCase("deathmatch")) return;
        if (getGameManager().getGameState() != GameState.DEATHMATCH) {
            Bukkit.getScheduler().runTask(UhcCore.getPlugin(), new PreStartDeathmatchThread(10));
            WorldBorderShrinkThread.enabled = false;
        }
    }
}
