package com.ceedlive.ggesture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * 연속된 직선들을 n 방향으로 구간을 나누어
 * 360도 중에서 해당 구간으로 뻗어 나가는 직선을 인식하여 번호를 부여함으로써
 * 미리 정의된 연속된 번호들과 일치 하는가를 통해 사용자의 입력을 받아 들이는 방식
 */
public class DrawView10 extends RelativeLayout {
	Paint pt;

//	static final float pi = 3.1415926535f;
	static final double pi = 3.14159265358979;
	static final float rtd = 57.29577951f;
//	static final float sectionNum = 8;
//	static final float sectionNum = 16;
	static final float sectionNum = 32; // 방향성 개수 (연속된 직선들을 n 방향으로 구간을 나눔)
//	static final float roundMinAngle = 2 * pi * 11/12;
	static final double roundMinAngle = 2 * pi * 11/12;

    private ArrayList<Vertex> arVertex1; // 사용자가 터치한 직선
    private ArrayList<Vertex> arVertex2; // 1차 보간된 선분
    private ArrayList<Vertex> arVertex3; // 최종 인식된 고정직선

	private Paint mPaint;
	private TextView tv;
	private Bitmap bitmap;

	private Canvas mCanvas;

	static Context mContext;


	private ImageView imgView;
	LayoutInflater inflater;


	float oldXvalue;
	float oldYvalue;

	int mWidth;
	int mHeight;


	public DrawView10(Context context) {
		super(context);
		setFocusable(true);

		mContext = context;
		init_variable();
	}

	// you will need the constructor public MyView(Context context, AttributeSet attrs), otherwise you will get an Exception when Android tries to inflate your View.
	public DrawView10(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);

		mContext = context;
		init_variable();
	}

	// if you add your View from xml and also spcify the android:style attribute like : <com.mypack.MyView style="@styles/MyCustomStyle" />
	// you will also need the first constructor public MyView(Context context, AttributeSet attrs,int defStyle)
	public DrawView10(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFocusable(true);

		mContext = context;
		init_variable();
	}


	public void init_variable() {
		LayoutInflater.from(mContext).inflate(R.layout.layout_gesture2, this);
		imgView = findViewById(R.id.imgView);
		bitmap = getScaledBitmap(R.drawable.logo_genesis_g);
		mWidth = bitmap.getWidth();
		mHeight = bitmap.getHeight();

		imgView.setVisibility(VISIBLE);
		imgView.setImageResource(R.drawable.ic_car_24);
		imgView.setX(mWidth);
		imgView.setY(50);

		setBackgroundColor(Color.WHITE);

		arVertex1 = new ArrayList<>();
		arVertex2 = new ArrayList<>();
		arVertex3 = new ArrayList<>();
		
		pt = new Paint();
		tv = new TextView(this.getContext());
	}
	
	@Override
	public void onDraw(Canvas canvas) {

//		super.onDraw(canvas);

		mPaint = new Paint();
		mPaint.setFilterBitmap(true);

//		Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(),R.drawable.logo_genesis_g);
//		int targetWidth  = bitmapOrg.getWidth() * 2;
//		int targetHeight = bitmapOrg.getHeight() * 2;
//		Bitmap bmp = Bitmap.createBitmap(targetWidth, targetHeight,Bitmap.Config.ARGB_8888);
//		RectF rectf = new RectF(0, 0, targetWidth, targetHeight);
//		Canvas c = new Canvas(bmp);
//		Path path = new Path();
//		path.addRect(rectf, Path.Direction.CW);
//		c.clipPath(path);
//		c.drawBitmap( bitmapOrg, new Rect(0, 0, bitmapOrg.getWidth(), bitmapOrg.getHeight()),
//				new Rect(0, 0, targetWidth, targetHeight), mPaint);
		Matrix matrix = new Matrix();
		matrix.postScale(1f, 1f);
//		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, targetWidth, targetHeight, matrix, true);
//		int h = bitmapOrg.getHeight();
//		canvas.drawBitmap(bitmapOrg, 10,10, mPaint);
//		canvas.drawBitmap(resizedBitmap, 10,10 + h + 10, mPaint);

//		Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(),R.drawable.logo_genesis_g);
//		bitmap = getScaledBitmap(R.drawable.logo_genesis_g);


//		int targetWidth  = bitmapOrg.getWidth() * 2;
//		int targetHeight = bitmapOrg.getHeight() * 2;
		int targetWidth  = bitmap.getWidth();
		int targetHeight = bitmap.getHeight();

//		RectF rectf = new RectF(0, 0, targetWidth, targetHeight);
//		Path path = new Path();
//		path.addRect(rectf, Path.Direction.CW);
//		canvas.clipPath(path);

		mPaint.setAlpha(200);// 투명도 설정, 0 ~ 255
		canvas.drawBitmap(bitmap, 0, 0, mPaint);

//		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, targetWidth, targetHeight, matrix, true);
//		canvas.drawBitmap(resizedBitmap, 10,10 + h + 10, mPaint);

		mPaint.setStrokeWidth(6);

		// 사용자가 터치한 직선
		for (int i=1; i<arVertex1.size(); i++) {
			if (i == 1) {
				mPaint.setColor(Color.BLACK);
				mPaint.setAlpha(255);
			} else if ( i == arVertex1.size()-1 ) {
				mPaint.setColor(Color.RED);
				mPaint.setAlpha(255);
			} else {
				mPaint.setColor(Color.BLUE);
				mPaint.setAlpha(100);
			}

			// 부드럽게 하기 위해서 원을 추가
			canvas.drawCircle(arVertex1.get(i-1).x, arVertex1.get(i-1).y, 3, mPaint);
			canvas.drawLine(arVertex1.get(i-1).x, arVertex1.get(i-1).y, arVertex1.get(i).x, arVertex1.get(i).y, mPaint);
			canvas.drawCircle(arVertex1.get(i).x, arVertex1.get(i).y, 3, mPaint);
		}

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
//			float x2 = x1 + arVertex3.get(i).length * (float)Math.cos(arVertex3.get(i).radian);
//			float y2 = y1 + (arVertex3.get(i).length * (float)Math.sin(arVertex3.get(i).radian));
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

		//
//		Resources res = getResources();
//		BitmapDrawable bd = (BitmapDrawable) res.getDrawable(R.drawable.ic_car_24);
//		bullet = bd.getBitmap();
////
////
////		canvas.drawBitmap(bullet, 600, 0, mPaint);
//
//		imageView.setVisibility(View.VISIBLE);
//		imageView.setImageResource(R.drawable.ic_car);

//
//		// source(on bitmap) → destination(on canvas)
//		rectSrc  = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
////		rectDest = new Rect((int) xStart, (int) yStart,
////				(int) (xStart + width), (int) (yStart + height));
//		rectDest = new Rect((int) xStart, (int) yStart,
//				(int) (xStart + width), (int) (yStart + height));


		// draw bitmap on canvas
//		mPaint.setAlpha(200);// 투명도 설정, 0 ~ 255
//		canvas.drawBitmap(bitmap, rectSrc, rectDest, mPaint);

		//out of run method
//in run method
//		canvas.drawBitmap(bitmap, null, dstRect, null);

//		float min = (width < height)? width : height;
//		float radius = min * 0.03f;

//		Log.e("min", min + "");
//		Log.e("radius", radius + "");

		// draw four red circles around bitmap
//		canvas.drawCircle(xStart + radius, yStart + radius,
//				radius, mPaint);
//		canvas.drawCircle(xStart + width - radius, yStart + radius,
//				radius, mPaint);
//		canvas.drawCircle(xStart + radius, yStart + height - radius,
//				radius, mPaint);
//		canvas.drawCircle(xStart + width - radius, yStart + height - radius,
//				radius, mPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){

		// 터치한 곳의 좌표 읽어오기
		switch (event.getAction()) {
			// 처음 눌렸을 때
			case MotionEvent.ACTION_DOWN:
			{
				int touchedX = (int) event.getX();
				int touchedY = (int) event.getY();
				int touchCount = event.getPointerCount();

				oldXvalue = touchedX;
				oldYvalue = touchedY;

				String msg = ": " + touchedX +" / " + touchedY;

				if ( touchCount == 1 ) {
					if ( touchedX >= 0 && touchedX < bitmap.getWidth()
							&& touchedY >= 0 && touchedY < bitmap.getHeight() ) {
						int pixel = bitmap.getPixel(touchedX, touchedY);

						int alpha = Color.alpha(pixel);
						int intColor = getRgbIntColor(pixel);

						String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
						String addAlpha = addAlpha(hexColor, alpha);

						Log.e("ACTION_DOWN", "intColor: " + intColor);
						Log.e("ACTION_DOWN", "hexColor: " + hexColor);
						Log.e("ACTION_DOWN", "addAlpha: " + addAlpha);
						Log.e("ACTION_DOWN", "====================");

						if ("#00000000".equals(addAlpha)) {

							imgView.setX(mWidth - 50);
							imgView.setY(50);

							arVertex1.removeAll(arVertex1);
							arVertex2.removeAll(arVertex2);
							arVertex3.removeAll(arVertex3);
							invalidate();
							break;
						}

						arVertex1.removeAll(arVertex1);
						arVertex2.removeAll(arVertex2);
						arVertex3.removeAll(arVertex3);
						arVertex1.add( new Vertex( event.getX(), event.getY() ) );
					}
				}

				break;

			} // end case
			// 누르고 움직였을 때
			case MotionEvent.ACTION_MOVE:
			{
				int touchedX = (int) event.getX();
				int touchedY = (int) event.getY();
				int touchCount = event.getPointerCount();

//				imgView.setX(event.getRawX() - oldXvalue);
//				imgView.setY(event.getRawY() - (oldYvalue + imgView.getHeight()));

				Log.e("ACTION_MOVE", "touchedX: " + touchedX + "");
				Log.e("ACTION_MOVE", "touchedY: " + touchedY + "");
				Log.e("ACTION_MOVE", "touchCount: " + touchCount + "");

				if ( touchedX >= 0 && touchedX < bitmap.getWidth()
						&& touchedY >= 0 && touchedY < bitmap.getHeight() ) {
					int pixel = bitmap.getPixel(touchedX, touchedY);

					int alpha = Color.alpha(pixel);
					int intColor = getRgbIntColor(pixel);

					String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
					String addAlpha = addAlpha(hexColor, alpha);

					Log.e("ACTION_MOVE", "intColor: " + intColor);
					Log.e("ACTION_MOVE", "hexColor: " + hexColor);
					Log.e("ACTION_MOVE", "addAlpha: " + addAlpha);
					Log.e("ACTION_MOVE", "====================");

					if ("#00000000".equals(addAlpha)) {

						imgView.setX(mWidth - 50);
						imgView.setY(50);

						arVertex1.removeAll(arVertex1);
						arVertex2.removeAll(arVertex2);
						arVertex3.removeAll(arVertex3);
						invalidate();
						break;
					}

					imgView.setX(touchedX);
					imgView.setY(touchedY);

					arVertex1.add( new Vertex( event.getX(), event.getY() ) );
					invalidate();
				} else {

					imgView.setX(mWidth - 50);
					imgView.setY(50);

					arVertex1.removeAll(arVertex1);
					arVertex2.removeAll(arVertex2);
					arVertex3.removeAll(arVertex3);
					invalidate();
				}

				break;
			} // end case
			// 누른걸 땠을 때
			case MotionEvent.ACTION_UP:
			{
				int touchedX = (int) event.getX();
				int touchedY = (int) event.getY();
				int touchCount = event.getPointerCount();

				if ( touchedX >= 0 && touchedX < bitmap.getWidth()
						&& touchedY >= 0 && touchedY < bitmap.getHeight() ) {
					int pixel = bitmap.getPixel(touchedX, touchedY);

					int alpha = Color.alpha(pixel);
					int intColor = getRgbIntColor(pixel);

					String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
					String addAlpha = addAlpha(hexColor, alpha);

					Log.e("ACTION_UP", "intColor: " + intColor);
					Log.e("ACTION_UP", "hexColor: " + hexColor);
					Log.e("ACTION_UP", "addAlpha: " + addAlpha);
					Log.e("ACTION_UP", "====================");

					if ("#00000000".equals(addAlpha)) {

						imgView.setX(mWidth - 50);
						imgView.setY(50);

						arVertex1.removeAll(arVertex1);
						arVertex2.removeAll(arVertex2);
						arVertex3.removeAll(arVertex3);
						invalidate();

						return false;
					}

					double section = 2 * pi / sectionNum;
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
//				float tempang = (radian + (section/2))% (2 * pi);
						double tempang = ( radian + (section / 2) ) % (2 * pi);
						int sec = (int) (tempang / section);

						arVertex1.get(i).radian = radian;
						arVertex1.get(i).length = length;
						arVertex1.get(i).section = sec;

						// 이전 직선과의 각도차
						if (!allAngleReset) {
//					float AngGap = arVertex1.get(i-1).radian - arVertex1.get(i).radian;
							double AngGap = arVertex1.get(i-1).radian - arVertex1.get(i).radian;
							if (AngGap > pi) {
								AngGap -= 2 * pi;
							} else if (AngGap < -pi) {
								AngGap += 2 * pi;
							}
							allAngle += AngGap;
							allAngle1 += AngGap;
						} else {
							allAngleReset = false;
						}

						allLength += length;
						Log.e("test", i +"번라인  구간  : "+sec+ "  각도 : " + (int)(radian*rtd) +
								" 길이합 : " + allLength+ " 각도차합 : " + (int)(allAngle*rtd) + "    " + (int)(allAngle1*rtd));

						if (allAngle > section * 3/2 || allAngle < -section * 3/2 ) {
							Log.e("test", i + "번째" +
									" 변곡점 각도 : "+(int)(allAngle*rtd)+
									" 총 길이는 " + allLength);

							allAngleReset = false;
							allAngle = 0;
							arVertex2.add(arVertex1.get(i));
						}
					}
					arVertex2.add( arVertex1.get(arVertex1.size() - 1) );

					Log.e("test","=========> 총각도 : "+ (int)(allAngle*rtd));

					if (allAngle1 > roundMinAngle) {
						int round = (int) ( allAngle1 / (2 * pi) );
						if ( allAngle1 % (2 * pi) > roundMinAngle ) {
							round++;
						}
						Toast.makeText(this.getContext(), "원(반시계방향) "+ round + "바퀴" , Toast.LENGTH_SHORT).show();

						// TODO BUSINESS LOGIC

						return false;
					} else if (-allAngle1 > roundMinAngle) {
						int round = (int)(-allAngle1 / (2*pi));
						if (-allAngle1 % (2*pi) > roundMinAngle) {
							round++;
						}
						Toast.makeText(this.getContext(), "원(시계방향) "+ round + "바퀴 " , Toast.LENGTH_SHORT).show();

						// TODO BUSINESS LOGIC

						return false;
					}
//			float AllmoveAngle = 0;
					double AllmoveAngle = 0;
					for (int i=1; i<arVertex2.size(); i+=1) {
						float x2, y2;
						x2 = arVertex2.get(i).x - arVertex2.get(i-1).x;
						y2 = arVertex2.get(i-1).y - arVertex2.get(i).y;

						float length = (float) Math.sqrt( Math.pow(x2, 2.f) + Math.pow(y2, 2.f) );
						if ( length < (allLength / arVertex2.size() / 2) ) {
							Log.e("test","2단계   ==> 전체 길이 : "+ allLength / arVertex2.size() + " 이부분길이 : "+length);
							continue;
						}
						// 각도 구하기
//				float radian = getAngle(x2, y2);
						double radian = getAngle(x2, y2);

						// 첫번째 직선으로 보정
						radian += AllmoveAngle;
						// 매칭되는 가장 가까운 직선각 구하기 22.5 도 회전
//				float tempang = (radian + (section/2))% (2 * pi);
						double tempang = (radian + (section / 2)) % (2 * pi);

//				float moveAngle = tempang % section;
						double moveAngle = tempang % section;
						moveAngle = (moveAngle < (section / 2) ? (section / 2) - moveAngle : (section / 2) - moveAngle);

						if (i == 1) { // 첫번째 직선에대해 보정.
							AllmoveAngle = moveAngle;
						}
						// 각도로 구역구하기
						int sec = (int) (tempang / section);

						Log.e("test","2단계   ==> "+ i +"번라인   구간  : "+sec+ "  각도 : " + (int) ( (radian+moveAngle) * rtd) );

						// TODO BUSINESS LOGIC

						if (arVertex3.size() > 0) {
							if ( arVertex3.get(arVertex3.size()-1).section == sec ) {
								arVertex3.get(arVertex3.size()-1).length += length;
								continue;
							}
						}
						Vertex vertex = new Vertex(arVertex2.get(i-1).x, arVertex2.get(i-1).y);
//				vertex.radian = (radian+moveAngle);
						vertex.radian = (radian + moveAngle);
						vertex.length = length;
						vertex.section = sec;
						arVertex3.add(vertex);
					}
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
				} else {
					imgView.setX(mWidth - 20);
					imgView.setY(50);

					arVertex1.removeAll(arVertex1);
					arVertex2.removeAll(arVertex2);
					arVertex3.removeAll(arVertex3);
					invalidate();
				}

//				float section = 2*pi / sectionNum;


				// TODO BUSINESS LOGIC

				break;
			} // end case
		} // end switch

		return true;
	}

	int getRgbIntColor(int pixel) {
		int red = Color.red(pixel);
		int blue = Color.blue(pixel);
		int green = Color.green(pixel);
		int alpha = Color.alpha(pixel);

		int intColor = Color.rgb(red, blue, green);

		return intColor;
	}
	
	// x = 0 직선에 대한 점의 각도를 계산한다
	float getAngle(float x2, float y2) {
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
			radian -= pi;
		}
		
		// 사분면별 각도 조정
		if ( y2 < y1 && x2 > x1 ) {
			// blank
		} else if( (y2 < y1 && x2 < x1) || (y2 > y1 && x2 < x1) ) {
			radian += pi;
		} else {
			radian += 2 * pi;
		}
		return radian;
	}

//	public class Vertex {
//		Vertex(float ax, float ay){
//	    	x = ax;
//	    	y = ay;
//	    }
//
//		float x;
//		float y;
//		float radian;
//		float length;
//		int section;
//	}

	/**
	 *
	 */
	public class Vertex {
		Vertex(float ax, float ay){
	    	x = ax;
	    	y = ay;
	    }

		float x;
		float y;
//		float radian;
		double radian;
//		float length;
		float length;
		int section;
	}


	/**
	 *
	 * @return
	 */
	private Bitmap getScaledBitmap(int resourceId) {
		// 화면 크기 구하기
		WindowManager windowManager = (WindowManager) mContext.getSystemService( Context.WINDOW_SERVICE );
		Display display = windowManager.getDefaultDisplay();

		int displayWidth = display.getWidth();
		int displayHeight = display.getHeight();

		// 리사이즈할 이미지 크기 구하기
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(filePath, options);
		BitmapFactory.decodeResource(getResources(), R.drawable.logo_genesis_g, options);

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

//        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), resourceId, options);
		return bmp;
	}


	/**
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

}
