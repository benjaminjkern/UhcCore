package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.scoreboard.ScoreboardType;

import org.bukkit.Bukkit;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PoliticsListener extends ScenarioListener {

    private static Map<UhcPlayer, PlayerNode> teamLeaders;
    private static Set<UhcPlayer> treason;
    static {
        teamLeaders = new HashMap<>();
        treason = new HashSet<>();
    }

    @Override
    public void onEnable() {
        List<String> scoreboardList = Arrays.asList("&fTime: &d%time%", "&fBorder: &d%border%", "&fPlayers: &d%alive%",
                "&fTeams left: &d%teamAlive%", "&fTeam: &d%teamColor%", "&fChain of command:", "&d%chainOfCommand%",
                "&fTeam size: &d%teammatesAlive%", "&fKills: &d%kills%", "&fScenarios:", "&d%scenarios%", "",
                "&fPlayer Rating:", "&6%userScore%");
        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.PLAYING,
                scoreboardList);
        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.DEATHMATCH,
                scoreboardList);

        scoreboardList = Arrays.asList("&fTime: &d%time%", "&fBorder: &d%border%", "&fPlayers: &d%alive%",
                "&fTeams left: &d%teamAlive%", "&fScenarios:", "&d%scenarios%", "", "&fPlayer Rating:",
                "&6%userScore%");
        GameManager.getGameManager().getScoreboardManager().getScoreboardLayout().setLines(ScoreboardType.SPECTATING,
                scoreboardList);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (!getGameManager().getPvp() || isActivated(Scenario.SLAYER)) return;

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

            uhcKiller.kills++;

            UhcTeam killedTeam = uhcKilled.getTeam();

            PlayerNode killerNode = getPlayerNode(uhcKiller);
            PlayerNode killedNode = getPlayerNode(uhcKilled);

            String killedName = uhcKilled.getDisplayName();
            killerNode.addChild(killedNode);

            Set<PlayerNode> teamBelow = killedNode.getTeamBelow();
            if (teamBelow.size() == 1) {
                uhcKiller.getTeam().sendMessage(killedName + " \u00a7fwas recruited by " + uhcKiller.getDisplayName());
            } else {
                uhcKiller.getTeam().sendMessage(killedName + " \u00a7fand \u00a7d" + (teamBelow.size() - 1)
                        + " \u00a7fothers were recruited by " + uhcKiller.getDisplayName());
            }

            killed.sendMessage("\u00a7fYou have joined the " + uhcKiller.getTeam().getPrefix() + "\u00a7fteam!");
            if (teamBelow.size() > 1) killed
                    .sendMessage("\u00a7fYou brought \u00a7d" + (teamBelow.size() - 1) + " \u00a7fteammates with you");

            teamBelow.forEach(node -> {
                UhcPlayer uhcPlayer = node.player;
                uhcPlayer.getTeam().getMembers().remove(uhcPlayer);
                uhcPlayer.setTeam(uhcKiller.getTeam());
                uhcPlayer.getTeam().getMembers().add(uhcPlayer);
                try {
                    Player player = uhcPlayer.getPlayer();
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 0);
                    player.getWorld().spawnParticle(Particle.BLOCK_CRACK, killed.getLocation().clone().add(0, 1, 0),
                            200, 0, 0, 0, Bukkit.createBlockData(Material.DIAMOND_BLOCK));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                            "\u00a7fYou have joined the " + uhcKiller.getTeam().getPrefix() + "\u00a7fteam!"));
                    if (node != killedNode) player.sendMessage(killedName + " \u00a7fjoined the "
                            + uhcKiller.getTeam().getPrefix() + "\u00a7fteam and has brought you with them!");
                } catch (UhcPlayerNotOnlineException exception) {
                    // nothing, its only messages anyways
                }
            });

            if (teamBelow.size() == 1) {
                killedTeam.sendMessage(killedName + " \u00a7fhas joined team " + uhcKilled.getTeam().getPrefix());
            } else {
                killedTeam.sendMessage(killedName + " \u00a7fand \u00a7d" + (teamBelow.size() - 1)
                        + " \u00a7fothers have joined team " + uhcKilled.getTeam().getPrefix());
            }

            gm.getScoreboardManager().updatePlayerTab(uhcKilled);

            killer.setHealth(killer.getMaxHealth());
            killer.setFoodLevel(20);
            killed.setHealth(killed.getMaxHealth() / 2.);

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
        UhcPlayer uhcKilled = pm.getUhcPlayer(killed);

        if (event.getDamage() < killed.getHealth()) return;

        PlayerNode killedNode = getPlayerNode(uhcKilled);
        String killedName = uhcKilled.getDisplayName();

        Set<PlayerNode> teamBelow = killedNode.getTeamBelow();

        String cause = event.getCause().toString().replace("_", " ");
        killed.sendMessage("\u00a77\u00a7oYou were killed by \u00a7d" + cause.substring(0, 1).toUpperCase()
                + cause.substring(1).toLowerCase() + " \u00a77\u00a7oand got moved to the bottom of the totem poll");

        if (killedNode.child != null) {
            uhcKilled.getTeam().sendMessage(killedName + " \u00a77\u00a7ohas been demoted");
            if (killedNode.isLeader()) uhcKilled.getTeam().sendMessage(
                    killedNode.child.player.getDisplayName() + " \u00a7ehas been promoted to team leader!");
        }

        killedNode.moveToBottom();
        teamBelow.forEach(node -> {
            if (node == killedNode) return;
            UhcPlayer uhcPlayer = node.player;
            try {
                Player player = uhcPlayer.getPlayer();
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("\u00a7e\u00a7lYou have been promoted to rank \u00a7f\u00a7l"
                                + node.chainOfCommand().size() + "\u00a7e\u00a7l!"));
            } catch (UhcPlayerNotOnlineException exception) {
                // nothing, its only messages anyways
            }
        });

        killed.setHealth(killed.getMaxHealth() / 2.);

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

        List<ItemStack> drops = new ArrayList<>();
        killed.setFireTicks(0);
        killed.getActivePotionEffects().forEach(effect -> killed.removePotionEffect(effect.getType()));
        if (isActivated(Scenario.SUPERHEROES)) {
            SuperHeroesListener.addHeroesEffect(uhcKilled, SuperHeroesListener.getPower(uhcKilled), false);
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
                    if (!isActivated(Scenario.TIMEBOMB) && killer == null) {
                        killed.getWorld().dropItemNaturally(killed.getEyeLocation(), item);
                    } else {
                        drops.add(item);
                    }
                    killed.getInventory().setItem(i, null);
                }
            }

            if (killer != null && !isActivated(Scenario.TIMEBOMB)) {
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
            if (killer == null) killed.teleport(PlayersManager.findRandomSafeLocation(killed.getWorld()));
            killed.setCanPickupItems(true);
        }
    }

    public static PlayerNode getPlayerNode(UhcPlayer uhcPlayer) {
        if (teamLeaders.containsKey(uhcPlayer)) return teamLeaders.get(uhcPlayer);
        PlayerNode newNode = new PlayerNode(uhcPlayer);
        teamLeaders.put(uhcPlayer, newNode);
        return newNode;
    }

    public static class PlayerNode {
        PlayerNode leader;
        PlayerNode child;
        public UhcPlayer player;

        PlayerNode(UhcPlayer player) {
            leader = null;
            child = null;
            this.player = player;
        }

        public boolean isLeader() { return leader == null; }

        public List<PlayerNode> chainOfCommand() {
            List<PlayerNode> chain = new ArrayList<>();
            PlayerNode current = leader;
            chain.add(this);
            while (current != null && current != this) {
                chain.add(current);
                current = current.leader;
            }
            return chain;
        }

        Set<PlayerNode> getTeamBelow() {
            Set<PlayerNode> teamBelow = new HashSet<>();
            teamBelow.add(this);
            if (child != null) teamBelow.addAll(child.getTeamBelow());
            return teamBelow;
        }

        public PlayerNode getTeamLeader() {
            if (leader == null) return this;
            return leader.getTeamLeader();
        }

        void moveToBottom() {
            if (child != null) child.addChild(this);
            else {
                List<PlayerNode> teamMates = player.getTeam().getMembers().stream()
                        .filter(uhcPlayer -> uhcPlayer != player).map(uhcPlayer -> getPlayerNode(uhcPlayer))
                        .collect(Collectors.toList());
                if (!teamMates.isEmpty()) teamMates.get(0).addChild(this);
            }
        }

        void addChild(PlayerNode newNode) {
            if (newNode == this) return;
            if (child != null) {
                child.addChild(newNode);
                return;
            }
            if (newNode.leader != null) { newNode.leader.child = newNode.child; }
            if (newNode.child != null) { newNode.child.leader = newNode.leader; }
            newNode.leader = this;
            newNode.child = null;

            child = newNode;
        }

        public String toString(int layers) {
            if (layers == 0) return player.getDisplayName();
            return player.getDisplayName() + ", " + child.toString(layers - 1);
        }

        public String toString() { return toString(4); }
    }

}