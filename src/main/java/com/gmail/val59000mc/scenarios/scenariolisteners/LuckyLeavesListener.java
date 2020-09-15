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

        int random = RandomUtils.randomInteger(0, 20);

        if (random > 1){
            return;
        }

        // add gapple
        e.getBlock().getWorld().dropItem(e.getBlock().getLocation().add(.5,0,.5),new ItemStack(Material.APPLE));

        random = RandomUtils.randomInteger(0, 200);

        if (random > 1){
            return;
        }

        // add gapple
        e.getBlock().getWorld().dropItem(e.getBlock().getLocation().add(.5,0,.5),new ItemStack(Material.GOLDEN_APPLE));


        random = RandomUtils.randomInteger(0, 2000);

        if (random > 1){
            return;
        }

        // add gapple
        e.getBlock().getWorld().dropItem(e.getBlock().getLocation().add(.5,0,.5),new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
    }

}