package com.gmail.val59000mc.scenarios;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.YamlFile;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.FileUtils;
import com.gmail.val59000mc.utils.NMSUtils;
import com.gmail.val59000mc.utils.RandomUtils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class ScenarioManager {

    private static final int ROW = 9;
    private final Map<Scenario, ScenarioListener> activeScenarios;

    private List<Scenario> votableScenarios;

    private Inventory scenarioVoteInventory;

    public ScenarioManager() {
        activeScenarios = new HashMap<>();

        Set<Scenario> blacklist = GameManager.getGameManager().getConfiguration().getScenarioBlackList();
        scenarioVoteInventory = Bukkit.createInventory(null, 5 * ROW,
                RandomUtils.randomTextColor() + Lang.SCENARIO_GLOBAL_INVENTORY_VOTE);

        votableScenarios = new ArrayList<>();
        int scenariosLoaded = 0;

        for (Scenario scenario : Scenario.values()) {
            // Don't add to menu when blacklisted / not compatible / already enabled.
            if (blacklist.contains(scenario) || !scenario.isCompatibleWithVersion() || isActivated(scenario)) continue;
            if (scenariosLoaded >= 27) break;
            votableScenarios.add(scenario);
            scenariosLoaded++;

            ItemStack item = scenario.getScenarioItem();
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7c0", Lang.SCENARIO_GLOBAL_ITEM_INFO));
            item.setItemMeta(meta);
            scenarioVoteInventory.addItem(item);
        }

        ItemStack item = Scenario.RANDOM.getScenarioItem();
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7c0", "\u00a77Vote to randomize the scenarios!",
                Lang.SCENARIO_GLOBAL_ITEM_INFO));
        item.setItemMeta(meta);
        scenarioVoteInventory.setItem(42, item);

        item = Scenario.BOTSIN.getScenarioItem();
        meta = item.getItemMeta();
        meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7c0", "\u00a77Vote to fill the game with bots!",
                Lang.SCENARIO_GLOBAL_ITEM_INFO));
        item.setItemMeta(meta);
        scenarioVoteInventory.setItem(40, item);

        item = Scenario.NONE.getScenarioItem();
        meta = item.getItemMeta();
        meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7c0", "\u00a77Vote to cancel all scenarios!",
                Lang.SCENARIO_GLOBAL_ITEM_INFO));
        item.setItemMeta(meta);
        scenarioVoteInventory.setItem(38, item);
    }

    public List<Scenario> getVotableScenarios() { return votableScenarios; }

    public void addScenario(Scenario scenario) {
        if (isActivated(scenario)) return;

        Class<? extends ScenarioListener> listenerClass = scenario.getListener();

        try {
            ScenarioListener scenarioListener = null;
            if (listenerClass != null) { scenarioListener = listenerClass.newInstance(); }

            activeScenarios.put(scenario, scenarioListener);

            if (scenarioListener != null) {
                loadScenarioOptions(scenario, scenarioListener);
                scenarioListener.onEnable();

                // If disabled in the onEnable method don't register listener.
                if (isActivated(scenario)) {
                    Bukkit.getServer().getPluginManager().registerEvents(scenarioListener, UhcCore.getPlugin());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void removeScenario(Scenario scenario) {
        ScenarioListener scenarioListener = activeScenarios.get(scenario);
        activeScenarios.remove(scenario);

        if (scenarioListener != null) {
            HandlerList.unregisterAll(scenarioListener);
            scenarioListener.onDisable();
        }
    }

    public boolean toggleScenario(Scenario scenario) {
        if (isActivated(scenario)) {
            removeScenario(scenario);
            return false;
        }

        addScenario(scenario);
        return true;
    }

    public synchronized Set<Scenario> getActiveScenarios() { return activeScenarios.keySet(); }

    public boolean isActivated(Scenario scenario) { return activeScenarios.containsKey(scenario); }

    public boolean isActivated(String scenarioName) {
        for (Scenario s : activeScenarios.keySet()) { if (s.name().equals(scenarioName)) return true; }
        return false;
    }

    public ScenarioListener getScenarioListener(Scenario scenario) { return activeScenarios.get(scenario); }

    public Inventory getScenarioMainInventory(boolean editItem) {

        Inventory inv = Bukkit.createInventory(null, 3 * ROW, Lang.SCENARIO_GLOBAL_INVENTORY);

        for (Scenario scenario : getActiveScenarios()) {
            if (scenario.isCompatibleWithVersion()) { inv.addItem(scenario.getScenarioItem()); }
        }

        if (editItem) {
            // add edit item
            ItemStack edit = new ItemStack(Material.BARRIER);
            ItemMeta itemMeta = edit.getItemMeta();
            itemMeta.setDisplayName(Lang.SCENARIO_GLOBAL_ITEM_EDIT);
            edit.setItemMeta(itemMeta);

            inv.setItem(26, edit);
        }
        return inv;
    }

    public Inventory getScenarioEditInventory() {

        Inventory inv = Bukkit.createInventory(null, 6 * ROW, Lang.SCENARIO_GLOBAL_INVENTORY_EDIT);

        // add edit item
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta itemMeta = back.getItemMeta();
        itemMeta.setDisplayName(Lang.SCENARIO_GLOBAL_ITEM_BACK);
        back.setItemMeta(itemMeta);
        inv.setItem(5 * ROW + 8, back);

        for (Scenario scenario : Scenario.values()) {
            if (!scenario.isCompatibleWithVersion()) { continue; }

            ItemStack scenarioItem = scenario.getScenarioItem();
            if (isActivated(scenario)) {
                scenarioItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                scenarioItem.setAmount(2);
            }
            inv.addItem(scenarioItem);
        }

        return inv;
    }

    public Inventory getScenarioVoteInventory(UhcPlayer uhcPlayer) { return scenarioVoteInventory; }

    public void disableAllScenarios() {
        Set<Scenario> active = new HashSet<>(getActiveScenarios());
        for (Scenario scenario : active) { removeScenario(scenario); }
    }

    public void countVotes() {
        Bukkit.getLogger().info("" + GameManager.getGameManager().getPlayersManager().getPlayersList().size());
        // should be a priority queue, though I think that messes with the randomness of
        // it

        List<Scenario> allValid = new ArrayList<>();

        Map<Scenario, Integer> votesMap = new HashMap<>();
        List<List<Scenario>> votesList = new ArrayList<>();
        votesList.add(new ArrayList<>());

        Set<Scenario> blacklist = GameManager.getGameManager().getConfiguration().getScenarioBlackList();
        int i = 0;
        for (Scenario scenario : Scenario.values()) {
            i++;
            if (!blacklist.contains(scenario)) {
                if (i <= 27) allValid.add(scenario);
                votesList.get(0).add(scenario);
                votesMap.put(scenario, 0);
            }
        }

        for (UhcPlayer uhcPlayer : GameManager.getGameManager().getPlayersManager().getPlayersList()) {
            for (Scenario scenario : uhcPlayer.getScenarioVotes()) {
                int currentVotes = votesMap.get(scenario);
                if (currentVotes + 1 == votesList.size()) { votesList.add(new ArrayList<>()); }
                votesList.get(currentVotes).remove(scenario);
                votesList.get(currentVotes + 1).add(scenario);
                votesMap.put(scenario, currentVotes + 1);
            }
        }

        if (votesMap.get(Scenario.BOTSIN) >= Bukkit.getOnlinePlayers().size() / 2.)
            GameManager.getGameManager().setBotsIn(true);
        else GameManager.getGameManager().setBotsIn(false);

        int scenarioCount = GameManager.getGameManager().getConfiguration().getElectedScenaroCount();
        int maxVotes = votesList.size() - 1;
        while (scenarioCount > 0) {
            if (maxVotes == 0) break;
            List<Scenario> currentList;
            if (maxVotes < 0) currentList = allValid;
            else currentList = votesList.get(maxVotes);
            if (currentList.isEmpty()) {
                maxVotes--;
                continue;
            }
            Scenario scenario = currentList.get((int) (Math.random() * currentList.size()));

            Bukkit.getLogger().info("[UhcCore] Attempting to load scenario " + scenario.getName());

            if (scenario == Scenario.BOTSIN) {
                allValid.remove(scenario);
                currentList.remove(scenario);
                continue;
            }

            if (scenario == Scenario.NONE) {
                if (maxVotes <= 0 || currentList.size() > 1) continue;
                Bukkit.getLogger().info("[UhcCore] NONE Scenario was selected, cancelling the rest of the scenarios");
                scenarioCount = 0;
                break;
            }
            if (scenario == Scenario.RANDOM) {
                if (maxVotes <= 0 || currentList.size() > 1) continue;
                Bukkit.getLogger()
                        .info("[UhcCore] RANDOM Scenario was selected, randomizing the rest of the scenarios");
                maxVotes = -1;
                continue;
            }
            allValid.remove(scenario);
            currentList.remove(scenario);

            try {
                if (scenario.condition.call()) addScenario(scenario);
                else {
                    Bukkit.getLogger()
                            .info("[UhcCore] Scenario " + scenario.getName() + "'s condition was not met, moving on");
                    continue;
                }
            } catch (Exception e) {
                continue;
            }
            scenarioCount--;
        }
    }

    private void loadScenarioOptions(Scenario scenario, ScenarioListener listener)
            throws ReflectiveOperationException, IOException, InvalidConfigurationException {
        List<Field> optionFields = NMSUtils.getAnnotatedFields(listener.getClass(), Option.class);

        if (optionFields.isEmpty()) { return; }

        YamlFile cfg = FileUtils.saveResourceIfNotAvailable("scenarios.yml");

        for (Field field : optionFields) {
            Option option = field.getAnnotation(Option.class);
            String key = option.key().isEmpty() ? field.getName() : option.key();
            Object value = cfg.get(scenario.name().toLowerCase() + "." + key, field.get(listener));
            field.set(listener, value);
        }

        if (cfg.addedDefaultValues()) { cfg.saveWithComments(); }
    }

}