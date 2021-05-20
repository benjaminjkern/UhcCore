package com.gmail.val59000mc.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.command.Command;

public class KillBotsExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("uhccore.admin")) return false;
        if (args.length == 0) {
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                Player p = (Player) npc.getEntity();
                p.addPotionEffect(new PotionEffect(PotionEffectType.HARM, 100, 100));
            }
        } else if (args.length == 1) {
            int max = Integer.parseInt(args[0]);
            int count = 0;
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                count++;
                if (count <= max) continue;
                Player p = (Player) npc.getEntity();
                p.addPotionEffect(new PotionEffect(PotionEffectType.HARM, 100, 100));
            }
        } else return false;
        return true;
    }
}
