package com.ceedlive.ggesture.test_activity;

import android.os.Bundle;

import com.ceedlive.ggesture.test_view.DrawView10;

import androidx.appcompat.app.AppCompatActivity;

public class Draw10Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawView10(this));
//        setContentView(R.layout.motion_08_cycle);
    }

}
