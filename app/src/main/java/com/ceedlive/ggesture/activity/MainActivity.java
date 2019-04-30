package com.ceedlive.ggesture.activity;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ceedlive.ggesture.R;

public class MainActivity extends AppCompatActivity {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private LinearLayout mWindowView;

    private float prevX = 200;
    private float prevY = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initialize();
        initWindowLayout();
    }

    private void initialize() {
        mWindowManager = getWindowManager();
    }

    /**
     * Window View 를 초기화 한다. X, Y 좌표는 0, 0으로 지정한다.
     */
    private void initWindowLayout() {
        mWindowView = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_gesture, null);
        mWindowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                0, 0, // X, Y 좌표
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowManager.addView(mWindowView, mWindowLayoutParams);
        mWindowView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN: // 처음 위치를 기억해둔다.
                        prevX = event.getRawX();
                        prevY = event.getRawY();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        float rawX = event.getRawX(); // 절대 X 좌표 값을 가져온다.
                        float rawY = event.getRawY(); // 절대 Y 좌표값을 가져온다.

                        // 이동한 위치에서 처음 위치를 빼서 이동한 거리를 구한다.
                        float x = rawX - prevX;
                        float y = rawY - prevY;

                        setCoordinateUpdate(x, y);

                        prevX = rawX;
                        prevY = rawY;
                        break;
                }// end switch
                return true;
            }
        });
    }

    /**
     * 이동한 거리를 x, y를 넘겨 LayoutParams 에 갱신한다.
     * @param x
     * @param y
     */
    private void setCoordinateUpdate(float x, float y) {
        if (mWindowLayoutParams != null) {
            mWindowLayoutParams.x += (int) x;
            mWindowLayoutParams.y += (int) y;

            mWindowManager.updateViewLayout(mWindowView, mWindowLayoutParams);
        }
    }

}
