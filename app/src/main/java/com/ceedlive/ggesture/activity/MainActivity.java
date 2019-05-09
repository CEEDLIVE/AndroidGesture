package com.ceedlive.ggesture.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ceedlive.ggesture.CustomGestureView;
import com.ceedlive.ggesture.R;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    private CustomGestureView mCustomGestureView;
    private Button mButton;

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
        mImageView = findViewById(R.id.iv_pointer);// 포인터 (이미지뷰)
        mCustomGestureView = findViewById(R.id.cv_gesture);// 커스텀뷰 (캔버스)
        mButton = findViewById(R.id.btn_login);// 로그인 버튼 (버튼)

        mImageView.setImageResource(R.drawable.ic_car_24);
        mImageView.setColorFilter(getResources().getColor(R.color.white));
    }

}
