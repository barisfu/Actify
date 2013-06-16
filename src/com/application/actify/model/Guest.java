package com.application.actify.model;

import java.util.Calendar;

import com.application.actify.core.Actify;

public class Guest {

	private int id;
	private String name;
	private Calendar start, end;
	private String householdid;
	private int sync;
	private int mode;
	
	public Guest(int id, String name, String householdid, Calendar start, Calendar end) {
		super();
		this.id = id;
		this.name = name;
		this.start = start;
		this.end = end;
		this.householdid = householdid;
		this.sync = Actify.NOT_SYNC;
		this.mode = Actify.MODE_RUNNING;
	}
	
	public Guest(int id, String name, Calendar start, Calendar end, String householdid, int sync, int mode) {
		super();
		this.id = id;
		this.name = name;
		this.start = start;
		this.end = end;
		this.householdid = householdid;
		this.sync = sync;
		this.mode = mode;
	}
	
	public Guest(String name, Calendar start, Calendar end, String householdid) {
		super();
		this.name = name;
		this.start = start;
		this.end = end;
		this.householdid = householdid;
		this.sync = Actify.NOT_SYNC;
		this.mode = Actify.MODE_RUNNING;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getHouseholdid() {
		return householdid;
	}

	public void setHouseholdid(String householdid) {
		this.householdid = householdid;
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
