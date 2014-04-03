package com.favekeeper.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;

public class AboutActivity extends Activity {
	
	private String LOGTAG = "AboutActivity";

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.history);
		
		//set text of about content
		WebView about = (WebView)findViewById(R.id.about_content);
		registerForContextMenu(about);
//		about.getSettings().setJavaScriptEnabled(true);
		
		Intent intent = getIntent();
		String path = intent.getStringExtra("url");
		if (path == null) {
			about.loadUrl("file:///android_asset/about.html");
		}else{
//			about.loadUrl("file://" + DownloadUtil.getLogDir() + path);
		}
		
	}
	
}
