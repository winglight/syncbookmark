package com.favekeeper.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;

import com.favekeeper.android.model.BookmarkModel;
import com.favekeeper.android.model.RootModel;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SyncbookmarkActivity extends Activity {

	private IntentFilter intentFilter;

	private List<BookmarkModel> bookmarkList = new ArrayList<BookmarkModel>();

	private TreeViewList list;

	private SimpleStandardAdapter dropAdapter;
	
	private TreeStateManager<String> manager = null;
	
	private SettingsDialog mDialog;
	private Handler mHandler = new Handler();
	
	private TextView statusTxt;
	private LinearLayout statusPanel;
	
	private String account;
	private String password;
	private JabberConfig config = new JabberConfig();

	private String TAG = "SyncbookmarkActivity";
	
	private static final int LEVEL_NUMBER = 5;
	
	private static int sequence = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		statusTxt = (TextView) findViewById(R.id.statusTxt);
		statusPanel =  (LinearLayout) findViewById(R.id.statusPanel);

		list = (TreeViewList) findViewById(R.id.bookmarkList);

		list.setCacheColorHint(0x00000000);
		
			if(!loadPref()){
				showSettingDialog();
			}else{

				startService();
			}
			
                reloadBookmarkFile();
		

		
	}

	@Override
	protected void onStop() {
//		stopService(new Intent(getBaseContext(), JabberService.class));
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		// ---intent to filter for file downloaded intent---
		intentFilter = new IntentFilter();
		intentFilter.addAction(JabberService.ACTION_REFRESH_LIST);
		intentFilter.addAction(JabberService.ACTION_RECEIVE_MESSAGE);
		// ---register the receiver---
		registerReceiver(intentReceiver, intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		// ---unregister the receiver---
		unregisterReceiver(intentReceiver);
	}
	
	
	@Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_item_account) {
        	showSettingDialog();
        } else if (item.getItemId() == R.id.menu_item_about) {
        	Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClass(SyncbookmarkActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;
        }else if(item.getItemId() == R.id.menu_item_share){
        	Intent intent=new Intent(Intent.ACTION_SEND);  
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSubject));  
			intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareBody));  
			startActivity(intent);  
        }else {
            return false;
        }
        return true;
    }

	public void startService() {
		loadConfig();
		
		// startService(new Intent(getBaseContext(), MyService.class));
		// OR
		// startService(new Intent(“net.learn2develop.MyService”));
		Intent intent = new Intent(getBaseContext(), JabberService.class);
		intent.putExtra("config", config);
		startService(intent);
	}

//	public void stopService() {
//		stopService(new Intent(getBaseContext(), JabberService.class));
//	}

	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(JabberService.ACTION_REFRESH_LIST)){
				showStatusPanel(true);
			//refresh list
				reloadBookmarkFile();
			}else if(intent.getAction().equals(JabberService.ACTION_RECEIVE_MESSAGE)){
				String msg = intent.getStringExtra("msg");
				
				if(msg.startsWith(JabberService.CMD_EXCEPTION)){
					Toast.makeText(getBaseContext(), msg,
							Toast.LENGTH_LONG).show();
					showStatusPanel(false);
				}else if(msg.startsWith(JabberService.CMD_HIDE)){
					showStatusPanel(false);
				}else{
					Toast.makeText(getBaseContext(), msg,
							Toast.LENGTH_LONG).show();
					statusTxt.setText(msg);
				}
			}
		}
	};
	
	public void showStatusPanel(boolean flag){
		if(flag){
			statusPanel.setVisibility(View.VISIBLE);
		}else{
			statusPanel.setVisibility(View.GONE);
			statusTxt.setText(R.string.loadingBookmark);
		}
	}
	
	public void showSettingDialog(){
		// Dialog for getting the xmpp settings
		if(mDialog == null){
			mDialog = new SettingsDialog(SyncbookmarkActivity.this);
		}

		// Set a listener to show the settings dialog
		mHandler.post(new Runnable() {
			public void run() {
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			    lp.copyFrom(mDialog.getWindow().getAttributes());
			    lp.width = WindowManager.LayoutParams.FILL_PARENT;
			    lp.height = WindowManager.LayoutParams.FILL_PARENT;
			    mDialog.show();
			    mDialog.getWindow().setAttributes(lp);
			}
		});
	}

	private boolean loadPref() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		account = prefs.getString("account", null);
		password = prefs.getString("password", null);
		return account != null && password != null;
	}

	public void saveConfig(String account, String password) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString("account", account);
		editor.putString("password", password);
		this.account = account;
		this.password = password;
		// editor.putString("uid", config.getUsername());
		editor.commit();
	}

	private void loadConfig() {
		if (config == null) {
			config = new JabberConfig();
		}

		// String uid = prefs.getString("uid", "");
		config.setUsername(account);
		config.setPassword(password);
		config.setUid(Settings.Secure.getString(this.getContentResolver(),
				Settings.Secure.ANDROID_ID));
	}

	private void loadBookmark() {
		try {
			String filename = UpdateBookmarkFile.FILE_PATH
					+ UpdateBookmarkFile.FILE_NAME;
			File file = new File(filename);
			if (!file.exists()) {
				return;
			}
			String json = IOUtils.toString(new FileInputStream(file), "utf-8");
			Gson gson = new Gson();
			RootModel root = gson.fromJson(json, RootModel.class);
			bookmarkList.addAll(root.getRoot().getBar().getChildren());
			
			//build tree
			sequence = 1;
			final TreeBuilder<String> treeBuilder = new TreeBuilder<String>(manager);
            
            for (BookmarkModel bookmark : bookmarkList) {
            	bookmark.setId(String.valueOf(sequence++));
                treeBuilder.sequentiallyAddNextNode(bookmark.getId(), 0);
                addAllChildren(treeBuilder, bookmark);
            }
            
            Log.d(TAG , manager.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addAllChildren(TreeBuilder<String> treeBuilder, BookmarkModel bookmark){
		if(bookmark.isFolder()){
		for(BookmarkModel sub : bookmark.getChildren()){
			sub.setId(String.valueOf(sequence++));
			treeBuilder.addRelation(bookmark.getId(), sub.getId());
			addAllChildren(treeBuilder, sub);
		}
		}
	}

	public void reloadBookmarkFile() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showStatusPanel(true);
				statusTxt.setText(R.string.buildingtree);
				manager = new InMemoryTreeStateManager<String>();
				bookmarkList = new ArrayList<BookmarkModel>();
				bookmarkList.clear();
				loadBookmark();
				refreshBookmarkList();
				showStatusPanel(false);
			}
		});

	}

	private void refreshBookmarkList() {
		ArrayList<BookmarkModel> mlist = new ArrayList<BookmarkModel>();
		mlist.addAll(bookmarkList);
		
		dropAdapter = new SimpleStandardAdapter(this, mlist, manager,
                LEVEL_NUMBER);
		
		list.setAdapter(dropAdapter);
		
		list.setCollapsible(true);
		
		manager.collapseChildren(null);
	}


	private View createView4Item(BookmarkModel bookmark) {
		View view = LayoutInflater.from(this).inflate(R.layout.dragitem, null);
		TextView titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		titleTxt.setText(bookmark.getName());
		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		if (bookmark.isFolder()) {
			icon.setImageResource(R.drawable.expand);
		} else {
			icon.setImageResource(R.drawable.url_24);
		}
		view.setTag(bookmark);
		return view;
	}

	private void removeAllChildren(List<BookmarkModel> list) {
		bookmarkList.removeAll(list);
		for (BookmarkModel model : list) {
			if (model.isFolder()) {
				removeAllChildren(model.getChildren());
			}
		}
	}

	private void addAllChildren(List<BookmarkModel> list) {

	}

	public JabberConfig getConfig() {
		return config;
	}

	public void setConfig(JabberConfig config) {
		this.config = config;
	}
	
}