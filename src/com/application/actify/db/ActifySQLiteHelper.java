package com.application.actify.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.application.actify.core.Actify;
import com.application.actify.model.ActivityGuest;
import com.application.actify.model.ActivityInstance;
import com.application.actify.model.ActivityPause;
import com.application.actify.model.Guest;
import com.application.actify.model.Reminder;
import com.application.actify.view.component.SliceInfo;

public class ActifySQLiteHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "actify.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String ID = "_id";
	public static final String SYNC = "sync";
	public static final String MODE = "mode";
	public static final String START = "start";
	public static final String END = "end";
	public static final String TIME = "time";
	public static final String ACTION = "action";
	
	public static final String TABLE_ACTIVITIES = "activities";
	public static final String ACTIVITIES_ID = ID;
	public static final String ACTIVITIES_USERID = "userid";
	public static final String ACTIVITIES_ACTIVITYID = "activityid";
	public static final String ACTIVITIES_LOCATIONID = "locationid";
	public static final String ACTIVITIES_START = START;
	public static final String ACTIVITIES_END = END;
	public static final String ACTIVITIES_SYNC = SYNC;
	public static final String ACTIVITIES_MODE = MODE;
	
	public static final String TABLE_ACTIVITIES_LOG = "activities_log";	
	public static final String TABLE_ACTIVITIES_UPDATE_TRIGGER = "activities_up_trg";
	public static final String TABLE_ACTIVITIES_DELETE_TRIGGER = "activities_del_trg";
	
	public static final String TABLE_GUESTS = "guests";
	public static final String GUESTS_ID = ID;
	public static final String GUESTS_NAME = "name";
	public static final String GUESTS_START = START;	
	public static final String GUESTS_END = END;
	public static final String GUESTS_HOUSEHOLDID = "householdid";
	public static final String GUESTS_SYNC = SYNC;
	public static final String GUESTS_MODE = MODE;
	
	public static final String TABLE_GUESTS_LOG = "guests_log";
	public static final String TABLE_GUESTS_UPDATE_TRIGGER = "guests_up_trg";
	public static final String TABLE_GUESTS_DELETE_TRIGGER = "guests_del_trg";
	
	public static final String TABLE_ACTIVITYPAUSES = "activitypauses";
	public static final String ACTIVITYPAUSES_ID= ID;
	public static final String ACTIVITYPAUSES_ACTIVITIESID = "activities_id";
	public static final String ACTIVITYPAUSES_START = START;
	public static final String ACTIVITYPAUSES_END = END;
	public static final String ACTIVITYPAUSES_SYNC = SYNC;
	public static final String ACTIVITYPAUSES_MODE = MODE;
	
	public static final String TABLE_ACTIVITYPAUSES_LOG = "activitypauses_log";
	public static final String TABLE_ACTIVITYPAUSES_UPDATE_TRIGGER = "activitypauses_up_trg";
	public static final String TABLE_ACTIVITYPAUSES_DELETE_TRIGGER = "activitypauses_del_trg";
	
	public static final String TABLE_ACTIVITYGUESTS = "activityguests";
	public static final String ACTIVITYGUESTS_ID = ID;
	public static final String ACTIVITYGUESTS_ACTIVITIESID = "activities_id";
	public static final String ACTIVITYGUESTS_GUESTSID = "guests_id";
	public static final String ACTIVITYGUESTS_SYNC = SYNC;
	public static final String ACTIVITYGUESTS_MODE = MODE;
	
	public static final String TABLE_REMINDER = "reminder";
	public static final String REMINDER_ID = ID;
	public static final String REMINDER_USERID = "userid";
	public static final String REMINDER_TYPE = "type";
	public static final String REMINDER_START = START;
	public static final String REMINDER_END = END;
	public static final String REMINDER_ACTIVITIESID = "activities_id";
	public static final String REMINDER_ACTION = "action";
	

	// Database creation sql statement
	private static final String ACTIVITIES_CREATE = "CREATE TABLE "
			+ TABLE_ACTIVITIES + "(" 
			+ ACTIVITIES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
			+ ACTIVITIES_USERID + " INTEGER, " 
			+ ACTIVITIES_ACTIVITYID + " INTEGER, " 
			+ ACTIVITIES_LOCATIONID + " INTEGER, " 
			+ ACTIVITIES_START + " DATETIME, " 
			+ ACTIVITIES_END + " DATETIME, "
			+ ACTIVITIES_SYNC + " INTEGER, " 
			+ ACTIVITIES_MODE + " INTEGER "
			+");";
	
	private static final String ACTIVITIES_LOG_CREATE = "CREATE TABLE "
			+ TABLE_ACTIVITIES_LOG + "(" 
			+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ TABLE_ACTIVITIES + ACTIVITIES_ID + " INTEGER,"
			+ ACTIVITIES_USERID + " INTEGER, " 
			+ ACTIVITIES_MODE + " INTEGER, "
			+ TIME + " DATETIME,"
			+ ACTION + " VARCHAR(10), "
			+ ACTIVITIES_ACTIVITYID + "_OLD" + " INTEGER, " 
			+ ACTIVITIES_LOCATIONID + "_OLD" +  " INTEGER, " 
			+ ACTIVITIES_START +  "_OLD" + " DATETIME, " 
			+ ACTIVITIES_END +  "_OLD" + " DATETIME, "
			+ ACTIVITIES_ACTIVITYID + "_NEW" + " INTEGER, " 
			+ ACTIVITIES_LOCATIONID + "_NEW" +  " INTEGER, " 
			+ ACTIVITIES_START +  "_NEW" + " DATETIME, " 
			+ ACTIVITIES_END +  "_NEW" + " DATETIME "			
			+");";
	
	private static final String ACTIVITIES_UPDATE_TRIGGER_CREATE = "CREATE TRIGGER "
			+ TABLE_ACTIVITIES_UPDATE_TRIGGER + " AFTER UPDATE OF "
			+ ACTIVITIES_ACTIVITYID + ","
			+ ACTIVITIES_LOCATIONID + ","
			+ ACTIVITIES_START + ","
			+ ACTIVITIES_END 
			+ " ON " + TABLE_ACTIVITIES
			+ " WHEN ("
			+ "NEW."+ ACTIVITIES_ACTIVITYID + "<> OLD." + ACTIVITIES_ACTIVITYID
			+ " OR NEW."+ ACTIVITIES_LOCATIONID + "<> OLD." + ACTIVITIES_LOCATIONID
			+ " OR NEW."+ ACTIVITIES_START + "<> OLD." + ACTIVITIES_START
			+ " OR NEW."+ ACTIVITIES_END + "<> OLD." + ACTIVITIES_END
			+ ") BEGIN "
			+ " INSERT INTO " + TABLE_ACTIVITIES_LOG + "("
			+ TABLE_ACTIVITIES + ACTIVITIES_ID + "," 
			+ ACTIVITIES_USERID + "," 
			+ ACTIVITIES_MODE + ", "
			+ TIME + "," 
			+ ACTION + ","
			+ ACTIVITIES_ACTIVITYID + "_OLD" + ","  
			+ ACTIVITIES_LOCATIONID + "_OLD" + ","
			+ ACTIVITIES_START + "_OLD" + ","
			+ ACTIVITIES_END + "_OLD" + ","
			+ ACTIVITIES_ACTIVITYID + "_NEW" + ","  
			+ ACTIVITIES_LOCATIONID + "_NEW" + ","
			+ ACTIVITIES_START + "_NEW" + ","
			+ ACTIVITIES_END + "_NEW"			
			+ ") VALUES ("
			+"OLD." + ACTIVITIES_ID + ","
			+"OLD." + ACTIVITIES_USERID + ","
			+"OLD." + ACTIVITIES_MODE + ","
			+"datetime('now'),"
			+"'UPDATE', "
			+"OLD." + ACTIVITIES_ACTIVITYID + ","
			+"OLD." + ACTIVITIES_LOCATIONID + ","
			+"OLD." + ACTIVITIES_START + ","
			+"OLD." + ACTIVITIES_END + ","
			+"NEW." + ACTIVITIES_ACTIVITYID + ","
			+"NEW." + ACTIVITIES_LOCATIONID + ","
			+"NEW." + ACTIVITIES_START + ","
			+"NEW." + ACTIVITIES_END			
			+"); END"
			+";";
	
	private static final String ACTIVITIES_DELETE_TRIGGER_CREATE = "CREATE TRIGGER "
			+ TABLE_ACTIVITIES_DELETE_TRIGGER + " AFTER UPDATE OF "
			+ ACTIVITIES_MODE 
			+ " ON " + TABLE_ACTIVITIES
			+ " WHEN ("
			+ "NEW."+ ACTIVITIES_MODE + "== " + Actify.MODE_DELETE
			+ ") BEGIN "
			+ " INSERT INTO " + TABLE_ACTIVITIES_LOG + "("
			+ TABLE_ACTIVITIES + ACTIVITIES_ID + "," 
			+ ACTIVITIES_USERID + "," 
			+ ACTIVITIES_MODE + ", "
			+ TIME + "," 
			+ ACTION + ","
			+ ACTIVITIES_ACTIVITYID + "_OLD" + ","  
			+ ACTIVITIES_LOCATIONID + "_OLD" + ","
			+ ACTIVITIES_START + "_OLD" + ","
			+ ACTIVITIES_END + "_OLD"		
			+ ") VALUES ("
			+"OLD." + ACTIVITIES_ID + ","
			+"OLD." + ACTIVITIES_USERID + ","
			+"OLD." + ACTIVITIES_MODE + ","
			+"datetime('now'),"
			+"'DELETE', "
			+"NEW." + ACTIVITIES_ACTIVITYID + ","
			+"NEW." + ACTIVITIES_LOCATIONID + ","
			+"NEW." + ACTIVITIES_START + ","
			+"NEW." + ACTIVITIES_END			
			+"); END"
			+";";		
	
	private static final String GUESTS_CREATE = "CREATE TABLE "
			+ TABLE_GUESTS + "(" 
			+ GUESTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
			+ GUESTS_NAME + " TEXT, "
			+ GUESTS_START + " DATETIME, "			
			+ GUESTS_END + " DATETIME, "
			+ GUESTS_HOUSEHOLDID + " TEXT, "
			+ GUESTS_SYNC + " INTEGER, " 
			+ GUESTS_MODE + " INTEGER "
			+");";
	
	
	private static final String GUESTS_LOG_CREATE = "CREATE TABLE "
			+ TABLE_GUESTS_LOG + "(" 
			+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ TABLE_GUESTS + GUESTS_ID + " INTEGER,"
			+ GUESTS_MODE + " INTEGER, "
			+ TIME + " DATETIME,"
			+ ACTION + " VARCHAR(10),"
			+ GUESTS_NAME +  "_OLD" + " TEXT, " 
			+ GUESTS_START +  "_OLD" + " DATETIME, " 
			+ GUESTS_END +  "_OLD" + " DATETIME, "
			+ GUESTS_NAME +  "_NEW" + " TEXT, " 
			+ GUESTS_START +  "_NEW" + " DATETIME, " 
			+ GUESTS_END +  "_NEW" + " DATETIME"			
			+");";
	
	private static final String GUESTS_UPDATE_TRIGGER_CREATE = "CREATE TRIGGER "
			+ TABLE_GUESTS_UPDATE_TRIGGER + " AFTER UPDATE OF "
			+ GUESTS_NAME + ","
			+ GUESTS_START + ","
			+ GUESTS_END 
			+ " ON " + TABLE_GUESTS
			+ " WHEN ("
			+ "NEW."+ GUESTS_NAME + "<> OLD." + GUESTS_NAME
			+ " OR NEW."+ GUESTS_START + "<> OLD." + GUESTS_START
			+ " OR NEW."+ GUESTS_END + "<> OLD." + GUESTS_END
			+ ") BEGIN "
			+ " INSERT INTO " + TABLE_GUESTS_LOG + "("
			+ TABLE_GUESTS + GUESTS_ID + "," 
			+ GUESTS_MODE + ", "
			+ TIME + "," 
			+ ACTION + ","
			+ GUESTS_NAME + "_OLD" + ","  
			+ GUESTS_START + "_OLD" + ","
			+ GUESTS_END + "_OLD" + ","
			+ GUESTS_NAME + "_NEW" + ","  
			+ GUESTS_START + "_NEW" + ","
			+ GUESTS_END + "_NEW"		
			+ ") VALUES ("
			+"OLD." + GUESTS_ID + ","
			+"OLD." + GUESTS_MODE + ","
			+"datetime('now'),"
			+"'UPDATE', "
			+"OLD." + GUESTS_NAME + ","
			+"OLD." + GUESTS_START + ","
			+"OLD." + GUESTS_END + ","
			+"NEW." + GUESTS_NAME + ","
			+"NEW." + GUESTS_START + ","
			+"NEW." + GUESTS_END			
			+"); END"
			+";";
	
	private static final String GUESTS_DELETE_TRIGGER_CREATE = "CREATE TRIGGER "
			+ TABLE_GUESTS_DELETE_TRIGGER + " BEFORE UPDATE OF "
			+ GUESTS_MODE 
			+ " ON " + TABLE_GUESTS
			+ " WHEN ("
			+ "NEW."+ GUESTS_MODE + "== " + Actify.MODE_DELETE
			+ ") BEGIN "
			+ " INSERT INTO " + TABLE_GUESTS_LOG + "("
			+ TABLE_GUESTS + GUESTS_ID + "," 
			+ GUESTS_MODE + ", "
			+ TIME + "," 
			+ ACTION + ","
			+ GUESTS_NAME + "_OLD" + ","  
			+ GUESTS_START + "_OLD" + ","
			+ GUESTS_END + "_OLD" 		
			+ ") VALUES ("
			+"OLD." + GUESTS_ID + ","
			+"OLD." + GUESTS_MODE + ","
			+"datetime('now'),"
			+"'DELETE', "
			+"OLD." + GUESTS_NAME + ","
			+"OLD." + GUESTS_START + ","
			+"OLD." + GUESTS_END 			
			+"); END"
			+";";

	
	private static final String ACTIVITYGUESTS_CREATE = "CREATE TABLE "
			+ TABLE_ACTIVITYGUESTS + "(" 
			+ ACTIVITYGUESTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
			+ ACTIVITYGUESTS_ACTIVITIESID + " INTEGER, " 
			+ ACTIVITYGUESTS_GUESTSID + " INTEGER, " 			
			+ ACTIVITYGUESTS_SYNC + " INTEGER, " 
			+ ACTIVITYGUESTS_MODE + " INTEGER, "
			+ "FOREIGN KEY ("+ ACTIVITYGUESTS_ACTIVITIESID + ") REFERENCES "+ TABLE_ACTIVITIES+ "(" +ACTIVITIES_ID+"),"
			+ "FOREIGN KEY ("+ ACTIVITYGUESTS_GUESTSID + ") REFERENCES "+ TABLE_GUESTS+ "(" +GUESTS_ID+")" 
			+");";
	
	private static final String ACTIVITYPAUSES_CREATE = "CREATE TABLE "
			+ TABLE_ACTIVITYPAUSES + "(" 
			+ ACTIVITYPAUSES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ ACTIVITYPAUSES_ACTIVITIESID + " INTEGER, " 			
			+ ACTIVITYPAUSES_START + " DATETIME, "
			+ ACTIVITYPAUSES_END + " DATETIME, " 
			+ ACTIVITYPAUSES_SYNC + " INTEGER, " 
			+ ACTIVITYPAUSES_MODE + " INTEGER, "
			+ "FOREIGN KEY ("+ ACTIVITYPAUSES_ACTIVITIESID + ") REFERENCES "+ TABLE_ACTIVITIES+ "(" +ACTIVITIES_ID+")"
			+");";
	
	private static final String ACTIVITYPAUSES_LOG_CREATE = "CREATE TABLE "
			+ TABLE_ACTIVITYPAUSES_LOG + "(" 
			+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ TABLE_ACTIVITYPAUSES + ACTIVITYPAUSES_ID + " INTEGER,"
			+ ACTIVITYPAUSES_ACTIVITIESID + " INTEGER,"
			+ ACTIVITYPAUSES_MODE + " INTEGER, "
			+ TIME + " DATETIME,"
			+ ACTION + " VARCHAR(10),"
			+ ACTIVITYPAUSES_START +  "_OLD" + " DATETIME, " 
			+ ACTIVITYPAUSES_END +  "_OLD" + " DATETIME, "
			+ ACTIVITYPAUSES_START +  "_NEW" + " DATETIME, " 
			+ ACTIVITYPAUSES_END +  "_NEW" + " DATETIME"			
			+");";
	
	private static final String ACTIVITYPAUSES_UPDATE_TRIGGER_CREATE = "CREATE TRIGGER "
			+ TABLE_ACTIVITYPAUSES_UPDATE_TRIGGER + " AFTER UPDATE OF "
			+ ACTIVITYPAUSES_START + ","
			+ ACTIVITYPAUSES_END 
			+ " ON " + TABLE_ACTIVITYPAUSES
			+ " WHEN ("
			+ " NEW."+ ACTIVITYPAUSES_START + "<> OLD." + ACTIVITYPAUSES_START
			+ " OR NEW."+ ACTIVITYPAUSES_END + "<> OLD." + ACTIVITYPAUSES_END
			+ ") BEGIN "
			+ " INSERT INTO " + TABLE_ACTIVITYPAUSES_LOG + "("
			+ TABLE_ACTIVITYPAUSES + ACTIVITYPAUSES_ID + "," 
			+ ACTIVITYPAUSES_ACTIVITIESID + ","
			+ ACTIVITYPAUSES_MODE + ", "
			+ TIME + "," 
			+ ACTION + ","
			+ ACTIVITYPAUSES_START +  "_OLD" + " , " 
			+ ACTIVITYPAUSES_END +  "_OLD" + " , "
			+ ACTIVITYPAUSES_START +  "_NEW" + " , " 
			+ ACTIVITYPAUSES_END +  "_NEW"		
			+ ") VALUES ("
			+"OLD." + ACTIVITYPAUSES_ID + ","
			+"OLD." + ACTIVITYPAUSES_ACTIVITIESID + ","
			+"OLD." + ACTIVITYPAUSES_MODE + ","
			+"datetime('now'),"
			+"'UPDATE', "
			+"OLD." + ACTIVITYPAUSES_START + ","
			+"OLD." + ACTIVITYPAUSES_END + ","
			+"NEW." + ACTIVITYPAUSES_START + ","
			+"NEW." + ACTIVITYPAUSES_END			
			+"); END"
			+";";
	
	private static final String ACTIVITYPAUSES_DELETE_TRIGGER_CREATE = "CREATE TRIGGER "
			+ TABLE_ACTIVITYPAUSES_DELETE_TRIGGER + " BEFORE UPDATE OF "	
			+ ACTIVITYPAUSES_MODE 
			+ " ON " + TABLE_ACTIVITYPAUSES
			+ " WHEN ("
			+ "NEW."+ ACTIVITYPAUSES_MODE + "== " + Actify.MODE_DELETE
			+ ") BEGIN "
			+ " INSERT INTO " + TABLE_ACTIVITYPAUSES_LOG + "("
			+ TABLE_ACTIVITYPAUSES + ACTIVITYPAUSES_ID + "," 
			+ ACTIVITYPAUSES_ACTIVITIESID + ","
			+ ACTIVITYPAUSES_MODE + ", "
			+ TIME + "," 
			+ ACTION + ","
			+ ACTIVITYPAUSES_START +  "_OLD" + " , " 
			+ ACTIVITYPAUSES_END +  "_OLD"		
			+ ") VALUES ("
			+"OLD." + ACTIVITYPAUSES_ID + ","
			+"OLD." + ACTIVITYPAUSES_ACTIVITIESID + ","
			+"OLD." + ACTIVITYPAUSES_MODE + ","
			+"datetime('now'),"
			+"'DELETE', "
			+"OLD." + ACTIVITYPAUSES_START + ","
			+"OLD." + ACTIVITYPAUSES_END			
			+"); END"
			+";";
	
	private static final String REMINDER_CREATE = "CREATE TABLE "
			+ TABLE_REMINDER + "(" 
			+ REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ REMINDER_USERID + " INTEGER, " 
			+ REMINDER_TYPE + " INTEGER, " 
			+ REMINDER_START + " DATETIME, " 
			+ REMINDER_END + " DATETIME, " 
			+ REMINDER_ACTIVITIESID + " INTEGER, " 
			+ REMINDER_ACTION + " INTEGER"
			+");";
	
	public ActifySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(ACTIVITIES_CREATE);
		database.execSQL(ACTIVITIES_LOG_CREATE);
		database.execSQL(ACTIVITIES_UPDATE_TRIGGER_CREATE);
		database.execSQL(ACTIVITIES_DELETE_TRIGGER_CREATE);
		
		database.execSQL(GUESTS_CREATE);
		database.execSQL(GUESTS_LOG_CREATE);
		database.execSQL(GUESTS_UPDATE_TRIGGER_CREATE);
		database.execSQL(GUESTS_DELETE_TRIGGER_CREATE);
		
		database.execSQL(ACTIVITYPAUSES_CREATE);
		database.execSQL(ACTIVITYPAUSES_LOG_CREATE);
		database.execSQL(ACTIVITYPAUSES_UPDATE_TRIGGER_CREATE);
		database.execSQL(ACTIVITYPAUSES_DELETE_TRIGGER_CREATE);
		
		database.execSQL(ACTIVITYGUESTS_CREATE);	
		
		database.execSQL(REMINDER_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ActifySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GUESTS);
		onCreate(db);
	}
	
	/**
	 * Methods for synchronizing purpose
	 */
	
	// To be used with:
	// getActivityList(String selectQuery)
	// getGuestList(String selectQuery)
	// getActivityPauseList(String selectQuery)
	// getActivityGuestList(String selectQuery)
	public String selectQueryBuilder(String table, int sync, int mode) {
    	String selectQuery = 
    			"SELECT * FROM " + table 
        		+ " WHERE " + ACTIVITIES_SYNC + "==" + sync
        		+ " AND " + ACTIVITIES_MODE + "==" + mode;
        return selectQuery;
    }	
	
	public String selectQueryBuilder(String table, String[] whereCols, int[] whereVals) {
		
    	String selectQuery = 
    			"SELECT * FROM " + table;
    	if (whereCols.length == whereVals.length) {
    		int l = whereCols.length;
    		int i;
    		if (l > 0) {
    			selectQuery += " WHERE ";
    			selectQuery += whereCols[0] + " == " + whereVals[0];
    			for (i=1; i< l; i++) {
    				selectQuery +=  " AND "+whereCols[i] + " == " + whereVals[i];
    			}
    		}
    	}
    	selectQuery += " ORDER BY "+ ID + " DESC";
    	
        return selectQuery;
    }	
	
	/**
     * TABLE_ACTIVITIES
     */
	// Adding new activity instance
    public ActivityInstance addActivity(ActivityInstance ai) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(ACTIVITIES_USERID, ai.getuserid()); 
        values.put(ACTIVITIES_ACTIVITYID, ai.getactivityid());
        values.put(ACTIVITIES_LOCATIONID, ai.getlocationid());
        values.put(ACTIVITIES_START, Actify.SQLiteDatetimeFormat.format(ai.getStart().getTime()));
        values.put(ACTIVITIES_END, Actify.SQLiteDatetimeFormat.format(ai.getEnd().getTime()));
        values.put(ACTIVITIES_SYNC, ai.getSync());
        values.put(ACTIVITIES_MODE, ai.getMode());
        
        // Inserting Row
        db.insert(TABLE_ACTIVITIES, null, values);
        
        // Get id
        String selectQuery = "SELECT "+ ACTIVITIES_ID + " FROM " + TABLE_ACTIVITIES 
        		+ " WHERE " + ACTIVITIES_MODE + "==" + Actify.MODE_RUNNING
        		+ " ORDER BY " + ACTIVITIES_ID + " DESC "
        		+ " LIMIT " + 1;
        Cursor cursor = db.rawQuery(selectQuery, null);
        int id = -1;
        if (cursor.moveToFirst()) {
        	id = Integer.parseInt(cursor.getString(0));
        }
        db.close(); // Closing database connection
        
        ActivityInstance new_ai = ai;
        new_ai.setId(id);
        return new_ai;        
    }    
    
    public String activityQueryBuilder(int user_id) {
    	String selectQuery = "SELECT * FROM " + TABLE_ACTIVITIES 
    			+ " WHERE (" + ACTIVITIES_MODE + "==" + Actify.MODE_RUNNING
        		+ " OR " + ACTIVITIES_MODE + "==" + Actify.MODE_PAUSED +") "
        		+ " AND " + ACTIVITIES_USERID + "==" + user_id
        		+ " ORDER BY " + ACTIVITIES_START+ " ASC ";
        return selectQuery;
    }   
    
    public String activityQueryBuilder(int user_id, Calendar cal) {
    	Calendar calendar = (Calendar) cal.clone();
    	String start = Actify.SQLiteDateFormat.format(calendar.getTime())+" 00:00:00";
    	calendar.add(Calendar.DAY_OF_YEAR, 1);
    	String end = Actify.SQLiteDateFormat.format(calendar.getTime())+" 00:00:00";
    	String selectQuery = 
    			"SELECT * FROM " + TABLE_ACTIVITIES 
        		+ " WHERE (" + ACTIVITIES_MODE + "==" + Actify.MODE_INSERT
        		+ " OR " + ACTIVITIES_MODE + "==" + Actify.MODE_UPDATE +") "
        		+ " AND " + ACTIVITIES_USERID + "==" + user_id
        		+ " AND " + ACTIVITIES_START + ">=" + "\"" + start + "\""
        		+ " AND " + ACTIVITIES_START + "<=" + "\"" + end + "\""
        		+ " ORDER BY " + ACTIVITIES_START + " DESC ";
        return selectQuery;
    } 
    
    public String activityQueryBuilder(int user_id, DateTime calStart, DateTime calEnd) {
    	String start = Actify.SQLiteDatetimeFormatJoda.print(calStart);
    	String end = Actify.SQLiteDatetimeFormatJoda.print(calEnd);
    	String selectQuery = 
    			"SELECT *, strftime('%s',"+ ACTIVITIES_END +") - strftime('%s',"+ ACTIVITIES_START +") AS duration"
    			+" FROM " + TABLE_ACTIVITIES 
        		+ " WHERE (" + ACTIVITIES_MODE + "==" + Actify.MODE_INSERT
        		+ " OR " + ACTIVITIES_MODE + "==" + Actify.MODE_UPDATE +") "
        		+ " AND " + ACTIVITIES_USERID + "==" + user_id
        		+ " AND " + ACTIVITIES_END + ">=" + "\"" + start + "\""
        		+ " AND " + ACTIVITIES_START + "<=" + "\"" + end + "\""
        		+ " ORDER BY duration DESC ";
        return selectQuery;
    } 
    
    
    public List<ActivityInstance> getActivityList(String selectQuery) {
        List<ActivityInstance> activityList = new ArrayList<ActivityInstance>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {            	
            	int id = Integer.parseInt(cursor.getString(0));
            	int userid = Integer.parseInt(cursor.getString(1));
            	int activityid = Integer.parseInt(cursor.getString(2));
            	int locationid = Integer.parseInt(cursor.getString(3));                
            	Calendar calStart = Calendar.getInstance();
            	Calendar calEnd = Calendar.getInstance();            	
                try {
					calStart.setTime(Actify.SQLiteDatetimeFormat.parse(cursor.getString(4)));
					calEnd.setTime(Actify.SQLiteDatetimeFormat.parse(cursor.getString(5)));
				} catch (ParseException e) {
					e.printStackTrace();
					Log.e("ActifySQLiteHelper", "Datetime parsing error", e);
				}
                int sync = Integer.parseInt(cursor.getString(6));
                int mode = Integer.parseInt(cursor.getString(7));
                ActivityInstance ai = new ActivityInstance(id, activityid, userid, calStart, calEnd, locationid, sync, mode);            	
            	activityList.add(ai);
            } while (cursor.moveToNext());
        }
 
        db.close();
        // return activity list
        return activityList;
    }
    
    public int countRunningActivity(int user_id) {
    	String selectQuery = 
    			"SELECT COUNT(*) FROM " + TABLE_ACTIVITIES 
        		+ " WHERE (" + ACTIVITIES_MODE + "==" + Actify.MODE_RUNNING
        		+ " OR " + ACTIVITIES_MODE + "==" + Actify.MODE_PAUSED +") "
        		+ " AND " + ACTIVITIES_USERID + "==" + user_id;
    	SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int count = 0;
        if (cursor.moveToFirst()) {
        	count = Integer.parseInt(cursor.getString(0));
        }        	
        db.close();
        
        return count;
    }
    
    public boolean exportActivityList(String filename, int user_id) {
    	boolean returnCode = false;
    	
    	String selectQuery = "SELECT * FROM " + TABLE_ACTIVITIES
    			+ " WHERE (" + ACTIVITIES_MODE + "==" + Actify.MODE_INSERT
    			+ " OR " + ACTIVITIES_MODE + "==" + Actify.MODE_UPDATE + ")"
    			+ " AND " + ACTIVITIES_USERID + "==" + user_id
        		+ " ORDER BY " + ACTIVITIES_START+ " ASC ";
    	List<ActivityInstance> list =  getActivityList(selectQuery);

    	String csv = 
    			ACTIVITIES_ID + ","
    			+ ACTIVITIES_USERID + ","
    			+ ACTIVITIES_ACTIVITYID + ","
    			+ ACTIVITIES_LOCATIONID + ","
    			+ ACTIVITIES_START + ","
    			+ ACTIVITIES_END + "\n";
    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File (sdCard.getAbsolutePath() + "/Actify");
    		dir.mkdirs();
    		File outFile = new File(dir, filename);
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(csv);
            for (ActivityInstance ai : list) {
            	csv = ai.getId() + ","
            			+ ai.getuserid() + ","
            			+ ai.getactivityid() + ","
            			+ ai.getlocationid() + ","
            			+ Actify.datetimeFormat.format(ai.getStart().getTime()) + ","
            			+ Actify.datetimeFormat.format(ai.getEnd().getTime()) + "\n";
            	out.write(csv);
            }
            out.close();
    		returnCode = true;
    	} catch (Exception e) {
            returnCode = false;
        }
    	return returnCode;
    }
    
    public boolean exportActivityLog(String filename, int user_id) {
    	boolean returnCode = false;
    	
    	String selectQuery = "SELECT " + TABLE_ACTIVITIES_LOG + ".* "
    			+ " FROM " + TABLE_ACTIVITIES_LOG + "," + TABLE_ACTIVITIES
    			+ " WHERE " + TABLE_ACTIVITIES + "."+ ACTIVITIES_USERID + "==" + user_id
    			+ " AND " + TABLE_ACTIVITIES + "." + ACTIVITIES_ID + "==" + TABLE_ACTIVITIES_LOG + "." + TABLE_ACTIVITIES + ID
        		+ " ORDER BY " + TABLE_ACTIVITIES_LOG + "." + TIME + " ASC ";
    	
    	SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        String csv = 
    			ID + ","
    			+ TABLE_ACTIVITIES + ACTIVITIES_ID + ","
    			+ ACTIVITIES_USERID + ","
    			+ ACTIVITIES_MODE + ","
    			+ TIME + ","
    			+ ACTION + ","
    			+ ACTIVITIES_ACTIVITYID + "_OLD," 
    			+ ACTIVITIES_LOCATIONID + "_OLD," 
    			+ ACTIVITIES_START +  "_OLD," 
    			+ ACTIVITIES_END +  "_OLD," 
    			+ ACTIVITIES_ACTIVITYID + "_NEW," 
    			+ ACTIVITIES_LOCATIONID + "_NEW," 
    			+ ACTIVITIES_START +  "_NEW," 
    			+ ACTIVITIES_END +  "_NEW,"     			
    			+ "\n";        

    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File (sdCard.getAbsolutePath() + "/Actify");
    		dir.mkdirs();
    		File outFile = new File(dir, filename);
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(csv);
            if (cursor.moveToFirst()) {
                do {        
                	csv = "";
                	for (int i=0; i < cursor.getColumnCount() - 1; i++) {
                		csv += cursor.getString(i)+",";                		
                	}
                	csv += cursor.getString(cursor.getColumnCount() - 1) + "\n";
                	out.write(csv);
                } while (cursor.moveToNext());
            }
            db.close();
            out.close();
    		returnCode = true;
    	} catch (Exception e) {
            returnCode = false;
        }
    	return returnCode;
    }
    
    public ActivityInstance getActivityInstance(int iid) {
    	String selectQuery = "SELECT * FROM " + TABLE_ACTIVITIES 
    			+ " WHERE " + ACTIVITIES_ID + "==" + iid;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ActivityInstance ai = null;
 
        if (cursor.moveToFirst()) {
        	
        	int id = Integer.parseInt(cursor.getString(0));
        	int userid = Integer.parseInt(cursor.getString(1));
        	int activityid = Integer.parseInt(cursor.getString(2));
        	int locationid = Integer.parseInt(cursor.getString(3));                
        	Calendar calStart = Calendar.getInstance();
        	Calendar calEnd = Calendar.getInstance();            	
            try {
				calStart.setTime(Actify.SQLiteDatetimeFormat.parse(cursor.getString(4)));
				calEnd.setTime(Actify.SQLiteDatetimeFormat.parse(cursor.getString(5)));
			} catch (ParseException e) {
				e.printStackTrace();
				Log.e("ActifySQLiteHelper", "Datetime parsing error", e);
			}
            int sync = Integer.parseInt(cursor.getString(6));
            int mode = Integer.parseInt(cursor.getString(7));
            ai = new ActivityInstance(id, activityid, userid, calStart, calEnd, locationid, sync, mode);            	

        }
 
        db.close();
        return ai;
    }
    

    
    public int getActivityDuration(ActivityInstance ai) {
    	String start = Actify.SQLiteDatetimeFormat.format(ai.getStart().getTime());
    	String end = Actify.SQLiteDatetimeFormat.format(ai.getEnd().getTime());
    	String selectQuery = "SELECT strftime('%s','"+ end+"') - strftime('%s','"+ start +"')";
    	SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int res = 0;
        if (cursor.moveToFirst()) {
        	res = Integer.parseInt(cursor.getString(0));
        }
        db.close();
        return res;
    }
    
    public List<SliceInfo> getActivityDuration(int userid, Calendar calStart, Calendar calEnd) {
    	List<SliceInfo> sliceInfos = new ArrayList<SliceInfo>();
    	String start = Actify.SQLiteDatetimeFormat.format(calStart.getTime());
    	String end = Actify.SQLiteDatetimeFormat.format(calEnd.getTime());
    	
    	String selectQuery = 
    			"SELECT " + ACTIVITIES_ACTIVITYID + ","
    			+ " SUM (duration) AS sum, COUNT (duration) FROM "
    			+ " (SELECT "+ ACTIVITIES_ACTIVITYID +","
    			+ "strftime('%s',"+ ACTIVITIES_END +") - strftime('%s',"+ ACTIVITIES_START +") AS duration"
    			+ " FROM " + TABLE_ACTIVITIES
    			+ " WHERE " + ACTIVITIES_START + ">=" + "\"" + start + "\""
        		+ " AND " + ACTIVITIES_START + "<=" + "\"" + end + "\""
        		+ " AND " + ACTIVITIES_USERID + "==" + userid 
        		+ " AND (" + ACTIVITIES_MODE + "==" + Actify.MODE_INSERT
        		+ " OR " + ACTIVITIES_MODE + "==" + Actify.MODE_UPDATE + ")"
        		+ ") AS tableduration "        	
        		+ " GROUP BY " + ACTIVITIES_ACTIVITYID
        		+ " ORDER BY sum DESC" ;
    	SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
        	do {   
	        	int activityid = Integer.parseInt(cursor.getString(0));
	        	float duration = Float.parseFloat(cursor.getString(1));
	        	int count = Integer.parseInt(cursor.getString(2));
	        	sliceInfos.add(new SliceInfo(activityid, duration, count));
        	} while (cursor.moveToNext());
        }
        db.close();
        return sliceInfos;
    }
    
    public float getPauseDuration(int activityid, int userid, Calendar calStart, Calendar calEnd) {
    	float duration = 0;
    	String start = Actify.SQLiteDateFormat.format(calStart.getTime())+" 00:00:00";
    	String end = Actify.SQLiteDatetimeFormat.format(calEnd.getTime())+" 00:00:00";
    	String selectQuery =
    			"SELECT SUM (duration) FROM "
    			+ " (SELECT strftime('%s',"+ TABLE_ACTIVITYPAUSES+"."+ACTIVITYPAUSES_END +") - strftime('%s',"+ TABLE_ACTIVITYPAUSES+"."+ACTIVITYPAUSES_START +") AS duration"
    			+ " FROM " + TABLE_ACTIVITYPAUSES +"," + TABLE_ACTIVITIES
    			+ " WHERE " + TABLE_ACTIVITYPAUSES+"."+ ACTIVITYPAUSES_ACTIVITIESID + "=="+TABLE_ACTIVITIES+"."+ACTIVITIES_ID
        		+ " AND " + TABLE_ACTIVITYPAUSES+"."+ACTIVITYPAUSES_MODE + "<>" + Actify.MODE_DELETE 
        		+ " AND " + TABLE_ACTIVITIES+"."+ACTIVITIES_START + ">=" + "\"" + start + "\""
        		+ " AND " + TABLE_ACTIVITIES+"."+ACTIVITIES_START + "<=" + "\"" + end + "\""
        		+ " AND " + TABLE_ACTIVITIES+"."+ACTIVITIES_ACTIVITYID + "==" + activityid
        		+ " AND " + TABLE_ACTIVITIES+"."+ACTIVITIES_USERID + "==" + userid 
        		+ ") AS tableduration ";
    	SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
        	if (cursor.getString(0)!=null)
        		duration = Float.parseFloat(cursor.getString(0));
        }
        db.close();
        return duration;
    }
    
    
    
    // Updating single activity
    public void updateActivity(ActivityInstance ai) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(ACTIVITIES_USERID, ai.getuserid()); 
        values.put(ACTIVITIES_ACTIVITYID, ai.getactivityid());
        values.put(ACTIVITIES_LOCATIONID, ai.getlocationid());
        values.put(ACTIVITIES_START, Actify.SQLiteDatetimeFormat.format(ai.getStart().getTime()));
        values.put(ACTIVITIES_END, Actify.SQLiteDatetimeFormat.format(ai.getEnd().getTime()));
        values.put(ACTIVITIES_SYNC, ai.getSync());
        values.put(ACTIVITIES_MODE, ai.getMode());
 
        // updating row
        db.update(TABLE_ACTIVITIES, values, ACTIVITIES_ID + " = ?",
                new String[] { String.valueOf(ai.getId()) });
        
        db.close();
    }
    
    // Deleting single activity
    public void deleteActivity(ActivityInstance ai) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACTIVITIES, ACTIVITIES_ID + " = ?",
                new String[] { String.valueOf(ai.getId()) });
        db.close();
    }  
    
    public void deleteLatestActivityLog() {
    	SQLiteDatabase db = this.getWritableDatabase();
    	String selectQuery = "SELECT "+ ID + " FROM " + TABLE_ACTIVITIES_LOG 
        		+ " ORDER BY " + ID + " DESC "
        		+ " LIMIT " + 1;
    	Cursor cursor = db.rawQuery(selectQuery, null);
        int id = -1;
        if (cursor.moveToFirst()) {
        	id = Integer.parseInt(cursor.getString(0));
        	db.delete(TABLE_ACTIVITIES_LOG, ID + " = ?",
                    new String[] { String.valueOf(id) });
        }        
        db.close(); // Closing database connection
    }
    
    // Update after synchronizing
    public void updateActivitySyncMode(List<ActivityInstance> list, int sync, int mode) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(ACTIVITIES_SYNC, sync);
        values.put(ACTIVITIES_MODE, mode);
        
        for (int i=0; i < list.size(); i++) {
        	db.update(TABLE_ACTIVITIES, values, ACTIVITIES_ID + " = ?", 
        			new String[] {String.valueOf(list.get(i).getId())});
        }
 
        db.close();
    }
    
    // Delete after synchronizing
    public void deleteActivitySyncMode(List<ActivityInstance> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i=0; i < list.size(); i++) {
        	db.delete(TABLE_ACTIVITIES, ACTIVITIES_ID + " = ?",
        			new String[] {String.valueOf(list.get(i).getId())});
        }
        db.close();
    }  
    
    /**
     * TABLE_GUESTS
     */
    // Adding new guest 
    public Guest addGuest(Guest guest) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(GUESTS_NAME, guest.getName());
        values.put(GUESTS_START, Actify.SQLiteDatetimeFormat.format(guest.getStart().getTime()));
        values.put(GUESTS_END, Actify.SQLiteDatetimeFormat.format(guest.getEnd().getTime()));
        values.put(GUESTS_HOUSEHOLDID, guest.getHouseholdid());
        values.put(GUESTS_SYNC, guest.getSync());
        values.put(GUESTS_MODE, guest.getMode());
 
        // Inserting Row
        db.insert(TABLE_GUESTS, null, values);
        
        // Get id
        String selectQuery = "SELECT "+ GUESTS_ID + " FROM " + TABLE_GUESTS 
        		+ " WHERE " + GUESTS_MODE + "==" + Actify.MODE_RUNNING
        		+ " ORDER BY " + GUESTS_ID + " DESC "
        		+ " LIMIT " + 1;
        Cursor cursor = db.rawQuery(selectQuery, null);
        int id = -1;
        if (cursor.moveToFirst()) {
        	id = Integer.parseInt(cursor.getString(0));
        }
        db.close(); // Closing database connection
        
        Guest new_guest = guest;
        new_guest.setId(id);
        return new_guest;  
        
    }
    
    public String guestQueryBuilder(String householdid) {
    	String 	selectQuery = 
    			"SELECT * FROM " + TABLE_GUESTS 
    			+ " WHERE (" + GUESTS_MODE + "==" + Actify.MODE_RUNNING
        		+ " OR " + GUESTS_MODE + "==" + Actify.MODE_PAUSED +") "
        		+ " AND " + GUESTS_HOUSEHOLDID + " == " + "\"" +householdid + "\""
        		+ " ORDER BY " + GUESTS_START + " ASC ";

        return selectQuery;
    }  
    
    public String guestQueryBuilder(ActivityInstance ai, String householdid) {
    	String END;
    	if (ai.getMode()==Actify.MODE_RUNNING || ai.getMode()==Actify.MODE_PAUSED) {
    		END = "\"" +Actify.SQLiteDatetimeFormat.format(Calendar.getInstance().getTimeInMillis())+ "\"";    		
    	} else {
    	 	END = TABLE_ACTIVITIES + "." +  ACTIVITIES_END ;				
    	}
    	String selectQuery =
    			"SELECT " + TABLE_GUESTS + "." + GUESTS_ID + ","
    			+ TABLE_GUESTS + "." +  GUESTS_NAME + ","
    			+ TABLE_GUESTS + "." +  GUESTS_START + ","
    			+ TABLE_GUESTS + "." +  GUESTS_END + ","
    			+ TABLE_GUESTS + "." +  GUESTS_HOUSEHOLDID + ","
    			+ TABLE_GUESTS + "." +  GUESTS_SYNC + ","
    			+ TABLE_GUESTS + "." +  GUESTS_MODE 
    			+ " FROM " + TABLE_GUESTS + "," + TABLE_ACTIVITIES
    			+ " WHERE "+ TABLE_GUESTS + "." +  GUESTS_START + " <" + END
    			+ " AND " + TABLE_GUESTS + "." + GUESTS_MODE +"<>" + Actify.MODE_DELETE
    			+ " AND " + TABLE_ACTIVITIES + "." +  ACTIVITIES_ID + " == " + ai.getId() 
    			+ " AND ("+ TABLE_GUESTS + "." +  GUESTS_END + " > " + TABLE_ACTIVITIES + "." +  ACTIVITIES_START +""
    			+ " OR "+ TABLE_GUESTS + "." +  GUESTS_MODE + " == " + Actify.MODE_RUNNING +")"
    			+ " AND " + GUESTS_HOUSEHOLDID + " == " + "\""+ householdid +"\""
    			+ " ORDER BY " + TABLE_GUESTS + "." +GUESTS_START + " DESC ";
    			   
        return selectQuery;
    } 
    
    public String guestQueryBuilder(String householdid, Calendar cal) {
    	Calendar calendar = (Calendar) cal.clone();
    	String start = Actify.SQLiteDateFormat.format(calendar.getTime())+" 00:00:00";
    	calendar.add(Calendar.DAY_OF_YEAR, 1);
    	String end = Actify.SQLiteDateFormat.format(calendar.getTime())+" 00:00:00";
    	String selectQuery = 
    			"SELECT * FROM " + TABLE_GUESTS 
        		+ " WHERE (" + GUESTS_MODE + "==" + Actify.MODE_INSERT
        		+ " OR " + GUESTS_MODE + "==" + Actify.MODE_UPDATE +") "
        		+ " AND " + GUESTS_HOUSEHOLDID + "==" +"\""+ householdid + "\""
        		+ " AND " + GUESTS_START + ">=" + "\"" + start + "\""
        		+ " AND " + GUESTS_START + "<=" + "\"" + end + "\""
        		+ " ORDER BY " + GUESTS_START + " DESC ";
        return selectQuery;
    } 
    
    
    public List<Guest> getGuestList(String selectQuery) {
        List<Guest> guestList = new ArrayList<Guest>();
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {            	
            	int id = Integer.parseInt(cursor.getString(0));
            	String name = cursor.getString(1);
            	Calendar calStart = Calendar.getInstance();
            	Calendar calEnd = Calendar.getInstance();
                try {
					calStart.setTime(Actify.SQLiteDatetimeFormat.parse(cursor.getString(2)));
					calEnd.setTime(Actify.SQLiteDatetimeFormat.parse(cursor.getString(3)));
				} catch (ParseException e) {
					e.printStackTrace();
					Log.e("ActifySQLiteHelper", "Datetime parsing error", e);
				}
                String householdid = cursor.getString(4);
                int sync = Integer.parseInt(cursor.getString(5));
                int mode = Integer.parseInt(cursor.getString(6));
                Guest guest = new Guest(id, name, calStart, calEnd, householdid, sync, mode);            	
                guestList.add(guest);
            } while (cursor.moveToNext());
        }
        db.close();
        return guestList;
    }
    
    public boolean exportGuestList(String filename, String householdid) {
    	boolean returnCode = false;
    	String 	selectQuery = 
    			"SELECT * FROM " + TABLE_GUESTS 
    			+ " WHERE (" + GUESTS_MODE + "==" + Actify.MODE_INSERT
        		+ " OR " + GUESTS_MODE + "==" + Actify.MODE_UPDATE +") "
        		+ " AND " + GUESTS_HOUSEHOLDID + " == " + "\"" +householdid + "\""
        		+ " ORDER BY " + GUESTS_START + " ASC ";
    	List<Guest> list =  getGuestList(selectQuery);

    	String csv = 
    			GUESTS_ID + ","
    			+ GUESTS_NAME + ","
    			+ GUESTS_START + ","
    			+ GUESTS_END + ","
    			+ GUESTS_HOUSEHOLDID + "\n";
    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File (sdCard.getAbsolutePath() + "/Actify");
    		dir.mkdirs();
    		File outFile = new File(dir, filename);
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(csv);
            for (Guest g : list) {
            	csv = g.getId() + ","
            			+ g.getName() + ","
            			+ Actify.datetimeFormat.format(g.getStart().getTime()) + ","
            			+ Actify.datetimeFormat.format(g.getEnd().getTime()) + ","
            			+ g.getHouseholdid() + "\n";
            	out.write(csv);
            }
            out.close();
    		returnCode = true;
    	} catch (Exception e) {
            returnCode = false;
        }
    	return returnCode;
    }
    
    public boolean exportGuestLog(String filename, String householdid) {
    	boolean returnCode = false;
    	
    	String selectQuery = "SELECT " + TABLE_GUESTS_LOG + ".* "
    			+ " FROM " + TABLE_GUESTS_LOG + "," + TABLE_GUESTS
    			+ " WHERE " + TABLE_GUESTS + "."+ GUESTS_HOUSEHOLDID + "==" + "'" + householdid + "'"
    			+ " AND " + TABLE_GUESTS + "." + GUESTS_ID+ "==" + TABLE_GUESTS_LOG + "." + TABLE_GUESTS + ID
        		+ " ORDER BY " + TABLE_GUESTS_LOG + "." + TIME + " ASC ";
    	
    	SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        String csv = 
    			ID + ","
    			+ TABLE_GUESTS + GUESTS_ID + ","
    			+ ACTIVITIES_USERID + ","
    			+ GUESTS_MODE + ","
    			+ TIME + ","
    			+ ACTION + ","
    			+ GUESTS_NAME + "_OLD," 
    			+ GUESTS_START +  "_OLD," 
    			+ GUESTS_END +  "_OLD,"  
    			+ GUESTS_NAME + "_NEW," 
    			+ GUESTS_START +  "_NEW," 
    			+ GUESTS_END +  "_NEW"     			
    			+ "\n";        

    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File (sdCard.getAbsolutePath() + "/Actify");
    		dir.mkdirs();
    		File outFile = new File(dir, filename);
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(csv);
            if (cursor.moveToFirst()) {
                do {        
                	csv = "";
                	for (int i=0; i < cursor.getColumnCount() - 1; i++) {
                		csv += cursor.getString(i)+",";                		
                	}
                	csv += cursor.getString(cursor.getColumnCount() - 1) + "\n";
                	out.write(csv);
                } while (cursor.moveToNext());
            }
            db.close();
            out.close();
    		returnCode = true;
    	} catch (Exception e) {
            returnCode = false;
        }
    	return returnCode;
    }
    
    // Updating single guest
    public int updateGuest(Guest guest) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(GUESTS_NAME, guest.getName());
        values.put(GUESTS_START, Actify.SQLiteDatetimeFormat.format(guest.getStart().getTime()));
        values.put(GUESTS_END, Actify.SQLiteDatetimeFormat.format(guest.getEnd().getTime()));
        values.put(GUESTS_SYNC, guest.getSync());
        values.put(GUESTS_MODE, guest.getMode());
 
        // updating row
        return db.update(TABLE_GUESTS, values, GUESTS_ID + " = ?",
                new String[] { String.valueOf(guest.getId()) });
    }
    
    // Deleting single guest
    public void deleteGuest(Guest guest) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GUESTS, GUESTS_ID + " = ?",
                new String[] { String.valueOf(guest.getId()) });
        db.close();
    }
    
    public void deleteLatestGuestLog() {
    	SQLiteDatabase db = this.getWritableDatabase();
    	String selectQuery = "SELECT "+ ID + " FROM " + TABLE_GUESTS_LOG 
        		+ " ORDER BY " + ID + " DESC "
        		+ " LIMIT " + 1;
    	Cursor cursor = db.rawQuery(selectQuery, null);
        int id = -1;
        if (cursor.moveToFirst()) {
        	id = Integer.parseInt(cursor.getString(0));
        	db.delete(TABLE_GUESTS_LOG, ID + " = ?",
                    new String[] { String.valueOf(id) });
        }        
        db.close(); // Closing database connection
    }
    
 // Update after synchronizing
    public void updateGuestSyncMode(List<Guest> list, int sync, int mode) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(GUESTS_SYNC, sync);
        values.put(GUESTS_MODE, mode);
        
        for (int i=0; i < list.size(); i++) {
        	db.update(TABLE_GUESTS, values, GUESTS_ID + " = ?", 
            		new String[] {String.valueOf(list.get(i).getId())});
        }    
        db.close();
    }
    
    // Delete after synchronizing
    public void deleteGuestSyncMode(List<Guest> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i=0; i < list.size(); i++) {
        	db.delete(TABLE_GUESTS, GUESTS_ID + " = ?",
        			new String[] {String.valueOf(list.get(i).getId())});
        }
        
        db.close();
    }  
    
    /**
     * TABLE_ACTIVITYPAUSES
     */
    
 // Adding new pause 
    public ActivityPause addActivityPause(ActivityPause ap) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(ACTIVITYPAUSES_ACTIVITIESID, ap.getActivities_id());
        values.put(ACTIVITYPAUSES_START, Actify.SQLiteDatetimeFormat.format(ap.getStart().getTime()));
        values.put(ACTIVITYPAUSES_END, Actify.SQLiteDatetimeFormat.format(ap.getEnd().getTime()));
        values.put(ACTIVITYPAUSES_SYNC, ap.getSync());
        values.put(ACTIVITYPAUSES_MODE, ap.getMode());
 
        // Inserting Row
        db.insert(TABLE_ACTIVITYPAUSES, null, values);
        
        // Get id
        String selectQuery = "SELECT "+ ACTIVITYPAUSES_ID + " FROM " + TABLE_ACTIVITYPAUSES 
        		+ " WHERE " + ACTIVITYPAUSES_MODE + "==" + Actify.MODE_RUNNING
        		+ " ORDER BY " +ACTIVITYPAUSES_ID + " DESC "
        		+ " LIMIT " + 1;
        Cursor cursor = db.rawQuery(selectQuery, null);
        int id = -1;
        if (cursor.moveToFirst()) {
        	id = Integer.parseInt(cursor.getString(0));
        }
        db.close(); // Closing database connection
        
        ActivityPause new_ap= ap;
        ap.setId(id);
        return new_ap;  
        
    }
    
    // Updating single guest
    public int updateActivityPause(ActivityPause ap) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(ACTIVITYPAUSES_ACTIVITIESID, ap.getActivities_id());
        values.put(ACTIVITYPAUSES_START, Actify.SQLiteDatetimeFormat.format(ap.getStart().getTime()));
        values.put(ACTIVITYPAUSES_END, Actify.SQLiteDatetimeFormat.format(ap.getEnd().getTime()));
        values.put(ACTIVITYPAUSES_SYNC, ap.getSync());
        values.put(ACTIVITYPAUSES_MODE, ap.getMode());
 
        // updating row
        return db.update(TABLE_ACTIVITYPAUSES, values, ACTIVITYPAUSES_ID + " = ?",
                new String[] { String.valueOf(ap.getId()) });
    }
    
    public int updateActivityPause(int activities_id, int sync, int mode) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(ACTIVITYPAUSES_SYNC, sync);
        values.put(ACTIVITYPAUSES_MODE, mode);
 
        // updating row
        return db.update(TABLE_ACTIVITYPAUSES, values, ACTIVITYPAUSES_ACTIVITIESID + " = ?",
                new String[] { String.valueOf(activities_id) });
    }
    
    public String activityPauseQueryBuilder(int viewmode, int activities_id) {
    	String selectQuery = ""; 
        if (viewmode == Actify.VIEW_HISTORY) {
        	selectQuery = "SELECT * FROM " + TABLE_ACTIVITYPAUSES 
        		+ " WHERE (" + ACTIVITYPAUSES_MODE + "==" + Actify.MODE_INSERT
        		+ " OR " + ACTIVITYPAUSES_MODE + "==" + Actify.MODE_UPDATE +") "
        		+ " AND " + ACTIVITYPAUSES_ACTIVITIESID + "==" + activities_id
        		+ " ORDER BY " + ACTIVITYPAUSES_START + " ASC ";
        } else if (viewmode == Actify.VIEW_TIMER){
        	selectQuery = "SELECT * FROM " + TABLE_ACTIVITYPAUSES 
        			+ " WHERE " + ACTIVITYPAUSES_MODE + "==" + Actify.MODE_RUNNING
            		+ " AND " + ACTIVITYPAUSES_ACTIVITIESID + "==" + activities_id
            		+ " ORDER BY " + ACTIVITYPAUSES_START + " ASC ";
        }
        return selectQuery;
    }
    
    public List<ActivityPause> getActivityPauseList(String selectQuery) {
        List<ActivityPause> pauseList = new ArrayList<ActivityPause>();        
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {            	
            	int id = Integer.parseInt(cursor.getString(0));
            	int activities_id = Integer.parseInt(cursor.getString(1));
            	
            	Calendar start = Calendar.getInstance();
            	Calendar end = Calendar.getInstance();
                try {
                	start.setTime(Actify.SQLiteDatetimeFormat.parse(cursor.getString(2)));
                	end.setTime(Actify.SQLiteDatetimeFormat.parse(cursor.getString(3)));
				} catch (ParseException e) {
					e.printStackTrace();
					Log.e("ActifySQLiteHelper", "Datetime parsing error", e);
				}
            	
                int sync = Integer.parseInt(cursor.getString(4));
                int mode = Integer.parseInt(cursor.getString(5));
                
                ActivityPause pause = new ActivityPause(id, activities_id, start, end, sync, mode);            	
                pauseList.add(pause);
            } while (cursor.moveToNext());
        }
        db.close();
        // return activity list
        return pauseList;
    }
    
    public boolean exportActivityPauseList(String filename, int user_id) {
    	boolean returnCode = false;
    	
    	String selectQuery = "SELECT "+ TABLE_ACTIVITYPAUSES +".*"
    			+ " FROM " + TABLE_ACTIVITYPAUSES + ", " + TABLE_ACTIVITIES 
        		+ " WHERE (" + TABLE_ACTIVITYPAUSES + "." + ACTIVITYPAUSES_MODE + "==" + Actify.MODE_INSERT
        		+ " OR " + TABLE_ACTIVITYPAUSES + "." + ACTIVITYPAUSES_MODE + "==" + Actify.MODE_UPDATE +") "
        		+ " AND " + TABLE_ACTIVITIES + "." + ACTIVITIES_USERID + "==" + user_id
        		+ " AND " + TABLE_ACTIVITIES + "." + ACTIVITIES_ID + "==" + TABLE_ACTIVITYPAUSES + "." + ACTIVITYPAUSES_ACTIVITIESID
        		+ " ORDER BY " + TABLE_ACTIVITYPAUSES + "." +ACTIVITYPAUSES_START + " ASC ";
    	List<ActivityPause> list =  getActivityPauseList(selectQuery);

    	String csv = 
    			""+ACTIVITYPAUSES_ID + ","
    			+ ""+ACTIVITYPAUSES_ACTIVITIESID + ","
    			+ ""+ACTIVITYPAUSES_START + ","
    			+ ""+ACTIVITYPAUSES_END + "\n";
    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File (sdCard.getAbsolutePath() + "/Actify");
    		dir.mkdirs();
    		File outFile = new File(dir, filename);
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(csv);
            for (ActivityPause ap : list) {
            	csv = ap.getId() + ","
            			+ ap.getActivities_id() + ","
            			+ Actify.datetimeFormat.format(ap.getStart().getTime()) + ","
            			+ Actify.datetimeFormat.format(ap.getEnd().getTime()) + "\n";
            	out.write(csv);
            }
            out.close();
    		returnCode = true;
    	} catch (Exception e) {
            returnCode = false;
        }
    	return returnCode;
    }
    
    public boolean exportActivityPauseLog(String filename, int user_id) {
    	boolean returnCode = false;
    	
    	String selectQuery = "SELECT " + TABLE_ACTIVITYPAUSES_LOG + ".* "
    			+ " FROM " + TABLE_ACTIVITYPAUSES_LOG + ", " + TABLE_ACTIVITIES 
    			+ " WHERE " + TABLE_ACTIVITIES + "." + ACTIVITIES_USERID + "==" + user_id
    			+ " AND " + TABLE_ACTIVITIES + "." + ACTIVITIES_ID + "==" + TABLE_ACTIVITYPAUSES_LOG + "." + ACTIVITYPAUSES_ACTIVITIESID
        		+ " ORDER BY " + TABLE_ACTIVITYPAUSES_LOG + "." + TIME + " ASC ";
    	
    	SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        String csv = 
    			ID + ","
    			+ TABLE_ACTIVITYPAUSES + ACTIVITYPAUSES_ID  + ","
    			+ ACTIVITYPAUSES_ACTIVITIESID + ","
    			+ ACTIVITYPAUSES_MODE + ","
    			+ TIME + ","
    			+ ACTION + ","
    			+ ACTIVITYPAUSES_START +  "_OLD," 
    			+ ACTIVITYPAUSES_END +  "_OLD,"  
    			+ ACTIVITYPAUSES_START +  "_NEW," 
    			+ ACTIVITYPAUSES_END +  "_NEW"     			
    			+ "\n";        

    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File (sdCard.getAbsolutePath() + "/Actify");
    		dir.mkdirs();
    		File outFile = new File(dir, filename);
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(csv);
            if (cursor.moveToFirst()) {
                do {        
                	csv = "";
                	for (int i=0; i < cursor.getColumnCount() - 1; i++) {
                		csv += cursor.getString(i)+",";                		
                	}
                	csv += cursor.getString(cursor.getColumnCount() - 1) + "\n";
                	out.write(csv);
                } while (cursor.moveToNext());
            }
            db.close();
            out.close();
    		returnCode = true;
    	} catch (Exception e) {
            returnCode = false;
        }
    	return returnCode;
    }
    
    public void deleteActivityPause(ActivityPause ap) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACTIVITYPAUSES, ACTIVITYPAUSES_ID + " = ?",
                new String[] { String.valueOf(ap.getId()) });
        db.close();
    }  
    
 // Update after synchronizing
    public void updateActivityPauseSyncMode(List<ActivityPause> list, int sync, int mode) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(ACTIVITYPAUSES_SYNC, sync);
        values.put(ACTIVITYPAUSES_MODE, mode);
        
        for (int i=0; i < list.size(); i++) {
 
        	 db.update(TABLE_ACTIVITYPAUSES, values, ACTIVITYPAUSES_ID + " = ?", 
        			 new String[] {String.valueOf(list.get(i).getId())});
        }
     
        db.close();
    }
    
    // Delete after synchronizing
    public void deleteActivityPauseSyncMode(List<ActivityPause> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i=0; i < list.size(); i++) {
        	db.delete(TABLE_ACTIVITYPAUSES, ACTIVITYPAUSES_ID + " = ?",
        			new String[] {String.valueOf(list.get(i).getId())});
        }
        
        db.close();
    } 
    
    public void deleteLatestActivityPauseLog() {
    	SQLiteDatabase db = this.getWritableDatabase();
    	String selectQuery = "SELECT "+ ID + " FROM " + TABLE_ACTIVITYPAUSES_LOG 
        		+ " ORDER BY " + ID + " DESC "
        		+ " LIMIT " + 1;
    	Cursor cursor = db.rawQuery(selectQuery, null);
        int id = -1;
        if (cursor.moveToFirst()) {
        	id = Integer.parseInt(cursor.getString(0));
        	db.delete(TABLE_ACTIVITYPAUSES_LOG, ID + " = ?",
                    new String[] { String.valueOf(id) });
        }        
        db.close(); // Closing database connection
    }
    
    /**
     * 
     * TABLE_ACTIVITYGUESTS
     */
    
    // adding new activity guest
    public void addActivityGuest(ActivityGuest ag) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // check if exist
        String selectQuery = "SELECT "+ ACTIVITYGUESTS_ID + " FROM " + TABLE_ACTIVITYGUESTS 
        		+ " WHERE " + ACTIVITYGUESTS_ACTIVITIESID + "==" + ag.getActivities_id()
        		+ " AND " + ACTIVITYGUESTS_GUESTSID + "==" + ag.getGuests_id();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.getCount() == 0) {
        	// add data
        	ContentValues values = new ContentValues();
            values.put(ACTIVITYGUESTS_ACTIVITIESID, ag.getActivities_id());
            values.put(ACTIVITYGUESTS_GUESTSID, ag.getGuests_id());
            values.put(ACTIVITYGUESTS_SYNC, ag.getSync());
            values.put(ACTIVITYGUESTS_MODE, ag.getMode());
            
            db.insert(TABLE_ACTIVITYGUESTS, null, values);
        } else if (cursor.moveToFirst()){
        	// it was deleted before, so just update
        	int id = Integer.parseInt(cursor.getString(0));
        	ContentValues values = new ContentValues();
        	values.put(ACTIVITYGUESTS_SYNC, ag.getSync());
        	values.put(ACTIVITYGUESTS_MODE, ag.getMode());
            // updating row
            db.update(TABLE_ACTIVITYGUESTS, values, ACTIVITYGUESTS_ID + " = ?",
                    new String[] { String.valueOf(id)});
            
        }
        db.close();       
    }
    
    public boolean isActivityGuest(int iactivities_id, int iguests_id) {
        
        String selectQuery = 
        		"SELECT * FROM " + TABLE_ACTIVITYGUESTS 
        		+ " WHERE " + ACTIVITYGUESTS_ACTIVITIESID + "==" + iactivities_id
        		+ " AND " + ACTIVITYGUESTS_GUESTSID + "==" + iguests_id
        		+ " AND " + ACTIVITYGUESTS_MODE + "<>" + Actify.MODE_DELETE;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() == 0)
        	return false;
        else return true;
    }
    
    
    public String activityGuestQueryBuilder(int viewmode, int mode, int activities_id) {
    	String selectQuery = ""; 
        if (viewmode == Actify.VIEW_HISTORY) {
        	selectQuery = "SELECT * FROM " + TABLE_ACTIVITYGUESTS 
        		+ " WHERE (" + ACTIVITYGUESTS_MODE + "==" + Actify.MODE_INSERT
        		+ " OR " + ACTIVITYGUESTS_MODE + "==" + Actify.MODE_UPDATE +") "
        		+ " ORDER BY " + ACTIVITYGUESTS_ID + " ASC ";
        } else if (viewmode == Actify.VIEW_TIMER) {
        	selectQuery = 
            		"SELECT * FROM " + TABLE_ACTIVITYGUESTS 
            		+ " WHERE " + ACTIVITYGUESTS_ACTIVITIESID + "==" + activities_id
            		+ " AND " + ACTIVITYGUESTS_MODE + "==" + mode
            		+ " ORDER BY " + ACTIVITYGUESTS_ID + " ASC ";
        }
        return selectQuery;
    }
    
    public List<ActivityGuest> getActivityGuestList(String selectQuery) {
        List<ActivityGuest> guestList = new ArrayList<ActivityGuest>();        
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {            	
            	int activities_id = Integer.parseInt(cursor.getString(1));
            	int guests_id = Integer.parseInt(cursor.getString(2));
                int sync = Integer.parseInt(cursor.getString(3));
                int mode = Integer.parseInt(cursor.getString(4));
                ActivityGuest guest = new ActivityGuest(activities_id, guests_id, sync, mode);            	
                guestList.add(guest);
            } while (cursor.moveToNext());
        }
        db.close();
        // return activity list
        return guestList;
    }
    
    public boolean exportActivityGuestList(String filename, int user_id) {
    	boolean returnCode = false;
    	String 	selectQuery = 
    			"SELECT "+ TABLE_ACTIVITYGUESTS +".*"
    			+ " FROM " + TABLE_ACTIVITYGUESTS + "," + TABLE_ACTIVITIES 
        		+ " WHERE (" + TABLE_ACTIVITYGUESTS + "." + ACTIVITYGUESTS_MODE + "==" + Actify.MODE_INSERT
        		+ " OR " + TABLE_ACTIVITYGUESTS + "." + ACTIVITYGUESTS_MODE + "==" + Actify.MODE_UPDATE +") "
        		+ " AND " + TABLE_ACTIVITIES + "." + ACTIVITIES_USERID + "==" + user_id
        		+ " AND " + TABLE_ACTIVITIES + "." + ACTIVITIES_ID + "==" + TABLE_ACTIVITYGUESTS + "." + ACTIVITYGUESTS_ID
        		+ " ORDER BY " + TABLE_ACTIVITYGUESTS + "." + ACTIVITYGUESTS_ID + " ASC ";
    	List<ActivityGuest> list =  getActivityGuestList(selectQuery);

    	String csv = 
    			ACTIVITYGUESTS_ACTIVITIESID + ","
    			+ ACTIVITYGUESTS_GUESTSID + "\n";
    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File (sdCard.getAbsolutePath() + "/Actify");
    		dir.mkdirs();
    		File outFile = new File(dir, filename);
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(csv);
            for (ActivityGuest ag : list) {
            	csv = ag.getActivities_id() + ","
            		+ ag.getGuests_id() + "\n";
            	out.write(csv);
            }
            out.close();
    		returnCode = true;
    	} catch (Exception e) {
            returnCode = false;
        }
    	return returnCode;
    }
    
    
    public List<String> getActivityGuestStringList(int activities_id) {
        List<String> guestList = new ArrayList<String>();
        
        String selectQuery = 
        		"SELECT "+ GUESTS_NAME +" FROM " + TABLE_ACTIVITYGUESTS +"," + TABLE_GUESTS
        		+ " WHERE " + ACTIVITYGUESTS_ACTIVITIESID + "==" + activities_id
        		+ " AND " + TABLE_ACTIVITYGUESTS+"."+ACTIVITYGUESTS_GUESTSID + "==" + TABLE_GUESTS +"."+GUESTS_ID
        		+ " AND " + TABLE_ACTIVITYGUESTS+"."+ACTIVITYGUESTS_MODE + "<>" + Actify.MODE_DELETE;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {            	
            	String name = cursor.getString(0);          	
                guestList.add(name);
            } while (cursor.moveToNext());
        }
        db.close();
        // return activity list
        return guestList;
    }
    
    public int updateActivityGuest(ActivityGuest ag) {
        SQLiteDatabase db = this.getWritableDatabase();
        
     // check if exist
        String selectQuery = "SELECT "+ ACTIVITYGUESTS_ID + " FROM " + TABLE_ACTIVITYGUESTS 
        		+ " WHERE " + ACTIVITYGUESTS_ACTIVITIESID + "==" + ag.getActivities_id()
        		+ " AND " + ACTIVITYGUESTS_GUESTSID + "==" + ag.getGuests_id();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
        	int id = Integer.parseInt(cursor.getString(0));
        	ContentValues values = new ContentValues();
            values.put(ACTIVITYGUESTS_SYNC, ag.getSync());
            values.put(ACTIVITYGUESTS_MODE, ag.getMode());
         // updating row
            return db.update(TABLE_ACTIVITYGUESTS, values, ACTIVITYGUESTS_ID + " = ?",
                    new String[] { String.valueOf(id)});
        }
 
        return 0;
        
    }
    
    public int updateActivityGuest(int activities_id, String ref, int sync, int mode) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(ACTIVITYGUESTS_SYNC, sync);
        values.put(ACTIVITYGUESTS_MODE, mode);
 
        // updating row
        return db.update(TABLE_ACTIVITYGUESTS, values, ref + " = ?",
                new String[] { String.valueOf(activities_id) });
    }
    

    
    
    public void deleteActivityGuest(ActivityGuest ag) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACTIVITYGUESTS, ACTIVITYGUESTS_ACTIVITIESID + " = ? AND "
        		+ ACTIVITYGUESTS_GUESTSID + " = ?",
                new String[] { String.valueOf(ag.getActivities_id()), String.valueOf(ag.getGuests_id()) });
        db.close();
    }
    
 // Update after synchronizing
    public void updateActivityGuestSyncMode(List<ActivityGuest> list, int sync, int mode) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(ACTIVITYGUESTS_SYNC, sync);
        values.put(ACTIVITYGUESTS_MODE, mode);
        
        for (int i=0; i < list.size(); i++) {
        	String activities_id = String.valueOf(list.get(i).getActivities_id());
        	String guests_id = String.valueOf(list.get(i).getGuests_id());
        	db.update(TABLE_ACTIVITYGUESTS, values, 
            		ACTIVITYGUESTS_ACTIVITIESID + " = ? AND "
            				+ ACTIVITYGUESTS_GUESTSID +"? ", 
            				new String[] {activities_id, guests_id});
        }
 
        db.close();
    }
    
    // Delete after synchronizing
    public void deleteActivityGuestSyncMode(List<ActivityGuest> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i=0; i < list.size(); i++) {
        	String activities_id = String.valueOf(list.get(i).getActivities_id());
        	String guests_id = String.valueOf(list.get(i).getGuests_id());
        	db.delete(TABLE_ACTIVITYGUESTS, 
            		ACTIVITYGUESTS_ACTIVITIESID + " = ? AND "
            				+ ACTIVITYGUESTS_GUESTSID +"? ", 
            				new String[] {activities_id, guests_id});
        }
        db.close();
    } 
    
    /**
     * TABLE_REMINDER
     */
	// Adding new reminder
    public void addReminder(Reminder rem) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();  
        values.put(REMINDER_TYPE, rem.getType());
        values.put(REMINDER_USERID, rem.getUserid());
        values.put(REMINDER_START, Actify.SQLiteDatetimeFormat.format(rem.getStart().getTime()));
        values.put(REMINDER_END, Actify.SQLiteDatetimeFormat.format(rem.getEnd().getTime()));        
        values.put(REMINDER_ACTIVITIESID, rem.getActivityid());
        values.put(REMINDER_ACTION, rem.getAction());
        
        // Inserting Row
        db.insert(TABLE_REMINDER, null, values);
                
        db.close(); // Closing database connection                       
    } 
    
    public List<Reminder> getReminderList(String selectQuery) {
        List<Reminder> reminderList = new ArrayList<Reminder>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {            	
            	int id = Integer.parseInt(cursor.getString(0));
            	int userid = Integer.parseInt(cursor.getString(1));
            	int type = Integer.parseInt(cursor.getString(2));            	
            	                
            	Calendar calStart = Calendar.getInstance();
            	Calendar calEnd = Calendar.getInstance();            	
                try {
					calStart.setTime(Actify.SQLiteDatetimeFormat.parse(cursor.getString(3)));
					calEnd.setTime(Actify.SQLiteDatetimeFormat.parse(cursor.getString(4)));
				} catch (ParseException e) {
					e.printStackTrace();
					Log.e("ActifySQLiteHelper", "Datetime parsing error", e);
				}
                int activityid = Integer.parseInt(cursor.getString(5));
                int action = Integer.parseInt(cursor.getString(6));

                Reminder rem = new Reminder(id, userid, type, calStart, calEnd, activityid, action);            	
            	reminderList.add(rem);
            } while (cursor.moveToNext());
        }
 
        db.close();
        // return activity list
        return reminderList;
    }
    
    public boolean exportReminderList(String filename, int user_id) {
    	boolean returnCode = false;
    	
    	String selectQuery = "SELECT * FROM " + TABLE_REMINDER
    			+ " WHERE " + REMINDER_USERID + "==" + user_id
        		+ " ORDER BY " + REMINDER_START+ " ASC ";
    	List<Reminder> list =  getReminderList(selectQuery);
    	
    	String csv = 
    			REMINDER_ID + ","
    			+ REMINDER_USERID + ","
    			+ REMINDER_TYPE + ","
    			+ REMINDER_START + ","
    			+ REMINDER_END + ","
    			+ REMINDER_ACTIVITIESID + ","
    			+ REMINDER_ACTION + "\n"
    			;
    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File (sdCard.getAbsolutePath() + "/Actify");
    		dir.mkdirs();
    		File outFile = new File(dir, filename);
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(csv);
            for (Reminder rem : list) {
            	csv = rem.getId() + ","            			
            			+ rem.getUserid() + ","
            			+ rem.getType() + ","
            			+ Actify.datetimeFormat.format(rem.getStart().getTime()) + ","
            			+ Actify.datetimeFormat.format(rem.getEnd().getTime()) + ","
            			+ rem.getActivityid() + ","
            			+ rem.getAction() + "\n"
            			;
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
