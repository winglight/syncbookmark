package com.favekeeper.android;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Gather the xmpp settings and create an XMPPConnection
 */
public class SettingsDialog extends Dialog implements android.view.View.OnClickListener {
	
	public final static String REGISTER_URL = "http://www.favekeeper.com/register.html";
	
    private SyncbookmarkActivity context;

    public SettingsDialog(SyncbookmarkActivity context) {
        super(context);
        this.context = context;
    }

    protected void onStart() {
        super.onStart();
        setContentView(R.layout.settings);
        getWindow().setFlags(4, 4);
        setTitle(R.string.settingTitle);
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);
        Button getBtn = (Button) findViewById(R.id.getAccountBtn);
        getBtn.setOnClickListener(this);
        
        EditText userTxt = (EditText) this.findViewById(R.id.userid);
        userTxt.setText(context.getConfig().getUsername());
        EditText pwdTxt = (EditText) this.findViewById(R.id.password);
        pwdTxt.setText(context.getConfig().getPassword());
    }

    public void onClick(View v) {
    	if(v.getId() == R.id.ok){
        String username = getText(R.id.userid);
        String password = getText(R.id.password);
        context.saveConfig(username, password);
        
        context.startService();
        
        context.showStatusPanel(true);
        dismiss();
    	}else{
    		Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(REGISTER_URL));
			this.context.startActivity(intent);
    	}
    }

    private String getText(int id) {
        EditText widget = (EditText) this.findViewById(id);
        return widget.getText().toString();
    }
}

