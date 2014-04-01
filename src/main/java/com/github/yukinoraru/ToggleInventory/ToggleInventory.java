package com.github.yukinoraru.ToggleInventory;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ToggleInventory extends JavaPlugin implements Listener {

    protected Logger log;
    protected InventoryManager inventoryManager;

    public void onEnable(){
    	this.log = this.getLogger();

    	saveDefaultConfig();

    	// for previous version
    	getConfig().options().copyDefaults(true);
    	saveConfig();

    	// update check
    	final int PROJECT_ID = 43601;
    	if(getConfig().getBoolean("update-check", true)){
    		Updater updater = new Updater(this, PROJECT_ID, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
    		boolean isUpdateAvailable = (updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE);

    		if(isUpdateAvailable){
    			this.log.info("New version is available:" + updater.getLatestName());
    			this.log.info("Download Link:" + updater.getLatestFileLink());
        		// execute auto update
        		if(getConfig().getBoolean("auto-update", false)){
            		this.log.info("Auto-update is enabled, I'll update to latest version...");
        			new Updater(this, PROJECT_ID, this.getFile(), Updater.UpdateType.DEFAULT, false);
        		}
    		}
    		else{
        		this.log.info("This is the latest version!");

    		}
    	}
    	else{
    		this.log.info("Update check was skipped.");
    	}

    	this.inventoryManager = new InventoryManager(this);

    	// copy default special inventory file
    	File spInvFile = inventoryManager.getDefaultSpecialInventoryFile();
    	if(!spInvFile.exists()){
    		saveResource(spInvFile.getName(), false);
    	}

    	// Use MCStats:
    	// http://mcstats.org/plugin/ToggleInventory
    	if(!getConfig().getBoolean("disable-mcstats", false)){
	    	try {
	    		this.log.info("MCStats enabled. You can disable via config.yml.");
				Metrics metrics = new Metrics(this);
				metrics.start();
			} catch (IOException e) {
				// Failed to submit the stats :-(
			}
    	}else{
    		this.log.info("MCStats disabled.");
    	}
    }

    //
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        // make sure the sender is a Player before casting
        Player player = null;
        String playerName = null;
        if (sender instanceof Player) {
            player = (Player)sender;
            playerName = player.getName();
         } else {
            sender.sendMessage("You must be a player!");
            return false;
         }

        // implement /togglei and /toggleir command.
        boolean isToggleInvCommand = cmd.getName().equalsIgnoreCase("togglei");
        boolean isReverse = cmd.getName().equalsIgnoreCase("toggleir");

        if(isToggleInvCommand || isReverse){
            if(!player.hasPermission("toggle_inventory.toggle")){
            	outputError("You don't have permission to toggle inventory.", player);
                return true;
            }

            // help command
            if (args.length >= 1 && args[0].length() > 0) {
				if (args[0].startsWith("h")) {
					player.sendMessage("USAGE1: /ti - toggle inventory like a ring");
					player.sendMessage("USAGE2: /it - toggle inventory like a ring (reverse)");
					player.sendMessage("Advanced: /ti [enable|disable] gamemode - (you can toggle with gamemode)");
					return true;
				}
            }

            // gamemode option
            if(args.length >= 2 && args[0].length() > 0 && args[1].length() > 0){
				try {
					if (args[0].startsWith("e")) {
						if (args[1].startsWith("g")) {
							inventoryManager.setGameModeSaving(playerName, true);
							player.sendMessage("[ToggleInventory] Game mode toggle is enabled.");
						}
						return true;
					} else if (args[0].startsWith("d")) {
						if (args[1].startsWith("g")) {
							inventoryManager.setGameModeSaving(playerName, false);
							player.sendMessage("[ToggleInventory] Game mode toggle is disabled.");
						}
						return true;
					}
				} catch (IOException e) {
					outputError("Something went wrong! (gamemode enable option)", player);
				}
            }

            // toggle inventory (/ti or /it, and /ti [n])
            try {
            	if(inventoryManager.getSpecialInventoryUsingStatus(playerName)){
            		inventoryManager.restoreInventory(player);
            		inventoryManager.setSpecialInventoryUsingStatus(playerName, false);
            	}else if (args.length >= 1 && args[0].length() > 0) {
    				int index = Integer.parseInt(args[0]);
    				inventoryManager.toggleInventory(player, index);
    			}
    			else{
    				inventoryManager.toggleInventory(player, !isReverse);
    			}
				player.sendMessage(inventoryManager.makeInventoryMessage(player) + " inventory toggled.");
			} catch(NumberFormatException e){
				outputError("Index must be a number.", player);
			} catch (Exception e) {
				outputError(e.getMessage(), player);
			}
            return true;
        }

        // toggle special inventory (/tis or /tis [name])
        boolean isTogglelInvSpecialCommand = cmd.getName().equalsIgnoreCase("toggleis");
        boolean isTISReverse = cmd.getName().equalsIgnoreCase("toggleisr");
        if(isTogglelInvSpecialCommand || isTISReverse){
			if (!player.hasPermission("toggle_inventory.toggle_special")) {
				outputError("You don't have permission to toggle special inventories.", player);
				return true;
			}
			try {
				// implement /tis [add|delete] command
				if(args.length >= 1 && (args[0].equals("add") || args[0].equals("delete"))){
					String name = (args.length == 2) ? args[1] : null;
					if(name == null){
						player.sendMessage("USAGE: /tis [add|delete] [name]");
						return true;
					}
					if(args[0].equals("add")){
						inventoryManager.saveSpecialInventory(player, name);
						player.sendMessage(ChatColor.DARK_GREEN+String.format("Add %s to special inventories.", ChatColor.GREEN+name+ChatColor.DARK_GREEN));
					}
					else if(args[0].equals("delete")){
						inventoryManager.deleteSpecialInventory(inventoryManager.getInventoryFile(playerName), name);
						player.sendMessage(ChatColor.DARK_GREEN+String.format("Delete %s from special inventories.", ChatColor.GREEN+name+ChatColor.DARK_GREEN));
					}
					return true;
				}
				// implement /tis [add-default|delete-default] command
				else if(args.length >= 1 && (args[0].equals("add-default") || args[0].equals("delete-default"))){
					String name = (args.length == 2) ? args[1] : null;
					if(name == null){
						player.sendMessage("USAGE: /tis [add-deafult|delete-default] [name]");
						return true;
					}
					if(args[0].equals("add-default")){
						inventoryManager.saveSpecialInventory(player, name);
						player.sendMessage(ChatColor.DARK_GREEN+String.format("Add %s to default special inventories.", ChatColor.GREEN+name+ChatColor.DARK_GREEN));
					}
					else if(args[0].equals("delete-default")){
						inventoryManager.deleteSpecialInventory(inventoryManager.getDefaultSpecialInventoryFile(), name);
						player.sendMessage(ChatColor.DARK_GREEN+String.format("Delete %s from default special inventories.", ChatColor.GREEN+name+ChatColor.DARK_GREEN));
					}
					return true;
				}
				// implement /tis copy [n]
				else if(args.length >= 1 && args[0].equals("copy")){
					int destinationIndex = (args.length == 3) ? Integer.parseInt(args[2]) : -1;
					String spInvName = (args.length == 3) ? args[1] : null;
					if(destinationIndex > 0){
						inventoryManager.copySpInvToNormalInventory(player, spInvName, destinationIndex);
						if(destinationIndex == inventoryManager.getCurrentInventoryIndex(playerName)){
							inventoryManager.restoreInventory(player);
						}
						player.sendMessage(String.format(
								"'%s' was copied to '%s' successfully!",
								ChatColor.GREEN + spInvName + ChatColor.RESET,
								ChatColor.BOLD
										+ String.valueOf(destinationIndex)
										+ ChatColor.RESET));
						return true;
					}
					else{
						player.sendMessage("USAGE: /tis copy [special inventory name] [invenotry index]");
					}
					return true;
				}
				// implement /tis reset [-f]
				else if(args.length >= 1 && args[0].equals("reset")){
					boolean isForce = (args.length == 2) ? args[1].equals("-f") : false;
					if(isForce){
						inventoryManager.initializeSPInvFromDefault(playerName);
						player.sendMessage(ChatColor.GOLD + "All special inventory were reset!");
						return true;
					}
					else{
						player.sendMessage(ChatColor.GOLD + "WARNING: All special inventory will be reset by default.");
						player.sendMessage(ChatColor.GOLD + "If you want to continue operation, retype " + ChatColor.DARK_RED +  "'/tis reset -f'");
					}
					return true;
				}
				// implement /tis reset-default [-f]
				else if(args.length >= 1 && args[0].equals("reset-default")){
					boolean isForce = (args.length == 2) ? args[1].equals("-f") : false;
					if(isForce){
				    	saveResource(inventoryManager.getDefaultSpecialInventoryFile().getName(), true);
						player.sendMessage(ChatColor.GOLD + "Default special inventory were reset!");
						return true;
					}
					else{
						player.sendMessage(ChatColor.GOLD + "WARNING: Default special inventory will be reset by default.");
						player.sendMessage(ChatColor.GOLD + "If you want to continue operation, retype " + ChatColor.DARK_RED +  "'/tis reset-default -f'");
					}
					return true;

				}
				// implement /tis and /its command
				else {
					// TODO: FIRST USE
					if(inventoryManager.isFirstUseForToggleInventorySpecial(playerName)){
						inventoryManager.initializeSPInvFromDefault(playerName);
						inventoryManager.setSpecialInventoryUsingStatusForFirstUse(playerName, false);
					}
					if (args.length == 1 && args[0].length() > 0) {
						inventoryManager.toggleSpecialInventory(player, args[0]);
					} else {
						inventoryManager.toggleSpecialInventory(player, !isTISReverse);
					}
				}
				player.sendMessage(inventoryManager.makeSpecialInventoryMessage(player) + " inventory toggled.");
			} catch (Exception e) {
				outputError(e.getMessage(), player);
				e.printStackTrace();
			}
        }
        return true;
    }

    public void onDisable(){
    }

    private void outputError(String msg){
        getLogger().warning(msg);
    }

    private void outputError(String msg, CommandSender sender){
        sender.sendMessage(ChatColor.RED + "[ERROR] " + msg);
        outputError(msg);
    }
}
