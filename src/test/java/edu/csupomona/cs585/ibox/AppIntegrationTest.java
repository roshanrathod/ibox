package edu.csupomona.cs585.ibox;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.FileList;
import edu.csupomona.cs585.ibox.sync.FileSyncManager;
import edu.csupomona.cs585.ibox.sync.GoogleDriveFileSyncManager;


public class AppIntegrationTest {
	
	static Drive googleDriveClient;
		
	//Initializes google drive service account
	@BeforeClass
	public static void before() throws IOException {
		        
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();

		try{
		    GoogleCredential credential = new  GoogleCredential.Builder()
		    									.setTransport(httpTransport)
		    									.setJsonFactory(jsonFactory)
		    									.setServiceAccountId("1005000173774-0jrohgud65kge93nc96e22t1sef6vnh1"
		    											+ "@developer.gserviceaccount.com")
		    									.setServiceAccountScopes(Collections.singleton(DriveScopes.DRIVE))
		    									.setServiceAccountPrivateKeyFromP12File(new File("ibox-87fe3bf1bffb.p12"))
		    									.build();

		     googleDriveClient = new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName("ibox").build();
		        
		}
		catch(GeneralSecurityException e){
		            e.printStackTrace();
		        }
		        

		   }
	
		

	@Test
	public void test() throws IOException {
		
				
		//Get the project directory path to perform operations
   	 	String dir = System.getProperty("user.dir");
		
   	 	//Assigned the service account instance to FileSyncmanager
		FileSyncManager fileSyncManager = new GoogleDriveFileSyncManager(googleDriveClient);
		
		//Watching the project directory
		WatchDir watch = new WatchDir(Paths.get(dir), fileSyncManager);
		
		//Thread to run and stop processEvents(watching directory) 
		new Watcher(watch).start();
		 
		 		
		 //Add a new file to the directory
		File newFile = new File(dir+"/"+"newTestFile.txt");
		assertTrue(checkFileExistsInDrive(newFile.getName()));
			
		//Renaming the file to check updateFile
		File newFileName = new File(dir+"/"+"newFileName.txt"); 
		
		Path source = newFile.toPath();
            try {
                Files.move(source, source.resolveSibling(newFileName.getName()));
            } 
            catch (IOException e) {
               
            }
            
            assertTrue(checkFileExistsInDrive(newFileName.getName()));

            
            //Delete the file from Drive
            newFileName.delete();
            assertTrue(checkFileExistsInDrive(newFileName.getName()));
 
            //stop the thread
            Watcher.Stop();
 		
	}


	
		//Check if the uploaded file exists in drive and return true if it does
		public boolean checkFileExistsInDrive(String fileName) {
			try {
			List request = googleDriveClient.files().list();
			FileList files = request.execute();
			for(com.google.api.services.drive.model.File file : files.getItems()) {
				//check if the file added and file in drive's name match
						if (file.getTitle().equals(fileName)) {
							return true;
						}
					}
				} 
			catch (IOException e) {
			System.out.println("An error occurred: " + e);
				}
		  return true;
		}

}//End class


//Thread to run processEvents for watchdir
class Watcher extends Thread{

 	public volatile static boolean stopThread = false;
    WatchDir watchThread;

    public Watcher(WatchDir watch){
    	watchThread = watch; 	
    }
    
    public static void Stop() {
    	stopThread = true;
    }

	@Override
	public void run() {
		while(!stopThread){
			watchThread.processEvents();
	}
	}       

}

