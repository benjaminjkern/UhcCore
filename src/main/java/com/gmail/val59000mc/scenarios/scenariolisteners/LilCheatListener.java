package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.UniversalMaterial;
import com.gmail.val59000mc.scenarios.Option;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import com.gmail.val59000mc.UhcCore;
import org.bukkit.scheduler.BukkitRunnable;

public class LilCheatListener extends ScenarioListener {

    @Option(key = "time")
    private int time = 60;

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        getGameManager().broadcastInfoMessage("You get " + time + " seconds of Creative!");
        getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player thisPlayer = uhcPlayer.getPlayer();
                thisPlayer.setGameMode(GameMode.CREATIVE);
                thisPlayer.setInvulnerable(true);

            } catch (UhcPlayerNotOnlineException ex) {
                // No gamemode for offline players
            }
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                getGameManager().broadcastInfoMessage("Alright that's enough time!");

                getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
                    try {
                        Player thisPlayer = uhcPlayer.getPlayer();
                        thisPlayer.setGameMode(GameMode.SURVIVAL);
                        thisPlayer.setInvulnerable(false);

                    } catch (UhcPlayerNotOnlineException ex) {
                        // No gamemode for offline players
                    }
                });
            }

        }.runTaskLater(UhcCore.getPlugin(), 20 * time);
    }

}