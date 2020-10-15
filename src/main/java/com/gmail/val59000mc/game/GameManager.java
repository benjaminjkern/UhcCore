package com.gmail.val59000mc.game;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.commands.*;
import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.configuration.VaultManager;
import com.gmail.val59000mc.configuration.YamlFile;
import com.gmail.val59000mc.customitems.CraftsManager;
import com.gmail.val59000mc.customitems.KitsManager;
import com.gmail.val59000mc.events.UhcGameStateChangedEvent;
import com.gmail.val59000mc.events.UhcStartingEvent;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.listeners.*;
import com.gmail.val59000mc.maploader.MapLoader;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.TeamManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import com.gmail.val59000mc.schematics.DeathmatchArena;
import com.gmail.val59000mc.schematics.Lobby;
import com.gmail.val59000mc.schematics.UndergroundNether;
import com.gmail.val59000mc.scoreboard.ScoreboardManager;
import com.gmail.val59000mc.threads.*;
import com.gmail.val59000mc.utils.*;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import com.gmail.val59000mc.scenarios.DogNameGenerator;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameManager {

	// GameManager Instance
	private static GameManager gameManager;

	// Managers
	private final PlayersManager playerManager;
	private final TeamManager teamManager;
	private final ScoreboardManager scoreboardManager;
	private final MainConfiguration configuration;
	private final MapLoader mapLoader;
	private final UhcWorldBorder worldBorder;

	private ScenarioManager scenarioManager;

	private Lobby lobby;
	private DeathmatchArena arena;

	private GameState gameState;
	private boolean pvp;
	private boolean gameIsEnding;
	private int episodeNumber;
	private long remainingTime;
	private long elapsedTime;

	private DogNameGenerator dogNames;
	private int startPlayers;
	private Socket lobbySocket;
	private PrintWriter lobbyOutputStream;

	static {
		gameManager = null;
	}

	public GameManager() {
		gameManager = this;
		playerManager = new PlayersManager();
		teamManager = new TeamManager(playerManager);
		scoreboardManager = new ScoreboardManager();
		configuration = new MainConfiguration(this);

		mapLoader = new MapLoader();
		worldBorder = new UhcWorldBorder();

		dogNames = new DogNameGenerator();

		episodeNumber = 0;
		elapsedTime = 0;
	}

	public static GameManager getGameManager() { return gameManager; }

	public PlayersManager getPlayersManager() { return playerManager; }

	public TeamManager getTeamManager() { return teamManager; }

	public ScoreboardManager getScoreboardManager() { return scoreboardManager; }

	public ScenarioManager getScenarioManager() { return scenarioManager; }

	public MainConfiguration getConfiguration() { return configuration; }

	public UhcWorldBorder getWorldBorder() { return worldBorder; }

	public MapLoader getMapLoader() { return mapLoader; }

	public synchronized GameState getGameState() { return gameState; }

	public Lobby getLobby() { return lobby; }

	public DeathmatchArena getArena() { return arena; }

	public PrintWriter getLobbyOutputStream() { return lobbyOutputStream; }

	public void setLobbySocket(Socket s) {
		lobbySocket = s;
		try {
			lobbyOutputStream = new PrintWriter(lobbySocket.getOutputStream(), true);
		} catch (Exception e) {
			// not really sure what to do if this fails
		}
	}

	public boolean sendInfoToServer(String s, boolean reEstablish) {
		if (lobbyOutputStream != null) {
			lobbyOutputStream.println(s);
			if (!lobbyOutputStream.checkError()) return true;
		}

		if (!reEstablish) return false;

		try {
			setLobbySocket(new Socket("localhost", 58901));
			Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new ReceiveScoresThread());
		} catch (Exception e) {
			lobbySocket = null;
			lobbyOutputStream = null;
			return false;
		}

		Bukkit.getLogger().info("[UhcCore] Connected to lobby server.");

		// send all information when created, slightly redundant but whatever
		sendInfoToServer("SERVERNAME:" + getConfiguration().getServerName(), true);
		sendInfoToServer("MINSIZE:" + getConfiguration().getMinPlayersToStart(), true);
		sendInfoToServer("MAXSIZE:" + Bukkit.getMaxPlayers(), true);
		sendInfoToServer("GAMESTATE:" + getGameState().name(), true);
		sendInfoToServer("CURRENTSIZE:" + Bukkit.getOnlinePlayers().size(), true);

		return true;
	}

	public boolean getGameIsEnding() { return gameIsEnding; }

	public synchronized long getRemainingTime() { return remainingTime; }

	public synchronized long getElapsedTime() { return elapsedTime; }

	public int getEpisodeNumber() { return episodeNumber; }

	public void setEpisodeNumber(int episodeNumber) { this.episodeNumber = episodeNumber; }

	public long getTimeUntilNextEpisode() {
		return episodeNumber * configuration.getEpisodeMarkersDelay() - getElapsedTime();
	}

	public String getFormatedRemainingTime() { return TimeUtils.getFormattedTime(getRemainingTime()); }

	public synchronized void setRemainingTime(long time) { remainingTime = time; }

	public synchronized void setElapsedTime(long time) { elapsedTime = time; }

	public boolean getPvp() { return pvp; }

	public void setPvp(boolean state) { pvp = state; }

	public void setGameState(GameState gameState) {
		Validate.notNull(gameState);

		if (this.gameState == gameState) {
			return; // Don't change the game state when the same.
		}

		GameState oldGameState = this.gameState;
		this.gameState = gameState;

		sendInfoToServer("GAMESTATE:" + gameState.name(), false);

		// Call UhcGameStateChangedEvent
		Bukkit.getPluginManager().callEvent(new UhcGameStateChangedEvent(oldGameState, gameState));

		// Update MOTD
		switch (gameState) {
			case ENDED:
				setMotd(Lang.DISPLAY_MOTD_ENDED);
				break;
			case LOADING:
				setMotd(Lang.DISPLAY_MOTD_LOADING);
				break;
			case DEATHMATCH:
				setMotd(Lang.DISPLAY_MOTD_PLAYING);
				break;
			case PLAYING:
				setMotd(Lang.DISPLAY_MOTD_PLAYING);
				break;
			case STARTING:
				setMotd(Lang.DISPLAY_MOTD_STARTING);
				break;
			case WAITING:
				setMotd(Lang.DISPLAY_MOTD_WAITING);
				break;
			default:
				setMotd(Lang.DISPLAY_MOTD_ENDED);
				break;
		}
	}

	private void setMotd(String motd) {
		if (getConfiguration().getDisableMotd()) {
			return; // No motd support
		}

		if (motd == null) {
			return; // Failed to load lang.yml so motd is null.
		}

		try {
			Class craftServerClass = NMSUtils.getNMSClass("CraftServer");
			Object craftServer = craftServerClass.cast(Bukkit.getServer());
			Object dedicatedPlayerList = NMSUtils.getHandle(craftServer);
			Object dedicatedServer = NMSUtils.getServer(dedicatedPlayerList);

			Method setMotd = NMSUtils.getMethod(dedicatedServer.getClass(), "setMotd");
			setMotd.invoke(dedicatedServer, motd);
		} catch (ReflectiveOperationException | NullPointerException ex) {
			ex.printStackTrace();
		}
	}

	private GameState getOldGameState() {
		YamlFile storage;

		try {
			storage = FileUtils.saveResourceIfNotAvailable("storage.yml");
		} catch (InvalidConfigurationException ex) {
			ex.printStackTrace();
			return GameState.DEATHMATCH;
		}

		String gameStateName = (String) storage.get("gameState");
		if (gameStateName == null) {
			Bukkit.getLogger().warning("Last game state was null, starting with new map");
			return GameState.DEATHMATCH;
		}
		return GameState.valueOf(gameStateName);
	}

	// force new map
	public void loadNewGame() { loadNewGame(getOldGameState()); }

	public void loadNewGame(GameState oldGameState) {
		deleteOldPlayersFiles();
		loadConfig();
		setGameState(GameState.LOADING);

		registerListeners();

		if (configuration.getReplaceOceanBiomes()) { VersionUtils.getVersionUtils().replaceOceanBiomes(); }

		List<GameState> validGameStates = Arrays.asList(new GameState[] { GameState.WAITING, GameState.STARTING });

		if (configuration.getDebug() || validGameStates.contains(oldGameState)) {
			mapLoader.loadOldWorld(configuration.getOverworldUuid(), Environment.NORMAL);
			if (configuration.getEnableNether()) {
				mapLoader.loadOldWorld(configuration.getNetherUuid(), Environment.NETHER);
			}
			if (configuration.getEnableTheEnd()) {
				mapLoader.loadOldWorld(configuration.getTheEndUuid(), Environment.THE_END);
			}
		} else {
			mapLoader.deleteLastWorld(configuration.getOverworldUuid());
			mapLoader.deleteLastWorld(configuration.getNetherUuid());
			mapLoader.deleteLastWorld(configuration.getTheEndUuid());
			mapLoader.createNewWorld(Environment.NORMAL);
			if (configuration.getEnableNether()) { mapLoader.createNewWorld(Environment.NETHER); }
			if (configuration.getEnableTheEnd()) { mapLoader.createNewWorld(Environment.THE_END); }
		}

		if (configuration.getEnableBungeeSupport()) UhcCore.getPlugin().getServer().getMessenger()
				.registerOutgoingPluginChannel(UhcCore.getPlugin(), "BungeeCord");

		if (configuration.getEnablePregenerateWorld() && !configuration.getDebug())
			mapLoader.generateChunks(Environment.NORMAL);
		else startWaitingPlayers();
	}

	private void deleteOldPlayersFiles() {

		if (Bukkit.getServer().getWorlds().size() > 0) {
			// Deleting old players files
			File playerdata = new File(Bukkit.getServer().getWorlds().get(0).getName() + "/playerdata");
			if (playerdata.exists() && playerdata.isDirectory()) {
				for (File playerFile : playerdata.listFiles()) { playerFile.delete(); }
			}

			// Deleting old players stats
			File stats = new File(Bukkit.getServer().getWorlds().get(0).getName() + "/stats");
			if (stats.exists() && stats.isDirectory()) {
				for (File statFile : stats.listFiles()) { statFile.delete(); }
			}

			// Deleting old players advancements
			File advancements = new File(Bukkit.getServer().getWorlds().get(0).getName() + "/advancements");
			if (advancements.exists() && advancements.isDirectory()) {
				for (File advancementFile : advancements.listFiles()) { advancementFile.delete(); }
			}
		}

	}

	public void startWaitingPlayers() {
		loadWorlds();
		registerCommands();
		setGameState(GameState.WAITING);
		Bukkit.getLogger().info(Lang.DISPLAY_MESSAGE_PREFIX + " Players are now allowed to join");
		Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new PreStartThread(this), 0);
	}

	public void startGame() {
		setGameState(GameState.STARTING);

		if (configuration.getEnableDayNightCycle()) {
			World overworld = Bukkit.getWorld(configuration.getOverworldUuid());
			VersionUtils.getVersionUtils().setGameRuleValue(overworld, "doDaylightCycle", true);
			overworld.setTime(0);
		}

		// scenario voting
		if (configuration.getEnableScenarioVoting()) { scenarioManager.countVotes(); }

		// if (fillgamewithbots) {
		if (true) {

			startPlayers = Bukkit.getMaxPlayers();

			for (int i = Bukkit.getOnlinePlayers().size(); i < Bukkit.getMaxPlayers(); i++) {
				NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Bot" + i);

				SentinelTrait sentinel = new SentinelTrait();
				npc.addTrait(sentinel);

				new SentinelTargetLabel("monsters").addToList(sentinel.allTargets);
				new SentinelTargetLabel("players").addToList(sentinel.allTargets);
				new SentinelTargetLabel("npcs").addToList(sentinel.allTargets);

				sentinel.attackRate += (int) (21 * Math.random() - 10);
				sentinel.range = 100;

				npc.spawn(Bukkit.getOnlinePlayers().iterator().next().getLocation());
				npc.setProtected(false);

				Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(),
						() -> playerManager.playerJoinsTheGame((Player) npc.getEntity()), 1);
			}
		} else startPlayers = getPlayersManager().getAllPlayingPlayers().size();

		Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new UpdateBotsThread(), 20);

		Bukkit.getPluginManager().callEvent(new UhcStartingEvent());

		broadcastInfoMessage(Lang.GAME_STARTING);
		broadcastInfoMessage(Lang.GAME_PLEASE_WAIT_TELEPORTING);
		playerManager.randomTeleportTeams();
		gameIsEnding = false;
	}

	public void startWatchingEndOfGame() {
		setGameState(GameState.PLAYING);

		World overworld = Bukkit.getWorld(configuration.getOverworldUuid());
		VersionUtils.getVersionUtils().setGameRuleValue(overworld, "doMobSpawning", true);

		lobby.destroyBoundingBox();
		playerManager.startWatchPlayerPlayingThread();
		Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new ElapsedTimeThread());
		Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new EnablePVPThread(this));

		if (configuration.getEnableEpisodeMarkers()) {
			Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new EpisodeMarkersThread(this));
		}

		if (configuration.getEnableTimeLimit()) {
			Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new TimeBeforeEndThread(this));
		}

		if (configuration.getEnableDayNightCycle() && configuration.getTimeBeforePermanentDay() != -1) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(),
					new EnablePermanentDayThread(configuration), configuration.getTimeBeforePermanentDay() * 20);
		}

		if (configuration.getEnableFinalHeal()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new FinalHealThread(this, playerManager),
					configuration.getFinalHealDelay() * 20);
		}

		worldBorder.startBorderThread();
		Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new TimeEndGameThread(this, playerManager),
				20 * 60 * 30);

		Bukkit.getPluginManager().callEvent(new UhcStartedEvent());
		UhcCore.getPlugin().addGameToStatistics();
	}

	public void broadcastMessage(String message) {
		for (UhcPlayer player : playerManager.getPlayersList()) { player.sendMessage(message); }
	}

	public void broadcastInfoMessage(String message) { broadcastMessage(Lang.DISPLAY_MESSAGE_PREFIX + " " + message); }

	public DogNameGenerator getDogNameGenerator() { return dogNames; }

	public int getStartPlayers() { return startPlayers; }

	public void loadConfig() {
		new Lang();

		YamlFile cfg;
		YamlFile storage;

		try {
			cfg = FileUtils.saveResourceIfNotAvailable("config.yml");
			storage = FileUtils.saveResourceIfNotAvailable("storage.yml");
		} catch (InvalidConfigurationException ex) {
			ex.printStackTrace();
			return;
		}

		// Dependencies
		configuration.loadWorldEdit();
		configuration.loadVault();
		configuration.loadProtocolLib();

		// Config
		configuration.preLoad(cfg);
		configuration.load(cfg, storage);

		scenarioManager = new ScenarioManager();

		// ping lobby
		if (!configuration.getServerName().equals("test"))
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new LobbyPingThread(this), 20);

		// Load kits
		KitsManager.loadKits();

		// Load crafts
		CraftsManager.loadBannedCrafts();
		CraftsManager.loadCrafts();

		VaultManager.setupEconomy();

		if (configuration.getProtocolLibLoaded()) {
			try {
				ProtocolUtils.register();
			} catch (Exception ex) {
				configuration.setProtocolLibLoaded(false);
				Bukkit.getLogger().severe("[UhcCore] Failed to load ProtocolLib, are you using the right version?");
				ex.printStackTrace();
			}
		}
	}

	private void registerListeners() {
		// Registers Listeners
		List<Listener> listeners = new ArrayList<>();
		listeners.add(new PlayerConnectionListener(this, playerManager));
		listeners.add(new PlayerChatListener(playerManager, configuration));
		listeners.add(new PlayerDamageListener(this));
		listeners.add(new ItemsListener());
		listeners.add(new TeleportListener());
		listeners.add(new PlayerDeathListener());
		listeners.add(new EntityDeathListener(playerManager, configuration));
		listeners.add(new CraftListener());
		listeners.add(new PingListener());
		listeners.add(new BlockListener(configuration));
		listeners.add(new WorldListener());
		listeners.add(new PlayerMovementListener(playerManager));
		listeners.add(new EntityDamageListener(this));
		listeners.add(new ArmorListener());
		for (Listener listener : listeners) {
			Bukkit.getServer().getPluginManager().registerEvents(listener, UhcCore.getPlugin());
		}
	}

	private void loadWorlds() {
		World overworld = Bukkit.getWorld(configuration.getOverworldUuid());
		overworld.save();
		if (!configuration.getEnableHealthRegen()) {
			VersionUtils.getVersionUtils().setGameRuleValue(overworld, "naturalRegeneration", false);
		}
		if (!configuration.getAnnounceAdvancements() && UhcCore.getVersion() >= 12) {
			VersionUtils.getVersionUtils().setGameRuleValue(overworld, "announceAdvancements", false);
		}
		VersionUtils.getVersionUtils().setGameRuleValue(overworld, "doDaylightCycle", false);
		VersionUtils.getVersionUtils().setGameRuleValue(overworld, "commandBlockOutput", false);
		VersionUtils.getVersionUtils().setGameRuleValue(overworld, "logAdminCommands", false);
		VersionUtils.getVersionUtils().setGameRuleValue(overworld, "sendCommandFeedback", false);
		VersionUtils.getVersionUtils().setGameRuleValue(overworld, "doMobSpawning", false);
		overworld.setTime(6000);
		overworld.setDifficulty(configuration.getGameDifficulty());
		overworld.setWeatherDuration(999999999);

		if (configuration.getEnableNether()) {
			World nether = Bukkit.getWorld(configuration.getNetherUuid());
			nether.save();
			if (!configuration.getEnableHealthRegen()) {
				VersionUtils.getVersionUtils().setGameRuleValue(nether, "naturalRegeneration", false);
			}
			if (!configuration.getAnnounceAdvancements() && UhcCore.getVersion() >= 12) {
				VersionUtils.getVersionUtils().setGameRuleValue(overworld, "announceAdvancements", false);
			}
			VersionUtils.getVersionUtils().setGameRuleValue(nether, "commandBlockOutput", false);
			VersionUtils.getVersionUtils().setGameRuleValue(nether, "logAdminCommands", false);
			VersionUtils.getVersionUtils().setGameRuleValue(nether, "sendCommandFeedback", false);
			nether.setDifficulty(configuration.getGameDifficulty());
		}

		if (configuration.getEnableTheEnd()) {
			World theEnd = Bukkit.getWorld(configuration.getTheEndUuid());
			theEnd.save();
			if (!configuration.getEnableHealthRegen()) {
				VersionUtils.getVersionUtils().setGameRuleValue(theEnd, "naturalRegeneration", false);
			}
			if (!configuration.getAnnounceAdvancements() && UhcCore.getVersion() >= 12) {
				VersionUtils.getVersionUtils().setGameRuleValue(overworld, "announceAdvancements", false);
			}
			VersionUtils.getVersionUtils().setGameRuleValue(theEnd, "commandBlockOutput", false);
			VersionUtils.getVersionUtils().setGameRuleValue(theEnd, "logAdminCommands", false);
			VersionUtils.getVersionUtils().setGameRuleValue(theEnd, "sendCommandFeedback", false);
			theEnd.setDifficulty(configuration.getGameDifficulty());
		}

		lobby = new Lobby(new Location(overworld, 0.5, 200, 0.5), Material.BARRIER);
		lobby.build();
		lobby.loadLobbyChunks();

		arena = new DeathmatchArena(new Location(overworld, 10000, configuration.getArenaPasteAtY(), 10000));
		arena.build();
		arena.loadChunks();

		UndergroundNether undergoundNether = new UndergroundNether(this);
		undergoundNether.build();

		worldBorder.setUpBukkitBorder(configuration);

		setPvp(false);
	}

	private void registerCommands() {
		// Registers CommandExecutor
		registerCommand("uhccore", new UhcCommandExecutor(this));
		registerCommand("chat", new ChatCommandExecutor(playerManager));
		registerCommand("teleport", new TeleportCommandExecutor(this));
		registerCommand("start", new StartCommandExecutor());
		registerCommand("scenarios", new ScenarioCommandExecutor(scenarioManager));
		registerCommand("teaminventory", new TeamInventoryCommandExecutor(playerManager, scenarioManager));
		registerCommand("hub", new HubCommandExecutor(this));
		registerCommand("iteminfo", new ItemInfoCommandExecutor());
		registerCommand("revive", new ReviveCommandExecutor(this));
		registerCommand("seed", new SeedCommandExecutor(configuration));
		registerCommand("crafts", new CustomCraftsCommandExecutor());
		registerCommand("top", new TopCommandExecutor(playerManager));
		registerCommand("spectate", new SpectateCommandExecutor(this));
		registerCommand("upload", new UploadCommandExecutor());
		registerCommand("deathmatch", new DeathmatchCommandExecutor(this));
		registerCommand("team", new TeamCommandExecutor(this));
		registerCommand("rating", new RatingCommandExecutor());
		registerCommand("ping", new PingCommandExecutor(this));
		registerCommand("discord", new DiscordCommand());
	}

	private void registerCommand(String commandName, CommandExecutor executor) {
		PluginCommand command = UhcCore.getPlugin().getCommand(commandName);
		if (command == null) {
			Bukkit.getLogger().warning("[UhcCore] Failed to register " + commandName + " command!");
			return;
		}

		command.setExecutor(executor);
	}

	public void endGame() {
		if (gameState.equals(GameState.PLAYING) || gameState.equals(GameState.DEATHMATCH)) {
			setGameState(GameState.ENDED);
			playerManager.getAllPlayingPlayers()
					.forEach(uhcPlayer -> { playerManager.getScoreKeeper().envWin(uhcPlayer); });
			pvp = false;
			gameIsEnding = true;
			broadcastInfoMessage(Lang.GAME_FINISHED);
			broadcastInfoMessage("You can use \u00a7d/lobby \u00a7fto go back to the lobby.");
			playerManager.playSoundToAll(UniversalSound.ENDERDRAGON_GROWL, 1, 2);
			playerManager.setAllPlayersEndGame();
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new StopRestartThread(), 20);
		}
	}

	public void startDeathmatch() {
		// DeathMatch can only be stated while GameState = Playing
		if (gameState != GameState.PLAYING) { return; }

		setGameState(GameState.DEATHMATCH);
		pvp = false;
		broadcastInfoMessage(Lang.GAME_START_DEATHMATCH);
		playerManager.playSoundToAll(UniversalSound.ENDERDRAGON_GROWL);

		// DeathMatch arena DeathMatch
		if (arena.isUsed()) {
			Location arenaLocation = arena.getLoc();

			// Set big border size to avoid hurting players
			worldBorder.setBukkitWorldBorderSize(arenaLocation.getWorld(), arenaLocation.getBlockX(),
					arenaLocation.getBlockZ(), 50000);

			// Teleport players
			playerManager.setAllPlayersStartDeathmatch();

			// Shrink border to arena size
			worldBorder.setBukkitWorldBorderSize(arenaLocation.getWorld(), arenaLocation.getBlockX(),
					arenaLocation.getBlockZ(), arena.getMaxSize());

			// Start Enable pvp thread
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new StartDeathmatchThread(this, false),
					20);
		}
		// 0 0 DeathMach
		else {
			Location deathmatchLocation = lobby.getLoc();

			// Set big border size to avoid hurting players
			worldBorder.setBukkitWorldBorderSize(deathmatchLocation.getWorld(), deathmatchLocation.getBlockX(),
					deathmatchLocation.getBlockZ(), 50000);

			// Teleport players
			playerManager.setAllPlayersStartDeathmatch();

			// Shrink border to arena size
			worldBorder.setBukkitWorldBorderSize(deathmatchLocation.getWorld(), deathmatchLocation.getBlockX(),
					deathmatchLocation.getBlockZ(), configuration.getDeathmatchStartSize() * 2);

			// Start Enable pvp thread
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new StartDeathmatchThread(this, true),
					20);
		}
	}

	public void startEndGameThread() {
		if (!gameIsEnding && (gameState.equals(GameState.DEATHMATCH) || gameState.equals(GameState.PLAYING))) {
			gameIsEnding = true;
			EndThread.start();
		}
	}

	public void stopEndGameThread() {
		if (gameIsEnding && (gameState.equals(GameState.DEATHMATCH) || gameState.equals(GameState.PLAYING))) {
			gameIsEnding = false;
			EndThread.stop();
		}
	}

	public Socket getLobbySocket() { return lobbySocket; }

}