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
	
	static Context c;

	private Rect rectSrc;
	private Rect rectDest;

	public DrawView(Context context) {
		super(context); 
		c = context; 
		init_variable();
	}


	public void init_variable(){
		setBackgroundColor(Color.WHITE);

		arVertex1 = new ArrayList<>();
		arVertex2 = new ArrayList<>();
		arVertex3 = new ArrayList<>();
		
        mPaint = new Paint();
        
		pt = new Paint();
		tv = new TextView(this.getContext());

		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_genesis_g);
	}
	
	@Override
	public void onDraw(Canvas canvas) {

//		super.onDraw(canvas);

		float canvasRate = (float)canvas.getWidth() / canvas.getHeight();
		float bitmapRate = (float)bitmap.getWidth() / bitmap.getHeight();

		//

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

		float width, height;        // drawn width & height
		float xStart, yStart;       // start point (left top)

		// calculation process to fit bitmap in canvas
		if (canvasRate < bitmapRate) {       // canvas is vertically long
			width  = canvas.getWidth();
			height = width / bitmapRate;
			xStart = 0;
			yStart = (canvas.getHeight() - height) / 2;
		} else {                            // canvas is horizontally wide
			height = canvas.getHeight();
			width  = height * bitmapRate;
			yStart = 0;
			xStart = (canvas.getWidth() - width) / 2;
		}

		// source(on bitmap) → destination(on canvas)
		rectSrc  = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		rectDest = new Rect((int) xStart, (int) yStart,
				(int) (xStart + width), (int) (yStart + height));

		// draw bitmap on canvas
		mPaint.setAlpha(200);// 투명도 설정, 0 ~ 255
		canvas.drawBitmap(bitmap, rectSrc, rectDest, mPaint);

		float min = (width < height)? width : height;
		float radius = min * 0.03f;

		Log.e("min", min + "");
		Log.e("radius", radius + "");

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

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			{
				// 터치한 곳의 좌표 읽어오기
				int touchedX = (int) event.getX();
				int touchedY = (int) event.getY();

				String msg = ": " + touchedX +" / " + touchedY;

				//
				if ( rectDest.contains(touchedX, touchedY) ) {
					Toast.makeText(c, "Hit: " + msg, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(c, "Miss" + msg, Toast.LENGTH_SHORT).show();
				}
				//

				arVertex1.removeAll(arVertex1);
				arVertex2.removeAll(arVertex2);
				arVertex3.removeAll(arVertex3);
				arVertex1.add( new Vertex( event.getX(), event.getY() ) );
			} // end case
			case MotionEvent.ACTION_MOVE:
			{
				arVertex1.add( new Vertex( event.getX(), event.getY() ) );
				invalidate();
				break;
			} // end case
			case MotionEvent.ACTION_UP:
			{
//				float section = 2*pi / sectionNum;
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

				// TODO BUSINESS LOGIC




				break;
			} // end case
		} // end switch

		return true;
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

}
