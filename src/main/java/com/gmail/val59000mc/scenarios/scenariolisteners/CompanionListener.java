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
        if (color.contains(ChatColor.RED.toString())) return DyeColor.PINK;
        if (color.contains(ChatColor.BLUE.toString())) return DyeColor.LIGHT_BLUE;
        if (color.contains(ChatColor.DARK_GREEN.toString())) return DyeColor.GREEN;
        if (color.contains(ChatColor.DARK_AQUA.toString())) return DyeColor.CYAN;
        if (color.contains(ChatColor.DARK_PURPLE.toString())) return DyeColor.PURPLE;
        if (color.contains(ChatColor.YELLOW.toString())) return DyeColor.YELLOW;
        if (color.contains(ChatColor.GOLD.toString())) return DyeColor.ORANGE;
        if (color.contains(ChatColor.GREEN.toString())) return DyeColor.LIME;
        if (color.contains(ChatColor.AQUA.toString())) return DyeColor.CYAN;
        if (color.contains(ChatColor.LIGHT_PURPLE.toString())) return DyeColor.MAGENTA;
        if (color.contains(ChatColor.DARK_BLUE.toString())) return DyeColor.BLUE;
        if (color.contains(ChatColor.DARK_GRAY.toString())) return DyeColor.GRAY;
        if (color.contains(ChatColor.DARK_PURPLE.toString())) return DyeColor.PURPLE;
        if (color.contains(ChatColor.DARK_RED.toString())) return DyeColor.RED;
        if (color.contains(ChatColor.GRAY.toString())) return DyeColor.LIGHT_GRAY;
        if (color.contains(ChatColor.WHITE.toString())) return DyeColor.WHITE;
        return DyeColor.BLACK;
    }

}