package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.RandomUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

public class EggsScenarioListener extends ScenarioListener {

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

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntityType() != EntityType.EGG) { return; }

        EntityType type = getRandomEntity();
        Location loc = e.getEntity().getLocation();
        loc.getWorld().spawnEntity(loc, type);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntityType() != EntityType.CHICKEN && e.getEntityType() != EntityType.PARROT) return;
        e.getDrops().add(new ItemStack(Material.EGG));
    }

    private EntityType getRandomEntity() { return MOBS[RandomUtils.randomInteger(0, MOBS.length - 1)]; }

}