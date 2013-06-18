package com.application.actify.core;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.util.SparseArray;
import android.widget.ArrayAdapter;

import com.application.actify.model.ActivitySetting;
import com.application.actify.util.XMLParser;

public class Actify {
	public static final String PREFS_NAME = "ActifyPrefs";
	
	public static final int MENU_HOME = 0;
	public static final int MENU_SETTING = 1;
	public static final int MENU_SETTING_LOCATION = 111;
	public static final int MENU_SETTING_VISIBILITY = 112;
	public static final int MENU_SETTING_ORDER = 113;
	public static final int MENU_SETTING_REMINDER = 114;
	public static final int MENU_SETTING_SOUND = 121;
	public static final int MENU_SETTING_IDLE = 122;
	public static final int MENU_SETTING_EMAIL_SETTINGS = 123;
	public static final int MENU_SETTING_EXPORT = 131;
	public static final int MENU_SETTING_SYNC = 132;
	public static final int MENU_SETTING_EMAIL = 133;
	public static final int MENU_LOGOUT = 2;
	public static final int MENU_ACTIVITY_HISTORY = 41;
	public static final int MENU_ACTIVITY_PIE = 42;
	public static final int MENU_ACTIVITY_DENSITY = 43;
	public static final int MENU_GUEST_HISTORY = 51;
	
		
	public static final String FILE_LOCATIONS = "locations.xml";
	public static final String FILE_COLORS = "colors.xml";
	public static final String FILE_ACTIVITY_SETTINGS = "activity_settings.xml";
	
	public static final String KEY_ITEM = "item";
	public static final String KEY_ACTIVITY = "activity";
	public static final String KEY_LOCATION = "location";
	public static final String KEY_COLOR = "color";
	public static final String KEY_ICON = "icon";
	public static final String KEY_ID = "id";
	public static final String KEY_ORDER = "order";
	public static final String KEY_VISIBILITY = "visibility";
	public static final String KEY_DURATION = "duration";
	
	public static final int VISIBLE = 1;
	public static final int INVISIBLE = 0;
	
	public static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
	public static DateFormat datetimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
	public static DateFormat datetimeFilenameFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
	public static DateFormat shortTimeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
	public static DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
	public static DateFormat SQLiteDatetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
	public static DateFormat SQLiteDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	public static DateFormat SQLiteTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
	
	public static DateTimeFormatter datetimeFormatJoda = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
	public static DateTimeFormatter dateFormatJoda = DateTimeFormat.forPattern("dd/MM/yyyy");
	public static DateTimeFormatter SQLiteDatetimeFormatJoda = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	
	public static final int CHART_DAY = 0;
	public static final int CHART_WEEK = 1;
	public static final int CHART_MONTH = 2;
	
	public static final int MAX_NUM_ACTIVITY_RUNNING = 5;
	public static final int MAX_NUM_ACTIVITY_HISTORY = 20;
	public static final int MAX_NUM_GUEST = 20;
	
	public static final int MODE_RUNNING = 0;
	public static final int MODE_PAUSED = 1;
	public static final int MODE_INSERT = 2;
	public static final int MODE_UPDATE = 3;
	public static final int MODE_DELETE = 4;
	
	public static final int VIEW_TIMER = 0;
	public static final int VIEW_HISTORY = 1;
	
	public static final int NOT_SYNC = 0;
	public static final int SYNC = 1;
	
	public static final int PAUSE_PAUSED = 0;
	public static final int PAUSE_RESUMED = 1;
	public static final String[] PAUSE_STRINGS = new String[] {"Paused", "Resumed"};
	
	public static boolean showPicker = false;
	
	public static final int PI_ID = -111;
	public static final int PI_IDLE_TIME = 15;

	public static ArrayAdapter<String> locationAdapter;
	public static ArrayAdapter<String> colorAdapter;
	public static ArrayAdapter<String> activityAdapter;
	
	public static SparseArray<PendingIntent> pendingIntents = new SparseArray<PendingIntent>();
	public static SparseArray<DateTime> pendingIntentTimes = new SparseArray<DateTime>();
    
    public static List<ActivitySetting> activitySettings;
    
    public static List<ActivitySetting> getVisibleActivitySettings() {
		List<ActivitySetting> result = new ArrayList<ActivitySetting>();		
		
		for (ActivitySetting as : activitySettings) {
			if (as.isVisible()) {
				result.add(as);
			}
		}		
		Collections.sort(result);
		return result;		
	}
    
    public static void resetOrderActivitySettings() {
    	Collections.sort(activitySettings);
    	activityAdapter.clear();
    	
    	int visibleCounter = 0;
    	
    	for (ActivitySetting as : activitySettings) {
    		if (as.isVisible()) {
    			as.setOrder(visibleCounter);
    			visibleCounter++;
    			activityAdapter.add(as.getActivity());
    		}
    	}
    	Collections.sort(activitySettings);
    }
    
    public static void reorderActivitySettings() {
    	activityAdapter.clear();
    	
    	for (ActivitySetting as : activitySettings) {
    		if (as.isVisible()) {
    			activityAdapter.add(as.getActivity());
    		}
    	}    
    	Collections.sort(activitySettings);
    }
    
    public static ActivitySetting findActivitySettingById(Integer id) {
    	for (ActivitySetting as : activitySettings) {
			if (as.getId().equals(id))
				return as;
		}
		return null;
	}
    
    public static ActivitySetting findActivitySettingByOrder(Integer order) {
    	for (ActivitySetting as : activitySettings) {
			if (as.getOrder().equals(order))
				return as;
		}
		return null;
	}
    
    public static int findActivitySettingPositionByOrder(Integer order) {
    	for (int i=0; i < activitySettings.size(); i++) {
    		ActivitySetting as = activitySettings.get(i);
			if (as.getOrder().equals(order))
				return i;
		}
		return -1;
	}
    
    public static void loadSettings(Activity act) {
    	AssetManager assetManager = act.getAssets();
    	SharedPreferences settings = act.getSharedPreferences(Actify.PREFS_NAME, 0);
		Editor editor = settings.edit();
		int userid = settings.getInt("userid", -1);
		
		Document docActivitySettings;
		
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
        		Actify.locationAdapter = new ArrayAdapter<String>(act, android.R.layout.simple_spinner_dropdown_item, locationList); 
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
        		Actify.colorAdapter = new ArrayAdapter<String>(act, android.R.layout.simple_spinner_dropdown_item, colorList); 
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
        		Actify.activityAdapter = new ArrayAdapter<String>(act, android.R.layout.simple_spinner_dropdown_item, activityList);
        		Actify.activitySettings = listAS;
            }       
        } catch (IOException e) {
            // TODO Auto-generated catch block
           	e.printStackTrace();
        }
                		
        
	}
    
}
