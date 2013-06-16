package com.application.actify.lifecycle;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.application.actify.R;
import com.application.actify.core.Actify;

public class LoginActivity extends Activity {

	EditText txtUsername, txtPassword;
	Button btnLogin;  
	int userid;
	String householdid;
	      
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        txtUsername = (EditText)findViewById(R.id.txtUsername);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(loginListener);  
        
        userid = -1;
        householdid = "";
    }
    
    private OnClickListener loginListener = new OnClickListener() {
    	public void onClick(View v) {
    		String username = txtUsername.getText().toString();
    		String password = txtPassword.getText().toString();
    		
    		if(username.trim().length() > 0 && password.trim().length() > 0){
    			validateLogin(username, password);
    			if (userid != -1) {    		
    	    		// Save login information 
    	    		SharedPreferences settings = getSharedPreferences(Actify.PREFS_NAME, 0);
    	    		SharedPreferences.Editor editor = settings.edit();
    	    		editor.putBoolean("logged_in", true);
    	    		editor.putInt("userid", userid);
    	    		editor.putString("householdid", householdid);
    	    		
    	    		// Save username, password, and userid in settings so we don't have to connect to DB everytime
    	    		if (!settings.contains("userid_"+username)) {
    	    			editor.putInt("userid_"+username, userid);
    	    			editor.putString("password_"+username, password);    	    			
    	    		}
    	    		
    	    		editor.commit();
    	    		
    	    		// Forward to the next screen
    	    		Intent intent = new Intent(getApplicationContext(), MainFragmentActivity.class);
    				startActivity(intent);
    				finish();
        		} else {
        			Toast.makeText(getApplicationContext(),"Username or password is incorrect. Please try again.",Toast.LENGTH_SHORT).show();
        		}
    		} else {
    			Toast.makeText(getApplicationContext(),"Username and password cannot be empty.",Toast.LENGTH_SHORT).show();
    		} 		
    	}	

    };
    
    private void validateLogin(String username, String password) {

		// TODO: login to server, get userid and householdid
		if (username.equals("q")&&password.equals("q")) {
    		userid = 1;   
    		householdid = "householdX";
		}

    }
}