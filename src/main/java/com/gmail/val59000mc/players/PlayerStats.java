package com.gmail.val59000mc.players;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerStats {
    public String player;
    public double rating;
    int playerKills;
    int botKills;
    int mobKills;
    int animalKills;
    int playerDeaths;
    int envDeaths;
    public int wins;
    int games;
    String nemesis;
    String lastKilledBy;
    int nemesisKills;
    int lastKilledByKills;
    long lastSeen;
    int rank;

    public PlayerStats(String player) {
        if (player != null) this.player = player.toLowerCase();
        else this.player = null;
        rating = 50;
        playerKills = botKills = mobKills = animalKills = playerDeaths = envDeaths = wins = games = nemesisKills = lastKilledByKills = 0;
        nemesis = lastKilledBy = null;
    }

    public void reset() {
        rating = 50;
        playerKills = botKills = mobKills = animalKills = playerDeaths = envDeaths = wins = games = nemesisKills = lastKilledByKills = 0;
        nemesis = lastKilledBy = null;
    }

    public static PlayerStats newFromParse(String... fields) {
        if (fields.length < 3) throw new IllegalArgumentException("Requires at least 2 inputs!");
        PlayerStats ps = new PlayerStats(fields[1]);
        ps.rating = Double.parseDouble(fields[2]);
        if (fields.length == 3) return ps;
        if (fields.length < 15) throw new IllegalArgumentException("OPE");

        ps.playerKills = Integer.parseInt(fields[3]);
        ps.botKills = Integer.parseInt(fields[4]);
        ps.mobKills = Integer.parseInt(fields[5]);
        ps.animalKills = Integer.parseInt(fields[6]);
        ps.playerDeaths = Integer.parseInt(fields[7]);
        ps.envDeaths = Integer.parseInt(fields[8]);
        ps.wins = Integer.parseInt(fields[9]);
        ps.games = Integer.parseInt(fields[10]);
        ps.nemesis = fields[11];
        ps.lastKilledBy = fields[12];
        ps.nemesisKills = Integer.parseInt(fields[13]);
        ps.lastKilledByKills = Integer.parseInt(fields[14]);
        return ps;
    }

    public void overwrite(PlayerStats newStats) {

        if (player != null && !player.equals(newStats.player)) return; // should never happen

        try {
            Player p = Bukkit.getPlayerExact(newStats.player);
            if (nemesis.equals("null")) nemesis = null;
            if (newStats.nemesis.equals("null")) newStats.nemesis = null;

            if (nemesis == null && newStats.nemesis != null)
                p.sendMessage("\u00a7c" + newStats.nemesis + " \u00a7fis now your nemesis!");

            if (nemesis != null && newStats.nemesis == null)
                p.sendMessage("\u00a7c" + nemesis + " \u00a7fis no longer your nemesis!");

            if (nemesis != null && newStats.nemesis != null && !nemesis.equals(newStats.nemesis))
                p.sendMessage("\u00a7c" + newStats.nemesis + " \u00a7fis your new nemesis!");
        } catch (NullPointerException | IllegalArgumentException e) {}

        player = newStats.player;
        rating = newStats.rating;
        playerKills = newStats.playerKills;
        botKills = newStats.botKills;
        mobKills = newStats.mobKills;
        animalKills = newStats.animalKills;
        playerDeaths = newStats.playerDeaths;
        envDeaths = newStats.envDeaths;
        wins = newStats.wins;
        games = newStats.games;
        nemesis = newStats.nemesis;
        lastKilledBy = newStats.lastKilledBy;
        nemesisKills = newStats.nemesisKills;
        lastKilledByKills = newStats.lastKilledByKills;
        lastSeen = newStats.lastSeen;
    }

    public String toString() {
        return player + ":" + rating + ":" + playerKills + ":" + botKills + ":" + mobKills + ":" + animalKills + ":"
                + playerDeaths + ":" + envDeaths + ":" + wins + ":" + games + ":" + nemesis + ":" + lastKilledBy + ":"
                + nemesisKills + ":" + lastKilledByKills + ":" + lastSeen;
    }

}