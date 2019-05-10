package com.ceedlive.ggesture.test_activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.ceedlive.ggesture.R;

import androidx.appcompat.app.AppCompatActivity;

public class Draw6Activity extends AppCompatActivity {

    private Bitmap mBitmap;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 실제 비트맵은 덩치가 큰 데이터이므로 실제프로젝트에서는 OnCreate 에서 미리 읽어 두는 것이 속도상 유리하다.

//        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_genesis_g);
        setContentView(new BitmapView(this));
    }

    public class BitmapView extends View{
        private Bitmap mViewBitmap;
        private int mWidth = 0;
        private int mHeight = 0;



        float canvasRate;
        float bitmapRate;


        float width, height;        // drawn width & height
        float xStart, yStart;       // start point (left top)

        private Rect rectSrc;
        private Rect rectDest;

        private Paint mPaint;

        private Canvas mCanvas;

        BitmapFactory.Options mBitmapFactoryOptions;


        public BitmapView(Context context){
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Paint _paint = new Paint();

            _paint.setColor(Color.BLUE);

            canvas.drawColor(Color.WHITE);

            canvas.drawCircle(200, 200, 100, _paint);



            _paint.setColor(0xFFFF00FF);

            canvas.drawText("글자 그리기", 200, 400, _paint);



            mViewBitmap = BitmapFactory.decodeResource(getResources(),

                    R.drawable.logo_genesis_g);

            canvas.drawBitmap(mViewBitmap, 100, 500, null);




            Path _path = new Path();

            _path.moveTo(400, 100);

            _path.lineTo(450, 150);

            _path.lineTo(100, 600);


            _paint.setColor(Color.RED);

            canvas.drawPath(_path, _paint);
        }

        private int[] createColors(){
            // 픽셀값  넣을 배열을 만들고 읽어온 이미지 픽셀값을 넣는다
            int[] colors = null;
            mWidth = mBitmap.getWidth();
            mHeight = mBitmap.getHeight();

            colors = new int[mWidth * mHeight];
            mBitmap.getPixels(colors, 0, mWidth, 0, 0, mWidth, mHeight);
            return colors;
        }

        private Bitmap adjustOpacity( Bitmap bitmap ) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Bitmap dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            dest.setPixels(pixels, 0, width, 0, 0, width, height);
            return dest;
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

//                    int canvasWidth = mCanvas.getWidth();
//                    int canvasHeight = mCanvas.getHeight();
//
//                    Log.e("canvasWidth", canvasWidth + "");
//                    Log.e("canvasHeight", canvasHeight + "");


                    String msg = ": " + touchedX +" / " + touchedY;

                    Log.e("ACTION_DOWN", msg);
                    Log.e("ACTION_DOWN", "touchCount: " + touchCount + "");









                    Toast.makeText(getApplicationContext(), "TEST:" + msg, Toast.LENGTH_SHORT).show();

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


}
