package com.github.yukinoraru.ToggleInventory;

import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

public class OutputChest {
	private ToggleInventory plugin;

	public OutputChest(ToggleInventory plugin) {
		this.plugin = plugin;
	}

	public boolean output(Player player, Location location, int n) {
		File[] files = playerFiles();
		ItemStack[][] fileItemStacks = new ItemStack[n][];
		int height = 0;

		player.sendMessage("get item stack:");
		n = Math.min(n, files.length);
		for (int index = 0; index < n; ++index) {
			try {
				fileItemStacks[index] = plugin.inventoryManager.getInventoryItemStacks(files[index]);
			} catch (Exception e) {
				e.printStackTrace();
				player.sendMessage("Config file read error");
				return false;
			}

			height += Math.ceil(fileItemStacks[index].length / 9.0 / 6.0);
		}


		player.sendMessage("check can create chest: " + height);
		Location checkChestLocation = location.clone();
		for (int i = 0; i < height; ++i, checkChestLocation.add(0, 1, 0)) {
			if (!canCreateChest(checkChestLocation)) {
				player.sendMessage("There is no place to put a chest");
				return false;
			}
		}

		Location chestLocation = location.clone();
		for (int index = 0; index < n; ++index) {
			ItemStack[] itemStacks = fileItemStacks[index];
			File file = files[index];
			String name = getFilePlayerName(file);
			player.sendMessage("process: " + name + " " + itemStacks.length + "stacks");

			for (int i = 0; i < itemStacks.length; i += 9 * 6) {
				int end = Math.min(itemStacks.length, i + 9 * 6);
				ItemStack[] partItems = Arrays.copyOfRange(itemStacks, i, end);
				createChest(chestLocation, name, partItems);
				chestLocation.add(0, 1, 0);
			}
			file.delete();
		}

		return true;
	}

	public boolean canCreateChest(Location location) {
		Location start = location.clone();
		Location startNeighborhood = location.clone();
		return
			start.getBlock().getType() == Material.AIR &&
			start.add(0, 0, 1).getBlock().getType() == Material.AIR &&
			start.add(0, 0, 1).getBlock().getType() == Material.AIR &&
			startNeighborhood.add(1, 0, 1).getBlock().getType() != Material.CHEST &&
			startNeighborhood.add(0, 0, 1).getBlock().getType() != Material.CHEST &&
			startNeighborhood.add(-1, 0, 1).getBlock().getType() != Material.CHEST &&
			startNeighborhood.add(-1, 0, -1).getBlock().getType() != Material.CHEST &&
			startNeighborhood.add(0, 0, -1).getBlock().getType() != Material.CHEST;
	}

	public void createChest(Location location, String name, ItemStack[] itemStacks) {
		Location start = location.clone();
		Block signBlock = start.getBlock();
		Block chestBlock = start.add(0, 0, 1).getBlock();
		start.add(0, 0, 1).getBlock().setType(Material.CHEST);

		signBlock.setType(Material.WALL_SIGN);
		chestBlock.setType(Material.CHEST);

		((Chest) chestBlock.getState()).getInventory().setContents(itemStacks);

		Sign sign = (Sign)signBlock.getState();
		sign.setLine(1, name);
		sign.update();
	}

	public File[] playerFiles() {
		FilenameFilter filter = new FilenameFilter(){
            public boolean accept(File file, String str) {
                return str.endsWith(".yml");
            }
        };
		return new File(plugin.getDataFolder() + File.separator + "players").listFiles(filter);
	}

	public String getFilePlayerName(File file) {
		return file.getName().substring(0, file.getName().length() - 4);
	}
}
