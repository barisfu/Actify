package com.application.actify.view.component;

public class SliceInfo {
	private int activityid;
	private float duration;
	private int count;
	
	public SliceInfo(int activityid, float duration, int count) {
		super();
		this.activityid = activityid;
		this.duration = duration;
		this.count = count;
	}
	public int getActivityid() {
		return activityid;
	}
	public void setActivityid(int activityid) {
		this.activityid = activityid;
	}
	public float getDuration() {
		return duration;
	}
	public void setDuration(float duration) {
		this.duration = duration;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	
}
