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
package ca.laplanete.mobile.pageddragdropgrid;

import com.application.actify.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;

public class DeleteDropZoneView extends View {
	private Context context;

	private Paint textPaintStraight;
	private Paint textPaintRed;
	private Paint bitmapPaint;
	private Paint bitmapPaintRed;
	private boolean straight = true;

	private Rect bounds;
	private String btnText;


	public DeleteDropZoneView(Context context, String text) {
		super(context);
		this.context = context;

		bounds = new Rect();

		textPaintStraight = createTextPaint();
		textPaintStraight.setColor(Color.WHITE);

		textPaintRed = createTextPaint();
		textPaintRed.setColor(Color.RED);

		bitmapPaint = createBaseBitmapPaint();

		bitmapPaintRed = createBaseBitmapPaint();
		ColorFilter filter = new LightingColorFilter(Color.RED, 1);
		bitmapPaintRed.setColorFilter(filter);

		setBackgroundColor(Color.BLUE);
		getBackground().setAlpha(200);
		
		btnText = text;
	}

	private Paint createTextPaint() {
		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setStyle(Style.FILL);
		textPaint.setTextAlign(Paint.Align.CENTER);
		return textPaint;
	}

	private Paint createBaseBitmapPaint() {
		Paint bitmapPaint = new Paint();
		bitmapPaint.setAntiAlias(true);
		bitmapPaint.setFilterBitmap(true);
		bitmapPaint.setDither(true);
		return bitmapPaint;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int measuredHeight = getMeasuredHeight();
		int measuredWidth = getMeasuredWidth();		

		textPaintStraight.getTextBounds(btnText, 0, btnText.length(), bounds);

		int proportion = 3 * measuredHeight / 4;
		if (straight) {
			textPaintStraight.setTextSize(proportion);
			canvas.drawText(btnText, (measuredWidth / 2) + 5, measuredHeight - ((measuredHeight - bounds.height()) / 2) , textPaintStraight);
		} else {
			textPaintRed.setTextSize(proportion);
			canvas.drawText(btnText, (measuredWidth / 2) + 5, measuredHeight - ((measuredHeight - bounds.height()) / 2) , textPaintRed);
		}
	}

	public void highlight() {
		straight = false;
		invalidate();
	}

	public void smother() {
		straight = true;
		invalidate();
	}
}
