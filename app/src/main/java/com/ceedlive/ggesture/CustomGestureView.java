package com.ceedlive.ggesture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

/**
 * 연속된 직선들을 n 방향으로 구간을 나누어
 * 360도 중에서 해당 구간으로 뻗어 나가는 직선을 인식하여 번호를 부여함으로써
 * 미리 정의된 연속된 번호들과 일치 하는가를 통해 사용자의 입력을 받아 들이는 방식
 */
public class CustomGestureView extends AppCompatImageView {

	private Paint mPaint;

	static final double sPi = 3.14159265358979;
	static final float sRtd = 57.29577951f;
	static final float sSectionNum = 32; // 방향성 개수 (연속된 직선들을 n 방향으로 구간을 나눔)
	static final double sRoundMinAngle = 2 * sPi * 11/12;

    private ArrayList<Vertex> arVertex1; // 사용자가 터치한 직선
    private ArrayList<Vertex> arVertex2; // 1차 보간된 선분
    private ArrayList<Vertex> arVertex3; // 최종 인식된 고정직선

	static final String HEX_BACKGROUND_TRANSPARENT = "#00000000";

	private Drawable mDrawable;
	private Bitmap mBitmap, mBitmapPointer;

	private Matrix mMatrix;

	private Context mContext;

	private int mWidth, mHeight;
	private int mPointerX, mPointerY;

	private int mDrawableWidth, mDrawableHeight;
	private int mViewWidth, mViewHeight;
	private int mRealImageWidth, mRealImageHeight;

	private Rect mRectStart;
	private Rect mRectIng;
	private Rect mRectEnd;

	private ColorFilter mColorFilter;

	private boolean mIsAuthorized = false;
	private boolean mIsPassRectIng = false;

	private boolean mDrawing = false;
	private GesturePointerListener mGesturePointerListener;


	public interface GesturePointerListener {
		void onButtonEnabled(boolean isEnabled);
	}
	public void gesturePointerCallback(GesturePointerListener listener) {
		this.mGesturePointerListener = listener;
	}


	/**
	 * Java Code 에서 뷰를 생성 할 때 호출되는 생성자
	 * @param context
	 */
	public CustomGestureView(Context context) {
		super(context);
		setFocusable(true);

		mContext = context;
		init_variable();
	}

	// you will need the constructor public MyView(Context context, AttributeSet attrs), otherwise you will get an Exception when Android tries to inflate your View.
	/**
	 * Java Code 에서 뷰를 생성 할 때 호출되는 생성자
	 * @param context
	 * @param attrs
	 */
	public CustomGestureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);

		mContext = context;
		init_variable();
	}

	// if you add your View from xml and also spcify the android:style attribute like : <com.mypack.MyView style="@styles/MyCustomStyle" />
	// you will also need the first constructor public MyView(Context context, AttributeSet attrs,int defStyle)
	/**
	 * Java Code 에서 뷰를 생성 할 때 호출되는 생성자
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CustomGestureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFocusable(true);

		mContext = context;
		init_variable();
	}


	public void init_variable() {
		mPaint = new Paint();// Avoid object allocations during draw/layout operations

		mMatrix = new Matrix();

		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_genesis_g);
		mBitmapPointer = BitmapFactory.decodeResource(getResources(), R.drawable.ic_car_24);

		setImageBitmap(mBitmap);
//		setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		mDrawable = getDrawable();

		mDrawableWidth = mDrawable.getIntrinsicWidth();
		mDrawableHeight = mDrawable.getIntrinsicHeight();

		mViewWidth = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
		mViewHeight = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom();

		mRealImageWidth = (int) ( (mViewWidth - mDrawableWidth) * 0.25f );
		// (I do not know why I had to put 0.25f instead of 0.5f,
		// but I think this issue is a consequence of the screen density)

		mRealImageHeight = (int) ( (mViewHeight - mDrawableHeight) * 0.25f );

		mWidth = mBitmap.getWidth();
		mHeight = mBitmap.getHeight();

		mPointerX = mWidth;
		mPointerY = mHeight;

		arVertex1 = new ArrayList<>();
		arVertex2 = new ArrayList<>();
		arVertex3 = new ArrayList<>();

		mRectStart = new Rect(mWidth - 300, 0, mWidth, 200); // 사각형 영역을 만든다
		mRectIng = new Rect(0, mHeight / 2, 300, (mHeight / 2) + 100); // 사각형 영역을 만든다
		mRectEnd = new Rect(mWidth - 300, mHeight / 2, mWidth, (mHeight / 2) + 100); // 사각형 영역을 만든다

		float density = getDotPerInch(mContext);
		float dp = getDotPoint(mWidth, density);

		mColorFilter = new PorterDuffColorFilter(ContextCompat.getColor(mContext, android.R.color.white), PorterDuff.Mode.SRC_IN);

		//
		Log.e("init_variable", "dp: " + dp);
		Log.e("init_variable", "density: " + density);

		Log.e("init_variable", "mDrawableWidth: " + mDrawableWidth);
		Log.e("init_variable", "mDrawableHeight: " + mDrawableHeight);

		Log.e("init_variable", "mViewWidth: " + mViewWidth);
		Log.e("init_variable", "mViewHeight: " + mViewHeight);

		Log.e("init_variable", "mRealImageWidth: " + mRealImageWidth);
		Log.e("init_variable", "mRealImageHeight: " + mRealImageHeight);

		Log.e("init_variable", "mWidth: " + mWidth);
		Log.e("init_variable", "mHeight: " + mHeight);

		Log.e("init_variable", "getPaddingLeft: " + this.getPaddingLeft());
		Log.e("init_variable", "getPaddingRight: " + this.getPaddingRight());

		Log.e("init_variable", "getPaddingTop: " + this.getPaddingTop());
		Log.e("init_variable", "getPaddingBottom: " + this.getPaddingBottom());
	}

	/**
	 * 뷰에 그림 그리는 행위를 담당하는 메소드
	 * @param canvas
	 */
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// drawing oval
//		mPaint.setColor(Color.argb(130, 255, 255, 255));
//		canvas.drawOval(mRectFEnd, mPaint);

		// drawing rect (for logic check)
		mPaint.setColor(Color.TRANSPARENT);
		canvas.drawRect(mRectStart, mPaint);
		canvas.drawRect(mRectIng, mPaint);
		canvas.drawRect(mRectEnd, mPaint);

		canvas.drawColor(Color.argb(50, 255, 0, 0));

//		mPaint.setColorFilter(mColorFilter);
		canvas.drawBitmap(mBitmapPointer, mPointerX, mPointerY, null);

		if (!mDrawing) {
			return;
		}

		mIsAuthorized = false;
		mGesturePointerListener.onButtonEnabled(false);

		mPaint.setFilterBitmap(true);

		float canvasRate = (float) getWidth() / getHeight();
		float bitmapRate = (float) mBitmap.getWidth() / mBitmap.getHeight();

		float width, height;	// drawn width & height
		float xStart, yStart;	// start point (left top)

		// calculation process to fit bitmap in canvas
		if (canvasRate < bitmapRate) { // canvas is vertically long
			width  = getWidth();
			height = width / bitmapRate;
			xStart = 0;
			yStart = (getHeight() - height) / 2;
		} else { // canvas is horizontally wide
			height = getHeight();
			width  = height * bitmapRate;
			yStart = 0;
			xStart = (getWidth() - width) / 2;
		}

		Log.e("onDraw", "calculation width: " + width);
		Log.e("onDraw", "calculation height: " + height);
		Log.e("onDraw", "calculation xStart: " + xStart);
		Log.e("onDraw", "calculation yStart: " + yStart);

		mPaint.setStrokeWidth(12);

		// 사용자가 터치한 직선
		for (int i=1; i<arVertex1.size(); i++) {
			if (i == 1) {
				//
				mPaint.setColor(Color.BLACK);
				mPaint.setAlpha(255);
			} else if ( i == arVertex1.size() - 1 ) {
				//
				mPaint.setColor(Color.RED);
				mPaint.setAlpha(255);
			} else {
				//
//				mPaint.setColor(Color.BLUE);
				mPaint.setColor(Color.WHITE);
//				mPaint.setAlpha(100);
			}

			// 부드럽게 하기 위해서 원을 추가
			canvas.drawCircle(arVertex1.get(i-1).x, arVertex1.get(i-1).y, 3, mPaint);
			canvas.drawLine(arVertex1.get(i-1).x, arVertex1.get(i-1).y, arVertex1.get(i).x, arVertex1.get(i).y, mPaint);
			canvas.drawCircle(arVertex1.get(i).x, arVertex1.get(i).y, 3, mPaint);
		}

		mPaint.setStrokeWidth(6);

		// 1차 보간된 선분
		for (int i=1; i<arVertex2.size(); i++) {
			// 부드럽게 하기 위해서 원을 추가
			float x1 = arVertex2.get(i-1).x;
			float y1 = arVertex2.get(i-1).y;
			float x2 = arVertex2.get(i).x;
			float y2 = arVertex2.get(i).y;
			float movePos = 25.f;
			x1 += movePos;
			y1 += movePos;
			x2 += movePos;
			y2 += movePos;
			mPaint.setColor(Color.GREEN);
			mPaint.setAlpha(120);
			canvas.drawLine(x1, y1, x2, y2, mPaint);
			mPaint.setAlpha(250);
			canvas.drawCircle(x2, y2, 3, mPaint);
			mPaint.setAlpha(250);
			canvas.drawCircle(x1, y1, 3, mPaint);
		}

		// 최종 인식된 고정직선
		for (int i=0; i<arVertex3.size(); i++) {
			// 부드럽게 하기 위해서 원을 추가
			mPaint.setAlpha(250);
			float x1 = arVertex3.get(i).x;
			float y1 = arVertex3.get(i).y;
			float x2 = x1 + arVertex3.get(i).length * (float) Math.cos(arVertex3.get(i).radian);
			float y2 = y1 + (arVertex3.get(i).length * (float) Math.sin(arVertex3.get(i).radian));
			float movePos = 50.f;
			x1 += movePos;
			y1 += movePos;
			x2 += movePos;
			y2 += movePos;

			mPaint.setColor(Color.GRAY);

			canvas.drawLine(x1, y1, x2, y2, mPaint);
			mPaint.setColor(Color.RED);

			canvas.drawCircle(x2, y2, 3, mPaint);
			mPaint.setColor(Color.BLACK);

			canvas.drawCircle(x1, y1, 3, mPaint);
		}

	}

	/**
	 * 터치 이벤트를 처리하는 콜백 메소드
	 * @param event
	 * @return
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// 터치한 곳의 좌표 읽어오기
		switch ( event.getAction() ) {
			// 처음 눌렸을 때
			case MotionEvent.ACTION_DOWN: {
				int touchCount = event.getPointerCount();

				mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();

//				Matrix inverse = new Matrix();
//				this.getImageMatrix().invert(inverse);
				this.getImageMatrix().invert(mMatrix);
				float[] touchPoint = new float[] {event.getX(), event.getY()};
//				inverse.mapPoints(touchPoint);
				mMatrix.mapPoints(touchPoint);
				int touchedX = (int) touchPoint[0];
				int touchedY = (int) touchPoint[1];

				// single touch
				if ( touchCount == 1 ) {

					mDrawing = true;

					if ( touchedX >= 0 && touchedX < mBitmap.getWidth()
							&& touchedY >= 0 && touchedY < mBitmap.getHeight() ) {
						int pixel = mBitmap.getPixel(touchedX, touchedY);
						int alpha = Color.alpha(pixel);
						int intColor = getRgbIntColor(pixel);

						String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
						String addAlpha = addAlpha(hexColor, alpha);

						Log.e("ACTION_DOWN", "touchedX: " + touchedX);
						Log.e("ACTION_DOWN", "touchedY: " + touchedY);
						Log.e("ACTION_DOWN", "intColor: " + intColor);
						Log.e("ACTION_DOWN", "hexColor: " + hexColor);
						Log.e("ACTION_DOWN", "addAlpha: " + addAlpha);
						Log.e("ACTION_DOWN", "====================");

						if ( !mRectStart.contains(touchedX, touchedY) ) {
//							initPointer(mImageViewPointer, mWidth - 50, 50);
							Log.e("ACTION_DOWN", "!!! contains: ");
//							mGesturePointerListener.onPointerInit(mWidth - 50, 50, this);
							mPointerX = mWidth;
							mPointerY = mHeight;

							arVertex1.clear();
							arVertex2.clear();
							arVertex3.clear();
							invalidate();// 뷰를 갱신
							return false;
						}
						Log.e("ACTION_DOWN", "contains: ");

						if ( HEX_BACKGROUND_TRANSPARENT.equals(addAlpha) ) {
//							initPointer(mImageViewPointer, mWidth - 50, 50);
//							mGesturePointerListener.onPointerInit(mWidth - 50, 50, this);

							mPointerX = mWidth;
							mPointerY = mHeight;

							arVertex1.clear();
							arVertex2.clear();
							arVertex3.clear();
							invalidate();// 뷰를 갱신
							return false;
						}

						arVertex1.clear();
						arVertex2.clear();
						arVertex3.clear();
						arVertex1.add( new Vertex( touchedX, touchedY ) );
					}
				}

				return true;

			} // end case
			// 누르고 움직였을 때
			case MotionEvent.ACTION_MOVE: {
				int touchCount = event.getPointerCount();

				mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();

//				Matrix inverse = new Matrix();
//				this.getImageMatrix().invert(inverse);
				this.getImageMatrix().invert(mMatrix);
				float[] touchPoint = new float[] {event.getX(), event.getY()};
//				inverse.mapPoints(touchPoint);
				mMatrix.mapPoints(touchPoint);
				int touchedX = (int) touchPoint[0];
				int touchedY = (int) touchPoint[1];

				Log.e("ACTION_MOVE", "touchedX: " + touchedX + "");
				Log.e("ACTION_MOVE", "touchedY: " + touchedY + "");
				Log.e("ACTION_MOVE", "touchCount: " + touchCount + "");

				if ( touchedX >= 0 && touchedX < mBitmap.getWidth()
						&& touchedY >= 0 && touchedY < mBitmap.getHeight() ) {
					int pixel = mBitmap.getPixel(touchedX, touchedY);
					int alpha = Color.alpha(pixel);
					int intColor = getRgbIntColor(pixel);

					String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
					String addAlpha = addAlpha(hexColor, alpha);

					Log.e("ACTION_MOVE", "intColor: " + intColor);
					Log.e("ACTION_MOVE", "hexColor: " + hexColor);
					Log.e("ACTION_MOVE", "addAlpha: " + addAlpha);
					Log.e("ACTION_MOVE", "====================");

					if ( mRectIng.contains(touchedX, touchedY) ) {
						mIsPassRectIng = true;
					}

					if ( HEX_BACKGROUND_TRANSPARENT.equals(addAlpha) ) {
//						initPointer(mImageViewPointer, mWidth - 50, 50);
//						mGesturePointerListener.onPointerInit(mWidth - 50, 50, this);

						mPointerX = mWidth;
						mPointerY = mHeight;

						arVertex1.clear();
						arVertex2.clear();
						arVertex3.clear();
						invalidate();// 뷰를 갱신

						return false;
					}

//					mGesturePointerListener.onPointerMove( (int) event.getX(), (int) event.getY(), this );

					mPointerX = touchedX - 50;
					mPointerY = touchedY - 50;
					arVertex1.add( new Vertex( event.getX(), event.getY() ) );
					invalidate();// 뷰를 갱신

				} else {
//					initPointer(mImageViewPointer, mWidth - 50, 50);
//					mGesturePointerListener.onPointerInit(mWidth - 50, 50, this);

					mPointerX = mWidth;
					mPointerY = mHeight;

					arVertex1.clear();
					arVertex2.clear();
					arVertex3.clear();

					invalidate();// 뷰를 갱신

					return false;
				}

				break;
			} // end case
			// 누른걸 땠을 때
			case MotionEvent.ACTION_UP: {
				mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();

//				Matrix inverse = new Matrix();
//				this.getImageMatrix().invert(inverse);
				this.getImageMatrix().invert(mMatrix);
				float[] touchPoint = new float[] {event.getX(), event.getY()};
//				inverse.mapPoints(touchPoint);
				mMatrix.mapPoints(touchPoint);
				int touchedX = (int) touchPoint[0];
				int touchedY = (int) touchPoint[1];

				if ( touchedX >= 0 && touchedX < mBitmap.getWidth()
						&& touchedY >= 0 && touchedY < mBitmap.getHeight() ) {
					int pixel = mBitmap.getPixel(touchedX, touchedY);
					int alpha = Color.alpha(pixel);
					int intColor = getRgbIntColor(pixel);

					String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
					String addAlpha = addAlpha(hexColor, alpha);

					Log.e("ACTION_UP", "intColor: " + intColor);
					Log.e("ACTION_UP", "hexColor: " + hexColor);
					Log.e("ACTION_UP", "addAlpha: " + addAlpha);
					Log.e("ACTION_UP", "====================");

					if (!mIsPassRectIng) {
						Log.e("ACTION_UP", "mIsPassRectIng 통과 XXX");
//						mGesturePointerListener.onPointerInit(mWidth - 50, 50);
//						arVertex1.clear();
//						arVertex2.clear();
//						arVertex3.clear();
//						invalidate();// 뷰를 갱신
//						return false;
						Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
					} else {
						Log.e("ACTION_UP", "mIsPassRectIng 통과 O");
					}

					if ( !mRectEnd.contains(touchedX, touchedY) ) {
//						mGesturePointerListener.onPointerInit(mWidth - 50, 50, this);

						mPointerX = mWidth;
						mPointerY = mHeight;

						arVertex1.clear();
						arVertex2.clear();
						arVertex3.clear();
						invalidate();// 뷰를 갱신

						Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
						return false;
					}
//
					if ( HEX_BACKGROUND_TRANSPARENT.equals(addAlpha) ) {
//						mGesturePointerListener.onPointerInit(mWidth - 50, 50, this);

						mPointerX = mWidth;
						mPointerY = mHeight;

						arVertex1.clear();
						arVertex2.clear();
						arVertex3.clear();
						invalidate();// 뷰를 갱신

						Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
						return false;
					}

					double section = 2 * sPi / sSectionNum;
					float allAngle = 0, allAngle1 = 0, allLength = 0;
					boolean allAngleReset = true;

					arVertex2.add(arVertex1.get(0));
					for (int i=1; i<arVertex1.size(); i+=1) {
						float x2, y2;
						x2 = arVertex1.get(i).x - arVertex1.get(i-1).x;
						y2 = arVertex1.get(i-1).y - arVertex1.get(i).y;

						// 각도 구하기
						float radian = getAngle(x2, y2);

						// 거리 구하기
						float length = (float) Math.sqrt( Math.pow(x2, 2.f) + Math.pow(y2, 2.f) );
						// Math.sqrt: 루트 근사값 구하기
						// Math.pow: 제곱 함수

						// 각도로 구역구하기
						double tempang = ( radian + (section / 2) ) % (2 * sPi);
						int sec = (int) (tempang / section);

						arVertex1.get(i).radian = radian;
						arVertex1.get(i).length = length;
						arVertex1.get(i).section = sec;

						// 이전 직선과의 각도차
						if (!allAngleReset) {
							double AngGap = arVertex1.get(i-1).radian - arVertex1.get(i).radian;
							if (AngGap > sPi) {
								AngGap -= 2 * sPi;
							} else if (AngGap < -sPi) {
								AngGap += 2 * sPi;
							}
							allAngle += AngGap;
							allAngle1 += AngGap;
						} else {
							allAngleReset = false;
						}

						allLength += length;
						Log.e("test", i +"번라인  구간  : "+sec+ "  각도 : " + (int)(radian*sRtd) +
								" 길이합 : " + allLength+ " 각도차합 : " + (int)(allAngle*sRtd) + "    " + (int)(allAngle1*sRtd));

						if (allAngle > section * 3/2 || allAngle < -section * 3/2 ) {
							Log.e("test", i + "번째" +
									" 변곡점 각도 : "+(int)(allAngle*sRtd)+
									" 총 길이는 " + allLength);

							allAngleReset = false;
							allAngle = 0;
							arVertex2.add(arVertex1.get(i));
						}
					} // end for each

					arVertex2.add( arVertex1.get(arVertex1.size() - 1) );

					Log.e("test","=========> 총각도 : "+ (int)(allAngle*sRtd));

					if (allAngle1 > sRoundMinAngle) {
						int round = (int) ( allAngle1 / (2 * sPi) );
						if ( allAngle1 % (2 * sPi) > sRoundMinAngle ) {
							round++;
						}
//						Toast.makeText(this.getContext(), "원(반시계방향) "+ round + "바퀴" , Toast.LENGTH_SHORT).show();
						Log.e("ACTION_UP", "원(반시계방향) "+ round + "바퀴");

						// TODO BUSINESS LOGIC
						mIsAuthorized = true;
						mDrawing = false;

						mGesturePointerListener.onButtonEnabled(true);
						Toast.makeText(mContext, "성공", Toast.LENGTH_SHORT).show();

						return false;
					} else if (-allAngle1 > sRoundMinAngle) {
						int round = (int) ( -allAngle1 / (2 * sPi) );
						if ( -allAngle1 % (2 * sPi) > sRoundMinAngle ) {
							round++;
						}
//						Toast.makeText(this.getContext(), "원(시계방향) "+ round + "바퀴 " , Toast.LENGTH_SHORT).show();
						Log.e("ACTION_UP", "원(시계방향) "+ round + "바퀴");

						// TODO BUSINESS LOGIC
						mIsAuthorized = true;
						mDrawing = false;

						mGesturePointerListener.onButtonEnabled(true);
						Toast.makeText(mContext, "성공", Toast.LENGTH_SHORT).show();

						return false;
					}

					double AllmoveAngle = 0;
					for (int i=1; i < arVertex2.size(); i+=1) {
						float x2, y2;
						x2 = arVertex2.get(i).x - arVertex2.get(i-1).x;
						y2 = arVertex2.get(i-1).y - arVertex2.get(i).y;

						float length = (float) Math.sqrt( Math.pow(x2, 2.f) + Math.pow(y2, 2.f) );
						if ( length < (allLength / arVertex2.size() / 2) ) {
							Log.e("test","2단계   ==> 전체 길이 : "+ allLength / arVertex2.size() + " 이부분길이 : "+length);
							continue;
						}
						// 각도 구하기
						double radian = getAngle(x2, y2);

						// 첫번째 직선으로 보정
						radian += AllmoveAngle;
						// 매칭되는 가장 가까운 직선각 구하기 22.5 도 회전
						double tempang = (radian + (section / 2)) % (2 * sPi);

						double moveAngle = tempang % section;
						moveAngle = (moveAngle < (section / 2) ? (section / 2) - moveAngle : (section / 2) - moveAngle);

						if (i == 1) { // 첫번째 직선에대해 보정.
							AllmoveAngle = moveAngle;
						}
						// 각도로 구역구하기
						int sec = (int) (tempang / section);

						Log.e("test","2단계   ==> "+ i +"번라인   구간  : "+sec+ "  각도 : " + (int) ( (radian+moveAngle) * sRtd) );

						// TODO BUSINESS LOGIC

						if (arVertex3.size() > 0) {
							if ( arVertex3.get(arVertex3.size()-1).section == sec ) {
								arVertex3.get(arVertex3.size()-1).length += length;
								continue;
							}
						}
						Vertex vertex = new Vertex(arVertex2.get(i-1).x, arVertex2.get(i-1).y);
						vertex.radian = (radian + moveAngle);
						vertex.length = length;
						vertex.section = sec;
						arVertex3.add(vertex);
					} // end for each

					Log.e("ACTION_UP", "arVertex1.size(): " + arVertex1.size() + "");
					Log.e("ACTION_UP", "arVertex2.size(): " + arVertex2.size() + "");
					Log.e("ACTION_UP", "arVertex3.size(): " + arVertex3.size() + "");
					Log.e("ACTION_UP", "AllmoveAngle: " + AllmoveAngle + "");
					Log.e("ACTION_UP", "allLength / arVertex2.size(): " + allLength / arVertex2.size());

					invalidate();

					// 텍스트 출력
					String str = "결과 : ";
					for (int i=0; i<arVertex3.size(); i++) {
						str = str + arVertex3.get(i).section;
						if ( i < arVertex3.size()-1 ) {
							str = str + " -> ";
						}
					}
					Toast.makeText(this.getContext(), str, Toast.LENGTH_SHORT).show();
					Log.e("test", "=================끝===============");

					boolean isValidTouch = arVertex1.size() > 100
							&& arVertex2.size() > 20
							&& arVertex3.size() > 10
//							&& Math.abs( (int) (allAngle * sRtd) ) > 4 // 총 각도
							&& allLength / arVertex2.size() > 80; // 전체 길이

					if (isValidTouch) {
						mIsAuthorized = true;
						mDrawing = false;

						mGesturePointerListener.onButtonEnabled(true);
						Toast.makeText(mContext, "성공", Toast.LENGTH_SHORT).show();
					}

				} else {
//					initPointer(mImageViewPointer, mWidth - 50, 50);
//					mGesturePointerListener.onPointerInit(mWidth - 50, 50, this);

					mPointerX = mWidth;
					mPointerY = mHeight;

					arVertex1.clear();
					arVertex2.clear();
					arVertex3.clear();
					invalidate();// 뷰를 갱신

					return false;
				}

				// TODO BUSINESS LOGIC
				Log.e("ACTION_UP", "인증성공여부: " + mIsAuthorized);
				break;

			} // end case
		} // end switch

		// true 를 반환하여 더 이상의 이벤트 처리가 이루어지지 않도록 완료한다.
		return true;
	}

    /**
     *
	 * @param target
     * @param x
     * @param y
	 */
	private void initPointer(ImageView target, int x, int y) {
		target.setX(x);
		target.setY(y);
	}

	/**
	 *
	 * @param pixel
	 * @return
	 */
	private int getRgbIntColor(int pixel) {
		int red = Color.red(pixel);
		int blue = Color.blue(pixel);
		int green = Color.green(pixel);
		return Color.rgb(red, blue, green);
	}
	
	/**
     * x = 0 직선에 대한 점의 각도를 계산한다
	 * @param x2
     * @param y2
     * @return
     */
	private float getAngle(float x2, float y2) {
		// 기준선문
		float x1 = 1.f, y1 = 0.f;
		// 0으로 나누는거 방지
		if (x2 == x1) {
			x2 *= 2;
			y2 *= 2;
		}
		float radian = -(float) Math.atan( (y2-y1) / (x2-x1) );
		
		// 180도
		if ( x2 < 0 && y2 ==0 ) {
			radian -= sPi;
		}
		
		// 사분면별 각도 조정
		if ( y2 < y1 && x2 > x1 ) {
			// blank
		} else if( (y2 < y1 && x2 < x1) || (y2 > y1 && x2 < x1) ) {
			radian += sPi;
		} else {
			radian += 2 * sPi;
		}
		return radian;
	}

	/**
	 * 꼭지점
	 */
	public class Vertex {
		Vertex(float ax, float ay){
	    	x = ax;
	    	y = ay;
	    }

		float x;
		float y;
		double radian;
		float length;
		int section;
	}

	/**
	 * 리스케일된 비트맵 이미지 객체 얻기
	 * @return
	 */
	private Bitmap getScaledBitmap(int resourceId) {
		try {
			// 화면 크기 구하기
			DisplayMetrics metrics = new DisplayMetrics();
			WindowManager windowManager = (WindowManager) mContext.getSystemService( Context.WINDOW_SERVICE );
			Display display = windowManager.getDefaultDisplay();
			display.getMetrics(metrics);

//		int displayWidth = display.getWidth();
//		int displayHeight = display.getHeight();
			int displayWidth = metrics.widthPixels;
			int displayHeight = metrics.heightPixels;

			// 리사이즈할 이미지 크기 구하기
			BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inPreferredConfig = Bitmap.Config.RGB_565;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(getResources(), resourceId, options);

			// 화면 크기에 가장 근접하는 이미지의 리스케일 사이즈를 구한다.
			float widthScale = options.outWidth / displayWidth;
			float heightScale = options.outHeight / displayHeight;
			float scale = widthScale > heightScale ? widthScale : heightScale;

			if (scale >= 8) {
				options.inSampleSize = 8;
			} else if (scale >= 4) {
				options.inSampleSize = 4;
			} else if (scale >= 2) {
				options.inSampleSize = 2;
			} else {
				options.inSampleSize = 1;
			}
			options.inJustDecodeBounds = false;

			return BitmapFactory.decodeResource(getResources(), resourceId, options);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 알파값이 더해진 hexadecimal color
	 * @param originalColor color, without alpha
	 * @param alpha         from 0.0 to 1.0
	 * @return
	 */
	public static String addAlpha(String originalColor, double alpha) {
		long alphaFixed = Math.round(alpha * 255);
		String alphaHex = Long.toHexString(alphaFixed);
		if (alphaHex.length() == 1) {
			alphaHex = "0" + alphaHex;
		}
		originalColor = originalColor.replace("#", "#" + alphaHex);
		return originalColor;
	}

	/**
	 * Project position on ImageView to position on Bitmap
	 * return the color on the position
	 * @param iv
	 * @param bm
	 * @param x
	 * @param y
	 * @return
	 */
	private int getProjectedColor(ImageView iv, Bitmap bm, int x, int y) {
		if ( x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight() ) {
			// outside ImageView
//			return color.background_light;
			return 0;
		}

		int projectedX = (int) ( (double) x * ( (double) bm.getWidth() / (double) iv.getWidth() ) );
		int projectedY = (int) ( (double) y * ( (double) bm.getHeight() / (double) iv.getHeight() ) );

		Log.e("getProjectedColor", x + ":" + y + "/" + iv.getWidth() + " : " + iv.getHeight() + "\n" +
				projectedX + " : " + projectedY + "/" + bm.getWidth() + " : " + bm.getHeight()
		);

		return bm.getPixel(projectedX, projectedY);
	}

//	float dpi = context.getResources().getDisplayMetrics().density;
//	To convert dp to px
//	float px = dp * dpi;
//	and to convert px to dp
//	float dp = px/dpi;

	/**
	 * get dpi: dot per inch
	 * @param context
	 * @return
	 */
	private float getDotPerInch(Context context) {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getSystemService( Context.WINDOW_SERVICE );
		Display display = windowManager.getDefaultDisplay();
		display.getMetrics(metrics);
		return metrics.density;
	}

	/**
	 *
	 * @param pixel
	 * @param dpi
	 * @return
	 */
	private float getDotPoint(float pixel, float dpi) {
		float dp;
		try {
			dp = pixel / dpi;
		} catch (ArithmeticException e) {
			return -1;
		}
		return dp;
	}

}
