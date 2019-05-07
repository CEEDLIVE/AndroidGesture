package com.ceedlive.ggesture.activity;

import android.os.Bundle;

import com.ceedlive.ggesture.DrawView10;
import com.ceedlive.ggesture.R;

import androidx.appcompat.app.AppCompatActivity;

public class Draw10Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawView10(this));
//        setContentView(R.layout.motion_08_cycle);
    }

}
