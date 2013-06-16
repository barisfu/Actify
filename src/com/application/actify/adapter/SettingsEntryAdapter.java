package com.application.actify.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.application.actify.R;
import com.application.actify.core.Actify;
import com.application.actify.view.component.SettingCheckboxItem;
import com.application.actify.view.component.SettingEntryItem;
import com.application.actify.view.component.SettingItem;
import com.application.actify.view.component.SettingSectionItem;


public class SettingsEntryAdapter extends ArrayAdapter<SettingItem> {

	private Context context;
	private ArrayList<SettingItem> items;
	private LayoutInflater vi;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private int userid;

	public SettingsEntryAdapter(Context context,ArrayList<SettingItem> items) {
		super(context,0, items);
		this.context = context;
		this.items = items;
		vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		settings = context.getSharedPreferences(Actify.PREFS_NAME, 0);
		editor = settings.edit();
		userid = settings.getInt("userid", -1);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		final SettingItem i = items.get(position);
		if (i != null) {
			if(i.isSection()){
				SettingSectionItem si = (SettingSectionItem)i;
				v = vi.inflate(R.layout.settings_item_section, null);

				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);
				
				final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
				sectionView.setText(si.getTitle());
			} else if (i.hasCheckbox()) {
				final SettingCheckboxItem ci = (SettingCheckboxItem) i;
				v = vi.inflate(R.layout.settings_item_checkbox, null);
				final TextView title = (TextView)v.findViewById(R.id.list_item_entry_title);
				if (title != null) 
					title.setText(ci.getTitle());
				final ImageView iv = (ImageView)v.findViewById(R.id.list_item_entry_drawable);
				int res = context.getResources()
						.getIdentifier("com.application.actify:drawable/"+ci.getIcon(), 
								null, null);
				iv.setImageResource(res);
				final CheckBox cb = (CheckBox) v.findViewById(R.id.cb);
				cb.setChecked(ci.isChecked());
				cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						String menuString = "";
						switch (ci.getMenuId()) {
						case Actify.MENU_SETTING_SOUND:
							menuString = "sound";
							break;
						}
						editor.remove(menuString+"_"+userid);				
						editor.putBoolean(menuString+"_"+userid, isChecked);
						editor.commit();
					}
					
				});
			} else{
				SettingEntryItem ei = (SettingEntryItem)i;
				v = vi.inflate(R.layout.settings_item_entry, null);
				final TextView title = (TextView)v.findViewById(R.id.list_item_entry_title);
				//final TextView subtitle = (TextView)v.findViewById(R.id.list_item_entry_summary);
				final ImageView iv = (ImageView)v.findViewById(R.id.list_item_entry_drawable);
				int res = context.getResources()
						.getIdentifier("com.application.actify:drawable/"+ei.getIcon(), 
								null, null);
				iv.setImageResource(res);
				if (title != null) 
					title.setText(ei.title);
				/*if(subtitle != null)
					subtitle.setText(ei.subtitle);*/
			}
		}
		return v;
	}

}
