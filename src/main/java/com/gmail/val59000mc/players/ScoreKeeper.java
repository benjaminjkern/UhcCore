package com.gmail.val59000mc.players;

import java.util.*;
import java.util.Map.Entry;

import com.gmail.val59000mc.exceptions.UhcPlayerDoesntExistException;
import com.gmail.val59000mc.game.GameManager;

import org.bukkit.Bukkit;

public class ScoreKeeper {

    private Map<String, Double> cache;
    private final double defaultValue = 50;
    private final double scale = 10;
    private final double factorOfTen = 400;
    private GameManager gm;

    public ScoreKeeper(GameManager gm) {
        cache = new HashMap<>();
        this.gm = gm;
    }

    public double getScore(String name, boolean create) throws UhcPlayerDoesntExistException {

        if (cache.containsKey(name)) return cache.get(name);

        gm.sendInfoToServer("RATINGREQUEST:" + name, false);

        // dont save to cache unless the server explicitly said to
        return defaultValue;
    }

    public double getScore(UhcPlayer p) {
        try {
            return getScore(p.getName(), true);
        } catch (UhcPlayerDoesntExistException e) {
            // shouldnt ever happen
            return -1;
        }
    }

    public double getScore(String name) throws UhcPlayerDoesntExistException { return getScore(name, false); }

    public double setScore(String name, double score) throws UhcPlayerDoesntExistException {
        // make sure it's in the cache first
        getScore(name);
        double filteredScore = Math.max(0, Math.min(100, score));

        cache.put(name, filteredScore);

        try {
            gm.getPlayersManager().getUhcPlayer(name).sendMessage(
                    "Your Player Rating has been updated to \u00a76" + String.format("%.2f", filteredScore));
        } catch (UhcPlayerDoesntExistException e) {}
        Bukkit.getLogger().info(name + "'s new score is " + filteredScore);
        return filteredScore;
    }

    public double setScore(UhcPlayer p, double score) {
        try {
            return setScore(p.getName(), score);
        } catch (UhcPlayerDoesntExistException e) {
            return -1;
        }
    }

    public double addScore(String name, double score) throws UhcPlayerDoesntExistException {
        return setScore(name, score + getScore(name));
    }

    public double addScore(UhcPlayer p, double score) {
        try {
            return addScore(p.getName(), score);
        } catch (UhcPlayerDoesntExistException e) {
            return -1;
        }
    }

    public double getScoreI(String name) throws UhcPlayerDoesntExistException { return inverse(getScore(name)); }

    public double getScoreI(UhcPlayer p) { return inverse(getScore(p)); }

    public double setScoreI(String name, double scoreI) throws UhcPlayerDoesntExistException {
        return setScore(name, forward(scoreI));
    }

    public double setScoreI(UhcPlayer p, double scoreI) { return setScore(p, forward(scoreI)); }

    public double addScoreI(String name, double scoreI) throws UhcPlayerDoesntExistException {
        return setScore(name, forward(getScoreI(name) + scoreI));
    }

    public double addScoreI(UhcPlayer p, double scoreI) { return setScore(p, forward(getScoreI(p) + scoreI)); }

    // write back to disk, should really only be done when plugin is disabled
    public void storeData() {
        for (Entry<String, Double> e : cache.entrySet()) {
            if (e.getValue() != defaultValue) gm.sendInfoToServer("RATING:" + e.getKey() + ":" + e.getValue(), false);
        }
        cache.clear();
    }

    private double Prob(double x) { return 1 / (1 + Math.pow(10, x / factorOfTen)); }

    private double forward(double x) { return 100 * Prob(1000 - x); }

    private double inverse(double x) { return 1000 - factorOfTen * Math.log10(100 / x - 1); }

    // these are the three that are called by the game

    public void updateScores(UhcPlayer winner, UhcPlayer loser) {
        // don't do anything if the two are the same
        if (winner.getName().equals(loser.getName())) return;

        double A = getScoreI(winner);
        double B = getScoreI(loser);

        double P = Prob(B - A);

        double diff = scale * (1 - P);

        gm.sendInfoToServer("RATING:" + winner.getName() + ":" + setScore(winner, forward(A + diff)), false);
        gm.sendInfoToServer("RATING:" + loser.getName() + ":" + setScore(loser, forward(B - diff)), false);
    }

    public void envDie(UhcPlayer loser) {
        gm.sendInfoToServer("RATING:" + loser.getName() + ":"
                + setScore(loser, forward(getScoreI(loser) - scale * getScore(loser) / 100)), false);
    }

    public void envWin(UhcPlayer winner) {
        gm.sendInfoToServer("RATING:" + winner.getName() + ":"
                + setScore(winner, forward(getScoreI(winner) + scale * getScore(winner) / 100)), false);
    }

}