package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.UhcCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DiscordCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        sender.sendMessage(UhcCore.PREFIX
                + "\u00a7fClick to join our discord community! \u00a75\u00a7l>\u00a7d\u00a7l>\u00a75\u00a7l> \u00a77\u00a7nhttps://discord.yeuh.net/");

        return true;
    }
}
