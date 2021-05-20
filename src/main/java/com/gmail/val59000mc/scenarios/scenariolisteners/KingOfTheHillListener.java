package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.UhcGameEndEvent;
import com.gmail.val59000mc.events.UhcPlayerDeathEvent;
import com.gmail.val59000mc.events.UhcPlayerKillEvent;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.listeners.AbstractPacket;
import com.gmail.val59000mc.listeners.PlayerDeathListener;
import com.gmail.val59000mc.players.SpawnLocations;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.scoreboard.SuperLayout;
import com.gmail.val59000mc.utils.UniversalSound;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class KingOfTheHillListener extends ScenarioListener {

    public static UhcPlayer king = null;

    @Override
    public void onEnable() {
        SuperLayout sl = getGameManager().getScoreboardManager().getSuperLayout();
        sl.replaceInLayoutSet(null, null, "time", "timeLeft");
        sl.addToLayoutSet(null, null, "king");
        sl.addToLayoutSet("playing", null, "deaths");
        sl.removeFromLayoutSet(null, true, "teams");

        PlayerDeathListener.autoRespawn = true;
        PlayerDeathListener.keepInventory = 0.5;
        PlayerDeathListener.publicAnnounceDeaths = false;
        PlayerDeathListener.privateAnnounceDeaths = true;
    }

    @EventHandler
    public void onKill(UhcPlayerKillEvent e) {
        try {
            if (e.getKilled() == king) {
                removeKing();
            } else if (king != null && e.getKiller() != king) {
                e.getKiller().getPlayerUnsafe().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                        "\u00a7cKills don't matter in this game aside from bragging rights! Dethrone the king to win!"));
            }

            if (king == null) newKing(e.getKiller());
        } catch (NullPointerException ex) {}
    }

    @EventHandler
    public void onDeath(UhcPlayerDeathEvent e) {
        Sound randomSound = PlayerDeathListener.getRandomHurtSound();
        Player killed = null;
        try {
            killed = e.getKilled().getPlayer();

            killed.getWorld().playSound(killed.getLocation(), randomSound, 1, 0);
            killed.teleport(SpawnLocations.findRandomSafeLocation(killed.getWorld()));
            killed.getWorld().playSound(killed.getLocation(), randomSound, 1, 0);
        } catch (UhcPlayerNotOnlineException ex) {}

        if (e.getKilled() == king) removeKing();
    }

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        BukkitRunnable infoThread = new BukkitRunnable() {
            @Override
            public void run() { lightningStrikeKing(); }
        };
        BukkitRunnable warnThread = new BukkitRunnable() {
            @Override
            public void run() {
                getGameManager().broadcastInfoMessage(
                        "\u00a7c\u00a7lWarning: in the last 5 minutes, the king can be randomly teleported as a punishment for hiding from other players!");
                infoThread.cancel();
            }
        };
        BukkitRunnable finalThread = new BukkitRunnable() {
            @Override
            public void run() {
                if (king == null) return;
                try {
                    double size = getGameManager().getWorldBorder().getCurrentSize() / 10;
                    double minDistSquared = Double.MAX_VALUE;
                    Location l = king.getPlayer().getLocation();
                    for (UhcPlayer otherPlayer : getPlayersManager().getAllPlayingPlayers()) {
                        if (otherPlayer == king) continue;
                        try {
                            minDistSquared = Math.min(minDistSquared,
                                    l.distanceSquared(otherPlayer.getPlayer().getLocation()));
                        } catch (UhcPlayerNotOnlineException e) {}
                    }
                    if (minDistSquared > size * size * 4) {
                        List<UhcPlayer> playingPlayers = new ArrayList<>(getPlayersManager().getAllPlayingPlayers());
                        getPlayersManager().strikeLightning(king);
                        Player otherPlayer = playingPlayers.get((int) (Math.random() * playingPlayers.size()))
                                .getPlayer();
                        king.getPlayer().teleport(otherPlayer);
                        king.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                "\u00a7cYou were too far from another player, so you were teleported!"));
                        king.getPlayer()
                                .sendMessage("\u00a7cYou were too far from another player, so you were teleported!");
                        getGameManager().broadcastInfoMessage(
                                king.getDisplayName() + " was trying to hide, and as a result got teleported!");
                    } else {
                        int x = (int) ((l.getX() + (Math.random() * 2 - 1) * size) / 10) * 10;
                        int z = (int) ((l.getZ() + (Math.random() * 2 - 1) * size) / 10) * 10;
                        getGameManager().broadcastInfoMessage(king.getDisplayName()
                                + " is somewhere near \u00a7d\u00a7lX: " + x + "\u00a7f, \u00a7d\u00a7lZ: " + z);
                        getPlayersManager().strikeLightning(king);
                    }
                } catch (UhcPlayerNotOnlineException ex) {}
            }
        };
        infoThread.runTaskTimer(UhcCore.getPlugin(), 50 * 60, 50 * 60);
        warnThread.runTaskLater(UhcCore.getPlugin(), 20 * 25 * 60);
        finalThread.runTaskTimer(UhcCore.getPlugin(), 20 * 26 * 60, 20 * 60);
    }

    public void lightningStrikeKing() {
        if (king == null) return;
        try {
            Location l = king.getPlayer().getLocation();
            int x = (int) ((l.getX()
                    + (Math.random() * 2 - 1) * getGameManager().getWorldBorder().getCurrentSize() / 10) / 10) * 10;
            int z = (int) ((l.getZ()
                    + (Math.random() * 2 - 1) * getGameManager().getWorldBorder().getCurrentSize() / 10) / 10) * 10;
            getGameManager().broadcastInfoMessage(king.getDisplayName() + " is somewhere near \u00a7d\u00a7lX: " + x
                    + "\u00a7f, \u00a7d\u00a7lZ: " + z);
            getGameManager().getPlayersManager().strikeLightning(king);
        } catch (UhcPlayerNotOnlineException ex) {}
    }

    private void removeKing() {
        if (king == null) return;
        getGameManager().broadcastInfoMessage(king.getDisplayName() + " \u00a7ehas been dethroned!");
        getPlayersManager().playSoundToAll(UniversalSound.ITEM_TRIDENT_RIPTIDE_3);

        Player kingPlayer = king.getPlayerUnsafe();

        if (kingPlayer != null) {
            kingPlayer.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            kingPlayer.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            kingPlayer.removePotionEffect(PotionEffectType.GLOWING);
            kingPlayer.setGlowing(false);
            if (king.getName().equalsIgnoreCase("YEUH-BOT")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "npc sel " + kingPlayer.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "npc glowing");
            }
        }

        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
            if (king.getName().equalsIgnoreCase("YEUH-BOT")) {
                new SentinelTargetLabel("npc:" + kingPlayer.getName()).removeFromList(sentinel.allTargets);
            } else {
                new SentinelTargetLabel("player:" + kingPlayer.getName()).removeFromList(sentinel.allTargets);
            }
            new SentinelTargetLabel("players").addToList(sentinel.allTargets);
            new SentinelTargetLabel("npcs").addToList(sentinel.allTargets);
        }

        king = null;
    }

    private void newKing(UhcPlayer player) {
        if (king != null) removeKing();
        king = player;

        Player kingPlayer = king.getPlayerUnsafe();

        getGameManager().broadcastInfoMessage("\u00a7eThe new king is " + king.getDisplayName() + "\u00a7e!");
        lightningStrikeKing();
        getPlayersManager().playSoundToAll(UniversalSound.ENTITY_RAVAGER_CELEBRATE);
        kingPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 1));
        kingPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 1));
        kingPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 999999, 1));
        kingPlayer.setGlowing(true);
        if (king.getName().equalsIgnoreCase("YEUH-BOT")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "npc sel " + kingPlayer.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "npc glowing");
        }

        king.getTeam().getMembers().forEach(uhcPlayer -> {
            try {
                uhcPlayer.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(uhcPlayer == king
                                ? "\u00a7e\u00a7lYou are the new king! Keep your crown as long as you can!"
                                : "\u00a76Your teammate is the new king! Protect them at all costs!"));

            } catch (UhcPlayerNotOnlineException ex) {}
        });

        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
            if (((Player) sentinel.getLivingEntity()).getName().equalsIgnoreCase(kingPlayer.getName())) continue;

            // ignore previous targets
            new SentinelTargetLabel("players").addToList(sentinel.allIgnores);
            new SentinelTargetLabel("npcs").addToList(sentinel.allIgnores);
            new SentinelTargetLabel("players").removeFromList(sentinel.allIgnores);
            new SentinelTargetLabel("npcs").removeFromList(sentinel.allIgnores);

            new SentinelTargetLabel("players").removeFromList(sentinel.allTargets);
            new SentinelTargetLabel("npcs").removeFromList(sentinel.allTargets);
            if (king.getName().equalsIgnoreCase("YEUH-BOT")) {
                new SentinelTargetLabel("npc:" + kingPlayer.getName()).addToList(sentinel.allTargets);
            } else {
                new SentinelTargetLabel("player:" + kingPlayer.getName()).addToList(sentinel.allTargets);
            }
        }
    }

    @EventHandler
    public void onEnd(UhcGameEndEvent e) {
        List<UhcPlayer> leaders = e.getLeaders();
        if (king == null) {
            int top = 0;
            Set<UhcPlayer> possibleKings = new HashSet<>();
            for (UhcPlayer p : getPlayersManager().getAllPlayingPlayers()) {
                if (p.kills > top) {
                    possibleKings.clear();
                    top = p.kills;
                }
                if (p.kills == top && p.kills > 0) possibleKings.add(p);
            }

            if (possibleKings.isEmpty()) {
                getGameManager().broadcastInfoMessage("\u00a7c\u00a7lNobody got any kills! You guys suck!");
                leaders.add(null);
                return;
            } else if (possibleKings.size() == 1) {
                king = possibleKings.iterator().next();
                getGameManager().broadcastInfoMessage("There was no king, so " + king.getDisplayName()
                        + " \u00a7fwas chosen as king for having the most kills (\u00a7d" + king.kills
                        + " \u00a7fkills)!");
            } else {
                Set<UhcPlayer> possibleKings2 = new HashSet<>();
                int topDeaths = Integer.MAX_VALUE;
                for (UhcPlayer p : possibleKings) {
                    if (p.deaths < topDeaths) {
                        possibleKings2.clear();
                        topDeaths = p.deaths;
                    }
                    if (p.deaths == top) possibleKings2.add(p);
                }
                if (possibleKings2.isEmpty()) return;
                // TODO: FIX THIS AND MAKE IT NOT SO SHITTY
                king = possibleKings2.iterator().next();
                getGameManager().broadcastInfoMessage("There was no king, so " + king.getDisplayName()
                        + " \u00a7fwas chosen as king for having the most kills and the least deaths (\u00a7d"
                        + king.kills + " \u00a7fkills, \u00a7d" + king.deaths + " \u00a7fdeaths)!");
            }
        }

        UhcTeam top = king.getTeam();

        if (top.isSolo())
            getGameManager().broadcastInfoMessage(top.getMembers().get(0).getDisplayName() + " is the winner!");
        else getGameManager().broadcastInfoMessage("Team " + top.getPrefix() + " \u00a7fis the winner with "
                + king.getDisplayName() + " \u00a7fto lead them to victory!");

        leaders.addAll(top.getMembers());
    }
}