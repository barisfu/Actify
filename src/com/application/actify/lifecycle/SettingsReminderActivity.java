package com.application.actify.lifecycle;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockActivity;
import com.application.actify.R;
import com.application.actify.core.Actify;
import com.application.actify.model.ActivitySetting;

public class SettingsReminderActivity extends SherlockActivity {
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private int userid;	
	private LinearLayout reminder_container;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settings = getSharedPreferences(Actify.PREFS_NAME, 0);
		editor = settings.edit();	
		
		userid = settings.getInt("userid", -1);
		
		setContentView(R.layout.reminder_pref);	
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		LayoutInflater inflater = getLayoutInflater();
		
		reminder_container = (LinearLayout) findViewById(R.id.reminder_pref_container);
				
		for (int i=0; i < Actify.activitySettings.size(); i++) {
		  	ActivitySetting as = Actify.activitySettings.get(i);
			
			View rowView = inflater.inflate(R.layout.reminder_pref_row, null);
			
			TextView txtActivity = (TextView) rowView.findViewById(R.id.txtActivity);
			txtActivity.setText(as.getActivity());

			int duration = as.getDuration();	
			String durationStr;
			final int hour, minute;
			if (duration >= 60) {
    			hour = (int) Math.floor(duration/60);
    			minute = duration - (hour*60);
    			durationStr = hour + "h " + minute + "m"; 
    		} else {
    			hour = 0;
    			minute = duration;
    			durationStr = minute + "m";
    		}
    					
			final int index = i; 
			
    		final Button btnReminder = (Button) rowView.findViewById(R.id.btnReminder);
    		btnReminder.setText(durationStr);
    		btnReminder.setOnClickListener(new OnClickListener() {
    			TimePickerDialog.OnTimeSetListener nextAlarmSettingListener =new TimePickerDialog.OnTimeSetListener() {
    				@Override
    				public void onTimeSet(TimePicker view, int hour, int minute) {
    					ActivitySetting as = Actify.activitySettings.get(index);
    					int duration = hour * 60 + minute;
    					as.setDuration(duration);
    					editor.remove("duration_"+as.getId()+"_"+userid);
    					editor.commit();
    					editor.putInt("duration_"+as.getId()+"_"+userid, 
    		    				as.getDuration());
    					editor.commit();
    					
    					if (hour > 0)
    						btnReminder.setText(hour + "h " + minute + "m");
    					else
    						btnReminder.setText(minute + "m");
    				}
    			};
				@Override
				public void onClick(View v) {
					TimePickerDialog tpDialog = new TimePickerDialog(SettingsReminderActivity.this, 
							nextAlarmSettingListener, hour, minute, true) ;
					tpDialog.setTitle("Default Reminder");
					tpDialog.setMessage("A reminder will be set for "+ Actify.activitySettings.get(index).getActivity() +" after: (HH:MM)");
					tpDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
						@Override
						public void onDismiss(DialogInterface dialog) {
							dialog.dismiss();			
						}
					});		
					tpDialog.show();
				}	    			
    		});	
			
			reminder_container.addView(rowView, reminder_container.getChildCount());
		}		  		

	}	

	class LocationOnItemSelectedListener implements OnItemSelectedListener {
		int i;
		
		public LocationOnItemSelectedListener(int i) {
			this.i = i;
		}
		 
		public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
				String strSelected = Actify.locationAdapter.getItem(pos);			
				Actify.activitySettings.get(i).setLocation(strSelected);
				editor.remove("loc_"+Actify.activitySettings.get(i).getId()+"_"+userid);
				editor.commit();
				editor.putString("loc_"+Actify.activitySettings.get(i).getId()+"_"+userid, 
	    				Actify.activitySettings.get(i).getLocation());
				editor.commit();

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}	
	 
	}
}
