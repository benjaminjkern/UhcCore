package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.utils.UniversalSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class PreStartThread implements Runnable {

	private static PreStartThread instance;

	private final GameManager gameManager;
	private final int timeBeforeStart;
	private int remainingTime;
	private final int minPlayers;
	private boolean pause, force;

	public PreStartThread(GameManager gameManager) {
		this.gameManager = gameManager;
		MainConfiguration cfg = gameManager.getConfiguration();
		instance = this;
		this.timeBeforeStart = cfg.getTimeBeforeStartWhenReady();
		this.remainingTime = cfg.getTimeBeforeStartWhenReady();
		this.minPlayers = cfg.getMinPlayersToStart();
		this.pause = false;
		this.force = false;
	}

	public static String togglePause() {
		instance.pause = !instance.pause;
		return "pause:" + instance.pause + "  " + "force:" + instance.force;
	}

	public static String toggleForce() {
		instance.force = !instance.force;
		return "pause:" + instance.pause + "  " + "force:" + instance.force;
	}

	@Override
	public void run() {
		List<UhcTeam> teams = gameManager.getPlayersManager().listUhcTeams();
		double readyTeams = 0;
		double teamsNumber = teams.size();

		for (UhcTeam team : teams) { if (team.isReadyToStart() && team.isOnline()) { readyTeams += 1; } }

		double percentageReadyTeams = 100 * readyTeams / teamsNumber;
		int playersNumber = Bukkit.getOnlinePlayers().size();

		if (force && remainingTime > 5) remainingTime = 5;

		// if its 5 seconds or less,
		if (force || (!pause && (remainingTime <= 5 || (playersNumber >= minPlayers
				&& readyTeams >= gameManager.getConfiguration().getMinimalReadyTeamsToStart()
				&& percentageReadyTeams >= gameManager.getConfiguration().getMinimalReadyTeamsPercentageToStart())))) {

			if (remainingTime == timeBeforeStart + 1) {
				gameManager.broadcastInfoMessage(Lang.GAME_ENOUGH_TEAMS_READY);
			} else if (remainingTime == timeBeforeStart) {
				gameManager
						.broadcastInfoMessage(Lang.GAME_STARTING_IN.replace("%time%", String.valueOf(remainingTime)));
				gameManager.getPlayersManager().playSoundToAll(UniversalSound.CLICK);
			} else if ((remainingTime > 0 && remainingTime <= 10) || (remainingTime > 0 && remainingTime % 10 == 0)) {
				gameManager
						.broadcastInfoMessage(Lang.GAME_STARTING_IN.replace("%time%", String.valueOf(remainingTime)));
				gameManager.getPlayersManager().playSoundToAll(UniversalSound.CLICK);
			}

			remainingTime--;

			if (remainingTime == -1) {
				GameManager.getGameManager().startGame();
			} else {
				Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this, 20);
			}
		} else {
			if (!pause && remainingTime < timeBeforeStart + 1) {
				gameManager.broadcastInfoMessage(Lang.GAME_STARTING_CANCELLED);
			} else {
				if (playersNumber < minPlayers) {
					for (Player p : Bukkit.getOnlinePlayers())
						gameManager.getPlayersManager().sendPlayerToBungeeServer(p);
				}
			}
			remainingTime = timeBeforeStart + 1;
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this, 20);
		}
	}

}