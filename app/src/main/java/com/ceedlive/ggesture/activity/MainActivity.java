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
    private Button mButton;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_gesture);
        initialize();
    }

    private void initialize() {
        mHandler = new Handler();

        mCustomGestureView = findViewById(R.id.cv_gesture);// 커스텀뷰 (캔버스)
        mButton = findViewById(R.id.btn_login);// 로그인 버튼 (버튼)

        mButton.setEnabled(false);
        mCustomGestureView.gesturePointerCallback(new CustomGestureView.GesturePointerListener() {
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

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "로그인 합니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
