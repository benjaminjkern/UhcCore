package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.RandomUtils;
import com.gmail.val59000mc.utils.UniversalMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

public class TripleOresListener extends ScenarioListener {

    Map<Material, Ore> ores;

    public TripleOresListener() {
        ores = new HashMap<>();
        ores.put(Material.IRON_ORE, new Ore(Material.IRON_ORE, 2, 5, Material.IRON_ORE, Material.IRON_INGOT));
        ores.put(Material.COAL_ORE, new Ore(Material.COAL_ORE, 0, 2, Material.COAL));
        ores.put(Material.DIAMOND_ORE, new Ore(Material.DIAMOND_ORE, 3,7, Material.DIAMOND));
        ores.put(Material.GOLD_ORE, new Ore(Material.GOLD_ORE, 2,5, 2, Material.GOLD_ORE, Material.GOLD_INGOT));
        ores.put(Material.LAPIS_ORE, new Ore(Material.LAPIS_ORE, 2, 5, Material.LAPIS_LAZULI, 4, 9));
        ores.put(Material.EMERALD_ORE, new Ore(Material.EMERALD_ORE, 3,7, Material.EMERALD));
        ores.put(Material.NETHER_GOLD_ORE, new Ore(Material.NETHER_GOLD_ORE, 0,0, Material.GOLD_NUGGET, 2, 6));
        ores.put(Material.NETHER_QUARTZ_ORE, new Ore(Material.NETHER_QUARTZ_ORE, 2,5, Material.QUARTZ));
        // this one isnt accurate but I didnt feel like coding in their weird
        // fuckin rules so if you have fortune youre just gonna get a lot of redstone
        ores.put(Material.REDSTONE_ORE, new Ore(Material.REDSTONE_ORE, 1,5, Material.REDSTONE, 4, 5));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        if (ores.containsKey(block.getType())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("[Triple Ores] You can't place that while Triple Ores is on!");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // handles it on its own
        if (isActivated(Scenario.KINGMIDAS) || (isActivated(Scenario.VEINMINER) && e.getPlayer().isSneaking())) return;

        Block block = e.getBlock();
        ItemStack hand = e.getPlayer().getItemInHand();

        if (!UniversalMaterial.isCorrectTool(block.getType(), hand.getType())) return;

        Location loc = e.getBlock().getLocation().add(0.5, 0, 0.5);
        boolean silkTouch = hand.containsEnchantment(Enchantment.SILK_TOUCH);
        int fortuneLevel = hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);

        if (ores.containsKey(block.getType())) {
            Ore thisOre = ores.get(block.getType());
            block.setType(Material.AIR);
            loc.getWorld().dropItem(loc, new ItemStack(thisOre.getMaterial(silkTouch), 3 * thisOre.getAmount(silkTouch, fortuneLevel)));
            UhcItems.spawnExtraXp(loc,thisOre.getExp(silkTouch));
        }
    }

    private class Ore {
        final Material silkMaterial, material, cutCleanMaterial;
        final int lowExp, highExp, doubleGoldAmount, lowAmount, highAmount;

        Ore(Material original, int lowExp, int highExp, Material m) {
            this.silkMaterial = original;
            this.material = m;
            this.cutCleanMaterial = m;
            this.doubleGoldAmount = 1;
            this.lowAmount = 1;
            this.highAmount = 1;
            this.lowExp = lowExp;
            this.highExp = highExp;
        }

        Ore(Material original, int lowExp, int highExp, Material m, Material cc) {
            this.silkMaterial = original;
            this.material = m;
            this.cutCleanMaterial = cc;
            this.doubleGoldAmount = 1;
            this.lowAmount = 1;
            this.highAmount = 1;
            this.lowExp = lowExp;
            this.highExp = highExp;
        }

        Ore(Material original, int lowExp, int highExp, Material m, int lowAmount, int highAmount) {
            this.silkMaterial = original;
            this.material = m;
            this.cutCleanMaterial = m;
            this.doubleGoldAmount = 1;
            this.lowAmount = lowAmount;
            this.highAmount = lowAmount;
            this.lowExp = lowExp;
            this.highExp = highExp;
        }
        
        Ore(Material original, int lowExp, int highExp, int dg, Material m, Material cc) {
            this.silkMaterial = original;
            this.material = m;
            this.cutCleanMaterial = cc;
            this.doubleGoldAmount = dg;
            this.lowAmount = 1;
            this.highAmount = 1;
            this.lowExp = lowExp;
            this.highExp = highExp;
        }

        Material getMaterial(boolean silkTouch) {
            if (isActivated(Scenario.CUTCLEAN)) return cutCleanMaterial;
            if (silkTouch) return silkMaterial;
            return material;
        }

        int getAmount(boolean silkTouch, int fortuneLevel) {
            if (isActivated(Scenario.DOUBLEGOLD)) return doubleGoldAmount * getFortune(silkTouch, fortuneLevel);
            if (silkTouch) return 1;
            return RandomUtils.randomInteger(lowAmount, highAmount) * getFortune(silkTouch, fortuneLevel);
        }

        private int getFortune(boolean silkTouch, int fortuneLevel) {
            if (ores.containsKey(getMaterial(silkTouch))) return 1;

            double r = Math.random() * 60;
            switch (fortuneLevel) {
                case 1:
                    return r < 40 ? 1 : 2;
                case 2:
                    return r < 30 ? 1 : r < 45 ? 2 : 3;
                case 3:
                    return r < 24 ? 1 : r < 36 ? 2 : r < 48 ? 3 : 4;
                default:
                    return 1;
            }
        }

        int getExp(boolean silkTouch) {
            if (silkTouch || ores.containsKey(getMaterial(silkTouch))) return 0;
            return RandomUtils.randomInteger(lowExp, highExp);
        }
    }

}