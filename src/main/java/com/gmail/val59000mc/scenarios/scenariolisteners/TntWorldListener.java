package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockIgniteEvent;

public class TntWorldListener extends ScenarioListener {

    @EventHandler
    public void onIgnite(BlockIgniteEvent event) {
        if (event.getCause() != BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) return;
        Block block = event.getPlayer().getTargetBlockExact(5, FluidCollisionMode.NEVER);

        event.getBlock().breakNaturally();

        block.setType(Material.AIR);
        block.getLocation().getWorld().spawnEntity(block.getLocation().clone().add(0.5, 0, 0.5), EntityType.PRIMED_TNT);
    }

}