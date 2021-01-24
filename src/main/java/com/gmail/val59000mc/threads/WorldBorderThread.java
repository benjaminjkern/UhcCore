package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class WorldBorderThread implements Runnable {

	private long timeBeforeShrink;
	private final long timeToShrink;
	private final int endSize;
	private boolean newShrink;

	private final double fullTime;

	public WorldBorderThread(long timeBeforeShrink, int endSize, long timeToShrink) {
		this.timeBeforeShrink = timeBeforeShrink;
		this.endSize = endSize;
		this.timeToShrink = timeToShrink;
		this.newShrink = true;
		BossBar bar = GameManager.getGameManager().getBossBar();
		bar.setTitle("\u00a7a\u00a7lBorder Timer");
		bar.setColor(BarColor.GREEN);
		bar.setVisible(true);
		bar.setProgress(0);
		fullTime = timeBeforeShrink;
	}

	@Override
	public void run() {
		if (!UhcCore.getPlugin().isEnabled()) return;
		BossBar bar = GameManager.getGameManager().getBossBar();
		bar.setProgress((fullTime - timeBeforeShrink) / fullTime);
		if (timeBeforeShrink <= 0) {
			bar.setVisible(false);
			if (!newShrink) startMoving();
			else Bukkit.getScheduler().runTaskLaterAsynchronously(UhcCore.getPlugin(),
					new WorldBorderShrinkThread(endSize, timeToShrink, 90, 30), 20);
		} else {
			timeBeforeShrink--;
			Bukkit.getScheduler().runTaskLaterAsynchronously(UhcCore.getPlugin(), this, 20);
		}
	}

	private void startMoving() {
		if (GameManager.getGameManager().getGameState() == GameState.DEATHMATCH) return;

		GameManager.getGameManager().broadcastInfoMessage(Lang.GAME_BORDER_START_SHRINKING);

		World overworld = Bukkit.getWorld(GameManager.getGameManager().getConfiguration().getOverworldUuid());
		WorldBorder overworldBorder = overworld.getWorldBorder();
		overworldBorder.setSize(2 * endSize, timeToShrink);

		World nether = Bukkit.getWorld(GameManager.getGameManager().getConfiguration().getNetherUuid());
		if (nether != null) {
			WorldBorder netherBorder = nether.getWorldBorder();
			netherBorder.setSize(endSize, timeToShrink);
		}
	}

}