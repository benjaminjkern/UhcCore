package com.gmail.val59000mc.threads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.game.UhcWorldBorder;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.scenariolisteners.SuperHeroesListener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Inventory;

public class UpdateBotsThread implements Runnable {

    private static Map<Material, Integer> attackRateMap;
    private static Map<Material, Double> damageMap;
    private static Map<Material, Double> armorMap;

    private static Map<NPC, Location> prevLoc;
    private static Map<NPC, Integer> locCount;

    private static UpdateBotsThread task;

    private static GameManager gm;

    public UpdateBotsThread() {
        task = this;
        gm = GameManager.getGameManager();
    }

    static {
        prevLoc = new HashMap<>();
        locCount = new HashMap<>();

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

        armorMap = new HashMap<>();
        armorMap.put(Material.TURTLE_HELMET, 2.);

        armorMap.put(Material.LEATHER_HELMET, 1.);
        armorMap.put(Material.LEATHER_CHESTPLATE, 3.);
        armorMap.put(Material.LEATHER_LEGGINGS, 2.);
        armorMap.put(Material.LEATHER_BOOTS, 1.);

        armorMap.put(Material.CHAINMAIL_HELMET, 2.);
        armorMap.put(Material.CHAINMAIL_CHESTPLATE, 5.);
        armorMap.put(Material.CHAINMAIL_LEGGINGS, 4.);
        armorMap.put(Material.CHAINMAIL_BOOTS, 1.);

        armorMap.put(Material.IRON_HELMET, 2.);
        armorMap.put(Material.IRON_CHESTPLATE, 6.);
        armorMap.put(Material.IRON_LEGGINGS, 5.);
        armorMap.put(Material.IRON_BOOTS, 2.);

        armorMap.put(Material.GOLDEN_HELMET, 2.);
        armorMap.put(Material.GOLDEN_CHESTPLATE, 5.);
        armorMap.put(Material.GOLDEN_LEGGINGS, 3.);
        armorMap.put(Material.GOLDEN_BOOTS, 1.);

        armorMap.put(Material.DIAMOND_HELMET, 3.);
        armorMap.put(Material.DIAMOND_CHESTPLATE, 8.);
        armorMap.put(Material.DIAMOND_LEGGINGS, 6.);
        armorMap.put(Material.DIAMOND_BOOTS, 3.);

        armorMap.put(Material.NETHERITE_HELMET, 3.);
        armorMap.put(Material.NETHERITE_CHESTPLATE, 8.);
        armorMap.put(Material.NETHERITE_LEGGINGS, 6.);
        armorMap.put(Material.NETHERITE_BOOTS, 3.);
    }

    @Override
    public void run() {

        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            try {
                if (!npc.isSpawned()) {
                    Location loc = PlayersManager.verifySafe(npc.getStoredLocation());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), () -> {
                        if (loc != null && loc.getX() * loc.getX() + loc.getZ() * loc.getZ() > 2) npc.spawn(loc);
                        else npc.spawn(PlayersManager.findRandomSafeLocation(
                                Bukkit.getWorld(GameManager.getGameManager().getConfiguration().getOverworldUuid())));
                    });
                    continue;
                }

                if (!Float.isFinite(npc.getStoredLocation().getYaw())
                        || !Float.isFinite(npc.getStoredLocation().getPitch())
                        || (locCount.containsKey(npc) && locCount.get(npc) > 3)) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), () -> {
                        npc.despawn();
                        Location loc = PlayersManager.verifySafe(npc.getStoredLocation());
                        if (loc != null && loc.getX() * loc.getX() + loc.getZ() * loc.getZ() > 2) npc.spawn(loc);
                        else npc.spawn(PlayersManager.findRandomSafeLocation(
                                Bukkit.getWorld(GameManager.getGameManager().getConfiguration().getOverworldUuid())));
                    });
                    locCount.put(npc, 0);
                    continue;
                }

                UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayersManager()
                        .getUhcPlayer((Player) npc.getEntity());
                GameManager.getGameManager().getScoreboardManager().updatePlayerTab(uhcPlayer);

                SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);

                Location loc = sentinel.getLivingEntity().getLocation().clone();

                if (prevLoc.containsKey(npc) && loc.distanceSquared(prevLoc.get(npc)) < 3)
                    locCount.put(npc, locCount.get(npc) + 1);
                else locCount.put(npc, 0);

                if (sentinel.chasing == null || !GameManager.getGameManager().getWorldBorder().isWithinBorder(loc, 0.9)
                        || (prevLoc.containsKey(npc) && loc.distanceSquared(prevLoc.get(npc)) < 3)
                        || ((sentinel.chasing.isInvisible()
                                || sentinel.chasing.hasPotionEffect(PotionEffectType.INVISIBILITY))
                                && sentinel.chasing.getEquipment().getItemInMainHand() == null
                                && sentinel.chasing.getEquipment().getItemInOffHand() == null)) {
                    sentinel.chasing = null;

                    // route to new random location
                    Location newLoc = PlayersManager.newRandomLocation(sentinel.getLivingEntity().getWorld());
                    Vector diff = newLoc.toVector().subtract(loc.toVector()).setY(0);
                    if (diff.lengthSquared() > 15 * 15) { newLoc = loc.clone().add(diff.normalize().multiply(15)); }

                    final Location fLoc = PlayersManager.findSafeLocationAround(newLoc, 15);

                    npc.getNavigator().cancelNavigation();
                    if (fLoc != null) npc.getNavigator().setTarget(fLoc);
                    else npc.getNavigator().setTarget(newLoc);

                    // copy a random real player's item
                    List<UhcPlayer> realPlayers = gm.getPlayersManager().getOnlinePlayingPlayers().stream()
                            .filter(player -> {
                                try {
                                    return !player.getPlayer().getInventory().isEmpty();
                                } catch (UhcPlayerNotOnlineException e) {
                                    // this SHOULD BE ENTIRELY REDUNDANT BUT WHATEVER
                                    return false;
                                }
                            }).collect(Collectors.toList());

                    if (!realPlayers.isEmpty() && Math.random() < 0.5
                            && !gm.getScenarioManager().isActivated(Scenario.WHATSMINE)) {
                        getAndAddNewItem(realPlayers, sentinel);
                    }
                }

                if (!gm.getScenarioManager().isActivated(Scenario.WHATSMINE)) dealWithInventory(sentinel, uhcPlayer);

                prevLoc.put(npc, loc);
            } catch (Exception e) {
                Bukkit.getLogger().warning(npc.getFullName());
                for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().warning(s.toString());
            }
        }

        if (gm.getGameState() == GameState.PLAYING || gm.getGameState() == GameState.DEATHMATCH) {
            Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20 * 5);
        }
    }

    private int countEnchants(ItemStack i) {
        if (i == null || !i.hasItemMeta()) return 0;
        return i.getItemMeta().getEnchants().values().stream().reduce(0, (p, c) -> p + c);
    }

    private double getDamage(ItemStack i) {
        if (i == null || !damageMap.containsKey(i.getType())) return 1;
        return damageMap.get(i.getType());
    }

    private double getArmor(ItemStack i) {
        if (i == null || !armorMap.containsKey(i.getType())) return 0;
        return armorMap.get(i.getType());
    }

    private boolean isHelmet(ItemStack i) {
        return i != null && new HashSet<Material>(Arrays.asList(new Material[] { Material.LEATHER_HELMET,
                Material.IRON_HELMET, Material.TURTLE_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET,
                Material.CHAINMAIL_HELMET, Material.NETHERITE_HELMET })).contains(i.getType());
    }

    private boolean isChest(ItemStack i) {
        return i != null && new HashSet<Material>(Arrays.asList(
                new Material[] { Material.LEATHER_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
                        Material.DIAMOND_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.NETHERITE_CHESTPLATE }))
                                .contains(i.getType());
    }

    private boolean isLegs(ItemStack i) {
        return i != null && new HashSet<Material>(Arrays
                .asList(new Material[] { Material.LEATHER_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS,
                        Material.DIAMOND_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.NETHERITE_LEGGINGS }))
                                .contains(i.getType());
    }

    private boolean isBoots(ItemStack i) {
        return i != null && new HashSet<Material>(
                Arrays.asList(new Material[] { Material.LEATHER_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS,
                        Material.DIAMOND_BOOTS, Material.CHAINMAIL_BOOTS, Material.NETHERITE_BOOTS }))
                                .contains(i.getType());
    }

    private void getAndAddNewItem(List<UhcPlayer> realPlayers, SentinelTrait sentinel)
            throws UhcPlayerNotOnlineException {
        PlayerInventory playerInv = realPlayers.get((int) (Math.random() * realPlayers.size())).getPlayer()
                .getInventory();
        List<ItemStack> realInv = Arrays.asList(playerInv.getStorageContents()).stream().filter(item -> item != null)
                .collect(Collectors.toList());
        List<ItemStack> armor = Arrays.asList(playerInv.getArmorContents()).stream().filter(item -> item != null)
                .collect(Collectors.toList());

        realInv.addAll(armor);
        realInv.add(playerInv.getItemInOffHand());

        ItemStack pickedItem = realInv.get((int) (Math.random() * realInv.size())).clone();

        if (pickedItem.getType() == Material.POTION || pickedItem.getType() == Material.ENDER_PEARL) return;

        if (!((Player) sentinel.getLivingEntity()).getInventory().addItem(pickedItem).isEmpty())
            ((Player) sentinel.getLivingEntity()).getInventory().setItem((int) (Math.random() * 36), pickedItem);
    }

    private void dealWithInventory(SentinelTrait sentinel, UhcPlayer uhcPlayer) {

        PlayerInventory sentInv = ((Player) sentinel.getLivingEntity()).getInventory();
        ItemStack currentOffHand = sentInv.getItemInOffHand();
        ItemStack currentHand = sentInv.getItemInMainHand();

        if ((currentOffHand == null || currentOffHand.getType() == Material.AIR)
                && (sentInv.contains(Material.TOTEM_OF_UNDYING) || sentInv.contains(Material.SHIELD))) {
            int slot = Math.max(sentInv.first(Material.TOTEM_OF_UNDYING), sentInv.first(Material.SHIELD));
            if (slot >= 0) {
                ItemStack item = sentInv.getItem(slot);
                sentInv.setItemInOffHand(item.clone());
                sentInv.setItem(slot, null);
            }
        }

        for (int slot = 0; slot <= 40; slot++) {
            ItemStack pickedItem = sentInv.getItem(slot);
            if (getDamage(pickedItem) > getDamage(currentHand) || (getDamage(pickedItem) == getDamage(currentHand)
                    && countEnchants(pickedItem) > countEnchants(currentHand))) {
                sentInv.setItemInMainHand(pickedItem.clone());
                sentInv.setItem(slot, currentHand);
                continue;
            }

            if (!gm.getScenarioManager().isActivated(Scenario.SUPERHEROES) || SuperHeroesListener
                    .getPower(uhcPlayer).powerType != SuperHeroesListener.SuperPower.SuperPowerType.GHOST) {

                if (isHelmet(pickedItem)) {
                    ItemStack currentArmor = sentinel.getLivingEntity().getEquipment().getHelmet();
                    if (getArmor(pickedItem) > getArmor(currentArmor) || (getArmor(pickedItem) == getArmor(currentArmor)
                            && countEnchants(pickedItem) > countEnchants(currentArmor))) {
                        pickedItem.removeEnchantment(Enchantment.THORNS);
                        sentinel.getLivingEntity().getEquipment().setHelmet(pickedItem.clone());
                        sentInv.setItem(slot, currentArmor);
                    }
                    continue;
                }
                if (isChest(pickedItem)) {
                    ItemStack currentArmor = sentinel.getLivingEntity().getEquipment().getChestplate();
                    if (!gm.getScenarioManager().isActivated(Scenario.FLYHIGH)
                            && getArmor(pickedItem) > getArmor(currentArmor)
                            || (getArmor(pickedItem) == getArmor(currentArmor)
                                    && countEnchants(pickedItem) > countEnchants(currentArmor))) {
                        pickedItem.removeEnchantment(Enchantment.THORNS);
                        sentinel.getLivingEntity().getEquipment().setChestplate(pickedItem.clone());
                        sentInv.setItem(slot, currentArmor);
                    }
                    continue;
                }
                if (isLegs(pickedItem)) {
                    ItemStack currentArmor = sentinel.getLivingEntity().getEquipment().getLeggings();
                    if (getArmor(pickedItem) > getArmor(currentArmor) || (getArmor(pickedItem) == getArmor(currentArmor)
                            && countEnchants(pickedItem) > countEnchants(currentArmor))) {
                        pickedItem.removeEnchantment(Enchantment.THORNS);
                        sentinel.getLivingEntity().getEquipment().setLeggings(pickedItem.clone());
                        sentInv.setItem(slot, currentArmor);
                    }
                    continue;
                }
                if (isBoots(pickedItem)) {
                    ItemStack currentArmor = sentinel.getLivingEntity().getEquipment().getBoots();
                    if (getArmor(pickedItem) > getArmor(currentArmor) || (getArmor(pickedItem) == getArmor(currentArmor)
                            && countEnchants(pickedItem) > countEnchants(currentArmor))) {
                        pickedItem.removeEnchantment(Enchantment.THORNS);
                        sentinel.getLivingEntity().getEquipment().setBoots(pickedItem.clone());
                        sentInv.setItem(slot, currentArmor);
                    }
                    continue;
                }
            }
        }
        if (Math.random() < 0.2) {
            if (sentInv.contains(Material.GOLDEN_APPLE)) {
                // Bukkit.getLogger().info(npc.getFullName() + " is trynna eat an apple");
                int slot = sentInv.first(Material.GOLDEN_APPLE);
                ItemStack item = sentInv.getItem(slot);
                item.setAmount(item.getAmount() - 1);

                sentinel.getLivingEntity().getWorld().playSound(sentinel.getLivingEntity().getLocation(),
                        Sound.ENTITY_GENERIC_EAT, 1, 0);
                sentinel.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 120, 0));
                sentinel.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 1));
            }

            if (sentInv.contains(Material.ENCHANTED_GOLDEN_APPLE)) {
                // Bukkit.getLogger().info(npc.getFullName() + " is trynna eat an GAPPLE");
                int slot = sentInv.first(Material.ENCHANTED_GOLDEN_APPLE);
                ItemStack item = sentInv.getItem(slot);
                item.setAmount(item.getAmount() - 1);

                sentinel.getLivingEntity().getWorld().playSound(sentinel.getLivingEntity().getLocation(),
                        Sound.ENTITY_GENERIC_EAT, 1, 0);
                sentinel.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 120, 3));
                sentinel.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 30, 1));
                sentinel.getLivingEntity()
                        .addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 5 * 60, 0));
                sentinel.getLivingEntity()
                        .addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 5 * 60, 0));
            }
        }

        Material m = currentHand.getType();

        if (attackRateMap.containsKey(m)) sentinel.attackRate = attackRateMap.get(m);
        else sentinel.attackRate = 5;

        sentinel.attackRate += (int) (Math.random() * 5);
    }

}
