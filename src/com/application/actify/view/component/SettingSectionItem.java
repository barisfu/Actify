package com.application.actify.view.component;

public class SettingSectionItem implements SettingItem{

	private final String title;
	
	public SettingSectionItem(String title) {
		this.title = title;
	}
	
	public String getTitle(){
		return title;
	}
	
	@Override
	public boolean isSection() {
		return true;
	}

	@Override
	public boolean hasCheckbox() {
		// TODO Auto-generated method stub
		return false;
	}

}
