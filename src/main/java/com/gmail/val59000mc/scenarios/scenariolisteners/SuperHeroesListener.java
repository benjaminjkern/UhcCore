package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.security.auth.callback.NameCallback;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.ArmorEquipEvent;
import com.gmail.val59000mc.events.PlayerStartsPlayingEvent;
import com.gmail.val59000mc.events.UhcStartingEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.RandomUtils;
import com.gmail.val59000mc.utils.VersionUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SuperHeroesListener extends ScenarioListener {

    private Map<Player, SuperPower> allPowers;

    private static final List<PotionEffectType> goodEffects = Arrays.asList(new PotionEffectType[] {
            PotionEffectType.ABSORPTION, PotionEffectType.CONDUIT_POWER, PotionEffectType.DAMAGE_RESISTANCE,
            PotionEffectType.DOLPHINS_GRACE, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.SPEED,
            PotionEffectType.HEAL, PotionEffectType.HEALTH_BOOST, PotionEffectType.LUCK, PotionEffectType.JUMP,
            PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffectType.FAST_DIGGING, PotionEffectType.INVISIBILITY,
            PotionEffectType.NIGHT_VISION, PotionEffectType.SATURATION, PotionEffectType.SLOW_FALLING,
            PotionEffectType.WATER_BREATHING, PotionEffectType.REGENERATION, PotionEffectType.INCREASE_DAMAGE });

    private static final List<PotionEffectType> badEffects = Arrays.asList(new PotionEffectType[] {
            PotionEffectType.BAD_OMEN, PotionEffectType.UNLUCK, PotionEffectType.SLOW_DIGGING,
            PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM, PotionEffectType.HUNGER,
            PotionEffectType.LEVITATION, PotionEffectType.GLOWING, PotionEffectType.POISON, PotionEffectType.SLOW,
            PotionEffectType.WEAKNESS, PotionEffectType.WITHER });

    private static final List<Material> luckyDrops = Arrays.asList(new Material[] { Material.DIAMOND,
            Material.GOLD_INGOT, Material.IRON_INGOT, Material.EMERALD, Material.LAPIS_LAZULI });

    @EventHandler
    public void onActivate(UhcStartingEvent e) { allPowers = new HashMap<>(); }

    @EventHandler
    public void onGameStart(PlayerStartsPlayingEvent e) {
        try {
            SuperPower newPower = new SuperPower();
            allPowers.put(e.getUhcPlayer().getPlayer(), newPower);
            e.getUhcPlayer().getPlayer().sendMessage("\u00a75You have been given the power of the \u00a7d\u00a7l"
                    + newPower.powerType.name() + "\u00a75!");
            addHeroesEffect(e.getUhcPlayer(), newPower);
        } catch (Exception ex) {
            Bukkit.getLogger().warning(ex.toString());
        }
    }

    private void addHeroesEffect(UhcPlayer uhcPlayer, SuperPower power) {

        Player player;

        try {
            player = uhcPlayer.getPlayer();
        } catch (UhcPlayerNotOnlineException ex) {
            // No effect for offline player
            return;
        }

        switch (power.powerType) {
            case TANK:
                double maxHealth = 40;
                VersionUtils.getVersionUtils().setPlayerMaxHealth(player, maxHealth);
                player.setHealth(maxHealth);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 1));
                break;
            case GHOST:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 2));
                break;
            case BLAZE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0));
                player.sendMessage("\u00a75Sneak to use your powers!");
                player.sendMessage("\u00a75Hold Sneak to use a charged attack!");
                break;
            // case SPIDER:
            // player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, 1));
            // break;
            case LUCKY:
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 999999, 1));
                break;
            case VAMPIRE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 0));
                player.sendMessage("\u00a75Harvest the blood of living beings to regain health!");
                break;
            case ICEMAN:
                player.sendMessage("\u00a75Sneak to use your powers!");
                player.sendMessage("\u00a75Players should be wary of getting close to you!");
                new BukkitRunnable() {
                    public void run() {
                        player.getNearbyEntities(3, 3, 3).forEach(ent -> {
                            if (!(ent instanceof LivingEntity)) return;

                            ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2 * 20, 3));
                            ((LivingEntity) ent)
                                    .addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 2 * 20, 3));
                        });

                    }
                }.runTaskTimer(UhcCore.getPlugin(), 20, 20);
                break;
            case NECROMANCER:
                player.sendMessage("\u00a75Sneak to use your powers!");
                player.sendMessage("\u00a75Amass a hoard of the undead!");
                break;
            case POTIONMASTER:
                player.sendMessage("\u00a75Sneak to use your powers!");
                player.sendMessage("\u00a75Potions vary based on where you're looking!");
                break;
            case CREEPER:
                player.sendMessage("\u00a75Sneak to use your powers!");
                break;
            default:
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        Player p = (Player) e.getEntity();

        // if (e.getCause() == EntityDamageEvent.DamageCause.FALL
        // && allPowers.get(p).powerType == SuperPower.SuperPowerType.SPIDER)
        // e.setCancelled(true);

        if ((e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
                && allPowers.get(p).powerType == SuperPower.SuperPowerType.CREEPER)
            e.setDamage(0);
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if (damager instanceof Projectile) damager = (Entity) ((Projectile) damager).getShooter();

        boolean entityIsPlayer = e.getEntity() instanceof Player;
        boolean damagerIsPlayer = damager instanceof Player;

        if (!entityIsPlayer && !damagerIsPlayer) return;

        if (damagerIsPlayer) {
            Player playerDamager = (Player) damager;
            SuperPower damagerPower = allPowers.get(playerDamager);

            if (entityIsPlayer) {
                Player p = (Player) e.getEntity();
                SuperPower power = allPowers.get(p);

                if (power.powerType == SuperPower.SuperPowerType.NECROMANCER) {
                    power.use(p, () -> {
                        Mob m = spawnRandomUndead(p, 0.7, playerDamager);
                        p.sendMessage("\u00a75Being struck has caused you to summon a \u00a7d\u00a7l" + m.getType()
                                + "\u00a75!");
                    }, false);
                }
            }

            if (damagerPower.powerType == SuperPower.SuperPowerType.NECROMANCER) {
                findMinionsAndAttack(playerDamager, e.getEntity());
            }

            if (damagerPower.powerType == SuperPower.SuperPowerType.ICEMAN && e.getEntity() instanceof LivingEntity) {
                ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30 * 20, 1));
                ((LivingEntity) e.getEntity())
                        .addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 60 * 20, 1));
            }

            if (damagerPower.powerType == SuperPower.SuperPowerType.VAMPIRE) {
                e.getEntity().getWorld().spawnParticle(Particle.BLOCK_CRACK,
                        e.getEntity().getLocation().clone().add(0, 1, 0), 50, 0, 0, 0,
                        Bukkit.createBlockData(Material.REDSTONE_BLOCK));
                playerDamager.setHealth(Math.min(playerDamager.getHealth() + e.getDamage() * 0.05,
                        playerDamager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
            }

        } else {
            // dont do damage if its on your team
            if (isTeamMate(damager, e.getEntity())) {
                e.setCancelled(true);
                findNewTarget(damager);
                return;
            }

            if (e.getDamage() > ((LivingEntity) e.getEntity()).getHealth()) {
                findNewTarget(damager);
                return;
            }
        }

        if (entityIsPlayer) {
            Player p = (Player) e.getEntity();
            SuperPower power = allPowers.get(p);

            if (power.powerType != SuperPower.SuperPowerType.NECROMANCER) {
                // Set zombies to attack for you if they are nearby you
                findMinionsAndAttack(p, damager);
            }
        }
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {
        Player p = e.getPlayer();
        if (allPowers.get(p).powerType == SuperPower.SuperPowerType.GHOST) {
            e.setCancelled(true);
            p.sendMessage("\u00a75The armor you equipped falls right through your ghostly form!");
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        if (GameManager.getGameManager().getPlayersManager().getUhcPlayer(p).getState() != PlayerState.PLAYING) return;

        SuperPower power = allPowers.get(p);

        if (!e.isSneaking()) {
            if (power.powerType == SuperPower.SuperPowerType.BLAZE) {
                power.use(p, () -> {
                    Location loc = p.getLocation();
                    SmallFireball f = (SmallFireball) p.getWorld().spawnEntity(loc.clone().add(0, 1, 0),
                            EntityType.SMALL_FIREBALL);
                    p.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1, 0);
                    f.setVelocity(loc.getDirection().multiply(2));
                });
            }
            return;
        }

        switch (power.powerType) {
            case BLAZE:
                power.setHolding(p, () -> {
                    Location loc = p.getLocation();
                    LargeFireball f = (LargeFireball) p.getWorld()
                            .spawnEntity(loc.clone().add(loc.getDirection()).add(0, 1, 0), EntityType.FIREBALL);
                    p.getWorld().playSound(loc, Sound.ENTITY_GHAST_SHOOT, 1, 0);
                    f.setVelocity(loc.getDirection());
                });
                break;
            case CREEPER:
                power.use(p, () -> {
                    Location loc = p.getLocation();
                    p.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
                });
                break;
            case ICEMAN:
                power.use(p, () -> {
                    Location loc = p.getLocation();
                    // top and bottom
                    setIce(loc.clone().add(0, -1, 0));
                    setIce(loc.clone().add(0, 2, 0));
                    double yaw = loc.getYaw();

                    if (yaw < 270 - 45 || yaw >= 270 + 45) {
                        setIce(loc.clone().add(-1, 0, 0));
                        setIce(loc.clone().add(-1, 1, 0));
                    }
                    if (yaw < 90 - 45 || yaw >= 90 + 45) {
                        setIce(loc.clone().add(1, 0, 0));
                        setIce(loc.clone().add(1, 1, 0));
                    }
                    if (yaw < 180 - 45 || yaw >= 180 + 45) {
                        setIce(loc.clone().add(0, 0, 1));
                        setIce(loc.clone().add(0, 1, 1));
                    }
                    if (yaw < 360 - 45 && yaw >= 0 + 45) {
                        setIce(loc.clone().add(0, 0, -1));
                        setIce(loc.clone().add(0, 1, -1));
                    }
                });
                break;
            case NECROMANCER:
                power.use(p, () -> {
                    Mob m = spawnRandomUndead(p, 0.9, p);
                    p.sendMessage("\u00a75You summoned a \u00a7d\u00a7l" + m.getType() + "\u00a75!");
                });
                break;
            case POTIONMASTER:
                power.use(p, () -> {
                    Location loc = p.getLocation();
                    List<PotionEffectType> effectList = loc.getDirection().angle(new Vector(0, -1, 0)) < Math.PI / 4
                            ? goodEffects
                            : badEffects;

                    PotionEffect potionEffect = new PotionEffect(
                            effectList.get((int) (Math.random() * effectList.size())),
                            RandomUtils.randomInteger(1, 60) * 20, RandomUtils.randomInteger(0, 2));

                    ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);
                    PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

                    potionMeta.addCustomEffect(potionEffect, true);

                    itemStack.setItemMeta(potionMeta);

                    ThrownPotion thrownPotion = (ThrownPotion) p.getWorld().spawnEntity(loc.clone().add(0, 1, 0),
                            EntityType.SPLASH_POTION);
                    thrownPotion.setItem(itemStack);
                    thrownPotion.setVelocity(loc.getDirection());
                });
                break;
            default:
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (allPowers.get(p).powerType == SuperPower.SuperPowerType.LUCKY) {
            if (e.getBlock().getType() == Material.STONE
                    && !p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)
                    && Math.random() < 0.1) {
                e.getBlock().getWorld().dropItem(e.getBlock().getLocation(),
                        new ItemStack(luckyDrops.get((int) (Math.random() * luckyDrops.size()))));
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player)) return;
        Player p = (Player) e.getTarget();

        if (allPowers.get(p).powerType == SuperPower.SuperPowerType.LUCKY) { e.setTarget(null); }
        if (allPowers.get(p).powerType == SuperPower.SuperPowerType.NECROMANCER) {
            findMinionsAndAttack(p, e.getEntity());
        }
    }

    private void setIce(Location l) {
        if (!l.getBlock().getType().isSolid()) {
            l.getBlock().setType(Material.ICE);
            ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE);
            pick.addEnchantment(Enchantment.SILK_TOUCH, 1);
            Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
                l.getBlock().getDrops().clear();
                l.getBlock().breakNaturally(pick);
            }, 20 * 10);
        }
    }

    @EventHandler
    public void onEntityKilled(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        if (entity.getCustomName() == null) return;
        String[] splitName = entity.getCustomName().split("'s ");
        if (splitName.length != 2) return;
        Player player = Bukkit.getPlayerExact(splitName[0]);
        String name = splitName[1];

        if (player == null) return;
        player.sendMessage("Your " + name + " has died.");

    }

    private void findNewTarget(Entity minion) {
        if (!(minion instanceof Mob) || minion.getCustomName() == null) return;
        String name = minion.getCustomName().split("'s ")[0];
        Player owner = Bukkit.getPlayerExact(name);

        // if cant find anything, just return to owner
        ((Mob) minion).setTarget(owner);

        for (Entity entity : minion.getNearbyEntities(10, 10, 10)) {
            // only go for mobs
            if (!(entity instanceof Mob) || isTeamMate(minion, entity)) continue;
            // dont go for shared ownership
            ((Mob) minion).setTarget((Mob) entity);
            break;
        }
    }

    private boolean isTeamMate(Entity checking, Entity toCheck) {

        if (checking == toCheck) return true;

        Player checkingOwner = null;
        Player toCheckOwner = null;
        PlayersManager pm = GameManager.getGameManager().getPlayersManager();

        if (checking instanceof Player) checkingOwner = (Player) checking;
        else if (checking instanceof Tameable) {
            AnimalTamer owner = ((Tameable) checking).getOwner();
            if (owner instanceof Player) checkingOwner = (Player) owner;
        } else if (checking instanceof Mob && checking.getCustomName() != null)
            checkingOwner = Bukkit.getPlayerExact(checking.getCustomName().split("'s ")[0]);
        else return false;

        if (toCheck instanceof Player) toCheckOwner = (Player) toCheck;
        else if (toCheck instanceof Tameable) {
            AnimalTamer owner = ((Tameable) toCheck).getOwner();
            if (owner instanceof Player) toCheckOwner = (Player) owner;
        } else if (toCheck instanceof Mob && toCheck.getCustomName() != null)
            toCheckOwner = Bukkit.getPlayerExact(toCheck.getCustomName().split("'s ")[0]);
        else return false;

        return pm.getUhcPlayer(checkingOwner).getTeam() == pm.getUhcPlayer(toCheckOwner).getTeam();
    }

    private Mob spawnRandomUndead(Player p, double rate, Entity toTarget) {
        Location loc = p.getLocation();
        double r = Math.random();

        Mob m;

        if (r > 1 - rate) {
            double r2 = Math.random();
            if (r2 > 0.4) {
                m = (Zombie) p.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                m.setCustomName(p.getName() + "'s Zombie");
            } else if (r2 > 0.3) {
                m = (Husk) p.getWorld().spawnEntity(loc, EntityType.HUSK);
                m.setCustomName(p.getName() + "'s Husk");
            } else if (r2 > 0.2) {
                m = (PigZombie) p.getWorld().spawnEntity(loc, EntityType.ZOMBIFIED_PIGLIN);
                m.setCustomName(p.getName() + "'s Zombified Piglin");
            } else if (r2 > 0.1) {
                m = (Drowned) p.getWorld().spawnEntity(loc, EntityType.DROWNED);
                m.setCustomName(p.getName() + "'s Drowned");
            } else {
                m = (ZombieVillager) p.getWorld().spawnEntity(loc, EntityType.ZOMBIE_VILLAGER);
                m.setCustomName(p.getName() + "'s Zombie Villager");
            }
        } else if (r > Math.pow(1 - rate, 2)) {
            double r2 = Math.random();
            if (r2 > 0.3) {
                m = (Skeleton) p.getWorld().spawnEntity(loc, EntityType.SKELETON);
                m.setCustomName(p.getName() + "'s Skeleton");
            } else if (r2 > 0.15) {
                m = (Stray) p.getWorld().spawnEntity(loc, EntityType.STRAY);
                m.setCustomName(p.getName() + "'s Stray");
            } else {
                m = (WitherSkeleton) p.getWorld().spawnEntity(loc, EntityType.WITHER_SKELETON);
                m.setCustomName(p.getName() + "'s Wither Skeleton");
            }
        } else if (r > Math.pow(1 - rate, 3)) {
            m = (Phantom) p.getWorld().spawnEntity(loc, EntityType.PHANTOM);
            m.setCustomName(p.getName() + "'s Phantom");
        } else if (r > Math.pow(1 - rate, 4)) {
            m = (Zoglin) p.getWorld().spawnEntity(loc, EntityType.ZOGLIN);
            m.setCustomName(p.getName() + "'s Zoglin");
        } else if (r > Math.pow(1 - rate, 5)) {
            double r2 = Math.random();
            if (r2 > 0.25) {
                m = (ZombieHorse) p.getWorld().spawnEntity(loc, EntityType.ZOMBIE_HORSE);
                m.setCustomName(p.getName() + "'s Zombie Horse");
                ((ZombieHorse) m).setOwner(p);
                ((ZombieHorse) m).getInventory().setSaddle(new ItemStack(Material.SADDLE));
            } else {
                m = (SkeletonHorse) p.getWorld().spawnEntity(loc, EntityType.SKELETON_HORSE);
                m.setCustomName(p.getName() + "'s Skeleton Horse");
                ((SkeletonHorse) m).setOwner(p);
                ((SkeletonHorse) m).getInventory().setSaddle(new ItemStack(Material.SADDLE));
            }
        } else {
            m = (Wither) p.getWorld().spawnEntity(loc, EntityType.WITHER);
            m.setCustomName(p.getName() + "'s Wither");
        }

        Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new CheckEntityTeleport(m, p), 20 * 10);
        if (toTarget == null || !(toTarget instanceof LivingEntity)) return m;
        m.setTarget((LivingEntity) toTarget);
        return m;
    }

    private void findMinionsAndAttack(Player p, Entity attack) {
        if (!(attack instanceof LivingEntity)) return;
        for (Entity e : p.getNearbyEntities(10, 10, 10)) {
            if (!(e instanceof Mob) || e == p || e == attack || isTeamMate(e, attack) || !isTeamMate(e, p)) continue;
            ((Mob) e).setTarget((LivingEntity) attack);
        }
    }

    /*
     * 
     * - Spiderman: - You can climb walls, you fall down walls slowly like ladders,
     * you can sneak to stay still on a wall, You get jump boost and dont take fall
     * damage
     * 
     * THIS IS HARD TO DO IM GONNA WAIT AND LOOK
     */

    private static class SuperPower {
        private enum SuperPowerType {
            TANK, GHOST, BLAZE(15, 5), CREEPER(15, 2), ICEMAN(15), NECROMANCER(15), VAMPIRE, POTIONMASTER(15, 5), LUCKY;
            // SPIDER

            private final int maxCooldown;

            private final int holdCooldown;

            private final int usesBeforeCooldown;

            SuperPowerType() { this(0, 1); }

            SuperPowerType(int cooldown) { this(cooldown, 1); }

            SuperPowerType(int cooldown, int uses) {
                this.maxCooldown = cooldown;
                usesBeforeCooldown = uses;
                holdCooldown = 2;
            }
        }

        private boolean canUse;
        private boolean holding;
        private SuperPowerType powerType;
        private int timesUsed;
        private double cooldown;

        SuperPower() {
            powerType = SuperPowerType.values()[(int) (Math.random() * SuperPowerType.values().length)];
            canUse = true;
            timesUsed = 0;
            cooldown = -1;
        }

        void setCanUse(Player p) {
            canUse = true;
            p.sendMessage(UhcCore.PREFIX + "\u00a7fPower Recharged!");
        }

        void setHolding(Player p, Runnable ifPass) {
            if (canUse) {
                holding = true;
                Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
                    if (holding && canUse) {
                        ifPass.run();
                        timesUsed = 0;
                        canUse = false;
                        cooldown = powerType.maxCooldown;
                        Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> setCanUse(p),
                                (int) (20 * cooldown));
                    }
                }, 20 * powerType.holdCooldown);
            } else if (cooldown > 1) p.sendMessage(
                    UhcCore.PREFIX + "\u00a7cYou must wait \u00a7f" + cooldown + " \u00a7cseconds before recharge!");
        }

        void use(Player p, Runnable r) { use(p, r, true); }

        void use(Player p, Runnable r, boolean talk) {
            holding = false;

            if (canUse) {
                Bukkit.getScheduler().runTask(UhcCore.getPlugin(), r);
                canUse = false;
                if (timesUsed >= powerType.usesBeforeCooldown - 1) {
                    cooldown = powerType.maxCooldown;
                    timesUsed = 0;
                    Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> setCanUse(p), (int) (20 * cooldown));
                } else {
                    timesUsed++;
                    cooldown = 0.5;
                    Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> { canUse = true; },
                            (int) (20 * cooldown));
                    Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
                        if (timesUsed > 0) {
                            timesUsed = 0;
                            p.sendMessage(UhcCore.PREFIX + "\u00a7fPower Recharged!");
                        }
                    }, (int) (20 * powerType.maxCooldown));
                }
            } else if (!holding && cooldown > 1 && talk) p.sendMessage(
                    UhcCore.PREFIX + "\u00a7cYou must wait \u00a7f" + cooldown + " \u00a7cseconds before recharge!");
        }

    }

    private class CheckEntityTeleport implements Runnable {
        Entity entity;
        Player player;
        CheckEntityTeleport task;

        CheckEntityTeleport(Entity w, Player p) {
            this.entity = w;
            this.player = p;
            task = this;
        }

        public void run() {
            if (entity != null) {
                if (entity.getLocation().distanceSquared(player.getLocation()) > 50 * 50) { entity.teleport(player); }
                Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20 * 10);
            }
        }
    }

}