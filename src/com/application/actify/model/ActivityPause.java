package com.application.actify.model;

import java.util.Calendar;

import com.application.actify.core.Actify;

public class ActivityPause {
	private int id;
	private int activities_id;
	private Calendar start;
	private Calendar end;
	private int sync;
	private int mode;
	
	public ActivityPause(int id, int activities_id, Calendar start, Calendar end,
			int sync, int mode) {
		super();
		this.id = id;
		this.activities_id = activities_id;
		this.start = start;
		this.end = end;
		this.sync = sync;
		this.mode = mode;
	}
	public ActivityPause(int activities_id, Calendar start, Calendar end) {
		super();
		this.activities_id = activities_id;
		this.start = start;
		this.end = end;
		this.sync = Actify.NOT_SYNC;
		this.mode = Actify.MODE_RUNNING;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getActivities_id() {
		return activities_id;
	}
	public void setActivities_id(int activities_id) {
		this.activities_id = activities_id;
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
