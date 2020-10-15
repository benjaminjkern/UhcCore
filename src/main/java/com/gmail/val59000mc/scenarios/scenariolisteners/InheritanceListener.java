package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class InheritanceListener extends ScenarioListener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        // wasn't killed by player
        if (e.getEntity().getKiller() == null) return;

        e.getEntity().getKiller().getInventory().setContents(e.getEntity().getInventory().getContents());
    }
}
