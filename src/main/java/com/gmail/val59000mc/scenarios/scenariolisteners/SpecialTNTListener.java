package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SpecialTNTListener extends ScenarioListener {

    private Set<Material> bannedMaterials;
    private List<Material> allItems;

    public SpecialTNTListener() {
        bannedMaterials = new HashSet<>();
        bannedMaterials.add(Material.BEDROCK);
        bannedMaterials.add(Material.BARRIER);
        bannedMaterials.add(Material.AIR);
        bannedMaterials.add(Material.COMMAND_BLOCK);
        bannedMaterials.add(Material.CHAIN_COMMAND_BLOCK);

        allItems = Arrays.asList(Material.values()).stream().filter(m -> !bannedMaterials.contains(m) && m.isSolid())
                .collect(Collectors.toList());
        Collections.shuffle(allItems);
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent e) { replaceBlocks(e.blockList()); }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) { replaceBlocks(e.blockList()); }

    private void replaceBlocks(List<Block> blockList) {
        new BukkitRunnable() {
            public void run() {
                blockList.forEach(block -> block
                        .setType(allItems.get((int) (getGameManager().getElapsedTime() % allItems.size()))));
            }
        }.runTaskLater(UhcCore.getPlugin(), 1);
    }

}