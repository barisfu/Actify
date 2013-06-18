package com.application.actify.lifecycle;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.application.actify.R;
import com.application.actify.adapter.ActivityPickerAdapter;
import com.application.actify.core.Actify;
import com.application.actify.db.ActifySQLiteHelper;
import com.application.actify.model.ActivityInstance;
import com.application.actify.model.ActivitySetting;
import com.application.actify.service.ActivityReminderBroadcastReceiver;
import com.application.actify.util.WakeLocker;

public class ReminderIdleActivity extends Activity {
	private Vibrator vibrator;
	private AlarmManager alarmManager;
	private ActifySQLiteHelper db;
	private int userid;
	private SharedPreferences settings;
	private MediaPlayer mediaPlayer; 
	private boolean soundOn;
	private AlertDialog currentActDialog, activityPickerDialog;
	private Window window;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		window = this.getWindow();
	    window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
	    window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	    window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
	    
	    // this is to prevent recreating when the screen is rotated
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
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
		vibrator.vibrate(pattern, 0);	
		
		db = new ActifySQLiteHelper(this);
		
		settings = getSharedPreferences(Actify.PREFS_NAME, 0);
        userid = settings.getInt("userid", -1);
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
		if (currentActDialog != null) {
			if (currentActDialog.isShowing())
				currentActDialog.dismiss();
		}
		if (activityPickerDialog != null) {
			if (activityPickerDialog.isShowing())
				activityPickerDialog.dismiss();
		}
		super.onDestroy();
	}
	
	private void playSound(Context context, Uri alert) {
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
            System.out.println("OOPS");
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
		currentActDialog  = new AlertDialog.Builder(this).create();
		currentActDialog.setTitle("Reminder");
		currentActDialog.setMessage("Are you doing something at the moment?");
		currentActDialog.setButton(AlertDialog.BUTTON_POSITIVE, 
				getResources().getString(R.string.btnYes), 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {		
						WakeLocker.release();
						vibrator.cancel();
						if (soundOn)  mediaPlayer.stop();
						dialog.dismiss();
						alarmManager.cancel(Actify.pendingIntents.get(Actify.PI_ID));
						Actify.pendingIntents.remove(Actify.PI_ID);
			        	Actify.pendingIntentTimes.remove(Actify.PI_ID);
						startNewActivityDialog();
					}			
		});
		currentActDialog.setButton(AlertDialog.BUTTON_NEGATIVE, 
				getResources().getString(R.string.btnNo), 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						WakeLocker.release();
						vibrator.cancel();	
						if (soundOn)  mediaPlayer.stop();
						alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
			    				+ (Actify.PI_IDLE_TIME * 60 * 1000), Actify.pendingIntents.get(Actify.PI_ID));
						ReminderIdleActivity.this.finish();
					}			
		});
		currentActDialog.show();
	}		
	
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
				int activityid = (int) id;
				ActivitySetting as = Actify.findActivitySettingById(activityid);
				String locationStr = as.getLocation();
				int locationid = Actify.locationAdapter.getPosition(locationStr);
    			
            	ActivityInstance ai = new ActivityInstance(activityid, userid, Calendar.getInstance(), Calendar.getInstance(), locationid);
            	ai = db.addActivity(ai);

            	
            	// start alarm
            	int duration = as.getDuration();
            	Intent intent = new Intent(ReminderIdleActivity.this, ActivityReminderBroadcastReceiver.class);
            	intent.putExtra("id", ai.getId());
            	intent.putExtra("activity", as.getActivity());
            	intent.putExtra("duration", duration);
        		PendingIntent pendingIntent = PendingIntent.getBroadcast(
        				ReminderIdleActivity.this, new Random().nextInt(10000), intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        		Toast.makeText(ReminderIdleActivity.this, "Reminder set in " + durationStr,
        				Toast.LENGTH_LONG).show();         	
            	
            	Actify.showPicker = true;
        		Intent mainIntent = new Intent(getApplicationContext(), MainFragmentActivity.class);
        		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(mainIntent);
        		ReminderIdleActivity.this.finish();
            	
			}
        });           
        activityPickerDialog.setView(dialogView);
        activityPickerDialog.setTitle("What are you doing now?");
        activityPickerDialog.show();   	            	            
    			
		
	}
	
}
