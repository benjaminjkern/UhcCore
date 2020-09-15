package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.scenarios.ScenarioListener;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DontWasteTimeListener extends ScenarioListener{

    List<Enchantment> dontDo;

    public void onEnable() {
        dontDo = new ArrayList<>();
        dontDo.add(Enchantment.SILK_TOUCH);
        dontDo.add(Enchantment.VANISHING_CURSE);
        dontDo.add(Enchantment.BINDING_CURSE);
    }

    @EventHandler
    public void onPlayerCraft(CraftItemEvent e){
        ItemStack item = e.getCurrentItem();

        for (Enchantment enchant : Enchantment.values()) {
            if (!dontDo.contains(enchant)) enchant(item, enchant);
        }
    }

    private void enchant(ItemStack item, Enchantment e) {
        try {
            item.addEnchantment(e, e.getMaxLevel());
        } catch (IllegalArgumentException f) {}
    }

}