package io.github.ebaldino.sssshot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class SSSShot extends JavaPlugin {
	
	private SSCommandExecutor cmdExec;
	private SSFileAccessor sssceneFile;
	
	@Override
    public void onEnable() {
		
		// Save the default folders and files if they don't exist		
		sssceneFile = new SSFileAccessor(this, "SStemplate.json", null);
		sssceneFile.saveDefaultConfig(); 			
		sssceneFile = new SSFileAccessor(this, "readme.txt", "scenes");
		sssceneFile.saveDefaultConfig(); 		
		sssceneFile = new SSFileAccessor(this, "chunky-core-1.3.5.jar", "lib");
		sssceneFile.saveDefaultConfig();
		sssceneFile = new SSFileAccessor(this, "JOCL-0.1.7.jar", "lib");
		sssceneFile.saveDefaultConfig();
		sssceneFile = new SSFileAccessor(this, "commons-math3-3.2.jar", "lib");
		sssceneFile.saveDefaultConfig();
		sssceneFile = new SSFileAccessor(this, "ppj99-1.0.1.jar", "lib");
		sssceneFile.saveDefaultConfig();
	
		
		// Set an executor for each command in plugin.yml
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
