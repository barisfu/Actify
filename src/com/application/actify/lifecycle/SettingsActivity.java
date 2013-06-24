package com.application.actify.lifecycle;

import java.io.File;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.application.actify.R;
import com.application.actify.adapter.SettingsEntryAdapter;
import com.application.actify.core.Actify;
import com.application.actify.db.ActifySQLiteHelper;
import com.application.actify.util.Exporter;
import com.application.actify.util.Mail;
import com.application.actify.util.Reminder;
import com.application.actify.view.component.SettingCheckboxItem;
import com.application.actify.view.component.SettingEntryItem;
import com.application.actify.view.component.SettingItem;
import com.application.actify.view.component.SettingSectionItem;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

public class SettingsActivity extends ListActivity {
    /** Called when the activity is first created. */
	
	 ArrayList<SettingItem> items = new ArrayList<SettingItem>();
	 SharedPreferences settings;
	 Editor editor;
	 int userid;	 
	 Exporter exporter;
	 String username;
	 int idle;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settings = getSharedPreferences(Actify.PREFS_NAME, 0);
        editor = settings.edit();
        userid = settings.getInt("userid", -1);
        username = settings.getString("gmail_username_"+userid, "");
        idle = settings.getInt("idle_"+userid, Reminder.PI_IDLE_TIME);
        
        ListView lv = getListView();
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.settings_header, lv, false);
        lv.addHeaderView(header, null, false);
       
        items.add(new SettingSectionItem("Application"));
        items.add(new SettingCheckboxItem("Sound", "", Actify.MENU_SETTING_SOUND, "sound", 
        		settings.getBoolean("sound"+"_"+userid, true)));
        items.add(new SettingEntryItem("Idle Reminder", "", Actify.MENU_SETTING_IDLE, "reminder"));
        items.add(new SettingEntryItem("Email settings", "", Actify.MENU_SETTING_EMAIL_SETTINGS, "email_settings"));
        
        items.add(new SettingSectionItem("Backup Data"));
        items.add(new SettingEntryItem("Export", "", Actify.MENU_SETTING_EXPORT, "export"));
        items.add(new SettingEntryItem("Send via email", "", Actify.MENU_SETTING_EMAIL, "email"));        
        
        items.add(new SettingSectionItem("Activity Settings"));
        items.add(new SettingEntryItem("Visibility", "", Actify.MENU_SETTING_VISIBILITY, "visibility"));
        items.add(new SettingEntryItem("Order", "", Actify.MENU_SETTING_ORDER, "order"));
        items.add(new SettingEntryItem("Location", "", Actify.MENU_SETTING_LOCATION, "location"));
        items.add(new SettingEntryItem("Reminder", "", Actify.MENU_SETTING_REMINDER, "reminder"));
                        
        SettingsEntryAdapter adapter = new SettingsEntryAdapter(this, items);
        
        setListAdapter(adapter);
        
        String householdid = settings.getString("householdid", "");
        ActifySQLiteHelper db = new ActifySQLiteHelper(SettingsActivity.this);
        exporter = new Exporter(db, userid, householdid);
        
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	int index = position - 1;
    	if(!items.get(index).isSection()){
    		Intent intent = null;
    		SettingEntryItem item = (SettingEntryItem)items.get(index);
    		switch (item.getMenuId()) {
    		case Actify.MENU_SETTING_LOCATION:
    			intent = new Intent(getApplicationContext(), SettingsLocationActivity.class);
    			startActivity(intent);
		        break;
    		case Actify.MENU_SETTING_ORDER:
    			intent = new Intent(getApplicationContext(), SettingsOrderActivity.class);
    			startActivity(intent);		
    			break;
	    	case Actify.MENU_SETTING_VISIBILITY:
				intent = new Intent(getApplicationContext(), SettingsVisibilityActivity.class);
				startActivity(intent);
		        break;
			case Actify.MENU_SETTING_REMINDER:
				intent = new Intent(getApplicationContext(), SettingsReminderActivity.class);
				startActivity(intent);
		        break;		   
			case Actify.MENU_SETTING_SOUND:
    			intent = new Intent(getApplicationContext(), SettingsLocationActivity.class);
    			startActivity(intent);
		        break;		        		
	    	case Actify.MENU_SETTING_EXPORT:
				exportData();
		        break;
			case Actify.MENU_SETTING_EMAIL:
				sendData();
		        break;	
			case Actify.MENU_SETTING_IDLE:
				setIdleReminder();
		        break;	
			case Actify.MENU_SETTING_EMAIL_SETTINGS:
				intent = new Intent(getApplicationContext(), SettingsEmailActivity.class);
    			startActivity(intent);
		        break;	
		    }
    		
    	}
	
    	super.onListItemClick(l, v, position, id);
    }
    
    // listener for idle alarm
 	TimePickerDialog.OnTimeSetListener idleSettingListener =new TimePickerDialog.OnTimeSetListener() {
 		@Override
 		public void onTimeSet(TimePicker view, int hour, int minute) {
 			editor.putInt("idle_"+userid, hour*60+minute);
 		}
 	};
    
    private void setIdleReminder() {
    	int hour = 0, minute = 0;
    	
    	if (idle >= 60) {
    		hour = (int) Math.floor(idle/60);
			minute = idle - (hour*60);
    	}  else {
    		minute = idle;
    	}
		
		TimePickerDialog tpDialog = new TimePickerDialog(this, idleSettingListener, hour, minute, true) ;
		tpDialog.setTitle("Idle Reminder");
		tpDialog.setMessage("Remind me when there is no activity after: (HH:MM)");
		tpDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
			@Override
			public void onDismiss(DialogInterface dialog) {
				dialog.dismiss();	
			}
		});		
		tpDialog.show();
		
    }
    
    private void exportData() {    	
    	AlertDialog dialog = new AlertDialog.Builder(this).create();
    	dialog.setTitle("Export Database");
    	dialog.setMessage("Export local database as CSV file to SD card");
    	dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        if (exporter.export())
			        Toast.makeText(getApplicationContext(), "The database is exported to [SD card]/Actify/ folder", 
			        		Toast.LENGTH_LONG).show();
		        else Toast.makeText(getApplicationContext(), "Failed to export database", 
		        		Toast.LENGTH_LONG).show();
		        dialog.dismiss();
			}    		
    	});
    	dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
    		
    	});
    	dialog.show();
    }
    
    private void sendData() {
    	final AlertDialog dialog = new AlertDialog.Builder(this).create();
    	dialog.setTitle("Send email");
    	dialog.setMessage("Send data via email to the following address:");
    	final EditText editEmailTo = new EditText(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		        LinearLayout.LayoutParams.MATCH_PARENT,
		        LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(10, 10, 10, 10);
		editEmailTo.setLayoutParams(lp);
		editEmailTo.setText(username+"@gmail.com");
		editEmailTo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus) {
		        	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		        }
		    }
		});				
		dialog.setView(editEmailTo);
		
		
    	dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (exporter.getFilename() != null) {
		    		File f = new File(exporter.getFilename());
		    		if (!f.exists()) 
		    			exporter.export();
		    	} else {
		    		exporter.export();
		    	}
		    	String[] attachements = new String[]{exporter.getFilename()};
		    	try {
					boolean sent = sendEmail("chitrahapsari@gmail.com", "chitrahapsari@gmail.com", "Actify data", "Actify data", attachements);
					if (sent)
						Toast.makeText(SettingsActivity.this, "Email sent", Toast.LENGTH_LONG).show();
					else Toast.makeText(SettingsActivity.this, "Ooops. The email is not sent. Please check your email account, internet connection and try again.", Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					Toast.makeText(SettingsActivity.this, "Ooops. The email is not sent. Please check your email account, internet connection and try again.", Toast.LENGTH_LONG).show();
				}
			}    		
    	});
    	dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
    		
    	});
    	dialog.show();
    	
    	
    }
    
    public boolean sendEmail(String to, String from, String subject,                                          
            String message,String[] attachements) throws Exception {     
	    	
    		String username = settings.getString("gmail_username_"+userid, "");
			String password = settings.getString("gmail_password_"+userid, "");
    	
			Mail mail = new Mail(username,password);
			if (subject != null && subject.length() > 0) {
				mail.setSubject(subject);
			} else {
				mail.setSubject("Subject");
			}
			
			if (message != null && message.length() > 0) {
				mail.setBody(message);
			} else {
				mail.setBody("Message");
			}
			
			mail.setTo(new String[] {to});
			mail.setFrom(from);
			
			if (attachements != null) {
				for (String attachement : attachements) {       
					mail.addAttachment(attachement);
				}
			}
			return mail.send();
		}
}