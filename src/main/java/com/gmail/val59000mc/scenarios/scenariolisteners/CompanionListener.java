package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;

public class CompanionListener extends ScenarioListener {

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {

        getPlayersManager().getAllPlayingPlayers().forEach(uhcPlayer -> {
            try {
                Player thisPlayer = uhcPlayer.getPlayer();
                Wolf baby = (Wolf) thisPlayer.getWorld().spawnEntity(thisPlayer.getLocation(), EntityType.WOLF);
                baby.setCustomName(getGameManager().getDogNameGenerator().newName());
                baby.setOwner(thisPlayer);
                baby.setCollarColor(getDyeColor(uhcPlayer.getTeam().getColor()));
                baby.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000, 1));
            } catch (UhcPlayerNotOnlineException ex) {
                // No elytra for offline players.
            }
        });
    }

    private DyeColor getDyeColor(String color) {
        if (color.equals(ChatColor.RED.toString())) return DyeColor.RED;
        if (color.equals(ChatColor.BLUE.toString())) return DyeColor.LIGHT_BLUE;
        if (color.equals(ChatColor.DARK_GREEN.toString())) return DyeColor.GREEN;
        if (color.equals(ChatColor.DARK_AQUA.toString())) return DyeColor.BLUE;
        if (color.equals(ChatColor.DARK_PURPLE.toString())) return DyeColor.PURPLE;
        if (color.equals(ChatColor.YELLOW.toString())) return DyeColor.YELLOW;
        if (color.equals(ChatColor.GOLD.toString())) return DyeColor.ORANGE;
        if (color.equals(ChatColor.GREEN.toString())) return DyeColor.LIME;
        if (color.equals(ChatColor.AQUA.toString())) return DyeColor.CYAN;
        if (color.equals(ChatColor.LIGHT_PURPLE.toString())) return DyeColor.PINK;
        return DyeColor.BLACK;
    }

}