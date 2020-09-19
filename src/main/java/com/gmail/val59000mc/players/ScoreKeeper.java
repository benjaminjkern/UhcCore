package com.gmail.val59000mc.players;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.gmail.val59000mc.UhcCore;

import org.bukkit.Bukkit;

public class ScoreKeeper {

    private Map<String, Double> cache;
    private final double defaultValue = 50;
    private final double scale = 10;

    private Scanner s;
    private File f;

    public ScoreKeeper() {
        cache = new HashMap<>();
        s = null;
        resetScanner();
    }

    private void resetScanner() {
        try {
            f = new File(UhcCore.getPlugin().getDataFolder(), "userScores.txt");
            f.createNewFile();
            s = new Scanner(f);
        } catch (IOException f) {
            Bukkit.getLogger().warning("[UhcCore] Something went wrong when openning userScores.txt!");
        }
    }

    public double getScore(UhcPlayer p) {
        if (p == null) Bukkit.getLogger().warning("[UhcCore] player is null is null!");

        String name = p.getName();
        if (cache.containsKey(name)) return cache.get(name);

        if (s == null) {
            cache.put(name, defaultValue);
            return defaultValue;
        }

        while (s.hasNextLine()) {
            String[] player = s.nextLine().split(":");

            double score = Double.parseDouble(player[1]);

            if (!cache.containsKey(player[0]))
                // if it finds other players while looking for one in specifics, it adds them to
                // the cache anyways
                cache.put(player[0], score);

            if (player[0].equals(name)) {
                cache.put(name, score);
                return score;
            }
        }

        Bukkit.getLogger().info("[UhcCore] Creating user score for " + name + ". Default value: " + defaultValue + ".");
        cache.put(name, defaultValue);
        return defaultValue;
    }

    public void setScore(UhcPlayer p, double score) {
        // make sure it's in the cache first
        getScore(p);
        String name = p.getName();
        double filteredScore = Math.max(0, score);

        cache.put(name, filteredScore);

        Bukkit.getLogger().info(name + "'s new score is " + filteredScore);
    }

    // write back to disk, should really only be done when plugin is disabled
    public void storeData() {
        Bukkit.getLogger().info("[UhcCore] Storing User Score data.");
        // finish the scanner
        while (s.hasNextLine()) {
            String[] player = s.nextLine().split(":");
            double score = Double.parseDouble(player[1]);

            if (!cache.containsKey(player[0])) cache.put(player[0], score);
        }
        s.close();

        // write everything in the cache
        try {
            FileWriter myWriter = new FileWriter(f, false);
            for (Entry<String, Double> e : cache.entrySet()) { myWriter.write(e.getKey() + ":" + e.getValue() + "\n"); }
            myWriter.close();
        } catch (IOException e) {
            Bukkit.getLogger().warning("[UhcCore] Something went wrong when opening userScores.txt!");
        }

        resetScanner();
        cache.clear();
    }

    private double forward(double x) { return 1 / (1 + Math.pow(10, x / 400)); }

    private double inverse(double x) { return 1000 - 400 * Math.log10(1 / x - 1); }

    public void updateScores(UhcPlayer winner, UhcPlayer loser) {
        double A = inverse(getScore(winner) / 100);
        double B = inverse(getScore(loser) / 100);

        double P = forward(B - A);

        double diff = scale * (P - 1);

        setScore(winner, forward(1000 - A - diff) * 100);
        setScore(loser, forward(1000 - B + diff) * 100);
    }

    public void envDie(UhcPlayer loser) {
        Bukkit.getLogger().info("[UhcCore] " + loser.getName() + " died what a loser");
        double B = inverse(getScore(loser) / 100);
        double P = forward(B - 1000);
        double diff = scale * (P / 100 - 1);
        setScore(loser, forward(1000 - B - diff) * 100);
    }

    public void envWin(UhcPlayer winner) {
        Bukkit.getLogger().info("[UhcCore] " + winner.getName() + " won the game what a loser");
        double A = inverse(getScore(winner) / 100);
        double P = forward(1000 - A);
        double diff = scale * (P - 1);
        setScore(winner, forward(1000 - A + diff) * 100);
    }

}