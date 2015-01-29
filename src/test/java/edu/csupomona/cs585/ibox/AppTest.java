package edu.csupomona.cs585.ibox;


import edu.csupomona.cs585.ibox.sync.FileSyncManager;
import edu.csupomona.cs585.ibox.sync.GoogleDriveFileSyncManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import static org.mockito.Mockito.*;



/**
 * Placeholder for unit test
 */
public class AppTest extends TestCase {
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    
    public static Test suite()
   {
       return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
 public void testApp() throws IOException
 {

     try
     {
    	 
    	 //Get the project directory path to perform operations
    	 String dir = System.getProperty("user.dir");
    	 
    	 //Mock FileSyncManager class
    	 FileSyncManager fileSyncManager = mock(GoogleDriveFileSyncManager.class);
         WatchDir watch = new WatchDir(Paths.get(dir), fileSyncManager);
     
        // Created a thread so could kill processEvents() since it runs into an infinite loop
         new MyThread(watch).start();
         	 
         //Add a new file
         File newFile= new File(dir+"/"+"newTestUnitFile.txt");
         
         //Update the file
         PrintWriter writer = new PrintWriter(newFile);
 		 writer.println("Updating file");
 		 writer.close();
 		 
 		 //Delete File
         newFile.delete();
       
         			
         //Verify all methods were called
         verify(fileSyncManager, atLeastOnce()).addFile(new File(dir+"/"+"newTestUnitFile.txt"));
         verify(fileSyncManager, atLeastOnce()).deleteFile(new File(dir+"/"+"newTestUnitFile.txt"));
         verify(fileSyncManager, atLeastOnce()).updateFile(new File(dir+"/"+"newTestUnitFile.txt"));
         	

         //Stop the thread
      	 MyThread.Stop();
      	

     }

     catch(Exception e)
     {
     }
}
}

//Thread to stop process events
 class MyThread extends Thread{

	 	public volatile static boolean stopThread = false;
	    WatchDir watchThread;

	    public MyThread(WatchDir watch){
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





 


