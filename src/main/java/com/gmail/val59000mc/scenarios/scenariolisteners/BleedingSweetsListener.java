package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.enchantments.Enchantment;

import java.util.*;
import java.util.stream.Collectors;

public class BleedingSweetsListener extends ScenarioListener {

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (!getGameManager().getPvp()) return;

        if (getGameManager().getGameState() != GameState.PLAYING
                && getGameManager().getGameState() != GameState.DEATHMATCH)
            return;

        Entity damager = e.getDamager();
        if (damager instanceof Projectile) damager = (Entity) ((Projectile) damager).getShooter();
        if (e.getEntityType() != EntityType.PLAYER || !(damager instanceof Player)) return;

        Player killed = (Player) e.getEntity();
        Player killer = (Player) damager;

        if (e.getDamage() < killed.getHealth()) return;

        PlayersManager pm = getPlayersManager();

        killer.setHealth(Math.min(killer.getHealth() + 2, killer.getMaxHealth()));

        // put everything into a list so I dont have to check empty items
        ItemStack[] contents = killer.getInventory().getContents();
        List<ItemStack> picker = new ArrayList<>();
        for (int i = 0; i < contents.length; i++) { if (contents[i] != null) picker.add(contents[i]); }

        // purge items that cant be enchanted
        while (picker.size() > 0) {
            int r = (int) (Math.random() * picker.size());
            ItemStack item = picker.get(r);
            picker.remove(r);
            if (randomEnchant(item)) {
                killer.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("Your \u00a7d" + item.getType() + " \u00a7fhas been enchanted!"));
                return;
            }
        }
        // player somehow killed the other player without anything in their inventory
        // that can be enchanted
    }

    private boolean randomEnchant(ItemStack item) {
        List<Enchantment> allEnchants = new ArrayList<>(Arrays.asList(Enchantment.values())).stream()
                .filter(ench -> !ench.getKey().getNamespace().contains("CURSE")).collect(Collectors.toList());

        // purge enchantments that dont work on this item
        while (allEnchants.size() > 0) {
            int r = (int) (Math.random() * allEnchants.size());
            Enchantment e = allEnchants.get(r);
            allEnchants.remove(r);
            if (!e.canEnchantItem(item) || (item.containsEnchantment(e) && e.getMaxLevel() == 1)) continue;
            item.addUnsafeEnchantment(e, item.getEnchantmentLevel(e) + 1);
            return true;
        }
        return false;
    }

}