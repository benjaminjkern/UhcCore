package com.gmail.val59000mc.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.exceptions.UhcPlayerDoesntExistException;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.event.NPCTeleportEvent;

public class PlayerMovementListener implements Listener {

    private final PlayersManager playersManager;

    public PlayerMovementListener(PlayersManager playersManager) { this.playersManager = playersManager; }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) { handleFrozenPlayers(event); }

    private void handleFrozenPlayers(PlayerMoveEvent e) {
        UhcPlayer uhcPlayer = playersManager.getUhcPlayer(e.getPlayer());
        if (uhcPlayer.isFrozen()) {
            Location freezeLoc = uhcPlayer.getFreezeLocation();
            Location toLoc = e.getTo();

            if (toLoc.getBlockX() != freezeLoc.getBlockX() || toLoc.getBlockZ() != freezeLoc.getBlockZ()) {
                Location newLoc = toLoc.clone();
                newLoc.setX(freezeLoc.getBlockX() + .5);
                newLoc.setZ(freezeLoc.getBlockZ() + .5);

                e.getPlayer().teleport(newLoc);
            }
        }
    }

    @EventHandler
    public void onNPCTeleport(NPCTeleportEvent e) {
        if (Bukkit.getOnlinePlayers().stream().anyMatch(player -> {
            if (GameManager.getGameManager().getPlayersManager().getUhcPlayer(player).getState() == PlayerState.PLAYING)
                return player.getLocation().distanceSquared(e.getFrom()) < 50 * 50
                        || player.getLocation().distanceSquared(e.getTo()) < 50 * 50;
            return false;
        })) {
            e.setCancelled(true);
            return;
        }

        if (GameManager.getGameManager().getWorldBorder().isWithinBorder(e.getFrom())) {
            e.setCancelled(true);
            return;
        }

        // if (e.getTo().getY() != PlayersManager.verifySafe(e.getTo()).getY()) {
        // e.setCancelled(true);
        // return;
        // }
    }

    @EventHandler
    public void onNPCDeSpawn(NPCDespawnEvent e) {
        LivingEntity npc = ((LivingEntity) e.getNPC().getEntity());
        if (npc == null) return;
        for (ItemStack item : npc.getEquipment().getArmorContents()) {
            if (item == null) continue;
            if (!((Player) e.getNPC().getEntity()).getInventory().addItem(item).isEmpty())
                ((Player) e.getNPC().getEntity()).getInventory().setItem((int) (Math.random() * 36), item);
        }
    }

    @EventHandler
    public void onNPCSpawn(NPCSpawnEvent e) {
        try {
            UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayersManager()
                    .getUhcPlayer((Player) e.getNPC().getEntity());
            GameManager.getGameManager().getScoreboardManager().updatePlayerTab(uhcPlayer);
        } catch (Exception ex) {}
    }

}