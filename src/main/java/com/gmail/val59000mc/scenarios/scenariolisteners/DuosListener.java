package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartingEvent;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.*;

public class DuosListener extends ScenarioListener{

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        List<UhcPlayer> players = new ArrayList<>(getPlayersManager().getOnlinePlayingPlayers());
        
        if (players.size() <= 2) return;

        if (players.size() % 2 == 1) {
            players.forEach(uhcPlayer->{
                if (uhcPlayer.getTeam().getMembers().size() < 2) {
                    try {
                        uhcPlayer.getPlayer().getInventory().addItem(new ItemStack(Material.TOTEM_OF_UNDYING));
                    } catch (UhcPlayerNotOnlineException u) {}
                }
            });
        }
    }

    @EventHandler
    public void onGameStateChange(UhcStartingEvent e){
        List<UhcTeam> teams = getPlayersManager().listUhcTeams();
        List<UhcPlayer> players = new ArrayList<>(getPlayersManager().getOnlinePlayingPlayers());

        // dont do it if its only 2 players, although this should never happen naturally
        if (players.size() <= 2) return;

        while (players.size() > 1) {
            int r = (int)(Math.random()*players.size());
            UhcPlayer player1 = players.get(r);
            players.remove(r);

            r = (int)(Math.random()*players.size());
            UhcPlayer player2 = players.get(r);
            players.remove(r);

            player2.setTeam(player1.getTeam());
            player1.getTeam().getMembers().add(player2);
        }
    }

}