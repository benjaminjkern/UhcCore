package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.scenarios.ScenarioListener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PassiveListener extends ScenarioListener {

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if (damager instanceof Projectile) damager = (Entity) ((Projectile) damager).getShooter();
        if (!(e.getEntity() instanceof Player) || !(damager instanceof Player)) return;
        damager.sendMessage(UhcCore.PREFIX + "You can't hurt them while Passive Aggressive is on!");
        e.setCancelled(true);
    }
}
