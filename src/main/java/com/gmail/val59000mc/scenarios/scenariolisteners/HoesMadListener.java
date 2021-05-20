package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class HoesMadListener extends ScenarioListener {

    private static Set<UhcPlayer> cooldown = new HashSet<>();

    public static boolean isHoe(ItemStack i) {
        if (i == null) return false;
        switch (i.getType()) {
            case GOLDEN_HOE:
            case IRON_HOE:
            case WOODEN_HOE:
            case DIAMOND_HOE:
            case NETHERITE_HOE:
            case STONE_HOE:
                return true;
            default:
        }
        return false;
    }

    private static int getDist(ItemStack i) {
        switch (i.getType()) {
            case GOLDEN_HOE:
                return 5;
            case IRON_HOE:
                return 20;
            case WOODEN_HOE:
                return 10;
            case DIAMOND_HOE:
                return 25;
            case STONE_HOE:
                return 15;
            case NETHERITE_HOE:
                return 30;
            default:
        }
        return 0;
    }

    public static int getDamage(ItemStack i) {
        switch (i.getType()) {
            case GOLDEN_HOE:
                return 21;
            case IRON_HOE:
                return 12;
            case WOODEN_HOE:
                return 6;
            case STONE_HOE:
                return 9;
            case DIAMOND_HOE:
                return 15;
            case NETHERITE_HOE:
                return 18;
            default:
        }
        return 0;
    }

    private static Particle getParticle(ItemStack i) {
        switch (i.getType()) {
            case GOLDEN_HOE:
                return Particle.DRIPPING_HONEY;
            case IRON_HOE:
                return Particle.NOTE;
            case WOODEN_HOE:
                return Particle.COMPOSTER;
            case STONE_HOE:
                return Particle.SMOKE_NORMAL;
            case DIAMOND_HOE:
                return Particle.SOUL_FIRE_FLAME;
            case NETHERITE_HOE:
                return Particle.HEART;
            default:
        }
        return Particle.SPELL;
    }

    @EventHandler
    public void onClickEntity(PlayerInteractEntityEvent e) { shootLaser(e.getPlayer()); }

    @EventHandler
    public void onHorseRide(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        shootLaser(e.getPlayer());
    }

    public static void shootLaser(Player player) {
        if (player == null) return;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isHoe(hand)) return;
        UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayersManager().getUhcPlayer(player);

        if (!cooldown.contains(uhcPlayer)) {

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_DEATH, 1, 0);

            hand.setDurability((short) (hand.getDurability() + 5 - hand.getEnchantmentLevel(Enchantment.DURABILITY)));
            if (hand.getDurability() > hand.getType().getMaxDurability()) {
                hand.setAmount(0);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 0);
            }

            for (Vector dir : getDirs(player.getEyeLocation(), hand)) {
                createLaser(player.getEyeLocation(), dir, hand, player);
            }
            cooldown.add(uhcPlayer);
            new BukkitRunnable() {
                double count = 3 - 2 * hand.getEnchantmentLevel(Enchantment.DIG_SPEED) / 5.;

                public void run() {
                    if (count <= 0) {
                        cancel();
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                        cooldown.remove(uhcPlayer);
                    } else {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                "\u00a7cCooldown: \u00a7f" + String.format("%.1f", count) + " \u00a7cseconds"));
                        count -= 0.1;
                    }
                }
            }.runTaskTimer(UhcCore.getPlugin(), 0, 2);
        }
    }

    private static void createLaser(Location position, Vector dir, ItemStack hand, Player player) {
        Location p = position.clone();
        for (int i = 0; i < getDist(hand); i++) {
            p.add(dir);
            p.getWorld().spawnParticle(getParticle(hand), p, 10);
        }
        RayTraceResult r = position.getWorld().rayTraceEntities(position.clone().add(dir), dir, getDist(hand));
        if (r == null) return;
        Entity hitEntity = r.getHitEntity();
        if (!(hitEntity instanceof LivingEntity)) return;

        player.playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_HURT, 1, 0);

        ((LivingEntity) hitEntity).damage(getDamage(hand), player);
    }

    private static List<Vector> getDirs(Location source, ItemStack hand) {
        List<Vector> returnList = new ArrayList<>();
        Location s;
        switch (hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)) {
            case 3:
                s = source.clone();
                s.setYaw(source.getYaw() + 7);
                s.setPitch(source.getPitch() + 7);
                returnList.add(s.getDirection());

                s = source.clone();
                s.setYaw(source.getYaw() + 7);
                s.setPitch(source.getPitch() - 7);
                returnList.add(s.getDirection());

                s = source.clone();
                s.setYaw(source.getYaw() - 7);
                s.setPitch(source.getPitch() + 7);
                returnList.add(s.getDirection());

                s = source.clone();
                s.setYaw(source.getYaw() - 7);
                s.setPitch(source.getPitch() - 7);
                returnList.add(s.getDirection());
            case 2:
                s = source.clone();
                s.setPitch(source.getPitch() + 10);
                returnList.add(s.getDirection());

                s = source.clone();
                s.setPitch(source.getPitch() - 10);
                returnList.add(s.getDirection());
            case 1:
                s = source.clone();
                s.setYaw(source.getYaw() + 10);
                returnList.add(s.getDirection());

                s = source.clone();
                s.setYaw(source.getYaw() - 10);
                returnList.add(s.getDirection());
            default:
                returnList.add(source.getDirection());
        }
        return returnList;
    }

}