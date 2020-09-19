package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.bukkit.enchantments.Enchantment;
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
    private boolean unlimitedLapis = true;
    @Option(key = "check-correct-tool")
    private boolean checkTool = false;

    private final Map<Material, FurnaceRecipe> furnaceRecipes;
    private final Set<Material> banList;

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

        banList = new HashSet<>();
        // dont want stone
        banList.add(Material.STONE);
        // dont want charcoal
        banList.add(Material.OAK_LOG);
        banList.add(Material.BIRCH_LOG);
        banList.add(Material.DARK_OAK_LOG);
        banList.add(Material.ACACIA_LOG);
        banList.add(Material.SPRUCE_LOG);
        banList.add(Material.JUNGLE_LOG);
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent e) {
        // don't bother cooking other people's stuff
        if (e.getEntityType() == EntityType.PLAYER)
            return;

        // doesnt check if killer is sneaking because thats not easily accessible, but
        // it should

        // also this should check for looting but I do NOT wanna deal with that, silk
        // touch and fortune are annoying enough

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
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();

        if (checkTool && !UniversalMaterial.isCorrectTool(block.getType(), hand.getType()))
            return;

        Location loc = block.getLocation().add(0.5, 0, 0.5);

        List<ItemStack> toDrop = new ArrayList<>();
        int exp = e.getExpToDrop();

        for (ItemStack drop : block.getDrops()) {
            ItemStack cookedDrop = drop;
            if (!banList.contains(drop.getType()) && furnaceRecipes.containsKey(drop.getType())) {
                cookedDrop = furnaceRecipes.get(drop.getType()).getResult();
                exp += furnaceRecipes.get(drop.getType()).getExperience();
            }
            int amount = TripleOresListener.ores.containsKey(drop.getType()) ? getFortune(hand) : 1;
            if (isActivated(Scenario.TRIPLEORES) && TripleOresListener.ores.containsKey(drop.getType())) {
                amount *= 3;
            }
            if (isActivated(Scenario.DOUBLEGOLD) && drop.getType() == Material.GOLD_ORE) {
                amount *= 2;
            }

            for (int i = 0; i < amount; i++)
                toDrop.add(cookedDrop);
        }

        block.setType(Material.AIR);
        for (ItemStack drop : toDrop) {
            loc.getWorld().dropItem(loc, drop);
        }
        UhcItems.spawnExtraXp(loc, exp);
        e.setCancelled(true);
    }

    private int getFortune(ItemStack hand) {
        double r = Math.random() * 60;
        switch (hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)) {
            case 1:
                return r < 40 ? 1 : 2;
            case 2:
                return r < 30 ? 1 : r < 45 ? 2 : 3;
            case 3:
                return r < 24 ? 1 : r < 36 ? 2 : r < 48 ? 3 : 4;
            default:
                return 1;
        }
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