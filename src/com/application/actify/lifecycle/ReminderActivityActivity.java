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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
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
import com.application.actify.service.ActivityReminderBroadcastReceiver;
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
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		
		soundOn = settings.getBoolean("sound_"+userid, false);
		if (soundOn) 
			playSound(this, getAlarmUri());				
		
		alarmManager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
		
		showCurrentActDialog();
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
			PendingIntent pi = Actify.pendingIntents.get(id);
			alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
    				+ (((hour*3600)+(minute*60)) * 1000), pi);
			Actify.pendingIntentTimes.remove(id);
			DateTime dtReminder = new DateTime();
			dtReminder = dtReminder.plusHours(hour);
			dtReminder = dtReminder.plusMinutes(minute);
    		Actify.pendingIntentTimes.put(id, dtReminder);
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
        	
        	alarmManager.cancel(Actify.pendingIntents.get(id));
        	Actify.pendingIntents.remove(id);
        	Actify.pendingIntentTimes.remove(id);
        	
        	startNewActivityDialog();
		}
	};

	// Show dialog to start a new activity
	private void startNewActivityDialog() {
		

    	final AlertDialog activityPickerDialog  = new AlertDialog.Builder(this).create();
    		
	    LayoutInflater inflater = getLayoutInflater();
	    View dialogView = inflater.inflate(R.layout.activity_picker, null);
	    
	    GridView gridView = (GridView) dialogView.findViewById(R.id.grid_view);
        gridView.setAdapter(new ActivityPickerAdapter(this));        
   
        gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				int activityid = (int) id;
				ActivitySetting as = Actify.findActivitySettingById(activityid);
				String locationStr = as.getLocation();
				int locationid = Actify.locationAdapter.getPosition(locationStr);
    			
            	ActivityInstance ai = new ActivityInstance(activityid, userid, calEnd, Calendar.getInstance(), locationid);
            	ai = db.addActivity(ai);

            	
            	// start alarm
            	int duration = as.getDuration();
            	Intent intent = new Intent(ReminderActivityActivity.this, ActivityReminderBroadcastReceiver.class);
            	intent.putExtra("id", ai.getId());
            	intent.putExtra("activity", as.getActivity());
            	intent.putExtra("duration", duration);
        		PendingIntent pendingIntent = PendingIntent.getBroadcast(
        				ReminderActivityActivity.this, new Random().nextInt(10000), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        		Actify.pendingIntents.put(ai.getId(), pendingIntent);	
        		DateTime dtReminder = new DateTime();
        		dtReminder = dtReminder.plusMinutes(duration);
        		Actify.pendingIntentTimes.put(ai.getId(), dtReminder);
        		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
        				+ (duration * 60 * 1000), pendingIntent);
        		String durationStr;
        		if (duration >= 60) {
        			int hour = (int) Math.floor(duration/60);
        			int minutes = duration - (hour*60);
        			durationStr = hour + " hours " + minutes + " minutes"; 
        		} else {
        			durationStr = duration + " minutes";
        		}
        		Toast.makeText(ReminderActivityActivity.this, "Reminder set in " + durationStr,
        				Toast.LENGTH_LONG).show();
            	activityPickerDialog.dismiss();            	
            	
            	Actify.showPicker = true;
        		Intent mainIntent = new Intent(getApplicationContext(), MainFragmentActivity.class);
        		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(mainIntent);
        		ReminderActivityActivity.this.finish();
            	
			}
        });      
                
        activityPickerDialog.setView(dialogView);
        activityPickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				ReminderActivityActivity.this.finish();
				
			}
		});
        
        activityPickerDialog.setTitle("What are you doing now, after "+ activity +"?");
        activityPickerDialog.show();   	            	            
    			
		
	}
	
}
