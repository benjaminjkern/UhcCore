package com.gmail.val59000mc.threads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.SpawnLocations;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.scenariolisteners.BleedingSweetsListener;
import com.gmail.val59000mc.scenarios.scenariolisteners.FlowerPowerListener;
import com.gmail.val59000mc.scenarios.scenariolisteners.HoesMadListener;
import com.gmail.val59000mc.scenarios.scenariolisteners.KingOfTheHillListener;
import com.gmail.val59000mc.scenarios.scenariolisteners.LilCheatListener;
import com.gmail.val59000mc.scenarios.scenariolisteners.RandomizedDropsListener;
import com.gmail.val59000mc.scenarios.scenariolisteners.SuperHeroesListener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class UpdateBotsThread implements Runnable {

    private static Map<Material, Integer> attackRateMap;
    private static Map<Material, Double> damageMap;
    private static Map<Material, Double> armorMap;
    private static Map<Material, List<Material>> craftMap;
    private static Map<Material, List<Material>> harvestMap;
    private static List<Material> woods;

    private static UpdateBotsThread task;

    private static GameManager gm;
    private static Map<NPC, Location> spawnLoc;

    public UpdateBotsThread() {
        task = this;
        gm = GameManager.getGameManager();
        spawnLoc = new HashMap<>();
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

        List<String> toolNames = Arrays.asList("PICKAXE", "AXE", "SHOVEL", "HOE", "SWORD");
        List<String> armorNames = Arrays.asList("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");
        List<String> woodNames = Arrays.asList("ACACIA", "OAK", "DARK_OAK", "BIRCH", "JUNGLE", "SPRUCE");
        woods = new ArrayList<>();
        for (String woodName : woodNames) {
            woods.add(Material.getMaterial(woodName + "_LOG"));
            woods.add(Material.getMaterial(woodName + "_WOOD"));
            woods.add(Material.getMaterial(woodName + "_PLANKS"));
        }

        craftMap = new HashMap<>();
        List<Material> woodTools = new ArrayList<>();
        for (String toolName : toolNames) { woodTools.add(Material.getMaterial("WOODEN_" + toolName)); }
        for (Material wood : woods) { craftMap.put(wood, woodTools); }
        for (Material woodTool : woodTools) craftMap.put(woodTool, woodTools);

        List<Material> stoneTools = new ArrayList<>();
        for (String toolName : toolNames) { stoneTools.add(Material.getMaterial("STONE_" + toolName)); }
        craftMap.put(Material.COBBLESTONE, stoneTools);
        for (Material stoneTool : stoneTools) craftMap.put(stoneTool, stoneTools);

        List<Material> ironTools = new ArrayList<>(Arrays.asList(Material.SHIELD, Material.FLINT_AND_STEEL,
                Material.BUCKET, Material.COMPASS, Material.SHEARS, Material.IRON_HORSE_ARMOR, Material.DAMAGED_ANVIL));
        for (String toolName : toolNames) { ironTools.add(Material.getMaterial("IRON_" + toolName)); }
        for (String armorName : armorNames) { ironTools.add(Material.getMaterial("IRON_" + armorName)); }
        craftMap.put(Material.IRON_INGOT, ironTools);
        for (Material ironTool : ironTools) craftMap.put(ironTool, ironTools);

        List<Material> goldTools = new ArrayList<>(
                Arrays.asList(Material.CLOCK, Material.GOLDEN_APPLE, Material.GOLDEN_HORSE_ARMOR));
        for (String toolName : toolNames) { goldTools.add(Material.getMaterial("GOLDEN_" + toolName)); }
        for (String armorName : armorNames) { goldTools.add(Material.getMaterial("GOLDEN_" + armorName)); }
        craftMap.put(Material.GOLD_INGOT, goldTools);
        for (Material goldTool : goldTools) craftMap.put(goldTool, goldTools);

        List<Material> diamondTools = new ArrayList<>(
                Arrays.asList(Material.DIAMOND_HORSE_ARMOR, Material.ENCHANTING_TABLE));
        for (String toolName : toolNames) { diamondTools.add(Material.getMaterial("DIAMOND_" + toolName)); }
        for (String armorName : armorNames) { diamondTools.add(Material.getMaterial("DIAMOND_" + armorName)); }
        craftMap.put(Material.DIAMOND, diamondTools);
        for (Material diamondTool : diamondTools) craftMap.put(diamondTool, diamondTools);

        List<Material> netheriteTools = new ArrayList<>();
        for (String toolName : toolNames) { netheriteTools.add(Material.getMaterial("NETHERITE_" + toolName)); }
        for (String armorName : armorNames) { netheriteTools.add(Material.getMaterial("NETHERITE_" + armorName)); }
        craftMap.put(Material.NETHERITE_INGOT, netheriteTools);
        for (Material netheriteTool : netheriteTools) craftMap.put(netheriteTool, netheriteTools);

        List<Material> leatherTools = new ArrayList<>(Arrays.asList(Material.SADDLE, Material.LEATHER_HORSE_ARMOR));
        for (String armorName : armorNames) { leatherTools.add(Material.getMaterial("LEATHER_" + armorName)); }
        craftMap.put(Material.LEATHER, leatherTools);
        for (Material leatherTool : netheriteTools) craftMap.put(leatherTool, leatherTools);

        craftMap.put(Material.IRON_ORE, Arrays.asList(Material.IRON_INGOT));
        craftMap.put(Material.IRON_NUGGET, Arrays.asList(Material.IRON_INGOT));
        craftMap.put(Material.IRON_BLOCK, Arrays.asList(Material.IRON_INGOT));
        craftMap.put(Material.GOLD_ORE, Arrays.asList(Material.GOLD_INGOT));
        craftMap.put(Material.GOLD_NUGGET, Arrays.asList(Material.GOLD_INGOT));
        craftMap.put(Material.GOLD_BLOCK, Arrays.asList(Material.GOLD_INGOT));
        craftMap.put(Material.DIAMOND_BLOCK, Arrays.asList(Material.DIAMOND));
        craftMap.put(Material.NETHERITE_BLOCK, Arrays.asList(Material.NETHERITE_INGOT));
        craftMap.put(Material.ANCIENT_DEBRIS, Arrays.asList(Material.NETHERITE_SCRAP));
        craftMap.put(Material.NETHERITE_SCRAP, Arrays.asList(Material.NETHERITE_INGOT));
        craftMap.put(Material.STRING, Arrays.asList(Material.BOW));
        craftMap.put(Material.FEATHER, Arrays.asList(Material.ARROW));
        craftMap.put(Material.PORKCHOP, Arrays.asList(Material.COOKED_PORKCHOP));
        craftMap.put(Material.BEEF, Arrays.asList(Material.COOKED_BEEF));
        craftMap.put(Material.CHICKEN, Arrays.asList(Material.COOKED_CHICKEN));
        craftMap.put(Material.RABBIT, Arrays.asList(Material.COOKED_RABBIT));
        craftMap.put(Material.MUTTON, Arrays.asList(Material.COOKED_MUTTON));
        craftMap.put(Material.COD, Arrays.asList(Material.COOKED_COD));
        craftMap.put(Material.SALMON, Arrays.asList(Material.COOKED_SALMON));
        craftMap.put(Material.BUCKET, Arrays.asList(Material.WATER_BUCKET, Material.LAVA_BUCKET, Material.MILK_BUCKET));
        craftMap.put(Material.GUNPOWDER, Arrays.asList(Material.TNT));
        craftMap.put(Material.WHEAT_SEEDS, Arrays.asList(Material.WHEAT));
        craftMap.put(Material.WHEAT, Arrays.asList(Material.BREAD));
        craftMap.put(Material.MELON_SEEDS, Arrays.asList(Material.MELON_SLICE));
        craftMap.put(Material.PUMPKIN_SEEDS, Arrays.asList(Material.PUMPKIN));
        craftMap.put(Material.PUMPKIN, Arrays.asList(Material.PUMPKIN_PIE));
        craftMap.put(Material.BEETROOT_SEEDS, Arrays.asList(Material.BEETROOT));
        craftMap.put(Material.POTATO, Arrays.asList(Material.POISONOUS_POTATO, Material.BAKED_POTATO));

        harvestMap = new HashMap<>();

        harvestMap.put(Material.BOW, Arrays.asList(Material.ARROW));

        harvestMap.put(Material.WOODEN_PICKAXE, Arrays.asList(Material.COBBLESTONE, Material.COAL));
        harvestMap.put(Material.STONE_PICKAXE,
                Arrays.asList(Material.COBBLESTONE, Material.COAL, Material.IRON_ORE, Material.LAPIS_LAZULI));
        harvestMap.put(Material.IRON_PICKAXE, Arrays.asList(Material.COBBLESTONE, Material.COAL, Material.IRON_ORE,
                Material.LAPIS_LAZULI, Material.GOLD_ORE, Material.EMERALD, Material.DIAMOND, Material.REDSTONE));
        harvestMap.put(Material.GOLDEN_PICKAXE, Arrays.asList(Material.COBBLESTONE, Material.COAL));
        harvestMap.put(Material.DIAMOND_PICKAXE,
                Arrays.asList(Material.COBBLESTONE, Material.COAL, Material.IRON_ORE, Material.LAPIS_LAZULI,
                        Material.GOLD_ORE, Material.EMERALD, Material.DIAMOND, Material.REDSTONE, Material.OBSIDIAN,
                        Material.QUARTZ, Material.ANCIENT_DEBRIS));
        harvestMap.put(Material.NETHERITE_PICKAXE,
                Arrays.asList(Material.COBBLESTONE, Material.COAL, Material.IRON_ORE, Material.LAPIS_LAZULI,
                        Material.GOLD_ORE, Material.EMERALD, Material.DIAMOND, Material.REDSTONE, Material.OBSIDIAN,
                        Material.QUARTZ, Material.ANCIENT_DEBRIS));

        harvestMap.put(Material.WOODEN_SWORD, Arrays.asList(Material.LEATHER, Material.FEATHER));
        harvestMap.put(Material.STONE_SWORD, Arrays.asList(Material.LEATHER, Material.FEATHER, Material.PORKCHOP,
                Material.BEEF, Material.CHICKEN, Material.RABBIT, Material.MUTTON));
        harvestMap.put(Material.GOLDEN_SWORD,
                Arrays.asList(Material.LEATHER, Material.FEATHER, Material.PORKCHOP, Material.BEEF, Material.CHICKEN,
                        Material.RABBIT, Material.MUTTON, Material.COD, Material.SALMON, Material.TROPICAL_FISH,
                        Material.PUFFERFISH));
        harvestMap.put(Material.IRON_SWORD,
                Arrays.asList(Material.LEATHER, Material.FEATHER, Material.PORKCHOP, Material.BEEF, Material.CHICKEN,
                        Material.RABBIT, Material.MUTTON, Material.SPIDER_EYE, Material.ROTTEN_FLESH, Material.STRING,
                        Material.BONE));
        harvestMap.put(Material.DIAMOND_SWORD,
                Arrays.asList(Material.LEATHER, Material.FEATHER, Material.PORKCHOP, Material.BEEF, Material.CHICKEN,
                        Material.RABBIT, Material.MUTTON, Material.SPIDER_EYE, Material.ROTTEN_FLESH, Material.STRING,
                        Material.BONE, Material.GUNPOWDER, Material.BLAZE_ROD, Material.SHULKER_SHELL,
                        Material.ENDER_PEARL));
        harvestMap.put(Material.NETHERITE_SWORD,
                Arrays.asList(Material.LEATHER, Material.FEATHER, Material.PORKCHOP, Material.BEEF, Material.CHICKEN,
                        Material.RABBIT, Material.MUTTON, Material.SPIDER_EYE, Material.ROTTEN_FLESH, Material.STRING,
                        Material.BONE, Material.GUNPOWDER, Material.BLAZE_ROD, Material.SHULKER_SHELL,
                        Material.ENDER_PEARL, Material.TOTEM_OF_UNDYING, Material.TRIDENT,
                        Material.WITHER_SKELETON_SKULL));

        harvestMap.put(Material.WOODEN_HOE, Arrays.asList(Material.WHEAT_SEEDS));
        harvestMap.put(Material.STONE_HOE, Arrays.asList(Material.WHEAT_SEEDS, Material.SWEET_BERRIES,
                Material.MELON_SEEDS, Material.BEETROOT_SEEDS, Material.PUMPKIN_SEEDS));
        harvestMap.put(Material.GOLDEN_HOE,
                Arrays.asList(Material.WHEAT_SEEDS, Material.SWEET_BERRIES, Material.MELON_SEEDS,
                        Material.BEETROOT_SEEDS, Material.PUMPKIN_SEEDS, Material.CARROT, Material.POTATO));
        harvestMap.put(Material.IRON_HOE, Arrays.asList(Material.WHEAT_SEEDS, Material.SWEET_BERRIES,
                Material.MELON_SEEDS, Material.BEETROOT_SEEDS, Material.PUMPKIN_SEEDS, Material.APPLE));
        harvestMap.put(Material.DIAMOND_HOE,
                Arrays.asList(Material.WHEAT_SEEDS, Material.SWEET_BERRIES, Material.MELON_SEEDS,
                        Material.BEETROOT_SEEDS, Material.PUMPKIN_SEEDS, Material.APPLE, Material.GOLDEN_APPLE));
        harvestMap.put(Material.NETHERITE_HOE,
                Arrays.asList(Material.WHEAT_SEEDS, Material.SWEET_BERRIES, Material.MELON_SEEDS,
                        Material.BEETROOT_SEEDS, Material.PUMPKIN_SEEDS, Material.APPLE, Material.GOLDEN_APPLE,
                        Material.ENCHANTED_GOLDEN_APPLE));

    }

    @Override
    public void run() {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            try {
                if (!npc.isSpawned() || !npc.getEntity().isValid()) {
                    Location loc2 = SpawnLocations.verifySafe(npc.getStoredLocation());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), () -> {
                        if (loc2 != null && loc2.getX() * loc2.getX() + loc2.getZ() * loc2.getZ() > 2) npc.spawn(loc2);
                        else npc.spawn(SpawnLocations.findRandomSafeLocation(
                                Bukkit.getWorld(GameManager.getGameManager().getConfiguration().getOverworldUuid())));
                    });
                    continue;
                }
                Player player = (Player) npc.getEntity();
                UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayersManager().getUhcPlayer(player);
                GameManager.getGameManager().getScoreboardManager().updatePlayerTab(uhcPlayer);
                SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
                Location loc = sentinel.getLivingEntity().getLocation();

                boolean justSet = false;

                if (!spawnLoc.containsKey(npc)) {
                    spawnLoc.put(npc, loc);
                    justSet = true;
                }

                // if not chasing anyone, or too close to a wall, or if chasing is too far away,
                // reroute course
                if (sentinel.chasing == null || !gm.getWorldBorder().isWithinBorder(loc, 0.9)
                        || sentinel.chasing.getLocation().distanceSquared(player.getLocation()) > sentinel.range
                        || isInvisible(sentinel.chasing)
                        || (!justSet && loc.distanceSquared(spawnLoc.get(npc)) < 0.5)) {
                    reroute(npc);
                }

                if (!gm.getScenarioManager().isActivated(Scenario.WHATSMINE)) {
                    // copy a random player's item
                    List<UhcPlayer> otherPlayers = gm.getPlayersManager().getAllPlayingPlayers().stream()
                            .filter(u -> u != uhcPlayer).collect(Collectors.toList());

                    if (Math.random() < 0.5) getAndAddNewItem(otherPlayers, sentinel);
                    dealWithInventory(sentinel, uhcPlayer);
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning(npc.getFullName());
                for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().warning(s.toString());
            }
        }

        if (gm.getGameState() == GameState.PLAYING || gm.getGameState() == GameState.DEATHMATCH) {
            Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20 * 5);
        }
    }

    public static void reroute(NPC npc) {
        SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
        Location loc = sentinel.getLivingEntity().getLocation();

        sentinel.chasing = null;

        // route to new random location
        Location newLoc = SpawnLocations.newRandomLocation(sentinel.getLivingEntity().getWorld(), true);
        Vector diff = newLoc.toVector().subtract(loc.toVector()).setY(0);
        if (diff.lengthSquared() > 15 * 15) { newLoc = loc.clone().add(diff.normalize().multiply(15)); }

        final Location fLoc = SpawnLocations.findSafeLocationAround(newLoc, 15);

        npc.getNavigator().cancelNavigation();
        if (fLoc != null) npc.getNavigator().setTarget(fLoc);
        else npc.getNavigator().setTarget(newLoc);
    }

    public static boolean isInvisible(LivingEntity chasing) {
        EntityEquipment equip = chasing.getEquipment();
        return (chasing.isInvisible() || chasing.hasPotionEffect(PotionEffectType.INVISIBILITY))
                && Arrays.asList(equip.getArmorContents()).stream().allMatch(item -> itemEmpty(item))
                && itemEmpty(equip.getItemInMainHand()) && itemEmpty(equip.getItemInOffHand());
    }

    private static boolean itemEmpty(ItemStack i) { return i == null || i.getType() == Material.AIR; }

    private int countEnchants(ItemStack i) {
        if (i == null || !i.hasItemMeta()) return 0;
        return i.getItemMeta().getEnchants().values().stream().reduce(0, (p, c) -> p + c);
    }

    private double getDamage(ItemStack i) {
        if (i == null) return 1;
        if (gm.getScenarioManager().isActivated(Scenario.HOESMAD) && HoesMadListener.isHoe(i))
            return HoesMadListener.getDamage(i);
        if (!damageMap.containsKey(i.getType())) return 1;
        return damageMap.get(i.getType());
    }

    private double getArmor(ItemStack i) {
        if (i == null || !armorMap.containsKey(i.getType())) return 0;
        return armorMap.get(i.getType());
    }

    private boolean isHelmet(ItemStack i) { return i != null && i.getType().name().contains("HELMET"); }

    private boolean isChest(ItemStack i) { return i != null && i.getType().name().contains("CHESTPLATE"); }

    private boolean isLegs(ItemStack i) { return i != null && i.getType().name().contains("LEGGINGS"); }

    private boolean isBoots(ItemStack i) { return i != null && i.getType().name().contains("BOOTS"); }

    private void getAndAddNewItem(List<UhcPlayer> players, SentinelTrait sentinel) throws UhcPlayerNotOnlineException {
        PlayerInventory playerInv;
        try {
            playerInv = players.get((int) (Math.random() * players.size())).getPlayer().getInventory();
        } catch (Exception e) {
            return;
        }
        List<ItemStack> realInv = new ArrayList<>();

        for (int slot = 0; slot <= 40; slot++) {
            ItemStack item = playerInv.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;
            if (realInv.stream().anyMatch(realItem -> realItem.getType() == item.getType())) continue;
            ItemStack newItem = item.clone();
            newItem.setAmount(1);
            realInv.add(newItem);
        }

        ItemStack pickedItem;

        PlayerInventory botInventory = ((Player) sentinel.getLivingEntity()).getInventory();

        if (gm.getScenarioManager().isActivated(Scenario.LILCHEAT)
                && gm.getElapsedTime() < LilCheatListener.time * 1000) {
            for (int i = 0; i < Math.random() * 5; i++) {
                Material m = null;
                while (m == null || !m.isItem()) {
                    m = Material.values()[(int) (Math.random() * Material.values().length)];
                }
                pickedItem = new ItemStack(m, (int) (Math.random() * m.getMaxStackSize()));

                addToInventory(botInventory, pickedItem, true);
            }
            return;
        } else if (gm.getScenarioManager().isActivated(Scenario.FLOWERPOWER) && Math.random() > 0.5) {
            pickedItem = FlowerPowerListener.flowerDrop();
            addToInventory(botInventory, pickedItem, true);
        } else if (realInv.isEmpty()) {
            pickedItem = new ItemStack(woods.get((int) (Math.random() * woods.size())));
        } else {
            pickedItem = realInv.stream().reduce(null, (best, current) -> {
                if (botInventory.contains(current)) return best;
                if (compare(current, best)) return current;
                return best;
            });
            if (pickedItem == null) pickedItem = realInv.get((int) (Math.random() * realInv.size())).clone();
        }

        if (pickedItem.getType() == Material.POTION || pickedItem.getType() == Material.ENDER_PEARL) return;

        addToInventory(botInventory, pickedItem, true);
    }

    private void addToInventory(PlayerInventory botInventory, ItemStack pickedItem, boolean force) {
        if (pickedItem == null) return;
        if (!botInventory.addItem(pickedItem).isEmpty() && force) {
            int slot = (int) (Math.random() * 35 + 1);
            ItemStack oldItem = botInventory.getItem(slot);
            if (compare(pickedItem, oldItem)) botInventory.setItem(slot, pickedItem);
        }
    }

    private boolean compare(ItemStack pickedItem, ItemStack oldItem) {
        return oldItem == null
                || (getDamage(pickedItem) == getDamage(oldItem) && getArmor(pickedItem) > getArmor(oldItem))
                || (getDamage(pickedItem) > getDamage(oldItem) && getArmor(pickedItem) == getArmor(oldItem))
                || isDownStream(oldItem.getType(), pickedItem.getType());
    }

    private boolean isDownStream(Material from, Material to) { return isDownStream(from, to, new HashSet<>()); }

    private boolean isDownStream(Material from, Material to, Set<Material> seen) {
        if (from == to) return true;
        if (seen.contains(from) || !craftMap.containsKey(from)) return false;
        seen.add(from);
        for (Material child : craftMap.get(from)) { if (isDownStream(child, to, seen)) return true; }
        return false;
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
            if (pickedItem == null) continue;

            if (Math.random() < 0.1) {
                if (pickedItem.getType() == Material.ENCHANTING_TABLE) {
                    pickedItem = null;
                    int i = (int) (Math.random() * 41);
                    for (int tries = 0; tries <= 40; tries++) {
                        pickedItem = sentInv.getItem(i);
                        i = (i + 1) % 41;
                        if (pickedItem == null || pickedItem.getType() == Material.AIR) continue;
                        if (BleedingSweetsListener.randomEnchant(pickedItem, true)) break;
                    }
                    continue;
                }
                if (slot <= 35 || slot == 40) {
                    if (gm.getScenarioManager().isActivated(Scenario.RANDOMIZEDDROPS)) {
                        pickedItem = RandomizedDropsListener.getItem(pickedItem.getType());
                        sentInv.setItem(slot, pickedItem);
                    } else {
                        if (Math.random() < 0.5) {
                            if (craftMap.containsKey(pickedItem.getType())) {
                                List<Material> chooseList = craftMap.get(pickedItem.getType());
                                pickedItem = new ItemStack(chooseList.get((int) (Math.random() * chooseList.size())));
                                sentInv.setItem(slot, pickedItem);
                            }
                        } else {
                            if (harvestMap.containsKey(pickedItem.getType())) {
                                List<Material> chooseList = harvestMap.get(pickedItem.getType());
                                ItemStack newItem = new ItemStack(
                                        chooseList.get((int) (chooseList.size() * Math.random())));
                                if (!sentInv.addItem(newItem).isEmpty()) {
                                    int i = (int) (Math.random() * 35 + 1);
                                    sentInv.setItem(i, pickedItem);
                                }
                            }
                        }
                    }
                }
            }

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
        if (gm.getScenarioManager().isActivated(Scenario.HOESMAD)) new BukkitRunnable() {
            public void run() { HoesMadListener.shootLaser(uhcPlayer.getPlayerUnsafe()); }
        }.runTaskLater(UhcCore.getPlugin(), (long) (20 * Math.random() * 5));
        if (Math.random() < 0.2) {
            if (sentInv.contains(Material.GOLDEN_APPLE)) {
                // Bukkit.getLogger().info(sentinel.getLivingEntity().getName() + " is trynna
                // ate a golden apple");
                int slot = sentInv.first(Material.GOLDEN_APPLE);
                ItemStack item = sentInv.getItem(slot);
                item.setAmount(item.getAmount() - 1);

                sentinel.getLivingEntity().getWorld().playSound(sentinel.getLivingEntity().getLocation(),
                        Sound.ENTITY_GENERIC_EAT, 3, 0);
                sentinel.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 120, 0));
                sentinel.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 1));
            }

            if (sentInv.contains(Material.ENCHANTED_GOLDEN_APPLE)) {
                // Bukkit.getLogger().info(npc.getFullName() + " is trynna eat an GAPPLE");
                int slot = sentInv.first(Material.ENCHANTED_GOLDEN_APPLE);
                ItemStack item = sentInv.getItem(slot);
                item.setAmount(item.getAmount() - 1);

                sentinel.getLivingEntity().getWorld().playSound(sentinel.getLivingEntity().getLocation(),
                        Sound.ENTITY_GENERIC_EAT, 3, 0);
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
