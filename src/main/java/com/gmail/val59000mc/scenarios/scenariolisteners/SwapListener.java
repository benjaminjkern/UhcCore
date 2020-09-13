package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.UniversalMaterial;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.PlayerState;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;


import java.util.*;

public class SwapListener extends ScenarioListener{

    @Option(key = "time")
    private int time = 60;

    private Map<UhcPlayer, UhcPlayer> order;
    private Map<UhcPlayer, Location> locations;

    @EventHandler
    public void onGameStart(UhcStartedEvent e){
        // create random order
        order = new HashMap<>();
        locations = new HashMap<>();

        UhcPlayer lastPlayer = null;
        UhcPlayer firstPlayer = null;

        for (UhcPlayer uhcPlayer : getPlayersManager().getOnlinePlayingPlayers()) {
                if (lastPlayer == null) firstPlayer = uhcPlayer;
                else order.put(lastPlayer, uhcPlayer);
                lastPlayer = uhcPlayer;
        };
        order.put(lastPlayer, firstPlayer);


        new BukkitRunnable() {
            @Override
            public void run() {
                getGameManager().broadcastInfoMessage("Swapping places!");

                order.keySet().forEach(uhcPlayer -> {
                    UhcPlayer nextPlayer = order.get(uhcPlayer);
                    
                    while (!(nextPlayer.getState().equals(PlayerState.PLAYING) && nextPlayer.isOnline()))
                        nextPlayer = order.get(nextPlayer);
                    
                    order.put(uhcPlayer, nextPlayer);
                    try {
                        locations.put(uhcPlayer, nextPlayer.getPlayer().getLocation());
                    } catch (UhcPlayerNotOnlineException e) {}
                });

                locations.keySet().forEach(uhcPlayer -> {
                    try {
                        uhcPlayer.getPlayer().teleport(locations.get(uhcPlayer));
                    } catch (UhcPlayerNotOnlineException e) {}
                });
            }
            
        }.runTaskTimer(UhcCore.getPlugin(), 20*time, 20*time);
    }

}