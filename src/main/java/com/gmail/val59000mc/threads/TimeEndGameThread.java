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

    public TimeEndGameThread(GameManager gm, PlayersManager pm) {
        count = 300;
        task = this;
        this.gm = gm;
        this.pm = pm;
    }

    public void run() {
        if (count > 0) {
            if (gm.getGameState() == GameState.ENDED) return;
            if (count == 300) {
                gm.broadcastInfoMessage("Game ending, regardless of outcome, in 5 minutes.");
                pm.playSoundToAll(UniversalSound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
            } else if (count == 60) {
                gm.broadcastInfoMessage("Game ending in 1 minute.");
                pm.playSoundToAll(UniversalSound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
            } else if (count <= 30 && (count % 10 == 0 || count <= 5)) {
                gm.broadcastInfoMessage("Game ending in " + count + " seconds.");
                pm.playSoundToAll(UniversalSound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
            }
            count--;
            Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20);
            return;
        }
        GameManager.getGameManager().endGame();
    }
}
