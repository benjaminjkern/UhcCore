package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HubCommandExecutor implements CommandExecutor {

    private final GameManager gameManager;

    public HubCommandExecutor(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        PlayersManager pm = gameManager.getPlayersManager();

        boolean lastPlayerTeamAlive = true;
        boolean lastPlayerAlive = true;

        for (UhcTeam team : pm.listUhcTeams()) {
            boolean seenYou = false;

            for (UhcPlayer uhcPlayer : team.getMembers()) {
                if (uhcPlayer.getState() != PlayerState.PLAYING)
                    continue;
                if (uhcPlayer.getName().equals("YEUH-BOT"))
                    continue;

                if (!uhcPlayer.getName().equals(player.getName())) {
                    lastPlayerAlive = false;
                    if (seenYou)
                        break;
                } else {
                    seenYou = true;
                    if (!lastPlayerAlive)
                        break;
                }
            }
            if (!seenYou && !lastPlayerAlive) {
                lastPlayerTeamAlive = false;
                break;
            }
        }

        if (args.length == 0) {
            if (pm.getUhcPlayer(player).getState() == PlayerState.PLAYING
                    && (!lastPlayerTeamAlive || GameManager.getGameManager().getStartRealTeams() == 1)) {
                sender.sendMessage(
                        "\u00a7eAre you sure you want to go back to the hub? Your player will be killed in the current game.\n\u00a7eUse \u00a7d/"
                                + s + " confirm \u00a7eto continue.");
                return true;
            }
        } else if (!args[0].toLowerCase().equals("confirm"))
            return false;

        if (pm.getUhcPlayer(player).getState() == PlayerState.PLAYING
                && (!lastPlayerTeamAlive || GameManager.getGameManager().getStartRealTeams() == 1)) {
            Bukkit.dispatchCommand(sender, "suicide");
        }

        if (lastPlayerAlive)
            gameManager.endGame();

        if (gameManager.getConfiguration().getEnableBungeeSupport()) {
            gameManager.getPlayersManager().sendPlayerToBungeeServer(player);
            return true;
        }

        player.sendMessage(Lang.PLAYERS_SEND_BUNGEE_DISABLED);
        return true;
    }

}