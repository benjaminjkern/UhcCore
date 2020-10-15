package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.VersionUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class SuddenDeathListener extends ScenarioListener {

    @Option
    private int time = 60 * 10;
    @Option
    private int healthAtStart = 2;

    private boolean canTakeDamage;

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {

        for (UhcPlayer uhcPlayer : e.getPlayersManager().getAllPlayingPlayers()) {
            try {
                Player player = uhcPlayer.getPlayer();
                player.setHealth(healthAtStart);
                VersionUtils.getVersionUtils().setPlayerMaxHealth(player, healthAtStart);
            } catch (UhcPlayerNotOnlineException ex) {
                // Don't set max health for offline players.
            }
        }
        canTakeDamage = false;
        Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
            canTakeDamage = true;
            getGameManager().broadcastMessage("You are no longer invulnerable!");
        }, 20 * time);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (canTakeDamage) return;
        e.setDamage(0);
    }
}
