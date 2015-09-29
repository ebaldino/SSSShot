package io.github.ebaldino.sssshot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SSFileAccessor {

	 public final SSSShot plugin;
	 private String fileName;
	 private File myFile;
	 private FileConfiguration fileConfiguration;
	  
	 public SSFileAccessor(SSSShot plugin, String fileName) {
		 if (plugin == null)
			 throw new IllegalArgumentException("plugin cannot be null");
		 this.plugin = plugin;
		 this.fileName = fileName;
		 File dataFolder = plugin.getDataFolder();
		 if (dataFolder == null)
			 throw new IllegalStateException();
		 this.myFile = new File(dataFolder, fileName);
	 }
	  
	 public void reloadConfig() {
		 fileConfiguration = YamlConfiguration.loadConfiguration(myFile);
	  
		 // Look for defaults in the jar
		 InputStream defConfigStream = plugin.getResource(fileName);
		 if (defConfigStream != null) {
			 YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
			 fileConfiguration.setDefaults(defConfig);
		 }
	 }
	  
	 public FileConfiguration getConfig() {
		 if (fileConfiguration == null) {
			 this.reloadConfig();
		 }
		 return fileConfiguration;
	 }
	  
	 public void saveConfig() {
		 if (fileConfiguration != null && myFile != null) {
			 try {
				 getConfig().save(myFile);
			 } catch (IOException ex) {
				 plugin.getLogger().log(Level.SEVERE, "Could not save config to " + myFile, ex);
			 }
		 }
	 }
	 public void saveDefaultConfig() {
		 if (!myFile.exists()) {
			 this.plugin.saveResource(fileName, false);
		 }
	 }

}
