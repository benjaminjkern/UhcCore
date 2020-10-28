package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.PlayerState;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.Location;

import java.util.*;

public class SwapListener extends ScenarioListener {

    @Option(key = "time")
    private int time = 60;

    private Map<UhcPlayer, UhcPlayer> order;
    private Map<UhcPlayer, Location> locations;

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        // create random order
        order = new HashMap<>();
        locations = new HashMap<>();

        UhcPlayer lastPlayer = null;
        UhcPlayer firstPlayer = null;

        for (UhcPlayer uhcPlayer : getPlayersManager().getAllPlayingPlayers()) {
            if (lastPlayer == null) firstPlayer = uhcPlayer;
            else order.put(lastPlayer, uhcPlayer);
            lastPlayer = uhcPlayer;
        } ;
        order.put(lastPlayer, firstPlayer);

        new BukkitRunnable() {
            @Override
            public void run() { getGameManager().broadcastInfoMessage("Swapping places in \u00a7f30 seconds!"); }
        }.runTaskTimer(UhcCore.getPlugin(), 20 * (time - 30), 20 * time);

        new BukkitRunnable() {
            @Override
            public void run() { getGameManager().broadcastInfoMessage("Swapping places in \u00a7f10 seconds!"); }
        }.runTaskTimer(UhcCore.getPlugin(), 20 * (time - 10), 20 * time);

        new BukkitRunnable() {
            @Override
            public void run() {
                getGameManager().broadcastInfoMessage("Swapping places!");

                order.keySet().forEach(uhcPlayer -> {
                    if (uhcPlayer.getState() != PlayerState.PLAYING) return;
                    UhcPlayer nextPlayer = order.get(uhcPlayer);

                    while (nextPlayer.getState() != PlayerState.PLAYING) nextPlayer = order.get(nextPlayer);

                    order.put(uhcPlayer, nextPlayer);
                    try {
                        locations.put(uhcPlayer, nextPlayer.getPlayer().getLocation());
                    } catch (UhcPlayerNotOnlineException e) {}
                });

                locations.keySet().forEach(uhcPlayer -> {
                    try {
                        if (uhcPlayer.getName().equals("YEUH-BOT")) CitizensAPI.getNPCRegistry()
                                .getNPC(uhcPlayer.getPlayer()).getNavigator().cancelNavigation();
                        uhcPlayer.getPlayer().teleport(locations.get(uhcPlayer));
                    } catch (UhcPlayerNotOnlineException e) {}
                });
            }

        }.runTaskTimer(UhcCore.getPlugin(), 20 * time, 20 * time);
    }

}