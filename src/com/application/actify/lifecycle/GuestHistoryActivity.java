package com.application.actify.lifecycle;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.application.actify.R;
import com.application.actify.core.Actify;
import com.application.actify.db.ActifySQLiteHelper;
import com.application.actify.model.Guest;

public class GuestHistoryActivity extends SherlockActivity {
	static int START = 0, END = 1;
	private LinearLayout history_container;
	private ActifySQLiteHelper db;
	private ScrollView scroller;
	private DateTimePicker dateTimePicker;
	private Calendar cal;
	private List<Guest> guests;
	private String householdid;
	
	
	 @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences settings = getSharedPreferences(Actify.PREFS_NAME, 0);
        householdid = settings.getString("householdid", ""); 
        
        cal = Calendar.getInstance();
                
        db = new ActifySQLiteHelper(this);
        
        String query = db.guestQueryBuilder(householdid, cal);
        guests = db.getGuestList(query);
        
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
				String strQuery = db.guestQueryBuilder(householdid, cal);
		        guests = db.getGuestList(strQuery);
		        history_container.removeAllViewsInLayout();
		        if (guests.isEmpty()) {
		        	scroller.setBackgroundResource(R.drawable.background_blank);
			    } else {  	
			        for (int i=0; i < guests.size(); i++) {
			        	inflateHistoryRow(guests.get(i), i);
			        }
			    }
			}});
        
        ImageButton btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cal.add(Calendar.DATE, 1);
				txtDate.setText(Actify.dateFormat.format(cal.getTime()));
				String strQuery = db.guestQueryBuilder(householdid, cal);
				guests = db.getGuestList(strQuery);
		        history_container.removeAllViewsInLayout();
		        if (guests.isEmpty()) {		        	
		        	scroller.setBackgroundResource(R.drawable.background_blank);
			    } else {  	
			        for (int i=0; i < guests.size(); i++) {
			        	inflateHistoryRow(guests.get(i), i);
			        }
			    }
			}});
        
        scroller = (ScrollView) findViewById(R.id.scroller);        
        if (guests.isEmpty()) {
        	scroller.setBackgroundResource(R.drawable.background_blank);
	    } else {     
	        for (int i=0; i < guests.size(); i++) {
	        	inflateHistoryRow(guests.get(i), i);
	        }
        }
    }
	 
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {	
		menu.add(1, Actify.MENU_HOME, 0, R.string.menuHome).setIcon(R.drawable.home);
		
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
		  }		
		  return true;
	  } 
	 

	private void inflateHistoryRow(final Guest g, final int index) {
		scroller.setBackgroundResource(android.R.color.transparent); 
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		final View rowView = inflater.inflate(R.layout.guest_history_row, null);
		
		final View idColor = (View) rowView.findViewById(R.id.idColor);
		int color = g.getId() % Actify.colorAdapter.getCount();
		idColor.setBackgroundColor(Color.parseColor(Actify.colorAdapter.getItem(color).toString()));
				
		final TextView txtName = (TextView) rowView.findViewById(R.id.txtName);
		txtName.setText(g.getName());
		final TextView txtTimestamp = (TextView) rowView.findViewById(R.id.txtTimestamp);    
		txtTimestamp.setText(Actify.datetimeFormat.format(g.getStart().getTime())+" - "+
				Actify.datetimeFormat.format(g.getEnd().getTime()));	
		
		final ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
		imageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog alertDialog = new AlertDialog.Builder(v.getContext()).create();	
				alertDialog.setMessage(getResources().getString(R.string.editGuestDeleteMsg));	
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.btnYes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// mark for delete, only delete from sqlite after sync with server
						guests.get(index).setSync(Actify.NOT_SYNC);
						guests.get(index).setMode(Actify.MODE_DELETE);
						db.updateGuest(guests.get(index));
						db.updateActivityGuest(guests.get(index).getId(), ActifySQLiteHelper.ACTIVITYGUESTS_GUESTSID, Actify.NOT_SYNC, Actify.MODE_DELETE);
						//sqliteHelper.deleteActivity(activityInstances.get(index));
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
				final AlertDialog guestEditDialog  = new AlertDialog.Builder(v.getContext()).create();
				LayoutInflater inflater = getLayoutInflater();
	    	    View dialogView = inflater.inflate(R.layout.guest_history_edit, null);
				
				final EditText editName = (EditText) dialogView.findViewById(R.id.editName);
				editName.setText(g.getName());
				
				final Button btnStart = (Button) dialogView.findViewById(R.id.btnStart);
	    		btnStart.setText(Actify.datetimeFormat.format(g.getStart().getTime()));
	    		btnStart.setOnClickListener(new OnClickListener() {
	    			
					@Override
					public void onClick(View v) {
						dateTimePicker = new DateTimePicker((Activity) v.getContext(), 
		            			g.getStart(), new DateTimePickerListener(btnStart, g, START),
		            			getResources().getString(R.string.editActivityHeaderStartTime));
		                dateTimePicker.set24HourFormat(true);
		                dateTimePicker.showDialog();
					}
	    			
	    		});
				
				final Button btnEnd = (Button) dialogView.findViewById(R.id.btnEnd);
	    		btnEnd.setText(Actify.datetimeFormat.format(g.getEnd().getTime()));
	    		btnEnd.setOnClickListener(new OnClickListener() {
	    			
					@Override
					public void onClick(View v) {
						dateTimePicker = new DateTimePicker((Activity) v.getContext(), 
		            			g.getStart(), new DateTimePickerListener(btnEnd, g, END),
		            			getResources().getString(R.string.editActivityHeaderStartTime));
		                dateTimePicker.set24HourFormat(true);
		                dateTimePicker.showDialog();
					}
	    			
	    		});			
				
				guestEditDialog.setView(dialogView);
				
	    		guestEditDialog.setTitle(getResources().getString(R.string.editGuestTitleDialog));
	    		guestEditDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.btnOk), new DialogInterface.OnClickListener() {
        	        public void onClick(DialogInterface dialog, int which) {
        	        	String name = editName.getText().toString(); 
        	        	if (name.isEmpty()) {
        	        		Toast.makeText(guestEditDialog.getContext(), getResources().getText(R.string.guest_empty), Toast.LENGTH_SHORT).show();
        	        	} else {
	        	        	g.setName(name);    
	        	        	g.setSync(Actify.NOT_SYNC);
							g.setMode(Actify.MODE_UPDATE);
	        				db.updateGuest(g);
	        				
							txtName.setText(name);
							txtTimestamp.setText(Actify.datetimeFormat.format(g.getStart().getTime())+" - "+
								Actify.datetimeFormat.format(g.getEnd().getTime()));
	        	        	dialog.dismiss();
        	        	}
        	        }
        		});
	    		
	    		guestEditDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int which) {
	    				dialog.dismiss();
	    			}
	    		});
	    		
	    		guestEditDialog.show();
				
			}
			
		});		
			
        
		
		history_container.addView(rowView, history_container.getChildCount());
	}
	
	private class DateTimePickerListener implements DateTimePicker.ICustomDateTimeListener {	
		
		private Button ib;
		private Guest guest;
		private int mode;
		
		
		public DateTimePickerListener(Button ib, Guest guest, int mode) {
			this.ib = ib;
			this.guest = guest;
			this.mode = mode;
		}

		@Override
		public void onSet(Calendar calendarSelected, Date dateSelected, int year,
				String monthFullName, String monthShortName, int monthNumber,
				int date, String weekDayFullName, String weekDayShortName,
				int hour24, int hour12, int min, int sec, String AM_PM) {
			
			String text = Actify.datetimeFormat.format(calendarSelected.getTime());
			ib.setText(text);
					
			if (mode == GuestHistoryActivity.START) {
				guest.setStart(calendarSelected);					
			} else if (mode == GuestHistoryActivity.END) {
				guest.setEnd(calendarSelected);
			}
			guest.setSync(Actify.NOT_SYNC);
			guest.setMode(Actify.MODE_UPDATE);			
			db.updateGuest(guest);
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}	
	}
}


