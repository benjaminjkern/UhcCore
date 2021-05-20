package com.gmail.val59000mc.events;

import java.util.List;

import com.gmail.val59000mc.players.UhcPlayer;

import org.bukkit.inventory.ItemStack;

public final class UhcPlayerDeathEvent extends UhcEvent {

	private final UhcPlayer killed;
	private final List<ItemStack> drops;

	public UhcPlayerDeathEvent(UhcPlayer killed, List<ItemStack> drops) {
		this.killed = killed;
		this.drops = drops;
	}

	public UhcPlayer getKilled() { return killed; }

	public List<ItemStack> getDrops() { return drops; }

}