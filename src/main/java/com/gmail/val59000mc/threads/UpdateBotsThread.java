package com.gmail.val59000mc.threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Scenario;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class UpdateBotsThread implements Runnable {

    private static Map<Material, Integer> attackRateMap;
    private static Map<Material, Double> damageMap;

    private static UpdateBotsThread task;

    private static GameManager gm;

    public UpdateBotsThread() {
        task = this;
        gm = GameManager.getGameManager();
    }

    static {
        attackRateMap = new HashMap<>();
        attackRateMap.put(Material.WOODEN_SWORD, 12);
        attackRateMap.put(Material.GOLDEN_SWORD, 12);
        attackRateMap.put(Material.STONE_SWORD, 12);
        attackRateMap.put(Material.IRON_SWORD, 12);
        attackRateMap.put(Material.DIAMOND_SWORD, 12);
        attackRateMap.put(Material.NETHERITE_SWORD, 12);

        attackRateMap.put(Material.TRIDENT, 18);

        attackRateMap.put(Material.WOODEN_SHOVEL, 20);
        attackRateMap.put(Material.GOLDEN_SHOVEL, 20);
        attackRateMap.put(Material.STONE_SHOVEL, 20);
        attackRateMap.put(Material.IRON_SHOVEL, 20);
        attackRateMap.put(Material.DIAMOND_SHOVEL, 20);
        attackRateMap.put(Material.NETHERITE_SHOVEL, 20);

        attackRateMap.put(Material.WOODEN_PICKAXE, 17);
        attackRateMap.put(Material.GOLDEN_PICKAXE, 17);
        attackRateMap.put(Material.STONE_PICKAXE, 17);
        attackRateMap.put(Material.IRON_PICKAXE, 17);
        attackRateMap.put(Material.DIAMOND_PICKAXE, 17);
        attackRateMap.put(Material.NETHERITE_PICKAXE, 17);

        attackRateMap.put(Material.WOODEN_AXE, 25);
        attackRateMap.put(Material.GOLDEN_AXE, 20);
        attackRateMap.put(Material.STONE_AXE, 25);
        attackRateMap.put(Material.IRON_AXE, 22);
        attackRateMap.put(Material.DIAMOND_AXE, 20);
        attackRateMap.put(Material.NETHERITE_AXE, 20);

        attackRateMap.put(Material.WOODEN_HOE, 20);
        attackRateMap.put(Material.GOLDEN_HOE, 20);
        attackRateMap.put(Material.STONE_HOE, 10);
        attackRateMap.put(Material.IRON_HOE, 7);

        damageMap = new HashMap<>();
        damageMap.put(Material.WOODEN_SWORD, 4.);
        damageMap.put(Material.GOLDEN_SWORD, 4.);
        damageMap.put(Material.STONE_SWORD, 5.);
        damageMap.put(Material.IRON_SWORD, 6.);
        damageMap.put(Material.DIAMOND_SWORD, 7.);
        damageMap.put(Material.NETHERITE_SWORD, 8.);

        damageMap.put(Material.TRIDENT, 9.);

        damageMap.put(Material.WOODEN_SHOVEL, 2.5);
        damageMap.put(Material.GOLDEN_SHOVEL, 2.5);
        damageMap.put(Material.STONE_SHOVEL, 3.5);
        damageMap.put(Material.IRON_SHOVEL, 4.5);
        damageMap.put(Material.DIAMOND_SHOVEL, 5.5);
        damageMap.put(Material.NETHERITE_SHOVEL, 6.5);

        damageMap.put(Material.WOODEN_PICKAXE, 2.);
        damageMap.put(Material.GOLDEN_PICKAXE, 2.);
        damageMap.put(Material.STONE_PICKAXE, 3.);
        damageMap.put(Material.IRON_PICKAXE, 4.);
        damageMap.put(Material.DIAMOND_PICKAXE, 5.);
        damageMap.put(Material.NETHERITE_PICKAXE, 6.);

        damageMap.put(Material.WOODEN_AXE, 7.);
        damageMap.put(Material.GOLDEN_AXE, 7.);
        damageMap.put(Material.STONE_AXE, 9.);
        damageMap.put(Material.IRON_AXE, 9.);
        damageMap.put(Material.DIAMOND_AXE, 9.);
        damageMap.put(Material.NETHERITE_AXE, 10.);
    }

    @Override
    public void run() {

        try {

            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (!npc.isSpawned()) continue;
                SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
                // Bukkit.getLogger().info(npc.getName());

                Location newLoc = sentinel.getLivingEntity().getLocation();

                if (sentinel.chasing == null || Math.abs(newLoc.getX()) >= gm.getWorldBorder().getCurrentSize() * 0.9
                        || Math.abs(newLoc.getZ()) >= gm.getWorldBorder().getCurrentSize() * 0.9) {
                    sentinel.chasing = null;
                    npc.getNavigator().cancelNavigation();
                    // route to new random location
                    Location testLoc = PlayersManager.findSafeLocationAround(
                            newLoc.clone().add(Math.random() * 30 - 15, 0, Math.random() * 30 - 15), 15);
                    Bukkit.getLogger().info(testLoc + "");
                    npc.getNavigator().setTarget(testLoc);

                    if (gm.getScenarioManager().isActivated(Scenario.WHATSMINE)) continue;

                    // copy a random real player's item
                    List<UhcPlayer> realPlayers = new ArrayList<>(gm.getPlayersManager().getOnlinePlayingPlayers());
                    ItemStack pickedItem = null;
                    ItemStack helmet = null;
                    ItemStack chest = null;
                    ItemStack pants = null;
                    ItemStack boots = null;
                    try {
                        double r = Math.random();
                        if (r < 0.1) pickedItem = realPlayers.get((int) (Math.random() * realPlayers.size()))
                                .getPlayer().getInventory().getItemInMainHand().clone();

                        else if (r < 0.2) helmet = realPlayers.get((int) (Math.random() * realPlayers.size()))
                                .getPlayer().getInventory().getHelmet().clone();
                        else if (r < 0.3) chest = realPlayers.get((int) (Math.random() * realPlayers.size()))
                                .getPlayer().getInventory().getChestplate().clone();
                        else if (r < 0.4) pants = realPlayers.get((int) (Math.random() * realPlayers.size()))
                                .getPlayer().getInventory().getLeggings().clone();
                        else if (r < 0.5) boots = realPlayers.get((int) (Math.random() * realPlayers.size()))
                                .getPlayer().getInventory().getBoots().clone();
                    } catch (Exception e) {
                        // uh
                    }

                    if (pickedItem != null) {
                        ItemStack currentHand = ((Player) sentinel.getLivingEntity()).getInventory()
                                .getItemInMainHand();
                        if (getDamage(currentHand) <= getDamage(pickedItem)) {
                            sentinel.getLivingEntity().getEquipment().setItemInMainHand(pickedItem);
                            ((Player) sentinel.getLivingEntity()).getInventory().addItem(currentHand);

                            Material m = pickedItem.getType();

                            if (attackRateMap.containsKey(m)) sentinel.attackRate = attackRateMap.get(m);
                            else sentinel.attackRate = 5;

                            sentinel.attackRate += (int) (Math.random() * 5);
                        } else {
                            ((Player) sentinel.getLivingEntity()).getInventory().addItem(pickedItem);
                        }
                    }

                    if (helmet != null) sentinel.getLivingEntity().getEquipment().setHelmet(helmet);
                    if (chest != null) sentinel.getLivingEntity().getEquipment().setChestplate(chest);
                    if (pants != null) sentinel.getLivingEntity().getEquipment().setLeggings(pants);
                    if (boots != null) sentinel.getLivingEntity().getEquipment().setBoots(boots);
                } else {
                    // forget about targets that move too far away, theres a chance this is already
                    // implemented but whatever
                    if (sentinel.chasing.getLocation().distanceSquared(
                            sentinel.getLivingEntity().getLocation()) > sentinel.range * sentinel.range)
                        sentinel.chasing = null;
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.toString());
        }

        if (gm.getGameState() == GameState.STARTING || gm.getGameState() == GameState.PLAYING
                || gm.getGameState() == GameState.DEATHMATCH) {
            Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20 * 5);
        }
    }

    private double getDamage(ItemStack i) {
        if (!damageMap.containsKey(i.getType())) return 1;
        return damageMap.get(i.getType());
    }

}
