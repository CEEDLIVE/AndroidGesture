package com.ceedlive.ggesture.test_activity;

import android.os.Bundle;

import com.ceedlive.ggesture.test_view.DrawView11;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Draw11Activity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawView11(this));
    }

}
