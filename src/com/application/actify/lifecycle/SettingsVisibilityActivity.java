package com.application.actify.lifecycle;

import android.os.Bundle;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockActivity;
import com.application.actify.R;
import com.application.actify.adapter.SettingsVisibilityAdapter;

public class SettingsVisibilityActivity extends SherlockActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.activity_picker);	
	}	
	
	@Override
	public void onResume() {
		super.onResume();
		GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setAdapter(new SettingsVisibilityAdapter(this));  
	}
}
