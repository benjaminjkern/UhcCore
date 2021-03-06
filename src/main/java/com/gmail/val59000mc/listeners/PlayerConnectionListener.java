package com.gmail.val59000mc.listeners;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.exceptions.UhcPlayerJoinException;
import com.gmail.val59000mc.exceptions.UhcTeamException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.threads.KillDisconnectedPlayerThread;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

	private final GameManager gameManager;
	private final PlayersManager playersManager;
	private static Set<Player> sendingToLobby = new HashSet<>();

	public PlayerConnectionListener(GameManager gameManager, PlayersManager playersManager) {
		this.gameManager = gameManager;
		this.playersManager = playersManager;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		// Player is not allowed to join so don't create UhcPlayer. (Server full,
		// whitelist, ban, ...)
		if (event.getResult() != Result.ALLOWED) { return; }

		try {
			boolean allowedToJoin = playersManager.isPlayerAllowedToJoin(event.getPlayer());

			if (allowedToJoin) {
				// Create player if not existent.
				playersManager.getOrCreateUhcPlayer(event.getPlayer());
			} else {
				throw new UhcPlayerJoinException("An unexpected error as occured.");
			}
		} catch (final UhcPlayerJoinException e) {
			event.setKickMessage(e.getMessage());
			event.setResult(Result.KICK_OTHER);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		gameManager.sendInfoToServer("CURRENTSIZE:" + (Bukkit.getOnlinePlayers().size()), false);
		gameManager.getBossBar().addPlayer(event.getPlayer());
		Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(),
				() -> playersManager.playerJoinsTheGame(event.getPlayer()), 1);

		if (Bukkit.getOnlinePlayers().size() <= 1) gameManager.setBotsIn(true);
		else {
			int botsInVotes = 0;
			for (Player p : Bukkit.getOnlinePlayers()) {
				UhcPlayer uhcPlayer = gameManager.getPlayersManager().getUhcPlayer(p);
				botsInVotes += uhcPlayer.getScenarioVotes().contains(Scenario.BOTSIN) ? 1 : 0;
			}
			gameManager.setBotsIn(botsInVotes >= Bukkit.getOnlinePlayers().size() / 2.);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		gameManager.sendInfoToServer("CURRENTSIZE:" + (Bukkit.getOnlinePlayers().size() - 1), false);

		if (Bukkit.getOnlinePlayers().size() <= 2) gameManager.setBotsIn(true);
		else {
			int botsInVotes = 0;
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.equals(event.getPlayer())) continue;
				UhcPlayer uhcPlayer = gameManager.getPlayersManager().getUhcPlayer(p);
				botsInVotes += uhcPlayer.getScenarioVotes().contains(Scenario.BOTSIN) ? 1 : 0;
			}
			gameManager.setBotsIn(botsInVotes >= (Bukkit.getOnlinePlayers().size() - 1) / 2.);
		}

		if (gameManager.getGameState().equals(GameState.WAITING)
				|| gameManager.getGameState().equals(GameState.STARTING)) {
			UhcPlayer uhcPlayer = playersManager.getUhcPlayer(event.getPlayer());

			// if (gameManager.getGameState().equals(GameState.STARTING)) {
			// playersManager.setPlayerSpectateAtLobby(uhcPlayer);
			// gameManager.broadcastInfoMessage(
			// uhcPlayer.getName() + " has left while the game was starting and has been
			// killed.");
			// playersManager.strikeLightning(uhcPlayer);
			// }

			// unvote for everything
			List<Scenario> scenariosVotedFor = new ArrayList<>(uhcPlayer.getScenarioVotes());
			for (Scenario scenario : scenariosVotedFor) { ItemsListener.voteForScenario(uhcPlayer, scenario); }
			GameManager.getGameManager().getListInventoryHandler().updatePlayer(uhcPlayer);
			if (uhcPlayer.getState() != PlayerState.PLAYING) {
				GameManager.getGameManager().getListInventoryHandler().removePlayer(event.getPlayer().getName());
			}

			try {
				uhcPlayer.getTeam().leave(uhcPlayer);
			} catch (UhcTeamException e) {
				// Nothing
			}

			playersManager.getPlayersList().remove(uhcPlayer);
		}

		if (gameManager.getGameState().equals(GameState.PLAYING)
				|| gameManager.getGameState().equals(GameState.DEATHMATCH)) {
			UhcPlayer uhcPlayer = playersManager.getUhcPlayer(event.getPlayer());
			if (gameManager.getConfiguration().getEnableKillDisconnectedPlayers()
					&& uhcPlayer.getState().equals(PlayerState.PLAYING)) {

				KillDisconnectedPlayerThread killDisconnectedPlayerThread = new KillDisconnectedPlayerThread(
						event.getPlayer().getUniqueId(), gameManager.getConfiguration().getMaxDisconnectPlayersTime());

				Bukkit.getScheduler().runTaskLaterAsynchronously(UhcCore.getPlugin(), killDisconnectedPlayerThread, 1);
			}
			if (gameManager.getConfiguration().getSpawnOfflinePlayers()
					&& uhcPlayer.getState().equals(PlayerState.PLAYING)) {
				playersManager.spawnOfflineZombieFor(event.getPlayer());
			}
			playersManager.checkIfRemainingPlayers();
		}

		Player p = event.getPlayer();

		if (sendingToLobby.contains(p)) {
			event.setQuitMessage("\u00a70(\u00a7d\u00a7l<\u00a70) \u00a77" + p.getName());
			sendingToLobby.remove(p);
		} else gameManager.sendInfoToServer("DISCONNECTED:" + p.getName(), false);
	}

	public static void addToSendingToLobby(Player p) { sendingToLobby.add(p); }

}