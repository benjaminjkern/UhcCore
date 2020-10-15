package com.gmail.val59000mc.threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.UhcPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class UpdateBotsThread implements Runnable {

    private static Map<Material, Integer> attackRateMap;

    private UpdateBotsThread task = this;

    private static GameManager gm = GameManager.getGameManager();

    static {
        attackRateMap = new HashMap<>();
        attackRateMap.put(Material.WOODEN_SWORD, 12);
        attackRateMap.put(Material.GOLDEN_SWORD, 12);
        attackRateMap.put(Material.STONE_SWORD, 12);
        attackRateMap.put(Material.IRON_SWORD, 12);
        attackRateMap.put(Material.DIAMOND_SWORD, 12);
        attackRateMap.put(Material.NETHERITE_SWORD, 12);

        attackRateMap.put(Material.TRIDENT, 18);

        attackRateMap.put(Material.WOODEN_SHOVEL, 20);
        attackRateMap.put(Material.GOLDEN_SHOVEL, 20);
        attackRateMap.put(Material.STONE_SHOVEL, 20);
        attackRateMap.put(Material.IRON_SHOVEL, 20);
        attackRateMap.put(Material.DIAMOND_SHOVEL, 20);
        attackRateMap.put(Material.NETHERITE_SHOVEL, 20);

        attackRateMap.put(Material.WOODEN_PICKAXE, 17);
        attackRateMap.put(Material.GOLDEN_PICKAXE, 17);
        attackRateMap.put(Material.STONE_PICKAXE, 17);
        attackRateMap.put(Material.IRON_PICKAXE, 17);
        attackRateMap.put(Material.DIAMOND_PICKAXE, 17);
        attackRateMap.put(Material.NETHERITE_PICKAXE, 17);

        attackRateMap.put(Material.WOODEN_AXE, 25);
        attackRateMap.put(Material.GOLDEN_AXE, 20);
        attackRateMap.put(Material.STONE_AXE, 25);
        attackRateMap.put(Material.IRON_AXE, 22);
        attackRateMap.put(Material.DIAMOND_AXE, 20);
        attackRateMap.put(Material.NETHERITE_AXE, 20);

        attackRateMap.put(Material.WOODEN_HOE, 20);
        attackRateMap.put(Material.GOLDEN_HOE, 20);
        attackRateMap.put(Material.STONE_HOE, 10);
        attackRateMap.put(Material.IRON_HOE, 7);
    }

    public void run() {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
            if (sentinel.chasing == null) {
                sentinel.pathTo(Bukkit.getWorld(gm.getConfiguration().getOverworldUuid()).getHighestBlockAt(0, 0)
                        .getLocation());
                // if its not chasing anyone, update inventory
                List<UhcPlayer> realPlayers = new ArrayList<>(gm.getPlayersManager().getOnlinePlayingPlayers());
                ItemStack pickedItem = null;
                try {
                    pickedItem = realPlayers.get((int) (Math.random() * realPlayers.size())).getPlayer().getInventory()
                            .getItemInMainHand();
                } catch (UhcPlayerNotOnlineException e) {
                    // uh
                }

                if (pickedItem != null) {
                    sentinel.getLivingEntity().getEquipment().setItemInMainHand(pickedItem);

                    Material m = sentinel.getLivingEntity().getEquipment().getItemInMainHand().getType();

                    if (attackRateMap.containsKey(m)) sentinel.attackRate = attackRateMap.get(m);
                    else sentinel.attackRate = 5;

                    sentinel.attackRate += (int) (Math.random() * 5);
                }
            }
        }

        if (gm.getGameState() == GameState.PLAYING || gm.getGameState() == GameState.DEATHMATCH) {
            Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20 * 60);
        }
    }

}
