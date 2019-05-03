package com.ceedlive.ggesture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * 연속된 직선들을 n 방향으로 구간을 나누어
 * 360도 중에서 해당 구간으로 뻗어 나가는 직선을 인식하여 번호를 부여함으로써
 * 미리 정의된 연속된 번호들과 일치 하는가를 통해 사용자의 입력을 받아 들이는 방식
 */
public class DrawView extends View {

	static Context c;

	private Bitmap mViewBitmap;
	private int mWidth = 0;
	private int mHeight = 0;

	public DrawView(Context context){
		super(context);
		setFocusable(true);
		c = context;



		// 이미지 그리기
//		mViewBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		mViewBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_genesis_g);

		// 픽셀작업
//		int[] colors = createColors();
//
//		mViewBitmap.setPixels(colors, 0, mWidth, 0, 0, mWidth, mHeight);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		int winWidth = getWidth();
		int winHeight = getHeight();
		// 이미지 그리기
		canvas.drawColor(Color.WHITE);
//		canvas.drawBitmap(mViewBitmap, winWidth / 2 - mWidth, winHeight / 2 - mHeight, null);
		canvas.drawBitmap(mViewBitmap, 0, 0, null);
//		canvas.translate(0, mViewBitmap.getHeight());
		super.onDraw(canvas);
	}

	private int[] createColors(){
		// 픽셀값  넣을 배열을 만들고 읽어온 이미지 픽셀값을 넣는다
		int[] colors = null;
		mWidth = mViewBitmap.getWidth();
		mHeight = mViewBitmap.getHeight();

		colors = new int[mWidth * mHeight];
		mViewBitmap.getPixels(colors, 0, mWidth, 0, 0, mWidth, mHeight);
		return colors;
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

				String msg = ": " + touchedX +" / " + touchedY;

				Toast.makeText(c, "TEST:" + msg, Toast.LENGTH_SHORT).show();

				if ( touchedX < mViewBitmap.getWidth() && touchedY < mViewBitmap.getHeight() ) {
					int pixel = mViewBitmap.getPixel(touchedX, touchedY);

					int red = Color.red(pixel);
					int blue = Color.blue(pixel);
					int green = Color.green(pixel);

					int intColor = Color.rgb(red, blue, green);
					int parsedColor = Color.parseColor("#000000");

					Log.e("intColor", "" + intColor);

					String hexColor = String.format("#%06X", (0xFFFFFF & intColor));

					Log.e("red", "" + red);
					Log.e("blue", "" + blue);
					Log.e("green", "" + green);

					Log.e("intColor", "" + intColor);
					Log.e("hexColor", "" + hexColor);

					if ( intColor == parsedColor ) {
						Toast.makeText(c, "하얀색", Toast.LENGTH_SHORT).show();
					}

				}

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
