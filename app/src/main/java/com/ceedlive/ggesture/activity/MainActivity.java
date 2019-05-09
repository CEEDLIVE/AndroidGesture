package com.ceedlive.ggesture.activity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.ceedlive.ggesture.CustomGestureView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(new CustomGestureView(this));
    }

}
