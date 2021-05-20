package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class NoStackListener extends ScenarioListener {

    @EventHandler
    public void onStart(UhcStartedEvent e) {
        new BukkitRunnable() {
            public void run() {
                for (UhcPlayer up : getPlayersManager().getAllPlayingPlayers()) {
                    try {
                        Player player = up.getPlayer();
                        player.getInventory().setMaxStackSize(1);
                        for (ItemStack i : player.getInventory()) {
                            if (i == null || i.getType() == Material.AIR) continue;
                            if (i.getAmount() > 1) {
                                ItemStack copy = new ItemStack(i);
                                copy.setAmount(i.getAmount() - 1);
                                i.setAmount(1);
                                player.getWorld().dropItemNaturally(player.getEyeLocation(), copy);
                            }
                        }
                    } catch (Exception e) {}
                }
            }
        }.runTaskTimer(UhcCore.getPlugin(), 20, 20);
    }
}
