package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.players.PlayerState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.api.CitizensAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;

public class SwapListener extends ScenarioListener {

    @Option(key = "time")
    private int time = 60;

    private Map<UhcTeam, UhcTeam> order;
    private Map<UhcPlayer, Location> locations;

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        // create random order
        order = new HashMap<>();
        locations = new HashMap<>();

        UhcTeam lastTeam = null;
        UhcTeam firstTeam = null;

        for (UhcTeam uhcTeam : getTeamManager().getUhcTeams()) {
            if (uhcTeam.getMemberCount() == 0) continue;
            if (lastTeam == null) firstTeam = uhcTeam;
            else order.put(lastTeam, uhcTeam);
            lastTeam = uhcTeam;
        }
        order.put(lastTeam, firstTeam);

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("Swapping places in \u00a7d30 seconds!")));
            }
        }.runTaskTimer(UhcCore.getPlugin(), 20 * (time - 30), 20 * time);

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("Swapping places in \u00a7d10 seconds!")));
            }
        }.runTaskTimer(UhcCore.getPlugin(), 20 * (time - 10), 20 * time);

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("Swapping places!")));
                getGameManager().broadcastInfoMessage("Swapping places!");

                if (isActivated(Scenario.CHICKENFIGHT)) ChickenFightListener.disabled = true;
                Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> ChickenFightListener.disabled = false,
                        20);

                order.keySet().forEach(uhcTeam -> {
                    if (uhcTeam.isSpectating()) return;
                    UhcTeam nextTeam = order.get(uhcTeam);

                    while (nextTeam.isSpectating() || nextTeam.getPlayingMembers().size() == 0)
                        nextTeam = order.get(nextTeam);

                    order.put(uhcTeam, nextTeam);
                    try {
                        for (int i = 0; i < uhcTeam.getPlayingMembers().size(); i++) {
                            UhcPlayer uhcPlayer = uhcTeam.getMembers().get(i);
                            if (isActivated(Scenario.CHICKENFIGHT)) uhcPlayer.getPlayer().eject();
                            locations.put(uhcPlayer, nextTeam.getMembers().get(i % nextTeam.getPlayingMembers().size())
                                    .getPlayer().getLocation());
                        }
                    } catch (UhcPlayerNotOnlineException e) {}
                });

                locations.keySet().forEach(uhcPlayer -> {
                    try {
                        if (uhcPlayer.getName().equals("YEUH-BOT")) CitizensAPI.getNPCRegistry()
                                .getNPC(uhcPlayer.getPlayer()).getNavigator().cancelNavigation();
                        if (uhcPlayer.getState() == PlayerState.PLAYING)
                            uhcPlayer.getPlayer().teleport(locations.get(uhcPlayer), TeleportCause.NETHER_PORTAL);
                    } catch (UhcPlayerNotOnlineException e) {}
                });
            }

        }.runTaskTimer(UhcCore.getPlugin(), 20 * time, 20 * time);
    }

}