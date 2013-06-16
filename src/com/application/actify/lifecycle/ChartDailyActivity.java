package com.application.actify.lifecycle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.application.actify.R;
import com.application.actify.component.SliceInfo;
import com.application.actify.core.Actify;
import com.application.actify.core.Util;
import com.application.actify.db.ActifySQLiteHelper;
import com.saulpower.piechart.adapter.PieChartAdapter;
import com.saulpower.piechart.views.PieChartView;
import com.saulpower.piechart.views.PieChartView.PieChartAnchor;

public class ChartDailyActivity extends SherlockActivity {
	static int START = 0, END = 1;
	private ActifySQLiteHelper db;
	private SharedPreferences settings;
	private int userid;
	private List<SliceInfo> sliceInfos;
	private Calendar calStart, calEnd;
	private PieChartView chart;
	private TextView txtDescription;
	
	 @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences(Actify.PREFS_NAME, 0);
        userid = settings.getInt("userid", -1);
        
        calStart = Calendar.getInstance();
        calStart = Util.getStartDay(calStart);
        calEnd = Util.getEndDay(calStart);
        
        db = new ActifySQLiteHelper(this);
        
        sliceInfos = new ArrayList<SliceInfo>();
        
        setContentView(R.layout.activity_piechart);		
       
        final TextView txtDate = (TextView) findViewById(R.id.txtDate);
        txtDate.setText(Actify.dateFormat.format(calStart.getTime()));
        
        final LinearLayout llinner = (LinearLayout) findViewById(R.id.llinner);
        chart = new PieChartView(this);
        llinner.addView(chart, 400, 400);
        //llinner.addView(chart);
        
        txtDescription = new TextView(this);
        txtDescription.setTextSize(14);
        txtDescription.setMaxLines(100);
        txtDescription.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (sliceInfos.size() > 0) {
					AlertDialog dialog = new AlertDialog.Builder(v.getContext()).create();
					LayoutInflater inflater = getLayoutInflater();
					View dialogView = inflater.inflate(R.layout.slice_infos, null);
					
					TextView textView = (TextView) dialogView.findViewById(R.id.txtInfo);
					
					String text = "";
					for (int i = 0; i < sliceInfos.size(); i++) {
						SliceInfo si = sliceInfos.get(i);
						String activity = Actify.findActivitySettingById(si.getActivityid()).getActivity();
						String count = String.valueOf(si.getCount())+ "x";
						
						long lduration = (long) si.getDuration();
						String hours = TimeUnit.SECONDS.toHours(lduration) + "h";
						String minutes = TimeUnit.SECONDS.toMinutes(lduration) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(lduration)) + "m";
						String seconds =  lduration - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(lduration)) + "s";
						String duration = hours + " " + minutes + " " + seconds;
						
						text += activity+", "+count+", "+duration+"\n";
					}
					textView.setText(text);
					
					dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.btnOk), 
							new DialogInterface.OnClickListener() {
			    		        public void onClick(DialogInterface dialog, int which) {
			    		        	dialog.dismiss();
			    		        }
	    			});
					
					dialog.setTitle(txtDate.getText());
					
					dialog.setView(dialogView);						
					dialog.show();
				}
			}
        	
        });	 
        llinner.addView(txtDescription);
        
        drawChart();	             	        	              	            	        	        	      	        
        
        ImageButton btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				calStart.add(Calendar.DATE, -1);
				calEnd.add(Calendar.DATE, -1);
				txtDate.setText(Actify.dateFormat.format(calStart.getTime()));
				drawChart();
			}});
        
        ImageButton btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				calStart.add(Calendar.DATE, 1);
				calEnd.add(Calendar.DATE, 1);
				txtDate.setText(Actify.dateFormat.format(calStart.getTime()));
				drawChart();
			}});	        	
	 }
	 
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {	
		menu.add(1, Actify.MENU_HOME, 0, R.string.menuHome).setIcon(R.drawable.home);
		menu.add(0, Actify.MENU_ACTIVITY_HISTORY, 1, R.string.tabActivityHistory).setIcon(R.drawable.list);
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
		    
	        case Actify.MENU_ACTIVITY_HISTORY:	        	
		          intent = new Intent(getApplicationContext(), ActivityHistoryActivity.class);
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
	 
	 private void drawChart() {
		 sliceInfos = db.getActivityDuration(userid, calStart, calEnd);
		 if (sliceInfos.size()==0) {
			 txtDescription.setText("");
			 chart.setVisibility(PieChartView.INVISIBLE);
			 chart.setBackgroundResource(R.drawable.blank);
			 
		 } else {
			 chart.setBackgroundResource(android.R.color.transparent);
			 List<Float> slices = calculateSlice();
			 List<Integer> colors = getColors();		 
			 
			 PieChartAdapter adapter = new PieChartAdapter(this, slices, colors);
			 chart.setVisibility(PieChartView.VISIBLE);
			 chart.setSnapToAnchor(PieChartAnchor.BOTTOM);
			 chart.setAdapter(adapter);
			 chart.setOnItemClickListener(new PieChartItemClickListener());
			 chart.onResume();
			 
			 
			 String str, spannable = "";
			 ArrayList<Integer> lengths = new ArrayList<Integer>();
			 for (int i=0; i<slices.size(); i++) {
				 int val = Math.round(slices.get(i)*100);
				 String strVal;
				 if (val == 0) 
					 strVal = "<1";
				 else strVal = String.valueOf(val);
				 str =  strVal + "% " + Actify.findActivitySettingById(sliceInfos.get(i).getActivityid()).getActivity() + "   ";
				 lengths.add(str.length());
				 spannable += str;
			 }
			 
			 SpannableString text = new SpannableString(spannable);  
			 int l = 0;
			 for (int i=0; i<slices.size(); i++) {
				 text.setSpan(new ForegroundColorSpan(colors.get(i)), l, l+lengths.get(i)-1, 0);
				 l += lengths.get(i);
			 }			 
			 
			 txtDescription.setText(text);
		 }
	 }
	 
	 private List<Float> calculateSlice() {
		 List<Float> slices = new ArrayList<Float>();
		 float total = 0;
		 float duration, pauseDuration;
		 		 
		 
		 for (int i=0; i < sliceInfos.size(); i++) {
			 pauseDuration = db.getPauseDuration(sliceInfos.get(i).getActivityid(), userid, calStart, calEnd);
			 duration = sliceInfos.get(i).getDuration() - pauseDuration;
			 slices.add(duration);
			 total += duration;
		 }
		 for (int i=0; i < slices.size(); i++) {
			 slices.set(i, slices.get(i)/total);
		 }
		 return slices;
	 }
	 
	 private List<Integer> getColors() {
		 List<Integer> colors = new ArrayList<Integer>();
		 for (int i=0; i < sliceInfos.size(); i++) {
			 colors.add(Color.parseColor(Actify.colorAdapter.getItem(sliceInfos.get(i).getActivityid()).toString()));
		 }
		 return colors;
	 }

	    
	class PieChartItemClickListener implements PieChartView.OnItemClickListener {

		@Override
		public void onItemClick(boolean secondTap, View parent,
				Drawable drawable, int position, long id) {
			SliceInfo si = sliceInfos.get(position);
			String activity = Actify.findActivitySettingById(si.getActivityid()).getActivity();
			String count = String.valueOf(si.getCount())+ " time";
			if (si.getCount() > 1)
				count += "s";
			
			long lduration = (long) si.getDuration();
			String hours = TimeUnit.SECONDS.toHours(lduration) + " hrs";
			String minutes = TimeUnit.SECONDS.toMinutes(lduration) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(lduration)) + " mins";
			String seconds =  lduration - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(lduration)) + " secs";
			String duration = hours + " " + minutes + " " + seconds;
			
			String message = activity+"\n"+count+"\n"+duration;
			Toast.makeText(parent.getContext(), message , Toast.LENGTH_SHORT).show();							
		}
    	
    }      
}


