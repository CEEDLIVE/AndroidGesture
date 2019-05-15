package com.ceedlive.ggesture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.ceedlive.ggesture.util.GraphicsUtil;

import java.util.ArrayList;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Quick Start
 * - 커스텀뷰 생성
 * - 상하좌우 여백이 없는 G 모양의 이미지 리소스 추가
 * - 화면의 정중앙에 위치하도록 레이아웃 구현
 * - G 모양 내부를 한붓 그리기로 통과하는 패스 추가
 * - 패스 위를 움직이는 이미지 리소스 추가
 * - 커스텀뷰 터치 이벤트 핸들러 추가
 * - 끝
 *
 *
 * 로직 1)
 * 연속된 직선들을 n 방향으로 구간을 나누고
 * 360도 중에서 해당 구간으로 뻗어 나가는 직선을 인식하여 번호를 부여함
 * 부여된 번호가 미리 정의된 연속된 번호들과 일치 하는지 확인
 * 확인된 값을 근거로 사용자의 입력을 처리하는 로직 구현
 *
 *
 * 로직 2)
 * 문자 모양의 G 내부를 한붓 그리기로 통과하는 패스 생성
 * 커스텀뷰 터치 이벤트 발생 시 조건에 맞게 패스 위를 통과하며 움직이는 비트맵 이미지 구현
 *
 */
public class CustomGestureView extends AppCompatImageView {

	private Paint mPaint;

//	static final double sPi = 3.14159265358979;
	static final double sPi = Math.PI;
	static final float sRtd = 57.29577951f;
	static final float sSectionNum = 32; // 방향성 개수 (연속된 직선들을 n 방향으로 구간을 나눔)
	static final double sRoundMinAngle = 2 * sPi * 11/12;

    private ArrayList<Vertex> arVertex1; // 사용자가 터치한 직선
    private ArrayList<Vertex> arVertex2; // 1차 보간된 선분
    private ArrayList<Vertex> arVertex3; // 최종 인식된 고정직선

	static final String HEX_BACKGROUND_TRANSPARENT = "#00000000";

	private Bitmap mBitmap, mPathBitmap;

	private Matrix mMatrix;

	private Context mContext;

	private int mWidth, mHeight;
	private int mPointerX, mPointerY;

	private Rect mRectStart, mRectIng1, mRectIng2, mRectEnd;

	private boolean mIsAuthorized = false;
	private boolean mIsPassRectIng1 = false;
	private boolean mIsPassRectIng2 = false;
	private boolean mDrawing = false;

	private float mDpi;
	private int mMaxResolution = 1280;


	private Path mAnimPath;
	private PathMeasure mPathMeasure;
	private float mPathLength;
	private Paint mAnimPaint;

	// 시작점
	private float[] mPointStart;
	private float[] mPointEnd;


	// 끝점


	private Bitmap bm;
	private int bm_offsetX, bm_offsetY;

	private float mDistanceEachStep;   //distance each step
	private float distance;  //distance moved

	private float[] pos;
	private float[] tan;

	private Matrix mPathMatrix;

	private GesturePointerListener mGesturePointerListener;


	public interface GesturePointerListener {
		void onButtonEnabled(boolean isEnabled);
	}
	public void gesturePointerCallback(GesturePointerListener listener) {
		this.mGesturePointerListener = listener;
	}

	// Constructor

	/**
	 * Java Code 에서 뷰를 생성 할 때 호출되는 생성자
	 * @param context
	 */
	public CustomGestureView(Context context) {
		super(context);
		setFocusable(true);

		mContext = context;
		initialize();
	}

	// you will need the constructor public CustomGestureView(Context context, AttributeSet attrs),
	// otherwise you will get an Exception when Android tries to inflate your View.
	/**
	 * Java Code 에서 뷰를 생성 할 때 호출되는 생성자
	 * @param context
	 * @param attrs
	 */
	public CustomGestureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);

		mContext = context;
		initialize();
	}

	// if you add your View from xml and also spcify the android:style attribute like : <com.mypack.MyView style="@styles/MyCustomStyle" />
	// you will also need the first constructor public CustomGestureView(Context context, AttributeSet attrs,int defStyle)
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
		initialize();
	}

	// variables

	public void initialize() {
		mPaint = new Paint();// Avoid object allocations during draw/layout operations
		mAnimPaint = new Paint();

		mDpi = GraphicsUtil.getDotPerInch(mContext);

		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.google_icon_nopad_circle);
		mBitmap = GraphicsUtil.getResizedBitmapByResolution(mBitmap, mMaxResolution);

		mPathBitmap = GraphicsUtil.getBitmapFromVectorDrawable(mContext, R.drawable.ic_happy);
		mPathBitmap = GraphicsUtil.getResizedBitmapByScale(mPathBitmap, 256, 256);

		mMatrix = new Matrix();

		arVertex1 = new ArrayList<>();
		arVertex2 = new ArrayList<>();
		arVertex3 = new ArrayList<>();

		mWidth = mBitmap.getWidth();
		mHeight = mBitmap.getHeight();

		setImageBitmap(mBitmap);

		mPointStart = new float[2];
		mPointEnd = new float[2];

		mPointStart[0] = (float) (mWidth * 0.75);
		mPointStart[1] = (float) (mHeight * 0.25);

		mPointEnd[0] = mWidth / 2;
		mPointEnd[1] = mHeight / 2;

		Log.e("init_variable", "mPointStart[0]: " + mPointStart[0]);
		Log.e("init_variable", "mPointStart[1]: " + mPointStart[1]);
		Log.e("init_variable", "mPointEnd[0]: " + mPointEnd[0]);
		Log.e("init_variable", "mPointEnd[1]: " + mPointEnd[1]);

		// ========================================

		mRectStart = new Rect(mWidth - 550, 0, mWidth - 200, 350); // 사각형 영역을 만든다
		mRectIng1 = new Rect(0, (mHeight / 2) - 200, 400, (mHeight / 2) + 200); // 사각형 영역을 만든다
		mRectIng2 = new Rect(mWidth - 400, (mHeight / 2) - 200, mWidth, (mHeight / 2) + 200); // 사각형 영역을 만든다
		mRectEnd = new Rect(mWidth - 800, (mHeight / 2) - 200, mWidth - 400, (mHeight / 2) + 200); // 사각형 영역을 만든다

		mAnimPath = new Path();
		mPathMeasure = new PathMeasure();

//		initPointerCoordinate();



		bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_hyundai_64);
		bm_offsetX = mPathBitmap.getWidth() / 2;
		bm_offsetY = mPathBitmap.getHeight() / 2;

		initPath();
		distance();
	}

	private void initPath() {
		mDistanceEachStep = 10; // ?
		distance = 0;
		pos = new float[2];
		tan = new float[2];

		mPathMatrix = new Matrix();

		RectF oval = new RectF();
		int weight = 150;// 가변
		oval.set(0 + weight, 0 + weight, mWidth - weight, mHeight - weight);

		mAnimPath.arcTo(oval, -45f, -315f);
		mAnimPath.lineTo(mWidth / 2, mHeight / 2);

		mAnimPath.close();

		mPathMeasure = new PathMeasure(mAnimPath, false);
		mPathLength = mPathMeasure.getLength();

		Log.e("initPath", "mPathLength: " + mPathLength);
	}

	// onDraw

	/**
	 * 뷰에 그림 그리는 행위를 담당하는 메소드
	 * @param canvas
	 */
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		mAnimPaint.setColor(Color.TRANSPARENT);
		mAnimPaint.setStrokeWidth(5f);
		mAnimPaint.setStyle(Paint.Style.STROKE);

		canvas.drawPath(mAnimPath, mAnimPaint);

		// 시작점, 중간점, 끝점을 담당하는 투명 사각형 그리기
		mPaint.setColor(Color.TRANSPARENT);
		canvas.drawRect(mRectStart, mPaint);
		canvas.drawRect(mRectIng1, mPaint);
		canvas.drawRect(mRectIng2, mPaint);
		canvas.drawRect(mRectEnd, mPaint);

		// 터치를 따라 이동하는 포인터 이미지 (old)
//		canvas.drawBitmap(mPathBitmap, mPointerX, mPointerY, null);

		// 터치를 따라 이동하는 포인터 이미지 (new)
		if (distance < mPathLength) {
			canvas.drawBitmap(mPathBitmap, mPathMatrix, null);
		}

		// 터치 이벤트가 시작점, 중간점, 끝점을 적절하게 지나는지 검증
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
				mPaint.setColor(Color.WHITE);
			}

			// FIXME
			mPaint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리

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

			// FIXME
			mPaint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리

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

			// FIXME
			mPaint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리

			canvas.drawLine(x1, y1, x2, y2, mPaint);
			mPaint.setColor(Color.RED);

			// FIXME
			mPaint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리

			canvas.drawCircle(x2, y2, 3, mPaint);
			mPaint.setColor(Color.BLACK);

			// FIXME
			mPaint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리

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

				this.getImageMatrix().invert(mMatrix);
				float[] touchPoint = new float[] {event.getX(), event.getY()};
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
						String addAlpha = GraphicsUtil.getHexaDecimalColorAddedAlpha(hexColor, alpha);

						distance();

						if ( !mRectStart.contains(touchedX, touchedY) ) {
//							initPointerCoordinate();

							arVertex1.clear();
							arVertex2.clear();
							arVertex3.clear();

							invalidate();// 뷰를 갱신
							return false;
						}

						if ( HEX_BACKGROUND_TRANSPARENT.equals(addAlpha) ) {
//							initPointerCoordinate();

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
				mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();

				this.getImageMatrix().invert(mMatrix);
				float[] touchPoint = new float[] {event.getX(), event.getY()};
				mMatrix.mapPoints(touchPoint);
				int touchedX = (int) touchPoint[0];
				int touchedY = (int) touchPoint[1];

				if ( touchedX >= 0 && touchedX < mBitmap.getWidth()
						&& touchedY >= 0 && touchedY < mBitmap.getHeight() ) {
					int pixel = mBitmap.getPixel(touchedX, touchedY);
					int alpha = Color.alpha(pixel);
					int intColor = getRgbIntColor(pixel);

					String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
					String addAlpha = GraphicsUtil.getHexaDecimalColorAddedAlpha(hexColor, alpha);

					if ( mRectIng1.contains(touchedX, touchedY) ) {
						mIsPassRectIng1 = true;
					}

					if ( mRectIng2.contains(touchedX, touchedY) ) {
						mIsPassRectIng2 = true;
					}

					if ( HEX_BACKGROUND_TRANSPARENT.equals(addAlpha) ) {
//						initPointerCoordinate();

						arVertex1.clear();
						arVertex2.clear();
						arVertex3.clear();

						distance();

						invalidate();// 뷰를 갱신

						return false;
					}


					// ==================== TEST START

					if (distance < mPathLength) {
						mPathMeasure.getPosTan(distance, pos, tan);

						mPathMatrix.reset();
						float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
						mPathMatrix.postRotate(degrees, bm_offsetX, bm_offsetY);
						mPathMatrix.postTranslate(pos[0] - bm_offsetX, pos[1] - bm_offsetY);

						distance += mDistanceEachStep;
					} else {
						distance = 0;
					}

					// ==================== TEST END


					mPointerX = touchedX - 100;
					mPointerY = touchedY - 100;
					arVertex1.add( new Vertex( event.getX(), event.getY() ) );
					invalidate();// 뷰를 갱신

				} else {
//					initPointerCoordinate();

					arVertex1.clear();
					arVertex2.clear();
					arVertex3.clear();

					distance();

					invalidate();// 뷰를 갱신

					return false;
				}

				break;
			} // end case
			// 누르고 있던 것을 떼었을 때
			case MotionEvent.ACTION_UP: {
				mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();

				this.getImageMatrix().invert(mMatrix);
				float[] touchPoint = new float[] {event.getX(), event.getY()};
				mMatrix.mapPoints(touchPoint);
				int touchedX = (int) touchPoint[0];
				int touchedY = (int) touchPoint[1];

				if ( touchedX >= 0 && touchedX < mBitmap.getWidth()
						&& touchedY >= 0 && touchedY < mBitmap.getHeight() ) {
					int pixel = mBitmap.getPixel(touchedX, touchedY);
					int alpha = Color.alpha(pixel);
					int intColor = getRgbIntColor(pixel);

					String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
					String addAlpha = GraphicsUtil.getHexaDecimalColorAddedAlpha(hexColor, alpha);

					if ( !mIsPassRectIng1 || !mIsPassRectIng2 ) {
//						initPointerCoordinate();

						arVertex1.clear();
						arVertex2.clear();
						arVertex3.clear();

						distance();

						invalidate();// 뷰를 갱신
						Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
						return false;
					}

					if ( !mRectEnd.contains(touchedX, touchedY) ) {
//						initPointerCoordinate();

						arVertex1.clear();
						arVertex2.clear();
						arVertex3.clear();

						distance();

						invalidate();// 뷰를 갱신
						Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
						return false;
					}
//
					if ( HEX_BACKGROUND_TRANSPARENT.equals(addAlpha) ) {
//						initPointerCoordinate();

						arVertex1.clear();
						arVertex2.clear();
						arVertex3.clear();

						distance();

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

						if (i == 1) { // 첫번째 직선에 대해 보정.
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
//					Toast.makeText(this.getContext(), str, Toast.LENGTH_SHORT).show();
					Log.e("test", str);
					Log.e("test", "=================끝===============");

					boolean isValidTouch = arVertex1.size() > 85
							&& arVertex2.size() > 20
							&& arVertex3.size() > 10
//							&& Math.abs( (int) (allAngle * sRtd) ) > 4 // 총 각도
							&& allLength / arVertex2.size() > 80; // 전체 길이

					if (isValidTouch) {
						mIsAuthorized = true;
						mDrawing = false;

						mGesturePointerListener.onButtonEnabled(true);
						Toast.makeText(mContext, "성공", Toast.LENGTH_SHORT).show();

						return false;
					} else {
//						initPointerCoordinate();

						arVertex1.clear();
						arVertex2.clear();
						arVertex3.clear();

						distance();

						invalidate();// 뷰를 갱신
					}

					Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();

				} else {
//					initPointerCoordinate();

					arVertex1.clear();
					arVertex2.clear();
					arVertex3.clear();

					distance();

					invalidate();// 뷰를 갱신

					Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
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
	 * 화면 가로/세로 전환 시 호출, 뷰 최초 진입(?) 시 호출
	 * @param w
	 * @param h
	 * @param oldw
	 * @param oldh
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		Log.e("onSizeChanged", String.format("width: %d, height: %d, oldWidth: %d, oldHeight: %d", w, h, oldw, oldh));
	}

	/**
	 *
	 */
	private void distance() {
		distance = 0;
		mPathMeasure.getPosTan(distance, pos, tan);

		mPathMatrix.reset();
		float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
		mPathMatrix.postRotate(degrees, bm_offsetX, bm_offsetY);
		mPathMatrix.postTranslate(pos[0] - bm_offsetX, pos[1] - bm_offsetY);
	}

	/**
	 *
	 */
	private void initPointerCoordinate() {
		mPointerX = mWidth - 500;
		mPointerY = 50;
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

}
