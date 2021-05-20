package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartingEvent;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.VersionUtils;

import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;

import java.util.*;

public class FiftyListener extends ScenarioListener {
    @Override
    public void onEnable() {

        getGameManager().getScoreboardManager().getSuperLayout().setTeams(true);
    }

    @EventHandler
    public void onGameStateChange(UhcStartingEvent e) {
        List<UhcPlayer> players = new ArrayList<>(getPlayersManager().getPlayersList());

        // dont do it if its only 2 players, although the scenario should block this
        // from happening
        if (players.size() <= 2)
            return;

        List<UhcPlayer> realPlayers = new ArrayList<>();
        List<UhcPlayer> botPlayers = new ArrayList<>();
        for (UhcPlayer up : players) {
            if (up.getName().equalsIgnoreCase("YEUH-BOT"))
                botPlayers.add(up);
            else
                realPlayers.add(up);
        }

        UhcTeam team1 = null;
        UhcTeam team2 = null;

        VersionUtils.getVersionUtils().setGameRuleValue(
                Bukkit.getWorld(GameManager.getGameManager().getConfiguration().getOverworldUuid()),
                "maxEntityCramming", 1000);
        // dont do it if its only 2 players, although this should never happen naturally

        while (!realPlayers.isEmpty()) {
            int r = (int) (Math.random() * realPlayers.size());
            UhcPlayer player1 = realPlayers.get(r);
            realPlayers.remove(r);

            if (team1 == null) {
                team1 = player1.getTeam();
            } else {
                player1.setTeam(team1);
                team1.getMembers().add(player1);
            }

            if (realPlayers.isEmpty())
                break;

            r = (int) (Math.random() * realPlayers.size());
            UhcPlayer player2 = realPlayers.get(r);
            realPlayers.remove(r);

            if (team2 == null) {
                team2 = player2.getTeam();
            } else {
                player2.setTeam(team2);
                team2.getMembers().add(player2);
            }
        }

        while (!botPlayers.isEmpty()) {
            int r = (int) (Math.random() * botPlayers.size());
            UhcPlayer player1 = botPlayers.get(r);
            botPlayers.remove(r);

            if (team2 == null) {
                team2 = player1.getTeam();
            } else {
                player1.setTeam(team2);
                team2.getMembers().add(player1);
            }

            if (botPlayers.isEmpty())
                break;

            r = (int) (Math.random() * botPlayers.size());
            UhcPlayer player2 = botPlayers.get(r);
            botPlayers.remove(r);

            if (team1 == null) {
                team1 = player2.getTeam();
            } else {
                player2.setTeam(team1);
                team1.getMembers().add(player2);
            }
        }
    }

}