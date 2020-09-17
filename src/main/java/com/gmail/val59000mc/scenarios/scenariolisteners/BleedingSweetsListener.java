package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.game.GameState;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

public class BleedingSweetsListener extends ScenarioListener{

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e){
        if (e.getEntityType() != EntityType.PLAYER || !(e.getDamager() instanceof Player)) return;

        if (getGameManager().getGameState() != GameState.PLAYING)return;

        Player killed = (Player) e.getEntity();
        Player killer = (Player) e.getDamager();

        if (e.getDamage() < killed.getHealth()) return;

        // put everything into a list so I dont have to check empty items
        ItemStack[] contents = killer.getInventory().getContents();
        List<ItemStack> picker = new ArrayList<>();
        for (int i=0;i<contents.length;i++) {
            if (contents[i] != null) picker.add(contents[i]);
        }

        // purge items that cant be enchanted
        while (picker.size() > 0) {
            int r = (int)(Math.random()*picker.size());
            ItemStack item = picker.get(r);
            picker.remove(r);
            if (randomEnchant(item)) return;
        }
        // player somehow killed the other player without anything in their inventory that can be enchanted
    }

    private boolean randomEnchant(ItemStack item) {
        List<Enchantment> allEnchants = Arrays.asList(Enchantment.values());
        
        // purge enchantments that dont work on this item
        while (allEnchants.size() > 0) {
            int r = (int)(Math.random()*allEnchants.size());
            Enchantment e = allEnchants.get(r);
            allEnchants.remove(r);
            if (!e.canEnchantItem(item)) continue;
            item.addUnsafeEnchantment(e, (item.containsEnchantment(e) ? item.getEnchantmentLevel(e) : 0) + 1);
            return true;
        }
        return false;
    }

}