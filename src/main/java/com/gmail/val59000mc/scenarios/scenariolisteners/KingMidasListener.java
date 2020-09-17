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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

public class KingMidasListener extends ScenarioListener {

    private Set<Material> ores;

    @Override
    public void onEnable() {
        ores = new HashSet<>();
        ores.add(Material.IRON_ORE);
        ores.add(Material.COAL_ORE);
        ores.add(Material.DIAMOND_ORE);
        ores.add(Material.GOLD_ORE);
        ores.add(Material.LAPIS_ORE);
        ores.add(Material.EMERALD_ORE);
        ores.add(Material.NETHER_GOLD_ORE);
        ores.add(Material.NETHER_QUARTZ_ORE);
        ores.add(Material.REDSTONE_ORE);
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Block block = e.getBlock();
        ItemStack hand = e.getPlayer().getItemInHand();

        if (!UniversalMaterial.isCorrectTool(block.getType(), hand.getType())) return;

        Location loc = e.getBlock().getLocation().add(0.5, 0, 0.5);

        if (ores.contains(block.getType())) {
            block.setType(Material.AIR);
            loc.getWorld().dropItem(loc, new ItemStack(isActivated(Scenario.CUTCLEAN) ? Material.GOLD_INGOT : Material.GOLD_ORE,(isActivated(Scenario.TRIPLEORES) ? 3 : 1)*getFortune(hand)));
            UhcItems.spawnExtraXp(loc,RandomUtils.randomInteger(2, 5));
        }
    }

    private int getFortune(ItemStack hand) {
        if (!isActivated(Scenario.CUTCLEAN) || hand.containsEnchantment(Enchantment.SILK_TOUCH)) return 1;

        double r = Math.random() * 60;
        switch (hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)) {
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

    @EventHandler
    public void onEntityKilled(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Player) return;

        e.getDrops().clear();
        Location loc = entity.getLocation();
        loc.getWorld().dropItem(loc, new ItemStack(entity instanceof Monster ? Material.GOLD_INGOT : Material.GOLD_NUGGET,1));
    }

}