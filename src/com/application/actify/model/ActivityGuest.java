package com.application.actify.model;

import com.application.actify.core.Actify;

public class ActivityGuest {
	private int activities_id;
	private int guests_id;
	private int sync;
	private int mode;
	
	public ActivityGuest(int activities_id, int guests_id) {
		super();
		this.activities_id = activities_id;
		this.guests_id = guests_id;
		this.sync = Actify.NOT_SYNC;
		this.mode = Actify.MODE_RUNNING;
	}

	public ActivityGuest(int activities_id, int guests_id, int sync, int mode) {
		super();
		this.activities_id = activities_id;
		this.guests_id = guests_id;
		this.sync = sync;
		this.mode = mode;
	}

	public int getActivities_id() {
		return activities_id;
	}

	public void setActivities_id(int activities_id) {
		this.activities_id = activities_id;
	}

	public int getGuests_id() {
		return guests_id;
	}

	public void setGuests_id(int guests_id) {
		this.guests_id = guests_id;
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
