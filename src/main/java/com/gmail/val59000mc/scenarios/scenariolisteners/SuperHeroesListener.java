package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.ArmorEquipEvent;
import com.gmail.val59000mc.events.PlayerStartsPlayingEvent;
import com.gmail.val59000mc.events.UhcPlayerDeathEvent;
import com.gmail.val59000mc.events.UhcStartingEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.listeners.PlayerDeathListener;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.RandomUtils;
import com.gmail.val59000mc.utils.VersionUtils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
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
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class SuperHeroesListener extends ScenarioListener {

    private static Map<UhcPlayer, SuperPower> allPowers;

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
        new BukkitRunnable() {
            public void run() {
                try {
                    Bukkit.getLogger().info("Making superpower for " + e.getUhcPlayer().getDisplayName());
                    SuperPower newPower = new SuperPower();
                    UhcPlayer uhcPlayer = e.getUhcPlayer();
                    allPowers.put(uhcPlayer, newPower);
                    uhcPlayer.getPlayer().sendTitle("",
                            "\u00a75You have the power of the \u00a7d\u00a7l" + newPower.powerType.name() + "\u00a75!");
                    uhcPlayer.getPlayer().sendMessage(
                            "\u00a75You have the power of the \u00a7d\u00a7l" + newPower.powerType.name() + "\u00a75!");
                    addHeroesEffect(uhcPlayer, newPower, true);
                } catch (Exception ex) {
                    Bukkit.getLogger().warning(ex.toString());
                }
            }
        }.runTaskLater(UhcCore.getPlugin(), 20);

        new BukkitRunnable() {
            public void run() {
                try {
                    UhcPlayer uhcPlayer = e.getUhcPlayer();
                    addHeroesEffect(uhcPlayer, getPower(uhcPlayer), false);
                } catch (Exception ex) {
                    Bukkit.getLogger().warning(ex.toString());
                }
            }
        }.runTaskTimer(UhcCore.getPlugin(), 20 * 5, 20 * 5);
    }

    public static void addHeroesEffect(UhcPlayer uhcPlayer, SuperPower power, boolean sendMessage) {

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
                if (GameManager.getGameManager().getScenarioManager().isActivated(Scenario.SUDDENDEATH)) maxHealth = 6;
                if (GameManager.getGameManager().getScenarioManager().isActivated(Scenario.ACHIEVEMENTHUNTER))
                    maxHealth = 10;
                if (sendMessage) VersionUtils.getVersionUtils().setPlayerMaxHealth(player, maxHealth);
                if (sendMessage) player.setHealth(maxHealth);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 1));
                if (sendMessage) for (int i = 0; i < 10; i++) {
                    Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent("\u00a75You are slow, but strong and sturdy!"));
                    }, 20 * i);
                }
                break;
            case GHOST:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 2));
                if (sendMessage) for (int i = 0; i < 10; i++) {
                    Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(),
                            () -> {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                        new TextComponent("\u00a75No one can see you!"));
                            }, 20 * i);
                }
                break;
            case BLAZE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0));
                if (sendMessage) for (int i = 0; i < 10; i++) {
                    Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                "\u00a75Sneak to use your powers, or hold sneak to use a charged attack!"));
                    }, 20 * i);
                }
                break;
            // case SPIDER:
            // player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, 1));
            // break;
            case LUCKY:
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 999999, 1));
                if (sendMessage) for (int i = 0; i < 10; i++) {
                    Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                "\u00a75Mining gives you extra goodies, and mobs will leave you alone!"));
                    }, 20 * i);
                }
                break;
            case VAMPIRE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 0));
                if (sendMessage) for (int i = 0; i < 10; i++) {
                    Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent("\u00a75Harvest the blood of living beings to regain health!"));
                    }, 20 * i);
                }
                break;
            case ICEMAN:
                if (sendMessage) {
                    for (int i = 0; i < 10; i++) {
                        Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                    "\u00a75Sneak to use your powers, or attack players to freeze them!"));
                        }, 20 * i);
                    }
                    new BukkitRunnable() {
                        public void run() {
                            if (uhcPlayer.getState() != PlayerState.PLAYING) {
                                this.cancel();
                                return;
                            }
                            player.getNearbyEntities(5, 5, 5).forEach(ent -> {
                                if (!(ent instanceof LivingEntity) || isTeamMate(player, ent)) return;

                                if (ent instanceof Player) {
                                    if (GameManager.getGameManager().getPlayersManager().getUhcPlayer((Player) ent)
                                            .getState() != PlayerState.PLAYING)
                                        return;
                                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                            "\u00a7bYou have been partially frozen because you are near an \u00a7fICE MAN\u00a7b!"));
                                }

                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                        new TextComponent("\u00a7bYou have partially frozen a nearby \u00a7f"
                                                + ent.getType() + "\u00a7b!"));
                                ((LivingEntity) ent)
                                        .addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2 * 20, 3));
                                ((LivingEntity) ent)
                                        .addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 2 * 20, 3));
                            });

                        }
                    }.runTaskTimer(UhcCore.getPlugin(), 20, 20);
                }
                break;
            case NECROMANCER:
                if (sendMessage) for (int i = 0; i < 10; i++) {
                    Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent("\u00a75Sneak to use your powers! Amass a hoard of the undead!"));
                    }, 20 * i);
                }
                break;
            case POTIONMASTER:
                if (sendMessage) for (int i = 0; i < 10; i++) {
                    Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                "\u00a75Sneak to use your powers! Potions vary based on whether you're looking up or down!"));
                    }, 20 * i);
                }
                break;
            case CREEPER:
                if (sendMessage) for (int i = 0; i < 10; i++) {
                    Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent("\u00a75Sneak to use your powers! Have fun!"));
                    }, 20 * i);
                }
                break;
            default:
        }
    }

    @EventHandler
    public void onDeath(UhcPlayerDeathEvent e) {
        if (PlayerDeathListener.autoRespawn) addHeroesEffect(e.getKilled(), getPower(e.getKilled()), false);
    }

    @EventHandler
    public void onPlayerDamage(EntityPotionEffectEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        Player p = (Player) e.getEntity();
        UhcPlayer uhcPlayer = getPlayersManager().getUhcPlayer(p);
        if (uhcPlayer.getState() != PlayerState.PLAYING) return;
        SuperPower power = allPowers.get(uhcPlayer);

        // if (e.getCause() == EntityDamageEvent.DamageCause.FALL
        // && allPowers.get(p).powerType == SuperPower.SuperPowerType.SPIDER)
        // e.setCancelled(true);

        if (e.getNewEffect() != null && badEffects.contains(e.getNewEffect().getType())
                && power.powerType == SuperPower.SuperPowerType.POTIONMASTER) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        Player p = (Player) e.getEntity();
        UhcPlayer uhcPlayer = getPlayersManager().getUhcPlayer(p);
        if (uhcPlayer.getState() != PlayerState.PLAYING) return;
        SuperPower power = allPowers.get(uhcPlayer);

        // if (e.getCause() == EntityDamageEvent.DamageCause.FALL
        // && allPowers.get(p).powerType == SuperPower.SuperPowerType.SPIDER)
        // e.setCancelled(true);

        if ((e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
                && power.powerType == SuperPower.SuperPowerType.CREEPER) {
            e.setCancelled(true);
            e.setDamage(0);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if (damager instanceof Projectile) damager = (Entity) ((Projectile) damager).getShooter();

        boolean damagedIsPlayer = e.getEntity() instanceof Player
                && getPlayersManager().getUhcPlayer((Player) e.getEntity()).getState() == PlayerState.PLAYING;
        boolean damagerIsPlayer = damager instanceof Player
                && getPlayersManager().getUhcPlayer((Player) damager).getState() == PlayerState.PLAYING;

        if (!damagedIsPlayer && !damagerIsPlayer) return;

        if (damagerIsPlayer) {
            Player playerDamager = (Player) damager;
            UhcPlayer uhcDamager = getPlayersManager().getUhcPlayer(playerDamager);

            SuperPower damagerPower = getPower(uhcDamager);

            if (damagedIsPlayer && getGameManager().getPvp()) {
                Player damaged = (Player) e.getEntity();
                UhcPlayer uhcDamaged = getPlayersManager().getUhcPlayer(damaged);
                SuperPower damagedPower = getPower(uhcDamaged);

                if (damagedPower.powerType == SuperPower.SuperPowerType.NECROMANCER
                        && !isTeamMate(playerDamager, damaged)) {
                    damagedPower.use(damaged, () -> {
                        Mob m = spawnRandomUndead(damaged, 0.7, playerDamager);
                        damaged.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent("\u00a75Being struck has caused you to summon a \u00a7d\u00a7l"
                                        + m.getType() + "\u00a75!"));
                    }, false);
                }
            }

            if (damagerPower.powerType == SuperPower.SuperPowerType.NECROMANCER) {
                findMinionsAndAttack(playerDamager, e.getEntity());
            }

            if (damagerPower.powerType == SuperPower.SuperPowerType.ICEMAN && e.getEntity() instanceof LivingEntity
                    && !isTeamMate(e.getEntity(), damager)) {
                if (e.getEntity() instanceof Player) ((Player) e.getEntity()).spigot()
                        .sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("\u00a7bYou were frozen!"));
                ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 2, 100));
                ((LivingEntity) e.getEntity())
                        .addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 2, 1));
                ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 2, 200));
            }

            if (damagerPower.powerType == SuperPower.SuperPowerType.BLAZE && e.getEntity() instanceof LivingEntity
                    && !isTeamMate(e.getEntity(), damager)) {
                if (e.getEntity() instanceof Player) ((Player) e.getEntity()).spigot()
                        .sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("\u00a7cYou were set ablaze!"));
                ((LivingEntity) e.getEntity()).setFireTicks(20 * 5);
            }

            if (damagerPower.powerType == SuperPower.SuperPowerType.VAMPIRE && e.getDamager() instanceof Player
                    && !isTeamMate(e.getEntity(), damager)) {
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

            // if they killed it
            if (e.getDamage() > ((LivingEntity) e.getEntity()).getHealth()) {
                findNewTarget(damager);
                return;
            }
        }

        if (damagedIsPlayer) {
            Player damaged = (Player) e.getEntity();
            UhcPlayer uhcDamaged = getPlayersManager().getUhcPlayer(damaged);
            SuperPower damagedPower = getPower(uhcDamaged);

            if (damagedPower.powerType == SuperPower.SuperPowerType.NECROMANCER) {
                // Set zombies to attack for you if they are nearby you
                findMinionsAndAttack(damaged, damager);
            }
            if (damagedPower.powerType == SuperPower.SuperPowerType.ICEMAN) {
                if (!(damager instanceof LivingEntity)) return;
                if (damager instanceof Player) {
                    ((Player) e.getDamager()).spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                            "\u00a7bYou have been partially frozen because you struck an \u00a7f\u00a7lICE MAN\u00a7b!"));
                }

                damaged.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("\u00a7bYou have partially frozen a nearby \u00a7f" + e.getDamager().getType()
                                + " \u00a7bafter being struck!"));
                ((LivingEntity) damager).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 5));
                ((LivingEntity) damager).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 5 * 20, 5));
            }
        }
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {
        Player p = e.getPlayer();
        UhcPlayer uhcPlayer = getPlayersManager().getUhcPlayer(p);
        if (uhcPlayer.getState() != PlayerState.PLAYING) return;
        SuperPower power = allPowers.get(uhcPlayer);

        if (power.powerType == SuperPower.SuperPowerType.GHOST) {
            e.setCancelled(true);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent("\u00a75The armor you equipped falls right through your ghostly form!"));
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        UhcPlayer uhcPlayer = getPlayersManager().getUhcPlayer(p);
        if (uhcPlayer.getState() != PlayerState.PLAYING) return;
        SuperPower power = allPowers.get(uhcPlayer);

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
                    TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(30);
                    tnt.setSource(p);
                });
                break;
            case ICEMAN:
                power.use(p, () -> {
                    Location loc = p.getLocation();
                    // top and bottom
                    setIce(loc.clone().add(0, -1, 0));
                    setIce(loc.clone().add(0, -1, 1));
                    setIce(loc.clone().add(0, -1, -1));
                    setIce(loc.clone().add(1, -1, 0));
                    setIce(loc.clone().add(-1, -1, 0));

                    setIce(loc.clone().add(0, 3, 0));
                    setIce(loc.clone().add(0, 3, 1));
                    setIce(loc.clone().add(0, 3, -1));
                    setIce(loc.clone().add(1, 3, 0));
                    setIce(loc.clone().add(-1, 3, 0));
                    double yaw = loc.getYaw();
                    while (yaw >= 360) yaw -= 360;
                    while (yaw < 0) yaw += 360;

                    if (yaw >= 270 - 45 && yaw < 270 + 45) {
                        setIce(loc.clone().add(1, 0, 1));
                        setIce(loc.clone().add(1, 1, 1));
                        setIce(loc.clone().add(1, 2, 1));
                        setIce(loc.clone().add(1, 0, -1));
                        setIce(loc.clone().add(1, 1, -1));
                        setIce(loc.clone().add(1, 2, -1));
                        setIce(loc.clone().add(2, 0, 0));
                        setIce(loc.clone().add(2, 1, 0));
                        setIce(loc.clone().add(2, 2, 0));
                        setIce(loc.clone().add(0, 0, 2));
                        setIce(loc.clone().add(0, 1, 2));
                        setIce(loc.clone().add(0, 2, 2));
                        setIce(loc.clone().add(0, 0, -2));
                        setIce(loc.clone().add(0, 1, -2));
                        setIce(loc.clone().add(0, 2, -2));
                    }
                    if (yaw >= 90 - 45 && yaw < 90 + 45) {
                        setIce(loc.clone().add(-1, 0, 1));
                        setIce(loc.clone().add(-1, 1, 1));
                        setIce(loc.clone().add(-1, 2, 1));
                        setIce(loc.clone().add(-1, 0, -1));
                        setIce(loc.clone().add(-1, 1, -1));
                        setIce(loc.clone().add(-1, 2, -1));
                        setIce(loc.clone().add(-2, 0, 0));
                        setIce(loc.clone().add(-2, 1, 0));
                        setIce(loc.clone().add(-2, 2, 0));
                        setIce(loc.clone().add(0, 0, 2));
                        setIce(loc.clone().add(0, 1, 2));
                        setIce(loc.clone().add(0, 2, 2));
                        setIce(loc.clone().add(0, 0, -2));
                        setIce(loc.clone().add(0, 1, -2));
                        setIce(loc.clone().add(0, 2, -2));
                    }
                    if (yaw >= 180 - 45 && yaw < 180 + 45) {
                        setIce(loc.clone().add(1, 0, -1));
                        setIce(loc.clone().add(1, 1, -1));
                        setIce(loc.clone().add(1, 2, -1));
                        setIce(loc.clone().add(-1, 0, -1));
                        setIce(loc.clone().add(-1, 1, -1));
                        setIce(loc.clone().add(-1, 2, -1));
                        setIce(loc.clone().add(0, 0, -2));
                        setIce(loc.clone().add(0, 1, -2));
                        setIce(loc.clone().add(0, 2, -2));
                        setIce(loc.clone().add(2, 0, 0));
                        setIce(loc.clone().add(2, 1, 0));
                        setIce(loc.clone().add(2, 2, 0));
                        setIce(loc.clone().add(-2, 0, 0));
                        setIce(loc.clone().add(-2, 1, 0));
                        setIce(loc.clone().add(-2, 2, 0));
                    }
                    if (yaw >= 360 - 45 || yaw < 0 + 45) {
                        setIce(loc.clone().add(1, 0, 1));
                        setIce(loc.clone().add(1, 1, 1));
                        setIce(loc.clone().add(1, 2, 1));
                        setIce(loc.clone().add(-1, 0, 1));
                        setIce(loc.clone().add(-1, 1, 1));
                        setIce(loc.clone().add(-1, 2, 1));
                        setIce(loc.clone().add(0, 0, 2));
                        setIce(loc.clone().add(0, 1, 2));
                        setIce(loc.clone().add(0, 2, 2));
                        setIce(loc.clone().add(2, 0, 0));
                        setIce(loc.clone().add(2, 1, 0));
                        setIce(loc.clone().add(2, 2, 0));
                        setIce(loc.clone().add(-2, 0, 0));
                        setIce(loc.clone().add(-2, 1, 0));
                        setIce(loc.clone().add(-2, 2, 0));
                    }
                });
                break;
            case NECROMANCER:
                power.use(p, () -> {
                    Mob m = spawnRandomUndead(p, 0.9, p);
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent("\u00a75You summoned a \u00a7d\u00a7l" + m.getType() + "\u00a75!"));
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

                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent("\u00a77You threw a potion of \u00a75"
                                    + potionEffect.getType().getName().replace("_", " ") + "\u00a77!"));

                    ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);
                    PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
                    potionMeta.setColor(Color.fromRGB((int) (Math.random() * 256), (int) (Math.random() * 256),
                            (int) (Math.random() * 256)));

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
        UhcPlayer uhcPlayer = getPlayersManager().getUhcPlayer(p);
        if (uhcPlayer.getState() != PlayerState.PLAYING) return;
        SuperPower power = allPowers.get(uhcPlayer);

        if (power.powerType == SuperPower.SuperPowerType.LUCKY) {
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
        UhcPlayer uhcPlayer = getPlayersManager().getUhcPlayer(p);
        if (uhcPlayer.getState() != PlayerState.PLAYING) return;
        SuperPower power = allPowers.get(uhcPlayer);

        if (power.powerType == SuperPower.SuperPowerType.LUCKY) { e.setTarget(null); }
        if (power.powerType == SuperPower.SuperPowerType.NECROMANCER) { findMinionsAndAttack(p, e.getEntity()); }
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

    private static boolean isTeamMate(Entity checking, Entity toCheck) {

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

        if (checkingOwner == null || toCheckOwner == null) return false;

        UhcPlayer uhcCheckingOwner = pm.getUhcPlayer(checkingOwner);
        UhcPlayer uhcToCheckOwner = pm.getUhcPlayer(toCheckOwner);

        if (uhcCheckingOwner == null || uhcToCheckOwner == null) return false;
        return uhcCheckingOwner.getTeam() == uhcToCheckOwner.getTeam();
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

    public static SuperPower getPower(UhcPlayer uhcPlayer) {
        if (allPowers.containsKey(uhcPlayer)) return allPowers.get(uhcPlayer);
        return new SuperPower(SuperPower.SuperPowerType.NONE);
    }

    /*
     * 
     * - Spiderman: - You can climb walls, you fall down walls slowly like ladders,
     * you can sneak to stay still on a wall, You get jump boost and dont take fall
     * damage
     * 
     * THIS IS HARD TO DO IM GONNA WAIT AND LOOK
     */

    public static class SuperPower {
        public enum SuperPowerType {
            TANK, GHOST, BLAZE(10, 5), CREEPER(5, 3), ICEMAN(3), NECROMANCER(15), VAMPIRE, POTIONMASTER(5, 5), LUCKY,
            NONE;
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
        public SuperPowerType powerType;
        private int timesUsed;
        private double cooldown;

        SuperPower(SuperPowerType type) {
            powerType = type;
            canUse = true;
            timesUsed = 0;
            cooldown = -1;
        }

        SuperPower() {
            powerType = SuperPowerType.values()[(int) (Math.random() * (SuperPowerType.values().length - 1))];
            canUse = true;
            timesUsed = 0;
            cooldown = -1;
        }

        void setCanUse(Player p) {
            canUse = true;

            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("\u00a7d\u00a7lPower Recharged!"));
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
            } else if (cooldown > 1) p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent("\u00a7cYou must wait \u00a7f" + cooldown + " \u00a7cseconds before recharge!"));
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
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                    new TextComponent("\u00a7d\u00a7lPower Recharged!"));
                        }
                    }, (int) (20 * powerType.maxCooldown));
                }
            } else if (!holding && cooldown > 1 && talk) p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent("\u00a7cYou must wait \u00a7f" + cooldown + " \u00a7cseconds before recharge!"));
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