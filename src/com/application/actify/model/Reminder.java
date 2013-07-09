package com.application.actify.model;

import java.util.Calendar;

public class Reminder {
	public static final int REMINDER_IDLE = 0;
	public static final int REMINDER_ACTIVITY = 1;
	
	public static final int REMINDER_NO = 0;
	public static final int REMINDER_YES = 1;
	public static final int REMINDER_NOTHING = 2;
	
	private int id;
	private int type;
	private int userid;
	private Calendar start, end;
	private int activityid;
	private int action;
	
	public Reminder(int userid, int type, Calendar start, Calendar end,
			int activityid, int action) {
		super();
		this.type = type;
		this.userid = userid;
		this.start = start;
		this.end = end;
		this.activityid = activityid;
		this.action = action;
	}
	
	public Reminder(int id, int userid, int type, Calendar start, Calendar end,
			int activityid, int action) {
		super();
		this.id = id;
		this.type = type;
		this.userid = userid;
		this.start = start;
		this.end = end;
		this.activityid = activityid;
		this.action = action;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
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
	public int getActivityid() {
		return activityid;
	}
	public void setActivityid(int activityid) {
		this.activityid = activityid;
	}
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	
	
}
