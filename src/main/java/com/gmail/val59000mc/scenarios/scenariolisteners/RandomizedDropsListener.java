package com.gmail.val59000mc.scenarios.scenariolisteners;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.utils.FileUtils;
import com.gmail.val59000mc.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.val59000mc.scenarios.ScenarioListener;
import com.gmail.val59000mc.utils.RandomUtils;

public class RandomizedDropsListener extends ScenarioListener {

	// private static final String LOOT_TABLES_URL =
	// "https://raw.githubusercontent.com/Mezy/UhcCore/master/resources/loot_tables.zip";
	// private File datapack;

	private Set<Material> bannedMaterials;
	private Queue<Material> items;
	private final Map<Material, ItemStack> dropList;

	public RandomizedDropsListener() {
		// datapack = null;
		bannedMaterials = new HashSet<>();
		bannedMaterials.add(Material.BEDROCK);
		bannedMaterials.add(Material.BARRIER);
		bannedMaterials.add(Material.AIR);
		bannedMaterials.add(Material.COMMAND_BLOCK);
		bannedMaterials.add(Material.CHAIN_COMMAND_BLOCK);
		dropList = new HashMap<>();
		List<Material> allItems = Arrays.asList(Material.values()).stream().filter(m -> !bannedMaterials.contains(m))
				.collect(Collectors.toList());
		Collections.shuffle(allItems);
		items = new LinkedList<>(allItems);
	}

	// @Override
	// public void onEnable() {
	// List<Material> left = Arrays.asList(Material.values());
	// Collections.shuffle(left);
	// for (Material m : Material.values()) {
	// dropList.put(m,)
	// }
	// // if (UhcCore.getVersion() == 15) {
	// // try {
	// // generateDataPack();
	// // } catch (IOException ex) {
	// // ex.printStackTrace();
	// // }
	// // } else {
	// // items = VersionUtils.getVersionUtils().getItemList();
	// // }
	// }

	// @Override
	// public void onDisable() { if (datapack != null) { disableDataPack(); } }

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		// Using datapack
		// if (datapack != null) { return; }

		// Create new HashMap so each each type of broken block drops the same random
		// item every time it is broken (configurable
		Block block = event.getBlock();

		event.setCancelled(true);
		Location dropLocation = block.getLocation().add(.5, 0, .5);

		ItemStack blockDrop;

		if (dropList.containsKey(block.getType())) {
			dropLocation.getWorld().dropItemNaturally(dropLocation, dropList.get(block.getType()));
		} else {
			boolean allGood = false;
			while (!allGood) {
				try {
					Material material = items.poll();
					Bukkit.getLogger().info(material + "");
					blockDrop = new ItemStack(material);
					dropList.put(block.getType(), blockDrop);
					dropLocation.getWorld().dropItemNaturally(dropLocation, blockDrop);
					allGood = true;
				} catch (Exception e) {
					allGood = false;
				}
			}
		}
		block.setType(Material.AIR);

		Player player = event.getPlayer();
		ItemStack tool = player.getInventory().getItemInMainHand();

		if (tool != null && tool.hasItemMeta() && tool.getItemMeta() instanceof Damageable) {
			ItemMeta im = tool.getItemMeta();
			((Damageable) im).setDamage(((Damageable) im).getDamage() - 1);
			tool.setItemMeta(im);
			player.getInventory().setItemInMainHand(tool);
		}
	}

	// private void generateDataPack() throws IOException {
	// File temp = new File(UhcCore.getPlugin().getDataFolder() + File.separator +
	// "temp");
	// FileUtils.deleteFile(temp);
	// temp.mkdirs();
	// File lootTableZip = new File(temp, "loot_tables.zip");

	// FileUtils.downloadFile(new URL(LOOT_TABLES_URL), lootTableZip);
	// FileUtils.unzip(new ZipFile(lootTableZip), temp);

	// File lootTables = new File(temp, "loot_tables");

	// List<File> fileList = FileUtils.getDirFiles(lootTables, true);
	// List<File> remaining = FileUtils.getDirFiles(lootTables, true);

	// Map<String, File> mappedTables = new HashMap<>();

	// for (File file : fileList){
	// int i = RandomUtils.randomInteger(0, remaining.size()-1);
	// String path = file.getPath().replace(lootTables.getPath(), "");
	// mappedTables.put(path, remaining.get(i));
	// remaining.remove(i);
	// }

	// World mainWorld = Bukkit.getWorlds().get(0);
	// datapack = new File(Bukkit.getWorldContainer() + File.separator +
	// mainWorld.getName() + File.separator + "datapacks/randomized_drops");
	// FileUtils.deleteFile(datapack);
	// File lootTableDestination = new File(datapack, "data/minecraft/loot_tables");

	// for (String name : mappedTables.keySet()){
	// File file = new File(lootTableDestination + name);
	// file.getParentFile().mkdirs();
	// Files.copy(Paths.get(mappedTables.get(name).toURI()),
	// Paths.get(file.toURI()));
	// }

	// FileWriter fw = new FileWriter(new File(datapack, "pack.mcmeta"));
	// fw.write("{\"pack\":{\"pack_format\":1,\"description\":\"Randomized
	// Drops\"}}");
	// fw.flush();
	// fw.close();

	// Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:reload");

	// FileUtils.deleteFile(temp);
	// }

	// private void disableDataPack(){
	// FileUtils.deleteFile(datapack);
	// Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:reload");
	// }

}