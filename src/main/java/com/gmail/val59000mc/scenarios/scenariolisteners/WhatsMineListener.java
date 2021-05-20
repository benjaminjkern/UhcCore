package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.listeners.PlayerDeathListener;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.UhcCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class WhatsMineListener extends ScenarioListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnable() { PlayerDeathListener.dropItems = false; }

    @EventHandler
    public void onPickUpItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        UhcPlayer up = GameManager.getGameManager().getPlayersManager().getUhcPlayer(player);
        if (up.getState() != PlayerState.PLAYING) return;
        Item item = e.getItem();

        getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player otherPlayer = uhcPlayer.getPlayer();
                if (otherPlayer == player) return;

                otherPlayer.getInventory().addItem(item.getItemStack());
            } catch (UhcPlayerNotOnlineException p) {}
        });
    }

    @EventHandler
    public void onThrowItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        UhcPlayer up = GameManager.getGameManager().getPlayersManager().getUhcPlayer(player);
        if (up.getState() != PlayerState.PLAYING) return;
        getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player otherPlayer = uhcPlayer.getPlayer();
                if (otherPlayer == player) return;

                int slot = player.getInventory().getHeldItemSlot();
                ItemStack items = otherPlayer.getInventory().getItem(slot);
                if (items != null) {
                    ItemMeta md = items.getItemMeta();
                    ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
                    newItems.setItemMeta(md);
                    otherPlayer.getInventory().setItem(slot, newItems);
                }
            } catch (UhcPlayerNotOnlineException p) {}
        });

    }

    @EventHandler
    public void onBreakTool(PlayerItemBreakEvent e) {
        Player player = e.getPlayer();
        UhcPlayer up = GameManager.getGameManager().getPlayersManager().getUhcPlayer(player);
        if (up.getState() != PlayerState.PLAYING) return;
        getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player otherPlayer = uhcPlayer.getPlayer();
                if (otherPlayer == player) return;

                int slot = player.getInventory().getHeldItemSlot();
                ItemStack items = otherPlayer.getInventory().getItem(slot);
                ItemMeta im = items.getItemMeta();
                ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
                newItems.setItemMeta(im);
                otherPlayer.getInventory().setItem(slot, newItems);
            } catch (UhcPlayerNotOnlineException p) {}
        });
    }

    @EventHandler
    public void onBreakTool(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        UhcPlayer up = GameManager.getGameManager().getPlayersManager().getUhcPlayer(player);
        if (up.getState() != PlayerState.PLAYING) return;
        getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player otherPlayer = uhcPlayer.getPlayer();
                if (otherPlayer == player) return;

                int slot = player.getInventory().getHeldItemSlot();
                ItemStack items = otherPlayer.getInventory().getItem(slot);
                ItemMeta im = items.getItemMeta();
                ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
                newItems.setItemMeta(im);
                otherPlayer.getInventory().setItem(slot, newItems);
            } catch (UhcPlayerNotOnlineException p) {}
        });
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        UhcPlayer up = GameManager.getGameManager().getPlayersManager().getUhcPlayer(player);
        if (up.getState() != PlayerState.PLAYING) return;
        getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player otherPlayer = uhcPlayer.getPlayer();
                if (otherPlayer == player) return;

                int slot = player.getInventory().getHeldItemSlot();
                ItemStack items = otherPlayer.getInventory().getItem(slot);
                ItemMeta im = items.getItemMeta();
                ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
                newItems.setItemMeta(im);
                otherPlayer.getInventory().setItem(slot, newItems);
            } catch (UhcPlayerNotOnlineException p) {}
        });
    }

    @EventHandler
    public void onInventoryAction(InventoryClickEvent e) {
        final Player player = (Player) e.getWhoClicked();

        UhcPlayer up = GameManager.getGameManager().getPlayersManager().getUhcPlayer(player);
        if (up.getState() != PlayerState.PLAYING) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                final ItemStack[] contents = player.getInventory().getContents();

                getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
                    try {
                        Player otherPlayer = uhcPlayer.getPlayer();
                        if (otherPlayer == player) return;

                        otherPlayer.getInventory().setContents(contents);
                    } catch (UhcPlayerNotOnlineException p) {}
                });
            }

        }.runTaskLater(UhcCore.getPlugin(), 1);
    }

    @EventHandler
    public void onInventoryAction(InventoryDragEvent e) {
        final Player player = (Player) e.getWhoClicked();

        UhcPlayer up = GameManager.getGameManager().getPlayersManager().getUhcPlayer(player);
        if (up.getState() != PlayerState.PLAYING) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                final ItemStack[] contents = player.getInventory().getContents();

                getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
                    try {
                        Player otherPlayer = uhcPlayer.getPlayer();
                        if (otherPlayer == player) return;

                        otherPlayer.getInventory().setContents(contents);
                    } catch (UhcPlayerNotOnlineException p) {}
                });
            }

        }.runTaskLater(UhcCore.getPlugin(), 1);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        final Player player = e.getPlayer();

        UhcPlayer up = GameManager.getGameManager().getPlayersManager().getUhcPlayer(player);
        if (up.getState() != PlayerState.PLAYING) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                final ItemStack[] contents = player.getInventory().getContents();

                getPlayersManager().getOnlinePlayingPlayers().forEach(uhcPlayer -> {
                    try {
                        Player otherPlayer = uhcPlayer.getPlayer();
                        if (otherPlayer == player) return;

                        otherPlayer.getInventory().setContents(contents);
                    } catch (UhcPlayerNotOnlineException p) {}
                });
            }

        }.runTaskLater(UhcCore.getPlugin(), 1);
    }

}