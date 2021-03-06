package com.gmail.val59000mc.listeners;

import java.util.Arrays;
import java.util.Iterator;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.customitems.*;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.exceptions.UhcTeamException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import com.gmail.val59000mc.utils.UniversalMaterial;
// import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemsListener implements Listener {

	@EventHandler
	public void onSwitchToOffHand(PlayerSwapHandItemsEvent event) {
		Player player = event.getPlayer();
		GameManager gm = GameManager.getGameManager();
		UhcPlayer uhcPlayer = gm.getPlayersManager().getUhcPlayer(player);
		if (uhcPlayer.getState() != PlayerState.PLAYING) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRightClickItem(PlayerInteractEvent event) {

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_AIR
				&& event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;

		Player player = event.getPlayer();
		GameManager gm = GameManager.getGameManager();
		UhcPlayer uhcPlayer = gm.getPlayersManager().getUhcPlayer(player);
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (GameItem.isGameItem(hand)) {
			if (uhcPlayer.getState() == PlayerState.PLAYING) hand.setAmount(0);
			event.setCancelled(true);
			GameItem gameItem = GameItem.getGameItem(hand);
			handleGameItemInteract(gameItem, player, uhcPlayer, hand);
			return;
		}

		if (hand.hasItemMeta()) {
			// Clicked scenario
			Scenario scenario = Scenario.getScenario(hand.getItemMeta().getDisplayName());

			// Clicked item is not a scenario item
			if (scenario == null) return;

			event.setCancelled(true);

			// Send scenario info
			player.sendMessage(Lang.SCENARIO_GLOBAL_DESCRIPTION_HEADER.replace("%scenario%", scenario.getName()));
			scenario.getDescription().forEach(s -> player.sendMessage(Lang.SCENARIO_GLOBAL_DESCRIPTION_PREFIX + s));
		}

		if ((gm.getGameState().equals(GameState.PLAYING) || gm.getGameState().equals(GameState.DEATHMATCH))
				&& UhcItems.isRegenHeadItem(hand) && uhcPlayer.getState().equals(PlayerState.PLAYING)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			event.setCancelled(true);
			uhcPlayer.getTeam().regenTeam(gm.getConfiguration().getEnableDoubleRegenHead());
			player.getInventory().remove(hand);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClickInInventory(InventoryClickEvent event) {
		handleScenarioInventory(event);

		ItemStack item = event.getCurrentItem();
		GameManager gm = GameManager.getGameManager();
		Player player = (Player) event.getWhoClicked();
		UhcPlayer uhcPlayer = gm.getPlayersManager().getUhcPlayer(player);

		// Stop players from moving game items in their inventory.
		// Above item == null check as item is null on hotbar swap.
		if (gm.getGameState() == GameState.WAITING && event.getAction() == InventoryAction.HOTBAR_SWAP) {
			event.setCancelled(true);
		}

		// Only handle clicked items.
		if (item == null) { return; }

		if (uhcPlayer.getState() == PlayerState.DEAD) event.setCancelled(true);

		// Listen for GameItems
		if (gm.getGameState() == GameState.WAITING) {
			if (GameItem.isGameItem(item)) {
				event.setCancelled(true);
				handleGameItemInteract(GameItem.getGameItem(item), player, uhcPlayer, item);
			}
		}

		if (event.getView().getTitle().equals(Lang.TEAM_INVENTORY_INVITE_PLAYER)) {
			if (item.getType() != UniversalMaterial.PLAYER_HEAD.getType() || !item.hasItemMeta()) { return; }

			event.setCancelled(true);
			player.closeInventory();

			String playerName = item.getItemMeta().getDisplayName().replace(ChatColor.GREEN.toString(), "");
			player.performCommand("team invite " + playerName);
		}

		if (event.getView().getTitle().equals(Lang.TEAM_INVENTORY_TEAM_VIEW)) {
			if (item.getType() == UniversalMaterial.PLAYER_HEAD.getType() && item.hasItemMeta()) {
				event.setCancelled(true);
			}
		}

		// Click on a player head to join a team
		if (event.getView().getTitle().equals(Lang.ITEMS_KIT_INVENTORY)) {
			if (KitsManager.isKitItem(item)) {
				event.setCancelled(true);
				Kit kit = KitsManager.getKitByName(item.getItemMeta().getDisplayName());
				if (kit.canBeUsedBy(player, gm.getConfiguration())) {
					uhcPlayer.setKit(kit);
					uhcPlayer.sendMessage(Lang.ITEMS_KIT_SELECTED.replace("%kit%", kit.getName()));
				} else {
					uhcPlayer.sendMessage(Lang.ITEMS_KIT_NO_PERMISSION);
				}
				player.closeInventory();
			}
		}

		if (UhcItems.isTeamSkullItem(item)) {
			event.setCancelled(true);

			UhcTeam team = gm.getTeamManager().getTeamByName(item.getItemMeta().getDisplayName());

			// Click on a player head to reply to invite
			if (event.getView().getTitle().equals(Lang.TEAM_INVENTORY_INVITES)) {
				if (team == null) {
					player.sendMessage(Lang.TEAM_MESSAGE_NO_LONGER_EXISTS);
				} else {
					UhcItems.openTeamReplyInviteInventory(player, team);
				}
			}
			// Open team view inventory
			else {
				if (team == null) {
					player.sendMessage(Lang.TEAM_MESSAGE_NO_LONGER_EXISTS);
				} else {
					UhcItems.openTeamViewInventory(player, team);
				}
			}
		}

		if (event.getView().getTitle().equals(Lang.TEAM_INVENTORY_COLOR)) {
			event.setCancelled(true);

			if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
				String selectedColor = item.getItemMeta().getLore().get(0).replace(ChatColor.RESET.toString(), "");
				player.closeInventory();

				// check if already used by this team
				if (uhcPlayer.getTeam().getColor().contains(selectedColor)) {
					uhcPlayer.sendMessage(Lang.TEAM_MESSAGE_COLOR_ALREADY_SELECTED);
					return;
				}

				// check if still available
				String newPrefix = gm.getTeamManager().getTeamPrefix(selectedColor);
				if (newPrefix == null) {
					uhcPlayer.sendMessage(Lang.TEAM_MESSAGE_COLOR_UNAVAILABLE);
					return;
				}

				// assign color and update color on tab
				uhcPlayer.getTeam().setPrefix(newPrefix);
				for (UhcPlayer teamMember : uhcPlayer.getTeam().getMembers()) {
					gm.getScoreboardManager().updatePlayerTab(teamMember);
				}

				uhcPlayer.sendMessage(Lang.TEAM_MESSAGE_COLOR_CHANGED);
				return;
			}
		}

		if (event.getView().getTitle().equals(Lang.ITEMS_CRAFT_BOOK_INVENTORY)) {
			event.setCancelled(true);

			if (CraftsManager.isCraftItem(item)) {
				player.closeInventory();
				Craft craft = CraftsManager.getCraftByDisplayName(item.getItemMeta().getDisplayName());
				if (!gm.getConfiguration().getEnableCraftsPermissions()
						|| (gm.getConfiguration().getEnableCraftsPermissions()
								&& player.hasPermission("uhc-core.craft." + craft.getName()))) {
					CraftsManager.openCraftInventory(player, craft);
				} else {
					player.sendMessage(Lang.ITEMS_CRAFT_NO_PERMISSION.replace("%craft%", craft.getName()));
				}
			}

			if (CraftsManager.isCraftBookBackItem(item)) {
				event.setCancelled(true);
				player.closeInventory();
				CraftsManager.openCraftBookInventory(player);
			}

		}

		// Ban level 2 potions
		if (event.getInventory().getType().equals(InventoryType.BREWING)
				&& gm.getConfiguration().getBanLevelTwoPotions()) {
			final BrewerInventory inv = (BrewerInventory) event.getInventory();
			final HumanEntity human = event.getWhoClicked();
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(),
					new CheckBrewingStandAfterClick(inv.getHolder(), human), 1);
		}
	}

	private void handleGameItemInteract(GameItem gameItem, Player player, UhcPlayer uhcPlayer, ItemStack item) {
		GameManager gm = GameManager.getGameManager();

		switch (gameItem) {
			case TEAM_SELECTION:
				UhcItems.openTeamMainInventory(player, uhcPlayer);
				break;
			case TEAM_SETTINGS:
				UhcItems.openTeamSettingsInventory(player);
				break;
			case KIT_SELECTION:
				KitsManager.openKitSelectionInventory(player);
				break;
			case CUSTOM_CRAFT_BOOK:
				CraftsManager.openCraftBookInventory(player);
				break;
			case TEAM_COLOR_SELECTION:
				UhcItems.openTeamColorInventory(player);
				break;
			case TEAM_RENAME:
				openTeamRenameGUI(player, uhcPlayer.getTeam());
				break;
			case SCENARIO_VIEWER:
				Inventory inv;
				if (gm.getConfiguration().getEnableScenarioVoting()) {
					inv = gm.getScenarioManager().getScenarioVoteInventory(uhcPlayer);
				} else {
					inv = gm.getScenarioManager()
							.getScenarioMainInventory(player.hasPermission("uhc-core.scenarios.edit"));
				}
				player.openInventory(inv);
				break;
			case BUNGEE_ITEM:
				GameManager.getGameManager().getPlayersManager().sendPlayerToBungeeServer(player);
				break;
			case COMPASS_ITEM:
				uhcPlayer.pointCompassToNextPlayer(gm.getConfiguration().getPlayingCompassMode(),
						gm.getConfiguration().getPlayingCompassCooldown());
				break;
			case TEAM_READY:
			case TEAM_NOT_READY:
				uhcPlayer.getTeam().changeReadyState();
				UhcItems.openTeamSettingsInventory(player);
				break;
			case TEAM_INVITE_PLAYER:
				UhcItems.openTeamInviteInventory(player);
				break;
			case TEAM_INVITE_PLAYER_SEARCH:
				openTeamInviteGUI(player);
				break;
			case TEAM_VIEW_INVITES:
				UhcItems.openTeamInvitesInventory(player, uhcPlayer);
				break;
			case TEAM_INVITE_ACCEPT:
				handleTeamInviteReply(uhcPlayer, item, true);
				player.closeInventory();
				break;
			case TEAM_INVITE_DENY:
				handleTeamInviteReply(uhcPlayer, item, false);
				player.closeInventory();
				break;
			case TEAM_LEAVE:
				try {
					uhcPlayer.getTeam().leave(uhcPlayer);
				} catch (UhcTeamException ex) {
					uhcPlayer.sendMessage(ex.getMessage());
				}
				break;
			case TEAM_LIST:
				UhcItems.openTeamsListInventory(player);
				break;
			case LIST_ITEM:
				Bukkit.dispatchCommand(player, "list");
				break;
			case SCENARIO_READER:
				Bukkit.dispatchCommand(player, "game");
				break;
		}
	}

	private void handleTeamInviteReply(UhcPlayer uhcPlayer, ItemStack item, boolean accepted) {
		if (!item.hasItemMeta()) {
			uhcPlayer.sendMessage("Something went wrong!");
			return;
		}

		ItemMeta meta = item.getItemMeta();

		if (!meta.hasLore()) {
			uhcPlayer.sendMessage("Something went wrong!");
			return;
		}

		if (meta.getLore().size() != 2) {
			uhcPlayer.sendMessage("Something went wrong!");
			return;
		}

		String line = meta.getLore().get(1).replace(ChatColor.DARK_GRAY.toString(), "");
		UhcTeam team = GameManager.getGameManager().getTeamManager().getTeamByName(line);

		if (team == null) {
			uhcPlayer.sendMessage(Lang.TEAM_MESSAGE_NO_LONGER_EXISTS);
			return;
		}

		GameManager.getGameManager().getTeamManager().replyToTeamInvite(uhcPlayer, team, accepted);
	}

	private void openTeamRenameGUI(Player player, UhcTeam team) {
		player.sendMessage("AnvilGUI isn't working at the moment! Idk why");
		// new
		// AnvilGUI.Builder().plugin(UhcCore.getPlugin()).title(Lang.TEAM_INVENTORY_RENAME).text(team.getTeamName())
		// .item(new ItemStack(Material.NAME_TAG)).onComplete(((p, s) -> {
		// if (GameManager.getGameManager().getTeamManager().isValidTeamName(s)) {
		// team.setTeamName(s);
		// p.sendMessage(Lang.TEAM_MESSAGE_NAME_CHANGED);
		// return AnvilGUI.Response.close();
		// } else {
		// p.sendMessage(Lang.TEAM_MESSAGE_NAME_CHANGED_ERROR);
		// return AnvilGUI.Response.close();
		// }
		// })).open(player);
	}

	private void openTeamInviteGUI(Player player) {
		player.sendMessage("AnvilGUI isn't working at the moment! Idk why");
		// new
		// AnvilGUI.Builder().plugin(UhcCore.getPlugin()).title(Lang.TEAM_INVENTORY_INVITE_PLAYER)
		// .text("Enter name ...").item(new
		// ItemStack(Material.NAME_TAG)).onComplete(((p, s) -> {
		// p.performCommand("team invite " + s);
		// return AnvilGUI.Response.close();
		// })).open(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHopperEvent(InventoryMoveItemEvent event) {
		Inventory inv = event.getDestination();
		if (inv.getType().equals(InventoryType.BREWING)
				&& GameManager.getGameManager().getConfiguration().getBanLevelTwoPotions()
				&& inv.getHolder() instanceof BrewingStand) {
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(),
					new CheckBrewingStandAfterClick((BrewingStand) inv.getHolder(), null), 1);
		}

	}

	private static class CheckBrewingStandAfterClick implements Runnable {
		private final BrewingStand stand;
		private final HumanEntity human;

		private CheckBrewingStandAfterClick(BrewingStand stand, HumanEntity human) {
			this.stand = stand;
			this.human = human;
		}

		@Override
		public void run() {
			ItemStack ingredient = stand.getInventory().getIngredient();
			if (ingredient != null && ingredient.getType().equals(Material.GLOWSTONE_DUST)) {
				if (human != null) { human.sendMessage(Lang.ITEMS_POTION_BANNED); }

				stand.getLocation().getWorld().dropItemNaturally(stand.getLocation(), ingredient.clone());
				stand.getInventory().setIngredient(new ItemStack(Material.AIR));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		GameManager gm = GameManager.getGameManager();

		if (gm.getGameState() == GameState.WAITING
				|| gm.getPlayersManager().getUhcPlayer(event.getPlayer()).getState() == PlayerState.DEAD) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
		if (e.getItem() == null) return;

		if (e.getItem().equals(UhcItems.createGoldenHead())) {
			e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
		}
	}

	@EventHandler
	public void onSignBook(PlayerEditBookEvent e) {
		GameManager gm = GameManager.getGameManager();
		if (gm.getGameState() == GameState.WAITING) e.setCancelled(true);
	}

	private void handleScenarioInventory(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) return;

		InventoryView clickedInv = e.getView();

		if (clickedInv == null || e.getCurrentItem() == null) return;

		Player player = ((Player) e.getWhoClicked()).getPlayer();
		ItemStack item = e.getCurrentItem();
		ItemMeta meta = item.getItemMeta();
		GameManager gm = GameManager.getGameManager();
		PlayersManager pm = gm.getPlayersManager();
		ScenarioManager scenarioManager = gm.getScenarioManager();

		if (gm.getGameState() == GameState.WAITING) { e.setCancelled(true); }

		if (!e.getCurrentItem().hasItemMeta()) return;

		boolean mainInventory = clickedInv.getTitle().equals(Lang.SCENARIO_GLOBAL_INVENTORY);
		boolean editInventory = clickedInv.getTitle().equals(Lang.SCENARIO_GLOBAL_INVENTORY_EDIT);
		boolean voteInventory = clickedInv.getTitle().substring(2).equals(Lang.SCENARIO_GLOBAL_INVENTORY_VOTE);
		boolean listInventory = clickedInv.getTitle().startsWith(InventoryGUIListener.LIST_TITLE);
		// No scenario inventory!
		if (!mainInventory && !editInventory && !voteInventory && !listInventory) { return; }

		// Get scenario info when right click or when on the global inventory menu.
		if (e.getClick() == ClickType.RIGHT || mainInventory) {

			e.setCancelled(true); // might be redundant
			player.closeInventory();
			// Handle edit item
			if (meta.getDisplayName().equals(Lang.SCENARIO_GLOBAL_ITEM_EDIT)) {
				Inventory inv = scenarioManager.getScenarioEditInventory();
				player.openInventory(inv);
				return;
			}

			// Clicked scenario
			Scenario scenario = Scenario.getScenario(meta.getDisplayName());

			// Clicked item is not a scenario item
			if (scenario == null) { return; }

			// Send scenario info
			player.sendMessage(Lang.SCENARIO_GLOBAL_DESCRIPTION_HEADER.replace("%scenario%", scenario.getName()));
			scenario.getDescription().forEach(s -> player.sendMessage(Lang.SCENARIO_GLOBAL_DESCRIPTION_PREFIX + s));
		} else if (editInventory) {

			e.setCancelled(true); // might be redundant
			player.closeInventory();
			// Handle back item
			if (item.getItemMeta().getDisplayName().equals(Lang.SCENARIO_GLOBAL_ITEM_BACK)) {
				Inventory inv = scenarioManager.getScenarioMainInventory(true);
				player.openInventory(inv);
				return;
			}

			// Clicked scenario
			Scenario scenario = Scenario.getScenario(meta.getDisplayName());

			// toggle scenario
			scenarioManager.toggleScenario(scenario);

			// Open edit inventory
			player.openInventory(scenarioManager.getScenarioEditInventory());
		} else if (voteInventory) {

			e.setCancelled(true); // might be redundant
			player.closeInventory();
			UhcPlayer uhcPlayer = pm.getUhcPlayer(player);

			if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) return;

			// Clicked scenario
			Scenario scenario = Scenario.getScenario(meta.getDisplayName());

			voteForScenario(uhcPlayer, scenario);

			player.openInventory(scenarioManager.getScenarioVoteInventory(uhcPlayer));
		} else if (listInventory) {
			if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) {
				if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) e.setCancelled(true);
				return;
			}
			e.setCancelled(true);

			int page = Integer.parseInt(clickedInv.getTitle().substring(InventoryGUIListener.LIST_TITLE.length() + 11,
					clickedInv.getTitle().length() - 3)) - 1;
			// Bukkit.getLogger().info(page + "");
			UhcPlayer uhcPlayer = pm.getUhcPlayer(player);

			item = e.getCurrentItem();
			if (item == null) return;
			switch (item.getType()) {
				case BARRIER:
					e.getView().close();
					break;
				case PLAYER_HEAD:
					try {
						UhcPlayer u = GameManager.getGameManager().getPlayersManager()
								.getUhcPlayer(((SkullMeta) item.getItemMeta()).getOwner());

						if (u.getState() == PlayerState.PLAYING && (uhcPlayer.getState() == PlayerState.DEAD
								|| player.hasPermission("uhc-core.commands.teleport-admin"))) {
							player.teleport(u.getPlayer().getLocation(), TeleportCause.NETHER_PORTAL);
							player.closeInventory();
						}
					} catch (Exception hasdflkj) {

					}
					break;
				case ARROW:
					player.closeInventory();
					player.openInventory(GameManager.getGameManager().getListInventoryHandler()
							.getListInventory(uhcPlayer, page - 1));
					break;
				case SPECTRAL_ARROW:
					player.closeInventory();
					player.openInventory(GameManager.getGameManager().getListInventoryHandler()
							.getListInventory(uhcPlayer, page + 1));
					break;
				default:
			}
		}
	}

	@EventHandler // idk why this is here
	public void onNPCPickup(EntityPickupItemEvent e) { if (e.getEntity().hasMetadata("NPC")) e.setCancelled(false); }

	public static void voteForScenario(UhcPlayer uhcPlayer, Scenario scenario) {
		ItemStack item = null;
		ItemMeta meta = null;
		for (ItemStack scenItem : GameManager.getGameManager().getScenarioManager().getScenarioVoteInventory(uhcPlayer)
				.getContents()) {
			if (scenItem == null || !scenItem.hasItemMeta()) continue;
			meta = scenItem.getItemMeta();
			if (scenario.equals(scenItem.getItemMeta().getDisplayName())) {
				item = scenItem;
				break;
			}
		}
		if (item == null || meta == null)
			throw new IllegalArgumentException("Scenario " + scenario.getName() + " is not in the voting inventory!");

		Player player;
		try {
			player = uhcPlayer.getPlayer();
		} catch (UhcPlayerNotOnlineException e) {
			return;
		}
		GameManager gm = GameManager.getGameManager();
		// toggle scenario
		if (uhcPlayer.getScenarioVotes().contains(scenario)) {
			uhcPlayer.getScenarioVotes().remove(scenario);
			if (item.getAmount() == 1) {
				switch (scenario) {
					case RANDOM:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7c0", "\u00a77Vote to randomize the scenarios!",
								Lang.SCENARIO_GLOBAL_ITEM_INFO));
						break;
					case NONE:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7c0", "\u00a77Vote to cancel all scenarios!",
								Lang.SCENARIO_GLOBAL_ITEM_INFO));
						break;
					case BOTSIN:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7c0", "\u00a77Vote to fill the game with bots!",
								Lang.SCENARIO_GLOBAL_ITEM_INFO));
						if (0 >= Bukkit.getOnlinePlayers().size() / 2. || Bukkit.getOnlinePlayers().size() <= 1)
							GameManager.getGameManager().setBotsIn(true);
						else GameManager.getGameManager().setBotsIn(false);
						break;
					default:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7c0", Lang.SCENARIO_GLOBAL_ITEM_INFO));
				}
				meta.removeEnchant(Enchantment.DURABILITY);
			} else {
				switch (scenario) {
					case RANDOM:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d" + (item.getAmount() - 1),
								"\u00a77Vote to randomize the scenarios!", Lang.SCENARIO_GLOBAL_ITEM_INFO));
						break;
					case NONE:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d" + (item.getAmount() - 1),
								"\u00a77Vote to cancel all scenarios!", Lang.SCENARIO_GLOBAL_ITEM_INFO));
						break;
					case BOTSIN:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d" + (item.getAmount() - 1),
								"\u00a77Vote to fill the game with bots!", Lang.SCENARIO_GLOBAL_ITEM_INFO));
						if (item.getAmount() - 1 >= Bukkit.getOnlinePlayers().size() / 2.
								|| Bukkit.getOnlinePlayers().size() <= 1)
							GameManager.getGameManager().setBotsIn(true);
						else GameManager.getGameManager().setBotsIn(false);
						break;
					default:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d" + (item.getAmount() - 1),
								Lang.SCENARIO_GLOBAL_ITEM_INFO));
				}
				item.setAmount(item.getAmount() - 1);
			}
			item.setItemMeta(meta);
		} else {
			int maxVotes = gm.getConfiguration().getMaxScenarioVotes();
			int nonScenarios = (int) uhcPlayer.getScenarioVotes().stream()
					.filter(scen -> scen == Scenario.RANDOM || scen == Scenario.NONE || scen == Scenario.BOTSIN)
					.count();
			if (scenario != Scenario.RANDOM && scenario != Scenario.NONE && scenario != Scenario.BOTSIN
					&& uhcPlayer.getScenarioVotes().size() - nonScenarios >= maxVotes) {
				player.sendMessage(Lang.SCENARIO_GLOBAL_VOTE_MAX.replace("%max%", String.valueOf(maxVotes)));
				return;
			}

			if (uhcPlayer.getScenarioVotes().contains(Scenario.NONE) && scenario == Scenario.RANDOM) {
				ItemsListener.voteForScenario(uhcPlayer, Scenario.NONE);
			}
			if (uhcPlayer.getScenarioVotes().contains(Scenario.RANDOM) && scenario == Scenario.NONE) {
				ItemsListener.voteForScenario(uhcPlayer, Scenario.RANDOM);
			}
			uhcPlayer.getScenarioVotes().add(scenario);

			if (item.getAmount() == 1 && !meta.hasEnchant(Enchantment.DURABILITY)) {
				switch (scenario) {
					case RANDOM:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d1", "\u00a77Vote to randomize the scenarios!",
								Lang.SCENARIO_GLOBAL_ITEM_INFO));
						break;
					case NONE:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d1", "\u00a77Vote to cancel all scenarios!",
								Lang.SCENARIO_GLOBAL_ITEM_INFO));
						break;
					case BOTSIN:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d1", "\u00a77Vote to fill the game with bots!",
								Lang.SCENARIO_GLOBAL_ITEM_INFO));
						if (1 >= Bukkit.getOnlinePlayers().size() / 2. || Bukkit.getOnlinePlayers().size() <= 1)
							GameManager.getGameManager().setBotsIn(true);
						else GameManager.getGameManager().setBotsIn(false);
						break;
					default:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d1", Lang.SCENARIO_GLOBAL_ITEM_INFO));
				}
				meta.addEnchant(Enchantment.DURABILITY, 1, true);
			} else {
				switch (scenario) {
					case RANDOM:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d" + (item.getAmount() + 1),
								"\u00a77Vote to randomize the scenarios!", Lang.SCENARIO_GLOBAL_ITEM_INFO));
						break;
					case NONE:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d" + (item.getAmount() + 1),
								"\u00a77Vote to cancel all scenarios!", Lang.SCENARIO_GLOBAL_ITEM_INFO));
						break;
					case BOTSIN:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d" + (item.getAmount() + 1),
								"\u00a77Vote to fill the game with bots!", Lang.SCENARIO_GLOBAL_ITEM_INFO));
						if (item.getAmount() + 1 >= Bukkit.getOnlinePlayers().size() / 2.
								|| Bukkit.getOnlinePlayers().size() <= 1)
							GameManager.getGameManager().setBotsIn(true);
						else GameManager.getGameManager().setBotsIn(false);
						break;
					default:
						meta.setLore(Arrays.asList("\u00a7fVotes: \u00a7d" + (item.getAmount() + 1),
								Lang.SCENARIO_GLOBAL_ITEM_INFO));
				}
				item.setAmount(item.getAmount() + 1);
			}

			item.setItemMeta(meta);
		}
		if (uhcPlayer.getScenarioVotes().contains(Scenario.BOTSIN)) {
			player.getInventory().setItem(4, Scenario.BOTSIN.getScenarioItem());
		} else {
			player.getInventory().clear(4);
		}

		if (uhcPlayer.getScenarioVotes().contains(Scenario.NONE)) {
			player.getInventory().setItem(3, Scenario.NONE.getScenarioItem());
		} else if (uhcPlayer.getScenarioVotes().contains(Scenario.RANDOM)) {
			player.getInventory().setItem(3, Scenario.RANDOM.getScenarioItem());
		} else {
			player.getInventory().clear(3);
		}
		// add to
		Iterator<Scenario> it = uhcPlayer.getScenarioVotes().iterator();
		for (int i = 8; i > 5; i--) {
			if (it.hasNext()) {
				Scenario scen = it.next();
				if (scen != Scenario.BOTSIN && scen != Scenario.NONE && scen != Scenario.RANDOM)
					player.getInventory().setItem(i, scen.getScenarioItem());
				else i++;
			} else player.getInventory().clear(i);
		}
	}

}