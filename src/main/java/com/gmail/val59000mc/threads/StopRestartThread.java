package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.game.GameManager;
import org.bukkit.Bukkit;

public class StopRestartThread implements Runnable {

	@Override
	public void run() {
		// this does not need to be its own thread but whatever
		GameManager gm = GameManager.getGameManager();
		for (String p : gm.getPlayersManager().getWinners()) { gm.sendInfoToServer("WIN:" + p, false); }
		Bukkit.getServer().getOnlinePlayers().forEach(player -> { player.kickPlayer("Server Restarting"); });
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
	}

}