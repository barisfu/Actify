package com.application.actify.lifecycle;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.application.actify.R;
import com.application.actify.adapter.ActivityPickerAdapter;
import com.application.actify.core.Actify;
import com.application.actify.db.ActifySQLiteHelper;
import com.application.actify.model.ActivityGuest;
import com.application.actify.model.ActivityInstance;
import com.application.actify.model.ActivitySetting;
import com.application.actify.model.Reminder;
import com.application.actify.service.ActivityReminderBroadcastReceiver;
import com.application.actify.util.ReminderUtil;
import com.application.actify.util.WakeLocker;

public class ReminderActivityActivity extends Activity {
	private String activity;
	private Vibrator vibrator;
	private int duration;
	private int id;
	private AlarmManager alarmManager;
	private ActifySQLiteHelper db;
	private int userid;
	private SharedPreferences settings;
	private Calendar calEnd;	
	private boolean soundOn;
	private MediaPlayer mediaPlayer; 
	private AlertDialog activityPickerDialog;
	private Window window;
	private Reminder rem;
	private int action = Reminder.REMINDER_NOTHING;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		window = this.getWindow();
	    window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
	    window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	    window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
	    
	    // this is to prevent recreating when the screen is rotated
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		settings = getSharedPreferences(Actify.PREFS_NAME, 0);
        userid = settings.getInt("userid", -1);
        db = new ActifySQLiteHelper(this);
		
		Intent intent = getIntent();
		duration = intent.getIntExtra("duration", 10);
		id = intent.getIntExtra("id", 0);
		activity = intent.getStringExtra("activity").toLowerCase(Locale.ENGLISH);
		
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// repeat: vibrate 10 times (2s vibration, 2s pause), pause 5 minutes
		long[] pattern = { 0, 2000, 2000, 
				2000, 2000, 
				2000, 2000, 
				2000, 2000, 
				2000, 2000, 
				2000, 2000, 
				2000, 2000, 
				2000, 2000, 
				2000, 2000,
				2000, 5*60*1000};
		// repeat: vibrate 2s, pause 2s
		//long[] pattern = { 0, 2000, 2000};		
		vibrator.vibrate(pattern, 0);
		
		// create reminder to be logged		
		rem = new Reminder(userid, Reminder.REMINDER_IDLE, Calendar.getInstance(), Calendar.getInstance(), id, -1);
		
		soundOn = settings.getBoolean("sound_"+userid, false);
		if (soundOn) 
			playSound(this, getAlarmUri());				
		
		alarmManager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
		
		if (Actify.activitySettings == null) {
        	Actify.loadSettings(this);
        }
		
		showCurrentActDialog();
	}
	
	@Override
	public void onDestroy() {	
		// put the dismiss here to prevent asynchronous tasks that causes window leak
		if (activityPickerDialog != null) {
			if (activityPickerDialog.isShowing())
				activityPickerDialog.dismiss();
		}
		rem.setAction(action);
		db.addReminder(rem);
		super.onDestroy();
	}
	
	private void playSound(Context context, Uri alert) {
		// TODO : play sound with a pattern, synchronized with vibrator
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
	
	protected Uri getAlarmUri() {
        Uri alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alert;
    }
	
	// Ask if current activity is still running
	private void showCurrentActDialog() {
		AlertDialog currentActDialog  = new AlertDialog.Builder(this).create();
		currentActDialog.setTitle("Activity Reminder");
		currentActDialog.setMessage("Are you still "+activity+ "?");
		currentActDialog.setButton(AlertDialog.BUTTON_POSITIVE, 
				getResources().getString(R.string.btnYes), 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {	
						rem.setEnd(Calendar.getInstance());
						action = Reminder.REMINDER_YES;
						WakeLocker.release();
						vibrator.cancel();
						if (soundOn)  mediaPlayer.stop();
						dialog.dismiss();
						showAlarmSettingDialog();
					}			
		});
		currentActDialog.setButton(AlertDialog.BUTTON_NEGATIVE, 
				getResources().getString(R.string.btnNo), 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						rem.setEnd(Calendar.getInstance());
						action = Reminder.REMINDER_NO;
						WakeLocker.release();
						vibrator.cancel();
						if (soundOn) mediaPlayer.stop();
						dialog.dismiss();
						stopLastActivityDialog();
					}			
		});
		currentActDialog.show();
	}	
	
	// Current activity is still running, snooze the alarm
	private void showAlarmSettingDialog() {
		int hour = 0, minute = 0;
		
		if (duration < 30) {
			minute = 5;
		} else if (duration < 60) {
			minute = 10;
		} else if (duration < 240) {
			minute = 30;
		} else {
			hour = 1;
		}
		
		TimePickerDialog tpDialog = new TimePickerDialog(this, nextAlarmSettingListener, hour, minute, true) ;
		tpDialog.setTitle("Snooze");
		tpDialog.setMessage("Remind me again after (HH:MM)");
		tpDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
			@Override
			public void onDismiss(DialogInterface dialog) {
				dialog.dismiss();
				ReminderActivityActivity.this.finish();				
			}
		});		
		tpDialog.show();
	}
	
	// listener for snoozing alarm
	TimePickerDialog.OnTimeSetListener nextAlarmSettingListener =new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hour, int minute) {
			
    		ReminderUtil.setActivityReminder(ReminderActivityActivity.this, id, hour*60+minute, activity);
    		
    		ReminderActivityActivity.this.finish();
		}
	};
	
	
	// Current activity was already stopped, ask how long time ago it was stopped
	private void stopLastActivityDialog() {
		int hour = 0, minute = 0;
		
		if (duration < 60) {
			minute = 10;
		} else if (duration >= 60 && duration < 240) {
			minute = 30;
		} else {
			hour = 1;
		}
		TimePickerDialog tpDialog = new TimePickerDialog(this, activityStopListener, hour, minute, true) ;
		tpDialog.setTitle("Stop");
		tpDialog.setMessage("How long time ago did you stop "+ activity + "? (HH:MM)");
		tpDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {			
			@Override
			public void onCancel(DialogInterface dialog) {
				ReminderActivityActivity.this.finish();				
			}
		});
		tpDialog.show();
	}
	
	// listener for saving last activity
	TimePickerDialog.OnTimeSetListener activityStopListener =new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hour, int minute) {
			
			
			ActivityInstance ai = db.getActivityInstance(id);
        	
        	calEnd = Calendar.getInstance();
        	calEnd.add(Calendar.HOUR, hour*-1);
        	calEnd.add(Calendar.MINUTE, minute*-1);
        	
        	ai.setEnd(calEnd);
        	ai.setMode(Actify.MODE_INSERT);
        	db.updateActivity(ai);            	
        	
        	db.updateActivityPause(ai.getId(), ai.getSync(), ai.getMode());
        	
        	String query = db.activityGuestQueryBuilder(Actify.VIEW_TIMER, Actify.MODE_RUNNING, ai.getId());
        	List<ActivityGuest> insertList = db.getActivityGuestList(query);
        	for (ActivityGuest ag : insertList) {
        		ag.setMode(Actify.MODE_INSERT);
        		db.updateActivityGuest(ag);
        	}
        	query = db.activityGuestQueryBuilder(Actify.VIEW_TIMER, Actify.MODE_DELETE, ai.getId());
        	List<ActivityGuest> deleteList = db.getActivityGuestList(query);
        	for (ActivityGuest ag : deleteList) {
        		db.deleteActivityGuest(ag);
        	}
        	
        	Toast.makeText(ReminderActivityActivity.this, 
        			activity + getResources().getString(R.string.toastActivitySaved), 
        			Toast.LENGTH_SHORT).show();
        	        	
        	ReminderUtil.cancelAlarm(ReminderActivityActivity.this, ai.getId());        	
        	
        	if (getRunningActivitySize()  == 0) {
        		startNewActivityDialog();
        	} else {
        		Intent mainIntent = new Intent(getApplicationContext(), MainFragmentActivity.class);
        		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(mainIntent);
        		ReminderActivityActivity.this.finish();
        	}
		}
	};

	// Show dialog to start a new activity
	private void startNewActivityDialog() {
		

    	activityPickerDialog  = new AlertDialog.Builder(this).create();
    		
	    LayoutInflater inflater = getLayoutInflater();
	    View dialogView = inflater.inflate(R.layout.activity_picker, null);
	    
	    GridView gridView = (GridView) dialogView.findViewById(R.id.grid_view);
        gridView.setAdapter(new ActivityPickerAdapter(this));        
   
        gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				
				ReminderUtil.cancelIdleAlarm(ReminderActivityActivity.this);
				
				int activityid = (int) id;
				ActivitySetting as = Actify.findActivitySettingById(activityid);
				String locationStr = as.getLocation();
				int locationid = Actify.locationAdapter.getPosition(locationStr);
    			
            	ActivityInstance ai = new ActivityInstance(activityid, userid, calEnd, Calendar.getInstance(), locationid);
            	ai = db.addActivity(ai);
            	
            	ReminderUtil.setActivityReminder(ReminderActivityActivity.this, ai.getId(), as.getDuration(), as.getActivity());
        		int newDuration = as.getDuration();
        		String durationStr;
        		if (newDuration >= 60) {
        			int hour = (int) Math.floor(newDuration/60);
        			int minutes = newDuration - (hour*60);
        			durationStr = hour + " hours " + minutes + " minutes"; 
        		} else {
        			durationStr = newDuration + " minutes";
        		}
        		Toast.makeText(ReminderActivityActivity.this, "Reminder set in " + durationStr,
        				Toast.LENGTH_LONG).show();          	
            	
            	Actify.showPicker = true;
        		Intent mainIntent = new Intent(getApplicationContext(), MainFragmentActivity.class);
        		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(mainIntent);
        		ReminderActivityActivity.this.finish();
            	
			}
        });      
        
        activityPickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
			@Override
			public void onDismiss(DialogInterface dialog) {
				ReminderActivityActivity.this.finish();
			}
		});
                
        activityPickerDialog.setView(dialogView);        
        activityPickerDialog.setTitle("What are you doing now, after "+ activity +"?");
        activityPickerDialog.show();   	            	            
    			   
	}
	
	private int getRunningActivitySize() {
		settings = getSharedPreferences(Actify.PREFS_NAME, 0);
		return db.countRunningActivity(userid);
	}
	
}
