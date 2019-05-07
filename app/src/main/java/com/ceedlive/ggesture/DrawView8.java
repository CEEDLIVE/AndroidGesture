package com.ceedlive.ggesture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

/**
 * 연속된 직선들을 n 방향으로 구간을 나누어
 * 360도 중에서 해당 구간으로 뻗어 나가는 직선을 인식하여 번호를 부여함으로써
 * 미리 정의된 연속된 번호들과 일치 하는가를 통해 사용자의 입력을 받아 들이는 방식
 */
public class DrawView8 extends AppCompatImageView {

	static Context c;

	private Bitmap mViewBitmap;
	private Paint mPaint;

	private int mWidth = 0;
	private int mHeight = 0;

	private int mLeft = 0;
	private int mTop = 0;

	private int mWinWidth = 0;
	private int mWinHeight = 0;

	private int mImgWidth = 0;
	private int mImgHeight = 0;

	private int[] mColors;

	private int mST = 0;

	private int sw;


	private Rect rectSrc;
	private Rect rectDest;

	private Drawable mDrawable;

	private int color = Color.TRANSPARENT;

	public DrawView8(Context context) {
		super(context);
		c = context;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mDrawable = ContextCompat.getDrawable(c, R.drawable.logo_genesis_g);
		Bitmap bitmap = ((BitmapDrawable) mDrawable).getBitmap();

		// bmp is your Bitmap object
		int imgHeight = bitmap.getHeight();
		int imgWidth = bitmap.getWidth();
		int containerHeight = getHeight();
		int containerWidth = getWidth();
		boolean ch2cw = containerHeight > containerWidth;
		float h2w = (float) imgHeight / (float) imgWidth;
		float newContainerHeight, newContainerWidth;

		if (h2w > 1) {
			// height is greater than width
			if (ch2cw) {
				newContainerWidth = (float) containerWidth;
				newContainerHeight = newContainerWidth * h2w;
			} else {
				newContainerHeight = (float) containerHeight;
				newContainerWidth = newContainerHeight / h2w;
			}
		} else {
			// width is greater than height
			if (ch2cw) {
				newContainerWidth = (float) containerWidth;
				newContainerHeight = newContainerWidth / h2w;
			} else {
				newContainerWidth = (float) containerHeight;
				newContainerHeight = newContainerWidth * h2w;
			}
		}

		Bitmap copy = Bitmap.createScaledBitmap(bitmap, (int) newContainerWidth, (int) newContainerHeight, false);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			setBackground(new BitmapDrawable(getResources(), copy));
		} else {
			setBackgroundDrawable(new BitmapDrawable(getResources(), copy));
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			{
				// TODO BUSINESS LOGIC
				// 터치한 곳의 좌표 읽어오기
				int touchedX = (int) event.getX();
				int touchedY = (int) event.getY();
				int touchCount = event.getPointerCount();

				String msg = ": " + touchedX +" / " + touchedY;

				if (touchCount == 1) {
					if (mDrawable instanceof BitmapDrawable) {
						Bitmap bitmap = ((BitmapDrawable) mDrawable).getBitmap();
						if ( touchedX < bitmap.getWidth() && touchedY < bitmap.getHeight() ) {
							int pixel = bitmap.getPixel(touchedX, touchedY);

							int red = Color.red(pixel);
							int blue = Color.blue(pixel);
							int green = Color.green(pixel);

							int intColor = Color.rgb(red, blue, green);
							String hexColor = String.format("#%06X", (0xFFFFFF & intColor));

							Log.e("ACTION_DOWN", "hexColor: " + hexColor + "");
						}
					}
				}

				Log.e("ACTION_DOWN", msg);
				Log.e("ACTION_DOWN", "touchCount: " + touchCount + "");

				Toast.makeText(c, "TEST:" + msg, Toast.LENGTH_SHORT).show();

				break;
			} // end case
			case MotionEvent.ACTION_MOVE:
			{
				// TODO BUSINESS LOGIC


				break;
			} // end case
			case MotionEvent.ACTION_UP:
			{
				// TODO BUSINESS LOGIC


				break;
			} // end case
		} // end switch

		return true;
	}

}
