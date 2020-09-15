package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.maploader.MapLoader;
import org.bukkit.Bukkit;

public class StopRestartThread implements Runnable{

	private long timeBeforeStop;
	
	public StopRestartThread(){
		this.timeBeforeStop = GameManager.getGameManager().getConfiguration().getTimeBeforeRestartAfterEnd();
	}
	
	@Override
	public void run() {
		if (timeBeforeStop < 0){
			return; // Stop thread
		}

		GameManager gm = GameManager.getGameManager();
		MapLoader mapLoader = gm.getMapLoader();
		MainConfiguration configuration = gm.getConfiguration();
			
		if(timeBeforeStop == 0){
			
			// supposed to unload and delete the world before stopping it but bukkit didnt like it, I'll fix it later maybe
			// Bukkit.unloadWorld(Bukkit.getServer().getWorld(configuration.getOverworldUuid()), false);
			// Bukkit.unloadWorld(Bukkit.getServer().getWorld(configuration.getNetherUuid()), false);
			// Bukkit.unloadWorld(Bukkit.getServer().getWorld(configuration.getTheEndUuid()), false);

			// mapLoader.deleteLastWorld(configuration.getOverworldUuid());
			// mapLoader.deleteLastWorld(configuration.getNetherUuid());
			// mapLoader.deleteLastWorld(configuration.getTheEndUuid());

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
		}else{
			if(timeBeforeStop<5 || timeBeforeStop%10 == 0){
				Bukkit.getLogger().info("[UhcCore] Server will shutdown in "+timeBeforeStop+"s");
				gm.broadcastInfoMessage(Lang.GAME_SHUTDOWN.replace("%time%", ""+timeBeforeStop));
			}

			timeBeforeStop--;
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), this,20);
		}
	}

}