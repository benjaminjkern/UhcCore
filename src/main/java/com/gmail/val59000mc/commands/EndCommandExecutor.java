package com.gmail.val59000mc.commands;

import java.util.HashSet;
import java.util.Set;

import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndCommandExecutor implements CommandExecutor {
    private final GameManager gameManager;

    private Set<UhcPlayer> voted;

    public EndCommandExecutor(GameManager gameManager) {
        this.voted = new HashSet<>();
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

        UhcTeam myTeam = null;

        for (UhcTeam team : pm.listUhcTeams()) {
            boolean seenYou = false;

            for (UhcPlayer uhcPlayer : team.getMembers()) {
                if (uhcPlayer.getState() != PlayerState.PLAYING) continue;
                if (uhcPlayer.getName().equals("YEUH-BOT")) continue;

                if (!uhcPlayer.getName().equals(player.getName())) {
                    lastPlayerAlive = false;
                    if (seenYou) break;
                } else {
                    seenYou = true;
                    myTeam = team;
                    voted.add(uhcPlayer);
                    if (!lastPlayerAlive) break;
                }
            }
            if (!seenYou && !lastPlayerAlive) {
                lastPlayerTeamAlive = false;
                break;
            }
        }

        if (!sender.hasPermission("uhccore.admin") && !lastPlayerTeamAlive) {
            sender.sendMessage("\u00a7cYou can only use this command if you are the last team alive!");
            return true;
        }

        if (lastPlayerTeamAlive) {
            if (myTeam == null) {
                sender.sendMessage("Something went wrong!");
                return false;
            }
            int neededVotes = myTeam.getOnlinePlayingMembers().size();
            if (voted.size() >= neededVotes) {
                gameManager.endGame();
                return true;
            } else {
                myTeam.sendMessage(
                        "\u00a7eYou have voted to end the game! All alive players on your team need to vote in order for the game to end!");
                myTeam.sendMessage("\u00a7eYou need \u00a7f\u00a7l" + (neededVotes - voted.size())
                        + " \u00a7emore votes to end the game!");
                return true;
            }
        }

        gameManager.endGame();
        return true;
    }
}
