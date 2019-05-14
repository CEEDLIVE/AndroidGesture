package com.ceedlive.ggesture.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ceedlive.ggesture.CustomGestureView;
import com.ceedlive.ggesture.R;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private CustomGestureView mCustomGestureView;
    private Button mButtonQuit;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_gesture);
        initialize();

//        setContentView(R.layout.motion_g);
//        setContentView(R.layout.activity_animation);
    }

    private void initialize() {
        mHandler = new Handler();

        mCustomGestureView = findViewById(R.id.cv_gesture);// 커스텀뷰 (캔버스)
        mButtonQuit = findViewById(R.id.btn_quit);// 종료 버튼 (버튼)

        mButtonQuit.setEnabled(false);
        mCustomGestureView.gesturePointerCallback(new CustomGestureView.GesturePointerListener() {
            @Override
            public void onButtonEnabled(final boolean isEnabled) {
                Log.e("callback", "onButtonEnabled -  isEnabled:" + isEnabled);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mButtonQuit.setEnabled(isEnabled);
                    }
                }, 100);
            }
        });

        mButtonQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "이전 화면으로 이동 합니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
