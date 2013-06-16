/**
 * Copyright 2012 
 * 
 * Nicolas Desjardins  
 * https://github.com/mrKlar
 * 
 * Facilite solutions
 * http://www.facilitesolutions.com/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.application.actify.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ca.laplanete.mobile.pageddragdropgrid.PagedDragDropGrid;
import ca.laplanete.mobile.pageddragdropgrid.PagedDragDropGridAdapter;

import com.application.actify.core.Actify;
import com.application.actify.model.ActivitySetting;
import com.application.actify.view.component.OrderItem;
import com.application.actify.view.component.OrderPage;

public class SettingsOrderAdapter implements PagedDragDropGridAdapter {

	private Context context;
	private PagedDragDropGrid gridview;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private int userid;
	
	List<OrderPage> pages = new ArrayList<OrderPage>();
	
	public SettingsOrderAdapter(Context context, PagedDragDropGrid gridview) {
		super();
		this.context = context;
		this.gridview = gridview;
		settings = context.getSharedPreferences(Actify.PREFS_NAME, 0);
		editor = settings.edit();
		userid = settings.getInt("userid", -1);
		
		List<OrderItem> visibleItems = new ArrayList<OrderItem>();
		
		List<ActivitySetting> list = Actify.getVisibleActivitySettings();
		for (ActivitySetting as : list) {
			OrderItem item = new OrderItem(as.getId(), as.getOrder(), 
					as.getActivity(), "com.application.actify:drawable/"+as.getIcon());
			visibleItems.add(item);
		}
		Collections.sort(visibleItems);			
		
		OrderPage page = new OrderPage();
		page.setItems(visibleItems);
		pages.add(page);		
	}

	@Override
	public int pageCount() {
		return pages.size();
	}

	private List<OrderItem> itemsInPage(int page) {
		if (pages.size() > page) {
			return pages.get(page).getItems();
		}	
		return Collections.emptyList();
	}

    @Override
	public View view(int page, int index) {
		
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		ImageView iv = new ImageView(context);
		OrderItem item = getItem(page, index);
		iv.setImageResource(context.getResources().getIdentifier(item.getIcon(), null, null));		
		//iv.setPadding(15, 15, 15, 15);
		
		layout.addView(iv);
		
		TextView label = new TextView(context);
		label.setTag("text");
		label.setText(item.getName());	
		label.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		label.setMaxLines(3);
		label.setWidth(120);
		label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));

		layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				
		// only set selector on every other page for demo purposes
		// if you do not wish to use the selector functionality, simply disregard this code
		if(page % 2 == 0) {
    		layout.setClickable(true);
    		layout.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return gridview.onLongClick(v);
                }
    		});
		}

		layout.addView(label);
		return layout;
	}

	private OrderItem getItem(int page, int index) {
		List<OrderItem> items = itemsInPage(page);
		return items.get(index);
	}

	@Override
	public int rowCount() {
		return AUTOMATIC;
	}

	@Override
	public int columnCount() {
		return AUTOMATIC;
	}

	@Override
	public int itemCountInPage(int page) {
		return itemsInPage(page).size();
	}

	public void printLayout() {
		int i=0;
		for (OrderPage page : pages) {
			Log.d("Page", Integer.toString(i++));
			
			for (OrderItem item : page.getItems()) {
				Log.d("Item", Long.toString(item.getId()));
			}
		}
	}

	private OrderPage getPage(int pageIndex) {
		return pages.get(pageIndex);
	}

	@Override
	public void swapItems(int pageIndex, int itemIndexA, int itemIndexB) {	
		ActivitySetting A = Actify.findActivitySettingByOrder(itemIndexA);			
		ActivitySetting B = Actify.findActivitySettingByOrder(itemIndexB);
		if (A!=null && B!=null) {
			A.setOrder(itemIndexB);
			B.setOrder(itemIndexA);
			Actify.reorderActivitySettings();
			editor.remove("order_"+A.getId()+"_"+userid);
			editor.remove("order_"+B.getId()+"_"+userid);
			editor.putInt("order_"+A.getId()+"_"+userid, A.getOrder());
			editor.putInt("order_"+B.getId()+"_"+userid, B.getOrder());
			editor.commit();
			getPage(pageIndex).swapItems(itemIndexA, itemIndexB);
		}
	}

	@Override
	public void moveItemToPreviousPage(int pageIndex, int itemIndex) {
		int leftPageIndex = pageIndex-1;
		if (leftPageIndex >= 0) {
			OrderPage startpage = getPage(pageIndex);
			OrderPage landingPage = getPage(leftPageIndex);
			
			OrderItem item = startpage.removeItem(itemIndex);
			landingPage.addItem(item);	
		}	
	}

	@Override
	public void moveItemToNextPage(int pageIndex, int itemIndex) {
		int rightPageIndex = pageIndex+1;
		if (rightPageIndex < pageCount()) {
			OrderPage startpage = getPage(pageIndex);
			OrderPage landingPage = getPage(rightPageIndex);
			
			OrderItem item = startpage.removeItem(itemIndex);
			landingPage.addItem(item);			
		}	
	}

	@Override
	public void deleteItem(int pageIndex, int itemIndex) {
		getPage(pageIndex).deleteItem(itemIndex);
	}

    @Override
    public int deleteDropZoneLocation() {        
        return BOTTOM;
    }

    @Override
    public boolean showRemoveDropZone() {
        return false;
    }
	
}
