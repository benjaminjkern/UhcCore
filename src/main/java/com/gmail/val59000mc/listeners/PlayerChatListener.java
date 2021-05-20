package com.gmail.val59000mc.listeners;

import java.util.HashSet;
import java.util.Set;

import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

	private final Set<Player> sentMessage;

	private final PlayersManager playersManager;
	private final MainConfiguration configuration;

	public PlayerChatListener(PlayersManager playersManager, MainConfiguration configuration) {
		this.playersManager = playersManager;
		this.configuration = configuration;
		this.sentMessage = new HashSet<>();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();

		if (e.isCancelled()) { return; }

		UhcPlayer uhcPlayer = playersManager.getUhcPlayer(player);

		// Spec chat
		if (!configuration.getCanSendMessagesAfterDeath() && uhcPlayer.getState() == PlayerState.DEAD) {
			// check if has override permissions
			if (player.hasPermission("uhc-core.chat.override")) return;

			// Send message in spec chat.
			String message = Lang.DISPLAY_SPECTATOR_CHAT.replace("%player%", player.getDisplayName())
					.replace("%message%", e.getMessage());

			playersManager.getOnlineSpectatingPlayers().forEach(p -> p.sendMessage(message));
			e.setCancelled(true);
			return;
		}

		if (uhcPlayer.getState() == PlayerState.DEAD) {
			String message = String.format("\u00a77(Spectating) " + e.getFormat(), player.getName(), e.getMessage());
			Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
			Bukkit.getLogger().info(message);
			return;
		}

		if (e.getMessage().equals("!")) {
			if (uhcPlayer.isGlobalChat()) player.sendMessage(
					"\u00a77\u00a7oTeam Chat has been enabled. Use \u00a7d! \u00a77\u00a7ofollowed by a message to temporarily use global chat.");
			else player.sendMessage("\u00a77\u00a7oGlobal Chat has been enabled.");
			uhcPlayer.setGlobalChat(!uhcPlayer.isGlobalChat());
			e.setCancelled(true);
			return;
		}

		// Team chat
		if (uhcPlayer.getState() == PlayerState.PLAYING && isTeamMessage(e, uhcPlayer)) {
			e.setCancelled(true);
			if (!sentMessage.contains(player)) {
				sentMessage.add(player);
				player.sendMessage(
						"\u00a77\u00a7oWhen teams are enabled, team chat is enabled by default. To use global chat, use \u00a7d! \u00a77\u00a7ofollowed by your message.");
				player.sendMessage(
						"\u00a77\u00a7oTo toggle permanent global chat, Just use \u00a7d! \u00a77\u00a7owithout any message.");
			}
			uhcPlayer.getTeam().sendChatMessageToTeamMembers(uhcPlayer, e.getMessage());
		}

	}

	private boolean isTeamMessage(AsyncPlayerChatEvent e, UhcPlayer uhcPlayer) {
		if (uhcPlayer.isGlobalChat()) return false;

		if (configuration.getEnableChatPrefix()) {
			if (e.getMessage().startsWith(configuration.getTeamChatPrefix())) {
				e.setMessage(e.getMessage().replaceFirst(configuration.getTeamChatPrefix(), ""));
				return true;
			}
			if (e.getMessage().startsWith(configuration.getGlobalChatPrefix())) {
				e.setMessage(e.getMessage().replaceFirst(configuration.getGlobalChatPrefix(), ""));
				return false;
			}
		}

		return true;
	}

}