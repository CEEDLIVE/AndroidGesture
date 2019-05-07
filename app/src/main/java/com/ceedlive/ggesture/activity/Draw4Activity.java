package com.ceedlive.ggesture.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.ceedlive.ggesture.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Draw4Activity extends AppCompatActivity {

    Bitmap bitmap;
    MyView myView;
    int[] location = new int[2];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myView = new MyView(this);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_genesis_g);

        setContentView(myView);
    }

    class MyView extends View {
        private Rect rect;   // rect를 전역변수로 선언한다

        //-----------------------------------
        //       Constructor
        //-----------------------------------
        public MyView(Context context) {
            super(context);
        }

        //-----------------------------------
        //       실제 그림을 그리는 부분
        //-----------------------------------
        public void onDraw(Canvas canvas) {

            Paint paint = new Paint();    // 새로운 paint 개체를 만들고
            paint.setColor(Color.GREEN);    // 연두색으로 설정

            final int color = 0xff424242;

            // View의 사이즈 만큼 Rect를 그려 준다. (사각형 영역을 만든다.)
            rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            // do some canvas drawing
            canvas.drawBitmap(bitmap, rect, rect, paint);

        } // onDraw 끝

        //-----------------------------------
        //      onTouchEvent
        //-----------------------------------
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                // 터치한 곳의 좌표 읽어오기
                int touchedX = (int) event.getX();
                int touchedY = (int) event.getY();

                float rowX = event.getRawX();
                float rowY = event.getRawY();

                int centerX = rect.centerX();
                int centerY = rect.centerY();

                myView.getLocationOnScreen(location);

                Log.e("touchedX", touchedX + "");
                Log.e("touchedY", touchedY + "");
                Log.e("rowX", rowX + "");
                Log.e("rowY", rowY + "");
                Log.e("centerX", centerX + "");
                Log.e("centerY", centerY + "");
                Log.e("location[0]", location[0] + "");
                Log.e("location[1]", location[1] + "");

                String msg = ": " + touchedX +" / " + touchedY;

                if ( rect.contains(touchedX, touchedY) ) {
                    Toast.makeText(getApplicationContext(), "Hit: " + msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Miss" + msg, Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        }

    } // GameView 끝
}
