package io.github.ebaldino.sssshot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

public class SSCommandExecutor implements CommandExecutor {
	
	private final SSSShot plugin;
	private HashMap<String, String[]> filesToProcess; 
	
	public SSCommandExecutor(SSSShot plugin) {
		this.plugin = plugin; 
		this.filesToProcess = plugin.getFileToProcess();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		boolean rc = false;

		if (cmd.getName().equalsIgnoreCase("ss")) { 
			// If the user typed /ss, he's talking to us...

			if (args.length == 0 || args[0].toLowerCase().equals("help")) { 
				// "ss" with no args: list syntax for each and every command
				rc = true;
				sender.sendMessage("/ss click r:<DVD|HD|FULLHD> i:<iterations> t:<texturepack>");

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
							
							// Make sure we have enough arguments to go around:
							String argres = "dvd";
							String argspp = "30";
							String argtex = null;
							if (!String.join("", args).contains("r:")) sender.sendMessage("No resolution parameter, using default of DVD.");
							if (!String.join("", args).contains("i:")) sender.sendMessage("No iterations (spp) parameter, using default of 30.");
							if (!String.join("", args).contains("t:")) sender.sendMessage("No texture pack parameter, using default.");
							
							for (String arg : args) {
								String argtype = arg.toLowerCase().substring(0,2);
								switch (argtype) {
									case "cl": {									
										break;
									}
									case "r:": {
										argres = arg.substring(2);									
										break;
									}
									case "i:": {
										argspp = arg.substring(2);
										break;
									}
									case "t:": {
										argtex = arg.substring(2);
										break;
									}									
									default: {
										sender.sendMessage("Argument not recognized: " + arg);
									}
									break;
								}
							}							

							// process the arguments
							// resolution....
							String resolution;
							if ("dvd hd fullhd".contains(argres.toLowerCase())) {
								resolution = argres.toUpperCase();
							} else {
								sender.sendMessage("Invalid resolution argument, using DVD.");
								resolution = "DVD";
							}
							
							// sppTarget...
							Integer sppTarget;
							if (checkVartype(argspp).equals("int")) {
								sppTarget = Integer.valueOf(argspp);
								if (sppTarget<0) sppTarget = sppTarget *-1;
								if (sppTarget>30000) sppTarget = 30000;
							} else {
								sender.sendMessage("Invalid sppTarget argument, using 30.");
								sppTarget = 30;
							}
							
							// texture pack
							String texFile;
							if (argtex == null || isTexturefile(argtex)) {
								texFile = argtex;
							} else {
								sender.sendMessage("Invalid texturepack argument (don't use any extensions), using default.");
								texFile = null;
							}

							// Run the actual "click" command
							Player player = (Player) sender;
							try {
								rc = this.clickCmd(player, resolution, sppTarget, texFile);
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
	public Boolean clickCmd(Player player, String res, Integer spp, String texture) throws IOException, ParseException {
		
		// Generate Screenshot
		SSScreenShot sshot = new SSScreenShot(plugin, player, res, spp, texture);
		sshot.updateSceneTemplate();
		sshot.renderscene();
		
		
		return true;
	}	

// =============================================================================================================================	
	public String checkVartype (String checkvar) {
		// This takes a string and returns the type of the data it contains: "int", "str" or "dbl"
		try {
			double dblvar = 0;
			dblvar = dblvar + Double.valueOf(checkvar);				
			if (checkvar.matches("\\-?\\d+")){    //optional minus and at least one digit
				return "int";
			} else {
				return "dbl";
			}
		} catch (Exception e) {
			return "str";
		}
	}	

// =============================================================================================================================	
	public Boolean isTexturefile (String fname) {
		// This checks that fname corresponds to an actual file in the texture directory
		String filepath = plugin.getDataFolder().getAbsolutePath().toString().replace(" ", "\\ ") + File.separator + "textures" + File.separator + fname + ".zip";
		File texfile = new File(filepath);
		return texfile.exists();		
	}		
	

// =============================================================================================================================	
    public void checkForFile() {
    	// This will run a timed process to check (every  10 seconds) for the presence of .png files in the Scenes directory and decide what to do with them
    	
		Integer repeatseconds = 10;
		
    	new Timer().schedule(new TimerTask() {
    		   		
		    @Override
		    public void run() {
		    	
		    	Boolean procOK;
		    	String[] hashvalue;
		    	
	    		 Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Running scheduled task.");

		    	for (String fullfname : filesToProcess.keySet()) {
		    		
		    		hashvalue = filesToProcess.get(fullfname);
		    		procOK = false;
		    		
		    		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Processing files hash: " + fullfname + " -- " + hashvalue[0]);
		    		
		    		switch(hashvalue[0]) {
			    		case ("sendToGD"): {
			    			SSGoogleDrive gd = new SSGoogleDrive();
			    			try {
								procOK = gd.sendFileToGD(fullfname);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}		    			
			    			break;
			    		}
			    		default:
			    			break;
		    		}		    		

		    		if (procOK) {
		    			// Processed the file OK, remove it from the hashmap
			    		filesToProcess.remove(fullfname);
			    		Player player = Bukkit.getServer().getPlayer((UUID.fromString(hashvalue[1])));
			    		player.sendMessage("Your screenshot was sent to Google Drive");
			    					    		
			    	}		    	
		    	}		    	

		    } // run()		    
    	}, 0, repeatseconds * 1000 ); // end Timer() - } ms_to_delay, ms_til_repeat);	

    }    	
	
	
}
