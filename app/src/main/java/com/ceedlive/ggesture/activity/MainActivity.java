package com.ceedlive.ggesture.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ceedlive.ggesture.CustomGestureView;
import com.ceedlive.ggesture.R;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private CustomGestureView mCustomGestureView;
    private Button mButtonQuit;// 종료

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gesture);
        initialize();
    }

    private void initialize() {
        mHandler = new Handler();

        mCustomGestureView = findViewById(R.id.cv_gesture);// 커스텀뷰 (캔버스)
        mButtonQuit = findViewById(R.id.btn_quit);// 종료 버튼 (버튼)

        mButtonQuit.setEnabled(false);
        mCustomGestureView.gesturePointerCallback(new CustomGestureView.GesturePointerListener() {
            @Override
            public void onButtonEnabled(final boolean isEnabled) {
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
                Toast.makeText(getApplicationContext(), R.string.text_move_before, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
