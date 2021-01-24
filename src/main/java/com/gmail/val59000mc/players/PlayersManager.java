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
import com.gmail.val59000mc.game.UhcWorldBorder;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.listeners.EntityHider;
import com.gmail.val59000mc.listeners.PlayerConnectionListener;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import javax.annotation.Nullable;
import java.util.*;

public class PlayersManager {

	private final List<UhcPlayer> players;
	private long lastDeathTime;
	private ScoreKeeper scoreKeeper;
	private static Queue<Location> randLocs;

	private static Set<Material> nonoBlocks;

	private Set<String> winners;

	public PlayersManager() {
		players = Collections.synchronizedList(new ArrayList<>());
		scoreKeeper = new ScoreKeeper();
		winners = new HashSet<>();
		nonoBlocks = new HashSet<>();
		nonoBlocks.add(Material.LAVA);
		nonoBlocks.add(Material.WATER);
		nonoBlocks.add(Material.MAGMA_BLOCK);
		nonoBlocks.add(Material.CACTUS);
	}

	public static void createSpawnLocations(int num) {
		GameManager gm = GameManager.getGameManager();
		World world = Bukkit.getWorld(gm.getConfiguration().getOverworldUuid());
		randLocs = getRandomSafeLocations(world, num, 400 / Math.sqrt(num));
		gm.finishLoad("GENERATESPAWNPOINTS");
	}

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
						double maxDistance = 0.9 * gm.getWorldBorder().getCurrentSize();
						uhcPlayer.getTeam().setStartingLocation(findRandomSafeLocation(world));
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
		PreStartThread.restartTimer();

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

	public void setPlayerSpectateAtLobby(UhcPlayer uhcPlayer) {

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
			if (GameManager.getGameManager().getGameState().equals(GameState.DEATHMATCH)) {
				player.teleport(GameManager.getGameManager().getArena().getLoc());
			} else {
				player.teleport(GameManager.getGameManager().getLobby().getLoc());
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

		Bukkit.getOnlinePlayers()
				.forEach(player -> { player.sendTitle("", "\u00a77Game starting momentarily...", 0, 6000, 0); });

		for (UhcTeam team : listUhcTeams()) { team.setStartingLocation(randLocs.poll()); }

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

	public static Location newRandomLocation(World world) {
		return newRandomLocation(world, Math.min(world.getWorldBorder().getSize() / 2.,
				GameManager.getGameManager().getConfiguration().getBorderStartSize()));
	}

	public static Location newRandomLocation(World world, double range) {
		Random r = new Random();
		WorldBorder wb = world.getWorldBorder();
		double x = range * (r.nextDouble() * 2 - 1) + wb.getCenter().getX();
		double z = range * (r.nextDouble() * 2 - 1) + wb.getCenter().getZ();
		return new Location(world, x, 0, z);
	}

	/**
	 * Returns location of ground.
	 * 
	 * @param loc        Location to look for ground.
	 * @param allowCaves When set to true, the first location on the y axis is
	 *                   returned. This will include caves.
	 * @return Ground location.
	 */
	private static Location getGroundLocation(Location loc, boolean allowCaves) {
		World w = loc.getWorld();

		Location setLoc = loc.clone();

		if (allowCaves) {
			setLoc.setY(0);
			setLoc.setX((int) (setLoc.getX()) + 0.5);
			setLoc.setZ((int) (setLoc.getZ()) + 0.5);
			while (setLoc.getBlock().getType() != Material.AIR
					|| setLoc.clone().add(0, 1, 0).getBlock().getType() != Material.AIR
					|| setLoc.clone().add(0, 2, 0).getBlock().getType() != Material.AIR
					|| setLoc.clone().add(0, 3, 0).getBlock().getType() != Material.AIR) {
				setLoc.add(0, 1, 0);
			}
		} else {
			setLoc = w.getHighestBlockAt(setLoc).getLocation().clone().add(0.5, 1, 0.5);
		}
		return setLoc;
	}

	public static Queue<Location> getRandomSafeLocations(World world, int num, double minDist) {
		return getRandomSafeLocations(world, num, minDist, 20);
	}

	public static Queue<Location> getRandomSafeLocations(World world, int num, double minDist, int bailout) {
		List<Location> checkedLocations = new LinkedList<>();

		for (int i = 0; i < num; i++) {
			Location randomLoc = findRandomSafeLocation(world);

			boolean verified = false;
			for (int j = 0; j < bailout && !verified; j++) {
				verified = true;
				for (Location l : checkedLocations) {
					if (l.distanceSquared(randomLoc) < minDist * minDist) {
						verified = false;
						// Bukkit.getLogger().info("[UhcCore] Location " + randomLoc.toString() + " was
						// within " + minDist
						// + " of another team");
						randomLoc = findRandomSafeLocation(world);
						break;
					}
				}
			}

			if (!verified) Bukkit.getLogger().info("[UhcCore] Location " + randomLoc.toString() + " was within "
					+ minDist + " blocks of another team, but the bailout was reached.");
			else Bukkit.getLogger().info("[UhcCore] Spawn location " + (i + 1) + "/" + num + " found.");
			checkedLocations.add(randomLoc);
		}

		return new LinkedList<>(checkedLocations);
	}

	public static Location findRandomSafeLocation(World world) { return findRandomSafeLocation(world, 20); }

	public static Location findRandomSafeLocation(World world, int bailout) {
		for (int i = 0; i < bailout; i++) {
			Location loc = verifySafe(newRandomLocation(world));
			if (loc != null) return loc;
		}
		Bukkit.getLogger().info("[UHCCore] Bailout reached, returning random location");
		boolean nether = world.getEnvironment() == World.Environment.NETHER;
		return getGroundLocation(newRandomLocation(world), nether);
	}

	public static Location verifySafe(Location loc) {
		boolean nether = loc.getWorld().getEnvironment() == World.Environment.NETHER;
		Location ground = getGroundLocation(loc, nether);

		ground.setPitch(0);
		ground.setYaw(0);

		if (nether && ground.getBlockY() > 120) return null;

		Material material = ground.clone().add(0, -1, 0).getBlock().getType();
		while (material == Material.BARRIER) {
			ground = getBelow(ground);
			if (ground == null) return null;
			material = ground.clone().add(0, -1, 0).getBlock().getType();
		}
		if (!material.isSolid() || nonoBlocks.contains(material)
				|| !GameManager.getGameManager().getWorldBorder().isWithinBorder(ground))
			return null;
		return ground;
	}

	private static Location getBelow(Location loc) {
		int y = -2;
		while (loc.getBlockY() + y > 0 && loc.getWorld()
				.getBlockAt(loc.getBlockX(), loc.getBlockY() + y, loc.getBlockZ()).getType() == Material.AIR) {
			if (loc.getBlockY() + y <= 0) return null;
			y--;
		}
		return loc.clone().add(0, y + 1, 0);
	}

	@Nullable
	public static Location findSafeLocationAround(Location loc, int range) {
		Location betterLocation = verifySafe(loc);
		if (betterLocation != null) return betterLocation;

		Location testLoc = loc.clone();

		int i = 1;
		Vector dir = new Vector(0, 0, 1);
		// dear god I hope it never gets to full map size
		while (i < range * 2) {
			for (int j = 0; j < i; j++) {
				betterLocation = verifySafe(testLoc);
				if (betterLocation != null) return betterLocation;
				testLoc.add(dir);
			}

			dir = new Vector(dir.getZ(), 0, -dir.getX());

			for (int j = 0; j < i; j++) {
				betterLocation = verifySafe(testLoc);
				if (betterLocation != null) return betterLocation;
				testLoc.add(dir);
			}

			dir = new Vector(dir.getZ(), 0, -dir.getX());
			i++;
		}

		// Bukkit.getLogger().info("[UhcCore] Could not find any safe spawn spot near "
		// + loc.toString() + " within "
		// + range + " blocks!");

		return null;
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
		int playingBots = 0;
		int playingPlayers = 0;
		int playingPlayersOnline = 0;
		int playingTeams = 0;
		int playingTeamsOnline = 0;

		for (UhcTeam team : listUhcTeams()) {

			int teamIsOnline = 0;
			int teamIsPlaying = 0;

			for (UhcPlayer player : team.getMembers()) {
				if (player.getState().equals(PlayerState.PLAYING)) {
					playingPlayers++;
					teamIsPlaying = 1;

					if (player.getName().equals("YEUH-BOT")) playingBots++;

					try {
						player.getPlayer();
						playingPlayersOnline++;
						teamIsOnline = 1;
					} catch (UhcPlayerNotOnlineException e) {
						// Player isn't online
					}
				}
			}

			playingTeamsOnline += teamIsOnline;
			playingTeams += teamIsPlaying;
		}

		GameManager gm = GameManager.getGameManager();
		MainConfiguration cfg = gm.getConfiguration();
		if (cfg.getEnableTimeLimit() && gm.getRemainingTime() <= 0 && gm.getGameState().equals(GameState.PLAYING)) {
			gm.startDeathmatch();
		} else if (playingPlayers == 0 || (Bukkit.getOnlinePlayers().size() == 0 && playingPlayers == playingBots)) {
			gm.endGame();
		} else if (gm.getGameState() == GameState.DEATHMATCH && cfg.getEnableDeathmatchForceEnd() && gm.getPvp()
				&& (lastDeathTime + (cfg.getDeathmatchForceEndDelay() * TimeUtils.SECOND)) < System
						.currentTimeMillis()) {
							gm.endGame();
						} else
			if (playingPlayers > 0 && playingPlayersOnline == 0) {
				// Check if all playing players have left the game
				if (cfg.getEndGameWhenAllPlayersHaveLeft()) { gm.startEndGameThread(); }
			} else if (playingPlayers > 0 && playingPlayersOnline > 0 && playingTeamsOnline == 1 && playingTeams == 1
					&& gm.getStartPlayers() > 1) {
						// Check if one playing team remains
						gm.endGame();
					} else
				if (playingPlayers > 0 && playingPlayersOnline > 0 && playingTeamsOnline == 1 && playingTeams > 1) {
					// Check if one playing team remains
					if (cfg.getEndGameWhenAllPlayersHaveLeft() && !cfg.getOnePlayerMode()) { gm.startEndGameThread(); }
				} else if (gm.getGameIsEnding()) { gm.stopEndGameThread(); }

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
			Queue<Location> randomLocations = getRandomSafeLocations(gm.getLobby().getLoc().getWorld(),
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
		MainConfiguration cfg = gm.getConfiguration();

		if (uhcPlayer.getState() != PlayerState.PLAYING) {
			Bukkit.getLogger().warning("[UhcCore] " + uhcPlayer.getName() + " died while already in 'DEAD' mode!");
			return;
		}

		// kill event
		if (killer != null) {
			UhcPlayer uhcKiller = pm.getUhcPlayer(killer);

			uhcKiller.kills++;

			gm.sendInfoToServer("KILL:" + uhcKiller.getName() + ":" + uhcPlayer.getName(), false);

			// Call Bukkit event
			UhcPlayerKillEvent killEvent = new UhcPlayerKillEvent(uhcKiller, uhcPlayer);
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

				killCommands.forEach(cmd -> {
					try {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%name%", killer.getName()));
					} catch (CommandException exception) {
						Bukkit.getLogger().warning("[UhcCore] Failed to execute kill reward command: " + cmd);
						exception.printStackTrace();
					}
				});

			}
		} else if (gm.getStartPlayers() > 1) { gm.sendInfoToServer("DEATH:" + uhcPlayer.getName(), false); }

		// Store drops in case player gets re-spawned.
		uhcPlayer.getStoredItems().clear();
		uhcPlayer.getStoredItems().addAll(playerDrops);

		// eliminations
		ScenarioManager sm = gm.getScenarioManager();
		if (!sm.isActivated(Scenario.SILENTNIGHT)
				|| !((SilentNightListener) sm.getScenarioListener(Scenario.SILENTNIGHT)).isNightMode()) {
			gm.broadcastInfoMessage(Lang.PLAYERS_ELIMINATED.replace("%player%", uhcPlayer.getName()));
		}

		if (cfg.getRegenHeadDropOnPlayerDeath()) { playerDrops.add(UhcItems.createRegenHead(uhcPlayer)); }

		if (location != null && cfg.getEnableGoldenHeads()) {
			if (cfg.getPlaceHeadOnFence() && !gm.getScenarioManager().isActivated(Scenario.TIMEBOMB)) {
				// place head on fence
				Location loc = location.clone().add(1, 0, 0);
				loc.getBlock().setType(UniversalMaterial.OAK_FENCE.getType());
				loc.add(0, 1, 0);
				loc.getBlock().setType(UniversalMaterial.PLAYER_HEAD_BLOCK.getType());

				Skull skull = (Skull) loc.getBlock().getState();
				VersionUtils.getVersionUtils().setSkullOwner(skull, uhcPlayer);
				skull.setRotation(BlockFace.NORTH);
				skull.update();
			} else {
				playerDrops.add(UhcItems.createGoldenHeadPlayerSkull(uhcPlayer.getName(), uhcPlayer.getUuid()));
			}
		}

		if (location != null && cfg.getEnableExpDropOnDeath()) {
			UhcItems.spawnExtraXp(location, cfg.getExpDropOnDeath());
		}

		if (location != null) { playerDrops.forEach(item -> location.getWorld().dropItem(location, item)); }

		uhcPlayer.setState(PlayerState.DEAD);
		pm.strikeLightning(uhcPlayer);
		// pm.playSoundPlayerDeath();

		pm.checkIfRemainingPlayers();
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