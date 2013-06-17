package com.application.actify.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.application.actify.lifecycle.ReminderIdleActivity;
import com.application.actify.util.WakeLocker;

public class IdleReminderBroadcastReceiver extends BroadcastReceiver{
	NotificationManager nm;
	@Override
	public void onReceive(Context context, Intent intent) {
		WakeLocker.acquire(context);
		context.startActivity(new Intent(context, ReminderIdleActivity.class)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.putExtras(intent.getExtras()));		
	}
}
