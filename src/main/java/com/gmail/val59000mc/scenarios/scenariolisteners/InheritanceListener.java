package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcPlayerKillEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.listeners.PlayerDeathListener;
import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class InheritanceListener extends ScenarioListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnable() {
        PlayerDeathListener.dropItems = false;
    }

    @EventHandler
    public void onPlayerDeath(UhcPlayerKillEvent e) {
        try {
            Bukkit.getLogger().info("Inheritance player death!");
            e.getKiller().getPlayer().getInventory()
                    .setContents(e.getKilled().getPlayer().getInventory().getContents());
        } catch (UhcPlayerNotOnlineException ex) {
            // no idea how we got here;
        }
    }
}
