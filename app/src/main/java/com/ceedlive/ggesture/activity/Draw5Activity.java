package com.ceedlive.ggesture.activity;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ceedlive.ggesture.DrawView;
import com.ceedlive.ggesture.R;

import androidx.appcompat.app.AppCompatActivity;

public class Draw5Activity extends AppCompatActivity {

    private WindowManager mWindowManager;
    private ImageView mImageView;
    private DrawView mDrawView;

    float oldXvalue;
    float oldYvalue;

    private Bitmap mBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_genesis_g);
        mDrawView = new DrawView(this);

        setContentView(mDrawView);

//        initialize();
//        initWindowLayout();
    }

    private void initialize() {
        mWindowManager = getWindowManager();
    }

    /**
     * Window View 를 초기화 한다. X, Y 좌표는 0, 0으로 지정한다.
     */
    private void initWindowLayout() {
        mImageView = findViewById(R.id.main_iv_me);
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int width = ((ViewGroup) v.getParent()).getWidth() - v.getWidth();
                int height = ((ViewGroup) v.getParent()).getHeight() - v.getHeight();

                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN: // 처음 위치를 기억해둔다.
                        Log.e("MotionEvent", "ACTION_DOWN: ");

                        oldXvalue = event.getX();
                        oldYvalue = event.getY();

                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        Log.e("MotionEvent", "ACTION_POINTER_DOWN");
                        break;

                    case MotionEvent.ACTION_MOVE:
                        Log.e("MotionEvent", "ACTION_MOVE");

                        v.setX(event.getRawX() - oldXvalue);
                        v.setY(event.getRawY() - (oldYvalue + v.getHeight()));

                        break;

                    case MotionEvent.ACTION_UP:
                        Log.e("MotionEvent", "ACTION_UP");

                        if (v.getX() > width && v.getY() > height) {
                            v.setX(width);
                            v.setY(height);
                        } else if (v.getX() < 0 && v.getY() > height) {
                            v.setX(0);
                            v.setY(height);
                        } else if (v.getX() > width && v.getY() < 0) {
                            v.setX(width);
                            v.setY(0);
                        } else if (v.getX() < 0 && v.getY() < 0) {
                            v.setX(0);
                            v.setY(0);
                        } else if (v.getX() < 0 || v.getX() > width) {
                            if (v.getX() < 0) {
                                v.setX(0);
                                v.setY(event.getRawY() - oldYvalue - v.getHeight());
                            } else {
                                v.setX(width);
                                v.setY(event.getRawY() - oldYvalue - v.getHeight());
                            }
                        } else if (v.getY() < 0 || v.getY() > height) {
                            if (v.getY() < 0) {
                                v.setX(event.getRawX() - oldXvalue);
                                v.setY(0);
                            } else {
                                v.setX(event.getRawX() - oldXvalue);
                                v.setY(height);
                            }
                        }


                        break;

                }// end switch

//                mImageView.setImageMatrix(matrix);
                return true;
            }
        });
    }

    // TEST 1
    private void limitDrag(Matrix m, ImageView view, int imageWidth, int imageHeight) {
        float[] values = new float[9];
        m.getValues(values);
        float[] orig = new float[] {0,0, imageWidth, imageHeight};
        float[] trans = new float[4];
        m.mapPoints(trans, orig);

        float transLeft = trans[0];
        float transTop = trans[1];
        float transRight = trans[2];
        float transBottom = trans[3];
        float transWidth = transRight - transLeft;
        float transHeight = transBottom - transTop;

        float xOffset = 0;
        if (transWidth > view.getWidth()) {
            if (transLeft > 0) {
                xOffset = -transLeft;
            } else if (transRight < view.getWidth()) {
                xOffset = view.getWidth() - transRight;
            }
        } else {
            if (transLeft < 0) {
                xOffset = -transLeft;
            } else if (transRight > view.getWidth()) {
                xOffset = -(transRight - view.getWidth());
            }
        }

        float yOffset = 0;
        if (transHeight > view.getHeight()) {
            if (transTop > 0) {
                yOffset = -transTop;
            } else if (transBottom < view.getHeight()) {
                yOffset = view.getHeight() - transBottom;
            }
        } else {
            if (transTop < 0) {
                yOffset = -transTop;
            } else if (transBottom > view.getHeight()) {
                yOffset = -(transBottom - view.getHeight());
            }
        }

        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];

        values[Matrix.MTRANS_X] = transX + xOffset;
        values[Matrix.MTRANS_Y] = transY + yOffset;
        m.setValues(values);
    }

    private float spacing(MotionEvent motionEvent) {
        float x = motionEvent.getX(0) - motionEvent.getX(1);
        float y = motionEvent.getY(0) - motionEvent.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        point.set((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2);
    }

}
