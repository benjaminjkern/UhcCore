package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.UniversalMaterial;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CutCleanListener extends ScenarioListener {

    private final ItemStack lapis;
    @Option(key = "unlimited-lapis")
    private final boolean unlimitedLapis = true;
    @Option(key = "check-correct-tool")
    private final boolean checkTool = false;

    private final Map<Material, FurnaceRecipe> furnaceRecipes;

    public CutCleanListener() {
        lapis = UniversalMaterial.LAPIS_LAZULI.getStack(64);

        furnaceRecipes = new HashMap<>();

        final Iterator<Recipe> iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            final Recipe recipe = iter.next();
            if (!(recipe instanceof FurnaceRecipe))
                continue;
            final FurnaceRecipe newRecipe = (FurnaceRecipe) recipe;
            furnaceRecipes.put(newRecipe.getInput().getType(), newRecipe);
        }
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent e) {
        // don't bother cooking other people's stuff
        if (e.getEntityType() == EntityType.PLAYER)
            return;

        // doesnt check if killer is sneaking because thats not easily accessible, but
        // it should

        for (int i = 0; i < e.getDrops().size(); i++) {
            final ItemStack drop = e.getDrops().get(i);

            if (furnaceRecipes.containsKey(drop.getType())) {
                e.getDrops().set(i, furnaceRecipes.get(drop.getType()).getResult());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent e) {

        // technically hsould check for veinminer as well but its redundant since that
        // only triggers when the player is sneaking
        if (isActivated(Scenario.KINGMIDAS) || e.getPlayer().isSneaking())
            return;

        Block block = e.getBlock();

        if (checkTool && !UniversalMaterial.isCorrectTool(block.getType(),
                e.getPlayer().getInventory().getItemInMainHand().getType()))
            return;

        Location loc = block.getLocation().add(0.5, 0, 0.5);

        Set<ItemStack> toDrop = new HashSet<>();
        int exp = e.getExpToDrop();

        for (ItemStack drop : block.getDrops()) {

            if (furnaceRecipes.containsKey(drop.getType())) {
                toDrop.add(furnaceRecipes.get(drop.getType()).getResult());
                exp += furnaceRecipes.get(drop.getType()).getExperience();
            } else
                toDrop.add(drop);
        }

        for (ItemStack drop : toDrop) {
            loc.getWorld().dropItem(loc, drop);
        }
        UhcItems.spawnExtraXp(loc, exp);
        e.setCancelled(true);
    }

    @EventHandler
    public void openInventoryEvent(final InventoryOpenEvent e) {
        if (!unlimitedLapis)
            return;

        ((Player) e.getPlayer()).setTotalExperience(Integer.MAX_VALUE);

        if (e.getInventory() instanceof EnchantingInventory) {
            e.getInventory().setItem(1, lapis);
        }
    }

    @EventHandler
    public void closeInventoryEvent(final InventoryCloseEvent e) {
        if (!unlimitedLapis)
            return;

        ((Player) e.getPlayer()).setTotalExperience(0);

        if (e.getInventory() instanceof EnchantingInventory)
            e.getInventory().setItem(1, null);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        final Inventory inv = e.getInventory();
        final ItemStack item = e.getCurrentItem();
        if (!unlimitedLapis)
            return;
        if (inv == null || item == null)
            return;

        if (inv instanceof EnchantingInventory) {

            if (item.getType().equals(lapis.getType())) {
                e.setCancelled(true);
            } else {
                e.getInventory().setItem(1, lapis);
            }
        }
    }

}