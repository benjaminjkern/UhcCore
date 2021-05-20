package com.gmail.val59000mc.scoreboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gmail.val59000mc.game.GameManager;

import org.bukkit.Bukkit;

public class SuperLayout {

    private Set<String> waiting;
    private Set<String> playing;
    private Set<String> spectating;

    private Set<String> waitingTeams;
    private Set<String> playingTeams;
    private Set<String> spectatingTeams;

    private boolean teamsOn;

    public SuperLayout() {
        waiting = new HashSet<>();
        playing = new HashSet<>();
        spectating = new HashSet<>();
        waitingTeams = new HashSet<>();
        playingTeams = new HashSet<>();
        spectatingTeams = new HashSet<>();

        // default settings
        addToLayoutSet("waiting", null, "totalPlayers", "willAddBots", "teamColor");
        addToLayoutSet("spectating", null, "time", "scenarios", "border", "players");
        addToLayoutSet("playing", null, "time", "coords", "scenarios", "kills", "border", "players");
        addToLayoutSet("playing", true, "team", "teamKills", "teamAlive", "teammates");
    }

    public void setTeams(boolean teamsOn) {
        this.teamsOn = teamsOn;
        for (ScoreboardType s : mapToLayout("waiting")) loadLayout(s);
        for (ScoreboardType s : mapToLayout("spectating")) loadLayout(s);
        for (ScoreboardType s : mapToLayout("playing")) loadLayout(s);
    }

    public void loadLayout(ScoreboardType scoreboardType) { loadLayout(scoreboardType, true); }

    public void loadLayout(ScoreboardType scoreboardType, boolean includeSpaces) {
        if (scoreboardType == null) {
            loadLayout(ScoreboardType.PLAYING, includeSpaces);
            loadLayout(ScoreboardType.SPECTATING, includeSpaces);
            loadLayout(ScoreboardType.DEATHMATCH, includeSpaces);
            loadLayout(ScoreboardType.SPECTATING, includeSpaces);
            return;
        }
        Set<String> source = getSet(scoreboardType, teamsOn);

        List<String> lines = new ArrayList<>();

        if (source.contains("totalPlayers")) {
            lines.add("&fPlayers:");
            lines.add("&d%online% &5/ &d%maxPlayers%");
        }
        if (source.contains("willAddBots")) lines.add("&7(Will add %bots% bots)");
        if (source.contains("teamColor")) {
            if (includeSpaces) lines.add("");
            lines.add("&fTeam Color:");
            lines.add("&d%teamColor%");
        }

        if (source.contains("time")) lines.add("&fTime: &d%time%");
        if (source.contains("timeLeft")) lines.add("&fTime Left: &d%timeLeft%");
        if (source.contains("coords")) lines.add("&fCoordinates: &d%xCoordinate%&f, &d%zCoordinate%");
        if (source.contains("border")) lines.add("&fWall size: &d%border%");
        if (includeSpaces) lines.add("");
        if (source.contains("players")) lines.add("&fPlayers: &d%alive%");
        if (source.contains("teams")) lines.add("&fTeams: &d%teamAlive%");
        if (source.contains("team")) lines.add("&fTeam: &d%teamColor%");
        if (source.contains("teammates")) lines.add("&fTeammates: &d%teammatesAlive%");
        if (source.contains("topTeam")) {
            lines.add("&fTop: &d%topTeam%");
            lines.add("&d%topTeamKills% kills");
        }
        if (source.contains("teamKills")) lines.add("&fTeam kills: &d%teamKills%");
        if (source.contains("top")) {
            lines.add("&fTop: &d%top%");
            lines.add("&d%topKills% kills");
        }
        if (source.contains("king")) {
            lines.add("&fKing:");
            lines.add("&d%king%");
        }
        if (source.contains("kills")) lines.add("&fYour Kills: &d%kills%");
        if (source.contains("deaths")) lines.add("&fYour Deaths: &d%deaths%");
        if (source.contains("scenarios")) {
            lines.add("&fScenarios:");
            lines.add("&d%scenarios%");
        }
        if (includeSpaces) lines.add("");
        lines.add("&fPlayer Rating:");
        lines.add("&6%userScore%");

        if (includeSpaces && lines.size() > 15) {
            loadLayout(scoreboardType, false);
            return;
        }

        try {
            GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(scoreboardType, lines);
        } catch (NullPointerException e) {

        }
    }

    public Set<String> getSet(ScoreboardType scoreboardType, boolean teams) {
        switch (scoreboardType) {
            case WAITING:
                return teams ? waitingTeams : waiting;
            case PLAYING:
            case DEATHMATCH:
                return teams ? playingTeams : playing;
            case SPECTATING:
                return teams ? spectatingTeams : spectating;
        }

        throw new IllegalArgumentException("ScoreboardType " + scoreboardType + " was not accepted!");
    }

    public Set<String> getSet(String setId, boolean teams) {
        switch (setId.toLowerCase()) {
            case "waiting":
                return teams ? waitingTeams : waiting;
            case "playing":
                return teams ? playingTeams : playing;
            case "spectating":
                return teams ? spectatingTeams : spectating;
        }

        throw new IllegalArgumentException("Set ID " + setId + " was not accepted!");
    }

    public void addToLayoutSet(String setId, Boolean teams, String... linesToAdd) {
        if (setId == null) {
            addToLayoutSet("waiting", teams, linesToAdd);
            addToLayoutSet("playing", teams, linesToAdd);
            addToLayoutSet("spectating", teams, linesToAdd);
            return;
        }

        if (teams == null) {
            addToLayoutSet(setId, true, linesToAdd);
            addToLayoutSet(setId, false, linesToAdd);
            return;
        }

        Set<String> set = getSet(setId, teams);
        for (String s : linesToAdd) set.add(s);

        for (ScoreboardType s : mapToLayout(setId)) loadLayout(s);
    }

    public void removeFromLayoutSet(String setId, Boolean teams, String... linesToAdd) {
        if (setId == null) {
            removeFromLayoutSet("waiting", teams, linesToAdd);
            removeFromLayoutSet("playing", teams, linesToAdd);
            removeFromLayoutSet("spectating", teams, linesToAdd);
            return;
        }

        if (teams == null) {
            removeFromLayoutSet(setId, true, linesToAdd);
            removeFromLayoutSet(setId, false, linesToAdd);
            return;
        }

        Set<String> set = getSet(setId, teams);
        for (String s : linesToAdd) set.remove(s);

        for (ScoreboardType s : mapToLayout(setId)) loadLayout(s);
    }

    public void replaceInLayoutSet(String setId, Boolean teams, String from, String to) {
        if (setId == null) {
            replaceInLayoutSet("waiting", teams, from, to);
            replaceInLayoutSet("playing", teams, from, to);
            replaceInLayoutSet("spectating", teams, from, to);
            return;
        }

        if (teams == null) {
            replaceInLayoutSet(setId, true, from, to);
            replaceInLayoutSet(setId, false, from, to);
            return;
        }

        removeFromLayoutSet(setId, teams, from);
        addToLayoutSet(setId, teams, to);

        for (ScoreboardType s : mapToLayout(setId)) loadLayout(s);
    }

    private ScoreboardType[] mapToLayout(String setId) {
        switch (setId.toLowerCase()) {
            case "waiting":
                return new ScoreboardType[] { ScoreboardType.WAITING };
            case "playing":
                return new ScoreboardType[] { ScoreboardType.PLAYING, ScoreboardType.DEATHMATCH };
            case "spectating":
                return new ScoreboardType[] { ScoreboardType.SPECTATING };
        }

        throw new IllegalArgumentException("Set ID " + setId + " was not accepted!");
    }

}