package io.github.ebaldino.sssshot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class SSSShot extends JavaPlugin {
	
	private SSCommandExecutor cmdExec;
	private SSFileAccessor sssceneFile;
	
	@Override
    public void onEnable() {
		
		// Save the default folder and files if they don't exist		
		sssceneFile = new SSFileAccessor(this, "SStemplate.json");
		sssceneFile.saveDefaultConfig(); 
		
		// Set an executor for each command in plugin.yml
		// This will throw a NullPointerException if you don't have the command defined in your plugin.yml file!
        cmdExec = new SSCommandExecutor(this);
		this.getCommand("ss").setExecutor(cmdExec);
		
			
		// Done, issue "enabled" message
		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Enabled SSSShot");
    }
 
    @Override
    public void onDisable() {
		// Done, issue "disabled" message
		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Disabled SSSShot");
    }
}
