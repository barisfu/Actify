/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.application.actify.adapter;

/**
 * @author Chitra H. Ayuningtyas
 */

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
 
    /**
     * Constructor
     * @param c application context
     */
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
