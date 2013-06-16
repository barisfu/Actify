package com.application.actify.lifecycle;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.application.actify.R;
import com.application.actify.core.Actify;

public class SettingsEmailActivity extends SherlockActivity {
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private int userid;	
	private String username;
	private String password;
	TextView txtUsername, txtPassword;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settings = getSharedPreferences(Actify.PREFS_NAME, 0);
		editor = settings.edit();			
		userid = settings.getInt("userid", -1);
		username = settings.getString("gmail_username_"+userid, "");
		password = settings.getString("gmail_password_"+userid, "");
		
		setContentView(R.layout.settings_email);	
		txtUsername = (TextView) this.findViewById(R.id.txtUsername);
		txtUsername.setText(username);
		txtPassword = (TextView) this.findViewById(R.id.txtPassword);
		txtPassword.setText(password);
	}
	
	
	@Override 
	public void onPause() {		
		super.onResume();
		editor.putString("gmail_username_"+userid, txtUsername.getText().toString());
		editor.putString("gmail_password_"+userid, txtPassword.getText().toString());
		editor.commit();
	}
}
