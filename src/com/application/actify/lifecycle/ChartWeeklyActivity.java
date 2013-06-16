package com.application.actify.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.Minutes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.application.actify.R;
import com.application.actify.component.BlockData;
import com.application.actify.core.Actify;
import com.application.actify.db.ActifySQLiteHelper;
import com.application.actify.model.ActivityInstance;
import com.application.actify.model.ActivityPause;
import com.application.actify.view.DensityView;

public class ChartWeeklyActivity extends SherlockActivity {
	private DensityView density;
	private DateTime weekStart, weekEnd;
	private ActifySQLiteHelper db;
	private int userid;
	private LinearLayout ll ;
	private TextView txtDate;
	
	 @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(Actify.PREFS_NAME, 0);
        userid = settings.getInt("userid", -1);
        
        weekStart = DateTime.now().withDayOfWeek(DateTimeConstants.MONDAY).withTime(0, 0, 0, 0);
        weekEnd = DateTime.now().withDayOfWeek(DateTimeConstants.SUNDAY).withTime(0, 0, 0, 0).plusDays(1);        
        
        db = new ActifySQLiteHelper(this);
        
        setContentView(R.layout.activity_densitychart);
		 
		 txtDate = (TextView) findViewById(R.id.txtDate);	    
		 ll = (LinearLayout) findViewById(R.id.ll2);		 
				 
		 ImageButton btnPrev = (ImageButton) findViewById(R.id.btnPrev);
	        btnPrev.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					weekStart = weekStart.minusDays(7);
					weekEnd = weekEnd.minusDays(7);
					drawChart();
				}});
	        
	        ImageButton btnNext = (ImageButton) findViewById(R.id.btnNext);
	        btnNext.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					weekStart = weekStart.plusDays(7);
					weekEnd = weekEnd.plusDays(7);
					drawChart();
				}});
	        
	     drawChart();
	 }	 
	 
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {	
		menu.add(1, Actify.MENU_HOME, 0, R.string.menuHome).setIcon(R.drawable.home);
		menu.add(0, Actify.MENU_ACTIVITY_HISTORY, 1, R.string.tabActivityHistory).setIcon(R.drawable.list);
		menu.add(0, Actify.MENU_ACTIVITY_PIE, 2, R.string.tabActivityChart).setIcon(R.drawable.pie_chart);
		
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
		    
	        case Actify.MENU_ACTIVITY_HISTORY:	        	
		          intent = new Intent(getApplicationContext(), ActivityHistoryActivity.class);
		          startActivity(intent);
		          finish();
		          break;
		          
	        case Actify.MENU_ACTIVITY_PIE:        	
		          intent = new Intent(getApplicationContext(), ChartDailyActivity.class);
		          startActivity(intent);
		          finish();
		          break;    	        
		  }		
		  return true;
	  } 

	 private void drawChart() {
		 
		 ll.removeAllViews();
		 txtDate.setText(Actify.dateFormatJoda.print(weekStart)+" - "+Actify.dateFormatJoda.print(weekEnd));
		 density = new DensityView(this, getData());
		 ll.addView(density);
		 
	 }

	 
	 private List<BlockData> getData() {
		 int x, yStart, yEnd, color;
		 List<BlockData> blockData = new ArrayList<BlockData>();
		 String selectQuery = db.activityQueryBuilder(userid, weekStart, weekEnd);
		 List<ActivityInstance> activityInstances = db.getActivityList(selectQuery);
		 for (int i=0; i<activityInstances.size(); i++) {
			 ActivityInstance ai = activityInstances.get(i);
			 DateTime aiStart = new DateTime(ai.getStart());				 
			 DateTime aiEnd = new DateTime(ai.getEnd());
			 DateTime dayStart = aiStart.withTime(0, 0, 0, 0);
			 DateTime dayEnd = dayStart.plusDays(1);
			 
			 selectQuery = db.activityPauseQueryBuilder(Actify.VIEW_HISTORY, ai.getId());
			 List<ActivityPause> pauses = db.getActivityPauseList(selectQuery);
			 
			 color = Color.parseColor(Actify.colorAdapter.getItem(ai.getactivityid()));
			 x = Days.daysBetween(weekStart, aiStart).getDays();
			 yStart = Minutes.minutesBetween(aiStart, dayStart).getMinutes();
			 if (aiStart.getDayOfWeek() == aiEnd.getDayOfWeek()) {				 				 
				 yEnd = Minutes.minutesBetween(aiEnd, dayStart).getMinutes();				 
				 blockData.add(new BlockData(x, yStart, yEnd, color));
			 } else if (aiStart.isAfter(weekStart)){
				 yEnd = Minutes.minutesBetween(dayEnd, dayStart).getMinutes();
				 blockData.add(new BlockData(x, yStart, yEnd, color));		
				 if (aiEnd.isBefore(weekEnd)) {		
					 x += 1;
					 yStart = 0;
					 yEnd = Minutes.minutesBetween(aiEnd, dayEnd).getMinutes();
					 blockData.add(new BlockData(x, yStart, yEnd, color));
				 }
			 } else if (aiStart.isBefore(weekStart)) {
				 x = 0;
				 yStart = 0;
				 yEnd = Minutes.minutesBetween(aiEnd, weekStart).getMinutes();
				 blockData.add(new BlockData(x, yStart, yEnd, color));
			 }
			 // TODO : deal with pauses			 
		 }
		 
		 return blockData;
	 }
	 
}


