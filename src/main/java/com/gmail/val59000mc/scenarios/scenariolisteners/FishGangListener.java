package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.HashMap;
import java.util.Map;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class FishGangListener extends ScenarioListener {

    private Map<UhcPlayer, Integer> lastAirValue;

    public FishGangListener() { lastAirValue = new HashMap<>(); }

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {

        getPlayersManager().getOnlinePlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player player = uhcPlayer.getPlayer();
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 100000, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 100000, 3));
                lastAirValue.put(uhcPlayer, 20 * 15);
            } catch (Exception k) {}
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                getPlayersManager().getOnlinePlayingPlayers().forEach(uhcPlayer -> {
                    try {
                        Player player = uhcPlayer.getPlayer();
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 100000, 3));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 100000, 3));

                        int air = lastAirValue.get(uhcPlayer);
                        Material m = player.getEyeLocation().getBlock().getType();
                        if (m == Material.WATER) {
                            air = Math.min(air + 5, player.getMaximumAir());
                        } else {
                            if (air <= -20) {
                                player.damage(2);
                                air = 0;
                            }
                            air--;
                        }
                        lastAirValue.put(uhcPlayer, air);
                        player.setRemainingAir(air);

                    } catch (Exception k) {}
                });
            }
        }.runTaskTimer(UhcCore.getPlugin(), 0, 1);
    }

}
