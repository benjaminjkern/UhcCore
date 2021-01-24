package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.configuration.MobLootConfiguration;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class EntityDeathListener implements Listener {

	private final PlayersManager playersManager;

	// Gold drops
	private final int min;
	private final int max;
	private final int chance;
	private final List<EntityType> affectedMobs;
	private final boolean allowGhastTearDrop;
	private final boolean enableGoldDrops;

	private static Set<EntityType> animals;

	static {
		animals = new HashSet<>();
		animals.add(EntityType.BAT);
		animals.add(EntityType.BEE);
		animals.add(EntityType.CAT);
		animals.add(EntityType.CHICKEN);
		animals.add(EntityType.COD);
		animals.add(EntityType.COW);
		animals.add(EntityType.DOLPHIN);
		animals.add(EntityType.DONKEY);
		animals.add(EntityType.FOX);
		animals.add(EntityType.HOGLIN);
		animals.add(EntityType.HORSE);
		animals.add(EntityType.LLAMA);
		animals.add(EntityType.MULE);
		animals.add(EntityType.MUSHROOM_COW);
		animals.add(EntityType.OCELOT);
		animals.add(EntityType.PANDA);
		animals.add(EntityType.PARROT);
		animals.add(EntityType.PIG);
		animals.add(EntityType.POLAR_BEAR);
		animals.add(EntityType.PUFFERFISH);
		animals.add(EntityType.RABBIT);
		animals.add(EntityType.SALMON);
		animals.add(EntityType.SHEEP);
		animals.add(EntityType.SQUID);
		animals.add(EntityType.STRIDER);
		animals.add(EntityType.TRADER_LLAMA);
		animals.add(EntityType.TROPICAL_FISH);
		animals.add(EntityType.TURTLE);
		animals.add(EntityType.VILLAGER);
		animals.add(EntityType.WANDERING_TRADER);
		animals.add(EntityType.WOLF);
	}

	// Fast mode mob loots
	private final Map<EntityType, MobLootConfiguration> mobLoots;

	public EntityDeathListener(PlayersManager playersManager, MainConfiguration configuration) {
		this.playersManager = playersManager;
		min = configuration.getMinGoldDrops();
		max = configuration.getMaxGoldDrops();
		chance = configuration.getGoldDropPercentage();
		affectedMobs = configuration.getAffectedGoldDropsMobs();
		allowGhastTearDrop = configuration.getAllowGhastTearsDrops();
		enableGoldDrops = configuration.getEnableGoldDrops();
		mobLoots = configuration.getEnableMobLoots() ? configuration.getMobLoots() : new HashMap<>();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		handleMobLoot(event);
		handleGoldDrop(event);
		handleGhastTearDrop(event);
		if (handleOfflineZombieDeath(event)) return;
		if (!(event.getEntity().getKiller() instanceof Player)) return;
		GameManager gm = GameManager.getGameManager();

		if (event.getEntity().hasMetadata("NPC")) {
			HashMap<Integer, ItemStack> a = ((Player) event.getEntity()).getInventory()
					.addItem(event.getDrops().stream().toArray(ItemStack[]::new));

			event.getDrops().clear();
			event.getDrops().addAll(a.values());
		}

		String playerName = gm.getPlayersManager().getUhcPlayer(event.getEntity().getKiller()).getName();
		if (event.getEntity() instanceof Mob) {
			if (animals.contains(event.getEntityType()))
				gm.sendInfoToServer("KILL:" + playerName + ":YEUH-ANIMAL", false);
			else gm.sendInfoToServer("KILL:" + playerName + ":YEUH-MONSTER", false);
		}
	}

	private void handleMobLoot(EntityDeathEvent event) {
		EntityType entity = event.getEntityType();
		if (mobLoots.containsKey(entity)) {
			MobLootConfiguration lootConfig = mobLoots.get(entity);
			event.getDrops().clear();
			event.getDrops().add(lootConfig.getLoot().clone());
			event.setDroppedExp(lootConfig.getAddXp());
			UhcItems.spawnExtraXp(event.getEntity().getLocation(), lootConfig.getAddXp());
		}
	}

	private void handleGoldDrop(EntityDeathEvent event) {
		if (enableGoldDrops && affectedMobs.contains(event.getEntityType())) {
			Random r = new Random();
			if (r.nextInt(100) < chance) {
				int drop;
				try {
					drop = min + r.nextInt(1 + max - min);
				} catch (IllegalArgumentException e) {
					drop = 0;
				}
				if (drop > 0) {
					ItemStack gold = new ItemStack(Material.GOLD_INGOT, drop);
					event.getDrops().add(gold);
				}
			}
		}
	}

	private void handleGhastTearDrop(EntityDeathEvent event) {
		if (event.getEntityType().equals(EntityType.GHAST) && !allowGhastTearDrop) {
			for (int i = event.getDrops().size() - 1; i >= 0; i--) {
				if (event.getDrops().get(i).getType().equals(Material.GHAST_TEAR)) { event.getDrops().remove(i); }
			}
		}
	}

	private boolean handleOfflineZombieDeath(EntityDeathEvent event) {
		if (event.getEntityType() != EntityType.ZOMBIE) { return false; }

		Zombie zombie = (Zombie) event.getEntity();

		if (zombie.getCustomName() == null) { return false; }

		UhcPlayer uhcPlayer = null;
		for (UhcPlayer player : playersManager.getPlayersList()) {
			if (player.getOfflineZombie() != null && player.getOfflineZombie().equals(zombie)) {
				// found player
				uhcPlayer = player;
				break;
			}
		}

		if (uhcPlayer == null) { return false; }

		event.getDrops().clear();
		uhcPlayer.setOfflineZombie(null);
		playersManager.killOfflineUhcPlayer(uhcPlayer, zombie.getLocation(), new HashSet<>(uhcPlayer.getStoredItems()),
				zombie.getKiller());
		return true;
	}

}