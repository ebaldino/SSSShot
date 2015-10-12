package io.github.ebaldino.sssshot;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

public class SSGoogleDrive {

	  private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	  private static String client_id = "533171702749-n3iceohplvvgagrdh9olaepc3jrbgs73.apps.googleusercontent.com";
	  private static String client_secret = "HWQ1DERzxFsv45wHT9ZTyBNw";
	  
	  private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	  private static HttpTransport HTTP_TRANSPORT;
	    
	  private static List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE); 
	  
	  static {
		  try {
			  HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		  } catch (Throwable t) {
			  t.printStackTrace();
			  System.exit(1);
		  }
	  }	


    public static Credential authorize() throws IOException {
		/**
		* Creates an authorized Credential object.
		* @return an authorized Credential object.
		* @throws IOException
		*/
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	    		HTTP_TRANSPORT, JSON_FACTORY, client_id, client_secret, SCOPES)
		        .setAccessType("online")
		        .setApprovalPrompt("auto").build();

	    String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
	    
	    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "debug 1");
	    
	    // Authorize
	    GoogleTokenResponse response = null;
		try {
			response = flow.newTokenRequest(url).setRedirectUri(REDIRECT_URI).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);
		
	    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "debug 2");  
	    
	    return credential;
    }
	

    public static Drive getDriveService() throws IOException {
		/**
		 * Build and return an authorized Drive client service.
		 * @return an authorized Drive client service
		 * @throws IOException
		 */
    	Credential credential = authorize();
	    return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
    }
    
    
	public Boolean sendFileToGD (String fullfname) throws IOException {
		
		Boolean rc = false;

        // Build a new authorized API client service.
        Drive service = getDriveService();		
		
	    // Insert file
	    File body = new File();
	    body.setTitle(fullfname);
	    body.setDescription("SSSShot generated image");
	    
	    java.io.File fileContent = new java.io.File(fullfname);
	    FileContent mediaContent = new FileContent("SSSShot generated image", fileContent);
	    
	    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Attempting to upload " + fullfname);
	    
	    // Upload
		try {
			File file = service.files().insert(body, mediaContent).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "File uploaded successfully ");
		
		rc = true;
		return rc;
	}
	
	
}
