package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;

import org.bukkit.Bukkit;

public class LobbyPingThread implements Runnable {

	private LobbyPingThread task;
	private GameManager gm;

	private static boolean running;

	public LobbyPingThread(GameManager gm) {
		task = this;
		this.gm = gm;
		running = true;
	}

	public static void stop() { running = false; }

	@Override
	public void run() {
		if (running) {
			gm.sendInfoToServer("CURRENTSIZE:" + Bukkit.getOnlinePlayers().size(), true);

			if (gm.getLobbySocket() == null) {
				Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20);
			} else {
				Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), task, 20 * 60);
			}
		}
	}
}