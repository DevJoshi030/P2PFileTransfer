package com.example.p2pfiletransfer;

import java.io.File;
import java.util.ArrayList;

import android.Manifest;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;


public class FileBrowser extends Activity {

	private static final int REQUEST_CODE = 1;
	private String root;
	private String currentPath;
	
	private ArrayList<String> targets;
	private ArrayList<String> paths;

	
	private File targetFile;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        getActionBar().setDisplayHomeAsUpEnabled(true);
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
              
        root = "/storage/emulated/0/";

        currentPath = root;
        
        targets = null;
        paths = null;
        
        targetFile = null;

        showDir(currentPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_file_browser, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    public void selectDirectory(View view) {
    	
	     File f = new File(currentPath);
	     targetFile = f;
	     
	     //Return target File to activity
		  returnTarget();


    }
    

    public void setCurrentPathText(String message)
    {
    	TextView fileTransferStatusText = (TextView) findViewById(R.id.current_path);
    	fileTransferStatusText.setText(message);	
    }


	private void showDir(String targetDirectory){
		
		setCurrentPathText("Current Directory: " + currentPath);
		
		targets = new ArrayList<String>();
		paths = new ArrayList<String>();
				
	     File f = new File(currentPath);
//	     f = Environment.getExternalStorageDirectory();
	     File[] directoryContents = f.listFiles();
	     
	     
		if (!targetDirectory.equals(root))

		{
			targets.add(root);
			paths.add(root);
			targets.add("../");
			paths.add(f.getParent());
		}

		if (directoryContents != null) {
			for (int i = 0; i < directoryContents.length; i++) {
				File target = directoryContents[i];
				paths.add(target.getPath());

				if (target.isDirectory()) {
					targets.add(target.getName() + "/");
				} else {
					targets.add(target.getName());

				}

			}
		}
		
		ListView fileBrowserListView = (ListView) findViewById(R.id.file_browser_listview);

	    ArrayAdapter<String> directoryData = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, targets);
	    fileBrowserListView.setAdapter(directoryData);
	    
	    
	    
	    
	    fileBrowserListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int pos,long id) {
				
				  File f = new File(paths.get(pos));
				  
				  if(f.isFile())
				  {
					  targetFile = f;
					  returnTarget();
					  //Return target File to activity
				  }
				  else
				  {
					  //f must be a dir
					  if(f.canRead())
					  {
						  currentPath = paths.get(pos);
						  showDir(paths.get(pos));
					  }
					  
				  }

				
			}			
				// TODO Auto-generated method stub				
			});
	    
	    /*
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("WiFi Direct File Transfer");
		*/
	   
		
	}
	
	public void returnTarget()
	{
		
		Intent returnIntent = new Intent();
		returnIntent.putExtra("file", targetFile);
		setResult(RESULT_OK, returnIntent);
		finish();
		
	}
	
	

}



