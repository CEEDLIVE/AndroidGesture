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

	private Rect mRectStart, mRectIng1, mRectIng2, mRectEnd;
	private RectF mPathRectF;

	private boolean mIsAuthorized, mIsPassRectIng1, mIsPassRectIng2, mIsDrawing;

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
	private int mMotionEventPreviousTouchedX, mMotionEventPreviousTouchedY;
	private int mGShapeCurrentPixel, mPathPointerCurrentPixel;
	private int mGShapeCurrentAlpha, mPathPointerCurrentAlpha;
	private int mGShapeCurrentRgbIntColor, mPathPointerCurrentRgbIntColor;
	private String mGShapeCurrentHexColor, mPathPointerCurrentHexColor;
	private String mGShapeCurrentHexColorAddedAlpha, mPathPointerCurrentHexColorAddedAlpha;

	private List<Rect> mValidationRectLIst;


	private boolean mIsTouchMove;

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
			mRectStart = new Rect(mWidth - 550, 0, mWidth - 200, 350); // 사각형 영역을 만든다
			mRectIng1 = new Rect(0, (mHeight / 2) - 200, 400, (mHeight / 2) + 200); // 사각형 영역을 만든다
			mRectIng2 = new Rect(mWidth - 400, (mHeight / 2) - 200, mWidth, (mHeight / 2) + 200); // 사각형 영역을 만든다
			mRectEnd = new Rect(mWidth - 800, (mHeight / 2) - 200, mWidth - 400, (mHeight / 2) + 200); // 사각형 영역을 만든다

			mValidationRectLIst = new ArrayList<>();

			mValidationRectLIst.add(mRectStart);
			mValidationRectLIst.add(mRectIng1);
			mValidationRectLIst.add(mRectIng2);
			mValidationRectLIst.add(mRectEnd);
		}

		// set path
		{
			mDistanceEachStep = 5; // 패스 위를 움직이는 이미지의 이동속도
			mDistanceMoved = 0;
			mPathPos = new float[2];
			mPathTan = new float[2];

			mPathRectFWeight = 150;// 가변
			mPathRectF.set(0 + mPathRectFWeight, 0 + mPathRectFWeight, mWidth - mPathRectFWeight, mHeight - mPathRectFWeight);
			mAnimPath.arcTo(mPathRectF, -45f, -315f);
			mAnimPath.lineTo(mWidth / 2, mHeight / 2);
			mAnimPath.close();

			mPathMeasure = new PathMeasure(mAnimPath, false);
			mPathLength = mPathMeasure.getLength();
		}

		// set default flag
		{
			mIsAuthorized = false; // 접근 권한
			mIsPassRectIng1 = false; // 첫 번째 영역 통과
			mIsPassRectIng2 = false; // 두 번째 영역 통과
			mIsDrawing = false; // onDraw 메서드 전체 실행 여부

			mMotionEventPreviousTouchedX = 0;
			mMotionEventPreviousTouchedY = 0;
		}

		initMotionTouchEvent();
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
		drawFinalRecognizedLine(canvas, mPaint, arrVertex2);

	} // end method onDraw

	/**
	 * 문자 모양의 G 내부를 한붓 그리기로 통과하는 패스 생성
	 * @param canvas
	 * @param paint
	 * @param path
	 */
	private void drawGShapePath(Canvas canvas, Paint paint, Path path) {
//		paint.setColor(Color.TRANSPARENT);
		paint.setColor(Color.RED);
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
		paint.setColor(Color.TRANSPARENT);
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

			paint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리 // FIXME

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

			paint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리 // FIXME

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

			paint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리 // FIXME

			canvas.drawLine(x1, y1, x2, y2, paint);
			paint.setColor(Color.RED);

			paint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리 // FIXME

			canvas.drawCircle(x2, y2, 3, paint);
			paint.setColor(Color.BLACK);

			paint.setColor(Color.TRANSPARENT);// 선 안 보이게 처리 // FIXME

			canvas.drawCircle(x1, y1, 3, paint);
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
			// 누르고 있던 것을 떼었을 때
			case MotionEvent.ACTION_UP: {
				handleTouchUp(event);
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

		// single touch
		if ( mMotionEventPointerCount == 1 ) {
			if ( mMotionEventCurrentTouchedX >= 0 && mMotionEventCurrentTouchedX < mGShapeBitmap.getWidth()
					&& mMotionEventCurrentTouchedY >= 0 && mMotionEventCurrentTouchedY < mGShapeBitmap.getHeight() ) {

				mGShapeCurrentPixel = mGShapeBitmap.getPixel(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY);
				mGShapeCurrentAlpha = Color.alpha(mGShapeCurrentPixel);
				mGShapeCurrentRgbIntColor = CustomUtil.getRgbIntColor(mGShapeCurrentPixel);
				mGShapeCurrentHexColor = CustomUtil.getHexColor(mGShapeCurrentRgbIntColor);
				mGShapeCurrentHexColorAddedAlpha = CustomUtil.getHexaDecimalColorAddedAlpha(mGShapeCurrentHexColor, mGShapeCurrentAlpha);

				initMotionTouchEvent();// 초기화

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

				mIsDrawing = true;
				mIsTouchMove = true;
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

		// single touch
		if ( mMotionEventPointerCount == 1 ) {
			if ( mMotionEventCurrentTouchedX >= 0 && mMotionEventCurrentTouchedX < mGShapeBitmap.getWidth()
					&& mMotionEventCurrentTouchedY >= 0 && mMotionEventCurrentTouchedY < mGShapeBitmap.getHeight() ) {

				mGShapeCurrentPixel = mGShapeBitmap.getPixel(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY);
				mGShapeCurrentAlpha = Color.alpha(mGShapeCurrentPixel);
				mGShapeCurrentRgbIntColor = CustomUtil.getRgbIntColor(mGShapeCurrentPixel);
				mGShapeCurrentHexColor = CustomUtil.getHexColor(mGShapeCurrentRgbIntColor);
				mGShapeCurrentHexColorAddedAlpha = CustomUtil.getHexaDecimalColorAddedAlpha(mGShapeCurrentHexColor, mGShapeCurrentAlpha);

				if ( mRectIng1.contains(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY) ) {
					mIsPassRectIng1 = true;
				}

				if ( mRectIng2.contains(mMotionEventCurrentTouchedX, mMotionEventCurrentTouchedY) ) {
					mIsPassRectIng2 = true;
				}

				if ( HEX_BACKGROUND_TRANSPARENT.equals(mGShapeCurrentHexColorAddedAlpha) ) {
					initMotionTouchEvent();// 초기화
					invalidate();// 뷰를 갱신
					return false;
				}

				// ========================================
				// logic1

				mMotionEventPreviousTouchedX = mMotionEventPreviousTouchedX == 0 ? mMotionEventCurrentTouchedX : mMotionEventPreviousTouchedX;
				mMotionEventPreviousTouchedY = mMotionEventCurrentTouchedY == 0 ? mMotionEventCurrentTouchedY : mMotionEventPreviousTouchedY;


				if (mDistanceMoved < mPathLength) {
					mPathMeasure.getPosTan(mDistanceMoved, mPathPos, mPathTan);
					mPathMatrix.reset();
					mPathDegrees = (float) (Math.atan2(mPathTan[1], mPathTan[0]) * 180.0 / Math.PI);
					mPathMatrix.postRotate(mPathDegrees, mPathBitmapOffsetX, mPathBitmapOffsetY);
					mPathMatrix.postTranslate(mPathPos[0] - mPathBitmapOffsetX, mPathPos[1] - mPathBitmapOffsetY);
					mDistanceMoved += mDistanceEachStep;
				} else {
					mDistanceMoved = 0;
				}
				// ========================================

				if (mMotionEventPreviousTouchedX == mMotionEventCurrentTouchedX) {
					Log.e("handleTouchMoveZZZ", "중복");
				}
				if (mMotionEventPreviousTouchedY == mMotionEventCurrentTouchedY) {
					Log.e("handleTouchMoveZZZ", "중복");
				}

				Log.e("handleTouchMoveZZZ", "mPathBitmapOffsetX: " + mPathBitmapOffsetX);
				Log.e("handleTouchMoveZZZ", "mPathBitmapOffsetY: " + mPathBitmapOffsetY);
				Log.e("handleTouchMoveZZZ", "mMotionEventCurrentTouchedX: " + mMotionEventCurrentTouchedX);
				Log.e("handleTouchMoveZZZ", "mMotionEventCurrentTouchedY: " + mMotionEventCurrentTouchedY);
				Log.e("handleTouchMoveZZZ", "mPathMatrix.toString(): " + mPathMatrix.toString());
				Log.e("handleTouchMoveZZZ", "(mPathPos[0] - mPathBitmapOffsetX): " + (mPathPos[0] - mPathBitmapOffsetX) );
				Log.e("handleTouchMoveZZZ", "(mPathPos[1] - mPathBitmapOffsetY): " + (mPathPos[1] - mPathBitmapOffsetY) );

				// ========================================
				// logic2
				arrVertex1.add( new Vertex( event.getX(), event.getY() ) );

				if ( mMotionEventCurrentTouchedX + 50 >= mWidth / 2 ) {
					Log.e("handleTouchMove", "mMotionEventCurrentTouchedX: " + mMotionEventCurrentTouchedX);
				}
				if ( mMotionEventCurrentTouchedY >= mHeight / 2 ) {
					Log.e("handleTouchMove", "mMotionEventCurrentTouchedY: " + mMotionEventCurrentTouchedY);
				}
				// ========================================

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

				if ( !mIsPassRectIng1 || !mIsPassRectIng2 ) {
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

				double section = 2 * sPi / sSectionNum;
				float allAngle = 0, allAngle1 = 0, allLength = 0;
				boolean allAngleReset = true;

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
					double tempang = ( radian + (section / 2) ) % (2 * sPi);
					int sec = (int) (tempang / section);

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
						arrVertex2.add(arrVertex1.get(i));
					}
				} // end for each

				arrVertex2.add( arrVertex1.get(arrVertex1.size() - 1) );

				Log.e("test","=========> 총각도 : "+ (int)(allAngle*sRtd));

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
					double tempang = (radian + (section / 2)) % (2 * sPi);

					double moveAngle = tempang % section;
					moveAngle = (moveAngle < (section / 2) ? (section / 2) - moveAngle : (section / 2) - moveAngle);

					if (i == 1) { // 첫번째 직선에 대해 보정.
						AllmoveAngle = moveAngle;
					}
					// 각도로 구역구하기
					int sec = (int) (tempang / section);

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

				boolean isValidTouch = arrVertex1.size() > 85
						&& arrVertex2.size() > 20
						&& arrVertex3.size() > 10
//							&& Math.abs( (int) (allAngle * sRtd) ) > 4 // 총 각도
						&& allLength / arrVertex2.size() > 80; // 전체 길이

				if (isValidTouch) {
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
