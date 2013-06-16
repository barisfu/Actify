package com.application.actify.view;

import java.util.List;

import com.application.actify.R;
import com.application.actify.view.component.BlockData;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class DensityView extends View {
	
	static float PADDING = 40;
	static float ORIGIN_X = 30;
	static float ORIGIN_Y = 30;

	static float STROKE = 1;
	static float STROKE_TEXT = 2;
	
	static float DAYS_PER_WEEK = 7;
	static float MINS_PER_DAY = 1440;
	float width;
	float height;
	float perDay;
	float perMin;
	
	List<BlockData> blockData;
	
    Paint paint = new Paint();

    public DensityView(Context context, List<BlockData> blockData) {
        super(context);  
        this.blockData = blockData;
    }

    @Override
    public void onDraw(Canvas canvas) {
    	width = getMeasuredWidth()-PADDING;
        height = getMeasuredHeight()-PADDING;    
        perDay = width / DAYS_PER_WEEK;
        perMin = height / MINS_PER_DAY;
        drawBlocks(canvas);
        drawBackground(canvas);
    }
    
    private void drawBackground(Canvas canvas) {       	
        
    	float startX = ORIGIN_X, startY = ORIGIN_Y, stopX = ORIGIN_X, stopY = height + ORIGIN_Y;        
       
        for (int i=0; i<= DAYS_PER_WEEK; i++) {    
        	paint.setStrokeWidth(STROKE);
        	paint.setColor(getContext().getResources().getColor(R.color.separatorColor));
    		canvas.drawLine(startX, startY, stopX, stopY, paint);
    		if (i < DAYS_PER_WEEK) {
    			paint.setStrokeWidth(STROKE_TEXT);
    			paint.setColor(Color.BLACK);
    			canvas.drawText(dayString(i), startX + (perDay/2) - 15, ORIGIN_Y - 10, paint);
    		}
    		startX += perDay;
    		stopX += perDay;
    	}
        float yTop = ORIGIN_Y + height - (MINS_PER_DAY * perMin);
        canvas.drawLine(ORIGIN_X, yTop, stopX-perDay, yTop, paint);
        canvas.drawLine(ORIGIN_X, stopY, stopX-perDay, stopY, paint);
        
        canvas.drawLine(ORIGIN_X, height + ORIGIN_Y - (height * 0.75f), stopX-perDay, height + ORIGIN_Y - (height * 0.75f), paint);
        canvas.drawLine(ORIGIN_X, height + ORIGIN_Y - (height * 0.5f), stopX-perDay, height + ORIGIN_Y - (height * 0.5f), paint);
        canvas.drawLine(ORIGIN_X, height + ORIGIN_Y - (height * 0.25f), stopX-perDay, height + ORIGIN_Y - (height * 0.25f), paint);
        
        paint.setStrokeWidth(STROKE_TEXT);
		paint.setColor(Color.BLACK);
        canvas.drawText("00h", 0, height + ORIGIN_Y, paint);
        canvas.drawText("24h", 0, ORIGIN_Y, paint);
        canvas.drawText("18h", 0, height + ORIGIN_Y - (height * 0.75f), paint);
        canvas.drawText("12h", 0, height + ORIGIN_Y - (height * 0.5f), paint);
        canvas.drawText("06h", 0, height + ORIGIN_Y - (height * 0.25f), paint);
    }
    
    private void drawBlocks(Canvas canvas) {
    	for (BlockData block : blockData) {
    		drawBlock(canvas, block);
    	}
    }

    private void drawBlock(Canvas canvas, BlockData block) {
    	float xLeft = ORIGIN_X + (block.getX() * perDay);
    	float xRight = ORIGIN_X + ((block.getX()+1) * perDay);
    	float yTop = ORIGIN_Y + height + (block.getyStart() * perMin) ;
    	float yBottom = ORIGIN_Y + height + (block.getyEnd() * perMin);
    	paint.setStrokeWidth(0);
    	paint.setColor(block.getColor());    	
    	canvas.drawRect(xLeft, yTop, xRight, yBottom, paint);
    }
    
    private String dayString (int i) {
    	String str = "";
    	switch(i) {
    	case 0: str = "Mon"; break;
    	case 1: str = "Tue"; break;
    	case 2: str = "Wed"; break;
    	case 3: str = "Thu"; break;
    	case 4: str = "Fri"; break;    	
    	case 5: str = "Sat"; break;
    	case 6: str = "Sun"; break;
    	}
    	return str;
    }
}
