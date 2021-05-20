package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gmail.val59000mc.events.UhcGameEndEvent;
import com.gmail.val59000mc.events.UhcPlayerDeathEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.listeners.PlayerDeathListener;
import com.gmail.val59000mc.players.SpawnLocations;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.scoreboard.SuperLayout;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class SlayerListener extends ScenarioListener {

    @Override
    public void onEnable() {
        SuperLayout sl = getGameManager().getScoreboardManager().getSuperLayout();
        sl.addToLayoutSet(null, true, "topTeam");
        sl.addToLayoutSet(null, false, "top", "topKills");
        sl.replaceInLayoutSet(null, null, "time", "timeLeft");
        sl.addToLayoutSet("playing", null, "deaths");
        sl.removeFromLayoutSet(null, true, "teams");

        PlayerDeathListener.autoRespawn = true;
        PlayerDeathListener.keepInventory = 0.5;
        PlayerDeathListener.publicAnnounceDeaths = false;
        PlayerDeathListener.privateAnnounceDeaths = true;
    }

    @EventHandler
    public void onDeath(UhcPlayerDeathEvent e) {
        Sound randomSound = PlayerDeathListener.getRandomHurtSound();
        try {
            Player killed = e.getKilled().getPlayer();
            killed.getWorld().playSound(killed.getLocation(), randomSound, 1, 0);
            killed.teleport(SpawnLocations.findRandomSafeLocation(killed.getWorld()));
            killed.getWorld().playSound(killed.getLocation(), randomSound, 1, 0);
        } catch (UhcPlayerNotOnlineException ex) {
        }
    }

    @EventHandler
    public void onEnd(UhcGameEndEvent e) {
        List<UhcPlayer> leaders = e.getLeaders();
        List<UhcTeam> topTeams = new ArrayList<>();
        int kills = 0;
        for (UhcTeam team : getGameManager().getTeamManager().getUhcTeams()) {
            int teamKills = team.getPlayingKills();
            if (teamKills > kills) {
                topTeams.clear();
                kills = teamKills;
            }
            if (teamKills >= kills && kills > 0)
                topTeams.add(team);
        }

        if (topTeams.isEmpty()) {
            getGameManager().broadcastInfoMessage("\u00a7c\u00a7lNobody got any kills! You guys suck!");
            leaders.add(null);
            return;
        }

        if (topTeams.size() == 1) {
            UhcTeam top = topTeams.get(0);
            if (top.isSolo())
                getGameManager().broadcastInfoMessage(top.getMembers().get(0).getDisplayName()
                        + " is the winner with \u00a7d" + kills + " \u00a7fkills!");
            else
                getGameManager().broadcastInfoMessage("Team " + top.getPrefix() + " \u00a7fis the winner with \u00a7d"
                        + kills + " \u00a7ftotal kills!");
            leaders.addAll(top.getMembers());
        } else {
            getGameManager().broadcastInfoMessage("It's a tie!");
            if (topTeams.stream().anyMatch(team -> team.getMembers().size() > 1)) {
                getGameManager().broadcastInfoMessage("Teams "
                        + topTeams.stream().map(team -> team.getPrefix().substring(0, team.getPrefix().length() - 1))
                                .collect(Collectors.joining(", "))
                        + " are the winners with \u00a7d" + kills + " \u00a7ftotal kills each!");
            } else {
                getGameManager().broadcastInfoMessage(topTeams.stream()
                        .map(team -> team.getMembers().get(0).getDisplayName()).collect(Collectors.joining(", "))
                        + " are the winners with \u00a7d" + kills + " \u00a7fkills each!");
            }
            topTeams.forEach(team -> team.getMembers().forEach(uhcPlayer -> {
                leaders.add(uhcPlayer);
            }));
        }
    }

}