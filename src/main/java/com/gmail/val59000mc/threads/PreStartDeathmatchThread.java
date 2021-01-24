
package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;

import org.bukkit.Bukkit;
import org.bukkit.Sound;

public class PreStartDeathmatchThread implements Runnable {

    public static PreStartDeathmatchThread task;
    private int time;

    public PreStartDeathmatchThread(int time) {
        task = this;
        this.time = time;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (time > 0) {
                if (time % 10 == 0 || time <= 10) {
                    player.sendTitle("", "\u00a7fDeathmatch Starting in: \u00a7d" + time);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1, 0);
                }
            } else {
                player.sendTitle("", "\u00a7dDeathmatch Started!");
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1, 1);
            }
        });
        if (time > 0) {
            Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20);
            time--;
        } else {
            GameManager.getGameManager().startDeathmatch();
        }

    }

}
