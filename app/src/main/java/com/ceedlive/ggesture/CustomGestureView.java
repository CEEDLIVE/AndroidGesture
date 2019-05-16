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
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.ceedlive.ggesture.util.CustomUtil;

import java.util.ArrayList;
import java.util.List;

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

	// 디버그 모드 여부: true 로 설정 시 미리 설정한 패스와 렉트 영역, 터치로 이동한 영역에 색상이 입혀져 표시됨
	static final boolean sIsDebugMode = true;

	private Paint mPaint;

	static final double sPi = Math.PI; // 파이, 3.14159265358979323846
	static final float sRtd = 57.29577951f;
	static final float sSectionNum = 32; // 방향성 개수 (연속된 직선들을 n 방향으로 구간을 나눔)
	static final double sRoundMinAngle = 2 * sPi * 11/12;

    private ArrayList<Vertex> arrVertex1; // 점의 연속: 사용자가 터치한 직선
    private ArrayList<Vertex> arrVertex2; // 점의 연속: 1차 보간된 선분
    private ArrayList<Vertex> arrVertex3; // 점의 연속: 최종 인식된 고정직선

	static final String HEX_BACKGROUND_TRANSPARENT = "#00000000";

	private Bitmap mGShapeBitmap, mPathBitmap;

	private Matrix mMatrix;

	private Context mContext;

	private int mWidth, mHeight;

	private Rect mRectStart, mRectIngNorth, mRectIngWest, mRectIngSouth, mRectIngEast, mRectEnd;
	private RectF mPathRectF;

	private boolean mIsAuthorized;
	private boolean mIsPassRectIngNorth, mIsPassRectIngWest, mIsPassRectIngSouth, mIsPassRectIngEast;
	private boolean mIsDrawing;
	private boolean mIsPathPointerPathThrough;// 패스 위를 움직이는 포인터가 끝점에 도달하였는지 체크

	static final int mMaxResolution = 1280;

	private Path mAnimPath;
	private PathMeasure mPathMeasure;
	private float mPathLength;
	private Paint mPathPaint;

	private int mPathBitmapOffsetX, mPathBitmapOffsetY;

	private float mDistanceEachStep; // distance each step
	private float mDistanceMoved; // distance moved

	private float[] mPathPos;
	private float[] mPathTan;

	private Matrix mPathMatrix;

	private int mPathRectFWeight;
	private float mPathDegrees;

	private int mMotionEventPointerCount;
	private int mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY;
	private int mGShapeCurrentPixel;
	private int mGShapeCurrentAlpha;
	private int mGShapeCurrentRgbIntColor;
	private String mGShapeCurrentHexColor;
	private String mGShapeCurrentHexColorAddedAlpha;

	private List<Rect> mValidationRectLIst;

	// 선분
	private double mSection = 2 * sPi / sSectionNum;
	private float mAllAngle = 0, allAngle1 = 0, allLength = 0;
	private boolean allAngleReset = true;


	private CheckForLongPress checkForLongPress;


	private GesturePointerListener mGesturePointerListener;

	public interface GesturePointerListener {
		void onButtonEnabled(boolean isEnabled);
	}
	public void gesturePointerCallback(GesturePointerListener listener) {
		this.mGesturePointerListener = listener;
	}

	// ====================
	// Constructor
	// ====================

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

	// ====================
	// variables
	// ====================

	public void initialize() {

		// init variables
		{
			mPaint = new Paint();// Avoid object allocations during draw/layout operations
			mPathPaint = new Paint();
			mMatrix = new Matrix();
			mPathMatrix = new Matrix();

			arrVertex1 = new ArrayList<>();
			arrVertex2 = new ArrayList<>();
			arrVertex3 = new ArrayList<>();

			mAnimPath = new Path();
			mPathMeasure = new PathMeasure();

			mPathRectF = new RectF();
		}

		// set a bitmap g shape
		{
			mGShapeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.google_icon_nopad_circle);
			mGShapeBitmap = CustomUtil.getResizedBitmapByResolution(mGShapeBitmap, mMaxResolution);
			mWidth = mGShapeBitmap.getWidth();
			mHeight = mGShapeBitmap.getHeight();
			setImageBitmap(mGShapeBitmap);
		}

		// set a bitmap path pointer
		{
			mPathBitmap = CustomUtil.getBitmapFromVectorDrawable(mContext, R.drawable.ic_happy);
			mPathBitmap = CustomUtil.getResizedBitmapByScale(mPathBitmap, 256, 256);
			mPathBitmapOffsetX = mPathBitmap.getWidth() / 2;
			mPathBitmapOffsetY = mPathBitmap.getHeight() / 2;
		}

		// set validation rect
		{
			// 시작점 (오른쪽 45도)
			int left_start = mWidth * 2/3;
			int top_start = 0;
			int right_start = mWidth;
			int bottom_start = (int) ( mHeight * (1 - Math.sqrt(2) / 2) );

			// 북
			int left_ing_north = mWidth * 1/3;
			int top_ing_north = 0;
			int right_ing_north = mWidth * 2/3;
			int bottom_ing_north = (int) ( mHeight * (1 - Math.sqrt(2) / 2) );

			// 서
			int left_ing_west = 0;
			int top_ing_west = mHeight * 1/3;
			int right_ing_west = mWidth * 1/3;
			int bottom_ing_west = mHeight * 2/3;

			// 동
			int left_ing_east = mWidth * 2/3;
			int top_ing_east = mHeight * 1/3;
			int right_ing_east = mWidth;
			int bottom_ing_east = mHeight * 2/3;

			// 남
			int left_ing_south = mWidth * 1/3;
			int top_ing_south = mHeight * 2/3;
			int right_ing_south = mWidth * 2/3;
			int bottom_ing_south = mHeight;

			// 끝점 (중앙)
			int left_end = mWidth * 1/3;
			int top_end = mHeight * 1/3;
			int right_end = mWidth * 2/3;
			int bottom_end = mHeight * 2/3;

//			mRectStart = new Rect(left, 0, mWidth - 200, 350); // 사각형 영역을 만든다
//			mRectIng1 = new Rect(0, (mHeight / 2) - 200, 400, (mHeight / 2) + 200); // 사각형 영역을 만든다
//			mRectIng2 = new Rect(mWidth - 400, (mHeight / 2) - 200, mWidth, (mHeight / 2) + 200); // 사각형 영역을 만든다
//			mRectEnd = new Rect(mWidth - 800, (mHeight / 2) - 200, mWidth - 400, (mHeight / 2) + 200); // 사각형 영역을 만든다

			mRectStart = new Rect(left_start, top_start, right_start, bottom_start); // 사각형 영역을 만든다
			mRectIngNorth = new Rect(left_ing_north, top_ing_north, right_ing_north, bottom_ing_north); // 사각형 영역을 만든다
			mRectIngWest = new Rect(left_ing_west, top_ing_west, right_ing_west, bottom_ing_west); // 사각형 영역을 만든다
			mRectIngSouth = new Rect(left_ing_south, top_ing_south, right_ing_south, bottom_ing_south); // 사각형 영역을 만든다
			mRectIngEast = new Rect(left_ing_east, top_ing_east, right_ing_east, bottom_ing_east); // 사각형 영역을 만든다
			mRectEnd = new Rect(left_end, top_end, right_end, bottom_end); // 사각형 영역을 만든다

			mValidationRectLIst = new ArrayList<>();

			mValidationRectLIst.add(mRectStart);
			mValidationRectLIst.add(mRectIngNorth);
			mValidationRectLIst.add(mRectIngWest);
			mValidationRectLIst.add(mRectIngSouth);
			mValidationRectLIst.add(mRectIngEast);
			mValidationRectLIst.add(mRectEnd);
		}

		// set path
		{
			mDistanceEachStep = 10; // 패스 위를 움직이는 이미지의 이동속도
			mDistanceMoved = 0;
			mPathPos = new float[2];
			mPathTan = new float[2];

			mPathRectFWeight = 128;// 가변
			mPathRectF.set(0 + mPathRectFWeight, 0 + mPathRectFWeight, mWidth - mPathRectFWeight, mHeight - mPathRectFWeight);
			mAnimPath.arcTo(mPathRectF, -45f, -315f);


			mAnimPath.lineTo(mWidth / 2, mHeight / 2);
			mAnimPath.close();

			mPathMeasure = new PathMeasure(mAnimPath, false);
			mPathLength = mPathMeasure.getLength();

			Log.e("롱터치 탐지 로직 진행", "mPathLength1: " + mPathLength);

			mPathLength = mPathLength - ( ( mWidth - (mPathRectFWeight * 2) ) / 2 ); //

			Log.e("롱터치 탐지 로직 진행", "mPathLength2: " + mPathLength);
			Log.e("롱터치 탐지 로직 진행", "mWidth / 2: " + (mWidth / 2) );
		}

		// set default flag
		{
			mIsAuthorized = false; // 접근 권한
			mIsPassRectIngNorth = false; // 첫 번째 영역 통과
			mIsPassRectIngWest = false; // 두 번째 영역 통과
			mIsPassRectIngSouth = false; // 두 번째 영역 통과
			mIsPassRectIngEast = false; // 두 번째 영역 통과

			mIsDrawing = false; // onDraw 메서드 전체 실행 여부
		}

		initMotionTouchEvent();


		checkForLongPress = new CheckForLongPress();

	} // end method initialize

	// onDraw

	/**
	 * 뷰에 그림 그리는 행위를 담당하는 메소드
	 * @param canvas
	 */
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 문자 모양의 G 내부를 한붓 그리기로 통과하는 패스 생성
		drawGShapePath(canvas, mPathPaint, mAnimPath);

		// 시작점, 중간점, 끝점을 포함하는 투명 사각형 영역 그리기
		drawValidationRect(canvas, mPaint, mValidationRectLIst);

		// 터치를 따라 이동하는 포인터 이미지 (new)
		if (mDistanceMoved < mPathLength) {
			canvas.drawBitmap(mPathBitmap, mPathMatrix, null);
		}

		// 터치 이벤트가 시작점, 중간점, 끝점을 적절하게 지나는지 체크하여 하단 코드 실행 여부 결정
		if (!mIsDrawing) {
			return;
		}

		mIsAuthorized = false; // 접근 권한 없음
		mGesturePointerListener.onButtonEnabled(false); // 버튼 비활성화

		mPaint.setFilterBitmap(true);
		mPaint.setStrokeWidth(12);

		// 사용자가 터치한 직선 렌더링
		drawUserTouchedLine(canvas, mPaint, arrVertex1);

		mPaint.setStrokeWidth(6);

		// 1차 보간된 선분 렌더링
		drawInterpolationLine(canvas, mPaint, arrVertex2);

		// 최종 인식된 고정직선 렌더링
		drawFinalRecognizedLine(canvas, mPaint, arrVertex3);

	} // end method onDraw

	/**
	 * 문자 모양의 G 내부를 한붓 그리기로 통과하는 패스 생성
	 * @param canvas
	 * @param paint
	 * @param path
	 */
	private void drawGShapePath(Canvas canvas, Paint paint, Path path) {
		if (sIsDebugMode) {
			paint.setColor(Color.RED);
		} else {
			paint.setColor(Color.TRANSPARENT);
		}

		paint.setStrokeWidth(5f);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawPath(path, paint);
	}

	/**
	 * 시작점, 중간점, 끝점을 포함하는 투명 사각형 영역 그리기
	 * @param canvas
	 * @param paint
	 * @param rectList
	 */
	private void drawValidationRect(Canvas canvas, Paint paint, List<Rect> rectList) {
		if (sIsDebugMode) {
			paint.setColor(Color.argb(100, 255, 0, 0));
		} else {
			paint.setColor(Color.TRANSPARENT);
		}

		for (Rect rect : rectList) {
			canvas.drawRect(rect, paint);
		}
	}

	/**
	 * 사용자가 터치한 직선
	 * @param canvas
	 * @param paint
	 * @param vertexArrayList
	 */
	private void drawUserTouchedLine(Canvas canvas, Paint paint, ArrayList<Vertex> vertexArrayList) {

		Log.e("drawUserTouchedLine", "vertexArrayList.size(): " + vertexArrayList.size());

		for (int i=1; i<vertexArrayList.size(); i++) {
			if (i == 1) {
				//
				paint.setColor(Color.BLACK);
				paint.setAlpha(255);
			} else if ( i == vertexArrayList.size() - 1 ) {
				//
				paint.setColor(Color.RED);
				paint.setAlpha(255);
			} else {
				//
				paint.setColor(Color.WHITE);
			}

			if (!sIsDebugMode) {
				paint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리 // FIXME
			}

			// 부드럽게 하기 위해서 원을 추가
			canvas.drawCircle(vertexArrayList.get(i-1).x, vertexArrayList.get(i-1).y, 3, paint);
			canvas.drawLine(vertexArrayList.get(i-1).x, vertexArrayList.get(i-1).y, vertexArrayList.get(i).x, vertexArrayList.get(i).y, paint);
			canvas.drawCircle(vertexArrayList.get(i).x, vertexArrayList.get(i).y, 3, paint);
		}
	}

	/**
	 * 1차 보간된 선분
	 * @param canvas
	 * @param paint
	 * @param vertexArrayList
	 *
	 * 보간공식: 변량의 아는 값을 이용해서 그 사이에 놓일 값을 근사치로 계산하는 공식.
	 */
	private void drawInterpolationLine(Canvas canvas, Paint paint, ArrayList<Vertex> vertexArrayList) {
		for (int i=1; i<vertexArrayList.size(); i++) {
			// 부드럽게 하기 위해서 원을 추가
			float x1 = vertexArrayList.get(i-1).x;
			float y1 = vertexArrayList.get(i-1).y;
			float x2 = vertexArrayList.get(i).x;
			float y2 = vertexArrayList.get(i).y;
			float movePos = 25.f;
			x1 += movePos;
			y1 += movePos;
			x2 += movePos;
			y2 += movePos;
			paint.setColor(Color.GREEN);

			if (!sIsDebugMode) {
				paint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리 // FIXME
			}

			paint.setAlpha(120);
			canvas.drawLine(x1, y1, x2, y2, paint);
			paint.setAlpha(250);
			canvas.drawCircle(x2, y2, 3, paint);
			paint.setAlpha(250);
			canvas.drawCircle(x1, y1, 3, paint);
		}
	}

	/**
	 * 최종 인식된 고정직선
	 * @param canvas
	 * @param paint
	 * @param vertexArrayList
	 */
	private void drawFinalRecognizedLine(Canvas canvas, Paint paint, ArrayList<Vertex> vertexArrayList) {
		for (int i=0; i<vertexArrayList.size(); i++) {
			// 부드럽게 하기 위해서 원을 추가
			paint.setAlpha(250);
			float x1 = vertexArrayList.get(i).x;
			float y1 = vertexArrayList.get(i).y;
			float x2 = x1 + vertexArrayList.get(i).length * (float) Math.cos(vertexArrayList.get(i).radian);
			float y2 = y1 + (vertexArrayList.get(i).length * (float) Math.sin(vertexArrayList.get(i).radian));
			float movePos = 50.f;
			x1 += movePos;
			y1 += movePos;
			x2 += movePos;
			y2 += movePos;

			paint.setColor(Color.GRAY);

			if (!sIsDebugMode) {
				paint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리 // FIXME
			}

			canvas.drawLine(x1, y1, x2, y2, paint);
			paint.setColor(Color.RED);

			if (!sIsDebugMode) {
				paint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리 // FIXME
			}

			canvas.drawCircle(x2, y2, 3, paint);
			paint.setColor(Color.BLACK);

			if (!sIsDebugMode) {
				paint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리 // FIXME
			}

			canvas.drawCircle(x1, y1, 3, paint);
		}
	}

	/**
	 * 터치 이벤트를 처리하는 콜백 메소드
	 *
	 * onTouch > onLongClick > onClick
	 *
	 * @param event
	 * @return
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 터치한 곳의 좌표 읽어오기
		switch ( event.getAction() ) {
			// 처음 눌렸을 때
			case MotionEvent.ACTION_DOWN: {
				handleTouchDown(event);
				break;
			} // end case
			// 누르고 움직였을 때
			case MotionEvent.ACTION_MOVE: {
				if (mIsDrawing) {
					handleTouchMove(event);
				}
				break;
			} // end case
			case MotionEvent.ACTION_CANCEL: {
				if (!checkForLongPress.mHasPerformedLongPress) {
					// This is a tap, so remove the long press check
					Log.e("ACTION_CANCEL", "This is a tap, so remove the long press check");
					checkForLongPress.removeLongPressCallback();
				}
				break;
			}
			// 누르고 있던 것을 떼었을 때
			case MotionEvent.ACTION_UP: {
				// 롱터치 탐지 로직 마무리
				if (!checkForLongPress.mHasPerformedLongPress) {
					// Long Click을 처리되지 않았으면 제거함.
					checkForLongPress.removeLongPressCallback();
					// Short Click 처리 루틴을 여기에 넣으면 됩니다.
					handleTouchUp(event);
				}
				break;
			} // end case
		} // end switch

		// true 를 반환하여 더 이상의 이벤트 처리가 이루어지지 않도록 완료한다.
		return true;
	} // end method onTouchEvent

	/**
	 * 화면 가로/세로 전환 시 호출, 뷰 최초 진입(?) 시 호출
	 * @param newWidth
	 * @param newHeight
	 * @param oldWidth
	 * @param oldHeight
	 */
	@Override
	protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
		super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
	} // end method onSizeChanged

	/**
	 * 처음 눌렸을 때 이벤트 핸들러
	 * @param event
	 * @return
	 */
	private boolean handleTouchDown(MotionEvent event) {
		mMotionEventPointerCount = event.getPointerCount();
		mGShapeBitmap = ((BitmapDrawable) getDrawable()).getBitmap();

		this.getImageMatrix().invert(mMatrix);
		float[] touchPoint = new float[] {event.getX(), event.getY()};
		mMatrix.mapPoints(touchPoint);
		mMotionEventCurrentTouchedX = (int) touchPoint[0];
		mMotionEventCurrentTouchedY = (int) touchPoint[1];

		// 싱글 터치
		if ( mMotionEventPointerCount == 1 ) {
			if ( mMotionEventCurrentTouchedX >= 0 && mMotionEventCurrentTouchedX < mGShapeBitmap.getWidth()
					&& mMotionEventCurrentTouchedY >= 0 && mMotionEventCurrentTouchedY < mGShapeBitmap.getHeight() ) {


				{
					// 롱터치 탐지 로직 시작
					checkForLongPress.mLastMotionX = mMotionEventCurrentTouchedX;
					checkForLongPress.mLastMotionY = mMotionEventCurrentTouchedY;// 시작 위치 저장
					checkForLongPress.mHasPerformedLongPress = false;
					checkForLongPress.postCheckForLongClick(0);// Long click message 설정
				}

				mGShapeCurrentPixel = mGShapeBitmap.getPixel(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY);
				mGShapeCurrentAlpha = Color.alpha(mGShapeCurrentPixel);
				mGShapeCurrentRgbIntColor = CustomUtil.getRgbIntColor(mGShapeCurrentPixel);
				mGShapeCurrentHexColor = CustomUtil.getHexColor(mGShapeCurrentRgbIntColor);
				mGShapeCurrentHexColorAddedAlpha = CustomUtil.getHexaDecimalColorAddedAlpha(mGShapeCurrentHexColor, mGShapeCurrentAlpha);

				// 터치이벤트 관련 세팅 초기화
				initMotionTouchEvent();

				// 시작점으로 지정한 영역(투명색)을 터치하지 않은 경우
				if ( !mRectStart.contains(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY) ) {
					Log.e("handleTouchDown", "시작점으로 지정한 영역(투명색)을 터치하지 않은 경우");
					invalidate();// 뷰를 갱신
					return false;
				}

				// G 모양 텍스트를 터치하지 않은 경우
				if ( HEX_BACKGROUND_TRANSPARENT.equals(mGShapeCurrentHexColorAddedAlpha) ) {
					Log.e("handleTouchDown", "G 모양 텍스트를 터치하지 않은 경우");
					invalidate();// 뷰를 갱신
					return false;
				}

				// 터치를 따라 이동하며 그려지는 시작점 생성 및 플래그 처리
				mIsDrawing = true;
				arrVertex1.add( new Vertex(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY) );

				// You should return true; in case MotionEvent.ACTION_DOWN:, so the MotionEvent.ACTION_UP will be handled.
				return true;
			}

			return false;
		}

		return false;
	}

	/**
	 * 누르고 움직였을 때 이벤트 핸들러
	 * @param event
	 * @return
	 */
	private boolean handleTouchMove(MotionEvent event) {
		mMotionEventPointerCount = event.getPointerCount();
		mGShapeBitmap = ((BitmapDrawable) getDrawable()).getBitmap();

		this.getImageMatrix().invert(mMatrix);
		float[] touchPoint = new float[] {event.getX(), event.getY()};
		mMatrix.mapPoints(touchPoint);
		mMotionEventCurrentTouchedX = (int) touchPoint[0];
		mMotionEventCurrentTouchedY = (int) touchPoint[1];

		// 싱글 터치
		if ( mMotionEventPointerCount == 1 ) {
			if ( mMotionEventCurrentTouchedX >= 0 && mMotionEventCurrentTouchedX < mGShapeBitmap.getWidth()
					&& mMotionEventCurrentTouchedY >= 0 && mMotionEventCurrentTouchedY < mGShapeBitmap.getHeight() ) {

				mGShapeCurrentPixel = mGShapeBitmap.getPixel(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY);
				mGShapeCurrentAlpha = Color.alpha(mGShapeCurrentPixel);
				mGShapeCurrentRgbIntColor = CustomUtil.getRgbIntColor(mGShapeCurrentPixel);
				mGShapeCurrentHexColor = CustomUtil.getHexColor(mGShapeCurrentRgbIntColor);
				mGShapeCurrentHexColorAddedAlpha = CustomUtil.getHexaDecimalColorAddedAlpha(mGShapeCurrentHexColor, mGShapeCurrentAlpha);

				if ( mRectIngNorth.contains(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY) ) {
					mIsPassRectIngNorth = true;
					Log.e("handleTouchMove", "북쪽 첫번째 지점 통과");// FIXME
				}

				if ( mRectIngWest.contains(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY) ) {
					mIsPassRectIngWest = true;
					Log.e("handleTouchMove", "서쪽 두번째 지점 통과");// FIXME
				}

				if ( mRectIngSouth.contains(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY) ) {
					mIsPassRectIngSouth = true;
					Log.e("handleTouchMove", "남쪽 세번째 지점 통과");// FIXME
				}

				if ( mRectIngEast.contains(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY) ) {
					mIsPassRectIngEast = true;
					Log.e("handleTouchMove", "동쪽 네번째 지점 통과");// FIXME
				}

				if ( mRectEnd.contains(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY) ) {
					Log.e("handleTouchMove", "중앙 마지막 지점 통과");// FIXME
				}

				if ( HEX_BACKGROUND_TRANSPARENT.equals(mGShapeCurrentHexColorAddedAlpha) ) {
					initMotionTouchEvent();// 초기화
					invalidate();// 뷰를 갱신
					return false;
				}

				final int deltaX = Math.abs((int) (checkForLongPress.mLastMotionX - mMotionEventCurrentTouchedX));
				final int deltaY = Math.abs((int) (checkForLongPress.mLastMotionY - mMotionEventCurrentTouchedY));
				{
					// ====================

					// 일정 범위 벗어나면 취소함
//					Log.e("롱터치 탐지 로직 진행", "checkForLongPress.mLastMotionX: " + checkForLongPress.mLastMotionX);
//					Log.e("롱터치 탐지 로직 진행", "checkForLongPress.mLastMotionY: " + checkForLongPress.mLastMotionY);
//					Log.e("롱터치 탐지 로직 진행", "checkForLongPress.mLastMotionX - mMotionEventCurrentTouchedX: " + (checkForLongPress.mLastMotionX - mMotionEventCurrentTouchedX) );
//					Log.e("롱터치 탐지 로직 진행", "checkForLongPress.mLastMotionY - mMotionEventCurrentTouchedY: " + (checkForLongPress.mLastMotionY - mMotionEventCurrentTouchedY) );
//					Log.e("롱터치 탐지 로직 진행", "deltaX: " + deltaX);
//					Log.e("롱터치 탐지 로직 진행", "deltaY: " + deltaY);
//					Log.e("롱터치 탐지 로직 진행", "x: " + x);
//					Log.e("롱터치 탐지 로직 진행", "y: " + y);
//					Log.e("롱터치 탐지 로직 진행", "checkForLongPress.mTouchSlop: " + checkForLongPress.mTouchSlop);
//					Log.e("롱터치 탐지 로직 진행", "mMotionEventCurrentTouchedX: " + mMotionEventCurrentTouchedX);
//					Log.e("롱터치 탐지 로직 진행", "mMotionEventCurrentTouchedY: " + mMotionEventCurrentTouchedY);
//					Log.e("롱터치 탐지 로직 진행", "mDistanceMoved: " + mDistanceMoved);
//					Log.e("롱터치 탐지 로직 진행", "mPathLength: " + mPathLength);
//					Log.e("롱터치 탐지 로직 진행", "deltaX + deltaY + mDistanceEachStep: " + (deltaX + deltaY + mDistanceEachStep) );
//					Log.e("롱터치 탐지 로직 진행", "mPathDegrees: " + mPathDegrees);
//					Log.e("롱터치 탐지 로직 진행", "mPathBitmapOffsetX: " + mPathBitmapOffsetX);
//					Log.e("롱터치 탐지 로직 진행", "mPathBitmapOffsetY: " + mPathBitmapOffsetY);
//					Log.e("롱터치 탐지 로직 진행", "==============================");

					// ====================

					// 일정 범위 벗어나면 취소함
					if (deltaX >= checkForLongPress.mTouchSlop || deltaY >= checkForLongPress.mTouchSlop) {
						if (!checkForLongPress.mHasPerformedLongPress) {
							// This is a tap, so remove the long press check
							checkForLongPress.removeLongPressCallback();
						}
					}
				}

				// ========================================
				// logic1

				if (deltaX + deltaY + mDistanceEachStep > mDistanceMoved) {
					Log.e("롱터치 탐지 로직 진행", "deltaX + deltaY + mDistanceEachStep: " + (deltaX + deltaY + mDistanceEachStep) );
					Log.e("롱터치 탐지 로직 진행", "==============================");
				}

				if ( mPathLength - mDistanceMoved < 32 ) {
					// 끝점에 다다른 경우 포인터 유지
					Log.e("롱터치 탐지 로직 진행", "유지");

					mIsPathPointerPathThrough = true;

				} else {
					if (mDistanceMoved < mPathLength) {
//						if (deltaX + deltaY + weight > mDistanceMoved) {
							mPathMeasure.getPosTan(mDistanceMoved, mPathPos, mPathTan);
							mPathMatrix.reset();
							mPathDegrees = (float) (Math.atan2(mPathTan[1], mPathTan[0]) * 180.0 / Math.PI);
							mPathMatrix.postRotate(mPathDegrees, mPathBitmapOffsetX, mPathBitmapOffsetY);
							mPathMatrix.postTranslate(mPathPos[0] - mPathBitmapOffsetX, mPathPos[1] - mPathBitmapOffsetY);
							mDistanceMoved += mDistanceEachStep;
//						}
					} else {
						mDistanceMoved = 0;
					}

				}

				arrVertex1.add( new Vertex(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY) );
				invalidate();// 뷰를 갱신

			} else {
				initMotionTouchEvent();// 초기화
				invalidate();// 뷰를 갱신
				return false;
			}
		}

		return true;
	}

	/**
	 * 누르고 있던 것을 떼었을 때 이벤트 핸들러
	 * @param event
	 * @return
	 */
	private boolean handleTouchUp(MotionEvent event) {
		mMotionEventPointerCount = event.getPointerCount();
		mGShapeBitmap = ((BitmapDrawable) getDrawable()).getBitmap();

		this.getImageMatrix().invert(mMatrix);
		float[] touchPoint = new float[] {event.getX(), event.getY()};
		mMatrix.mapPoints(touchPoint);
		mMotionEventCurrentTouchedX = (int) touchPoint[0];
		mMotionEventCurrentTouchedY = (int) touchPoint[1];

		// single touch
		if ( mMotionEventPointerCount == 1 ) {
			if ( mMotionEventCurrentTouchedX >= 0 && mMotionEventCurrentTouchedX < mGShapeBitmap.getWidth()
					&& mMotionEventCurrentTouchedY >= 0 && mMotionEventCurrentTouchedY < mGShapeBitmap.getHeight() ) {

				mGShapeCurrentPixel = mGShapeBitmap.getPixel(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY);
				mGShapeCurrentAlpha = Color.alpha(mGShapeCurrentPixel);
				mGShapeCurrentRgbIntColor = CustomUtil.getRgbIntColor(mGShapeCurrentPixel);
				mGShapeCurrentHexColor = CustomUtil.getHexColor(mGShapeCurrentRgbIntColor);
				mGShapeCurrentHexColorAddedAlpha = CustomUtil.getHexaDecimalColorAddedAlpha(mGShapeCurrentHexColor, mGShapeCurrentAlpha);

				if (!mIsPassRectIngNorth) {
					initMotionTouchEvent();// 초기화
					invalidate();// 뷰를 갱신
					Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
					return false;
				}

				if (!mIsPassRectIngWest) {
					initMotionTouchEvent();// 초기화
					invalidate();// 뷰를 갱신
					Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
					return false;
				}

				if (!mIsPassRectIngSouth) {
					initMotionTouchEvent();// 초기화
					invalidate();// 뷰를 갱신
					Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
					return false;
				}

				if (!mIsPassRectIngEast) {
					initMotionTouchEvent();// 초기화
					invalidate();// 뷰를 갱신
					Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
					return false;
				}

				if ( !mRectEnd.contains(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY) ) {
					initMotionTouchEvent();// 초기화
					invalidate();// 뷰를 갱신
					Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
					return false;
				}
//
				if ( HEX_BACKGROUND_TRANSPARENT.equals(mGShapeCurrentHexColorAddedAlpha) ) {
					initMotionTouchEvent();// 초기화
					invalidate();// 뷰를 갱신
					Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
					return false;
				}

				// OutOfBoundException 방지
				if ( arrVertex1.isEmpty() && arrVertex2.isEmpty() && arrVertex3.isEmpty() ) {
					invalidate();// 뷰를 갱신
					Toast.makeText(mContext, "OutOfBoundException 방지", Toast.LENGTH_SHORT).show();
					return false;
				}

				// 동그라미가 끝점에 도달하였는지 체크
				if (!mIsPathPointerPathThrough) {
					initMotionTouchEvent();// 초기화
					invalidate();// 뷰를 갱신
					Toast.makeText(mContext, "터치가 너무 빨라요. 동그라미가 끝점에 도달할 수 있도록 다시 한번 천천히 이동해 보세요.", Toast.LENGTH_SHORT).show();
					return false;
				}

				arrVertex2.add(arrVertex1.get(0));
				for (int i=1; i<arrVertex1.size(); i+=1) {
					float x2, y2;
					x2 = arrVertex1.get(i).x - arrVertex1.get(i-1).x;
					y2 = arrVertex1.get(i-1).y - arrVertex1.get(i).y;

					// 각도 구하기
					float radian = getAngle(x2, y2);

					// 거리 구하기
					float length = (float) Math.sqrt( Math.pow(x2, 2.f) + Math.pow(y2, 2.f) );
					// Math.sqrt: 루트 근사값 구하기
					// Math.pow: 제곱 함수

					// 각도로 구역구하기
					double tempang = ( radian + (mSection / 2) ) % (2 * sPi);
					int sec = (int) (tempang / mSection);

					arrVertex1.get(i).radian = radian;
					arrVertex1.get(i).length = length;
					arrVertex1.get(i).section = sec;

					// 이전 직선과의 각도차
					if (!allAngleReset) {
						double AngGap = arrVertex1.get(i-1).radian - arrVertex1.get(i).radian;
						if (AngGap > sPi) {
							AngGap -= 2 * sPi;
						} else if (AngGap < -sPi) {
							AngGap += 2 * sPi;
						}
						mAllAngle += AngGap;
						allAngle1 += AngGap;
					} else {
						allAngleReset = false;
					}

					allLength += length;
					Log.e("test", i + "번라인  구간  : " + sec + "  각도 : " + (int) (radian * sRtd) +
							" 길이합 : " + allLength + " 각도차합 : " + (int) (mAllAngle * sRtd) + "    " + (int) (allAngle1 * sRtd));

					if (mAllAngle > mSection * 3/2 || mAllAngle < -mSection * 3/2 ) {
						Log.e("test", i + "번째" +
								" 변곡점 각도 : "+(int)(mAllAngle * sRtd) +
								" 총 길이는 " + allLength);

						allAngleReset = false;
						mAllAngle = 0;
						arrVertex2.add(arrVertex1.get(i));
					}
				} // end for each

				arrVertex2.add( arrVertex1.get(arrVertex1.size() - 1) );

				Log.e("test","=========> 총각도 : "+ (int) (mAllAngle * sRtd));

				if (allAngle1 > sRoundMinAngle) {
					int round = (int) ( allAngle1 / (2 * sPi) );
					if ( allAngle1 % (2 * sPi) > sRoundMinAngle ) {
						round++;
					}
					Log.e("ACTION_UP", "원(반시계방향) "+ round + "바퀴");

					// TODO BUSINESS LOGIC
					mIsAuthorized = true;
					mIsDrawing = false;

					mGesturePointerListener.onButtonEnabled(true);
					Toast.makeText(mContext, "성공", Toast.LENGTH_SHORT).show();

					return false;
				} else if (-allAngle1 > sRoundMinAngle) {
					int round = (int) ( -allAngle1 / (2 * sPi) );
					if ( -allAngle1 % (2 * sPi) > sRoundMinAngle ) {
						round++;
					}
					Log.e("ACTION_UP", "원(시계방향) "+ round + "바퀴");

					// TODO BUSINESS LOGIC
					mIsAuthorized = true;
					mIsDrawing = false;

					mGesturePointerListener.onButtonEnabled(true);
					Toast.makeText(mContext, "성공", Toast.LENGTH_SHORT).show();

					return false;
				}

				double AllmoveAngle = 0;
				for (int i=1; i < arrVertex2.size(); i+=1) {
					float x2, y2;
					x2 = arrVertex2.get(i).x - arrVertex2.get(i-1).x;
					y2 = arrVertex2.get(i-1).y - arrVertex2.get(i).y;

					float length = (float) Math.sqrt( Math.pow(x2, 2.f) + Math.pow(y2, 2.f) );
					if ( length < (allLength / arrVertex2.size() / 2) ) {
						Log.e("test","2단계   ==> 전체 길이 : "+ allLength / arrVertex2.size() + " 이부분길이 : "+length);
						continue;
					}
					// 각도 구하기
					double radian = getAngle(x2, y2);

					// 첫번째 직선으로 보정
					radian += AllmoveAngle;
					// 매칭되는 가장 가까운 직선각 구하기 22.5 도 회전
					double tempang = (radian + (mSection / 2)) % (2 * sPi);

					double moveAngle = tempang % mSection;
					moveAngle = (moveAngle < (mSection / 2) ? (mSection / 2) - moveAngle : (mSection / 2) - moveAngle);

					if (i == 1) { // 첫번째 직선에 대해 보정.
						AllmoveAngle = moveAngle;
					}
					// 각도로 구역구하기
					int sec = (int) (tempang / mSection);

					Log.e("test","2단계   ==> "+ i +"번라인   구간  : "+sec+ "  각도 : " + (int) ( (radian+moveAngle) * sRtd) );

					if (arrVertex3.size() > 0) {
						if ( arrVertex3.get(arrVertex3.size()-1).section == sec ) {
							arrVertex3.get(arrVertex3.size()-1).length += length;
							continue;
						}
					}

					Vertex vertex = new Vertex(arrVertex2.get(i-1).x, arrVertex2.get(i-1).y);
					vertex.radian = (radian + moveAngle);
					vertex.length = length;
					vertex.section = sec;
					arrVertex3.add(vertex);
				} // end for each

				invalidate();

				// 텍스트 출력
				String str = "결과 : ";
				for (int i=0; i<arrVertex3.size(); i++) {
					str = str + arrVertex3.get(i).section;
					if ( i < arrVertex3.size()-1 ) {
						str = str + " -> ";
					}
				}

				Log.e("test", "텍스트 출력 - str: " + str);

				if ( isValidGesture() ) {
					mIsAuthorized = true;
					mIsDrawing = false;

					mGesturePointerListener.onButtonEnabled(true);
					Toast.makeText(mContext, "성공", Toast.LENGTH_SHORT).show();

					return true;
				} else {
					initMotionTouchEvent();// 초기화
					invalidate();// 뷰를 갱신
					Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
					return false;
				}

			} else {
				initMotionTouchEvent();// 초기화
				invalidate();// 뷰를 갱신
				Toast.makeText(mContext, "문자 형태에 맞게 다시 한번 제스처를 취해보세요.", Toast.LENGTH_SHORT).show();
				return false;
			}
		}

		// TODO BUSINESS LOGIC
		Log.e("ACTION_UP", "인증성공여부: " + mIsAuthorized);
		return true;
	}

	private boolean isValidGesture() {

		Log.e("test", "arrVertex1.size(): " + arrVertex1.size());
		Log.e("test", "arrVertex2.size(): " + arrVertex2.size());
		Log.e("test", "arrVertex3.size(): " + arrVertex3.size());
		Log.e("test", "allLength: " + allLength);
		Log.e("test", "arrVertex2.size(): " + arrVertex2.size());
		Log.e("test", "allLength / arrVertex2.size() > 80: " + (allLength / arrVertex2.size() > 80) );

		return arrVertex1.size() > 85
				&& arrVertex2.size() > 20
				&& arrVertex3.size() > 10;
//							&& Math.abs( (int) (allAngle * sRtd) ) > 4 // 총 각도
//				&& allLength / arrVertex2.size() > 80; // 전체 길이
	}

	/**
	 * 모션 터치 이벤트 초기화
	 */
	private void initMotionTouchEvent() {
		arrVertex1.clear();// 점의 연속: 사용자가 터치한 직선
		arrVertex2.clear();// 점의 연속: 1차 보간된 선분
		arrVertex3.clear();// 점의 연속: 최종 인식된 고정직선

		mDistanceMoved = 0;
		mPathMeasure.getPosTan(mDistanceMoved, mPathPos, mPathTan);

		mPathMatrix.reset();
		mPathDegrees = (float) (Math.atan2(mPathTan[1], mPathTan[0]) * 180.0 / Math.PI);
		mPathMatrix.postRotate(mPathDegrees, mPathBitmapOffsetX, mPathBitmapOffsetY);
		mPathMatrix.postTranslate(mPathPos[0] - mPathBitmapOffsetX, mPathPos[1] - mPathBitmapOffsetY);

		mIsDrawing = false;
		mIsPathPointerPathThrough = false;
	} // end method distance

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
	} // end method getAngle

	// =====

	/**
	 * 마우스 down
	 * - 초기 위치 기억
	 * - delayedMassage 를 생성합니다.
	 *
	 * 마우스 move
	 * - 일정범위 벗어나면 취소
	 *
	 * 마우스 up
	 * - long click 을 처리안 되었으면  message 를 지우고,  Short Click 을 수행합니다.
	 *
	 * message 함수
	 * - Long Click 을 처리합니다.
	 */
	class CheckForLongPress implements Runnable {

		private Handler mHandler = new Handler();

		// 시작 위치를 저장을 위한 변수
		private float mLastMotionX = 0;
		private float mLastMotionY = 0;

		//  마우스 move 로 일정범위 벗어나면 취소하기 위한  값
		private int mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();

		// long click 을  위한  변수들
		private boolean mHasPerformedLongPress;
		private CheckForLongPress mPendingCheckForLongPress;


		@Override
		public void run() {
			if ( performLongClick() ) {
				mHasPerformedLongPress = true;
				Log.e("CheckForLongPress", "run performLongClick()");
			}
		}

		/**
		 * Long Click 처리 설정을 위한 함수
		 * @param delayOffset
		 */
		private void postCheckForLongClick(int delayOffset) {
			mHasPerformedLongPress = false;
			if (mPendingCheckForLongPress == null) {
				mPendingCheckForLongPress = new CheckForLongPress();
			}
			mHandler.postDelayed(mPendingCheckForLongPress,
					ViewConfiguration.getLongPressTimeout() - delayOffset);

			// ViewConfiguration.getLongPressTimeout(): ViewConfiguration 에 설정된 Long Press 를 판단하기까지 기준 시간
			// 여기서 시스템의 getLongPressTimeout() 후에 message 수행하게 합니다.
			// 추가 delay 가 필요한 경우를 위해서 파라미터로 조절가능하게 합니다.
		}

		/**
		 * Remove the long press detection timer.
		 * 중간에 취소하는 용도입니다.
		 */
		private void removeLongPressCallback() {

			Log.e("CheckForLongPress", "removeLongPressCallback: 중간에 취소하는 용도입니다.");

			if (mPendingCheckForLongPress != null) {
				mHandler.removeCallbacks(mPendingCheckForLongPress);
			}
		}

		/**
		 *
		 * @return
		 */
		public boolean performLongClick() {
			// 실제 Long Click 처리하는 부분을 여기 둡니다.

			Log.d("CheckForLongPress", "performLongClick");

			return true;
		}
	}

	// =====

	/**
	 * 꼭지점
	 */
	public class Vertex {
		Vertex(float ax, float ay) {
	    	x = ax;
	    	y = ay;
	    }

		float x;
		float y;
		double radian;
		float length;
		int section;
	}


	private class Variables {

		public class MotionEvent {
			public int pointerCount;
			public int currentTouchedX, mMotionEventCurrentTouchedY;
			public int currentPixel;
			public int currentAlpha;
			public int currentRgbIntColor;
			public String currentHexColor;
			public String currentHexColorAddedAlpha;
		}

	}

}
