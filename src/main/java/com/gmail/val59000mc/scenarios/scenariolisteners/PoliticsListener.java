package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PoliticsListener extends ScenarioListener{

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e){
        if (e.getEntityType() != EntityType.PLAYER || !(e.getDamager() instanceof Player)) return;

        if (getGameManager().getGameState() != GameState.PLAYING)return;

        Player killed = (Player) e.getEntity();
        Player killer = (Player) e.getDamager();

        if (e.getDamage() < killed.getHealth()) return;

        PlayersManager pm = getPlayersManager();
        UhcPlayer uhcKilled = pm.getUhcPlayer(killed);
        UhcPlayer uhcKiller = pm.getUhcPlayer(killer);

        // check if it was friendlyfire
        if (uhcKiller.getTeam() == uhcKilled.getTeam()) return;

        boolean result = addPlayerToTeam(uhcKilled, uhcKiller.getTeam());

        if (result) {
            killed.setHealth(10);
            e.setCancelled(true);
        }
    }

    private boolean addPlayerToTeam(UhcPlayer player, UhcTeam team){
        Inventory teamInventory = team.getTeamInventory();

        for (ItemStack item : player.getTeam().getTeamInventory().getContents()){
            if (item == null || item.getType() == Material.AIR){
                continue;
            }

            if (teamInventory.getContents().length < teamInventory.getSize()){
                teamInventory.addItem(item);
            }else {
                try {
                    Player bukkitPlayer = player.getPlayer();
                    bukkitPlayer.getWorld().dropItem(bukkitPlayer.getLocation(), item);
                }catch (UhcPlayerNotOnlineException ex){
                    ex.printStackTrace();
                }
            }
        }

        player.setTeam(team);
        team.getMembers().add(player);

        team.sendMessage(Lang.TEAM_MESSAGE_PLAYER_JOINS.replace("%player%", player.getName()));
        GameManager gm = GameManager.getGameManager();
        gm.getScoreboardManager().updatePlayerTab(player);
        return true;
    }

}