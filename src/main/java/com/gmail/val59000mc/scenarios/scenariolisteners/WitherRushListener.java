package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.UhcGameStateChangedEvent;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.listeners.PlayerDamageListener;
import com.gmail.val59000mc.listeners.PlayerDeathListener;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class WitherRushListener extends ScenarioListener {

    private UhcPlayer lastToHit;
    public static Wither gameWither;

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        lastToHit = null;

        new BukkitRunnable() {
            @Override
            public void run() { spawnWither(); }
        }.runTaskLater(UhcCore.getPlugin(), 10 * 60 * 20);
    }

    private void spawnWither() {
        if (gameWither != null) return;
        World overworld = Bukkit.getWorld(getGameManager().getConfiguration().getOverworldUuid());
        if (getGameManager().getGameState() == GameState.PLAYING) gameWither = (Wither) overworld
                .spawnEntity(overworld.getWorldBorder().getCenter().clone().add(0, 100, 0), EntityType.WITHER);
        else gameWither = (Wither) overworld.spawnEntity(overworld.getWorldBorder().getCenter().clone().add(0, 20, 0),
                EntityType.WITHER);
        getGameManager().broadcastInfoMessage("\u00a7d\u00a7oThe Wither has been spawned at \u00a7f\u00a7l("
                + overworld.getWorldBorder().getCenter().getBlockX() + ", "
                + overworld.getWorldBorder().getCenter().getBlockZ() + ")\u00a7d\u00a7o! Kill it to win the game!");
        gameWither.setCustomName("\u00a75\u00a7lWITHER");
    }

    @EventHandler
    public void onWitherHit(EntityDamageByEntityEvent e) {
        Entity damager = PlayerDeathListener.getRealDamager(e);
        if (damager instanceof Player) lastToHit = getPlayersManager().getUhcPlayer((Player) damager);
    }

    @EventHandler
    public void onWitherKill(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Wither)) return;
        Wither wither = (Wither) e.getEntity();
        UhcPlayer winner;
        if (wither.getKiller() != null) {
            winner = getPlayersManager().getUhcPlayer(wither.getKiller());
        } else {
            winner = lastToHit;
        }

        getGameManager()
                .broadcastInfoMessage(winner.getDisplayName() + " slayed the wither and won the game for their team!");

        getGameManager().endGame(winner.getTeam().getMembers().toArray(new UhcPlayer[0]));
    }

    @EventHandler
    public void onDebugChat(PlayerChatEvent e) {
        if (!e.getMessage().equalsIgnoreCase("wither")) return;
        spawnWither();
    }

}
