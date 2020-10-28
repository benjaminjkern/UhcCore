package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.configuration.VaultManager;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.events.UhcPlayerKillEvent;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioManager;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerDeathListener implements Listener {

	private static Set<String> animalTypes;
	private static Set<String> monsterTypes;

	static {
		animalTypes = new HashSet<>();
		animalTypes.add("Bee");
		animalTypes.add("Cat");
		animalTypes.add("Chicken");
		animalTypes.add("Cow");
		animalTypes.add("Donkey");
		animalTypes.add("Fox");
		animalTypes.add("Hoglin");
		animalTypes.add("Horse");
		animalTypes.add("Llama");
		animalTypes.add("Mule");
		animalTypes.add("Mushroom Cow");
		animalTypes.add("Ocelot");
		animalTypes.add("Panda");
		animalTypes.add("Parrot");
		animalTypes.add("Pig");
		animalTypes.add("Polar Bear");
		animalTypes.add("Rabbit");
		animalTypes.add("Sheep");
		animalTypes.add("Skeleton Horse");
		animalTypes.add("Strider");
		animalTypes.add("Chicken");
		animalTypes.add("Trader Llama");
		animalTypes.add("Turtle");
		animalTypes.add("Wolf");
		animalTypes.add("Zombie Horse");

		monsterTypes = new HashSet<>();
		monsterTypes.add("Blaze");
		monsterTypes.add("Cave Spider");
		monsterTypes.add("Creeper");
		monsterTypes.add("Drowned");
		monsterTypes.add("Elder Guardian");
		monsterTypes.add("Enderman");
		monsterTypes.add("Endermite");
		monsterTypes.add("Evoker");
		monsterTypes.add("Giant");
		monsterTypes.add("Guardian");
		monsterTypes.add("Husk");
		monsterTypes.add("Illager");
		monsterTypes.add("Illusioner");
		monsterTypes.add("Piglin");
		monsterTypes.add("Piglin Brute");
		monsterTypes.add("Zombified Piglin");
		monsterTypes.add("Pillager");
		monsterTypes.add("Raider");
		monsterTypes.add("Savager");
		monsterTypes.add("Silverfish");
		monsterTypes.add("Skeleton");
		monsterTypes.add("Spellcaster");
		monsterTypes.add("Spider");
		monsterTypes.add("Stray");
		monsterTypes.add("Vex");
		monsterTypes.add("Vindicator");
		monsterTypes.add("Witch");
		monsterTypes.add("Wither");
		monsterTypes.add("Wither Skeleton");
		monsterTypes.add("Zoglin");
		monsterTypes.add("Zombie");
		monsterTypes.add("Zombie Villager");
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		GameManager gm = GameManager.getGameManager();
		PlayersManager pm = gm.getPlayersManager();
		MainConfiguration cfg = gm.getConfiguration();
		UhcPlayer uhcPlayer = pm.getUhcPlayer(player);

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

		// kill event
		Player killer = player.getKiller();
		if (killer != null) {
			UhcPlayer uhcKiller = pm.getUhcPlayer(killer);

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
		} else if (gm.getStartPlayers() > 1) {
			boolean found = false;

			for (String s : animalTypes) {
				if (event.getDeathMessage().contains(s)) {
					gm.sendInfoToServer("KILL:YEUH-ANIMAL:" + uhcPlayer.getName(), false);
					found = true;
					break;
				}
			}

			for (String s : monsterTypes) {
				if (event.getDeathMessage().contains(s) && !found) {
					found = true;
					gm.sendInfoToServer("KILL:YEUH-MONSTER:" + uhcPlayer.getName(), false);
					break;
				}
			}
			if (!found) gm.sendInfoToServer("DEATH:" + uhcPlayer.getName(), false);
		}

		// Store drops in case player gets re-spawned.
		uhcPlayer.getStoredItems().clear();
		uhcPlayer.getStoredItems().addAll(event.getDrops());

		// eliminations
		ScenarioManager sm = gm.getScenarioManager();
		if (!sm.isActivated(Scenario.SILENTNIGHT)
				|| !((SilentNightListener) sm.getScenarioListener(Scenario.SILENTNIGHT)).isNightMode()) {
			gm.broadcastInfoMessage(Lang.PLAYERS_ELIMINATED.replace("%player%", player.getName()));
		}

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

		if (cfg.getEnableExpDropOnDeath()) { UhcItems.spawnExtraXp(player.getLocation(), cfg.getExpDropOnDeath()); }

		uhcPlayer.setState(PlayerState.DEAD);
		pm.strikeLightning(uhcPlayer);
		pm.playSoundPlayerDeath();
		Bukkit.getLogger().info(UhcCore.PREFIX + player.getName() + " has been eliminated!");

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
		}

		pm.checkIfRemainingPlayers();
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