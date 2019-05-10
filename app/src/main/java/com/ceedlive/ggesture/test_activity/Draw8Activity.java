package com.ceedlive.ggesture.test_activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.ceedlive.ggesture.test_view.DrawView8;

import androidx.appcompat.app.AppCompatActivity;

public class Draw8Activity extends AppCompatActivity {

    private Bitmap mBitmap;

    private ImageView imageView;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawView8(this));
    }


}
