package com.example.p2pfiletransfer;

import java.io.File;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
	private static final int REQUEST_WRITE_STORAGE_REQUEST_CODE = 10;
	public final int fileRequestID = 55;
	public final int port = 7950;


	private WifiP2pManager wifiManager;
	private Channel wifichannel;
	private BroadcastReceiver wifiServerReceiver;

	private IntentFilter wifiServerReceiverIntentFilter;

	private String path;
	private File downloadTarget;

	private Intent serverServiceIntent;

	private boolean serverThreadActive;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		requestAppPermissions();
		//Block auto opening keyboard
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		wifichannel = wifiManager.initialize(this, getMainLooper(), null);
		wifiServerReceiver = new WiFiServerBroadcastReceiver(wifiManager, wifichannel, this);

		wifiServerReceiverIntentFilter = new IntentFilter();
		;
		wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		//set status to stopped
		TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
		serverServiceStatus.setText(R.string.server_stopped);

		path = "/";
		downloadTarget = new File(path);

		serverServiceIntent = null;
		serverThreadActive = false;

		setServerFileTransferStatus("No File being transfered");

		registerReceiver(wifiServerReceiver, wifiServerReceiverIntentFilter);

/*        
        wifiManager.createGroup(wifichannel,  new WifiP2pManager.ActionListener()  {
    	    public void onSuccess() {
    	    	setServerFileTransferStatus("WiFi Group creation successful");

    	    	//Group creation successful
    	    }

    	    public void onFailure(int reason) {
    	    	setServerFileTransferStatus("WiFi Group creation failed");

    	    	//Group creation failed

    	    }
    	});    	
    	*/

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		wifiManager.discoverPeers(wifichannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
        		//set
				setServerFileTransferStatus("Peers searching");
			}

			@Override
			public void onFailure(int reasonCode) {
        		//
				setServerFileTransferStatus("Peers searching Failed");
			}
		});
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
       
    public void startFileBrowseActivity(View view) {
    	
        Intent clientStartIntent = new Intent(this, FileBrowser.class);
        startActivityForResult(clientStartIntent, fileRequestID);  
        //Path returned to onActivityResult
              
    }
      
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	if (resultCode == Activity.RESULT_OK && requestCode == fileRequestID) {
    		//Fetch result
    		File targetDir = (File) data.getExtras().get("file");
    		
    		if(targetDir.isDirectory())
    		{
    			if(targetDir.canWrite())
    			{
    				downloadTarget = targetDir;
	    	    	TextView filePath = (TextView) findViewById(R.id.server_file_path);
	    	    	filePath.setText(targetDir.getPath());
	    			setServerFileTransferStatus("Download directory set to " + targetDir.getName());
	    			
    			}
    			else
    			{
	    			setServerFileTransferStatus("You do not have permission to write to " + targetDir.getName());
    			}

    		}
    		else
    		{
    			setServerFileTransferStatus("The selected file is not a directory. Please select a valid download directory.");
    		}

        }
    }
    
    public void startServer(View view) {
    	
    	//If server is already listening on port or transfering data, do not attempt to start server service 
    	if(!serverThreadActive)
    	{
	    	//Create new thread, open socket, wait for connection, and transfer file 
	
	    	serverServiceIntent = new Intent(this, ServerService.class);
	    	serverServiceIntent.putExtra("saveLocation", downloadTarget);
	    	serverServiceIntent.putExtra("port", new Integer(port));
	    	serverServiceIntent.putExtra("serverResult", new ResultReceiver(null) {
	    	    @Override
	    	    protected void onReceiveResult(int resultCode, final Bundle resultData) {
	    	    	
	    	    	if(resultCode == port )
	    	    	{
		    	        if (resultData == null) {
		    	           //Server service has shut down. Download may or may not have completed properly. 
		    	        	serverThreadActive = false;	
		    	        	
		    	        			    	        	
		    	        	final TextView server_status_text = (TextView) findViewById(R.id.server_status_text);
		    	        	server_status_text.post(new Runnable() {
		    	                public void run() {
				    	        	server_status_text.setText(R.string.server_stopped);
		    	                }
		    	        	});	
		    	        	
		    	        			    	        			    	        	
		    	        }
		    	        else
		    	        {    	        	
		    	        	final TextView server_file_status_text = (TextView) findViewById(R.id.server_file_transfer_status);

		    	        	server_file_status_text.post(new Runnable() {
		    	                public void run() {
		    	                	server_file_status_text.setText((String)resultData.get("message"));
		    	                }
		    	        	});		    	   		    	        	
		    	        }
	    	    	}
	    	           	        
	    	    }
	    	});
	    		    		
	    	serverThreadActive = true;
	        startService(serverServiceIntent);
	
	    	//Set status to running
	    	TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
	    	serverServiceStatus.setText(R.string.server_running);
	    	
	    }
    	else
    	{
	    	//Set status to already running
	    	TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
	    	serverServiceStatus.setText("The server is already running");
    		
    	}
    }
    
    public void stopServer(View view) {
    		
    	
    	//stop download thread 
    	if(serverServiceIntent != null)
    	{
    		stopService(serverServiceIntent);
    	
    	}
       	
    }
     
    
    public void startClientActivity(View view) {
    	
    	stopServer(null);
        Intent clientStartIntent = new Intent(this, ClientActivity.class);
        startActivity(clientStartIntent);    		
    }   
    
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        //stopServer(null);
        //unregisterReceiver(wifiServerReceiver);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        stopServer(null);
        
        //stopService(serverServiceIntent);
        
        //Unregister broadcast receiver		
		try {
			unregisterReceiver(wifiServerReceiver);
		} catch (IllegalArgumentException e) {
			// This will happen if the server was never running and the stop
			// button was pressed.
			// Do nothing in this case.
		}      
    }
    
    
    
    public void setServerWifiStatus(String message)
    {
    	TextView server_wifi_status_text = (TextView) findViewById(R.id.server_wifi_status_text);
    	server_wifi_status_text.setText(message);	
    }
    
    public void setServerStatus(String message)
    {
    	TextView server_status_text = (TextView) findViewById(R.id.server_status_text_2);
    	server_status_text.setText(message);	
    }
    
    
    public void setServerFileTransferStatus(String message)
    {
    	TextView server_status_text = (TextView) findViewById(R.id.server_file_transfer_status);
    	server_status_text.setText(message);	
    }

	private void requestAppPermissions() {
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			return;
		}

		if (hasReadPermissions() && hasWritePermissions()) {
			return;
		}

		ActivityCompat.requestPermissions(this,
				new String[] {
						Manifest.permission.READ_EXTERNAL_STORAGE,
						Manifest.permission.WRITE_EXTERNAL_STORAGE
				}, REQUEST_WRITE_STORAGE_REQUEST_CODE); // your request code
	}

	private boolean hasReadPermissions() {
		return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
	}

	private boolean hasWritePermissions() {
		return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
	}
    
         
}
