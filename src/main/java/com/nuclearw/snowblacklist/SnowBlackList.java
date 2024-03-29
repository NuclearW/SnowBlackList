package com.nuclearw.snowblacklist;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SnowBlackList extends JavaPlugin implements Listener {
	private static String mainDirectory = "plugins" + File.separator + "SnowBlackList";
	private static File configFile = new File(mainDirectory + File.separator + "config");
	private static File versionFile = new File(mainDirectory + File.separator + "VERSION");

	private Logger log = Logger.getLogger("Minecraft");

	private Properties prop = new Properties();

	private ArrayList<World> worlds = new ArrayList<World>();
	private ArrayList<Integer> blocks = new ArrayList<Integer>();

	public void onEnable() {
		new File(mainDirectory).mkdir();

		if(!versionFile.exists()) {
			updateVersion();
		} else {
			String vnum = readVersion();
			if(vnum.equals("0.1")) updateVersion();
		}

		if(!configFile.exists()) {
			try {
				configFile.createNewFile();
				FileOutputStream out = new FileOutputStream(configFile);
				prop.put("active-worlds", "");
				prop.put("blacklisted-blocks", "");
				prop.store(out, "Separate worlds and block values with commas.");
				out.flush();
				out.close();
				prop.clear();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		FileInputStream configIn;
		try {
			configIn = new FileInputStream(configFile);
			prop.load(configIn);
			configIn.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		String activeWorldsStr = prop.getProperty("active-worlds");
		if(activeWorldsStr.contains(",")) {
			String[] activeWorlds = activeWorldsStr.split(",");
			for(String w : activeWorlds) {
				this.worlds.add(getServer().getWorld(w));
			}
		} else {
			this.worlds.add(getServer().getWorld(activeWorldsStr));
		}

		if(prop.getProperty("blacklisted-blocks") != null) {
			String customBlocksStr = prop.getProperty("blacklisted-blocks");
			if(customBlocksStr.contains(",")) {
				String[] customBlocks = customBlocksStr.split(",");
				for(String b : customBlocks) {
					try {
						int blockid = Integer.parseInt(b);
						this.blocks.add(blockid);
					} catch (NumberFormatException ex) {
						log.severe("[SnowBlackList] Block ID: " + b + " could not be parsed!");
					}
				}
			} else {
				try {
					int blockid = Integer.parseInt(customBlocksStr);
					this.blocks.add(blockid);
				} catch (NumberFormatException ex) {
					log.severe("[SnowBlackList] Block ID: " + customBlocksStr + " could not be parsed!");
				}
			}
		}

		PluginManager pluginManager = getServer().getPluginManager();

		pluginManager.registerEvents(this, this);

		log.info("[SnowBlackList] version "+ this.getDescription().getVersion() +" loaded.");
	}

	public void onDisable() {
		log.info("[SnowBlackList] version "+ this.getDescription().getVersion() +" unloaded.");
	}

	@EventHandler
	public void onSnowForm(BlockFormEvent event) {
		BlockState snowTile = event.getNewState();
		if(snowTile.getType() != Material.SNOW) return;
		int x = snowTile.getX();
		int y = snowTile.getY() - 1;
		int z = snowTile.getZ();
		World world = snowTile.getWorld();
		if(!worlds.contains(world)) return;
		if(!blocks.contains(world.getBlockTypeIdAt(x, y, z))) return;
		event.setCancelled(true);
	}

	public void updateVersion() {
		try {
			versionFile.createNewFile();
			BufferedWriter vout = new BufferedWriter(new FileWriter(versionFile));
			vout.write(this.getDescription().getVersion());
			vout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		}
	}

	public String readVersion() {
		byte[] buffer = new byte[(int) versionFile.length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(versionFile));
			f.read(buffer);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (f != null) try { f.close(); } catch (IOException ignored) { }
		}

		return new String(buffer);
	}
}
