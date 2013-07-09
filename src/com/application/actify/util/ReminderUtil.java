package com.application.actify.util;

import java.util.Random;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.application.actify.core.Actify;
import com.application.actify.model.ActivityInstance;
import com.application.actify.model.ActivitySetting;
import com.application.actify.service.ActivityReminderBroadcastReceiver;
import com.application.actify.service.IdleReminderBroadcastReceiver;

public class ReminderUtil {
	public static final int PI_ID = -111;
	public static final int PI_IDLE_TIME = 15;
	
	
	public static void setIdleReminder(Activity act) {				               
		PendingIntent pendingIntent =  createIdlePI(act);
		//Actify.pendingIntents.put(Actify.PI_ID, pendingIntent);	
		//DateTime dtReminder = new DateTime();
		//dtReminder = dtReminder.plusMinutes(PI_IDLE_TIME);
		//Actify.pendingIntentTimes.put(Actify.PI_ID, dtReminder);
		AlarmManager alarmManager = (AlarmManager) act.getSystemService(Activity.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ (PI_IDLE_TIME * 60 * 1000), pendingIntent); 
		
	}
	
	public static PendingIntent createIdlePI(Activity act) {
		Intent intent = new Intent(act, IdleReminderBroadcastReceiver.class);
    	intent.putExtra("id", PI_ID);                	
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				act, PI_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}
	
	public static void setActivityReminder(Activity act, int id, int duration, String activity) {
		DateTime dtReminder = new DateTime();
		dtReminder = dtReminder.plusMinutes(duration);
		
		PendingIntent pendingIntent = createActivityPI(act, id, duration, activity);
		AlarmManager alarmManager = (AlarmManager) act.getSystemService(Activity.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ (duration * 60 * 1000), pendingIntent);
		
		SharedPreferences settings = act.getSharedPreferences(Actify.PREFS_NAME, 0);
	    Editor editor = settings.edit();
	    editor.putString("reminder_"+id, dtReminder.toString(Actify.datetimeFormatJoda));
	    editor.commit();
	}
	
	public static PendingIntent createActivityPI(Activity act, int id, int duration, String activity) {
    	Intent intent = new Intent(act, ActivityReminderBroadcastReceiver.class);
    	intent.putExtra("id", id);
    	intent.putExtra("activity", activity);
    	intent.putExtra("duration", duration);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				act, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		return pendingIntent;
	}
	
	public static PendingIntent createActivityPI(Activity act, int id) {
    	Intent intent = new Intent(act, ActivityReminderBroadcastReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				act, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		//Actify.pendingIntents.put(ai.getId(), pendingIntent);	
		
		//Actify.pendingIntentTimes.put(ai.getId(), dtReminder);
		return pendingIntent;
	}
	
	public static void cancelAlarm(Activity act, int id) {
		AlarmManager alarmManager = (AlarmManager) act.getSystemService(Activity.ALARM_SERVICE);
		alarmManager.cancel(createActivityPI(act, id));
		SharedPreferences settings = act.getSharedPreferences(Actify.PREFS_NAME, 0);
	    Editor editor = settings.edit();
	    editor.remove("reminder_"+id);
	    editor.commit();
	}
	
	
	public static void cancelIdleAlarm(Activity act) {
		AlarmManager alarmManager = (AlarmManager) act.getSystemService(Activity.ALARM_SERVICE);
		alarmManager.cancel(createIdlePI(act));
	}
}
