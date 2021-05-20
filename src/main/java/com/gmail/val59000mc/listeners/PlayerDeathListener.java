package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.events.UhcPlayerDeathEvent;
import com.gmail.val59000mc.events.UhcPlayerKillEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.CitizensAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PlayerDeathListener implements Listener {

	private static GameManager gm = GameManager.getGameManager();
	private static PlayersManager pm = gm.getPlayersManager();
	private static MainConfiguration cfg = gm.getConfiguration();

	public static boolean dropItems;
	public static boolean publicAnnounceDeaths;
	public static boolean privateAnnounceDeaths;
	public static boolean autoRespawn;

	public static boolean trackKills;
	public static boolean trackNaturalDeaths;

	public static double keepInventory;

	private static List<Sound> hurtSounds;
	static {
		hurtSounds = new ArrayList<>(Arrays.asList(Sound.values()));
		hurtSounds.removeIf(sound -> !sound.name().contains("HURT"));
	}

	public PlayerDeathListener() {
		dropItems = true;
		publicAnnounceDeaths = true;
		privateAnnounceDeaths = false;
		autoRespawn = false;

		trackKills = true;
		trackNaturalDeaths = true;

		keepInventory = 0;
	}

	public static Sound getRandomHurtSound() {
		return hurtSounds.get((int) (Math.random() * hurtSounds.size()));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player killed = event.getEntity();
		UhcPlayer uhcKilled = pm.getUhcPlayer(killed);

		if (gm.getGameState() == GameState.WAITING || gm.getGameState() == GameState.STARTING) {
			if (!killed.hasMetadata("NPC"))
				pm.sendPlayerToBungeeServer(killed);
			return;
		}
		// Bukkit.getLogger().info(player.getLocation() + "");

		if (uhcKilled.getState() != PlayerState.PLAYING) {
			Bukkit.getLogger().warning("[UhcCore] " + killed.getName() + " died while already in 'DEAD' mode!");
			return;
		}

		Player killer = killed.getKiller();
		if (killer != null)
			handleKill(killed, killer, event.getDeathMessage() + "");
		else {
			handleNaturalDeath(killed, event.getDeathMessage() + "", "Death");
		}

		event.setDeathMessage(null);
	}

	public static Entity getRealDamager(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (damager instanceof Trident)
			return (Entity) ((Trident) damager).getShooter();
		if (damager instanceof Projectile)
			return (Entity) ((Projectile) damager).getShooter();
		if (damager instanceof TNTPrimed)
			return ((TNTPrimed) damager).getSource();
		return damager;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		if (!autoRespawn || !(event.getEntity() instanceof Player) || event.isCancelled())
			return;

		Entity damager = getRealDamager(event);
		if (!(event.getEntity() instanceof Player))
			return;

		Player killed = (Player) event.getEntity();

		// use totem
		if (killed.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
				|| killed.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING)
			return;

		if (event.getDamage() < killed.getHealth())
			return;

		event.setCancelled(true);

		if (damager instanceof Player) {
			Player killer = (Player) damager;
			handleKill(killed, killer, killed.getName() + " was slain by " + killer.getName());
		} else {
			handleNaturalDeath(killed, killed.getName() + " was slain by " + damager.getName(), damager.getName());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeath(EntityDamageEvent event) {
		if (!autoRespawn || !(event.getEntity() instanceof Player) || event.isCancelled())
			return;
		if (gm.getGameState() != GameState.PLAYING && gm.getGameState() != GameState.DEATHMATCH)
			return;

		Player killed = (Player) event.getEntity();

		// use totem
		if (killed.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
				|| killed.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING)
			return;

		if (event.getDamage() < killed.getHealth())
			return;

		event.setCancelled(true);

		EntityDamageEvent lastCause = killed.getLastDamageCause();
		if (lastCause instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) lastCause;
			if (getRealDamager(ev) instanceof Player) {
				onPlayerDamage(ev);
				return;
			}
		}

		String cause = event.getCause().toString().replace("_", " ");
		handleNaturalDeath(killed, killed.getName() + " died",
				cause.substring(0, 1).toUpperCase() + cause.substring(1).toLowerCase());
	}

	public static String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
	}

	public static void handleKill(Player killed, Player killer, String deathMessage) {
		UhcPlayer uhcKiller = pm.getUhcPlayer(killer);
		UhcPlayer uhcKilled = pm.getUhcPlayer(killed);

		if (uhcKiller.getTeam() == uhcKilled.getTeam()) {
			// ignore for now
			return;
		}

		deathMessage = replaceLast(deathMessage, killer.getName(), uhcKiller.getDisplayName());

		if (privateAnnounceDeaths) {
			killer.sendMessage("You slayed " + uhcKilled.getDisplayName());
			killed.sendMessage("You were slain by " + uhcKiller.getDisplayName());
		}

		uhcKiller.kills++;

		if (trackKills)
			gm.sendInfoToServer("KILL:" + uhcKiller.getName() + ":" + uhcKilled.getName(), false);

		List<ItemStack> drops = getDrops(killed);

		if (killer.hasMetadata("NPC")) {
			// player is bot, immediately absorb experience and as much of the drops as
			// possible
			killer.setTotalExperience(
					killer.getTotalExperience() + killed.getTotalExperience() + cfg.getExpDropOnDeath());

			HashMap<Integer, ItemStack> leftover = killer.getInventory()
					.addItem(drops.stream().toArray(ItemStack[]::new));
			drops.clear();
			drops.addAll(leftover.values());
		} else {
			// player is not bot, drop experience normally and all drops
			UhcItems.spawnExtraXp(killed.getLocation(), cfg.getExpDropOnDeath());
		}

		// Call Bukkit event
		Bukkit.getServer().getPluginManager().callEvent(new UhcPlayerKillEvent(uhcKilled, uhcKiller));

		handleDeath(killed, deathMessage, drops);
	};

	public static List<ItemStack> getDrops(Player killed) {
		List<ItemStack> drops = new ArrayList<>();

		if (!dropItems)
			return drops;

		for (int i = 0; i <= 40; i++) {
			ItemStack item = killed.getInventory().getItem(i);
			if (item == null)
				continue;
			if (item.containsEnchantment(Enchantment.VANISHING_CURSE)) {
				killed.getInventory().setItem(i, null);
				continue;
			}
			if (!autoRespawn || Math.random() >= keepInventory) {
				drops.add(item);
				killed.getInventory().setItem(i, null);
			}
		}

		return drops;
	}

	public static void handleNaturalDeath(Player killed, String deathMessage, String deathCause) {
		UhcPlayer uhcKilled = pm.getUhcPlayer(killed);
		if (trackNaturalDeaths) {
			if (autoRespawn)
				gm.sendInfoToServer("DEATH:" + uhcKilled.getName() + ":donttakemeoffthelist", false);
			else
				gm.sendInfoToServer("DEATH:" + uhcKilled.getName(), false);
		}

		if (privateAnnounceDeaths)
			killed.sendMessage("You were killed by \u00a7d" + deathCause);

		UhcItems.spawnExtraXp(killed.getLocation(), killed.getTotalExperience());
		handleDeath(killed, deathMessage, getDrops(killed));
	}

	public static void handleDeath(Player killed, String deathMessage, List<ItemStack> drops) {
		UhcPlayer uhcKilled = pm.getUhcPlayer(killed);

		// set bots to stop guarding a player so that they don't teleport to them
		for (UhcPlayer teamMate : uhcKilled.getTeam().getMembers()) {
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

		uhcKilled.deaths++;

		killed.setFireTicks(0);
		killed.setArrowsInBody(0);
		killed.setHealth(killed.getMaxHealth());
		killed.setFoodLevel(20);
		killed.setLevel(0);
		killed.setExp(0);
		killed.getActivePotionEffects().forEach(effect -> killed.removePotionEffect(effect.getType()));
		PlayerExpListener.lastLevel.remove(uhcKilled);

		deathMessage = deathMessage.replaceFirst(killed.getName(), uhcKilled.getDisplayName());

		killed.setCanPickupItems(false);
		drops.forEach(item -> killed.getWorld().dropItemNaturally(killed.getEyeLocation(), item));
		killed.setCanPickupItems(true);

		if (!autoRespawn || (!uhcKilled.isOnline() && !uhcKilled.getName().equals("YEUH-BOT"))) {
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), () -> pm.setPlayerSpectateAtLobby(uhcKilled, false),
					1);
		}

		if (publicAnnounceDeaths) {
			if (!GameManager.getGameManager().getWorldBorder().isWithinBorder(killed.getLocation())) {
				deathMessage = deathMessage.replaceFirst("suffocated in a wall", "got stuck behind the wall");
			}
			gm.broadcastInfoMessage(deathMessage);
			// pm.playSoundPlayerDeath();
			Bukkit.getLogger().info(UhcCore.PREFIX + "\u00a7f" + deathMessage);
			pm.strikeLightning(uhcKilled);
		}

		Bukkit.getServer().getPluginManager().callEvent(new UhcPlayerDeathEvent(uhcKilled, drops));

		GameManager.getGameManager().getListInventoryHandler().updatePlayer(uhcKilled);

		pm.checkIfRemainingPlayers();
	}

}