package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.events.UhcStartedEvent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Recipe;
import org.bukkit.event.EventHandler;

import java.util.*;

public class NoCraftListener extends ScenarioListener {

    @EventHandler
    public void onGameStart(UhcStartedEvent e) {
        Iterator<Recipe> iterator = Bukkit.recipeIterator();

        Recipe recipe;
        while (iterator.hasNext()) {
            recipe = iterator.next();
            Bukkit.removeRecipe(recipe.getResult().getType().getKey());
        }
    }

}