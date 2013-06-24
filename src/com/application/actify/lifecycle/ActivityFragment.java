package com.application.actify.lifecycle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.Duration;

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
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.application.actify.R;
import com.application.actify.adapter.ActivityPickerAdapter;
import com.application.actify.core.Actify;
import com.application.actify.db.ActifySQLiteHelper;
import com.application.actify.model.ActivityGuest;
import com.application.actify.model.ActivityInstance;
import com.application.actify.model.ActivityPause;
import com.application.actify.model.ActivitySetting;
import com.application.actify.model.Guest;
import com.application.actify.service.ActivityReminderBroadcastReceiver;
import com.application.actify.service.IdleReminderBroadcastReceiver;
import com.application.actify.util.Reminder;

public class ActivityFragment extends SherlockFragment {
	
	static final int PICK_ACTIVITY_REQUEST = 0;
	static final int START = 0, END = 1;
	
	
	private LinearLayout timer_container;
	private ScrollView scroller;
	private Button btnStart;
	private View view;
	private DateTimePicker dateTimePicker;
		
	private List<ActivityInstance> activityInstances;
	private List<ActivityPause> activityPauses;
	
	private SharedPreferences settings;
	private Editor editor;
	private ActifySQLiteHelper db;
	private int userid;
	private String householdid;
	
	private Activity act;
	private Resources res;
	
	private AlarmManager alarmManager;		
	
	private int newHour, newMinute;
	private boolean changeReminder;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        act = getActivity();
        res = act.getResources();
        
        settings = act.getSharedPreferences(Actify.PREFS_NAME, 0);
        editor = settings.edit();
        userid = settings.getInt("userid", -1);
        householdid = settings.getString("householdid", "");  
        
        db = new ActifySQLiteHelper(act);
                
        alarmManager = (AlarmManager) getActivity().getSystemService(Activity.ALARM_SERVICE);
              
    }	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		String strQuery = db.activityQueryBuilder(userid);
		activityInstances = db.getActivityList(strQuery);
		activityPauses = new ArrayList<ActivityPause>(); 
		
		view = inflater.inflate(R.layout.activity, container, false);
        timer_container = (LinearLayout) view.findViewById(R.id.timer_container);        
        btnStart = (Button) view.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(startListener);  
        scroller = (ScrollView) view.findViewById(R.id.scroller);
        
        if (activityInstances.isEmpty()) {
        	scroller.setBackgroundResource(R.drawable.background_blank);
        	Reminder.setIdleReminder(getActivity());
        } else {        
	        for (int i = 0; i < activityInstances.size(); i++) {	        		   
	        	activityPauses.add(new ActivityPause(activityInstances.get(i).getId(), Calendar.getInstance(), Calendar.getInstance()));
	        	inflateTimerRow(activityInstances.get(i), activityPauses.get(i));
	        }	        
        }   
        return view;
    }	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {				
		menu.add(0, Actify.MENU_ACTIVITY_HISTORY, 0, R.string.tabActivityHistory).setIcon(R.drawable.list);
		menu.add(0, Actify.MENU_ACTIVITY_PIE, 1, R.string.tabActivityChart).setIcon(R.drawable.pie_chart);
		menu.add(0, Actify.MENU_ACTIVITY_DENSITY, 2, R.string.tabActivityDensity).setIcon(R.drawable.bar_chart);
		menu.add(1, Actify.MENU_SETTING, 3, R.string.menuSettings).setIcon(R.drawable.settings);
		menu.add(2, Actify.MENU_LOGOUT, 4, R.string.menuLogout).setIcon(R.drawable.logout);
		super.onCreateOptionsMenu(menu, inflater);
	}	
	
	// Helper for inflating a row
	private void inflateTimerRow(final ActivityInstance ai, final ActivityPause ap) {		
		scroller.setBackgroundResource(android.R.color.transparent); 
		int activityid = ai.getactivityid();
		ActivitySetting as = Actify.findActivitySettingById(activityid);
		final String strActivity = as.getActivity();
		final String strLocation = as.getLocation();
		final int mode = ai.getMode();
		final long oriStart = ai.getStart().getTimeInMillis();
		
		LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		final View rowView = inflater.inflate(R.layout.activity_row, null);
				
		final Chronometer mChronometer = (Chronometer) rowView.findViewById(R.id.chronometer);			
		
		long base;
		if (mode == Actify.MODE_RUNNING) {		
			base = SystemClock.elapsedRealtime() - (Calendar.getInstance().getTimeInMillis() - ai.getStart().getTimeInMillis());
			mChronometer.setBase(base);
			mChronometer.start();			
			
		} else if (mode == Actify.MODE_PAUSED) {
			base = SystemClock.elapsedRealtime() - ai.getEnd().getTimeInMillis();
			mChronometer.setBase(base);
		}
		
		final View idColor = (View) rowView.findViewById(R.id.idColor);
		idColor.setBackgroundColor(Color.parseColor(Actify.colorAdapter.getItem(ai.getactivityid()).toString()));
		
		final TextView txtActivity = (TextView) rowView.findViewById(R.id.txtActivity);        
		
		final TextView txtStart = (TextView) rowView.findViewById(R.id.txtStart);    
		txtStart.setText(Actify.datetimeFormat.format(ai.getStart().getTime()));
                
        if (mode == Actify.MODE_PAUSED) {
        	txtActivity.setText(strActivity+getResources().getString(R.string.paused));
        } else {
        	txtActivity.setText(strActivity);
        }
		
		final Button btnStop = (Button) rowView.findViewById(R.id.btnStop);		
		btnStop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	/*alarmManager.cancel(Actify.pendingIntents.get(ai.getId()));
            	Actify.pendingIntents.remove(ai.getId());
            	Actify.pendingIntentTimes.remove(ai.getId());
            	Actify.printIntents();
            	*/
            	Reminder.cancelAlarm(getActivity(), ai.getId());
            	
            	mChronometer.stop();	
            	timer_container.removeView(rowView);   
            	
            	Calendar calEnd = Calendar.getInstance();
            	if (ai.getMode() == Actify.MODE_PAUSED) {
            		ap.setEnd(calEnd);
            		db.updateActivityPause(ap);
            	}
            	
            	ai.setEnd(calEnd);
            	ai.setMode(Actify.MODE_INSERT);
            	db.updateActivity(ai);            	
            	db.deleteLatestActivityLog();
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
            	
            	Toast.makeText(act, 
            			strActivity +res.getString(R.string.toastActivitySaved), 
            			Toast.LENGTH_SHORT).show();            	
            	
            	
            	
            	if (timer_container.getChildCount() == 0) {
            		//setIdleReminder();
            		Reminder.setIdleReminder(getActivity());
            	}
            	
            }
        });		
		
		final Button btnPause = (Button) rowView.findViewById(R.id.btnPause);
		btnPause.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {            	
        		switch (ai.getMode()) {        		
        			case Actify.MODE_RUNNING: {
        				// pausing
        				        				
        				Calendar calStart = Calendar.getInstance();
        				ap.setStart(calStart);
        				ActivityPause newap = db.addActivityPause(ap);  
        				ap.setId(newap.getId());     
        				
        				Calendar calPaused = Calendar.getInstance();
        				calPaused.setTimeInMillis(SystemClock.elapsedRealtime()-mChronometer.getBase());        				
        				mChronometer.stop();         				
                    	ai.setMode(Actify.MODE_PAUSED);
                    	ai.setEnd(calPaused);
        				db.updateActivity(ai);
 		        				
        				btnPause.setBackgroundResource(R.drawable.button_selector_resume);  
        				btnPause.setText(R.string.btnResume);
        				txtActivity.setText(strActivity+getResources().getString(R.string.paused));        			
        				break;
        			}
        			
        			case Actify.MODE_PAUSED: {
        				// resuming
        				ai.setMode(Actify.MODE_RUNNING);
        				db.updateActivity(ai);
        				
        				Calendar calResumed = Calendar.getInstance();  
        				ap.setEnd(calResumed);        				

        				db.updateActivityPause(ap);
        				db.deleteLatestActivityPauseLog();
        				
        				mChronometer.start();        				
        				btnPause.setBackgroundResource(R.drawable.button_selector_pause);
        				btnPause.setText(R.string.btnPause);
        				txtActivity.setText(strActivity);
        				        				            	
        				break;
        			}
        		}
            }
        });
		if (mode == Actify.MODE_PAUSED) {
			btnPause.setBackgroundResource(R.drawable.button_selector_resume);
			btnPause.setText(R.string.btnResume);
		}
			
		final TextView txtLocation = (TextView) rowView.findViewById(R.id.txtLocation);
		txtLocation.setText(getResources().getString(R.string.txtLocation)+strLocation);		
		
		final TextView txtGuest = (TextView) rowView.findViewById(R.id.txtGuest);
		List<String> guestList =  db.getActivityGuestStringList(ai.getId());
		
		if (guestList.isEmpty())
			txtGuest.setText("");
		else {
			String guestNames = "";
			for (String s : guestList) {
				guestNames += s + " ";
			}   
			txtGuest.setText(getResources().getString(R.string.txtGuests)+guestNames);
		}
		
		final LinearLayout ll = (LinearLayout) rowView.findViewById(R.id.ll);
		ll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/********************************* Activity Edit *************************************/
				
				final AlertDialog activityEditDialog  = new AlertDialog.Builder(act).create();
	    	    LayoutInflater inflater = act.getLayoutInflater();
	    	    View dialogView = inflater.inflate(R.layout.activity_edit, null);
	    	    
	    	    final Spinner spinnerActivity = (Spinner) dialogView.findViewById(R.id.spinnerActivity);
	    	    spinnerActivity.setAdapter(Actify.activityAdapter);	    		    	    
	    	    spinnerActivity.setSelection(Actify.findActivitySettingById(ai.getactivityid()).getOrder());
	    	    spinnerActivity.setPadding(2, 2, 2, 2);
	    	    
	    	    final Spinner spinnerLocation = (Spinner) dialogView.findViewById(R.id.spinnerLocation);
	    		spinnerLocation.setAdapter(Actify.locationAdapter);
	    		spinnerLocation.setSelection(ai.getlocationid());
	    		spinnerLocation.setPadding(2, 2, 2, 2);
	    		
	    		final Button btnStart = (Button) dialogView.findViewById(R.id.btnStart);
	    		btnStart.setText(Actify.datetimeFormat.format(ai.getStart().getTime()));
	    		btnStart.setOnClickListener(new OnClickListener() {
	    			
					@Override
					public void onClick(View v) {
						dateTimePicker = new DateTimePicker(act, 
		            			ai.getStart(), new DateTimePickerListener(btnStart, ai),
		            			res.getString(R.string.editActivityHeaderStartTime));
		                dateTimePicker.set24HourFormat(true);
		                dateTimePicker.showDialog();
					}
	    			
	    		});
	    			    	
	    		String dtReminderString = settings.getString("reminder_"+ai.getId(), "");
	    		DateTime dtReminder = DateTime.parse(dtReminderString, Actify.datetimeFormatJoda);
	    		DateTime dtNow = new DateTime();
	    		Duration duration = new Duration(dtNow, dtReminder);
	    		int hours = (int) duration.getStandardHours();
	    		int minutes = (int) duration.getStandardMinutes();
	    		int seconds = (int) duration.getStandardSeconds();
	    		String durationStr;	    		
	    		
	    		if (seconds > 30) {
	    			minutes = minutes + 1;
	    			if (minutes % 60 == 0) {
	    				hours = minutes / 60;
	    			}
	    		} 
	    		
	    		if (hours > 0) {
	    			minutes = minutes - (hours*60);
	    			durationStr = hours+"h "+minutes+"m from now";
	    		} else {
	    			durationStr = minutes+"m from now";
	    		}
	    		final int oldMinutes = minutes;
	    		final int oldHours = hours;
	    		changeReminder = false;
	    		
	    		final Button btnReminder = (Button) dialogView.findViewById(R.id.btnReminder);
	    		btnReminder.setText(durationStr);
	    		btnReminder.setOnClickListener(new OnClickListener() {
	    			TimePickerDialog.OnTimeSetListener nextAlarmSettingListener =new TimePickerDialog.OnTimeSetListener() {
	    				@Override
	    				public void onTimeSet(TimePicker view, int hour, int minute) {
	    					newHour = hour;
	    					newMinute = minute;
	    					String durationStr;
	    					if (newHour > 0)
	    		    			durationStr = newHour+" hours "+newMinute+" mins";
	    		    		else
	    		    			durationStr = newMinute+" mins";
	    					btnReminder.setText(durationStr);
	    					changeReminder = true;
	    				}
	    			};
					@Override
					public void onClick(View v) {
						TimePickerDialog tpDialog = new TimePickerDialog(ActivityFragment.this.getActivity(), 
								nextAlarmSettingListener, oldHours, oldMinutes, true) ;
						tpDialog.setTitle("Reminder");
						tpDialog.setMessage("Remind me after (HH:MM)");
						tpDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
							@Override
							public void onDismiss(DialogInterface dialog) {
								dialog.dismiss();			
							}
						});		
						tpDialog.show();
					}	    			
	    		});
	    		
	    		final Button btnPauses = (Button) dialogView.findViewById(R.id.btnPauses);
	    		btnPauses.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						/********************************* Pauses Edit *************************************/
						String query = db.activityPauseQueryBuilder(Actify.VIEW_TIMER, ai.getId());
						List<ActivityPause> activityPauses = db.getActivityPauseList(query);
						
						if (activityPauses.isEmpty()) {
							Toast.makeText(act, 
									res.getString(R.string.toastActivityNoPauses), 
									Toast.LENGTH_SHORT).show();
						} else {
						
							final AlertDialog dialogPause  = new AlertDialog.Builder(act).create();
							
							ScrollView sv = new ScrollView(act);
						    
							final LinearLayout ll = new LinearLayout(act);
						    ll.setOrientation(LinearLayout.VERTICAL);
						    for (int i=0; i < activityPauses.size(); i++) {
						    	final ActivityPause ap = activityPauses.get(i);
						    	LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
								final View pauseRowView = inflater.inflate(R.layout.activity_pause_row, null);							
								
								final Button btnStart = (Button) pauseRowView.findViewById(R.id.btnStart);		
								final String start = Actify.dateFormat.format(ap.getStart().getTime())+"\n"+
										Actify.timeFormat.format(ap.getStart().getTime());
								btnStart.setText(start);
								btnStart.setId(START);
								btnStart.setOnClickListener(new OnClickListener() {
									
						            public void onClick(View v) {
						            	dateTimePicker = new DateTimePicker(act, 
						            			ap.getStart(), new DateTimePickerListener(btnStart, ap),
						            			res.getString(R.string.datetimePickerHeader));
						                dateTimePicker.set24HourFormat(true);
						                dateTimePicker.showDialog();						                
						            }
						        });
								
								final Button btnEnd = (Button) pauseRowView.findViewById(R.id.btnEnd);		
								final String end = Actify.dateFormat.format(ap.getEnd().getTime())+"\n"+
										Actify.timeFormat.format(ap.getEnd().getTime());
								btnEnd.setText(end);
								btnEnd.setId(END);
								btnEnd.setOnClickListener(new OnClickListener() {
									
						            public void onClick(View v) {
						            	dateTimePicker = new DateTimePicker(act, 
						            			ap.getEnd(), new DateTimePickerListener(btnEnd, ap),
						            			res.getString(R.string.datetimePickerHeader));
						                dateTimePicker.set24HourFormat(true);
						                dateTimePicker.showDialog();						                
						            }
						        });
								
								final ImageButton btnDelete = (ImageButton) pauseRowView.findViewById(R.id.btnDelete);
								btnDelete.setOnClickListener(new OnClickListener() {
						            public void onClick(View v) {
						            	AlertDialog alertDialog = new AlertDialog.Builder(act).create();	
						        		alertDialog.setMessage(res.getString(R.string.editPauseDeleteMsg));	
						        		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, res.getString(R.string.btnYes), 
						        				new DialogInterface.OnClickListener() {
						        	        public void onClick(DialogInterface dialog, int which) {
						        	        	db.deleteActivityPause(ap);
						        	        	ll.removeView(pauseRowView);
						        	        	activityEditDialog.dismiss();
						        	        	dialog.dismiss();
						        	        }
						        		});
						        		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, res.getString(R.string.btnNo), 
						        				new DialogInterface.OnClickListener() {
						        	        public void onClick(DialogInterface dialog, int which) {
						        	        	dialog.dismiss();
						        	        }
						        		});
						        		alertDialog.show();
						            }
						        });
								
								ll.addView(pauseRowView);
						    }
						    sv.addView(ll);
						    
						    dialogPause.setView(sv);
						    dialogPause.setTitle(res.getString(R.string.editActivityTitlePauses));
						    
						    dialogPause.setButton(AlertDialog.BUTTON_POSITIVE, 
						    		res.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
			        	        public void onClick(DialogInterface dialog, int which) {		        	        
			        	        	dialog.dismiss();
			        	        }
			        		});
						    
						    dialogPause.setButton(AlertDialog.BUTTON_NEGATIVE, 
						    		res.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
				    			public void onClick(DialogInterface dialog, int which) {
				    				dialog.dismiss();
				    			}
				    		});				    		
						    
						    dialogPause.show();
						}
					}
	    			
	    		});
	    		
	    		final ArrayList<Guest> addGuests = new ArrayList<Guest>();
	    		final ArrayList<Guest> delGuests = new ArrayList<Guest>();  
	    		final Button btnGuests = (Button) dialogView.findViewById(R.id.btnGuests);
	    		btnGuests.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						/********************************* Activity Guests Edit *************************************/
						String query = db.guestQueryBuilder(ai, householdid);
						final List<Guest> guests = db.getGuestList(query);
						if (guests.isEmpty()) {
							Toast.makeText(act, res.getString(R.string.editActivityToastNoGuest), 
									Toast.LENGTH_SHORT).show();
						} else {
							final AlertDialog dialogGuest  = new AlertDialog.Builder(act).create();
							
							ScrollView sv = new ScrollView(act);
						    
							final LinearLayout ll = new LinearLayout(act);
						    ll.setOrientation(LinearLayout.VERTICAL);
						    						    
						    for (int i=0; i < guests.size(); i++) {
						    	Guest g = guests.get(i);
						    	CheckBox ch = new CheckBox(act);
						    	if (db.isActivityGuest(ai.getId(), g.getId())) {
						    		ch.setChecked(true);
						    	} else 
						    		ch.setChecked(false);
						    	String start = Actify.datetimeFormat.format(g.getStart().getTime());
						    	String end = "...";
						    	if (g.getMode()!=Actify.MODE_RUNNING)
						    		end = Actify.datetimeFormat.format(g.getEnd().getTime());
						    	String text = g.getName() + " ("+ start + " - "+ end +")";
						    	ch.setText(text);
						    	ch.setTextSize(14);
						    	ll.addView(ch);
						    }
						    
						    sv.addView(ll);
						    
						    dialogGuest.setView(sv);
						    dialogGuest.setTitle(res.getString(R.string.editActivityTitleGuest));
						    dialogGuest.setMessage(res.getString(R.string.editActivityMsgGuest));
						    
						    dialogGuest.setButton(AlertDialog.BUTTON_POSITIVE, 
						    		res.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
			        	        public void onClick(DialogInterface dialog, int which) {
			        	        	for (int i=0; i < guests.size(); i++) {
			        	        		CheckBox ch = (CheckBox) ll.getChildAt(i);
			        	        		if (ch.isChecked()) {
			        	        			addGuests.add(guests.get(i));
			        	        		} else
			        	        			delGuests.add(guests.get(i));
								    }
			        	        				        	        
			        	        	dialog.dismiss();
			        	        }
			        		});
						    
						    dialogGuest.setButton(AlertDialog.BUTTON_NEGATIVE, 
						    		res.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
				    			public void onClick(DialogInterface dialog, int which) {
				    				dialog.dismiss();
				    			}
				    		});				    		
						    
						    dialogGuest.show();
						}
					}	    			
	    		});
	    		
	    		activityEditDialog.setView(dialogView);
	    		activityEditDialog.setTitle(res.getString(R.string.editActivityTitleDialog));
	    		activityEditDialog.setButton(AlertDialog.BUTTON_POSITIVE, 
	    				res.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
        	        public void onClick(DialogInterface dialog, int which) {  
        	        	
        	        	int activityid = Actify.findActivitySettingByOrder(spinnerActivity.getSelectedItemPosition()).getId();
        	        	int locationid = spinnerLocation.getSelectedItemPosition();
        	        	ai.setactivityid(activityid);
        	        	ai.setlocationid(locationid);
        				db.updateActivity(ai);
        				
        				for (Guest g : addGuests) {
        					ActivityGuest ag = new ActivityGuest(ai.getId(), g.getId());
        					ag.setMode(Actify.MODE_RUNNING);
        					db.addActivityGuest(ag);
        					//Log.w("guest", "guest");
        				}
        				for (Guest g : delGuests) {
        					ActivityGuest ag = new ActivityGuest(ai.getId(), g.getId());
        					ag.setMode(Actify.MODE_DELETE);
        					db.updateActivityGuest(ag);
        				}
        				
        				List<String> guestList =  db.getActivityGuestStringList(ai.getId());
        				
        				if (guestList.isEmpty())
        					txtGuest.setText("");
        				else {
        					String guestNames = "";
	        				for (String s : guestList) {
	        					guestNames += s + " ";
	        				}     
	        				txtGuest.setText(getResources().getString(R.string.txtGuests)+guestNames);
        				}
        				
        				
        				idColor.setBackgroundColor(Color.parseColor(Actify.colorAdapter.getItem(activityid).toString()));        				
        		        
        				txtLocation.setText(getResources().getString(R.string.txtLocation)
        						+Actify.locationAdapter.getItem(locationid).toString());
        				txtStart.setText(Actify.datetimeFormat.format(ai.getStart().getTime()));
        				
        				long changeStart = ai.getStart().getTimeInMillis() - oriStart;
        				long base;
        				String activityStr = Actify.findActivitySettingById(ai.getactivityid()).getActivity();

        				if (ai.getMode() == Actify.MODE_RUNNING) {	
        					txtActivity.setText(activityStr);
        					mChronometer.stop();
        					base = SystemClock.elapsedRealtime() - (Calendar.getInstance().getTimeInMillis() - ai.getStart().getTimeInMillis());
        					mChronometer.setBase(base);
        					mChronometer.start();			
        					
        				} else if (ai.getMode() == Actify.MODE_PAUSED) {
        					base = SystemClock.elapsedRealtime() - ai.getEnd().getTimeInMillis() + changeStart;
        					mChronometer.setBase(base);
        					mChronometer.stop();
        					txtActivity.setText(activityStr+
        							res.getString(R.string.paused));
        				}
        				
        				if (changeReminder) {
    	    				ActivitySetting as = Actify.findActivitySettingById(activityid);
        					Reminder.cancelAlarm(getActivity(), ai.getId());
        					Reminder.setActivityReminder(getActivity(), ai.getId(), newHour*60+newMinute, as.getActivity());
        					
        				}        				
        				
        	        	dialog.dismiss();
        	        }
        		});
	    		
	    		activityEditDialog.setButton(AlertDialog.BUTTON_NEGATIVE, 
	    				res.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int which) {
	    				dialog.dismiss();
	    			}
	    		});
	    		
	    		activityEditDialog.show(); 

		}});
		timer_container.addView(rowView, timer_container.getChildCount());
	}
		
	
	private OnClickListener startListener = new OnClickListener() {
    	public void onClick(View v) {
    			final AlertDialog activityPickerDialog  = new AlertDialog.Builder(act).create();
 	    		
	    	    LayoutInflater inflater = act.getLayoutInflater();
	    	    View dialogView = inflater.inflate(R.layout.activity_picker, null);
	    	    
	    	    GridView gridView = (GridView) dialogView.findViewById(R.id.grid_view);
	            gridView.setAdapter(new ActivityPickerAdapter(act));        
	            gridView.setOnItemClickListener(new OnItemClickListener() {

	    			@Override
	    			public void onItemClick(AdapterView<?> parent, View v, int position,
	    					long id) {
	    				Reminder.cancelIdleAlarm(getActivity());
	    				
	    				int activityid = (int) id;
	    				ActivitySetting as = Actify.findActivitySettingById(activityid);
	    				String locationStr = as.getLocation();
	    				int locationid = Actify.locationAdapter.getPosition(locationStr);
	        			
	                	ActivityInstance ai = new ActivityInstance(activityid, userid, Calendar.getInstance(), Calendar.getInstance(), locationid);
	                	ai = db.addActivity(ai);
	                	ActivityPause ap = new ActivityPause(ai.getId(), Calendar.getInstance(), Calendar.getInstance());
	                	activityPauses.add(ap);
	                	inflateTimerRow(ai, ap);
	                	
	                	Reminder.setActivityReminder(getActivity(), ai.getId(), as.getDuration(), as.getActivity());
	            		
	                	int duration = as.getDuration();
	            		String durationStr;
	            		if (duration >= 60) {
	            			int hour = (int) Math.floor(duration/60);
	            			int minutes = duration - (hour*60);
	            			durationStr = hour + " hours " + minutes + " minutes"; 
	            		} else {
	            			durationStr = duration + " minutes";
	            		}
	            		Toast.makeText(getActivity(), "Reminder set in " + durationStr,
	            				Toast.LENGTH_LONG).show();
	                	
	            		activityPickerDialog.dismiss();
	                	Actify.showPicker = false;
	                	
	                	 
	    			}
	            });	            
	            activityPickerDialog.setView(dialogView);
	            activityPickerDialog.setTitle("What are you doing right now?");
	            activityPickerDialog.show();   	            	            
	    		
    	}	
    };
	
	
	private class DateTimePickerListener implements DateTimePicker.ICustomDateTimeListener {	
		
		private Button ib;
		private ActivityPause ap;
		private ActivityInstance ai;
		
		
		public DateTimePickerListener(Button ib, ActivityPause ap) {
			this.ib = ib;
			this.ap = ap;
		}
		
		public DateTimePickerListener(Button ib, ActivityInstance ai) {
			this.ib = ib;
			this.ai = ai;
		}

		@Override
		public void onSet(Calendar calendarSelected, Date dateSelected, int year,
				String monthFullName, String monthShortName, int monthNumber,
				int date, String weekDayFullName, String weekDayShortName,
				int hour24, int hour12, int min, int sec, String AM_PM) {
			
			if (calendarSelected.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
				AlertDialog dialog =  new AlertDialog.Builder(act).create();
				dialog.setMessage(res.getString(R.string.check_date_now));
				dialog.setIcon(res.getDrawable(R.drawable.alert));
				dialog.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) {
    		        	dialog.dismiss();
    		        }
    			});
				dialog.show();
			} else {			
				String text = Actify.datetimeFormat.format(calendarSelected.getTime());
				if (ai != null) {				
					ai.setStart(calendarSelected);
				}else if (ap!=null)  {
					if (ib.getId() == START)
						ap.setStart(calendarSelected);		
					else if (ib.getId() == END)
						ap.setEnd(calendarSelected);
					db.updateActivityPause(ap);
				}
				ib.setText(text);
			}
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}	
	}

}
