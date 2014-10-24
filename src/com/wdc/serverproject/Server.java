package com.wdc.serverproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class Server extends ActionBarActivity {
	private ServerSocket mServerSocket;
	private Socket mNintenbroSocket;
	Handler updateConversationHandler;
	
	// adb will have the first emulator launcher on port 5554 of the localhost
	// second emulator will be port 5556
	
	// telnet into the server emulator
	// redir add tcp:5000:6000
	
	// Port to open on the emulator's IP
	public static final int SERVERPORT = 6000;
	
	// 10.0.2.2:5000 is the alias for the localhost
	private static final int NINTENBRO_PORT = 5000;
    private static final String NINTENBRO_IP = "10.0.2.2";
    
    private static final boolean mServerModeFlag = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        if ( mServerModeFlag == false ) {
            
	        // Start the client thread to open a socket
	        new Thread( new ClientThread() ).start();
        
        }
        else {
        	
        	updateConversationHandler = new Handler();
        
	        // Start listening on the server thread
	        new Thread( new ServerThread() ).start();
        
        }
        
    }
    
    @Override
	protected void onStop() {
		super.onStop();
		
		try {
			if ( mServerModeFlag == false )
				mNintenbroSocket.close();
			else
				mServerSocket.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
    
    class ServerThread implements Runnable {

		public void run() {
			Socket socket = null;
			
			try {
				mServerSocket = new ServerSocket(SERVERPORT);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			
			while ( !Thread.currentThread().isInterrupted() ) {

				try {
					socket = mServerSocket.accept();
					
					InputCommunicationThread commThread = new InputCommunicationThread(socket);
					new Thread(commThread).start();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
	} // end class ServerThread
    
    class ClientThread implements Runnable {
    	
    	@Override
    	public void run() {
    		
    		try {
    			InetAddress nintenbroAddress = InetAddress.getByName(NINTENBRO_IP);
    			mNintenbroSocket = new Socket(nintenbroAddress, NINTENBRO_PORT);
    			Log.v("ServerProject", "Opened socket to Nintenbro");
    		}
    		catch (Exception e) {
    			Log.v("ServerProject", "Socket to Nintenbro failed");
    			e.printStackTrace();
    		}
    		
    	}
    	
    } // end class ClientThread
    
    public void sendItem(View view) {
    	
    	try {
    		Log.v("ServerProject", "send item view id " + view.getId());
    		Toast.makeText(getApplicationContext(), "send item", Toast.LENGTH_SHORT).show();
    		PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter( mNintenbroSocket.getOutputStream() ) ), true );
    	
    		switch( view.getId() ){
	        	case R.id.mushroombutton :
	        		out.println("receive mushroom");
	        		break;
	        	case R.id.redshellbutton :
	        		out.println("receive redshell");
	        		break;
	        	case R.id.bananabutton :
	        		out.println("receive banana");
	        		break;
	    	}
    	
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
		
    }
    
    class InputCommunicationThread implements Runnable {
		private Socket clientSocket;
		private BufferedReader input;

		public InputCommunicationThread(Socket clientSocket) {

			this.clientSocket = clientSocket;

			try {
				this.input = new BufferedReader( new InputStreamReader( this.clientSocket.getInputStream() ) );
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void run() {

			while ( !Thread.currentThread().isInterrupted() ) {

				try {
					String read = input.readLine();
					updateConversationHandler.post( new updateUIThread(read) );
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		}

	} // end class Communication Thread
    
    class updateUIThread implements Runnable {
		private String msg;

		public updateUIThread(String str) {
			this.msg = str;
		}

		@Override
		public void run() {
			Toast.makeText(getApplicationContext(), "Client Says: "+ msg, Toast.LENGTH_SHORT).show();
		}
		
	} // end class updateUIThread

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
} // end class Server
