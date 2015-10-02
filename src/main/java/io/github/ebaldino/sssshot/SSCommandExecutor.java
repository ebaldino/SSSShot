package io.github.ebaldino.sssshot;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

public class SSCommandExecutor implements CommandExecutor {
	
	private final SSSShot plugin;
	
	public SSCommandExecutor(SSSShot plugin) {
		this.plugin = plugin; 
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		boolean rc = false;

		if (cmd.getName().equalsIgnoreCase("ss")) { 
			// If the user typed /ss, he's talking to us...

			if (args.length == 0 || args[0].toLowerCase().equals("help")) { 
				// "ss" with no args: list syntax for each and every command
				rc = true;
				sender.sendMessage("/ss click [DVD|HD|FULLHD]");

			} else {

				// ========================================================================
				// ADMIN commands are grouped first...
				// ========================================================================
				switch (args[0].toLowerCase()) {
					case "click": { 
						if (!(sender instanceof Player)) {
							rc = false;
							sender.sendMessage("You must be a player!");
						} else {
							
							String resolution;
							if ("dvd hd fullhd".contains(args[1])) {
								resolution = args[1].toUpperCase();
							} else {
								resolution = "DVD";
							}
							
							Player player = (Player) sender;
							try {
								rc = this.clickCmd(player, resolution);
							} catch (IOException e) {
								e.printStackTrace();
								rc = false;
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (!rc) sender.sendMessage("click command failed.");	
							break;
						}
					}
					default: 
					break;
				}					
			}		
		}
		return rc;		
	}

// =============================================================================================================================
	public Boolean clickCmd(Player player, String res) throws IOException, ParseException {
		
		SSScreenShot sshot = new SSScreenShot(plugin, player, "res");
		sshot.updateSceneTemplate();
		sshot.renderscene();
		
		return true;
	}	
	
}
