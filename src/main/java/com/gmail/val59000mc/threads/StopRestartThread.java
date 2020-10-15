package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import org.bukkit.Bukkit;

public class StopRestartThread implements Runnable {

	private long timeBeforeStop;

	public StopRestartThread() {
		this.timeBeforeStop = GameManager.getGameManager().getConfiguration().getTimeBeforeRestartAfterEnd();
	}

	@Override
	public void run() {
		if (timeBeforeStop < 0) {
			return; // Stop thread
		}

		GameManager gm = GameManager.getGameManager();

		if (timeBeforeStop == 0) {

			// Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
			// Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");

			// if you do it this way, it will just reload the plugin and not have to turn
			// off and back on the server
			Bukkit.getServer().getOnlinePlayers().forEach(player -> { player.kickPlayer("Server Restarting"); });
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
		} else {
			if (timeBeforeStop < 5 || timeBeforeStop % 10 == 0) {
				Bukkit.getLogger().info("[UhcCore] Sending players back to lobby in " + timeBeforeStop + "s");
				gm.broadcastInfoMessage("Sending players back to lobby in \u00a7d" + timeBeforeStop + " seconds.");
			}

			timeBeforeStop--;
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), this, 20);
		}
	}

}