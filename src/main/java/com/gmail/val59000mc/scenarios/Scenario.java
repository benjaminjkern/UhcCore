package com.gmail.val59000mc.scenarios;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.scenarios.scenariolisteners.*;
import com.gmail.val59000mc.utils.UniversalMaterial;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public enum Scenario {
    // CURRENTLY IN
    CUTCLEAN(UniversalMaterial.IRON_INGOT, CutCleanListener.class,
            () -> !GameManager.getGameManager().getScenarioManager().isActivated("RANDOMIZEDDROPS")),
    TIMEBOMB(UniversalMaterial.TRAPPED_CHEST, TimebombListener.class),
    TRIPLEORES(UniversalMaterial.REDSTONE_ORE, TripleOresListener.class,
            () -> !GameManager.getGameManager().getScenarioManager().isActivated("RANDOMIZEDDROPS")),
    LUCKYLEAVES(UniversalMaterial.OAK_LEAVES, LuckyLeavesListener.class),
    FLOWERPOWER(UniversalMaterial.SUNFLOWER, FlowerPowerListener.class,
            () -> !GameManager.getGameManager().getScenarioManager().isActivated("RANDOMIZEDDROPS")),
    SKYHIGH(UniversalMaterial.ANVIL, SkyHighListener.class), EGGS(UniversalMaterial.EGG, EggsScenarioListener.class),
    DUOS(UniversalMaterial.ARMOR_STAND, DuosListener.class,
            () -> GameManager.getGameManager().getPlayersManager().getOnlinePlayingPlayers().size() > 2),
    FLYHIGH(UniversalMaterial.ELYTRA, FlyHighListener.class, 9),
    RANDOMIZEDDROPS(UniversalMaterial.EXPERIENCE_BOTTLE, RandomizedDropsListener.class, new Callable<Boolean>() {
        public Boolean call() {
            ScenarioManager sm = GameManager.getGameManager().getScenarioManager();
            return !sm.isActivated("FLOWERPOWER") && !sm.isActivated("CUTCLEAN") && !sm.isActivated("TRIPLEORES")
                    && !sm.isActivated("KINGMIDAS");
        }
    }), ACHIEVEMENTHUNTER(UniversalMaterial.BOOK, AchievementHunter.class),
    NINESLOTS(UniversalMaterial.BARRIER, NineSlotsListener.class),
    POLITICS(UniversalMaterial.IRON_SWORD, PoliticsListener.class,
            () -> GameManager.getGameManager().getPlayersManager().getOnlinePlayingPlayers().size() > 2),
    NETHERSTART(UniversalMaterial.LAVA_BUCKET, NetherStartListener.class),
    LAGWORLD(UniversalMaterial.DIRT, LagWorldListener.class),
    LILCHEAT(UniversalMaterial.DIAMOND_BLOCK, LilCheatListener.class),
    SWAP(UniversalMaterial.RAW_PORK, SwapListener.class), COMPANION(UniversalMaterial.BONE, CompanionListener.class),
    DONTWASTETIME(UniversalMaterial.DIAMOND_PICKAXE, DontWasteTimeListener.class),
    KINGMIDAS(UniversalMaterial.GOLD_NUGGET, KingMidasListener.class,
            () -> !GameManager.getGameManager().getScenarioManager().isActivated("RANDOMIZEDDROPS")),
    WHATSMINE(UniversalMaterial.ENDER_CHEST, WhatsMineListener.class),
    INHERITANCE(UniversalMaterial.LEAD, InheritanceListener.class, true),
    ONETRICKPONY(UniversalMaterial.SADDLE, OneTrickPonyListener.class, true),
    TNTWORLD(UniversalMaterial.TNT, TntWorldListener.class, true),
    SUPERHEROES(UniversalMaterial.NETHER_STAR, SuperHeroesListener.class),
    SUDDENDEATH(UniversalMaterial.WOODEN_SWORD, SuddenDeathListener.class),
    PASSIVE(UniversalMaterial.SNOWBALL, PassiveListener.class),

    // ONES THAT ARENT ENTIRELY LAME AND CAN BE USED (DISABLED IN CONFIG)
    GOLDLESS(UniversalMaterial.GOLD_ORE, GoldLessListener.class),
    DOUBLEORES(UniversalMaterial.REDSTONE_ORE, DoubleOresListener.class),
    NOCLEAN(UniversalMaterial.QUARTZ, NoCleanListener.class),
    BLOODDIAMONDS(UniversalMaterial.DIAMOND_ORE, BloodDiamondsListener.class),
    BESTPVE(UniversalMaterial.REDSTONE, BestPvEListener.class),
    BLEEDINGSWEETS(UniversalMaterial.BEETROOT_SOUP, BleedingSweetsListener.class),
    WEAKESTLINK(UniversalMaterial.DIAMOND_SWORD, WeakestLinkListener.class),
    DOUBLEGOLD(UniversalMaterial.GOLD_INGOT, DoubleGoldListener.class),
    VEINMINER(UniversalMaterial.COAL_ORE, VeinMinerListener.class),
    DRAGONRUSH(UniversalMaterial.DRAGON_EGG, DragonRushListener.class),
    LOVEATFIRSTSIGHT(UniversalMaterial.POPPY, LoveAtFirstSightListener.class),
    RANDOMIZEDCRAFTS(UniversalMaterial.CRAFTING_TABLE, RandomizedCraftsListener.class, 13),
    MONSTERSINC(UniversalMaterial.IRON_DOOR, MonstersIncListener.class), NOGOINGBACK(UniversalMaterial.NETHER_BRICK),
    NOCRAFT(UniversalMaterial.HONEYCOMB_BLOCK, NoCraftListener.class),
    FAST(UniversalMaterial.FEATHER, FastListener.class), SWITCHEROO(UniversalMaterial.ARROW, SwitcherooListener.class),
    UPSIDEDOWNCRAFTING(UniversalMaterial.SCAFFOLDING, UpsideDownCraftsListener.class, 13),

    // LAME/USELESS ONES
    // PERMAKILL(UniversalMaterial.IRON_SWORD, PermaKillListener.class),
    // INFINITEENCHANTS(UniversalMaterial.ENCHANTING_TABLE,
    // InfiniteEnchantsListener.class),
    // GONEFISHING(UniversalMaterial.FISHING_ROD, GoneFishingListener.class),
    // FASTLEAVESDECAY(UniversalMaterial.ACACIA_LEAVES,
    // FastLeavesDecayListener.class),
    // FASTSMELTING(UniversalMaterial.FURNACE, FastSmeltingListener.class),
    // HASTEYBOYS(UniversalMaterial.DIAMOND_PICKAXE, HasteyBoysListener.class),
    // FIRELESS(UniversalMaterial.LAVA_BUCKET, FirelessListener.class),
    // BOWLESS(UniversalMaterial.BOW, BowlessListener.class),
    // RODLESS(UniversalMaterial.FISHING_ROD, RodlessListener.class),
    // SHIELDLESS(UniversalMaterial.SHIELD, ShieldlessListener.class, 9),
    // NOFALL(UniversalMaterial.FEATHER, NoFallListener.class),
    // HORSELESS(UniversalMaterial.SADDLE, HorselessListener.class),
    // TIMBER(UniversalMaterial.OAK_LOG, TimberListener.class),
    ANONYMOUS(UniversalMaterial.NAME_TAG, AnonymousListener.class), // had issues with disabling, so I just left these
                                                                    // ones in
    SILENTNIGHT(UniversalMaterial.CLOCK, SilentNightListener.class),

    // ONLY WORK IF THE GAME IS DEFINITELY DUOS/QUADS
    // SHAREDHEALTH(UniversalMaterial.RED_DYE, SharedHealthListener.class),
    // DOUBLEDATES(UniversalMaterial.RED_BANNER, DoubleDatesListener.class),
    // CHILDRENLEFTUNATTENDED(UniversalMaterial.WOLF_SPAWN_EGG,
    // ChildrenLeftUnattended.class),
    TEAMINVENTORY(UniversalMaterial.CHEST); // same as above, had issues with disabling

    private String name;
    private final UniversalMaterial material;
    private final Class<? extends ScenarioListener> listener;
    private final int fromVersion;
    private boolean isNew;
    private List<String> description;
    public Callable<Boolean> condition;

    Scenario(UniversalMaterial material) { this(material, null, 8, false); }

    Scenario(UniversalMaterial material, Class<? extends ScenarioListener> listener) {
        this(material, listener, 8, false);
    }

    Scenario(UniversalMaterial material, Class<? extends ScenarioListener> listener, Callable<Boolean> condition) {
        this(material, listener, 8, false, condition);
    }

    Scenario(UniversalMaterial material, Class<? extends ScenarioListener> listener, int fromVersion) {
        this(material, listener, fromVersion, false);
    }

    Scenario(UniversalMaterial material, Class<? extends ScenarioListener> listener, int fromVersion,
            Callable<Boolean> condition) {
        this(material, listener, fromVersion, false, condition);
    }

    Scenario(UniversalMaterial material, Class<? extends ScenarioListener> listener, boolean isNew) {
        this(material, listener, 8, isNew);
    }

    Scenario(UniversalMaterial material, Class<? extends ScenarioListener> listener, int fromVersion, boolean isNew) {
        this(material, listener, fromVersion, isNew, null);
    }

    Scenario(UniversalMaterial material, Class<? extends ScenarioListener> listener, int fromVersion, boolean isNew,
            Callable<Boolean> condition) {
        this.material = material;
        this.listener = listener;
        this.fromVersion = fromVersion;
        this.isNew = isNew;
        this.condition = condition == null ? () -> true : condition;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public List<String> getDescription() { return description; }

    public void setDescription(List<String> description) { this.description = description; }

    public String getLowerCase() { return name().toLowerCase(); }

    public UniversalMaterial getMaterial() { return material; }

    @Nullable
    public Class<? extends ScenarioListener> getListener() { return listener; }

    public boolean equals(String name) {
        return name.contains(getName()) || name.replace(" ", "").toLowerCase().equals(name().toLowerCase());
    }

    public static Scenario getScenario(String s) {

        for (Scenario scenario : values()) { if (scenario.equals(s)) { return scenario; } }
        return null;
    }

    public ItemStack getScenarioItem() {
        ItemStack item = material.getStack();
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName((isNew ? ("\u00a76NEW: ") : Lang.SCENARIO_GLOBAL_ITEM_COLOR) + name);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.setLore(Arrays.asList(Lang.SCENARIO_GLOBAL_ITEM_INFO));

        item.setItemMeta(meta);
        return item;
    }

    public boolean isCompatibleWithVersion() { return fromVersion <= UhcCore.getVersion(); }

}
