package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.utils.UniversalSound;

import org.bukkit.Bukkit;

public class TimeEndGameThread implements Runnable {

    int count;
    TimeEndGameThread task;
    GameManager gm;
    PlayersManager pm;
    boolean sendMessage;

    public TimeEndGameThread(GameManager gm, PlayersManager pm, boolean sendMessage) {
        count = 300;
        task = this;
        this.gm = gm;
        this.pm = pm;
        this.sendMessage = sendMessage;
    }

    public void run() {
        if (count > 0) {
            if (gm.getGameState() == GameState.ENDED) return;
            if (count == 300) {
                if (sendMessage) gm.broadcastInfoMessage("Game ending, regardless of outcome, in 5 minutes.");
                Bukkit.getOnlinePlayers()
                        .forEach(player -> player.sendTitle("", "Game ending in \u00a7d5 \u00a7fminutes."));
                pm.playSoundToAll(UniversalSound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
            } else if (count == 240) {
                if (!sendMessage) {
                    Bukkit.getOnlinePlayers()
                            .forEach(player -> player.sendTitle("", "Game ending in \u00a7d4 \u00a7fminutes."));
                    pm.playSoundToAll(UniversalSound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
                }
            } else if (count == 180) {
                if (!sendMessage) {
                    Bukkit.getOnlinePlayers()
                            .forEach(player -> player.sendTitle("", "Game ending in \u00a7d3 \u00a7fminutes."));
                    pm.playSoundToAll(UniversalSound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
                }
            } else if (count == 120) {
                if (!sendMessage) {
                    Bukkit.getOnlinePlayers()
                            .forEach(player -> player.sendTitle("", "Game ending in \u00a7d2 \u00a7fminutes."));
                    pm.playSoundToAll(UniversalSound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
                }
            } else if (count == 60) {
                if (sendMessage) gm.broadcastInfoMessage("Game ending in 1 minute.");
                Bukkit.getOnlinePlayers()
                        .forEach(player -> player.sendTitle("", "Game ending in \u00a7d1 \u00a7fminute."));
                pm.playSoundToAll(UniversalSound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
            } else if (count <= 30 && (count % 10 == 0 || count <= 5)) {
                if (sendMessage) gm.broadcastInfoMessage("Game ending in " + count + " seconds.");
                Bukkit.getOnlinePlayers()
                        .forEach(player -> player.sendTitle("", "Game ending in \u00a7d" + count + " \u00a7fseconds."));
                pm.playSoundToAll(UniversalSound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
            }
            count--;
            Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20);
            return;
        }
        GameManager.getGameManager().endGame();
    }
}
