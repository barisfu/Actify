package com.application.actify.adapter;

import com.application.actify.R;
import com.application.actify.core.Actify;
import com.application.actify.model.ActivitySetting;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;


public class SettingsVisibilityAdapter extends BaseAdapter {
    private Context mContext;
    private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private int userid;
 
    // Constructor
    public SettingsVisibilityAdapter(Context c){
        mContext = c;
        settings = mContext.getSharedPreferences(Actify.PREFS_NAME, 0);
		editor = settings.edit();
		userid = settings.getInt("userid", -1);
    }
 
    @Override
    public int getCount() {
    	return Actify.activitySettings.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mContext.getResources()
				.getIdentifier("com.application.actify:drawable/"+Actify.activitySettings.get(position).getIcon(), 
						null, null);
    }
 
    @Override
    public long getItemId(int position) {
        return 0;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	View v;
    	if(convertView==null){
    		LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.icon_checkbox, null);	
		} else {
			v = convertView;
		}
    	final ActivitySetting as = Actify.activitySettings.get(position);
		
    	TextView tv = (TextView)v.findViewById(R.id.icon_text);
		tv.setText(as.getActivity());
		
		ImageView iv = (ImageView)v.findViewById(R.id.icon_image);		
		int res = mContext.getResources()
				.getIdentifier("com.application.actify:drawable/"+as.getIcon(), 
						null, null);
		iv.setImageResource(res);
		iv.setContentDescription(as.getActivity());
		
		CheckBox cb = (CheckBox) v.findViewById(R.id.icon_cb);
		cb.setOnCheckedChangeListener(null);
		if (as.isVisible()) {
			cb.setChecked(true);
		} else
			cb.setChecked(false);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				editor.remove("vis_"+as.getId()+"_"+userid);
				if (isChecked) {
					as.setVisible(true);										
				} else {					
					editor.remove("order_"+as.getId()+"_"+userid);					
					as.setVisible(false);
					as.setOrder(1000);
					editor.putInt("order_"+as.getId()+"_"+userid, as.getOrder());
				}				
				editor.putBoolean("vis_"+as.getId()+"_"+userid, as.isVisible());
				editor.commit();
				
				Actify.resetOrderActivitySettings();													
				for (ActivitySetting ast : Actify.activitySettings) {
					editor.remove("order_"+ast.getId()+"_"+userid);
					editor.putInt("order_"+ast.getId()+"_"+userid, ast.getOrder());
				}
				editor.commit();
			}
			
		});

		return v;
    }
 
}
