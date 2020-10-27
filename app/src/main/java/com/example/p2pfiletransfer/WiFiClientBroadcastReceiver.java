package com.example.p2pfiletransfer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pInfo;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class WiFiClientBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private ClientActivity activity;

    public WiFiClientBroadcastReceiver(WifiP2pManager manager, Channel channel, ClientActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        activity.setClientStatus("Client Broadcast receiver created");

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                activity.setClientWifiStatus("Wifi Direct is enabled");
            } else {
                activity.setClientWifiStatus("Wifi Direct is not enabled");
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //This broadcast is sent when status of in range peers changes. Attempt to get current list of peers.

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(context, "No permission", Toast.LENGTH_SHORT).show();
                return;
            }
            manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {

                public void onPeersAvailable(WifiP2pDeviceList peers) {

                    activity.displayPeers(peers);

                }
            });
        	
        	//update UI with list of peers 
        	
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
        	
        	NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        	WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
        	WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        	
        	if(networkState.isConnected())
        	{
        		//set client state so that all needed fields to make a transfer are ready
        		
        		//activity.setTransferStatus(true);
        		activity.setNetworkToReadyState(true, wifiInfo, device);
        		activity.setClientStatus("Connection Status: Connected");
        	}
        	else
        	{
        		//set variables to disable file transfer and reset client back to original state

        		activity.setTransferStatus(false);
        		activity.setClientStatus("Connection Status: Disconnected");
        		manager.cancelConnect(channel, null);

        	}
        	//activity.setClientStatus(networkState.isConnected());
        	
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}