package com.application.actify.model;

import java.util.Calendar;

import com.application.actify.core.Actify;

public class ActivityInstance  {

	private int id;
	private int activityid;
	private int userid;
	private Calendar start, end;
	private int locationid;
	private int sync;
	private int mode;
	
	public ActivityInstance(int id, int activityid, int userid, Calendar start,
			Calendar end, int locationid) {
		super();
		this.id = id;
		this.activityid = activityid;
		this.userid = userid;
		this.start = start;
		this.end = end;
		this.locationid = locationid;
		this.sync = Actify.NOT_SYNC;
		this.mode = Actify.MODE_RUNNING;
	}
	
	public ActivityInstance(int id, int activityid, int userid, Calendar start,
			Calendar end, int locationid, int sync, int mode) {
		super();
		this.id = id;
		this.activityid = activityid;
		this.userid = userid;
		this.start = start;
		this.end = end;
		this.locationid = locationid;
		this.sync = sync;
		this.mode = mode;
	}
	
	public ActivityInstance(int activityid, int userid, Calendar start,
			Calendar end, int locationid) {
		super();
		this.activityid = activityid;
		this.userid = userid;
		this.start = start;
		this.end = end;
		this.locationid = locationid;
		this.sync = Actify.NOT_SYNC;
		this.mode = Actify.MODE_RUNNING;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getactivityid() {
		return activityid;
	}
	public void setactivityid(int activityid) {
		this.activityid = activityid;
	}
	public int getuserid() {
		return userid;
	}
	public void setuserid(int userid) {
		this.userid = userid;
	}
	public Calendar getStart() {
		return start;
	}
	public void setStart(Calendar start) {
		this.start = start;
	}
	public Calendar getEnd() {
		return end;
	}
	public void setEnd(Calendar end) {
		this.end = end;
	}
	public int getlocationid() {
		return locationid;
	}
	public void setlocationid(int location) {
		this.locationid = location;
	}

	public int getSync() {
		return sync;
	}

	public void setSync(int sync) {
		this.sync = sync;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}
	
	
}
