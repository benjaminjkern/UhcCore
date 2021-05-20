package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FlowerPowerListener extends ScenarioListener {

    private static final Set<Material> FLOWERS;
    static {
        FLOWERS = new HashSet<>();
        FLOWERS.add(Material.POPPY);
        FLOWERS.add(Material.BLUE_ORCHID);
        FLOWERS.add(Material.ALLIUM);
        FLOWERS.add(Material.AZURE_BLUET);
        FLOWERS.add(Material.RED_TULIP);
        FLOWERS.add(Material.ORANGE_TULIP);
        FLOWERS.add(Material.WHITE_TULIP);
        FLOWERS.add(Material.PINK_TULIP);
        FLOWERS.add(Material.OXEYE_DAISY);
        FLOWERS.add(Material.SUNFLOWER);
        FLOWERS.add(Material.LILAC);
        FLOWERS.add(Material.ROSE_BUSH);
        FLOWERS.add(Material.PEONY);
        FLOWERS.add(Material.DEAD_BUSH);
        FLOWERS.add(Material.DANDELION);
        FLOWERS.add(Material.CORNFLOWER);
        FLOWERS.add(Material.LILY_OF_THE_VALLEY);
        FLOWERS.add(Material.WITHER_ROSE);
        FLOWERS.add(Material.BROWN_MUSHROOM);
        FLOWERS.add(Material.RED_MUSHROOM);
        FLOWERS.add(Material.CRIMSON_FUNGUS);
        FLOWERS.add(Material.WARPED_FUNGUS);
        FLOWERS.add(Material.TUBE_CORAL);
        FLOWERS.add(Material.BRAIN_CORAL);
        FLOWERS.add(Material.BUBBLE_CORAL);
        FLOWERS.add(Material.FIRE_CORAL);
        FLOWERS.add(Material.HORN_CORAL);
        FLOWERS.add(Material.TUBE_CORAL_FAN);
        FLOWERS.add(Material.BRAIN_CORAL_FAN);
        FLOWERS.add(Material.BUBBLE_CORAL_FAN);
        FLOWERS.add(Material.FIRE_CORAL_FAN);
        FLOWERS.add(Material.HORN_CORAL_FAN);
        FLOWERS.add(Material.TUBE_CORAL_WALL_FAN);
        FLOWERS.add(Material.BRAIN_CORAL_WALL_FAN);
        FLOWERS.add(Material.BUBBLE_CORAL_WALL_FAN);
        FLOWERS.add(Material.FIRE_CORAL_WALL_FAN);
        FLOWERS.add(Material.HORN_CORAL_WALL_FAN);
    }

    private static final Map<Material, Integer> DROPS;
    private static List<Material> flowerDrops;
    static {
        DROPS = new HashMap<>();
        DROPS.put(Material.COOKED_BEEF, 16);
        DROPS.put(Material.GOLDEN_APPLE, 4);
        DROPS.put(Material.APPLE, 16);
        DROPS.put(Material.COOKED_COD, 16);
        DROPS.put(Material.GOLDEN_CARROT, 16);
        DROPS.put(Material.MELON, 64);
        DROPS.put(Material.MUSHROOM_STEW, 1);

        DROPS.put(Material.WHEAT, 64);
        DROPS.put(Material.SUGAR, 64);
        DROPS.put(Material.EGG, 16);

        DROPS.put(Material.SADDLE, 1);
        DROPS.put(Material.IRON_HORSE_ARMOR, 1);
        DROPS.put(Material.GOLDEN_HORSE_ARMOR, 1);
        DROPS.put(Material.DIAMOND_HORSE_ARMOR, 1);
        DROPS.put(Material.LEATHER_HORSE_ARMOR, 1);

        DROPS.put(Material.SMITHING_TABLE, 1);
        DROPS.put(Material.BREWING_STAND, 1);
        DROPS.put(Material.ENCHANTING_TABLE, 1);
        DROPS.put(Material.ANVIL, 4);
        DROPS.put(Material.BOOKSHELF, 16);
        DROPS.put(Material.CRAFTING_TABLE, 1);

        DROPS.put(Material.ANCIENT_DEBRIS, 16);
        DROPS.put(Material.NETHERITE_SCRAP, 8);
        DROPS.put(Material.NETHERITE_INGOT, 1);

        DROPS.put(Material.DIAMOND_ORE, 16);
        DROPS.put(Material.DIAMOND_SHOVEL, 1);
        DROPS.put(Material.DIAMOND, 8);
        DROPS.put(Material.DIAMOND_BLOCK, 1);

        DROPS.put(Material.IRON_ORE, 16);
        DROPS.put(Material.IRON_INGOT, 8);
        DROPS.put(Material.IRON_BLOCK, 1);
        DROPS.put(Material.IRON_AXE, 1);
        DROPS.put(Material.IRON_SWORD, 1);
        DROPS.put(Material.IRON_PICKAXE, 1);
        DROPS.put(Material.IRON_SHOVEL, 1);
        DROPS.put(Material.IRON_HOE, 1);
        DROPS.put(Material.IRON_BOOTS, 1);
        DROPS.put(Material.IRON_LEGGINGS, 1);
        DROPS.put(Material.IRON_CHESTPLATE, 1);
        DROPS.put(Material.IRON_HELMET, 1);

        DROPS.put(Material.EXPERIENCE_BOTTLE, 64);
        DROPS.put(Material.GLASS_BOTTLE, 64);
        DROPS.put(Material.NETHER_WART, 64);
        DROPS.put(Material.REDSTONE_BLOCK, 64);
        DROPS.put(Material.SUGAR_CANE, 64);
        DROPS.put(Material.BONE, 64);

        DROPS.put(Material.SPECTRAL_ARROW, 16);
        DROPS.put(Material.TOTEM_OF_UNDYING, 1);
        DROPS.put(Material.SHIELD, 1);
        DROPS.put(Material.TRIDENT, 1);
        DROPS.put(Material.CROSSBOW, 1);
        DROPS.put(Material.TURTLE_HELMET, 1);
        DROPS.put(Material.COMPASS, 1);
        DROPS.put(Material.SHEARS, 1);
        DROPS.put(Material.NAME_TAG, 1);

        DROPS.put(Material.TIPPED_ARROW, 8);
        DROPS.put(Material.ENCHANTED_BOOK, 1);
        DROPS.put(Material.CAT_SPAWN_EGG, 5);

        DROPS.put(Material.CHAINMAIL_HELMET, 1);
        DROPS.put(Material.CHAINMAIL_CHESTPLATE, 1);
        DROPS.put(Material.CHAINMAIL_LEGGINGS, 1);
        DROPS.put(Material.CHAINMAIL_BOOTS, 1);

        DROPS.put(Material.LEATHER, 32);
        DROPS.put(Material.LEATHER_HELMET, 1);
        DROPS.put(Material.LEATHER_BOOTS, 1);
        DROPS.put(Material.LEATHER_LEGGINGS, 1);
        DROPS.put(Material.LEATHER_CHESTPLATE, 1);

        DROPS.put(Material.GOLD_INGOT, 16);
        DROPS.put(Material.GOLD_BLOCK, 2);
        DROPS.put(Material.GOLD_ORE, 32);
        DROPS.put(Material.GOLDEN_SHOVEL, 1);
        DROPS.put(Material.GOLDEN_PICKAXE, 1);
        DROPS.put(Material.GOLDEN_SWORD, 1);
        DROPS.put(Material.GOLDEN_AXE, 1);
        DROPS.put(Material.GOLDEN_HOE, 1);
        DROPS.put(Material.GOLDEN_HELMET, 1);
        DROPS.put(Material.GOLDEN_CHESTPLATE, 1);
        DROPS.put(Material.GOLDEN_LEGGINGS, 1);
        DROPS.put(Material.GOLDEN_BOOTS, 1);

        DROPS.put(Material.MILK_BUCKET, 1);
        DROPS.put(Material.FISHING_ROD, 1);
        DROPS.put(Material.ARROW, 32);
        DROPS.put(Material.BOW, 1);
        DROPS.put(Material.WATER_BUCKET, 1);
        DROPS.put(Material.LAVA_BUCKET, 1);
        DROPS.put(Material.FLINT_AND_STEEL, 1);
        DROPS.put(Material.CARROT_ON_A_STICK, 1);
        DROPS.put(Material.ENDER_PEARL, 2);

        DROPS.put(Material.EMERALD_ORE, 32);
        DROPS.put(Material.EMERALD_BLOCK, 2);

        DROPS.put(Material.TNT, 8);

        DROPS.put(Material.FLINT, 64);
        DROPS.put(Material.STICK, 64);
        DROPS.put(Material.FEATHER, 64);
        DROPS.put(Material.STRING, 64);
        DROPS.put(Material.GUNPOWDER, 16);

        DROPS.put(Material.LAPIS_ORE, 64);
        DROPS.put(Material.LAPIS_BLOCK, 4);

        DROPS.put(Material.OBSIDIAN, 16);

        DROPS.put(Material.CACTUS, 64);
        DROPS.put(Material.GRAVEL, 64);
        DROPS.put(Material.COBWEB, 64);
        DROPS.put(Material.COBBLESTONE, 64);
        DROPS.put(Material.SPRUCE_LOG, 64);

        DROPS.put(Material.STONE, 64);
        DROPS.put(Material.DIRT, 64);
        DROPS.put(Material.SAND, 64);
        DROPS.put(Material.LADDER, 64);
        DROPS.put(Material.STONE_BUTTON, 64);
        DROPS.put(Material.SPONGE, 64);
        DROPS.put(Material.PUMPKIN, 64);
        DROPS.put(Material.CHEST, 64);
        DROPS.put(Material.LILY_PAD, 64);
        DROPS.put(Material.SOUL_SAND, 64);
        DROPS.put(Material.ACACIA_FENCE, 64);
        DROPS.put(Material.CLAY, 64);
        DROPS.put(Material.QUARTZ, 64);
        DROPS.put(Material.BRICK_STAIRS, 64);
        DROPS.put(Material.MYCELIUM, 64);
        DROPS.put(Material.OAK_TRAPDOOR, 64);
        DROPS.put(Material.STONE_BRICKS, 64);
        DROPS.put(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, 64);
        DROPS.put(Material.HOPPER, 64);
        DROPS.put(Material.POWERED_RAIL, 64);
        DROPS.put(Material.GLASS, 64);
        DROPS.put(Material.LEVER, 64);
        DROPS.put(Material.GRASS, 64);
        DROPS.put(Material.ICE, 64);
        DROPS.put(Material.CAULDRON, 64);
        DROPS.put(Material.PISTON, 64);
        DROPS.put(Material.IRON_BARS, 64);
        DROPS.put(Material.CLAY_BALL, 64);
        DROPS.put(Material.BRICK, 64);
        DROPS.put(Material.SANDSTONE, 64);
        DROPS.put(Material.ITEM_FRAME, 64);
        DROPS.put(Material.SPRUCE_DOOR, 64);
        DROPS.put(Material.ACTIVATOR_RAIL, 64);
        DROPS.put(Material.MOSSY_COBBLESTONE_WALL, 64);
        DROPS.put(Material.COBBLESTONE_STAIRS, 64);

        DROPS.put(Material.RED_WOOL, 64);
        DROPS.put(Material.LIGHT_BLUE_WOOL, 64);

        DROPS.put(Material.JUKEBOX, 64);

        DROPS.put(Material.MUSIC_DISC_FAR, 1);

        flowerDrops = new ArrayList<>();
        DROPS.keySet().forEach(key -> flowerDrops.add(key));
    }

    private static final int expPerFlower = 2;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();

        // For tall flowers start with the bottom block.
        Block below = block.getRelative(BlockFace.DOWN);
        if (isFlower(below)) { block = below; }

        if (isFlower(block)) {
            Location blockLoc = block.getLocation().add(.5, .5, .5);
            block.setType(Material.AIR);
            UhcItems.spawnExtraXp(blockLoc, expPerFlower);

            ItemStack drop = flowerDrop();
            blockLoc.getWorld().dropItem(blockLoc, drop);
        }
    }

    public static ItemStack flowerDrop() {
        Material m = flowerDrops.get((int) (Math.random() * flowerDrops.size()));
        ItemStack drop = new ItemStack(m, (int) (Math.random() * DROPS.get(m) + 1));
        if (m == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta im = (EnchantmentStorageMeta) drop.getItemMeta();
            double r = 0;
            while (r < 0.5) {
                List<Enchantment> allEnchants = new ArrayList<>(Arrays.asList(Enchantment.values()));
                Enchantment ench = allEnchants.get((int) (Math.random() * allEnchants.size()));
                im.addStoredEnchant(ench, (int) Math.ceil(Math.random() * ench.getMaxLevel()), true);
                r = Math.random();
            }
            drop.setItemMeta(im);
        } else if (m == Material.TIPPED_ARROW) {
            PotionMeta pm = (PotionMeta) drop.getItemMeta();
            List<PotionEffectType> allEnchants = new ArrayList<>(Arrays.asList(PotionEffectType.values()));
            PotionEffectType potionType = allEnchants.get((int) (Math.random() * allEnchants.size()));
            pm.addCustomEffect(new PotionEffect(potionType, (int) (Math.random() * 20 * 20), (int) (Math.random() * 2)),
                    true);
            drop.setItemMeta(pm);
        } else if (m == Material.CAT_SPAWN_EGG) {
            List<Material> allEnchants = new ArrayList<>(Arrays.asList(Material.values())).stream()
                    .filter(material -> material.name().contains("SPAWN_EGG")).collect(Collectors.toList());
            drop.setType(allEnchants.get((int) (Math.random() * allEnchants.size())));
        } else if (m == Material.MUSIC_DISC_FAR) {
            List<Material> allEnchants = new ArrayList<>(Arrays.asList(Material.values())).stream()
                    .filter(material -> material.name().contains("MUSIC_DISC")).collect(Collectors.toList());
            drop.setType(allEnchants.get((int) (Math.random() * allEnchants.size())));
        }
        return drop;
    }

    private boolean isFlower(Block block) { return FLOWERS.contains(block.getType()); }

}