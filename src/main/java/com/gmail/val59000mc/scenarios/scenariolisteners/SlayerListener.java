package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.scoreboard.ScoreboardLayout;
import com.gmail.val59000mc.scoreboard.ScoreboardType;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class SlayerListener extends ScenarioListener {

    private static Map<UhcPlayer, Integer> deaths;
    private static Set<UhcPlayer> treason;

    private static List<Sound> hurtSounds;
    static {
        hurtSounds = new ArrayList<>(Arrays.asList(Sound.values()));
        hurtSounds.removeIf(sound -> !sound.name().contains("HURT"));

        deaths = new HashMap<>();
        treason = new HashSet<>();
    }

    @Override
    public void onEnable() {
        List<String> scoreboardList;
        if (isActivated(Scenario.DUOS) || isActivated(Scenario.FIFTY)) {
            scoreboardList = Arrays.asList("&fTime left: &d%timeLeft%", "&fBorder: &d%border%", "&fPlayers: &d%alive%",
                    "&fTeam: &d%teamColor%", "&fTop: &d%topTeam%", "&d%topTeamKills% kills",
                    "&fTeam kills: &d%teamKills%", "&fYour Kills: &d%kills%", "&fYour Deaths: &d%deaths%",
                    "&fScenarios:", "&d%scenarios%", "", "&fPlayer Rating:", "&6%userScore%");
        } else {
            scoreboardList = Arrays.asList("&fTime left: &d%timeLeft%", "&fBorder: &d%border%", "&fPlayers: &d%alive%",
                    "&fTop: &d%top%", "&d%topKills% kills", "&fYour Kills: &d%kills%", "&fYour Deaths: &d%deaths%",
                    "&fScenarios:", "&d%scenarios%", "", "&fPlayer Rating:", "&6%userScore%");
        }
        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.PLAYING,
                scoreboardList);
        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.DEATHMATCH,
                scoreboardList);

        if (isActivated(Scenario.DUOS) || isActivated(Scenario.FIFTY)) {
            scoreboardList = Arrays.asList("&fTime left: &d%timeLeft%", "&fBorder: &d%border%", "&fPlayers: &d%alive%",
                    "&fTop: &d%topTeam%", "&d%topTeamKills% kills", "&fScenarios:", "&d%scenarios%", "",
                    "&fPlayer Rating:", "&6%userScore%");
        } else {
            scoreboardList = Arrays.asList("&fTime left: &d%timeLeft%", "&fBorder: &d%border%", "&fPlayers: &d%alive%",
                    "&fTop: &d%top%", "&d%topKills% kills", "&fScenarios:", "&d%scenarios%", "", "&fPlayer Rating:",
                    "&6%userScore%");
        }
        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.SPECTATING,
                scoreboardList);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (!getGameManager().getPvp() || isActivated(Scenario.POLITICS)) return;

        if (getGameManager().getGameState() != GameState.PLAYING
                && getGameManager().getGameState() != GameState.DEATHMATCH)
            return;

        Entity damager = e.getDamager();
        if (damager instanceof Projectile) damager = (Entity) ((Projectile) damager).getShooter();
        if (damager instanceof TNTPrimed) damager = ((TNTPrimed) damager).getSource();
        if (!(e.getEntity() instanceof Player) || !(damager instanceof Player)) return;

        Player killed = (Player) e.getEntity();
        Player killer = (Player) damager;

        if (e.getDamage() < killed.getHealth()) return;

        PlayersManager pm = getPlayersManager();
        UhcPlayer uhcKilled = pm.getUhcPlayer(killed);
        UhcPlayer uhcKiller = pm.getUhcPlayer(killer);

        if (isActivated(Scenario.INHERITANCE)) killer.getInventory().setContents(killed.getInventory().getContents());

        // check if it was friendlyfire
        if (uhcKiller.getTeam() != uhcKilled.getTeam()) {
            GameManager gm = getGameManager();
            gm.sendInfoToServer("KILL:" + uhcKiller.getName() + ":" + uhcKilled.getName() + ":" + true, false);
            killer.sendMessage("You slayed " + uhcKilled.getDisplayName());
            killed.sendMessage("You were slain by " + uhcKiller.getDisplayName());

            uhcKiller.kills++;
            handleDeath(uhcKilled, killed, killer);
        } else {
            if (getGameManager().getConfiguration().getEnableFriendlyFire()) {
                getGameManager().broadcastInfoMessage(uhcKiller.getDisplayName() + " committed treason!");
                GameManager gm = getGameManager();
                gm.sendInfoToServer("KILL:" + uhcKilled.getName() + ":" + uhcKiller.getName() + ":" + true, false);
                if (treason.contains(uhcKiller)) {
                    Bukkit.dispatchCommand(killer, "suicide");
                } else {
                    killer.sendMessage(
                            UhcCore.PREFIX + " \u00a7cDon't do that again or you'll get kicked from the game!");
                    treason.add(uhcKiller);
                }
                uhcKiller.kills--;

                handleDeath(uhcKilled, killed, killer);
            }
        }
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.isCancelled()) return;

        if (getGameManager().getGameState() != GameState.PLAYING
                && getGameManager().getGameState() != GameState.DEATHMATCH)
            return;
        Player killed = (Player) event.getEntity();
        PlayersManager pm = getPlayersManager();
        GameManager gm = getGameManager();
        UhcPlayer uhcKilled = pm.getUhcPlayer(killed);

        if (event.getDamage() < killed.getHealth()) return;

        String cause = event.getCause().toString().replace("_", " ");
        killed.sendMessage(
                "You were killed by \u00a7d" + cause.substring(0, 1).toUpperCase() + cause.substring(1).toLowerCase());
        // gm.sendInfoToServer("DEATH:" + uhcKilled.getName() + ":" + true, false);

        handleDeath(uhcKilled, killed, null);

        event.setCancelled(true);
    }

    private void handleDeath(UhcPlayer uhcKilled, Player killed, Player killer) {
        for (UhcPlayer teamMate : uhcKilled.getTeam().getMembers()) {
            if (teamMate.getName().equals("YEUH-BOT") && teamMate.getState().equals(PlayerState.PLAYING)) {
                Player matePlayer;
                try {
                    matePlayer = teamMate.getPlayer();
                } catch (UhcPlayerNotOnlineException haggle) {
                    continue;
                }
                NPC npc = CitizensAPI.getNPCRegistry().getNPC(matePlayer);
                SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
                sentinel.setGuarding(null);
            }
        }

        deaths.put(uhcKilled, getDeaths(uhcKilled) + 1);

        MainConfiguration cfg = getGameManager().getConfiguration();

        if (cfg.getEnableExpDropOnDeath()) {
            UhcItems.spawnExtraXp(killed.getLocation(), (int) (cfg.getExpDropOnDeath() + killed.getExp()));
        }
        killed.setExp(0);

        killed.getWorld().playSound(killed.getLocation(), getRandomHurtSound(), 1, 0);
        List<ItemStack> drops = new ArrayList<>();
        killed.setFireTicks(0);
        killed.setArrowsInBody(0);
        killed.getActivePotionEffects().forEach(effect -> killed.removePotionEffect(effect.getType()));
        if (isActivated(Scenario.SUPERHEROES)) {
            SuperHeroesListener.addHeroesEffect(uhcKilled, SuperHeroesListener.getPower(uhcKilled), false);
        }
        killed.setHealth(killed.getMaxHealth());
        killed.setFoodLevel(20);

        if (isActivated(Scenario.LILCHEAT)) {
            killed.setGameMode(GameMode.CREATIVE);
            killed.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent("You have been given \u00a7d10 \u00a7fseconds of \u00a7dCreative Mode\u00a7f!"));
            new BukkitRunnable() {
                @Override
                public void run() {
                    killed.setGameMode(GameMode.SURVIVAL);
                    killed.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent("Alright that's enough time!"));
                }
            }.runTaskLater(UhcCore.getPlugin(), 10 * 20);
        }

        GameManager.getGameManager().getListInventoryHandler().updatePlayer(uhcKilled);

        if (isActivated(Scenario.INHERITANCE)) {
            if (killer != null) killer.getInventory().setContents(killed.getInventory().getContents());
        } else if (!isActivated(Scenario.WHATSMINE)) {

            killed.setCanPickupItems(false);

            for (int i = 0; i <= 40; i++) {
                ItemStack item = killed.getInventory().getItem(i);
                if (item == null) continue;
                if (item.containsEnchantment(Enchantment.VANISHING_CURSE)) {
                    killed.getInventory().setItem(i, null);
                    continue;
                }
                if (Math.random() < 0.5
                        && (!isActivated(Scenario.NINESLOTS) || !item.isSimilar(NineSlotsListener.fillItem))) {
                    if (!isActivated(Scenario.TIMEBOMB) && (killer == null || !killer.hasMetadata("NPC"))) {
                        killed.getWorld().dropItemNaturally(killed.getEyeLocation(), item);
                    } else {
                        drops.add(item);
                    }
                    killed.getInventory().setItem(i, null);
                }
            }

            if (killer != null && killer.hasMetadata("NPC")) {
                HashMap<Integer, ItemStack> a = killer.getInventory().addItem(drops.stream().toArray(ItemStack[]::new));
                drops.clear();
                drops.addAll(a.values());
            }

            if (isActivated(Scenario.TIMEBOMB)) {
                TimebombListener.TimebombThread timebombThread = new TimebombListener.TimebombThread(drops,
                        killed.getLocation().getBlock().getLocation(), killed.getName(), TimebombListener.delay);
                Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), timebombThread, 1L);
            } else {
                drops.forEach(item -> killed.getWorld().dropItemNaturally(killed.getEyeLocation(), item));
            }
            killed.setCanPickupItems(true);
        }
        killed.teleport(PlayersManager.findRandomSafeLocation(killed.getWorld()));
    }

    private Sound getRandomHurtSound() { return hurtSounds.get((int) (Math.random() * hurtSounds.size())); }

    public static int getDeaths(UhcPlayer uhcPlayer) {
        if (deaths.containsKey(uhcPlayer)) return deaths.get(uhcPlayer);
        return 0;
    }

}