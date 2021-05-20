package com.gmail.val59000mc.listeners;

import java.util.Arrays;

import com.gmail.val59000mc.exceptions.UhcPlayerDoesntExistException;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.threads.UpdateBotsThread;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffectType;

import net.citizensnpcs.api.event.*;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.event.*;

import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.events.*;

public class BotListener implements Listener {
    @EventHandler
    public void onNavigation(NavigationBeginEvent e) {
        Navigator nav = e.getNavigator();
        EntityTarget target = nav.getEntityTarget();
        if (target == null) return;
        Entity chasing = target.getTarget();

        if (!GameManager.getGameManager().getWorldBorder().isWithinBorder(chasing.getLocation())
                || (chasing.isInvulnerable() || chasing.isDead())) {
            UpdateBotsThread.reroute(e.getNPC());
            return;
        }
        if (!(chasing instanceof LivingEntity)) return;
        LivingEntity lchasing = (LivingEntity) chasing;
        if (UpdateBotsThread.isInvisible(lchasing)) { UpdateBotsThread.reroute(e.getNPC()); }
    }

    @EventHandler
    public void onNPCTeleport(NPCTeleportEvent e) {
        if (Bukkit.getOnlinePlayers().stream().anyMatch(player -> {
            if (GameManager.getGameManager().getPlayersManager().getUhcPlayer(player).getState() == PlayerState.PLAYING)
                return player.getLocation().distanceSquared(e.getFrom()) < 50 * 50
                        || player.getLocation().distanceSquared(e.getTo()) < 50 * 50;
            return false;
        }) || GameManager.getGameManager().getWorldBorder().isWithinBorder(e.getFrom())) { e.setCancelled(true); }
    }

    @EventHandler
    public void onNPCSpawn(NPCSpawnEvent e) {
        try {
            UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayersManager()
                    .getUhcPlayer((Player) e.getNPC().getEntity());
            GameManager.getGameManager().getScoreboardManager().updatePlayerTab(uhcPlayer);
        } catch (Exception ex) {}
        // exception because it thinks uhcplayerdoesntexistexception can't be thrown
        // here
    }
}
