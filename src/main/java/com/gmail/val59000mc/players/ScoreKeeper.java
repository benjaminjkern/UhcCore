package com.gmail.val59000mc.players;

import java.util.*;
import com.gmail.val59000mc.game.GameManager;

public class ScoreKeeper {

    private Map<String, PlayerStats> cache;

    private static final PlayerStats PLAYER_NOT_FOUND = new PlayerStats(null);

    public ScoreKeeper() { cache = new HashMap<>(); }

    public PlayerStats getStats(String name) {
        if (cache.containsKey(name.toLowerCase())) return cache.get(name.toLowerCase());

        GameManager.getGameManager().sendInfoToServer("STATSREQUEST:" + name, false);

        return PLAYER_NOT_FOUND;
    }

    public void storePlayer(PlayerStats p) {
        if (cache.containsKey(p.player.toLowerCase())) {
            cache.get(p.player.toLowerCase()).overwrite(p);
            return;
        }
        cache.put(p.player, p);
    }
}