package com.gmail.val59000mc.scoreboard;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.configuration.VaultManager;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.*;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.scenariolisteners.PoliticsListener;
import com.gmail.val59000mc.scenarios.scenariolisteners.SilentNightListener;
import com.gmail.val59000mc.scenarios.scenariolisteners.SlayerListener;
import com.gmail.val59000mc.scoreboard.placeholders.BlocksToTeamLeader;
import com.gmail.val59000mc.scoreboard.placeholders.ScenariosPlaceholder;
import com.gmail.val59000mc.scoreboard.placeholders.TeamMembersPlaceholder;
import com.gmail.val59000mc.scoreboard.placeholders.TimersPlaceholder;
import com.gmail.val59000mc.threads.UpdateScoreboardThread;
import com.gmail.val59000mc.threads.WorldBorderShrinkThread;
import com.gmail.val59000mc.utils.TimeUtils;
import com.gmail.val59000mc.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreboardManager {

    private final ScoreboardLayout scoreboardLayout;
    private final List<Placeholder> placeholders;

    public ScoreboardManager() {
        scoreboardLayout = new ScoreboardLayout();
        scoreboardLayout.loadFile();
        placeholders = new ArrayList<>();
        placeholders.add(new BlocksToTeamLeader());
        placeholders.add(new TeamMembersPlaceholder());
        placeholders.add(new ScenariosPlaceholder());
        placeholders.add(new TimersPlaceholder());
    }

    public ScoreboardLayout getScoreboardLayout() { return scoreboardLayout; }

    public void setUpPlayerScoreboard(UhcPlayer scoreboardPlayer) {

        Scoreboard scoreboard = scoreboardPlayer.getScoreboard();

        GameManager gm = GameManager.getGameManager();
        PlayersManager pm = gm.getPlayersManager();
        MainConfiguration cfg = gm.getConfiguration();

        // add teams for no flicker scoreboard
        for (int i = 0; i < 15; i++) {
            Team team = scoreboard.registerNewTeam(getScoreboardLine(i));
            team.addEntry(getScoreboardLine(i));
        }

        // setup teams
        if (!cfg.getUseTeamColors()) {

            Objective healthTab = scoreboard.getObjective("health_tab");
            Objective healthBelowName = scoreboard.getObjective(ChatColor.RED + "\u2764");

            Team friends = scoreboard.registerNewTeam("friends");
            Team enemies = scoreboard.registerNewTeam("enemies");
            friends.setPrefix(ChatColor.GREEN + "");
            enemies.setPrefix(ChatColor.RED + "");
            friends.setSuffix(ChatColor.RESET + "");
            enemies.setSuffix(ChatColor.RESET + "");

            if (cfg.getDisableEnemyNametags()) {
                VersionUtils.getVersionUtils().setTeamNameTagVisibility(enemies, false);
            }

            Team spectators = scoreboard.registerNewTeam("spectators");
            spectators.setPrefix(ChatColor.GRAY + "");
            spectators.setSuffix(ChatColor.RESET + "");

            // Putting players in colored teams
            for (UhcPlayer uhcPlayer : pm.getPlayersList()) {

                try {
                    if (healthTab != null) {
                        healthTab.getScore(uhcPlayer.getPlayer().getName())
                                .setScore((int) uhcPlayer.getPlayer().getHealth());
                    }
                    if (healthBelowName != null) {
                        healthBelowName.getScore(uhcPlayer.getPlayer().getName())
                                .setScore((int) uhcPlayer.getPlayer().getHealth());
                    }

                    if (uhcPlayer.getState().equals(PlayerState.DEAD)
                            || uhcPlayer.getState().equals(PlayerState.WAITING)) {
                        spectators.addEntry(uhcPlayer.getPlayer().getName());
                    } else if (uhcPlayer.isInTeamWith(scoreboardPlayer)) {
                        friends.addEntry(uhcPlayer.getPlayer().getName());
                    } else {
                        enemies.addEntry(uhcPlayer.getPlayer().getName());
                    }
                } catch (UhcPlayerNotOnlineException ex) {
                    // No health display for offline players.
                }

            }

            updatePlayerTab(scoreboardPlayer);

        } else {

            // Team colors
            Objective healthTab = scoreboard.getObjective("health_tab");
            Objective healthBelowName = scoreboard.getObjective(ChatColor.RED + "\u2764");

            Team spectators = scoreboard.registerNewTeam("spectators");
            spectators.setPrefix(ChatColor.GRAY + "");
            spectators.setSuffix(ChatColor.RESET + "");

            for (UhcTeam uhcTeam : gm.getTeamManager().getUhcTeams()) {

                if (uhcTeam.contains(scoreboardPlayer)) {

                    Team team = scoreboard.registerNewTeam("0" + uhcTeam.getTeamNumber());
                    team.setPrefix(uhcTeam.getPrefix());
                    team.setSuffix(ChatColor.RESET + "");

                    for (UhcPlayer member : uhcTeam.getMembers()) {

                        try {
                            if (healthTab != null) {
                                healthTab.getScore(member.getPlayer().getName())
                                        .setScore((int) member.getPlayer().getHealth());
                            }
                            if (healthBelowName != null) {
                                healthBelowName.getScore(member.getPlayer().getName())
                                        .setScore((int) member.getPlayer().getHealth());
                            }

                            if (member.getState().equals(PlayerState.DEAD)) {
                                // spec team
                                spectators.addEntry(member.getPlayer().getName());
                            } else {
                                team.addEntry(member.getPlayer().getName());
                            }
                        } catch (UhcPlayerNotOnlineException ex) {
                            // No health display for offline players.
                        }
                    }

                } else {

                    Team team = scoreboard.registerNewTeam("" + uhcTeam.getTeamNumber());
                    team.setPrefix(uhcTeam.getPrefix());
                    team.setSuffix(ChatColor.RESET + "");

                    if (gm.getConfiguration().getDisableEnemyNametags()) {
                        VersionUtils.getVersionUtils().setTeamNameTagVisibility(team, false);
                    }

                    for (UhcPlayer member : uhcTeam.getMembers()) {

                        try {
                            if (healthTab != null) {
                                healthTab.getScore(member.getPlayer().getName())
                                        .setScore((int) member.getPlayer().getHealth());
                            }
                            if (healthBelowName != null) {
                                healthBelowName.getScore(member.getPlayer().getName())
                                        .setScore((int) member.getPlayer().getHealth());
                            }

                            if (member.getState().equals(PlayerState.DEAD)) {
                                // spec team
                                spectators.addEntry(member.getPlayer().getName());
                            } else {
                                team.addEntry(member.getPlayer().getName());
                            }
                        } catch (UhcPlayerNotOnlineException ex) {
                            // No health display for offline players.
                        }
                    }
                }
            }

            updatePlayerTab(scoreboardPlayer);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(),
                new UpdateScoreboardThread(gm, scoreboardPlayer), 1L);
    }

    public void updatePlayerTab(UhcPlayer uhcPlayer) {
        GameManager gm = GameManager.getGameManager();

        if (!gm.getConfiguration().getUseTeamColors()) {

            for (UhcPlayer all : gm.getPlayersManager().getPlayersList()) {
                Scoreboard scoreboard = all.getScoreboard();
                if (scoreboard == null) { continue; }

                try {
                    if (uhcPlayer.getState().equals(PlayerState.PLAYING)) {
                        if (all.isInTeamWith(uhcPlayer)) {
                            // add to there friend team
                            scoreboard.getTeam("friends").addEntry(uhcPlayer.getPlayer().getName());
                        } else {
                            // add to enemies team
                            scoreboard.getTeam("enemies").addEntry(uhcPlayer.getPlayer().getName());
                        }
                    } else {
                        // add to spectators team
                        scoreboard.getTeam("spectators").addEntry(uhcPlayer.getPlayer().getName());
                    }
                } catch (UhcPlayerNotOnlineException e) {}

            }

        } else {

            for (UhcPlayer all : gm.getPlayersManager().getPlayersList()) {
                Scoreboard scoreboard = all.getScoreboard();
                if (scoreboard == null) { continue; }

                if (uhcPlayer.getState().equals(PlayerState.PLAYING)
                        || uhcPlayer.getState().equals(PlayerState.WAITING)) {

                    try {
                        if (all.isInTeamWith(uhcPlayer)) {
                            // add to there team with 0 in front

                            String teamName = "0" + uhcPlayer.getTeam().getTeamNumber();

                            if (gm.getScenarioManager().isActivated(Scenario.POLITICS)
                                    && PoliticsListener.getPlayerNode(uhcPlayer).isLeader()) {
                                teamName += "l";
                            }

                            Team team = scoreboard.getTeam(teamName);
                            if (team == null) { team = scoreboard.registerNewTeam(teamName); }
                            team.setPrefix(uhcPlayer.getTeam().getPrefix());
                            if (gm.getScenarioManager().isActivated(Scenario.POLITICS)
                                    && PoliticsListener.getPlayerNode(uhcPlayer).isLeader()) {
                                team.setSuffix("\u00a7e⚜" + ChatColor.RESET + "");
                            } else {
                                team.setSuffix(ChatColor.RESET + "");
                            }
                            team.addEntry(uhcPlayer.getPlayer().getName());

                        } else {
                            // add to normal team

                            String teamName = "" + uhcPlayer.getTeam().getTeamNumber();

                            if (gm.getScenarioManager().isActivated(Scenario.POLITICS)
                                    && PoliticsListener.getPlayerNode(uhcPlayer).isLeader()) {
                                teamName += "l";
                            }

                            Team team = scoreboard.getTeam(teamName);
                            if (team == null) {
                                team = scoreboard.registerNewTeam(teamName);
                                if (gm.getConfiguration().getDisableEnemyNametags()) {
                                    VersionUtils.getVersionUtils().setTeamNameTagVisibility(team, false);
                                }
                            }
                            team.setPrefix(uhcPlayer.getTeam().getPrefix());
                            if (gm.getScenarioManager().isActivated(Scenario.POLITICS)
                                    && PoliticsListener.getPlayerNode(uhcPlayer).isLeader()) {
                                team.setSuffix("\u00a7e⚜" + ChatColor.RESET + "");
                            } else {
                                team.setSuffix(ChatColor.RESET + "");
                            }
                            team.addEntry(uhcPlayer.getPlayer().getName());
                        }
                    } catch (UhcPlayerNotOnlineException e) {}

                } else {
                    // add to no-team team
                    Team team = scoreboard.getTeam("spectators");
                    try {
                        if (team != null) { team.addEntry(uhcPlayer.getPlayer().getName()); }
                    } catch (UhcPlayerNotOnlineException e) {}
                }

            }

            // Change player display name
            if (gm.getConfiguration().getChangeDisplayNames()) {
                try {
                    uhcPlayer.getPlayer().setDisplayName(uhcPlayer.getDisplayName());
                } catch (UhcPlayerNotOnlineException ex) {
                    // Player left while updating tab.
                }
            }
        }
    }

    public String getScoreboardLine(int line) {
        if (line == 0) return ChatColor.UNDERLINE + "" + ChatColor.RESET;
        if (line == 1) return ChatColor.ITALIC + "" + ChatColor.RESET;
        if (line == 2) return ChatColor.BOLD + "" + ChatColor.RESET;
        if (line == 3) return ChatColor.RESET + "" + ChatColor.RESET;
        if (line == 4) return ChatColor.GREEN + "" + ChatColor.RESET;
        if (line == 5) return ChatColor.DARK_GRAY + "" + ChatColor.RESET;
        if (line == 6) return ChatColor.GOLD + "" + ChatColor.RESET;
        if (line == 7) return ChatColor.RED + "" + ChatColor.RESET;
        if (line == 8) return ChatColor.YELLOW + "" + ChatColor.RESET;
        if (line == 9) return ChatColor.WHITE + "" + ChatColor.RESET;
        if (line == 10) return ChatColor.DARK_GREEN + "" + ChatColor.RESET;
        if (line == 11) return ChatColor.BLUE + "" + ChatColor.RESET;
        if (line == 12) return ChatColor.STRIKETHROUGH + "" + ChatColor.RESET;
        if (line == 13) return ChatColor.MAGIC + "" + ChatColor.RESET;
        if (line == 14) return ChatColor.DARK_RED + "" + ChatColor.RESET;
        return null;
    }

    public String translatePlaceholders(String s, UhcPlayer uhcPlayer, Player bukkitPlayer,
            ScoreboardType scoreboardType, long k) {
        try {

            String returnString = s;
            GameManager gm = GameManager.getGameManager();
            MainConfiguration cfg = gm.getConfiguration();

            if (scoreboardType.equals(ScoreboardType.WAITING)) {
                returnString = returnString.replace("%online%", Bukkit.getOnlinePlayers().size() + "")
                        .replace("%needed%", cfg.getMinPlayersToStart() + "");
            }

            if (returnString.contains("%kit%")) {
                if (uhcPlayer.getKit() == null) {
                    returnString = returnString.replace("%kit%", Lang.ITEMS_KIT_SCOREBOARD_NO_KIT);
                } else {
                    returnString = returnString.replace("%kit%", uhcPlayer.getKit().getName());
                }
            }

            if (returnString.contains("%kills%")) {
                returnString = returnString.replace("%kills%", uhcPlayer.kills + "");
            }

            if (returnString.contains("%teamKills%")) {
                returnString = returnString.replace("%teamKills%", uhcPlayer.getTeam().getPlayingKills() + "");
            }

            if (returnString.contains("%teamColor%")) {
                returnString = returnString.replace("%teamColor%", uhcPlayer.getTeam().getPrefix());
            }

            if (returnString.contains("%border%")) {

                int size = (int) bukkitPlayer.getWorld().getWorldBorder().getSize();

                if (size == 30000000) { size = 0; }

                String borderString = size + "";

                int distanceX = (int) (size / 2 - Math.abs(bukkitPlayer.getLocation().getX()
                        - bukkitPlayer.getWorld().getWorldBorder().getCenter().getX()));
                int distanceZ = (int) (size / 2 - Math.abs(bukkitPlayer.getLocation().getZ()
                        - bukkitPlayer.getWorld().getWorldBorder().getCenter().getZ()));

                if (WorldBorderShrinkThread.isShrinking) {
                    if (distanceX == 0 || distanceZ == 0 || size / distanceX >= 50 || size / distanceZ >= 50) {
                        borderString = ChatColor.RED + borderString;
                        if (uhcPlayer.getState() == PlayerState.PLAYING) {
                            bukkitPlayer.sendTitle("",
                                    "\u00a7cYou are " + Math.min(distanceX, distanceZ) + " blocks from the wall!");

                            bukkitPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                        }
                    } else if (size / distanceX >= 10 || size / distanceZ >= 10) {
                        borderString = ChatColor.RED + borderString;

                        if (uhcPlayer.getState() == PlayerState.PLAYING) {
                            bukkitPlayer.sendTitle("", "");
                            bukkitPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                    "\u00a7eYou are " + Math.min(distanceX, distanceZ) + " blocks from the wall"));
                        }
                    }
                    borderString = ChatColor.YELLOW + borderString;
                } else {
                    borderString = ChatColor.GREEN + borderString;
                }

                returnString = returnString.replace("%border%", borderString);
            }

            if (returnString.contains("%ylayer%")) {
                returnString = returnString.replace("%ylayer%", (int) bukkitPlayer.getLocation().getY() + "");
            }

            if (returnString.contains("%xCoordinate%")) {
                returnString = returnString.replace("%xCoordinate%", (int) bukkitPlayer.getLocation().getX() + "");
            }

            if (returnString.contains("%zCoordinate%")) {
                returnString = returnString.replace("%zCoordinate%", (int) bukkitPlayer.getLocation().getZ() + "");
            }

            if (returnString.contains("%deathmatch%")) {
                returnString = returnString.replace("%deathmatch%", gm.getFormatedRemainingTime());
            }

            if (returnString.contains("%time%")) {
                returnString = returnString.replace("%time%", TimeUtils.getFormattedTime(gm.getElapsedTime()));
            }

            if (returnString.contains("%timeLeft%")) {
                long timeLeft = (gm.getConfiguration().getBorderTimeBeforeShrink()
                        + gm.getConfiguration().getBorderTimeToShrink()) - gm.getElapsedTime();
                returnString = returnString.replace("%timeLeft%", TimeUtils.getFormattedTime(timeLeft));
            }

            if (returnString.contains("%pvp%")) {
                long pvp = cfg.getTimeBeforePvp() - gm.getElapsedTime();

                if (pvp < 0) {
                    returnString = returnString.replace("%pvp%", "-");
                } else {
                    returnString = returnString.replace("%pvp%", TimeUtils.getFormattedTime(pvp));
                }
            }

            if (returnString.contains("%alive%")) {
                if (gm.getScenarioManager().isActivated(Scenario.SILENTNIGHT)
                        && ((SilentNightListener) gm.getScenarioManager().getScenarioListener(Scenario.SILENTNIGHT))
                                .isNightMode()) {
                    returnString = returnString.replace("%alive%", "?");
                } else {
                    returnString = returnString.replace("%alive%",
                            gm.getPlayersManager().getAllPlayingPlayers().size() + "");
                }
            }

            if (returnString.contains("%episode%")) {
                returnString = returnString.replace("%episode%", gm.getEpisodeNumber() + "");
            }

            if (returnString.contains("%nextEpisode%")) {
                returnString = returnString.replace("%nextEpisode%",
                        TimeUtils.getFormattedTime(gm.getTimeUntilNextEpisode()) + "");
            }

            if (returnString.contains("%teamAlive%")) {
                returnString = returnString.replace("%teamAlive%",
                        String.valueOf(gm.getTeamManager().getPlayingUhcTeams().size()));
            }

            if (returnString.contains("%playerAlive%")) {
                returnString = returnString.replace("%playerAlive%",
                        String.valueOf(gm.getPlayersManager().getAllPlayingPlayers().size()));
            }

            if (returnString.contains("%playerSpectator%")) {
                returnString = returnString.replace("%playerSpectator%",
                        String.valueOf(gm.getPlayersManager().getOnlineSpectatingPlayers().size()));
            }

            if (returnString.contains("%money%")) {
                returnString = returnString.replace("%money%",
                        String.format("%.2f", VaultManager.getPlayerMoney(bukkitPlayer)));
            }

            if (returnString.contains("%maxPlayers%"))
                returnString = returnString.replace("%maxPlayers%", Bukkit.getMaxPlayers() + "");

            if (returnString.contains("%userScore%")) returnString = returnString.replace("%userScore%", String
                    .format("%.2f", gm.getPlayersManager().getScoreKeeper().getStats(uhcPlayer.getName()).rating));

            if (returnString.contains("%bots%")) {
                returnString = returnString.replace("%bots%",
                        (Bukkit.getMaxPlayers() - Bukkit.getOnlinePlayers().size()) + "");
            }
            if (returnString.contains("%chainOfCommand%")) {
                List<UhcPlayer> chain = PoliticsListener.getPlayerNode(uhcPlayer).chainOfCommand().stream()
                        .map(node -> node.player).collect(Collectors.toList());
                int newK = (int) (k % (long) chain.size());
                returnString = returnString.replace("%chainOfCommand%",
                        !chain.isEmpty() ? ((chain.size() - newK) + ". " + chain.get(newK).getDisplayName()) : "-");
            }

            if (returnString.contains("%topTeam%") || returnString.contains("%topTeamKills%")) {
                List<UhcTeam> top = new ArrayList<>();
                int kills = 0;
                for (UhcTeam team : gm.getTeamManager().getUhcTeams()) {
                    int teamKills = team.getPlayingKills();
                    if (teamKills > kills) {
                        top.clear();
                        kills = teamKills;
                    }
                    if (teamKills >= kills && kills > 0) top.add(team);
                }
                returnString = returnString
                        .replace("%topTeam%", !top.isEmpty() ? top.get((int) (k % (long) top.size())).getPrefix() : "-")
                        .replace("%topTeamKills%", kills + "");
            }

            if (returnString.contains("%top%") || returnString.contains("%topKills%")) {
                List<UhcPlayer> top = new ArrayList<>();
                int kills = 0;
                for (UhcPlayer u : gm.getPlayersManager().getAllPlayingPlayers()) {
                    if (u.kills > kills) {
                        top.clear();
                        kills = u.kills;
                    }
                    if (u.kills >= kills && kills > 0) top.add(u);
                }
                returnString = returnString
                        .replace("%top%",
                                !top.isEmpty() ? top.get((int) (k % (long) top.size())).getDisplayName() : "-")
                        .replace("%topKills%", kills + "");
            }

            if (returnString.contains("%deaths%")) {
                returnString = returnString.replace("%deaths%", SlayerListener.getDeaths(uhcPlayer) + "");
            }

            if (returnString.contains("%teamLeader%")) {
                returnString = returnString.replace("%teamLeader%",
                        PoliticsListener.getPlayerNode(uhcPlayer).getTeamLeader().player.getDisplayName());
            }

            if (returnString.contains("%teammatesAlive%")) {
                returnString = returnString.replace("%teammatesAlive%",
                        uhcPlayer.getTeam().getPlayingMembers().size() + "");
            }

            // Parse custom placeholders
            for (Placeholder placeholder : placeholders) {
                returnString = placeholder.parseString(returnString, uhcPlayer, bukkitPlayer, scoreboardType);
            }

            if (returnString.length() > 32) {
                Bukkit.getLogger().warning("[UhcCore] Scoreboard line is too long: '" + returnString + "'!");
                returnString = "";
            }

            return returnString;
        } catch (Exception e) {
            return s;
        }
    }

    /**
     * Used to register custom placeholders.
     * 
     * @param placeholder The placeholder you want to register.
     */
    public void registerPlaceholder(Placeholder placeholder) { placeholders.add(placeholder); }
}
