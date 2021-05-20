package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcPlayerDeathEvent;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.listeners.PlayerDamageListener;
import com.gmail.val59000mc.listeners.PlayerDeathListener;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.scenarios.Option;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import com.gmail.val59000mc.UhcCore;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class LilCheatListener extends ScenarioListener {

    @Option(key = "time")
    public static int time = 60;

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        getGameManager().broadcastInfoMessage("You get " + time + " seconds of Creative!");
        getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player thisPlayer = uhcPlayer.getPlayer();
                thisPlayer.setGameMode(GameMode.CREATIVE);
                thisPlayer.setInvulnerable(true);

            } catch (UhcPlayerNotOnlineException ex) {
                // No gamemode for offline players
            }
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                getGameManager().broadcastInfoMessage("Alright that's enough time!");

                getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
                    try {
                        Player thisPlayer = uhcPlayer.getPlayer();
                        thisPlayer.setGameMode(GameMode.SURVIVAL);
                        thisPlayer.setInvulnerable(false);

                    } catch (UhcPlayerNotOnlineException ex) {
                        // No gamemode for offline players
                    }
                });
            }

        }.runTaskLater(UhcCore.getPlugin(), 20 * time);
    }

    public void onDamage(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        if (getGameManager().getElapsedTime() < time * 1000) e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(UhcPlayerDeathEvent e) {
        if (!PlayerDeathListener.autoRespawn) return;
        UhcPlayer uhcKilled = e.getKilled();
        Player killed;
        try {
            killed = uhcKilled.getPlayer();
        } catch (UhcPlayerNotOnlineException ex) {
            return;
        }
        killed.setGameMode(GameMode.CREATIVE);
        killed.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent("You have been given \u00a7d10 \u00a7fseconds of\u00a7dCreative Mode\u00a7f!"));
        new BukkitRunnable() {
            @Override
            public void run() {
                killed.setGameMode(GameMode.SURVIVAL);
                killed.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("Alright that's enough time!"));
            }
        }.runTaskLater(UhcCore.getPlugin(), 10 * 20);
    }

}