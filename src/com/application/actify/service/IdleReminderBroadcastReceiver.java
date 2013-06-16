package com.application.actify.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.application.actify.lifecycle.ReminderIdleActivity;

public class IdleReminderBroadcastReceiver extends BroadcastReceiver{
	NotificationManager nm;
	@Override
	public void onReceive(Context context, Intent intent) {
		
		context.startActivity(new Intent(context, ReminderIdleActivity.class)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.putExtras(intent.getExtras()));		
	}
}
