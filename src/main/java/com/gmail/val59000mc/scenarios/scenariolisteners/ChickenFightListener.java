package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartingEvent;
import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ChickenFightListener extends ScenarioListener {

    public static Map<UhcPlayer, UhcPlayer> pairs;
    Set<UhcPlayer> tryingToSwitch;
    public static boolean disabled;

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        pairs = new HashMap<>();
        disabled = false;
        tryingToSwitch = new HashSet<>();

        getTeamManager().getUhcTeams().forEach(uhcTeam -> {
            for (int i = 0; i < uhcTeam.getMembers().size(); i += 2) {
                ride(uhcTeam.getMembers().get(i), uhcTeam.getMembers().get(i + 1));
            }
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                if (disabled) return;
                Set<Entry<UhcPlayer, UhcPlayer>> entrySet = new HashSet<>(pairs.entrySet());
                entrySet.forEach(entry -> { ride(entry.getValue(), entry.getKey()); });
            }
        }.runTaskTimer(UhcCore.getPlugin(), 20, 20);

    }

    private void ride(UhcPlayer uhcBottom, UhcPlayer uhcTop) {

        pairs.remove(uhcTop);
        pairs.remove(uhcBottom);
        pairs.put(uhcTop, uhcBottom);

        try {
            Player bottom = uhcBottom.getPlayer();
            Player top = uhcTop.getPlayer();

            bottom.eject();
            top.eject();

            try {
                bottom.addPassenger(top);
            } catch (IllegalStateException e) {
                top.eject();
                bottom.eject();
            }
        } catch (Exception e1) {
            Bukkit.getLogger().info("OOPS");
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (disabled) return;

        UhcPlayer uhcPlayer = getPlayersManager().getUhcPlayer(e.getPlayer());
        UhcPlayer chickenMate = getPartner(uhcPlayer);

        try {
            if (isTop(uhcPlayer)) {
                if (!chickenMate.getPlayer().hasMetadata("NPC") && !chickenMate.getPlayer().isSneaking())
                    e.setCancelled(true);
                return;
            }

            if (chickenMate.getPlayer().hasMetadata("NPC")) {
                tryingToSwitch.add(chickenMate);
                e.getPlayer().eject();
            }
        } catch (Exception e1) {

        }

    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (disabled) return;
        if (e.getDismounted() instanceof Player && e.getEntity() instanceof Player && !e.getDismounted().isDead()) {
            Player bottom = (Player) e.getDismounted();
            Player top = (Player) e.getEntity();

            UhcPlayer uhcBottom = getPlayersManager().getUhcPlayer(bottom);
            UhcPlayer uhcTop = getPlayersManager().getUhcPlayer(top);

            if ((bottom.hasMetadata("NPC") || bottom.isSneaking())
                    && (!top.hasMetadata("NPC") || tryingToSwitch.contains(uhcTop))) {
                tryingToSwitch.remove(uhcTop);

                bottom.setSneaking(false);
                top.setSneaking(false);

                Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> ride(uhcTop, uhcBottom), 10);
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onGameStateChange(UhcStartingEvent e) {
        List<UhcPlayer> players = new ArrayList<>(getPlayersManager().getPlayersList());

        // dont do it if its only 2 players in the game, although this should never
        // happen since bots exist also the compatibility check should run
        if (players.size() <= 2) return;

        int maxPlayers = GameManager.getGameManager().getConfiguration().getMaxPlayersPerTeam();
        if (maxPlayers % 2 == 0) return;
        maxPlayers += 1;
        List<UhcPlayer> realPlayers = players.stream()
                .filter(uhcPlayer -> !uhcPlayer.getName().equalsIgnoreCase("YEUH-BOT")).collect(Collectors.toList());

        while (players.size() >= maxPlayers) {
            int r;
            UhcPlayer player1;
            if (!realPlayers.isEmpty()) {
                r = (int) (Math.random() * realPlayers.size());
                player1 = realPlayers.get(r);
                realPlayers.remove(player1);
            } else {
                r = (int) (Math.random() * players.size());
                player1 = players.get(r);
            }
            players.remove(player1);

            for (int i = 1; i < maxPlayers && !players.isEmpty(); i++) {
                UhcPlayer playerNext;
                if (!realPlayers.isEmpty()) {
                    r = (int) (Math.random() * realPlayers.size());
                    playerNext = realPlayers.get(r);
                    realPlayers.remove(playerNext);
                } else {
                    r = (int) (Math.random() * players.size());
                    playerNext = players.get(r);
                }
                players.remove(playerNext);

                playerNext.setTeam(player1.getTeam());
                player1.getTeam().getMembers().add(playerNext);
            }
        }
    }

    private boolean isTop(UhcPlayer p) { return pairs.containsKey(p); }

    private UhcPlayer getPartner(UhcPlayer p) {
        if (pairs.containsKey(p)) return pairs.get(p);
        for (Entry<UhcPlayer, UhcPlayer> entry : pairs.entrySet()) { if (entry.getValue() == p) return entry.getKey(); }
        return null;
    }

    @EventHandler
    public void onPartnerDeath(PlayerDeathEvent e) {
        UhcPlayer dead = getPlayersManager().getUhcPlayer(e.getEntity());
        UhcPlayer deadMate = getPartner(dead);

        UhcPlayer killer = null;
        UhcPlayer killerMate = null;
        if (e.getEntity().getKiller() != null && e.getEntity().getKiller() instanceof Player) {
            try {
                killer = getPlayersManager().getUhcPlayer(e.getEntity().getKiller());
                killerMate = getPartner(killer);
            } catch (Exception e1) {}
        }

        pairs.remove(dead);
        try {
            if (deadMate != null) {
                if (killer != null) deadMate.getPlayer().damage(100, killerMate.getPlayer());
                else deadMate.getPlayer().damage(100);
            }
        } catch (Exception e1) {

        }
    }

    // COPY INVENTORIES

    @EventHandler
    public void onPickUpItem(EntityPickupItemEvent e) {
        try {
            if (!(e.getEntity() instanceof Player)) return;
            Player player = (Player) e.getEntity();
            PlayerInventory otherInv = getPartner(getPlayersManager().getUhcPlayer(player)).getPlayer().getInventory();
            Item item = e.getItem();

            otherInv.addItem(item.getItemStack());
        } catch (Exception e1) {

        }
    }

    @EventHandler
    public void onThrowItem(PlayerDropItemEvent e) {
        try {
            Player player = e.getPlayer();
            PlayerInventory otherInv = getPartner(getPlayersManager().getUhcPlayer(player)).getPlayer().getInventory();

            int slot = player.getInventory().getHeldItemSlot();
            ItemStack items = otherInv.getItem(slot);
            if (items != null) {
                ItemMeta md = items.getItemMeta();
                ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
                newItems.setItemMeta(md);
                otherInv.setItem(slot, newItems);
            }
        } catch (Exception e1) {

        }

    }

    @EventHandler
    public void onBreakTool(PlayerItemBreakEvent e) {
        try {
            Player player = e.getPlayer();
            PlayerInventory otherInv = getPartner(getPlayersManager().getUhcPlayer(player)).getPlayer().getInventory();

            int slot = player.getInventory().getHeldItemSlot();
            ItemStack items = otherInv.getItem(slot);
            ItemMeta im = items.getItemMeta();
            ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
            newItems.setItemMeta(im);
            otherInv.setItem(slot, newItems);
        } catch (Exception e1) {

        }
    }

    @EventHandler
    public void onBreakTool(PlayerItemConsumeEvent e) {
        try {
            Player player = e.getPlayer();
            PlayerInventory otherInv = getPartner(getPlayersManager().getUhcPlayer(player)).getPlayer().getInventory();

            int slot = player.getInventory().getHeldItemSlot();
            ItemStack items = otherInv.getItem(slot);
            ItemMeta im = items.getItemMeta();
            ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
            newItems.setItemMeta(im);
            otherInv.setItem(slot, newItems);
        } catch (Exception e1) {

        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        try {
            Player player = e.getPlayer();
            PlayerInventory otherInv = getPartner(getPlayersManager().getUhcPlayer(player)).getPlayer().getInventory();

            int slot = player.getInventory().getHeldItemSlot();
            ItemStack items = otherInv.getItem(slot);
            ItemMeta im = items.getItemMeta();
            ItemStack newItems = new ItemStack(items.getType(), items.getAmount() - 1);
            newItems.setItemMeta(im);
            otherInv.setItem(slot, newItems);
        } catch (Exception e1) {

        }
    }

    @EventHandler
    public void onInventoryAction(InventoryClickEvent e) {
        try {
            final Player player = (Player) e.getWhoClicked();
            final PlayerInventory otherInv = getPartner(getPlayersManager().getUhcPlayer(player)).getPlayer()
                    .getInventory();

            new BukkitRunnable() {
                @Override
                public void run() {
                    final ItemStack[] contents = player.getInventory().getContents();

                    otherInv.setContents(contents);
                }

            }.runTaskLater(UhcCore.getPlugin(), 1);
        } catch (Exception e1) {

        }
    }

    @EventHandler
    public void onInventoryAction(InventoryDragEvent e) {
        try {
            final Player player = (Player) e.getWhoClicked();
            final PlayerInventory otherInv = getPartner(getPlayersManager().getUhcPlayer(player)).getPlayer()
                    .getInventory();

            new BukkitRunnable() {
                @Override
                public void run() {
                    final ItemStack[] contents = player.getInventory().getContents();

                    otherInv.setContents(contents);
                }

            }.runTaskLater(UhcCore.getPlugin(), 1);
        } catch (Exception e1) {

        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        try {
            final Player player = e.getPlayer();
            final PlayerInventory otherInv = getPartner(getPlayersManager().getUhcPlayer(player)).getPlayer()
                    .getInventory();

            new BukkitRunnable() {
                @Override
                public void run() {
                    final ItemStack[] contents = player.getInventory().getContents();

                    otherInv.setContents(contents);
                }

            }.runTaskLater(UhcCore.getPlugin(), 1);
        } catch (Exception e1) {

        }
    }
}
