package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class OneTrickPonyListener extends ScenarioListener {

    private static final EntityType[] MOBS = new EntityType[] { EntityType.BAT, EntityType.BEE, EntityType.BLAZE,
            EntityType.CAT, EntityType.CAVE_SPIDER, EntityType.CHICKEN, EntityType.COD, EntityType.COW,
            EntityType.CREEPER, EntityType.DOLPHIN, EntityType.DONKEY, EntityType.DROWNED, EntityType.ELDER_GUARDIAN,
            EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.ENDER_DRAGON, EntityType.EVOKER, EntityType.FOX,
            EntityType.GHAST, EntityType.GIANT, EntityType.GUARDIAN, EntityType.HOGLIN, EntityType.HORSE,
            EntityType.HUSK, EntityType.ILLUSIONER, EntityType.IRON_GOLEM, EntityType.LLAMA, EntityType.MAGMA_CUBE,
            EntityType.MULE, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PANDA, EntityType.PARROT,
            EntityType.PHANTOM, EntityType.PIG, EntityType.PIGLIN, EntityType.PILLAGER, EntityType.POLAR_BEAR,
            EntityType.PUFFERFISH, EntityType.RABBIT, EntityType.RAVAGER, EntityType.SALMON, EntityType.SHEEP,
            EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SKELETON_HORSE, EntityType.SLIME,
            EntityType.SNOWMAN, EntityType.SPIDER, EntityType.SQUID, EntityType.STRAY, EntityType.STRIDER,
            EntityType.TRADER_LLAMA, EntityType.TROPICAL_FISH, EntityType.TURTLE, EntityType.VEX, EntityType.VILLAGER,
            EntityType.VINDICATOR, EntityType.WANDERING_TRADER, EntityType.WITCH, EntityType.WITHER,
            EntityType.WITHER_SKELETON, EntityType.WOLF, EntityType.ZOGLIN, EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE,
            EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIFIED_PIGLIN };

    private EntityType chosenType;
    private static Set<EntityType> mobSet = new HashSet<>(Arrays.asList(MOBS));

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        chosenType = MOBS[(int) (Math.random() * MOBS.length)];
        mobSet.remove(chosenType);

        new BukkitRunnable() {
            public void run() {
                for (World w : Bukkit.getWorlds()) {
                    for (Entity entity : w.getEntities()) {
                        if (isActivated(Scenario.WITHERRUSH) && WitherRushListener.gameWither != null
                                && entity.getCustomName().equals(WitherRushListener.gameWither.getCustomName()))
                            continue;
                        if (entity.getType() == chosenType) ((LivingEntity) entity).setPersistent(false);
                        if (!mobSet.contains(entity.getType())) continue;

                        entity.setFireTicks(0);
                        if (entity.isInvulnerable()) continue;

                        entity.setInvulnerable(true);
                        entity.setSilent(true);
                        ((LivingEntity) entity).setPersistent(false);
                        ((LivingEntity) entity).getEquipment().clear();
                        ((LivingEntity) entity).setAI(false);
                        ((LivingEntity) entity).setCollidable(false);
                        ((LivingEntity) entity).getCollidableExemptions().clear();
                        ((LivingEntity) entity)
                                .addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 0));
                        if (entity.getType() == EntityType.SPIDER || entity.getType() == EntityType.CAVE_SPIDER
                                || entity.getType() == EntityType.ENDERMAN)
                            entity.remove();
                        entity.getWorld().spawnEntity(entity.getLocation(), chosenType);
                    }
                }

            }
        }.runTaskTimer(UhcCore.getPlugin(), 20 * 5, 0);

        getGameManager().broadcastMessage(
                "\u00a77All mobs for this game will be: \u00a7d\u00a7l" + chosenType.name() + "\u00a77!");
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        if (isActivated(Scenario.WITHERRUSH) && e.getEntityType() == EntityType.WITHER) return;
        if (chosenType == null) return;
        if (e.getEntityType() == chosenType) ((LivingEntity) e.getEntity()).setPersistent(false);
        if (!mobSet.contains(e.getEntityType())) return;

        e.getEntity().setInvulnerable(true);
        e.getEntity().setSilent(true);
        ((LivingEntity) e.getEntity()).setPersistent(false);
        ((LivingEntity) e.getEntity()).getEquipment().clear();
        ((LivingEntity) e.getEntity()).setAI(false);
        ((LivingEntity) e.getEntity()).setCollidable(false);
        ((LivingEntity) e.getEntity()).getCollidableExemptions().clear();
        ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 0));

        if (e.getEntityType() == EntityType.SPIDER || e.getEntityType() == EntityType.CAVE_SPIDER
                || e.getEntityType() == EntityType.ENDERMAN)
            e.getEntity().remove();
        e.getLocation().getWorld().spawnEntity(e.getLocation(), chosenType);
    }

}
