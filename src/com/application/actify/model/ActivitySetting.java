package com.application.actify.model;

public class ActivitySetting implements Comparable<ActivitySetting> {
	private Integer id;
	private Integer order;
	private String activity;
	private String location;
	private String icon;
	private boolean visible;
	private int duration;
		
	public ActivitySetting(int id, int order, String activity, String location,
			String icon, boolean visible, int duration) {
		this.id = id;
		this.order = order;
		this.activity = activity;
		this.location = location;
		this.icon = icon;
		this.visible = visible;
		this.duration = duration;
		
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Integer getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	@Override
	public int compareTo(ActivitySetting another) {
		return this.order - another.order;
	}

}

