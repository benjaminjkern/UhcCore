package com.gmail.val59000mc.listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class InventoryGUIListener {

    private Map<String, ItemStack> alive;
    private Map<String, ItemStack> spectators;

    public void updatePlayer(UhcPlayer up) {
        Player p;
        try {
            p = up.getPlayer();
        } catch (UhcPlayerNotOnlineException e) {
            alive.remove(up.getDisplayName().substring(2));
            spectators.remove(up.getDisplayName().substring(2));
            return;
        }
        String name = p.getName();

        ItemStack item;
        SkullMeta im;

        if (alive.containsKey(name)) {
            item = alive.get(name);
            im = (SkullMeta) item.getItemMeta();
            if (up.getState() != PlayerState.PLAYING) { alive.remove(name); }
        } else if (spectators.containsKey(name)) {
            item = spectators.get(name);
            im = (SkullMeta) item.getItemMeta();
            if (up.getState() == PlayerState.PLAYING) { spectators.remove(name); }
        } else {
            item = new ItemStack(Material.PLAYER_HEAD);
            im = (SkullMeta) item.getItemMeta();
            im.setOwningPlayer(p);
        }
        if (up.getState() == PlayerState.PLAYING) {
            im.setLore(Arrays.asList("\u00a7c" + heartFormat((p.getPlayer().getHealth()) / 2) + " \u00a7fHearts"));
            alive.put(name, item);
        } else {
            im.setLore(Arrays.asList("\u00a77Spectating"));
            spectators.put(name, item);
        }
        im.setDisplayName(up.getDisplayName());
        item.setItemMeta(im);
    }

    public InventoryGUIListener() {
        alive = new HashMap<>();
        spectators = new HashMap<>();

        for (UhcPlayer up : GameManager.getGameManager().getPlayersManager().getPlayersList()) { updatePlayer(up); }
    }

    private static void addButton(Inventory inv, Material mat, int slot, String name, String... lore) {
        ItemStack btn = new ItemStack(mat);
        ItemMeta btnMeta = btn.getItemMeta();
        btnMeta.setDisplayName(name);
        if (lore.length > 0) btnMeta.setLore(Arrays.asList(lore));
        btn.setItemMeta(btnMeta);
        addButton(inv, btn, slot);
    }

    private static void addButton(Inventory inv, Material mat, int slot, String name) {
        addButton(inv, mat, slot, name, new String[0]);
    }

    private static void addButton(Inventory inv, ItemStack item, int slot) { inv.setItem(slot, item); }

    private static String heartFormat(double hearts) {
        return ((int) hearts) + (hearts % 1 < 0.75 && hearts % 1 >= 0.25 ? ".5" : "");
    }

    private static final String EXIT_NAME = "\u00a7cExit Menu";

    public static final String LIST_TITLE = "\u00a7dPlayers in this Game:";

    public Inventory getListInventory(UhcPlayer viewer) { return getListInventory(viewer, 0); }

    public Inventory getListInventory(UhcPlayer viewer, int page) {

        int invSize;

        if (spectators.isEmpty()) invSize = (int) (Math.ceil(alive.size() / 8.)) * 9;
        else if (alive.isEmpty()) invSize = (int) (Math.ceil(spectators.size() / 8.)) * 9;
        else invSize = (int) (Math.ceil(alive.size() / 8.) + 1 + Math.ceil(spectators.size() / 8.)) * 9;
        Inventory listInventory = Bukkit.createInventory(null, Math.max(Math.min(invSize - page * 54, 54), 27),
                LIST_TITLE + " \u00a75(\u00a7fPage " + (page + 1) + "\u00a75)");

        addButton(listInventory, Material.BARRIER, 8, EXIT_NAME);
        if (page > 0) addButton(listInventory, Material.ARROW, listInventory.getSize() - 10, "\u00a76Previous Page");
        if (invSize - page * 54 > 54) {
            addButton(listInventory, Material.SPECTRAL_ARROW, listInventory.getSize() - 1, "\u00a76Next Page");
        }

        int i = 0;

        Player viewPlayer;
        try {
            viewPlayer = viewer.getPlayer();
        } catch (UhcPlayerNotOnlineException e) {
            viewPlayer = null;
        }

        if (!alive.isEmpty()) {
            for (ItemStack item : alive.values()) {
                SkullMeta im = (SkullMeta) item.getItemMeta();
                if (viewer.getState() == PlayerState.DEAD
                        || (viewPlayer != null && viewPlayer.hasPermission("uhc-core.admin"))) {
                    im.setLore(Arrays.asList(im.getLore().get(0), "\u00a77Click to Teleport"));
                    item.setItemMeta(im);
                }

                while (i % 9 > 7) i++;

                if (i >= page * 54 && i < (page + 1) * 54) addButton(listInventory, item, i % 54);
                if (i >= (page + 1) * 54) return listInventory;
                i++;
            }
        }

        if (!alive.isEmpty() && !spectators.isEmpty()) {
            while (i % 9 != 0) i++;

            for (int j = 0; j < 8; j++) {
                if (i >= page * 54 && i < (page + 1) * 54) addButton(listInventory, Material.WHITE_STAINED_GLASS_PANE,
                        i % 54, "\u00a7f--- \u00a7dSpectating \u00a7f---");
                if (i >= (page + 1) * 54) return listInventory;
                i++;
            }
        }

        if (!spectators.isEmpty()) {
            for (ItemStack item : spectators.values()) {
                while (i % 9 > 7) i++;

                if (i >= page * 54 && i < (page + 1) * 54) addButton(listInventory, item, i % 54);
                if (i >= (page + 1) * 54) return listInventory;
                i++;
            }
        }

        return listInventory;

    }
}
