package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

public class LuckyLeavesListener extends ScenarioListener{

    @EventHandler
    public void onLeaveDecay(LeavesDecayEvent e){

        if (Math.random()*20 <= 1)
            e.getBlock().getWorld().dropItem(e.getBlock().getLocation().add(.5,0,.5),new ItemStack(Material.APPLE));

        if (Math.random()*200 <= 1)
            e.getBlock().getWorld().dropItem(e.getBlock().getLocation().add(.5,0,.5),new ItemStack(Material.GOLDEN_APPLE));

        if (Math.random()*2000 <= 1)
            e.getBlock().getWorld().dropItem(e.getBlock().getLocation().add(.5,0,.5),new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
    }

}