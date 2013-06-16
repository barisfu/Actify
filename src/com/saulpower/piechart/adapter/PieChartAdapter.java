package com.saulpower.piechart.adapter;

import java.util.List;

import android.content.Context;

import com.saulpower.piechart.extra.UiUtils;
import com.saulpower.piechart.views.PieChartView;
import com.saulpower.piechart.views.PieSliceDrawable;

public class PieChartAdapter extends BasePieChartAdapter {
    
    public final String TAG = this.getClass().getSimpleName();

	private Context mContext;
	private List<Float> mObjects;
	private List<Integer> mColors;
	
	public PieChartAdapter(Context context, List<Float> objects) {
		init(context, objects);
	}
	
	public PieChartAdapter(Context context, List<Float> objects, List<Integer> colors) {
		init(context, objects, colors);
	}
	
	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public Object getItem(int position) {
		return mObjects.get(position);
	}
	
	private void init(Context context, List<Float> objects) {
		
		mContext = context;
		mObjects = objects;
	}
	
	private void init(Context context, List<Float> objects, List<Integer> colors) {
		
		mContext = context;
		mObjects = objects;
		mColors = colors;
	}

	@Override
	public float getPercent(int position) {
		Float percent = (Float) getItem(position);
		
		return percent;
	}

	@Override
	public PieSliceDrawable getSlice(PieChartView parent, PieSliceDrawable convertDrawable, int position, float offset) {

		PieSliceDrawable sliceView = convertDrawable;
		
		if (sliceView == null) {
			sliceView = new PieSliceDrawable(parent, mContext);
		}
		if (mColors == null)
			sliceView.setSliceColor(UiUtils.getRandomColor(mContext, position));
		else
			sliceView.setSliceColor(mColors.get(position));
		sliceView.setPercent(mObjects.get(position));
		sliceView.setDegreeOffset(offset);
		
		return sliceView;
	}
}
