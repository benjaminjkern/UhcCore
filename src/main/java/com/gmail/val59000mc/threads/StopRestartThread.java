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

			// Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
			// Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");

			// if you do it this way, it will just reload the plugin and not have to turn off and back on the server
			Bukkit.getServer().getOnlinePlayers().forEach(player->{
				player.kickPlayer("Server Restarting");
			});
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload");
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