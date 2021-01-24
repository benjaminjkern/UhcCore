package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.listeners.PlayerConnectionListener;

import org.bukkit.Bukkit;

public class StopRestartThread implements Runnable {

	@Override
	public void run() {
		// this does not need to be its own thread but whatever
		Bukkit.getServer().getOnlinePlayers().forEach(player -> {
			PlayerConnectionListener.addToSendingToLobby(player);
			player.kickPlayer("Server Restarting");
		});
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
	}

}