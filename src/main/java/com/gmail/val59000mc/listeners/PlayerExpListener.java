package com.gmail.val59000mc.listeners;

import java.util.HashMap;
import java.util.Map;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerExpListener implements Listener {

    private final GameManager gameManager;

    public static Map<UhcPlayer, Integer> lastLevel = new HashMap<>();

    public PlayerExpListener(GameManager gameManager) { this.gameManager = gameManager; }

    private int getLastLevel(UhcPlayer p) {
        if (!lastLevel.containsKey(p)) return 0;
        return lastLevel.get(p);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDamage(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        UhcPlayer uhcPlayer = gameManager.getPlayersManager().getUhcPlayer(player);
        if (uhcPlayer.getState() != PlayerState.PLAYING) {
            UhcItems.spawnExtraXp(player.getLocation(), event.getAmount());
            event.setAmount(0);
            return;
        }
        Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
            int currentLevel = player.getLevel();
            while (true) {
                int targetLevel = getLastLevel(uhcPlayer) + 5;
                if (currentLevel < targetLevel) return;

                lastLevel.put(uhcPlayer, targetLevel);

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                        "\u00a7fYou reached level \u00a7d" + targetLevel + " \u00a7fand were healed half a heart!"));
                player.setHealth(Math.min(player.getHealth() + 1, player.getMaxHealth()));
            }
        }, 1);

    }

}