package com.favekeeper.android;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

public class JabberService extends Service {
	
	public final static String ACTION_REFRESH_LIST = "ACTION_REFRESH_LIST";
	public final static String ACTION_RECEIVE_MESSAGE = "ACTION_RECEIVE_MESSAGE";
	
	public final static String CMD_UPDATE = "CMD_UPDATE";
	public final static String CMD_EXCEPTION = "[EXCEPTION]";
	public final static String CMD_HIDE = "[HIDE]";

	private JabberConfig config = new JabberConfig();
	private XMPPClient client;
	
	private Handler mHandler = new Handler();
	
	private Integer mStatus = 0; // 0 - offline; 1 - online
	
	private Observer observer = new Observer() {
		
		@Override
		public void update(Observable observable, Object data) {
			
			if(data != null && data instanceof String){
				String str = (String) data;
				Intent broadcastIntent = new Intent();
				if(str.startsWith(XMPPClient.MSG_HEAD_SYNC)){
					//---send a broadcast to inform the activity
					// that the file has been downloaded---
					
					broadcastIntent.setAction(ACTION_REFRESH_LIST);
					getBaseContext().sendBroadcast(broadcastIntent);
				}
			}
		}
	};

	public JabberConfig getConfig() {
		return config;
	}

	public void setConfig(JabberConfig config) {
		this.config = config;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    handleCommand(intent);
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}

	protected void handleCommand(Intent intent) {
		JabberConfig config2 = (JabberConfig) intent.getExtras().get("config");
		
		//get config
		if (mStatus == 0 || config == null || !config.equals(config2)){
				config = config2;
				if (config != null) {
					connect();
				} 
		}
	}

	public void connect() {

		if(client == null || client.getConnection() == null){
			client = new XMPPClient(this.config, this);
			
			String result = client.connect();
			if(result == null){
				//toast success message
				sendBroadcast(getString(R.string.connectSucess));
				
				mStatus = 1;
			}else{
				//toast failed reason
				sendBroadcast("[EXCEPTION] " + getString(R.string.connectFail, result));
				
				mStatus = 0;
			}
		}
	}

	public void sendBroadcast(int msgId){
		String msg = getString(msgId);
		sendBroadcast(msg);
	}
	
	public void sendBroadcast(String msg){
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_RECEIVE_MESSAGE);
			broadcastIntent.putExtra("msg", msg);
		getBaseContext().sendBroadcast(broadcastIntent);
	}
	
	public void updateFile(final String lastModifiedDate){
		mHandler.post(new Runnable() {
			public void run() {
					UnZipper unzipper = new UnZipper(lastModifiedDate, UpdateBookmarkFile.FILE_PATH,
							UpdateBookmarkFile.FILE_PATH);
					unzipper.addObserver(observer);
					unzipper.unzip();
			}
		});
		
	}
	
}
