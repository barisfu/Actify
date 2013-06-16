package com.application.actify.component;


public class SettingEntryItem implements SettingItem{

	public final String title;
	public final String subtitle;
	public final int menuid;
	public final String icon;

	public SettingEntryItem(String title, String subtitle, int menuid, String icon) {
		this.title = title;
		this.subtitle = subtitle;
		this.menuid = menuid;
		this.icon = icon;
	}
	
	@Override
	public boolean isSection() {
		return false;
	}

	public String getTitle() {
		return title;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public int getMenuId() {
		return menuid;
	}

	@Override
	public boolean hasCheckbox() {
		// TODO Auto-generated method stub
		return false;
	}
}
