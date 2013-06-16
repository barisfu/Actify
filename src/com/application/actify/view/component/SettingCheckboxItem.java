package com.application.actify.view.component;


public class SettingCheckboxItem implements SettingItem{

	private final String title;
	private final String subtitle;
	private final int menuid;
	private final String icon;
	private boolean cbValue;

	public SettingCheckboxItem(String title, String subtitle, int menuid, String icon, boolean cbValue) {
		this.title = title;
		this.subtitle = subtitle;
		this.menuid = menuid;
		this.icon = icon;
		this.cbValue = cbValue;
	}
	
	@Override
	public boolean isSection() {
		return false;
	}

	public String getTitle() {
		return title;
	}
	
	public int getMenuId() {
		return menuid;
	}

	public String getIcon() {
		return icon;
	}

	@Override
	public boolean hasCheckbox() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isChecked() {
		return cbValue;
	}

	public void setChecked(boolean cbValue) {
		this.cbValue = cbValue;
	}
}
