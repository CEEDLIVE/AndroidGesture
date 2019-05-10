package com.ceedlive.ggesture.test_view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.ceedlive.ggesture.R;

/**
 * 연속된 직선들을 n 방향으로 구간을 나누어
 * 360도 중에서 해당 구간으로 뻗어 나가는 직선을 인식하여 번호를 부여함으로써
 * 미리 정의된 연속된 번호들과 일치 하는가를 통해 사용자의 입력을 받아 들이는 방식
 */
public class DrawView4 extends View {

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



	public DrawView4(Context context){
		super(context);
		setFocusable(true);


		WindowManager wm = (WindowManager)context.getSystemService( Context.WINDOW_SERVICE );
		Display display = wm.getDefaultDisplay();
		sw = display.getWidth();

		mViewBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_genesis_g);
		// 얘 뒤져보면 byte[] 에서 bitmap생성하는 것도 있심

		int[] colors = createColors();

//		mViewBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
//		mViewBitmap.setPixels(colors, 0, mWidth, 0, 0, mWidth, mHeight);



		mWidth = mViewBitmap.getWidth();
		mHeight = mViewBitmap.getHeight();

//		c = mViewBitmap.getWidth() / 2;
//		X = sw / 2; // 초기 X값을 구한다. (가운데)


//		mViewBitmap = bitmap;
		c = context;

		// 이미지 그리기
//		mViewBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);


		// 픽셀작업
//		mColors = createColors();
//		int[] colors = mColors;


//		mViewBitmap = Bitmap.createBitmap(colors, 0, mST, mWidth, mHeight, Bitmap.Config.ARGB_8888);
//		mPaint = new Paint();
//		mPaint.setDither(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {




		float canvasRate = (float) canvas.getWidth() / canvas.getHeight();
		float bitmapRate = (float) mViewBitmap.getWidth() / mViewBitmap.getHeight();


		float width, height;        // drawn width & height
		float xStart, yStart;       // start point (left top)

		// calculation process to fit bitmap in canvas
		if (canvasRate < bitmapRate) { // canvas is vertically long
			width  = canvas.getWidth();
			height = width / bitmapRate;
			xStart = 0;
			yStart = (canvas.getHeight() - height) / 2;
		} else { // canvas is horizontally wide
			height = canvas.getHeight();
			width  = height * bitmapRate;
			yStart = 0;
			xStart = (canvas.getWidth() - width) / 2;
		}

		// source(on bitmap) → destination(on canvas)
		rectSrc  = new Rect(0, 0, mViewBitmap.getWidth(), mViewBitmap.getHeight());
		rectDest = new Rect((int) xStart, (int) yStart,
				(int) (xStart + width), (int) (yStart + height));


		mPaint = new Paint();

		mWinWidth = getWidth();
		mWinHeight = getHeight();

		mImgWidth = mViewBitmap.getWidth();
		mImgHeight = mViewBitmap.getHeight();

		Log.e("onDraw", "mWinWidth: " + mWinWidth);
		Log.e("onDraw", "mWinHeight: " + mWinHeight);
		Log.e("onDraw", "mImgWidth: " + mImgWidth);
		Log.e("onDraw", "mImgHeight: " + mImgHeight);

		mLeft = mWinWidth - mImgWidth;
		mTop = mWinHeight - mImgHeight;

		mLeft = mLeft / 2;
		mTop = mTop / 2;

		Log.e("onDraw", "mLeft: " + mLeft);
		Log.e("onDraw", "mTop: " + mTop);






		// 이미지 그리기
//		canvas.drawBitmap(mViewBitmap, rectSrc, rectDest, mPaint);




//		canvas.drawBitmap(
//				mViewBitmap, // 출력할 bitmap
//				new Rect(0,0, mImgWidth, mImgHeight),   // 출력할 bitmap의 지정된 영역을 (sub bitmap)
//				new Rect(mLeft, mTop, mImgWidth, mImgHeight),  // 이 영역에 출력한다. (화면을 벗어나면 clipping됨)
//				null);

		canvas.drawColor(Color.WHITE);
//		canvas.drawBitmap(mViewBitmap, winWidth / 2 - mWidth, winHeight / 2 - mHeight, null);
		canvas.drawBitmap(mViewBitmap, mLeft, mTop, null);
//		canvas.drawBitmap(mViewBitmap, 0, 0, mPaint);
//		canvas.translate(mLeft, mTop);
//		canvas.translate(0, mImgHeight);


		canvas.save();

//		mViewBitmap.recycle();

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
				int touchCount = event.getPointerCount();

				String msg = ": " + touchedX +" / " + touchedY;

				Log.e("ACTION_DOWN", msg);
				Log.e("ACTION_DOWN", "touchCount: " + touchCount + "");

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

					Log.e("intColor", "" + intColor);
					Log.e("hexColor", "" + hexColor);

//					if ( intColor == parsedColor ) {
//						Toast.makeText(c, "하얀색", Toast.LENGTH_SHORT).show();
//					}

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
