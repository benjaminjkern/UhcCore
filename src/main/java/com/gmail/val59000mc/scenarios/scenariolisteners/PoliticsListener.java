package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PoliticsListener extends ScenarioListener {

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (e.getEntityType() != EntityType.PLAYER || !(e.getDamager() instanceof Player)) return;

        if (getGameManager().getGameState() != GameState.PLAYING) return;

        Player killed = (Player) e.getEntity();
        Player killer = (Player) e.getDamager();

        if (e.getDamage() < killed.getHealth()) return;

        PlayersManager pm = getPlayersManager();
        UhcPlayer uhcKilled = pm.getUhcPlayer(killed);
        UhcPlayer uhcKiller = pm.getUhcPlayer(killer);

        // check if it was friendlyfire
        if (uhcKiller.getTeam() == uhcKilled.getTeam()) return;

        getGameManager().broadcastMessage(
                uhcKilled.getDisplayName() + " was killed and joined " + uhcKiller.getDisplayName() + "'s team!");

        GameManager gm = GameManager.getGameManager();
        gm.sendInfoToServer("KILL:" + uhcKiller.getName() + ":" + uhcKilled.getName(), false);

        uhcKiller.kills++;

        if (getGameManager().getConfiguration().getEnableExpDropOnDeath()) {
            UhcItems.spawnExtraXp(killed.getLocation(), getGameManager().getConfiguration().getExpDropOnDeath());
        }

        uhcKilled.getTeam().getMembers().remove(uhcKilled);
        uhcKilled.setTeam(uhcKiller.getTeam());
        uhcKiller.getTeam().getMembers().add(uhcKilled);

        uhcKiller.getTeam().sendMessage(Lang.TEAM_MESSAGE_PLAYER_JOINS.replace("%player%", uhcKilled.getName()));

        gm.getScoreboardManager().updatePlayerTab(uhcKilled);

        killed.setHealth(10);
        e.setCancelled(true);
    }

}