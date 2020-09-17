package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.UniversalMaterial;
import com.gmail.val59000mc.scenarios.Option;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.Location;
import org.bukkit.Material;

public class FlyHighListener extends ScenarioListener{

    @Option(key = "height")
    private int height = 500;

    @EventHandler
    public void onPetDamage(EntityDamageEvent e) {
        // all pets also get free no fall damage
        if (e.getEntity() instanceof Tameable) {
            if (((Tameable) e.getEntity()).getOwner() == null) return;
            if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {

            if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                e.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onGameStart(UhcStartedEvent e){
        getPlayersManager().getOnlinePlayingPlayers().forEach(uhcPlayer -> {
            try{
                Player thisPlayer = uhcPlayer.getPlayer();
                thisPlayer.getInventory().setChestplate(UniversalMaterial.ELYTRA.getStack());
                if (thisPlayer.getLocation().getBlockY() > thisPlayer.getWorld().getHighestBlockYAt(thisPlayer.getLocation()))
                    thisPlayer.teleport(thisPlayer.getLocation().add(0, height, 0));
                else {
                    Location loc = thisPlayer.getEyeLocation().add(0,1,0);
 
                    while(loc.getY() < thisPlayer.getLocation().getBlockY() + height) {
                        if(loc.getBlock().getType() != Material.AIR) {
                            loc.add(0,-2,0);
                            thisPlayer.teleport(loc);
                            break;
                        }
                        loc.add(0,1,0);
                    }
                }
            }catch (UhcPlayerNotOnlineException ex){
                // No elytra for offline players.
            }
        });
    }

}