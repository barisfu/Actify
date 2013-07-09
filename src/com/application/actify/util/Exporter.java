package com.application.actify.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.os.Environment;

import com.application.actify.core.Actify;
import com.application.actify.db.ActifySQLiteHelper;
import com.application.actify.model.ActivitySetting;

public class Exporter {
	private static final int BUFFER = 2048; 
	private ActifySQLiteHelper db;
	private int userid;
	private String householdid;
	private String filename = null;
	
	public Exporter(ActifySQLiteHelper db, int userid, String householdid) {
		this.db = db;
		this.userid = userid;
		this.householdid = householdid;
	}
	
	public boolean export() {
		filename = null;
		Calendar now = Calendar.getInstance();
		String datestring = Actify.datetimeFilenameFormat.format(now.getTime());
		boolean result = true;
        result = result & db.exportActivityList("activities.csv", userid);
        result = result & db.exportActivityPauseList("activity_pauses.csv", userid);
        result = result & db.exportGuestList("guests.csv", householdid);
        result = result & db.exportActivityGuestList("activity_guests.csv", userid);
        result = result & db.exportActivityLog("activities_log.csv", userid);
        result = result & db.exportGuestLog("guests_log.csv", householdid);
        result = result & db.exportActivityPauseLog("activitypauses_log.csv", userid);
        result = result & db.exportReminderList("reminder.csv", userid);
        result = result & this.exportActivitySetting();
        result = result & this.exportLocationSetting();
        
        File sdCard = Environment.getExternalStorageDirectory();
		String dir = sdCard.getAbsolutePath() + "/Actify";
        String[] files = {
        		dir+"/activities.csv",
        		dir+"/activity_pauses.csv",
        		dir+"/guests.csv",
        		dir+"/activity_guests.csv",
        		dir+"/activities_log.csv",
        		dir+"/guests_log.csv",
        		dir+"/activitypauses_log.csv",
        		dir+"/reminder.csv",
        		dir+"/activity_list.csv",
        		dir+"/location_list.csv"};
        
        try  { 
            BufferedInputStream origin = null; 
            String zipFilename = dir+"/"+datestring+"_actify.zip";
            FileOutputStream dest = new FileOutputStream(zipFilename); 
       
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest)); 
       
            byte data[] = new byte[BUFFER]; 
       
            for(int i=0; i < files.length; i++) { 
              FileInputStream fi = new FileInputStream(files[i]); 
              origin = new BufferedInputStream(fi, BUFFER); 
              ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1)); 
              out.putNextEntry(entry); 
              int count; 
              while ((count = origin.read(data, 0, BUFFER)) != -1) { 
                out.write(data, 0, count); 
              } 
              origin.close(); 
              File file = new File(files[i]);
              file.delete();
            } 
       
            out.close();
            filename = zipFilename;
          } catch(Exception e) { 
            e.printStackTrace(); 
            result = result & false;
          } 
        
        return result;
	}
	
	public String getFilename() {
		return this.filename;
	}
	
	public boolean exportActivitySetting () {
		boolean returnCode;
		String csv = 
    			"id" + ","
    			+ "activity" + "\n";
		try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File (sdCard.getAbsolutePath() + "/Actify");
    		dir.mkdirs();
    		File outFile = new File(dir, "activity_list.csv");
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(csv);
            for (ActivitySetting as : Actify.activitySettings) {
            	csv = as.getId() + ","
            		+ as.getActivity() + "\n";
            	out.write(csv);
            }
            out.close();
    		returnCode = true;
    	} catch (Exception e) {
            returnCode = false;
        }
		return returnCode;
	}
	
	public boolean exportLocationSetting () {
		boolean returnCode;
		String csv = 
    			"id" + ","
    			+ "location" + "\n";
		try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File (sdCard.getAbsolutePath() + "/Actify");
    		dir.mkdirs();
    		File outFile = new File(dir, "location_list.csv");
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(csv);
            for (int i=0; i < Actify.locationAdapter.getCount(); i++) {
            	csv = i + ","
            		+ Actify.locationAdapter.getItem(i) + "\n";
            	out.write(csv);
            }
            out.close();
    		returnCode = true;
    	} catch (Exception e) {
            returnCode = false;
        }
		return returnCode;
	}
}
