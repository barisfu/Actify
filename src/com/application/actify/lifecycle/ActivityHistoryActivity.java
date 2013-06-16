package com.application.actify.lifecycle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.application.actify.R;
import com.application.actify.core.Actify;
import com.application.actify.db.ActifySQLiteHelper;
import com.application.actify.model.ActivityGuest;
import com.application.actify.model.ActivityInstance;
import com.application.actify.model.ActivityPause;
import com.application.actify.model.Guest;

public class ActivityHistoryActivity extends SherlockActivity {
	static int START = 0, END = 1;
	private LinearLayout history_container;
	private ScrollView scroller;
	private DateTimePicker dateTimePicker;	
	private ActifySQLiteHelper db;
	private SharedPreferences settings;
	private int userid;
	private String householdid;
	private List<ActivityInstance> activityInstances;
	private Calendar cal;
	private Resources res;
	
	 @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        res = getResources();
        settings = getSharedPreferences(Actify.PREFS_NAME, 0);
        userid = settings.getInt("userid", -1);
        householdid = settings.getString("householdid", ""); 
        
        cal = Calendar.getInstance();
        
        db = new ActifySQLiteHelper(this);
        
        String strQuery = db.activityQueryBuilder(userid, cal);
        activityInstances = db.getActivityList(strQuery);
        
        setContentView(R.layout.history);
        history_container = (LinearLayout) findViewById(R.id.history_container);        
        
        final TextView txtDate = (TextView) findViewById(R.id.txtDate);
        txtDate.setText(Actify.dateFormat.format(cal.getTime()));
        
        ImageButton btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cal.add(Calendar.DATE, -1);
				txtDate.setText(Actify.dateFormat.format(cal.getTime()));
				String strQuery = db.activityQueryBuilder(userid, cal);
		        activityInstances = db.getActivityList(strQuery);
		        history_container.removeAllViews();
		        history_container.removeAllViewsInLayout();
		        if (activityInstances.isEmpty()) {
		        	scroller.setBackgroundResource(R.drawable.background_blank);
			    } else {  	
			        for (int i=0; i < activityInstances.size(); i++) {			        	
			        	inflateHistoryRow(activityInstances.get(i), i);
			        }
			    }
			}});
        
        ImageButton btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cal.add(Calendar.DATE, 1);
				txtDate.setText(Actify.dateFormat.format(cal.getTime()));
				String strQuery = db.activityQueryBuilder(userid, cal);
		        activityInstances = db.getActivityList(strQuery);
		        history_container.removeAllViews();
		        history_container.removeAllViewsInLayout();
		        if (activityInstances.isEmpty()) {		        	
		        	scroller.setBackgroundResource(R.drawable.background_blank);
			    } else {  	
			        for (int i=0; i < activityInstances.size(); i++) {
			        	inflateHistoryRow(activityInstances.get(i), i);
			        }
			    }
			}});
        
        scroller = (ScrollView) findViewById(R.id.scroller);   
        if (activityInstances.isEmpty()) {
        	scroller.setBackgroundResource(R.drawable.background_blank);
	    } else {  	
	        for (int i=0; i < activityInstances.size(); i++) {
	        	inflateHistoryRow(activityInstances.get(i), i);
	        }
	    }
    }	
	 
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {	
		menu.add(1, Actify.MENU_HOME, 0, R.string.menuHome).setIcon(R.drawable.home);
		menu.add(0, Actify.MENU_ACTIVITY_PIE, 1, R.string.tabActivityChart).setIcon(R.drawable.pie_chart);
		menu.add(0, Actify.MENU_ACTIVITY_DENSITY, 2, R.string.tabActivityDensity).setIcon(R.drawable.bar_chart);
		
		return super.onCreateOptionsMenu(menu);
	}	
	 
	 @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
		  Intent intent;
		  switch (item.getItemId()) {
	        
	        case Actify.MENU_HOME:	        	
		          intent = new Intent(getApplicationContext(), MainFragmentActivity.class);
		          startActivity(intent);
		          finish();
		          break;
		    
	        case Actify.MENU_ACTIVITY_PIE:	        	
		          intent = new Intent(getApplicationContext(), ChartDailyActivity.class);
		          startActivity(intent);
		          finish();
		          break;
		          
	        case Actify.MENU_ACTIVITY_DENSITY:        	
		          intent = new Intent(getApplicationContext(), ChartWeeklyActivity.class);
		          startActivity(intent);
		          finish();
		          break;    	        
		  }		
		  return true;
	  }  
	
	private void inflateHistoryRow(final ActivityInstance ai, final int index) {
		scroller.setBackgroundResource(android.R.color.transparent); 
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		final View rowView = inflater.inflate(R.layout.activity_history_row, null);		
		
		final View idColor = (View) rowView.findViewById(R.id.idColor);
		idColor.setBackgroundColor(Color.parseColor(Actify.colorAdapter.getItem(ai.getactivityid()).toString()));
		final TextView txtActivity = (TextView) rowView.findViewById(R.id.txtActivity);
		txtActivity.setText(Actify.findActivitySettingById(ai.getactivityid()).getActivity());
		
		final TextView txtTimestamp = (TextView) rowView.findViewById(R.id.txtTimestamp);    
		txtTimestamp.setText(Actify.datetimeFormat.format(ai.getStart().getTime())+" - "+
				Actify.datetimeFormat.format(ai.getEnd().getTime()));
		
		final TextView txtLocation = (TextView) rowView.findViewById(R.id.txtLocation);
		txtLocation.setText(getResources().getString(R.string.txtLocation)+Actify.locationAdapter.getItem(ai.getlocationid()));
		
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
		
		final ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
            	AlertDialog alertDialog = new AlertDialog.Builder(v.getContext()).create();	
        		alertDialog.setMessage(getResources().getString(R.string.editActivityDeleteMsg));	
        		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.btnYes), new DialogInterface.OnClickListener() {
        	        public void onClick(DialogInterface dialog, int which) {
        	        	// mark for delete, only delete from sqlite after sync with server
        	        	activityInstances.get(index).setSync(Actify.NOT_SYNC);
        	        	activityInstances.get(index).setMode(Actify.MODE_DELETE);
        	        	db.updateActivity(activityInstances.get(index));
        	        	db.updateActivityPause(activityInstances.get(index).getId(), Actify.NOT_SYNC, Actify.MODE_DELETE);
        	        	db.updateActivityGuest(activityInstances.get(index).getId(), ActifySQLiteHelper.ACTIVITYGUESTS_ACTIVITIESID, Actify.NOT_SYNC, Actify.MODE_DELETE);
        	        	history_container.removeView(rowView);
        	        	dialog.dismiss();
        	        }
        		});
        		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.btnNo), new DialogInterface.OnClickListener() {
        	        public void onClick(DialogInterface dialog, int which) {
        	        	dialog.dismiss();
        	        }
        		});
        		alertDialog.show();
            }		
		});
		
		final LinearLayout ll = (LinearLayout) rowView.findViewById(R.id.ll);		
		ll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/********************************* Activity Edit *************************************/
				final AlertDialog activityEditDialog  = new AlertDialog.Builder(v.getContext()).create();
	    	    LayoutInflater inflater = getLayoutInflater();
	    	    View dialogView = inflater.inflate(R.layout.activity_history_edit, null);
	    	    
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
						dateTimePicker = new DateTimePicker((Activity)v.getContext(), 
		            			ai.getStart(), new DateTimePickerListener(btnStart, ai, START),
		            			res.getString(R.string.editActivityHeaderStartTime));
		                dateTimePicker.set24HourFormat(true);
		                dateTimePicker.showDialog();
					}
	    			
	    		});
				
				final Button btnEnd = (Button) dialogView.findViewById(R.id.btnEnd);	
				btnEnd.setText(Actify.datetimeFormat.format(ai.getEnd().getTime()));
	    		btnEnd.setOnClickListener(new OnClickListener() {
	    			
					@Override
					public void onClick(View v) {
						dateTimePicker = new DateTimePicker((Activity)v.getContext(), 
		            			ai.getEnd(), new DateTimePickerListener(btnEnd, ai, END),
		            			res.getString(R.string.editActivityHeaderEndTime));
		                dateTimePicker.set24HourFormat(true);
		                dateTimePicker.showDialog();
					}
	    			
	    		});
	    		
	    		final Button btnPauses = (Button) dialogView.findViewById(R.id.btnPauses);
	    		btnPauses.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						/********************************* Pauses Edit *************************************/
						String query = db.activityPauseQueryBuilder(Actify.VIEW_HISTORY, ai.getId());
						List<ActivityPause> activityPauses = db.getActivityPauseList(query);
						
						if (activityPauses.isEmpty()) {
							Toast.makeText(arg0.getContext(), res.getString(R.string.toastActivityNoPauses), Toast.LENGTH_SHORT).show();
						} else {
						
							final AlertDialog dialogPause  = new AlertDialog.Builder(arg0.getContext()).create();
							
							ScrollView sv = new ScrollView(arg0.getContext());
						    
							final LinearLayout ll = new LinearLayout(arg0.getContext());
						    ll.setOrientation(LinearLayout.VERTICAL);
						    for (int i=0; i < activityPauses.size(); i++) {
						    	final ActivityPause ap = activityPauses.get(i);
						    	LayoutInflater inflater = (LayoutInflater) arg0.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
								final View pauseRowView = inflater.inflate(R.layout.activity_pause_row, null);
								
								final Button btnStart = (Button) pauseRowView.findViewById(R.id.btnStart);		
								final String start = Actify.datetimeFormat.format(ap.getStart().getTime());
								btnStart.setText(start);
								btnStart.setId(START);
								btnStart.setOnClickListener(new OnClickListener() {
									
						            public void onClick(View v) {
						            	dateTimePicker = new DateTimePicker((Activity)v.getContext(), 
						            			ap.getStart(), new DateTimePickerListener(btnStart, ap),
						            			res.getString(R.string.datetimePickerHeader));
						                dateTimePicker.set24HourFormat(true);
						                dateTimePicker.showDialog();						                
						            }
						        });
								
								final Button btnEnd = (Button) pauseRowView.findViewById(R.id.btnEnd);		
								final String end = Actify.datetimeFormat.format(ap.getEnd().getTime());
								btnEnd.setText(end);
								btnEnd.setId(END);
								btnEnd.setOnClickListener(new OnClickListener() {
									
						            public void onClick(View v) {
						            	dateTimePicker = new DateTimePicker((Activity)v.getContext(), 
						            			ap.getEnd(), new DateTimePickerListener(btnEnd, ap),
						            			res.getString(R.string.datetimePickerHeader));
						                dateTimePicker.set24HourFormat(true);
						                dateTimePicker.showDialog();						                
						            }
						        });
								
								final ImageButton btnDelete = (ImageButton) pauseRowView.findViewById(R.id.btnDelete);
								btnDelete.setOnClickListener(new OnClickListener() {
						            public void onClick(View v) {
						            	AlertDialog alertDialog = new AlertDialog.Builder(v.getContext()).create();	
						        		alertDialog.setMessage(res.getString(R.string.editPauseDeleteMsg));	
						        		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, res.getString(R.string.btnYes), new DialogInterface.OnClickListener() {
						        	        public void onClick(DialogInterface dialog, int which) {
						        	        	ap.setMode(Actify.MODE_DELETE);
						        	        	ap.setSync(Actify.NOT_SYNC);
						        	        	db.updateActivityPause(ap);
						        	        	ll.removeView(pauseRowView);
						        	        	dialog.dismiss();
						        	        }
						        		});
						        		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, res.getString(R.string.btnNo), new DialogInterface.OnClickListener() {
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
						    
						    dialogPause.setButton(AlertDialog.BUTTON_POSITIVE, res.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
			        	        public void onClick(DialogInterface dialog, int which) {		        	        
			        	        	dialog.dismiss();
			        	        }
			        		});
						    
						    dialogPause.setButton(AlertDialog.BUTTON_NEGATIVE, res.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
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
						/********************************* Guests Edit *************************************/
						String query = db.guestQueryBuilder(ai, householdid);
						final List<Guest> guests = db.getGuestList(query);
						
						if (guests.isEmpty()) {
							Toast.makeText(v.getContext(), res.getString(R.string.editActivityHistoryToastNoGuest), Toast.LENGTH_SHORT).show();
						} else {
							final AlertDialog dialogGuest  = new AlertDialog.Builder(v.getContext()).create();
							
							ScrollView sv = new ScrollView(v.getContext());
						    
							final LinearLayout ll = new LinearLayout(v.getContext());
						    ll.setOrientation(LinearLayout.VERTICAL);
						    						    
						    for (int i=0; i < guests.size(); i++) {
						    	Guest g = guests.get(i);
						    	CheckBox ch = new CheckBox(v.getContext());
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
						    	ch.setTextColor(res.getColor(R.color.btnTextColor));
						    	ll.addView(ch);
						    }
						    
						    sv.addView(ll);
						    
						    dialogGuest.setView(sv);
						    dialogGuest.setTitle(res.getString(R.string.editActivityTitleGuest));
						    dialogGuest.setMessage(res.getString(R.string.editActivityMsgHistoryGuest));
						    
						    dialogGuest.setButton(AlertDialog.BUTTON_POSITIVE, res.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
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
						    
						    dialogGuest.setButton(AlertDialog.BUTTON_NEGATIVE, res.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
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
	    		activityEditDialog.setButton(AlertDialog.BUTTON_POSITIVE, res.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
        	        public void onClick(DialogInterface dialog, int which) {
        	        	int activityid = Actify.findActivitySettingByOrder(spinnerActivity.getSelectedItemPosition()).getId();
        	        	int locationid = spinnerLocation.getSelectedItemPosition();
        	        	ai.setactivityid(activityid);
        	        	ai.setlocationid(locationid);
        	        	ai.setSync(Actify.NOT_SYNC);
        				db.updateActivity(ai);
        							
        				for (Guest g : addGuests) {
        					if (!db.isActivityGuest(ai.getId(), g.getId())) {
	        					ActivityGuest ag = new ActivityGuest(ai.getId(), g.getId());
	        					ag.setMode(Actify.MODE_INSERT);
	        					db.addActivityGuest(ag);
        					}
        				}
        				for (Guest g : delGuests) {
        					if (db.isActivityGuest(ai.getId(), g.getId())) {
	        					ActivityGuest ag = new ActivityGuest(ai.getId(), g.getId());
	        					ag.setMode(Actify.MODE_DELETE);
	        					db.updateActivityGuest(ag);
        					}
        				}
        				
        				List<String> guestList =  db.getActivityGuestStringList(ai.getId());
        				String guestNames = "";
        				if (guestList.isEmpty())
        					guestNames = " - ";
        				else {
	        				for (String s : guestList) {
	        					guestNames += s + " ";
	        				}   
							txtGuest.setText(getResources().getString(R.string.txtGuests)+guestNames);
        				}
						txtActivity.setText(Actify.findActivitySettingById(activityid).getActivity());						
						idColor.setBackgroundColor(Color.parseColor(Actify.colorAdapter.getItem(activityid).toString())); 
						
						txtLocation.setText(getResources().getString(R.string.txtLocation)
        						+Actify.locationAdapter.getItem(locationid).toString());
    				
						txtTimestamp.setText(Actify.datetimeFormat.format(ai.getStart().getTime())+" - "+
								Actify.datetimeFormat.format(ai.getEnd().getTime()));
						
        	        	dialog.dismiss();
        	        }
        		});
	    		
	    		activityEditDialog.setButton(AlertDialog.BUTTON_NEGATIVE, res.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int which) {
	    				dialog.dismiss();
	    			}
	    		});
	    		
	    		activityEditDialog.show(); 

				
			}
			
		});
				
		history_container.addView(rowView, history_container.getChildCount());
	}
	
	private class DateTimePickerListener implements DateTimePicker.ICustomDateTimeListener {	
		
		private Button ib;
		private int mode;
		private ActivityPause ap;
		private ActivityInstance ai;
		
		
		public DateTimePickerListener(Button ib, ActivityInstance ai, int mode) {
			this.ib = ib;
			this.ai = ai;
			this.mode = mode;
		}
		
		public DateTimePickerListener(Button ib,  ActivityPause ap) {
			this.ib = ib;
			this.ap = ap;
		}

		@Override
		public void onSet(Calendar calendarSelected, Date dateSelected, int year,
				String monthFullName, String monthShortName, int monthNumber,
				int date, String weekDayFullName, String weekDayShortName,
				int hour24, int hour12, int min, int sec, String AM_PM) {
			if (calendarSelected.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
				AlertDialog dialog =  new AlertDialog.Builder(ib.getContext()).create();
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
				
				if (ap==null) {			
					
					if (mode == ActivityHistoryActivity.START) {
						ai.setStart(calendarSelected);					
					} else if (mode == ActivityHistoryActivity.END) {
						ai.setEnd(calendarSelected);
					}
					ai.setSync(Actify.NOT_SYNC);
					ai.setMode(Actify.MODE_UPDATE);			
					db.updateActivity(ai);
				} else {
					if (ib.getId() == START)
						ap.setStart(calendarSelected);		
					else if (ib.getId() == END)
						ap.setEnd(calendarSelected);	
					ap.setSync(Actify.NOT_SYNC);
					ap.setMode(Actify.MODE_UPDATE);	
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


