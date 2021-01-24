package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartingEvent;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.scoreboard.ScoreboardType;

import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.*;

public class DuosListener extends ScenarioListener {
    @Override
    public void onEnable() {
        List<String> scoreboardList;
        if (!isActivated(Scenario.SLAYER)) {
            scoreboardList = Arrays.asList("&fTime: &d%time%", "&fBorder: &d%border%", "&fPlayers left: &d%alive%",
                    "&fTeams left: &d%teamAlive%", "&fTeam size: &d%teammatesAlive%", "&fTeam: &d%teamColor%",
                    "&fKills: &d%kills%", "&fScenarios:", "&d%scenarios%", "", "&fPlayer Rating:", "&6%userScore%");
        } else {
            scoreboardList = Arrays.asList("&fTime left: &d%timeLeft%", "&fBorder: &d%border%", "&fPlayers: &d%alive%",
                    "&fTeam: &d%teamColor%", "&fTop: &d%topTeam%", "&d%topTeamKills% kills",
                    "&fTeam kills: &d%teamKills%", "&fYour Kills: &d%kills%", "&fYour Deaths: &d%deaths%",
                    "&fScenarios:", "&d%scenarios%", "", "&fPlayer Rating:", "&6%userScore%");
        }

        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.PLAYING,
                scoreboardList);
        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.DEATHMATCH,
                scoreboardList);
        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.SPECTATING,
                scoreboardList);
    }

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        List<UhcPlayer> players = new ArrayList<>(getPlayersManager().getOnlinePlayingPlayers());

        Bukkit.getLogger().info(players.size() + " STARTED");
        if (players.size() <= 2) return;

        if (players.size() % 2 == 1) {
            players.forEach(uhcPlayer -> {
                if (uhcPlayer.getTeam().getMembers().size() < 2) {
                    try {
                        uhcPlayer.getPlayer().getInventory().addItem(new ItemStack(Material.TOTEM_OF_UNDYING));
                    } catch (UhcPlayerNotOnlineException u) {}
                }
            });
        }
    }

    @EventHandler
    public void onGameStateChange(UhcStartingEvent e) {
        List<UhcPlayer> players = new ArrayList<>(getPlayersManager().getPlayersList());

        Bukkit.getLogger().info(players.size() + " STARTING");
        // dont do it if its only 2 players, although this should never happen naturally
        if (players.size() <= 2) return;

        while (players.size() > 1) {
            int r = (int) (Math.random() * players.size());
            UhcPlayer player1 = players.get(r);
            players.remove(r);

            r = (int) (Math.random() * players.size());
            UhcPlayer player2 = players.get(r);
            players.remove(r);

            player2.setTeam(player1.getTeam());
            player1.getTeam().getMembers().add(player2);
        }
    }

}