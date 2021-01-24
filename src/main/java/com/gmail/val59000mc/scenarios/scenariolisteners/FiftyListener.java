package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartingEvent;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.scoreboard.ScoreboardType;
import com.gmail.val59000mc.utils.VersionUtils;

import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.*;

public class FiftyListener extends ScenarioListener {
    @Override
    public void onEnable() {
        if (isActivated(Scenario.POLITICS) || isActivated(Scenario.SLAYER)) return;

        List<String> scoreboardList = Arrays.asList("&fTime: &d%time%", "&fBorder: &d%border%",
                "&fPlayers left: &d%alive%", "&fTeams left: &d%teamAlive%", "&fTeam size: &d%teammatesAlive%",
                "&fTeam: &d%teamColor%", "&fKills: &d%kills%", "&fScenarios:", "&d%scenarios%", "", "&fPlayer Rating:",
                "&6%userScore%");

        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.PLAYING,
                scoreboardList);
        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.DEATHMATCH,
                scoreboardList);
        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.SPECTATING,
                scoreboardList);
    }

    @EventHandler
    public void onGameStateChange(UhcStartingEvent e) {
        List<UhcPlayer> players = new ArrayList<>(getPlayersManager().getPlayersList());

        UhcTeam team1 = null;
        UhcTeam team2 = null;

        VersionUtils.getVersionUtils().setGameRuleValue(
                Bukkit.getWorld(GameManager.getGameManager().getConfiguration().getOverworldUuid()),
                "maxEntityCramming", 1000);
        // dont do it if its only 2 players, although this should never happen naturally

        while (!players.isEmpty()) {
            int r = (int) (Math.random() * players.size());
            UhcPlayer player1 = players.get(r);
            players.remove(r);

            r = (int) (Math.random() * players.size());
            UhcPlayer player2 = players.get(r);
            players.remove(r);

            if (team1 == null) {
                team1 = player1.getTeam();
            } else {
                player1.setTeam(team1);
                team1.getMembers().add(player1);
            }
            if (team2 == null) {
                team2 = player2.getTeam();
            } else {
                player2.setTeam(team2);
                team2.getMembers().add(player2);
            }
        }
    }

}