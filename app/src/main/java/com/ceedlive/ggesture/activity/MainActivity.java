package com.ceedlive.ggesture.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ceedlive.ggesture.CustomGestureView;
import com.ceedlive.ggesture.R;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageViewPointer;
    private CustomGestureView mCustomGestureView;
    private Button mButton;

    private Handler mHandler;

    private int delayMillis = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        setContentView(new CustomGestureView(this));
        setContentView(R.layout.activity_gesture);

        initialize();
    }

    private void initialize() {
        mHandler = new Handler();

        mImageViewPointer = findViewById(R.id.iv_pointer);// 포인터 (이미지뷰)
        mCustomGestureView = findViewById(R.id.cv_gesture);// 커스텀뷰 (캔버스)
        mButton = findViewById(R.id.btn_login);// 로그인 버튼 (버튼)

        mButton.setEnabled(false);

        mImageViewPointer.setImageResource(R.drawable.ic_car_24);
//        mImageViewPointer.setColorFilter(getResources().getColor(R.color.white));
        mImageViewPointer.setVisibility(View.VISIBLE);

        mCustomGestureView.gesturePointerCallback(new CustomGestureView.GesturePointerListener() {
            @Override
            public void onPointerInit(final int x, final int y) {
                Log.e("callback", "onPointerInit -  x:" + x + " y: " + y);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mImageViewPointer.setX(x);
                        mImageViewPointer.setY(y);
                    }
                }, delayMillis);
            }

            @Override
            public void onPointerMove(final int x, final int y) {
                Log.e("callback", "onPointerMove -  x:" + x + " y: " + y);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mImageViewPointer.setX(x);
                        mImageViewPointer.setY(y);
                    }
                }, delayMillis);
            }

            @Override
            public void onButtonEnabled(final boolean isEnabled) {
                Log.e("callback", "onButtonEnabled -  isEnabled:" + isEnabled);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mButton.setEnabled(isEnabled);
                    }
                }, 100);
            }
        });
    }

}
