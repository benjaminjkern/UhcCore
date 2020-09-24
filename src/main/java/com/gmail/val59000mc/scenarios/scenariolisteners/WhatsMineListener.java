package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartingEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.RandomUtils;
import com.gmail.val59000mc.UhcCore;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
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

import java.util.List;

public class WhatsMineListener extends ScenarioListener {

    // I'm not entirely sure how to do this
    @EventHandler
    public void onPickUpItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        Item item = e.getItem();

        getPlayersManager().getOnlinePlayingPlayers().forEach(uhcPlayer -> {
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
        getPlayersManager().getOnlinePlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player otherPlayer = uhcPlayer.getPlayer();
                if (otherPlayer == player) return;

                int slot = player.getInventory().getHeldItemSlot();
                ItemStack items = otherPlayer.getInventory().getItem(slot);
                MaterialData md = items.getData();
                ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
                newItems.setData(md);
                otherPlayer.getInventory().setItem(slot, newItems);
            } catch (UhcPlayerNotOnlineException p) {}
        });
    }

    @EventHandler
    public void onBreakTool(PlayerItemBreakEvent e) {
        Player player = e.getPlayer();
        getPlayersManager().getOnlinePlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player otherPlayer = uhcPlayer.getPlayer();
                if (otherPlayer == player) return;

                int slot = player.getInventory().getHeldItemSlot();
                ItemStack items = otherPlayer.getInventory().getItem(slot);
                MaterialData md = items.getData();
                ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
                newItems.setData(md);
                otherPlayer.getInventory().setItem(slot, newItems);
            } catch (UhcPlayerNotOnlineException p) {}
        });
    }

    @EventHandler
    public void onBreakTool(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        getPlayersManager().getOnlinePlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player otherPlayer = uhcPlayer.getPlayer();
                if (otherPlayer == player) return;

                int slot = player.getInventory().getHeldItemSlot();
                ItemStack items = otherPlayer.getInventory().getItem(slot);
                MaterialData md = items.getData();
                ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
                newItems.setData(md);
                otherPlayer.getInventory().setItem(slot, newItems);
            } catch (UhcPlayerNotOnlineException p) {}
        });
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        getPlayersManager().getOnlinePlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player otherPlayer = uhcPlayer.getPlayer();
                if (otherPlayer == player) return;

                int slot = player.getInventory().getHeldItemSlot();
                ItemStack items = otherPlayer.getInventory().getItem(slot);
                MaterialData md = items.getData();
                ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
                newItems.setData(md);
                otherPlayer.getInventory().setItem(slot, newItems);
            } catch (UhcPlayerNotOnlineException p) {}
        });
    }

    @EventHandler
    public void onInventoryAction(InventoryClickEvent e) {
        final Player player = (Player) e.getWhoClicked();

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

    @EventHandler
    public void onInventoryAction(InventoryDragEvent e) {
        final Player player = (Player) e.getWhoClicked();

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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        final Player player = e.getPlayer();

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