package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;

import org.bukkit.Bukkit;

public class LobbySocketThread implements Runnable {

	private LobbySocketThread task;

	public LobbySocketThread() { task = this; }

	@Override
	public void run() {
		GameManager gm = GameManager.getGameManager();

		gm.sendInfoToServer("CURRENTSIZE:" + Bukkit.getOnlinePlayers().size());

		if (gm.getLobbySocket() == null) {
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20);
		} else {
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20 * 60);
		}
	}
}