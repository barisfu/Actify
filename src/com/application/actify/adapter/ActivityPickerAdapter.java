package com.application.actify.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.application.actify.R;
import com.application.actify.core.Actify;
import com.application.actify.model.ActivitySetting;


public class ActivityPickerAdapter extends BaseAdapter {
    private Context mContext;
    private List<ActivitySetting> activitySettings;
 
    // Constructor
    public ActivityPickerAdapter(Context c){
        mContext = c;
        activitySettings = Actify.getVisibleActivitySettings();
    }
 
    @Override
    public int getCount() {
    	return activitySettings.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mContext.getResources()
				.getIdentifier("com.application.actify:drawable/"+activitySettings.get(position).getIcon(), 
						null, null);
    }
 
    @Override
    public long getItemId(int position) {
        return activitySettings.get(position).getId();
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	View v;
    	if(convertView==null){
    		LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.icon, null);	
		} else {
			v = convertView;
		}
    	ActivitySetting as = activitySettings.get(position);
		TextView tv = (TextView)v.findViewById(R.id.icon_text);
		tv.setText(as.getActivity());
		ImageView iv = (ImageView)v.findViewById(R.id.icon_image);
		int res = mContext.getResources()
				.getIdentifier("com.application.actify:drawable/"+as.getIcon(), 
						null, null);
		iv.setImageResource(res);
		iv.setContentDescription(as.getActivity());

		return v;
    }
 
}
