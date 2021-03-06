package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.PlayerStartsPlayingEvent;
import com.gmail.val59000mc.events.UhcPlayerDeathEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.listeners.PlayerDeathListener;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.UniversalMaterial;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class NineSlotsListener extends ScenarioListener {

    public static ItemStack fillItem;

    @Override
    public void onEnable() {
        fillItem = UniversalMaterial.LIGHT_GRAY_STAINED_GLASS_PANE.getStack();
        ItemMeta meta = fillItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "BLOCKED");
        fillItem.setItemMeta(meta);
    }

    @EventHandler
    public void onGameStarted(PlayerStartsPlayingEvent e) {
        try {
            fillInventory(e.getUhcPlayer().getPlayer());
        } catch (UhcPlayerNotOnlineException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();

        // Only handle clicked items.
        if (item == null) { return; }

        if (item.equals(fillItem)) { e.setCancelled(true); }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        List<ItemStack> drops = e.getDrops();

        // Remove all fill items.
        while (drops.remove(fillItem)) {}
    }

    private void fillInventory(Player player) {
        for (int i = 9; i <= 35; i++) { player.getInventory().setItem(i, fillItem); }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(UhcPlayerDeathEvent e) {
        List<ItemStack> drops = e.getDrops();
        drops = drops.stream().filter(item -> item.getType() != fillItem.getType()).collect(Collectors.toList());

        if (PlayerDeathListener.autoRespawn) fillInventory(e.getKilled().getPlayerUnsafe());
    }

}