package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.events.UhcStartingEvent;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

public class DuosListener extends ScenarioListener {
    @Override
    public void onEnable() { getGameManager().getScoreboardManager().getSuperLayout().setTeams(true); }

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {

        Set<UhcPlayer> players = getPlayersManager().getAllPlayingPlayers();

        if (players.size() <= 2) return; // should never happen

        players.forEach(uhcPlayer -> {
            if (uhcPlayer.getTeam().getMembers().size() < 2) {
                try {
                    uhcPlayer.getPlayer().getInventory().addItem(new ItemStack(Material.TOTEM_OF_UNDYING));
                    uhcPlayer.getPlayer().sendMessage(
                            "There were an odd number of players, so you were given a \u00a7eTotem of Undying\u00a7f!");
                    uhcPlayer.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                            "There were an odd number of players, so you were given a \u00a7eTotem of Undying\u00a7f!"));
                } catch (UhcPlayerNotOnlineException u) {}
            } else {
                uhcPlayer.getTeam().getMembers().forEach(member -> {
                    if (uhcPlayer == member) return;
                    uhcPlayer.getPlayerUnsafe().sendMessage("Your teammate is " + member.getDisplayName() + "!");
                    uhcPlayer.getPlayerUnsafe().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent("Your teammate is " + member.getDisplayName() + "!"));
                    Location fixedLoc = member.getPlayerUnsafe().getLocation();
                    fixedLoc.setYaw(0);
                    fixedLoc.setPitch(0);
                    uhcPlayer.getPlayerUnsafe().teleport(fixedLoc);
                });
            }
        });
    }

    @EventHandler
    public void onTeammateDeath(PlayerDeathEvent e) {
        UhcPlayer deadPlayer = getPlayersManager().getUhcPlayer(e.getEntity());
        deadPlayer.getTeam().getMembers().forEach(member -> {
            if (deadPlayer == member) return;
            member.getPlayerUnsafe().sendMessage("\u00a7cYour teammate has been slain!");
            member.getPlayerUnsafe().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent("\u00a7cYour teammate has been slain!"));
        });
    }

    @EventHandler
    public void onGameStateChange(UhcStartingEvent e) {
        List<UhcPlayer> players = new ArrayList<>(getPlayersManager().getPlayersList());

        // dont do it if its only 2 players, although the scenario should block this
        // from happening
        if (players.size() <= 2) return;

        List<UhcPlayer> realPlayers = new ArrayList<>();
        List<UhcPlayer> botPlayers = new ArrayList<>();
        for (UhcPlayer up : players) {
            if (up.getName().equalsIgnoreCase("YEUH-BOT")) botPlayers.add(up);
            else realPlayers.add(up);
        }

        if (realPlayers.size() == 2) {
            while (!realPlayers.isEmpty()) {
                int r = (int) (Math.random() * realPlayers.size());
                UhcPlayer player1 = realPlayers.get(r);
                realPlayers.remove(r);

                r = (int) (Math.random() * botPlayers.size());
                UhcPlayer player2 = botPlayers.get(r);
                botPlayers.remove(r);

                player2.setTeam(player1.getTeam());
                player1.getTeam().getMembers().add(player2);
            }
        } else {
            while (realPlayers.size() > 1) {
                int r = (int) (Math.random() * realPlayers.size());
                UhcPlayer player1 = realPlayers.get(r);
                realPlayers.remove(r);

                r = (int) (Math.random() * realPlayers.size());
                UhcPlayer player2 = realPlayers.get(r);
                realPlayers.remove(r);

                player2.setTeam(player1.getTeam());
                player1.getTeam().getMembers().add(player2);
            }
            if (realPlayers.size() > 0 && botPlayers.size() > 0) {
                int r = (int) (Math.random() * realPlayers.size());
                UhcPlayer player1 = realPlayers.get(r);
                realPlayers.remove(r);

                r = (int) (Math.random() * botPlayers.size());
                UhcPlayer player2 = botPlayers.get(r);
                botPlayers.remove(r);

                player2.setTeam(player1.getTeam());
                player1.getTeam().getMembers().add(player2);
            }
        }

        while (botPlayers.size() > 1) {
            int r = (int) (Math.random() * botPlayers.size());
            UhcPlayer player1 = botPlayers.get(r);
            botPlayers.remove(r);

            r = (int) (Math.random() * botPlayers.size());
            UhcPlayer player2 = botPlayers.get(r);
            botPlayers.remove(r);

            player2.setTeam(player1.getTeam());
            player1.getTeam().getMembers().add(player2);
        }
    }

}