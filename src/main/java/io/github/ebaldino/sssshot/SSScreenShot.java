package io.github.ebaldino.sssshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SSScreenShot {

    public final SSSShot plugin;
    private Player player;
    private String playeruuid;
    private String sep;
    private String basepath; 
    private String templatefile;	
    private String jsonfile;
    private Integer sheight;
    private Integer swidth;


    public SSScreenShot(SSSShot plugin, Player player, String resolution) {
	this.plugin = plugin;
	this.player = player;

	this.playeruuid = player.getUniqueId().toString();
	this.sep = File.separator;
	this.basepath = plugin.getDataFolder().getAbsolutePath().toString().replace(" ", "\\ ");
	this.templatefile = basepath +  sep + "SStemplate.json";	
	this.jsonfile = basepath +  sep + "scenes" + sep + playeruuid + ".json";
	//worldpath = Bukkit.getServer().getWorld("world").getWorldFolder().getPath();
	//basepath = worldpath + File.separator + ".." + File.separator + "plugins" + File.separator + "SSSShot";
	switch (resolution.toUpperCase()) {
	case "DVD": {
	    sheight = 480;
	    swidth = 720;
	    break;
	}
	case "HD": {
	    sheight = 720;
	    swidth = 1280;
	    break;
	}
	case "FULLHD": {
	    sheight = 1080;
	    swidth = 1920;
	    break;
	}
	default: {
	    sheight = 480;
	    swidth = 720;
	    break;
	}			
	}
    }

    // =============================================================================================================================	
    public String chunkList(String type) {
	// This will make a list of all the chunks needed to assemble the screenshot
	// The list is formatted for use in the scene file
	// There's no need to use view angles; default FOV is 70 degrees but may be defined up to 180 degrees, so we just do a rectangular area based on view distance and get all the possible chunks
	// view distance is the number of chunks rendered to all directions from the player position, plus the chunk he is in; so viewdist = 5 means 11x11 chunks
	// type = direct will produce a chunk list of actual X,Z coordinates
	// type = relative will produce a chunk list that counts the chunks from 0,0 to each chunk

	String nl = System.getProperty("line.separator");
	String chlist = "";
	Integer step = 1;

	// define the corner coordinates for the are we want to grab
	Integer blockdist = Bukkit.getViewDistance() * 16;

	World world = player.getWorld(); 
	Integer minX = player.getLocation().getBlockX() - blockdist;
	Integer maxX = player.getLocation().getBlockX() + blockdist;
	Integer minZ = player.getLocation().getBlockZ() - blockdist;
	Integer maxZ = player.getLocation().getBlockZ() + blockdist;

	// define the initial chunk corners
	Integer minchunkX = world.getChunkAt(minX, minZ).getX();
	Integer minchunkZ = world.getChunkAt(minX, minZ).getZ();		
	Integer maxchunkX = world.getChunkAt(maxX, maxZ).getX();
	Integer maxchunkZ = world.getChunkAt(maxX, maxZ).getZ();


	//TO DO: ONLY NEED TO GET THE CHUNKS IN FRONT OF THE PLAYER!!!!

	// assemble chunk list according to requested type:
	switch (type) {
	case "direct": { 
	    step = 16;
	    break;
	}
	case "relative": { 
	    step = 1;
	    minchunkX = minchunkX / 16;
	    minchunkZ = minchunkZ / 16;		
	    maxchunkX = maxchunkX / 16;
	    maxchunkZ = maxchunkZ / 16;
	    break;
	}
	default: {
	    step = 1;
	    minchunkX = minchunkX / 16;
	    minchunkZ = minchunkZ / 16;		
	    maxchunkX = maxchunkX / 16;
	    maxchunkZ = maxchunkZ / 16;
	    break;
	}
	}

	for(Integer i = minchunkX; i <= maxchunkX; i = i + step) {
	    for(Integer j = minchunkZ; j <= maxchunkZ; j = j + step) {
		chlist = chlist + "," + nl + "[" + nl + i + "," + nl + j + nl + "]";
	    }
	}

	chlist = "[" + chlist.substring(1) + "]";
	//Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "chlist = " + chlist);
	return chlist;
    }


    // =============================================================================================================================	
    @SuppressWarnings("unchecked")
    public void updateSceneTemplate() throws IOException, ParseException {
	// This will read the scene file template and update it so it's ready to be copied to a new user scene file

	JSONParser jparser = new JSONParser();

	//	try {
	// get the contents of the template file into a json object
	FileReader jfilein = new FileReader(templatefile);
	Object jobj = jparser.parse(jfilein);
	JSONObject jsonobj = (JSONObject) jobj;
	jfilein.close();

	// change whatever we need in the template's content
	//Date date = new Date();
	//SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	//String ssname = playeruuid + "_" + sdf.format(date); 
	Double light = 2.0;
	Double camx = player.getLocation().getX();
	Double camy = player.getEyeLocation().getY();
	Double camz = player.getLocation().getZ();
	Double camroll = 0.0;
	Float campitch = (float) (player.getEyeLocation().getPitch() * -0.01745329 + 1.570796);  // pitch adusted by to fall between 0 and PI - use y = ax + b with a= 0.01745329 and b= 1.570796 
	Float camyaw = (float) ((player.getEyeLocation().getYaw() +90) * (-6.28 / 360)); // yaw adusted by adding 90 and multiplying by 6/360  			

	// the world path
	String worldpath = Bukkit.getServer().getWorld(player.getWorld().getName()).getWorldFolder().getAbsolutePath();
	worldpath = worldpath.replaceAll("\\\\","\\\\\\\\"); 
	JSONObject world = (JSONObject) jsonobj.get("world");
	world.put("path", worldpath);

	// the camera position and orientation:
	JSONObject camera = (JSONObject) jsonobj.get("camera"); 

	JSONObject camerapos = (JSONObject) camera.get("position"); 
	camerapos.clear();
	camerapos.put("x", camx);
	camerapos.put("y", camy);
	camerapos.put("z", camz);		

	JSONObject cameraorient = (JSONObject) camera.get("orientation"); 
	cameraorient.clear();
	cameraorient.put("roll", camroll);
	cameraorient.put("pitch", campitch);
	cameraorient.put("yaw", camyaw);			
	jsonobj.put("camera", camera);

	// the skylight
	JSONObject sky = (JSONObject) jsonobj.get("sky");
	sky.put("skyLight", light);
	jsonobj.put("sky", sky);

	// the chunks
	String chunkstr = "{\"chunkList\": " + this.chunkList("relative") +"}";
	JSONObject jchunks = (JSONObject) jparser.parse(chunkstr);			
	jsonobj.putAll(jchunks);

	// assorted elements
	jsonobj.put("name", playeruuid);
	jsonobj.put("height", sheight);
	jsonobj.put("width", swidth);
	jsonobj.put("sppTarget", 10);
	jsonobj.put("pathTrace", false);

	// write out the new file
	FileWriter jfileout = new FileWriter(jsonfile);
	//try{
	jfileout.write(JsonFormatter.format(jsonobj));
	jfileout.flush();
	jfileout.close();
	//} catch (Exception e) {
	//	Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "JSON file output error");
	//}							

	//	} catch (Exception e) {
	//		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "JSON I/O error");
	//	}

	//delete the temporary files in the scene directory:
	String scenepath = basepath +  sep + "scenes";
	Path deletepath; 
	deletepath = FileSystems.getDefault().getPath(scenepath, playeruuid +  ".dump");
	deletefile(deletepath);
	deletepath = FileSystems.getDefault().getPath(scenepath, playeruuid +  ".octree");
	deletefile(deletepath);
    }	

    // =============================================================================================================================	
    public void renderscene() {
	// call the external renderer

	//basepath = "." + sep + basepath;
	//String javapath = System.getProperty("java.home") + sep;
	String libpath = basepath +  sep + "lib" + sep;	
	String scenepath = basepath +  sep + "scenes";

	String rendercmd = "-Xmx1024m -classpath " + libpath + "chunky-core-1.3.5.jar;" + libpath + "commons-math3-3.2.jar;" + libpath + "JOCL-0.1.7.jar;" + libpath + "ppj99-1.0.1.jar se.llbit.chunky.main.Chunky -render ";
	rendercmd = rendercmd + playeruuid + " -scene-dir " + scenepath;		

	try {
	    runProc(rendercmd, basepath + sep + "tempfile");
	} catch (Exception e) {
	    e.printStackTrace();
	}			
    }

    // =============================================================================================================================	
    public void runProc(String cmd, String directory) throws InterruptedException,IOException {
	//String javapath = System.getProperty("java.home") + sep;
	//ProcessBuilder pb = new ProcessBuilder(cmd);
	//pb.directory(new File(directory));
	//pb.start();
	StringBuffer output = new StringBuffer();
	Process proc;
	String cmdoutput = "";

	try {
	    String os = System.getProperty("os.name").toLowerCase();
	    if (os.contains("win")) {		
		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "SSSShot: Running windows command" + cmd);
		proc = Runtime.getRuntime().exec("cmd /c start /min /separate /low java " + cmd);
		proc.waitFor();
	    } else {
		// Unix uses : for classpath delimiters
		cmd = cmd.replace(";", ":");
		List<String> params = new ArrayList<String>();
		params.add("/usr/bin/java");
		params.addAll(Arrays.asList(cmd.split(" ")));
		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "SSSShot: Running command " + params);
		final ProcessBuilder pb = new ProcessBuilder(params);

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

		    @Override
		    public void run() {
			StringBuffer output = new StringBuffer();
			String cmdoutput = "";
			Process p;
			try {
			    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "SSSShot: Starting render");
			    p = pb.start();
			    int exitCode = p.waitFor();
			    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "SSSShot: exit code = " + exitCode);
			    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			    while ((cmdoutput = reader.readLine())!= null) {
				output.append(cmdoutput + "\n");
			    }				
			} catch (Exception e) {
			    e.printStackTrace();
			    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "SSSShot: Render failed");
			}
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Command output: " + cmdoutput);
		    }});

	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "SSSShot: Render failed");
	}
		

    }

    // =============================================================================================================================	
    public void deletefile(Path deletepath) {
	try {
	    Files.deleteIfExists(deletepath);
	} catch (IOException | SecurityException e) {
	    e.printStackTrace();
	}

    }
}
