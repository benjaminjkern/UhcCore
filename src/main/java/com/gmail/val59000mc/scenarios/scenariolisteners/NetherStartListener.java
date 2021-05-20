package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.Queue;

import com.gmail.val59000mc.events.UhcPreTeleportEvent;
import com.gmail.val59000mc.players.SpawnLocations;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;

public class NetherStartListener extends ScenarioListener {

    @Override
    public void onEnable() {
        if (!getConfiguration().getEnableNether()) {
            Bukkit.broadcastMessage(ChatColor.RED + "[UhcCore] For NetherStart the nether needs to be enabled first!");
            getScenarioManager().removeScenario(Scenario.NETHERSTART);
        }

        Location portalLoc = SpawnLocations.findSafeLocationAround(
                new Location(Bukkit.getWorld(getGameManager().getConfiguration().getNetherUuid()), 0, 0, 0), 500);

        portalLoc.clone().add(0, -1, 0).getBlock().setType(Material.OBSIDIAN);
        portalLoc.clone().add(0, -1, 1).getBlock().setType(Material.OBSIDIAN);
        portalLoc.clone().add(0, 3, 0).getBlock().setType(Material.OBSIDIAN);
        portalLoc.clone().add(0, 3, 1).getBlock().setType(Material.OBSIDIAN);
        portalLoc.clone().add(0, 0, -1).getBlock().setType(Material.OBSIDIAN);
        portalLoc.clone().add(0, 0, 2).getBlock().setType(Material.OBSIDIAN);
        portalLoc.clone().add(0, 1, -1).getBlock().setType(Material.OBSIDIAN);
        portalLoc.clone().add(0, 1, 2).getBlock().setType(Material.OBSIDIAN);
        portalLoc.clone().add(0, 2, -1).getBlock().setType(Material.OBSIDIAN);
        portalLoc.clone().add(0, 2, 2).getBlock().setType(Material.OBSIDIAN);
    }

    @EventHandler
    public void onPreTeleport(UhcPreTeleportEvent e) {
        World nether = Bukkit.getWorld(getConfiguration().getNetherUuid());
        double maxDistance = 0.9 * (nether.getWorldBorder().getSize() / 2);
        Queue<Location> randLocs = SpawnLocations.getRandomSafeLocations(nether, Bukkit.getMaxPlayers(), maxDistance,
                50);

        for (UhcTeam team : getPlayersManager().listUhcTeams()) { team.setStartingLocation(randLocs.poll()); }
    }

}