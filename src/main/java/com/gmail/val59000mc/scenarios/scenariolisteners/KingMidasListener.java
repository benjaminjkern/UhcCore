package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
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
        Location loc = e.getBlock().getLocation().add(0.5, 0, 0.5);
        if (ores.contains(block.getType())) {
            block.setType(Material.AIR);
            loc.getWorld().dropItem(loc, new ItemStack(isActivated(Scenario.CUTCLEAN) ? Material.GOLD_INGOT : Material.GOLD_ORE,isActivated(Scenario.TRIPLEORES) ? 3 : 1));
            UhcItems.spawnExtraXp(loc,3);
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