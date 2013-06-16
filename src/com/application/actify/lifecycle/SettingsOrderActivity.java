package com.application.actify.lifecycle;

import android.os.Bundle;
import ca.laplanete.mobile.pageddragdropgrid.PagedDragDropGrid;

import com.actionbarsherlock.app.SherlockActivity;
import com.application.actify.R;
import com.application.actify.adapter.SettingsOrderAdapter;

public class SettingsOrderActivity extends SherlockActivity{
	private PagedDragDropGrid gridview;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);	
	}
	
	@Override
	public void onResume() {
		super.onResume();
		gridview = (PagedDragDropGrid) findViewById(R.id.gridview);	
		SettingsOrderAdapter adapter = new SettingsOrderAdapter(this, gridview);		
        gridview.setAdapter(adapter); 
	}
}
