package com.application.actify.lifecycle;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.application.actify.R;
import com.application.actify.core.Actify;
import com.application.actify.model.ActivitySetting;

public class SettingsLocationActivity extends SherlockActivity {
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private int userid;	
	private LinearLayout location_pref_container;
	private LayoutInflater inflater;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settings = getSharedPreferences(Actify.PREFS_NAME, 0);
		editor = settings.edit();			
		userid = settings.getInt("userid", -1);
		
		inflater = getLayoutInflater();
		
		setContentView(R.layout.location_pref);	
		
			
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		location_pref_container = (LinearLayout) findViewById(R.id.location_pref_container);
		location_pref_container.removeAllViews();
		for (int i=0; i < Actify.activitySettings.size(); i++) {
		  	ActivitySetting as = Actify.activitySettings.get(i);
			String locationStr = as.getLocation(); 			
			int locationid = Actify.locationAdapter.getPosition(locationStr);
			
			View rowView = inflater.inflate(R.layout.location_pref_row, null);
			
			TextView txtActivity = (TextView) rowView.findViewById(R.id.txtActivity);
			txtActivity.setText(as.getActivity());

			final Spinner spinnerLocation = (Spinner) rowView.findViewById(R.id.spinnerLocation);
			spinnerLocation.setAdapter(Actify.locationAdapter);
			spinnerLocation.setSelection(locationid);			
			spinnerLocation.setPadding(2, 2, 2, 2);		
			final int index = i;
			spinnerLocation.post(new Runnable() {
				public void run() {
					spinnerLocation.setOnItemSelectedListener(new LocationOnItemSelectedListener(index));
				}
			});			
			
			location_pref_container.addView(rowView, location_pref_container.getChildCount());
		}
	}

	class LocationOnItemSelectedListener implements OnItemSelectedListener {
		int i;
		
		public LocationOnItemSelectedListener(int i) {
			this.i = i;
		}
		 
		public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
				String strSelected = Actify.locationAdapter.getItem(pos);			
				Actify.activitySettings.get(i).setLocation(strSelected);
				editor.remove("loc_"+Actify.activitySettings.get(i).getId()+"_"+userid);
				editor.commit();
				editor.putString("loc_"+Actify.activitySettings.get(i).getId()+"_"+userid, 
	    				Actify.activitySettings.get(i).getLocation());
				editor.commit();

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}	
	 
	}
}
