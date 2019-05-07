package com.ceedlive.ggesture.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ceedlive.ggesture.R;

public class Draw7Activity extends AppCompatActivity {

    private Bitmap mBitmap;

    private ImageView imageView;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw7);

        imageView = findViewById(R.id.iv_draw7);


        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_genesis_g);
        imageView.setImageBitmap(mBitmap);


        imageView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    {
                        // TODO BUSINESS LOGIC
                        // 터치한 곳의 좌표 읽어오기
                        int touchedX = (int) event.getX();
                        int touchedY = (int) event.getY();
                        int touchCount = event.getPointerCount();


                        String msg = ": " + touchedX +" / " + touchedY;

                        Log.e("ACTION_DOWN", msg);
                        Log.e("ACTION_DOWN", "touchCount: " + touchCount + "");


                        Toast.makeText(getApplicationContext(), "TEST:" + msg, Toast.LENGTH_SHORT).show();

                        if ( touchedX < mBitmap.getWidth() && touchedY < mBitmap.getHeight() ) {
                            int pixel = mBitmap.getPixel(touchedX, touchedY);

                            int red = Color.red(pixel);
                            int blue = Color.blue(pixel);
                            int green = Color.green(pixel);

                            int intColor = Color.rgb(red, blue, green);
                            int parsedColor = Color.parseColor("#000000");

                            Log.e("intColor", "" + intColor);

                            String hexColor = String.format("#%06X", (0xFFFFFF & intColor));

                            Log.e("intColor", "" + intColor);
                            Log.e("hexColor", "" + hexColor);
                        }

                        break;
                    } // end case
                    case MotionEvent.ACTION_MOVE:
                    {
                        // TODO BUSINESS LOGIC


                        break;
                    } // end case
                    case MotionEvent.ACTION_UP:
                    {
                        // TODO BUSINESS LOGIC


                        break;
                    } // end case
                } // end switch

                return true;
            }
        });
    }

}
