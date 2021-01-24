package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.configuration.VaultManager;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.events.UhcPlayerKillEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import com.gmail.val59000mc.scenarios.scenariolisteners.NineSlotsListener;
import com.gmail.val59000mc.scenarios.scenariolisteners.SilentNightListener;
import com.gmail.val59000mc.threads.TimeBeforeSendBungeeThread;
import com.gmail.val59000mc.utils.UniversalMaterial;
import com.gmail.val59000mc.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.CitizensAPI;

import java.util.List;

public class PlayerDeathListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		GameManager gm = GameManager.getGameManager();
		PlayersManager pm = gm.getPlayersManager();
		MainConfiguration cfg = gm.getConfiguration();
		UhcPlayer uhcPlayer = pm.getUhcPlayer(player);

		// if (gm.getScenarioManager().isActivated(Scenario.SLAYER)) return;

		if (!player.hasMetadata("NPC")
				&& (gm.getGameState() == GameState.WAITING || gm.getGameState() == GameState.STARTING)) {
			if (cfg.getEnableBungeeSupport()) {
				pm.sendPlayerToBungeeServer(player);
			} else {
				player.kickPlayer("Oof");
			}
		} else {
			// TODO: fix
			Bukkit.getLogger().info(player.getLocation() + "");
		}

		if (uhcPlayer.getState() != PlayerState.PLAYING) {
			Bukkit.getLogger().warning("[UhcCore] " + player.getName() + " died while already in 'DEAD' mode!");
			// player.kickPlayer("Don't cheat!");
			return;
		}

		pm.setLastDeathTime();
		String deathMessage = event.getDeathMessage().replaceFirst(player.getName(), uhcPlayer.getDisplayName());

		if (!GameManager.getGameManager().getWorldBorder().isWithinBorder(player.getLocation())) {
			deathMessage = deathMessage.replaceFirst("suffocated in a wall", "got stuck behind the wall");
		}

		// kill event
		Player killer = player.getKiller();
		if (killer != null) {
			UhcPlayer uhcKiller = pm.getUhcPlayer(killer);

			deathMessage = replaceLast(deathMessage, killer.getName(), uhcKiller.getDisplayName());

			uhcKiller.kills++;

			gm.sendInfoToServer("KILL:" + uhcKiller.getName() + ":" + uhcPlayer.getName(), false);

			// Call Bukkit event
			UhcPlayerKillEvent killEvent = new UhcPlayerKillEvent(uhcPlayer, uhcKiller);
			Bukkit.getServer().getPluginManager().callEvent(killEvent);

			if (cfg.getEnableKillEvent()) {
				double reward = cfg.getRewardKillEvent();
				List<String> killCommands = cfg.getKillCommands();
				if (reward > 0) {
					VaultManager.addMoney(killer, reward);
					if (!Lang.EVENT_KILL_REWARD.isEmpty()) {
						killer.sendMessage(Lang.EVENT_KILL_REWARD.replace("%money%", "" + reward));
					}
				}
				// If the list is empty, this will never execute
				killCommands.forEach(cmd -> {
					try {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
								cmd.replace("%name%", uhcKiller.getRealName()));
					} catch (CommandException exception) {
						Bukkit.getLogger().warning("[UhcCore] Failed to execute kill reward command: " + cmd);
						exception.printStackTrace();
					}
				});
			}
		} else if (gm.getStartPlayers() > 1) gm.sendInfoToServer("DEATH:" + uhcPlayer.getName(), false);

		// Store drops in case player gets re-spawned.
		uhcPlayer.getStoredItems().clear();
		uhcPlayer.getStoredItems().addAll(event.getDrops());

		// eliminations
		ScenarioManager sm = gm.getScenarioManager();
		if (!sm.isActivated(Scenario.SILENTNIGHT)
				|| !((SilentNightListener) sm.getScenarioListener(Scenario.SILENTNIGHT)).isNightMode()) {
			gm.broadcastInfoMessage(deathMessage);
			event.setDeathMessage(null);
		}

		if (sm.isActivated(Scenario.WHATSMINE) || sm.isActivated(Scenario.INHERITANCE)) event.getDrops().clear();

		if (!player.hasMetadata("NPC")) {

			if (cfg.getRegenHeadDropOnPlayerDeath()) { event.getDrops().add(UhcItems.createRegenHead(uhcPlayer)); }

			if (cfg.getEnableGoldenHeads()) {
				if (cfg.getPlaceHeadOnFence() && !gm.getScenarioManager().isActivated(Scenario.TIMEBOMB)) {
					// place head on fence
					Location loc = player.getLocation().clone().add(1, 0, 0);
					loc.getBlock().setType(UniversalMaterial.OAK_FENCE.getType());
					loc.add(0, 1, 0);
					loc.getBlock().setType(UniversalMaterial.PLAYER_HEAD_BLOCK.getType());

					Skull skull = (Skull) loc.getBlock().getState();
					VersionUtils.getVersionUtils().setSkullOwner(skull, uhcPlayer);
					skull.update();
				} else {
					event.getDrops().add(UhcItems.createGoldenHeadPlayerSkull(player.getName(), player.getUniqueId()));
				}
			}
		}

		for (UhcPlayer teamMate : uhcPlayer.getTeam().getMembers()) {
			if (teamMate.getName().equals("YEUH-BOT") && teamMate.getState().equals(PlayerState.PLAYING)) {
				Player matePlayer;
				try {
					matePlayer = teamMate.getPlayer();
				} catch (UhcPlayerNotOnlineException haggle) {
					continue;
				}
				NPC npc = CitizensAPI.getNPCRegistry().getNPC(matePlayer);
				SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
				sentinel.setGuarding(null);
			}
		}

		if (cfg.getEnableExpDropOnDeath()) { UhcItems.spawnExtraXp(player.getLocation(), cfg.getExpDropOnDeath()); }

		uhcPlayer.setState(PlayerState.DEAD);
		GameManager.getGameManager().getListInventoryHandler().updatePlayer(uhcPlayer);
		pm.strikeLightning(uhcPlayer);
		// pm.playSoundPlayerDeath();
		Bukkit.getLogger().info(UhcCore.PREFIX + "\u00a7f" + deathMessage);

		if (!player.hasMetadata("NPC")) {

			// handle player leaving the server
			boolean canContinueToSpectate = player.hasPermission("uhc-core.spectate.override")
					|| cfg.getCanSpectateAfterDeath();

			if (!canContinueToSpectate) {
				if (cfg.getEnableBungeeSupport()) {
					Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(),
							new TimeBeforeSendBungeeThread(pm, uhcPlayer, cfg.getTimeBeforeSendBungeeAfterDeath()));
				} else {
					player.kickPlayer(Lang.DISPLAY_MESSAGE_PREFIX + " " + Lang.KICK_DEAD);
				}
			}
		} else {
			// drop inventory when dead
			if (!gm.getScenarioManager().isActivated(Scenario.TIMEBOMB) && !sm.isActivated(Scenario.WHATSMINE)
					&& !sm.isActivated(Scenario.INHERITANCE)) {
				player.getInventory().forEach((item) -> {
					if (item != null) {
						if (gm.getScenarioManager().isActivated(Scenario.NINESLOTS)
								&& item.isSimilar(NineSlotsListener.fillItem))
							return;
						player.getWorld().dropItem(player.getLocation(), item);
					}
				});
			}
		}

		pm.checkIfRemainingPlayers();
	}

	public static String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		PlayersManager pm = GameManager.getGameManager().getPlayersManager();
		UhcPlayer uhcPlayer = pm.getUhcPlayer(event.getPlayer());

		if (uhcPlayer.getState().equals(PlayerState.DEAD)) {
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> pm.setPlayerSpectateAtLobby(uhcPlayer), 1);
		}
	}

}