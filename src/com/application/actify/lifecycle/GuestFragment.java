package com.application.actify.lifecycle;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.application.actify.R;
import com.application.actify.core.Actify;
import com.application.actify.db.ActifySQLiteHelper;
import com.application.actify.model.Guest;

public class GuestFragment extends SherlockFragment{
			
	private LinearLayout timer_container;
	private Button btnAdd;
	private View view;
	private ScrollView scroller;
	private DateTimePicker dateTimePicker;
	
	private List<Guest> guests;
	private String householdid;
	
	private ActifySQLiteHelper db;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setHasOptionsMenu(true);
        
        SharedPreferences settings = getActivity().getSharedPreferences(Actify.PREFS_NAME, 0);
        householdid = settings.getString("householdid", "");  
        
        db = new ActifySQLiteHelper(getActivity());
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, Actify.MENU_GUEST_HISTORY, 0, R.string.tabGuestHistory).setIcon(R.drawable.list);
		menu.add(0, Actify.MENU_SETTING, 1, R.string.menuSettings).setIcon(R.drawable.settings);
		menu.add(0, Actify.MENU_LOGOUT, 2, R.string.menuLogout).setIcon(R.drawable.logout);				
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		String query = db.guestQueryBuilder(householdid);
		guests = db.getGuestList(query);
		
		view = inflater.inflate(R.layout.guest, container, false);
        timer_container = (LinearLayout) view.findViewById(R.id.timer_container);        
        btnAdd = (Button) view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(startListener);  
        
        scroller = (ScrollView) view.findViewById(R.id.scroller);
        
        if (guests.isEmpty()) {
        	scroller.setBackgroundResource(R.drawable.background_blank);
        } else {     
	        for (Guest g : guests) {
	        	inflateTimerRow(g);
	        }
        }
              
        return view;
    }
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
	}
	
	// Helper for inflating a row
	private void inflateTimerRow(final Guest g) {	
		scroller.setBackgroundResource(android.R.color.transparent); 
				
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		final View rowView = inflater.inflate(R.layout.guest_row, null);
		
		final TextView txtName = (TextView) rowView.findViewById(R.id.txtName);   		
        txtName.setText(g.getName());
        
        final TextView txtStart = (TextView) rowView.findViewById(R.id.txtStart);    
		txtStart.setText(Actify.datetimeFormat.format(g.getStart().getTime()));
              
        final View idColor = (View) rowView.findViewById(R.id.idColor);
        int color = g.getId() % Actify.colorAdapter.getCount();
        idColor.setBackgroundColor(Color.parseColor(Actify.colorAdapter.getItem(color)));
				
		final Chronometer mChronometer = (Chronometer) rowView.findViewById(R.id.chronometer);	
		
		long base = SystemClock.elapsedRealtime() - (Calendar.getInstance().getTimeInMillis() - g.getStart().getTimeInMillis());
		mChronometer.setBase(base);
		mChronometer.start();							
		
		final Button btnStop = (Button) rowView.findViewById(R.id.btnStop);		
		btnStop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	mChronometer.stop();	
            	timer_container.removeView(rowView);   
            	
            	Calendar calEnd = Calendar.getInstance();
            	g.setEnd(calEnd);
            	g.setMode(Actify.MODE_INSERT);
            	db.updateGuest(g);
            	db.deleteLatestGuestLog();
            	
            	Toast.makeText(getActivity(), 
            			g.getName() + getActivity().getResources().getString(R.string.toastGuestSaved), Toast.LENGTH_SHORT).show();
            }
        });
								
		rowView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog guestEditDialog  = new AlertDialog.Builder(getActivity()).create();
				LayoutInflater inflater = getActivity().getLayoutInflater();
	    	    View dialogView = inflater.inflate(R.layout.guest_edit, null);
				
				final EditText editName = (EditText) dialogView.findViewById(R.id.editName);
				editName.setText(g.getName());
				
				final Button btnStart = (Button) dialogView.findViewById(R.id.btnStart);
	    		btnStart.setText(Actify.datetimeFormat.format(g.getStart().getTime()));
	    		btnStart.setOnClickListener(new OnClickListener() {
	    			
					@Override
					public void onClick(View v) {
						dateTimePicker = new DateTimePicker(getActivity(), 
		            			g.getStart(), new DateTimePickerListener(btnStart, g),
		            			getActivity().getResources().getString(R.string.editActivityHeaderStartTime));
		                dateTimePicker.set24HourFormat(true);
		                dateTimePicker.showDialog();
					}
	    			
	    		});

				guestEditDialog.setView(dialogView);
				
	    		guestEditDialog.setTitle(getActivity().getResources().getString(R.string.editGuestTitleDialog));
	    		guestEditDialog.setButton(AlertDialog.BUTTON_POSITIVE, getActivity().getResources().getString(R.string.btnOk), 
	    				new DialogInterface.OnClickListener() {
	    			
        	        public void onClick(DialogInterface dialog, int which) {
        	        	String name = editName.getText().toString(); 
        	        	if (name.isEmpty()) {
        	        		Toast.makeText(getActivity(), getResources().getText(R.string.guest_empty), Toast.LENGTH_SHORT).show();
        	        	} else {
	        	        	g.setName(name);     
	        				db.updateGuest(g);
	        				txtName.setText(name);
	        				txtStart.setText(Actify.datetimeFormat.format(g.getStart().getTime()));
	        				mChronometer.stop();
        					long base = SystemClock.elapsedRealtime() - (Calendar.getInstance().getTimeInMillis() - g.getStart().getTimeInMillis());
        					mChronometer.setBase(base);
        					mChronometer.start();	
	        	        	dialog.dismiss();
        	        	}
        	        }
        		});
	    		
	    		guestEditDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getActivity().getResources().getString(R.string.btnCancel), 
	    				new DialogInterface.OnClickListener() {
	    			
	    			public void onClick(DialogInterface dialog, int which) {
	    				dialog.dismiss();
	    			}
	    		});
	    		guestEditDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
	    		
	    		guestEditDialog.show(); 

		}});
		
		timer_container.addView(rowView, timer_container.getChildCount());
	}
	
	private OnClickListener startListener = new OnClickListener() {
    	public void onClick(View v) {
    		if (guests.size() < Actify.MAX_NUM_GUEST) {
    			final AlertDialog guestEditDialog  = new AlertDialog.Builder(getActivity()).create();
				
				final EditText editName = new EditText(getActivity());
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				        LinearLayout.LayoutParams.MATCH_PARENT,
				        LinearLayout.LayoutParams.WRAP_CONTENT);
				lp.setMargins(10, 10, 10, 10);
				editName.setText("Guest "+String.valueOf(timer_container.getChildCount()));
				editName.setLayoutParams(lp);
				editName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				    @Override
				    public void onFocusChange(View v, boolean hasFocus) {
				        if (hasFocus) {
				        	guestEditDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				        }
				    }
				});				
				guestEditDialog.setView(editName);				
				
	    		guestEditDialog.setTitle("Add a guest");
	    		guestEditDialog.setMessage("Enter a name for this guest");
	    		guestEditDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
        	        public void onClick(DialogInterface dialog, int which) {
        	        	String name = editName.getText().toString(); 
        	        	if (name.isEmpty()) {
        	        		Toast.makeText(getActivity(), getResources().getText(R.string.guest_empty), Toast.LENGTH_SHORT).show();
        	        	} else {
	        	        	Guest g = new Guest(name, Calendar.getInstance(), Calendar.getInstance(), householdid);   
	        				g = db.addGuest(g);
	        				inflateTimerRow(g);
	        	        	dialog.dismiss();
        	        	}
        	        }
        		});
	    		
	    		guestEditDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int which) {
	    				dialog.dismiss();
	    			}
	    		});
	    		
	    		
	    		guestEditDialog.show();    
	    		
    		} else {    			
    			AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();	
    			alertDialog.setMessage("You can only have maximum "+Actify.MAX_NUM_GUEST+" guests");		
    			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
    		        public void onClick(DialogInterface dialog, int which) {
    		        	dialog.dismiss();
    		        }
    			});
    			alertDialog.show();
    		}
    	}	
    };
	
	
	private class DateTimePickerListener implements DateTimePicker.ICustomDateTimeListener {	
		
		private Button ib;
		private Guest guest;
		
		
		public DateTimePickerListener(Button ib, Guest guest) {
			this.ib = ib;
			this.guest = guest;
		}

		@Override
		public void onSet(Calendar calendarSelected, Date dateSelected, int year,
				String monthFullName, String monthShortName, int monthNumber,
				int date, String weekDayFullName, String weekDayShortName,
				int hour24, int hour12, int min, int sec, String AM_PM) {
			String text = Actify.datetimeFormat.format(calendarSelected.getTime());
			guest.setStart(calendarSelected);
			ib.setText(text);
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}	
	}
}
