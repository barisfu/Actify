package com.application.actify.lifecycle;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.application.actify.R;
import com.application.actify.core.Actify;
import com.application.actify.db.ActifySQLiteHelper;
import com.application.actify.model.ActivityGuest;
import com.application.actify.model.ActivityInstance;
import com.application.actify.model.ActivityPause;
import com.application.actify.model.ActivitySetting;
import com.application.actify.model.Guest;
import com.application.actify.parser.XMLParser;

public class MainFragmentActivity extends SherlockFragmentActivity {
	public static Context appContext;
	
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private int userid;				
	private boolean logout = false;			
	private ActifySQLiteHelper db;
	private AssetManager assetManager;
	private Document docActivitySettings;
	
	private ViewPager viewPager;
	private TabsAdapter tabsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);	  
		
		viewPager = new ViewPager(this);
		viewPager.setId(R.id.pager);
		setContentView(viewPager);		
		
		assetManager = getAssets();
		settings = getSharedPreferences(Actify.PREFS_NAME, 0);
		editor = settings.edit();				
		
		db = new ActifySQLiteHelper(this);
		
		boolean loggedin = settings.getBoolean("logged_in", false);
					
		if (loggedin) {
			userid = settings.getInt("userid", -1);
			
			loadSettings();		
			
			ActionBar bar = getSupportActionBar();
			bar.setDisplayShowHomeEnabled(false);
			bar.setDisplayShowTitleEnabled(false); 
			bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			
			/*ActionBar.Tab tabActivity = bar.newTab().setText(getResources().getString(R.string.tabActivity));
		    ActionBar.Tab tabGuest = bar.newTab().setText(getResources().getString(R.string.tabGuest));

		    tabActivity.setTabListener(new MyTabListener());
		    tabGuest.setTabListener(new MyTabListener());
	        bar.addTab(tabActivity);
	        bar.addTab(tabGuest);
	        */
		    
			tabsAdapter = new TabsAdapter(this, viewPager);
			
			tabsAdapter.addTab(
					bar.newTab().setText(getResources().getString(R.string.tabActivity)),
					ActivityFragment.class, null);
			tabsAdapter.addTab(
					bar.newTab().setText(getResources().getString(R.string.tabGuest)),
					GuestFragment.class, null);					
			
		} else {
			// go to login
			Intent intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
		}		
	}	
	
	
	private void loadSettings() {
		
        String xml = "";        
        
        InputStream input;
        
        
        try {
        	
        	// Parse locations
        	input = assetManager.open(Actify.FILE_LOCATIONS);

            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            xml = new String(buffer);
            if (!xml.isEmpty()) {
            	List<String> locationList = new ArrayList<String>();
            	
            	XMLParser parser = new XMLParser();
            	Document doc = parser.getDomElement(xml);
            	NodeList nl = doc.getElementsByTagName(Actify.KEY_ITEM);
        		for (int i = 0; i < nl.getLength(); i++) {
        			Element e = (Element) nl.item(i);
        			locationList.add(parser.getValue(e, Actify.KEY_LOCATION));
        		}
        		Actify.locationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, locationList); 
            }
            
         // Parse colors
        	input = assetManager.open(Actify.FILE_COLORS);

            size = input.available();
            buffer = new byte[size];
            input.read(buffer);
            input.close();

            xml = new String(buffer);
            if (!xml.isEmpty()) {
            	List<String> colorList = new ArrayList<String>();
            	
            	XMLParser parser = new XMLParser();
            	Document doc = parser.getDomElement(xml);
            	NodeList nl = doc.getElementsByTagName(Actify.KEY_ITEM);
        		for (int i = 0; i < nl.getLength(); i++) {
        			Element e = (Element) nl.item(i);
        			colorList.add(parser.getValue(e, Actify.KEY_COLOR));
        		}
        		Actify.colorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, colorList); 
            }
            
            // Parse activities
            input = assetManager.open(Actify.FILE_ACTIVITY_SETTINGS);
  
            size = input.available();
            buffer = new byte[size];
            input.read(buffer);
            input.close();

            xml = new String(buffer);
            
            if (!xml.isEmpty()) {
            	List<String> activityList = new ArrayList<String>();
            	
            	List<ActivitySetting> listAS = new ArrayList<ActivitySetting>();
            	
            	XMLParser parser = new XMLParser();
            	docActivitySettings = parser.getDomElement(xml);
            	NodeList nl = docActivitySettings.getElementsByTagName(Actify.KEY_ITEM);
        		// looping through all item nodes <item>
        		for (int i = 0; i < nl.getLength(); i++) {
   			
        			Element e = (Element) nl.item(i);
        			listAS.add(new ActivitySetting(Integer.parseInt(parser.getValue(e, Actify.KEY_ID)),
        					Integer.parseInt(parser.getValue(e, Actify.KEY_ORDER)),
        					parser.getValue(e, Actify.KEY_ACTIVITY),
        					parser.getValue(e, Actify.KEY_LOCATION),
        					parser.getValue(e, Actify.KEY_ICON),
        					(Integer.parseInt(parser.getValue(e, Actify.KEY_VISIBILITY)) == 1) ? true : false,
        					Integer.parseInt(parser.getValue(e, Actify.KEY_DURATION))));
        		}        		        		       
        		  		
        		if (settings.contains("loc_"+listAS.get(0).getId()+"_"+userid)) {        			
                	for (int i = 0; i < listAS.size(); i++) {
                		ActivitySetting as = listAS.get(i);
                		as.setOrder(settings.getInt("order_"+as.getId()+"_"+userid, -1));
                		as.setDuration(settings.getInt("duration_"+as.getId()+"_"+userid, 0));
                		as.setVisible(settings.getBoolean("vis_"+as.getId()+"_"+userid, false));
                		as.setLocation(settings.getString("loc_"+as.getId()+"_"+userid, ""));
                	}        	
                } else {
                	editor.putBoolean("sound"+"_"+userid, false);
                	for (int i = 0; i <  listAS.size(); i++) {
                		ActivitySetting as = listAS.get(i);
                		editor.putString("loc_"+as.getId()+"_"+userid, as.getLocation());
                		editor.putInt("order_"+as.getId()+"_"+userid, as.getOrder());
                		editor.putBoolean("vis_"+as.getId()+"_"+userid, as.isVisible());
                		editor.putInt("duration_"+as.getId()+"_"+userid, as.getDuration());
                		editor.putInt("idle_"+userid, Actify.PI_IDLE_TIME);
                	}    
                	editor.commit();
                }
        		
        		Collections.sort(listAS);
        		
        		for (int i = 0; i < listAS.size(); i++) {
        			ActivitySetting as = listAS.get(i);
        			if (as.isVisible())
        				activityList.add(as.getActivity());
        		}
        		Actify.activityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, activityList);
        		Actify.activitySettings = listAS;
            }       
        } catch (IOException e) {
            // TODO Auto-generated catch block
           	e.printStackTrace();
        }
                		
        
	}
	  
	
	@Override
	public void onDestroy() {
		super.onResume();
		if (logout) {
			editor.remove("logged_in");
			editor.remove("userid");
		    editor.commit();
		}
	}
		
	  @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	            
	      return super.onCreateOptionsMenu(menu);
	  }
	  
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
		  Intent intent;
		  switch (item.getItemId()) {
	        case Actify.MENU_LOGOUT:

	            intent = new Intent(getApplicationContext(), LoginActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(intent);
	            logout = true;
	            finish();  
	            break;
	            
	        case Actify.MENU_SETTING:
	        	//showLocationSettings();
	        	intent = new Intent(getApplicationContext(), SettingsActivity.class);
		        startActivity(intent);
	        	break;
	        
	        case Actify.MENU_ACTIVITY_HISTORY:	        	
		          intent = new Intent(getApplicationContext(), ActivityHistoryActivity.class);
		          startActivity(intent);
		          break;
		    
	        case Actify.MENU_ACTIVITY_PIE:	        	
		          intent = new Intent(getApplicationContext(), ChartDailyActivity.class);
		          startActivity(intent);
		          break;
		          
	        case Actify.MENU_ACTIVITY_DENSITY:        	
		          intent = new Intent(getApplicationContext(), ChartWeeklyActivity.class);
		          startActivity(intent);
		          break;    
	        
	        case Actify.MENU_GUEST_HISTORY:        	
		          intent = new Intent(getApplicationContext(), GuestHistoryActivity.class);
		          startActivity(intent);
		          break;  
	        }
			
		  return true;
	  } 
	  
	  protected void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	  }
	  
	  /**
		  * TabsAdapter
		  * from https://bitbucket.org/owentech/abstabsviewpager
		  */
		 
		 public static class TabsAdapter extends FragmentPagerAdapter implements
			ActionBar.TabListener, ViewPager.OnPageChangeListener
			{
				private final Context mContext;
				private final ActionBar mActionBar;
				private final ViewPager mViewPager;
				private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
				
				static final class TabInfo
				{
					private final Class<?> clss;
					private final Bundle args;
				
					TabInfo(Class<?> _class, Bundle _args)
					{
						clss = _class;
						args = _args;
					}
				}
			
				public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager)
				{
					super(activity.getSupportFragmentManager());
					mContext = activity;
					mActionBar = activity.getSupportActionBar();
					mViewPager = pager;
					mViewPager.setAdapter(this);
					mViewPager.setOnPageChangeListener(this);
				}
				
				public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args)
				{
					TabInfo info = new TabInfo(clss, args);
					tab.setTag(info);
					tab.setTabListener(this);
					mTabs.add(info);
					mActionBar.addTab(tab);
					notifyDataSetChanged();
				}
				
				@Override
				public int getCount()
				{
					return mTabs.size();
				}
				
				@Override
				public Fragment getItem(int position)
				{
					TabInfo info = mTabs.get(position);
					return Fragment.instantiate(mContext, info.clss.getName(),
							info.args);
				}
				
				public void onPageScrolled(int position, float positionOffset,
						int positionOffsetPixels)
				{
				}
				
				public void onPageSelected(int position)
				{
					mActionBar.setSelectedNavigationItem(position);
				}
				
				public void onPageScrollStateChanged(int state)
				{
				}
				
				public void onTabSelected(Tab tab, FragmentTransaction ft)
				{
					Object tag = tab.getTag();
					for (int i = 0; i < mTabs.size(); i++)
					{
						if (mTabs.get(i) == tag)
						{
							mViewPager.setCurrentItem(i);
						}
					}
				}
				
				public void onTabUnselected(Tab tab, FragmentTransaction ft)
				{
				}
				
				public void onTabReselected(Tab tab, FragmentTransaction ft)
				{
				}
			}
	  
	  /********************************************************
	   * SYNCHRONIZING PURPOSES
	   ******************************************************** 
	   */
	  
	  private void synchronize() {
		  // every xx minutes
		  if (isConnected()) {
			  syncInsert();
			  syncUpdate();
			  syncDelete();
		  }
	  }
	  
	  private boolean isConnected() {
		  boolean isCon = false;
		  ConnectivityManager conMgr =  
				  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		  
		  if (conMgr != null) {
			  NetworkInfo ni = conMgr.getActiveNetworkInfo();
			  if (ni != null) {
				  if (ni.isConnected() && ni.isAvailable()) {
			        isCon = true;             
				  }
			  }
		  } 
		  return isCon;
	 }	  
	  
	 private void syncInsert() {
		 String query;
		 int sync = Actify.NOT_SYNC;
		 int mode = Actify.MODE_INSERT;
		 
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_ACTIVITIES, sync, mode);
		 List<ActivityInstance> activityInstances = db.getActivityList(query);
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_GUESTS, sync, mode);
		 List<Guest> guests = db.getGuestList(query);
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_ACTIVITYPAUSES, sync, mode);
		 List<ActivityPause> pauses = db.getActivityPauseList(query);
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_ACTIVITYGUESTS, sync, mode);
		 List<ActivityGuest> activityGuests = db.getActivityGuestList(query);
		 
		 // TODO INSERT data in the list to the remote server
		 
		 // TODO if successful
		 sync = Actify.SYNC;
		 db.updateActivitySyncMode(activityInstances, sync, mode);
		 db.updateGuestSyncMode(guests, sync, mode);
		 db.updateActivityPauseSyncMode(pauses, sync, mode);
		 db.updateActivityGuestSyncMode(activityGuests, sync, mode);
	 }
	 
	 private void syncUpdate() {
		 String query;
		 int sync = Actify.NOT_SYNC;
		 int mode = Actify.MODE_UPDATE;
		 
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_ACTIVITIES, sync, mode);
		 List<ActivityInstance> activityInstances = db.getActivityList(query);
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_GUESTS, sync, mode);
		 List<Guest> guests = db.getGuestList(query);
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_ACTIVITYPAUSES, sync, mode);
		 List<ActivityPause> pauses = db.getActivityPauseList(query);
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_ACTIVITYGUESTS, sync, mode);
		 List<ActivityGuest> activityGuests = db.getActivityGuestList(query);
		 
		 // TODO UPDATE data in the list in the remote server
		 
		 // TODO if successful
		 sync = Actify.SYNC;
		 db.updateActivitySyncMode(activityInstances, sync, mode);
		 db.updateGuestSyncMode(guests, sync, mode);
		 db.updateActivityPauseSyncMode(pauses, sync, mode);
		 db.updateActivityGuestSyncMode(activityGuests, sync, mode);
	 }
	 
	 private void syncDelete() {
		 String query;
		 int sync = Actify.NOT_SYNC;
		 int mode = Actify.MODE_DELETE;
		 
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_ACTIVITIES, sync, mode);
		 List<ActivityInstance> activityInstances = db.getActivityList(query);
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_GUESTS, sync, mode);
		 List<Guest> guests = db.getGuestList(query);
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_ACTIVITYPAUSES, sync, mode);
		 List<ActivityPause> pauses = db.getActivityPauseList(query);
		 query = db.selectQueryBuilder(
				 ActifySQLiteHelper.TABLE_ACTIVITYGUESTS, sync, mode);
		 List<ActivityGuest> activityGuests = db.getActivityGuestList(query);
		 
		 // TODO DELETE data in the list in the remote server
		 
		 // TODO if successful
		 sync = Actify.SYNC;
		 db.deleteActivitySyncMode(activityInstances);
		 db.deleteGuestSyncMode(guests);
		 db.deleteActivityPauseSyncMode(pauses);
		 db.deleteActivityGuestSyncMode(activityGuests);
	 }
	 
	 
	  
}
