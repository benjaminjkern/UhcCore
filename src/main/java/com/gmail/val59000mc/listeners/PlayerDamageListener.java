package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.exceptions.UhcPlayerDoesntExistException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class PlayerDamageListener implements Listener {

	private final GameManager gameManager;
	private final boolean friendlyFire;

	public PlayerDamageListener(GameManager gameManager) {
		this.gameManager = gameManager;
		friendlyFire = gameManager.getConfiguration().getEnableFriendlyFire();
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		handlePvpAndFriendlyFire(event);
		handleLightningStrike(event);
		handleArrow(event);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDamage(EntityDamageEvent event) { handleAnyDamage(event); }

	///////////////////////
	// EntityDamageEvent //
	///////////////////////

	private void handleAnyDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			PlayersManager pm = gameManager.getPlayersManager();
			UhcPlayer uhcPlayer = pm.getUhcPlayer(player);

			GameManager.getGameManager().getListInventoryHandler().updatePlayer(uhcPlayer);

			PlayerState uhcPlayerState = uhcPlayer.getState();
			if (uhcPlayerState.equals(PlayerState.WAITING) || uhcPlayerState.equals(PlayerState.DEAD)
					|| uhcPlayer.isFrozen()) {
				event.setCancelled(true);
				return;
			}
		}
	}

	///////////////////////////////
	// EntityDamageByEntityEvent //
	///////////////////////////////

	private void handlePvpAndFriendlyFire(EntityDamageByEntityEvent event) {

		PlayersManager pm = gameManager.getPlayersManager();

		if (event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			UhcPlayer uhcDamager = pm.getUhcPlayer(damager);

			if (!uhcDamager.getState().equals(PlayerState.PLAYING)) {
				event.setCancelled(true);
				return;
			}

			if (event.getEntity() instanceof Player) {
				if (!gameManager.getPvp()) {
					event.setCancelled(true);
					return;
				}

				Player damaged = (Player) event.getEntity();
				UhcPlayer uhcDamaged = pm.getUhcPlayer(damaged);

				if (uhcDamager.getName().equals("YEUH-BOT") && uhcDamager.getState().equals(PlayerState.PLAYING)
						&& uhcDamager.isInTeamWith(uhcDamaged)) {
					NPC npc = CitizensAPI.getNPCRegistry().getNPC(damager);
					SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
					if (!damaged.hasMetadata("NPC")) sentinel.setGuarding(damaged.getUniqueId());
					else new SentinelTargetLabel("npc:" + damaged.getName()).addToList(sentinel.allIgnores);
					event.setCancelled(true);
				}

				if (!friendlyFire && uhcDamager.getState().equals(PlayerState.PLAYING)
						&& uhcDamager.isInTeamWith(uhcDamaged)) {
					damager.sendMessage(Lang.PLAYERS_FF_OFF);
					event.setCancelled(true);
				}
			}
		} else {

		}
	}

	private void handleLightningStrike(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof LightningStrike && event.getEntity() instanceof Player) {
			event.setCancelled(true);
		}
	}

	private void handleArrow(EntityDamageByEntityEvent event) {

		PlayersManager pm = gameManager.getPlayersManager();

		if (event.getEntity() instanceof Player && event.getDamager() instanceof Projectile) {
			Projectile arrow = (Projectile) event.getDamager();
			final Player shot = (Player) event.getEntity();
			if (arrow.getShooter() instanceof Player) {

				if (!gameManager.getPvp()) {
					event.setCancelled(true);
					return;
				}

				UhcPlayer uhcDamager = pm.getUhcPlayer((Player) arrow.getShooter());
				UhcPlayer uhcDamaged = pm.getUhcPlayer(shot);

				if (uhcDamager.getName().equals("YEUH-BOT") && uhcDamager.getState().equals(PlayerState.PLAYING)
						&& uhcDamager.isInTeamWith(uhcDamaged)) {
					NPC npc = CitizensAPI.getNPCRegistry().getNPC((Player) arrow.getShooter());
					SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
					if (sentinel.getGuardingEntity() == null || sentinel.getGuardingEntity().hasMetadata("NPC"))
						sentinel.setGuarding(shot.getUniqueId());
					event.setCancelled(true);
				}

				if (!friendlyFire && uhcDamager.getState().equals(PlayerState.PLAYING)
						&& uhcDamager.isInTeamWith(uhcDamaged)) {
					uhcDamager.sendMessage(Lang.PLAYERS_FF_OFF);
					event.setCancelled(true);
				}
			}
		}
	}

}