package com.gmail.val59000mc.players;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.configuration.VaultManager;
import com.gmail.val59000mc.customitems.GameItem;
import com.gmail.val59000mc.customitems.KitsManager;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.events.*;
import com.gmail.val59000mc.exceptions.UhcPlayerDoesntExistException;
import com.gmail.val59000mc.exceptions.UhcPlayerJoinException;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.exceptions.UhcTeamException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.listeners.ItemsListener;
import com.gmail.val59000mc.listeners.PlayerConnectionListener;
import com.gmail.val59000mc.listeners.PlayerDeathListener;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import com.gmail.val59000mc.scenarios.scenariolisteners.SilentNightListener;
import com.gmail.val59000mc.schematics.DeathmatchArena;
import com.gmail.val59000mc.threads.CheckRemainingPlayerThread;
import com.gmail.val59000mc.threads.PreStartThread;
import com.gmail.val59000mc.threads.StartGameTimerThread;
import com.gmail.val59000mc.threads.TeleportPlayersThread;
import com.gmail.val59000mc.threads.TimeBeforeSendBungeeThread;
import com.gmail.val59000mc.utils.TimeUtils;
import com.gmail.val59000mc.utils.UniversalMaterial;
import com.gmail.val59000mc.utils.UniversalSound;
import com.gmail.val59000mc.utils.VersionUtils;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandException;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PlayersManager {

	private final List<UhcPlayer> players;
	private long lastDeathTime;
	private ScoreKeeper scoreKeeper;
	private Set<String> winners;
	private static Set<String> seen;

	public static SpawnLocations spawnLocations;

	private boolean lastTeam;

	public PlayersManager() {
		players = Collections.synchronizedList(new ArrayList<>());
		scoreKeeper = new ScoreKeeper();
		winners = new HashSet<>();
		spawnLocations = new SpawnLocations();
		seen = new HashSet<>();

		lastTeam = false;
	}

	public SpawnLocations getSpawnLocations() { return spawnLocations; }

	public void setLastDeathTime() { lastDeathTime = System.currentTimeMillis(); }

	public boolean isPlayerAllowedToJoin(Player player) throws UhcPlayerJoinException {
		GameManager gm = GameManager.getGameManager();
		UhcPlayer uhcPlayer;

		switch (gm.getGameState()) {
			case LOADING:
				throw new UhcPlayerJoinException(
						GameManager.getGameManager().getMapLoader().getLoadingState() + "% " + Lang.KICK_LOADING);

			case WAITING:
				return true;

			case STARTING:
				if (doesPlayerExist(player)) {
					uhcPlayer = getUhcPlayer(player);
					if (uhcPlayer.getState().equals(PlayerState.PLAYING)) {
						return true;
					} else {
						throw new UhcPlayerJoinException(Lang.KICK_STARTING);
					}
				} else {
					throw new UhcPlayerJoinException(Lang.KICK_STARTING);
				}
			case DEATHMATCH:
			case PLAYING:
				if (doesPlayerExist(player)) {
					uhcPlayer = getUhcPlayer(player);

					boolean canSpectate = gm.getConfiguration().getCanSpectateAfterDeath();
					if (uhcPlayer.getState().equals(PlayerState.PLAYING)
							|| ((canSpectate || player.hasPermission("uhc-core.spectate.override"))
									&& uhcPlayer.getState().equals(PlayerState.DEAD))) {
						return true;
					} else {
						throw new UhcPlayerJoinException(Lang.KICK_PLAYING);
					}
				} else {
					if (player.hasPermission("uhc-core.join-override")
							|| player.hasPermission("uhc-core.spectate.override")
							|| gm.getConfiguration().getCanJoinAsSpectator()
									&& gm.getConfiguration().getCanSpectateAfterDeath()) {
						UhcPlayer spectator = newUhcPlayer(player);
						spectator.setState(PlayerState.DEAD);
						return true;
					}
					throw new UhcPlayerJoinException(Lang.KICK_PLAYING);
				}

			case ENDED:
				if (player.hasPermission("uhc-core.join-override")) { return true; }
				throw new UhcPlayerJoinException(Lang.KICK_ENDED);

		}
		return false;
	}

	/**
	 * This method is used to get the UhcPlayer object from Bukkit Player. When
	 * using this method in the PlayerJoinEvent please check the
	 * doesPlayerExist(Player) to see if the player has a matching UhcPlayer.
	 * 
	 * @param player The Bukkit player you want the UhcPlayer from.
	 * @return Returns a UhcPlayer.
	 */
	public UhcPlayer getUhcPlayer(Player player) {
		try {
			return getUhcPlayer(player.getUniqueId());
		} catch (UhcPlayerDoesntExistException ex) {
			throw new RuntimeException(ex);
		}
	}

	public boolean doesPlayerExist(Player player) {
		try {
			getUhcPlayer(player.getUniqueId());
			return true;
		} catch (UhcPlayerDoesntExistException ex) {
			return false;
		}
	}

	public ScoreKeeper getScoreKeeper() { return scoreKeeper; }

	public UhcPlayer getUhcPlayer(String name) throws UhcPlayerDoesntExistException {
		for (UhcPlayer uhcPlayer : getPlayersList()) {
			if (uhcPlayer.getName().equals(name)) { return uhcPlayer; }
			try {
				if (uhcPlayer.getPlayer().getName().equalsIgnoreCase(name)) return uhcPlayer;
			} catch (UhcPlayerNotOnlineException e) {}
		}
		throw new UhcPlayerDoesntExistException(name);
	}

	public UhcPlayer getUhcPlayer(UUID uuid) throws UhcPlayerDoesntExistException {
		for (UhcPlayer uhcPlayer : getPlayersList()) { if (uhcPlayer.getUuid().equals(uuid)) { return uhcPlayer; } }
		throw new UhcPlayerDoesntExistException(uuid.toString());
	}

	public UhcPlayer getOrCreateUhcPlayer(Player player) {
		if (doesPlayerExist(player)) {
			return getUhcPlayer(player);
		} else {
			return newUhcPlayer(player);
		}
	}

	public synchronized UhcPlayer newUhcPlayer(Player bukkitPlayer) {
		return newUhcPlayer(bukkitPlayer.getUniqueId(), bukkitPlayer.getName());
	}

	public synchronized UhcPlayer newUhcPlayer(UUID uuid, String name) {
		UhcPlayer newPlayer = new UhcPlayer(uuid, name);
		getPlayersList().add(newPlayer);
		return newPlayer;
	}

	public synchronized List<UhcPlayer> getPlayersList() { return players; }

	public Set<UhcPlayer> getOnlinePlayingPlayers() {
		Set<UhcPlayer> playingPlayers = new HashSet<>();
		for (UhcPlayer p : getPlayersList()) {
			if (p.getState().equals(PlayerState.PLAYING) && p.isOnline()) { playingPlayers.add(p); }
		}
		return playingPlayers;
	}

	public Set<UhcPlayer> getOnlineSpectatingPlayers() {
		Set<UhcPlayer> playingPlayers = new HashSet<>();
		for (UhcPlayer p : getPlayersList()) {
			if (p.getState().equals(PlayerState.DEAD) && p.isOnline()) { playingPlayers.add(p); }
		}
		return playingPlayers;
	}

	public Set<UhcPlayer> getAllPlayingPlayers() {
		Set<UhcPlayer> playingPlayers = new HashSet<>();
		for (UhcPlayer p : getPlayersList()) {
			if (p.getState().equals(PlayerState.PLAYING)) { playingPlayers.add(p); }
		}
		return playingPlayers;
	}

	public void playerJoinsTheGame(Player player) {
		UhcPlayer uhcPlayer;

		GameManager gm = GameManager.getGameManager();

		if (doesPlayerExist(player)) {
			uhcPlayer = getUhcPlayer(player);
		} else {
			uhcPlayer = newUhcPlayer(player.getUniqueId(), "YEUH-BOT");
		}

		uhcPlayer.setUpScoreboard();

		switch (uhcPlayer.getState()) {
			case WAITING:
				setPlayerWaitsAtLobby(uhcPlayer);

				if (gm.getConfiguration().getAutoAssignNewPlayerTeam()) { autoAssignPlayerToTeam(uhcPlayer); }
				break;
			case PLAYING:
				setPlayerStartPlaying(uhcPlayer);

				if (!uhcPlayer.getHasBeenTeleportedToLocation()) {
					List<UhcPlayer> onlinePlayingMembers = uhcPlayer.getTeam().getOnlinePlayingMembers();

					// Only player in team so create random spawn location.
					if (onlinePlayingMembers.size() <= 1) {
						World world = gm.getLobby().getLoc().getWorld();
						uhcPlayer.getTeam().setStartingLocation(SpawnLocations.findRandomSafeLocation(world));
					}
					// Set spawn location at team mate.
					else {
						UhcPlayer teamMate = onlinePlayingMembers.get(0);
						if (teamMate == uhcPlayer) { teamMate = onlinePlayingMembers.get(1); }

						try {
							uhcPlayer.getTeam().setStartingLocation(teamMate.getPlayer().getLocation());
						} catch (UhcPlayerNotOnlineException ex) {
							ex.printStackTrace();
						}
					}

					// Apply start potion effect.
					for (PotionEffect effect : GameManager.getGameManager().getConfiguration()
							.getPotionEffectOnStart()) {
						player.addPotionEffect(effect);
					}

					// Teleport player
					player.teleport(uhcPlayer.getStartingLocation());
					uhcPlayer.setHasBeenTeleportedToLocation(true);

					// Remove lobby potion effects.
					player.removePotionEffect(PotionEffectType.BLINDNESS);
					player.removePotionEffect(PotionEffectType.SLOW_DIGGING);

					// Call event
					Bukkit.getPluginManager().callEvent(new PlayerStartsPlayingEvent(uhcPlayer));
				}
				if (uhcPlayer.getOfflineZombie() != null) {
					uhcPlayer.getOfflineZombie().remove();
					uhcPlayer.setOfflineZombie(null);
				}
				uhcPlayer.sendPrefixedMessage(Lang.PLAYERS_WELCOME_BACK_IN_GAME);
				break;
			case DEAD:
				setPlayerSpectateAtLobby(uhcPlayer);
				break;
		}
	}

	private void autoAssignPlayerToTeam(UhcPlayer uhcPlayer) {
		GameManager gm = GameManager.getGameManager();

		if (gm.getScenarioManager().isActivated(Scenario.LOVEATFIRSTSIGHT)) { return; }

		for (UhcTeam team : listUhcTeams()) {
			// Don't assign player to spectating team.
			if (team.isSpectating()) continue;

			if (team != uhcPlayer.getTeam()
					&& team.getMembers().size() < gm.getConfiguration().getMaxPlayersPerTeam()) {
				try {
					team.join(uhcPlayer);
				} catch (UhcTeamException ignored) {}
				break;
			}
		}
	}

	public void setPlayerWaitsAtLobby(UhcPlayer uhcPlayer) {
		uhcPlayer.setState(PlayerState.WAITING);
		GameManager gm = GameManager.getGameManager();
		if (!seen.contains(uhcPlayer.getName())) {
			PreStartThread.restartTimer();
			seen.add(uhcPlayer.getName());
		}

		GameManager.getGameManager().getListInventoryHandler().updatePlayer(uhcPlayer);

		Player player;
		try {
			player = uhcPlayer.getPlayer();
			player.teleport(gm.getLobby().getLoc());
			clearPlayerInventory(player);
			player.sendTitle("", "\u00a7dPick your favorite scenarios!");
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
					"Pick up to \u00a7d" + gm.getConfiguration().getMaxScenarioVotes() + " \u00a7fscenarios!"));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 99999999, 0), false);
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 99999999, 0), false);
			player.setHealth(20);
			player.setExhaustion(20);
			player.setFoodLevel(20);
			player.setExp(0);

			UhcItems.giveLobbyItemsTo(player);
		} catch (UhcPlayerNotOnlineException e) {
			// Do nothing beacause WAITING is a safe state
		}

		ItemsListener.voteForScenario(uhcPlayer, Scenario.BOTSIN);

	}

	public void setPlayerStartPlaying(UhcPlayer uhcPlayer) {

		Player player;
		MainConfiguration cfg = GameManager.getGameManager().getConfiguration();

		if (!uhcPlayer.getHasBeenTeleportedToLocation()) {
			uhcPlayer.setState(PlayerState.PLAYING);
			uhcPlayer.selectDefaultGlobalChat();
			GameManager.getGameManager().getListInventoryHandler().updatePlayer(uhcPlayer);

			try {
				player = uhcPlayer.getPlayer();
				clearPlayerInventory(player);
				player.setFireTicks(0);

				for (PotionEffect effect : player.getActivePotionEffects()) {
					player.removePotionEffect(effect.getType());
				}
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 1));
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 999999, 40));
				player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, 200));
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, 40));
				player.setGameMode(GameMode.SURVIVAL);
				if (cfg.getEnableExtraHalfHearts()) {
					VersionUtils.getVersionUtils().setPlayerMaxHealth(player, 20 + ((double) cfg.getExtraHalfHearts()));
					player.setHealth(20 + ((double) cfg.getExtraHalfHearts()));
				}

				// default world hunger levels
				player.setExhaustion(0);
				player.setSaturation(5);
				player.setFoodLevel(20);

				UhcItems.giveGameItemTo(player, GameItem.COMPASS_ITEM);
				UhcItems.giveGameItemTo(player, GameItem.CUSTOM_CRAFT_BOOK);
				KitsManager.giveKitTo(player);

				if (!uhcPlayer.getStoredItems().isEmpty()) {
					player.getInventory().addItem(uhcPlayer.getStoredItems().toArray(new ItemStack[] {}));
					uhcPlayer.getStoredItems().clear();
				}
			} catch (UhcPlayerNotOnlineException e) {
				// Nothing done
			}
		}
	}

	private void clearPlayerInventory(Player player) {
		player.getInventory().clear();

		// clear player armor
		ItemStack[] emptyArmor = new ItemStack[4];
		for (int i = 0; i < emptyArmor.length; i++) { emptyArmor[i] = new ItemStack(Material.AIR); }
		player.getInventory().setArmorContents(emptyArmor);

	}

	public void setPlayerSpectateAtLobby(UhcPlayer uhcPlayer) { setPlayerSpectateAtLobby(uhcPlayer, true); }

	public void setPlayerSpectateAtLobby(UhcPlayer uhcPlayer, boolean teleportToCenter) {

		uhcPlayer.setState(PlayerState.DEAD);
		uhcPlayer.sendPrefixedMessage(Lang.PLAYERS_WELCOME_BACK_SPECTATING);
		GameManager.getGameManager().getListInventoryHandler().updatePlayer(uhcPlayer);

		Player player;
		try {
			player = uhcPlayer.getPlayer();
			player.getEquipment().clear();
			clearPlayerInventory(player);

			player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
					new TextComponent("Use \u00a7d/tp <Player> \u00a7fto go to spectate a player!"));

			player.setCollidable(false);
			player.setCanPickupItems(false);
			player.setAllowFlight(true);
			player.setFlying(true);
			player.setInvisible(true);
			player.setInvulnerable(true);
			player.setArrowsInBody(0);
			player.setPlayerListName("\u00a78\u00a7o" + player.getName());

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p == player) continue;
				GameManager.getGameManager().getEntityHider().showEntity(player, p);
				if (getUhcPlayer(p).getState() == PlayerState.PLAYING) {
					GameManager.getGameManager().getEntityHider().hideEntity(p, player);
				} else {
					GameManager.getGameManager().getEntityHider().showEntity(p, player);
				}
			}

			for (NPC npc : CitizensAPI.getNPCRegistry()) {
				SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
				new SentinelTargetLabel("player:" + player.getName()).addToList(sentinel.allIgnores);
			}

			for (PotionEffect effect : player.getActivePotionEffects()) { player.removePotionEffect(effect.getType()); }
			if (teleportToCenter) {
				if (GameManager.getGameManager().getGameState().equals(GameState.DEATHMATCH)) {
					player.teleport(GameManager.getGameManager().getArena().getLoc());
				} else {
					player.teleport(GameManager.getGameManager().getLobby().getLoc());
				}
			}

			UhcItems.giveSpectatorItemsTo(player);
		} catch (UhcPlayerNotOnlineException e) {
			// Do nothing because DEAD is a safe state
		}
	}

	public void setAllPlayersEndGame() {
		GameManager gm = GameManager.getGameManager();
		MainConfiguration cfg = gm.getConfiguration();

		List<UhcPlayer> uhcWinners = getUhcWinners();

		// send to bungee
		if (cfg.getEnableBungeeSupport() && cfg.getTimeBeforeSendBungeeAfterEnd() >= 0) {
			for (UhcPlayer player : getPlayersList()) {
				Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(),
						new TimeBeforeSendBungeeThread(this, player, cfg.getTimeBeforeSendBungeeAfterEnd()));
			}
		}

		UhcWinEvent event = new UhcWinEvent(new HashSet<>(uhcWinners));
		Bukkit.getServer().getPluginManager().callEvent(event);

		double reward = cfg.getRewardWinEnvent();
		List<String> winCommands = cfg.getWinCommands();
		List<String> winCommandsPlayer = new ArrayList<>();
		for (String cmd : winCommands) { if (cmd.contains("%name%")) { winCommandsPlayer.add(cmd); } }
		winCommands.removeAll(winCommandsPlayer);

		Bukkit.getOnlinePlayers().forEach(player -> {
			player.setAllowFlight(true);
			player.setFlying(true);
		});

		if (cfg.getEnableWinEvent()) {
			for (UhcPlayer player : uhcWinners) {
				try {
					if (reward > 0) {
						if (!Lang.EVENT_WIN_REWARD.isEmpty()) {
							player.getPlayer().sendMessage(Lang.EVENT_WIN_REWARD.replace("%money%", "" + reward));
						}
						VaultManager.addMoney(player.getPlayer(), reward);
					}

					winCommandsPlayer.forEach(cmd -> {
						try {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
									cmd.replace("%name%", player.getRealName()));
						} catch (CommandException exception) {
							Bukkit.getLogger().warning("[UhcCore] Failed to execute win reward command: " + cmd);
							exception.printStackTrace();
						}
					});
				} catch (UhcPlayerNotOnlineException e) {
					// no reward for offline players
				}
			}
			if (!winCommands.isEmpty()) {
				winCommands.forEach(cmd -> {
					try {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
					} catch (CommandException exception) {
						Bukkit.getLogger().warning("[UhcCore] Failed to execute win reward command: " + cmd);
						exception.printStackTrace();
					}
				});
			}
		}
		// When the game finished set all player states to DEAD
		getPlayersList().forEach(player -> player.setState(PlayerState.DEAD));
	}

	public Set<String> getWinners() { return winners; }

	private List<UhcPlayer> getUhcWinners() {
		List<UhcPlayer> uhcWinners = new ArrayList<>();
		for (UhcPlayer player : getPlayersList()) {
			if (player.getState().equals(PlayerState.PLAYING)) winners.add(player.getName());
			try {
				Player connected = player.getPlayer();
				if (connected.isOnline() && player.getState().equals(PlayerState.PLAYING)) uhcWinners.add(player);
			} catch (UhcPlayerNotOnlineException e) {
				// not adding the player to winner list
			}
		}
		return uhcWinners;
	}

	public List<UhcTeam> listUhcTeams() {
		List<UhcTeam> teams = new ArrayList<>();
		for (UhcPlayer player : getPlayersList()) {
			UhcTeam team = player.getTeam();
			if (!teams.contains(team)) teams.add(team);
		}
		return teams;
	}

	public void randomTeleportTeams() {
		GameManager gm = GameManager.getGameManager();

		// Fore solo players to join teams
		if (gm.getConfiguration().getForceAssignSoloPlayerToTeamWhenStarting()) {
			for (UhcPlayer uhcPlayer : getPlayersList()) {
				// If player is spectating don't assign player.
				if (uhcPlayer.getState() == PlayerState.DEAD) { continue; }

				if (uhcPlayer.getTeam().getMembers().size() == 1) { autoAssignPlayerToTeam(uhcPlayer); }
			}
		}

		Bukkit.getOnlinePlayers().forEach(player -> {
			player.sendTitle("", "\u00a77Game starting momentarily...", 0, 6000, 0);
			player.sendMessage(UhcCore.PREFIX + "\u00a7fScenarios for this game: "
					+ gm.getScenarioManager().getActiveScenarios().stream()
							.map(scenario -> "\u00a7d" + scenario.getName() + "\u00a7f")
							.collect(Collectors.joining(", ")));
			player.sendMessage(UhcCore.PREFIX + "\u00a77(Use \u00a7d/game \u00a77for more info)");
		});

		for (UhcTeam team : listUhcTeams()) { team.setStartingLocation(SpawnLocations.getStoredLocs().poll()); }

		Bukkit.getPluginManager().callEvent(new UhcPreTeleportEvent());

		long delayTeleportByTeam = 0;

		for (UhcTeam team : listUhcTeams()) {

			if (team.isSpectating()) {
				gm.getPlayersManager().setPlayerSpectateAtLobby(team.getLeader());
				continue;
			}

			for (UhcPlayer uhcPlayer : team.getMembers()) { gm.getPlayersManager().setPlayerStartPlaying(uhcPlayer); }

			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(),
					new TeleportPlayersThread(GameManager.getGameManager(), team), delayTeleportByTeam);
			delayTeleportByTeam += 1; // ticks
		}

		Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new StartGameTimerThread(), delayTeleportByTeam + 20);

	}

	public void strikeLightning(UhcPlayer uhcPlayer) {
		try {
			Location loc = uhcPlayer.getPlayer().getLocation();
			loc.getWorld().strikeLightningEffect(loc);
			loc.getWorld().getBlockAt(loc).setType(Material.AIR);
		} catch (UhcPlayerNotOnlineException e) {
			Location loc = GameManager.getGameManager().getLobby().getLoc();
			loc.getWorld().strikeLightningEffect(loc);
			loc.getWorld().getBlockAt(loc).setType(Material.AIR);
		}

		// Extinguish fire
	}

	public void playSoundToAll(UniversalSound sound) {
		for (UhcPlayer player : getPlayersList()) { playsoundTo(player, sound); }
	}

	public void playSoundToAll(UniversalSound sound, float v, float v1) {
		for (UhcPlayer player : getPlayersList()) { playsoundTo(player, sound, v, v1); }
	}

	public void playsoundTo(UhcPlayer player, UniversalSound sound) { playsoundTo(player, sound, 1, 1); }

	public void playsoundTo(UhcPlayer player, UniversalSound sound, float v, float v1) {
		try {
			Player p = player.getPlayer();
			p.playSound(p.getLocation(), sound.getSound(), v, v1);
		} catch (UhcPlayerNotOnlineException e) {
			// No sound played
		}
	}

	public void checkIfRemainingPlayers() {
		int aliveBots = 0;
		int alivePlayers = 0;
		int alivePlayersOnline = 0;

		int alivePlayerTeams = 0;
		int aliveOnlyBotTeams = 0;
		int alivePlayerTeamsOnline = 0;

		boolean botsin = false;

		for (UhcPlayer player : getPlayersList()) {
			if (player.getName().equals("YEUH-BOT")) {
				botsin = true;
				break;
			}
		}

		for (UhcTeam team : listUhcTeams()) {

			int teamIsOnline = 0;
			int teamIsPlaying = 0;
			int botTeam = botsin ? 1 : 0;

			for (UhcPlayer player : team.getMembers()) {
				if (player.getState().equals(PlayerState.PLAYING)) {

					teamIsPlaying = 1;

					if (player.getName().equals("YEUH-BOT")) aliveBots++;
					else {
						botTeam = 0;
						alivePlayers++;

						try {
							player.getPlayer();
							alivePlayersOnline++;
							teamIsOnline = 1;
						} catch (UhcPlayerNotOnlineException e) {
							// Player isn't online
						}
					}
				}
			}

			alivePlayerTeamsOnline += teamIsOnline;
			alivePlayerTeams += (1 - botTeam) * teamIsPlaying;
			aliveOnlyBotTeams += botTeam * teamIsPlaying;
		}

		int aliveTeams = alivePlayerTeams + aliveOnlyBotTeams;

		GameManager gm = GameManager.getGameManager();
		MainConfiguration cfg = gm.getConfiguration();

		if (cfg.getEnableTimeLimit() && gm.getRemainingTime() <= 0 && gm.getGameState().equals(GameState.PLAYING)) {
			// start deathmatch if time limit (I dont think I need this)
			gm.startDeathmatch();
			return;
		}

		if (gm.getGameState() == GameState.DEATHMATCH && cfg.getEnableDeathmatchForceEnd() && gm.getPvp()
				&& (lastDeathTime + (cfg.getDeathmatchForceEndDelay() * TimeUtils.SECOND)) < System
						.currentTimeMillis()) {
			// if deathmatch last death time runs out, end game
			gm.endGame();
			return;
		}

		if (alivePlayers == 0 || alivePlayerTeams == 0) {
			if (alivePlayers > 0 || alivePlayerTeams > 0)
				Bukkit.getLogger().warning("Something went wrong when counting alive players and teams");

			// there are no alive real players, end game
			gm.endGame();
			return;
		}

		if (aliveTeams == 1) {
			// win game
			gm.endGame();
			return;
		}

		if (alivePlayerTeams == 1) {
			// only one team that has players in it exists, give them the option to end game
			// or continue playing
			if (!lastTeam && gm.getStartRealTeams() > 1) {
				Bukkit.getScheduler().runTaskTimer(UhcCore.getPlugin(), () -> {
					for (UhcTeam team : gm.getTeamManager().getUhcTeams()) {
						if (team.getMembers().stream().allMatch(member -> member.getState() != PlayerState.PLAYING
								|| member.getName().equalsIgnoreCase("YEUH-BOT")))
							continue;
						team.sendMessage("");
						if (!team.isSolo()) team.sendMessage(UhcCore.PREFIX
								+ "Your team is the last team that has real players in it! You can continue to play if you like,");
						else team.sendMessage(UhcCore.PREFIX
								+ "You are the last real player alive! You can continue to play if you like,");
						team.sendMessage(UhcCore.PREFIX
								+ "But if you die it will count as a loss for you. Otherwise, use \u00a7d/end \u00a7fto end the game and claim your victory!");
						team.sendMessage("");
					}
				}, 0, 30 * 20);
				lastTeam = true;
			}
		}

		if (alivePlayersOnline == 0 || alivePlayerTeamsOnline == 0) {
			if (alivePlayersOnline > 0 || alivePlayerTeamsOnline > 0)
				Bukkit.getLogger().warning("Something went wrong when counting alive players and teams online");
			// If all alive players have logged out mid game, start a countdown
			if (cfg.getEndGameWhenAllPlayersHaveLeft()) gm.startEndGameThread();
			return;
		}

		if ((aliveBots == 0 || aliveOnlyBotTeams == 0) && alivePlayerTeamsOnline == 1) {
			// If all competition has left, start a countdown
			if (cfg.getEndGameWhenAllPlayersHaveLeft() && !cfg.getOnePlayerMode()) gm.startEndGameThread();
			return;
		}

		// stop game ending if it gets here
		if (gm.getGameIsEnding()) gm.stopEndGameThread();
	}

	public void startWatchPlayerPlayingThread() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
		}

		// Unfreeze players
		for (UhcPlayer uhcPlayer : getPlayersList()) {
			uhcPlayer.releasePlayer();
			Bukkit.getPluginManager().callEvent(new PlayerStartsPlayingEvent(uhcPlayer));
		}

		Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(),
				new CheckRemainingPlayerThread(GameManager.getGameManager()), 40);
	}

	public void sendPlayerToBungeeServer(Player player) {
		PlayerConnectionListener.addToSendingToLobby(player);
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(GameManager.getGameManager().getConfiguration().getServerBungee());
		player.sendMessage(Lang.PLAYERS_SEND_BUNGEE_NOW);
		player.sendPluginMessage(UhcCore.getPlugin(), "BungeeCord", out.toByteArray());
	}

	public void setAllPlayersStartDeathmatch() {
		GameManager gm = GameManager.getGameManager();
		MainConfiguration cfg = gm.getConfiguration();
		DeathmatchArena arena = gm.getArena();

		if (arena.isUsed()) {
			List<Location> spots = arena.getTeleportSpots();

			int spotIndex = 0;

			for (UhcTeam teams : listUhcTeams()) {
				boolean playingPlayer = false;
				for (UhcPlayer player : teams.getMembers()) {
					try {
						Player bukkitPlayer = player.getPlayer();
						if (player.getState().equals(PlayerState.PLAYING)) {
							if (cfg.getIsDeathmatchAdvantureMode()) {
								bukkitPlayer.setGameMode(GameMode.ADVENTURE);
							} else {
								bukkitPlayer.setGameMode(GameMode.SURVIVAL);
							}
							bukkitPlayer.eject();
							Location loc = spots.get(spotIndex);
							// player.freezePlayer(loc);
							bukkitPlayer.teleport(loc, TeleportCause.NETHER_PORTAL);
							playingPlayer = true;
						} else {
							bukkitPlayer.teleport(arena.getLoc(), TeleportCause.NETHER_PORTAL);
						}
					} catch (UhcPlayerNotOnlineException e) {
						// Do nothing for offline players
					}
				}
				if (playingPlayer) { spotIndex++; }
				if (spotIndex == spots.size()) { spotIndex = 0; }
			}
		}

		// DeathMatch at 0 0
		else {
			Queue<Location> randomLocations = SpawnLocations.getRandomSafeLocations(gm.getLobby().getLoc().getWorld(),
					listUhcTeams().size(), cfg.getDeathmatchStartSize() - 10, 10);

			for (UhcTeam team : listUhcTeams()) {
				Location teleportSpot = randomLocations.poll();

				for (UhcPlayer player : team.getMembers()) {
					try {
						Player bukkitPlayer = player.getPlayer();

						if (player.getState().equals(PlayerState.PLAYING)) {
							if (cfg.getIsDeathmatchAdvantureMode()) {
								bukkitPlayer.setGameMode(GameMode.ADVENTURE);
							} else {
								bukkitPlayer.setGameMode(GameMode.SURVIVAL);
							}

							player.freezePlayer(teleportSpot);
							bukkitPlayer.teleport(teleportSpot);
						} else {
							bukkitPlayer.teleport(gm.getLobby().getLoc());
						}
					} catch (UhcPlayerNotOnlineException e) {
						// Do nothing for offline players
					}
				}
			}
		}
	}

	public void playSoundPlayerDeath() {
		Sound sound = GameManager.getGameManager().getConfiguration().getSoundOnPlayerDeath();
		if (sound != null) {
			for (Player player : Bukkit.getOnlinePlayers()) { player.playSound(player.getLocation(), sound, 1, 1); }
		}
	}

	public void killOfflineUhcPlayer(UhcPlayer uhcPlayer, Set<ItemStack> playerDrops) {
		killOfflineUhcPlayer(uhcPlayer, null, playerDrops, null);
	}

	public void killOfflineUhcPlayer(UhcPlayer uhcPlayer, @Nullable Location location, Set<ItemStack> playerDrops,
			@Nullable Player killer) {
		GameManager gm = GameManager.getGameManager();
		PlayersManager pm = gm.getPlayersManager();

		if (uhcPlayer.getState() != PlayerState.PLAYING) {
			Bukkit.getLogger().warning("[UhcCore] " + uhcPlayer.getName() + " died while already in 'DEAD' mode!");
			return;
		}

		Player killedUnsafe = uhcPlayer.getPlayerUnsafe();
		if (location != null) killedUnsafe.teleport(location);

		// kill event
		if (killer != null) {
			UhcPlayer uhcKiller = pm.getUhcPlayer(killer);

			PlayerDeathListener.handleKill(uhcPlayer.getPlayerUnsafe(), killer,
					uhcPlayer.getDisplayName() + " was slain by " + uhcKiller.getDisplayName());
		} else if (gm.getStartPlayers() > 1) {
			PlayerDeathListener.handleNaturalDeath(uhcPlayer.getPlayerUnsafe(), uhcPlayer.getName() + " died", "Death");
		}
	}

	public void spawnOfflineZombieFor(Player player) {
		UhcPlayer uhcPlayer = getUhcPlayer(player);

		Zombie zombie = (Zombie) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
		zombie.setCustomName(uhcPlayer.getDisplayName());
		zombie.setCustomNameVisible(true);
		// 1.8 doesn't have setAI method so use VersionUtils.
		VersionUtils.getVersionUtils().setEntityAI(zombie, false);
		zombie.setBaby(false);
		zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1, true, true));

		EntityEquipment equipment = zombie.getEquipment();
		equipment.setHelmet(VersionUtils.getVersionUtils().createPlayerSkull(player.getName(), player.getUniqueId()));
		equipment.setChestplate(player.getInventory().getChestplate());
		equipment.setLeggings(player.getInventory().getLeggings());
		equipment.setBoots(player.getInventory().getBoots());
		equipment.setItemInHand(player.getItemInHand());

		uhcPlayer.getStoredItems().clear();
		for (ItemStack item : player.getInventory().getContents()) {
			if (item != null) { uhcPlayer.getStoredItems().add(item); }
		}

		uhcPlayer.setOfflineZombie(zombie);
	}

	public UhcPlayer revivePlayer(UUID uuid, String name, boolean spawnWithItems) {
		UhcPlayer uhcPlayer;

		try {
			uhcPlayer = getUhcPlayer(uuid);
		} catch (UhcPlayerDoesntExistException ex) {
			uhcPlayer = newUhcPlayer(uuid, name);
		}

		revivePlayer(uhcPlayer, spawnWithItems);
		return uhcPlayer;
	}

	public void revivePlayer(UhcPlayer uhcPlayer, boolean spawnWithItems) {
		uhcPlayer.setHasBeenTeleportedToLocation(false);
		uhcPlayer.setState(PlayerState.PLAYING);

		// If not respawn with items, clear stored items.
		if (!spawnWithItems) { uhcPlayer.getStoredItems().clear(); }

		try {
			playerJoinsTheGame(uhcPlayer.getPlayer());
		} catch (UhcPlayerNotOnlineException ex) {
			// Player gets revived next time they attempt to join.
		}
	}

}