package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;

public class SharedHealthListener extends ScenarioListener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        // Check if entity is player
        if (e.getEntityType() != EntityType.PLAYER) { return; }

        // Check if GameState is Playing | Deathmatch
        GameState state = getGameManager().getGameState();
        if (state != GameState.PLAYING && state != GameState.DEATHMATCH) { return; }

        UhcPlayer uhcPlayer = getPlayersManager().getUhcPlayer((Player) e.getEntity());

        // Check if player is playing
        if (uhcPlayer.getState() != PlayerState.PLAYING) { return; }

        // If solo player don't share damage
        List<UhcPlayer> onlinePlayingMembers = uhcPlayer.getTeam().getOnlinePlayingMembers();
        if (onlinePlayingMembers.size() <= 1) { return; }

        double damage = e.getDamage() / onlinePlayingMembers.size();
        e.setDamage(damage);

        for (UhcPlayer uhcMember : onlinePlayingMembers) {
            if (uhcMember == uhcPlayer) { continue; }

            try {
                Player thisPlayer = uhcMember.getPlayer();
                double health = thisPlayer.getHealth();
                uhcMember.getPlayer().setHealth(health - damage);
            } catch (UhcPlayerNotOnlineException ex) {
                ex.printStackTrace();
            }
        }
    }

}