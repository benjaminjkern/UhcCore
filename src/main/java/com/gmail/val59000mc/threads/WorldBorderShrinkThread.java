package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.scenarios.Scenario;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.util.Vector;

public class WorldBorderShrinkThread implements Runnable {

    private long timer;

    public static boolean enabled;

    public static boolean isShrinking;

    private static WorldBorderShrinkThread task;
    private long timeToShrink;
    private final int endSize;
    private final long interval;
    private final long waitPeriod;

    private Vector toMove;

    public WorldBorderShrinkThread(int endSize, long timeToShrink, long interval, long waitPeriod) {
        this.endSize = GameManager.getGameManager().getScenarioManager().isActivated(Scenario.SLAYER)
                || GameManager.getGameManager().getScenarioManager().isActivated(Scenario.POLITICS) ? 50 : endSize;
        this.timeToShrink = 20 * timeToShrink;
        this.interval = 20 * interval;
        this.waitPeriod = 20 * waitPeriod;
        enabled = true;
        isShrinking = false;

        BossBar bar = GameManager.getGameManager().getBossBar();
        bar.setTitle("\u00a74\u00a7lBorder is Shrinking...");
        bar.setColor(BarColor.RED);
        bar.setVisible(true);

        task = this;

        timer = 0;
    }

    private double getNewSize(double currentSize) {
        if (timeToShrink == 0) return currentSize;
        return currentSize - (currentSize - endSize) * (interval + waitPeriod) / timeToShrink;
    }

    @Override
    public void run() {
        BossBar bar = GameManager.getGameManager().getBossBar();
        if (!UhcCore.getPlugin().isEnabled() || GameManager.getGameManager().getGameState() == GameState.DEATHMATCH
                || GameManager.getGameManager().getGameState() == GameState.ENDED
                || PreStartDeathmatchThread.task != null || !enabled) {
            bar.setVisible(false);
            return;
        }

        World overworld = Bukkit.getWorld(GameManager.getGameManager().getConfiguration().getOverworldUuid());
        WorldBorder overworldBorder = overworld.getWorldBorder();
        World nether = Bukkit.getWorld(GameManager.getGameManager().getConfiguration().getNetherUuid());

        if (timer == 0) {
            isShrinking = true;
            double newSize = getNewSize(overworldBorder.getSize() / 2.);
            overworldBorder.setSize(2 * newSize, interval / 20);
            if (nether != null) {
                WorldBorder netherBorder = nether.getWorldBorder();
                netherBorder.setSize(newSize, interval / 20);
            }

            Location newCenter = PlayersManager.newRandomLocation(overworld, overworldBorder.getSize() / 10. + 20);
            toMove = newCenter.toVector().subtract(overworld.getWorldBorder().getCenter().toVector())
                    .multiply(1 / (double) interval);

            timeToShrink -= interval + waitPeriod;
        }

        timer++;

        if (timer < interval) {
            bar.setTitle("\u00a74\u00a7lThe border is moving...");
            bar.setColor(BarColor.RED);
            bar.setProgress((interval - timer) / (double) interval);
            bar.setVisible(true);

            overworldBorder.setCenter(overworldBorder.getCenter().clone().add(toMove));
            if (nether != null) {
                WorldBorder netherBorder = nether.getWorldBorder();
                netherBorder.setCenter(netherBorder.getCenter().clone().add(toMove.clone().multiply(0.5)));
            }
        } else if (timer < interval + waitPeriod) {
            isShrinking = false;
            bar.setTitle("\u00a7e\u00a7lThe border is not currently moving.");
            bar.setColor(BarColor.YELLOW);
            bar.setProgress((timer - interval) / (double) waitPeriod);
            bar.setVisible(true);
        } else {
            timer = 0;
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(UhcCore.getPlugin(), task, 1);
    }

}