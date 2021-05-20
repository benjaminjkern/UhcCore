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
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.CitizensAPI;

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
                if (player.hasMetadata("NPC")) {
                    SentinelTrait sentinel = CitizensAPI.getNPCRegistry().getNPC(player).getTrait(SentinelTrait.class);
                    sentinel.health = healthAtStart;
                } else {
                    player.setHealth(healthAtStart);
                    VersionUtils.getVersionUtils().setPlayerMaxHealth(player, healthAtStart);
                }
            } catch (UhcPlayerNotOnlineException ex) {
                // Don't set max health for offline players.
            }
        }
        Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> { getGameManager().setPvp(false); }, 20);
        canTakeDamage = false;
        Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
            canTakeDamage = true;
            getGameManager().setPvp(true);
            getGameManager().broadcastMessage("You are no longer invulnerable!");
        }, 20 * time);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (canTakeDamage) return;

        e.setDamage(0);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) return;
        if (canTakeDamage) return;

        e.getDamager().sendMessage("\u00a75You can't damage them until " + (time / 60) + " minutes into the game!");
    }
}
