package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import com.gmail.val59000mc.UhcCore;
import org.bukkit.scheduler.BukkitRunnable;

public class LagWorldListener extends ScenarioListener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        if (isActivated(Scenario.VEINMINER) || isActivated(Scenario.DOUBLEORES)) {
            return;
        }

        final Block block = e.getBlock();
        final Material material = block.getType();


        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(material);
            }
            
        }.runTaskLater(UhcCore.getPlugin(), 1);

    }

}