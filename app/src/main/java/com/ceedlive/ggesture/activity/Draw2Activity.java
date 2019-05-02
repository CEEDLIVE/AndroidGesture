package com.ceedlive.ggesture.activity;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ceedlive.ggesture.R;

public class Draw2Activity extends AppCompatActivity {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private LinearLayout mWindowView;

    private ImageView mImageView;

    private final int xpos = 400;
    private final int ypos = 400;

    private float prevX = 200;
    private float prevY = 200;

    // TEST

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    private PointF start = new PointF();
    private PointF mid = new PointF();

    private float oldDistance = 1f;

    private float dx; // postTranslate X distance
    private float dy; // postTranslate Y distance
    private float[] matrixValues = new float[9];
    float matrixX = 0; // X coordinate of matrix inside the ImageView
    float matrixY = 0; // Y coordinate of matrix inside the ImageView
    float width = 0; // width of drawable
    float height = 0; // height of drawable


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initialize();
        initWindowLayout();
    }

    private void initialize() {
        mWindowManager = getWindowManager();
    }

    /**
     * Window View 를 초기화 한다. X, Y 좌표는 0, 0으로 지정한다.
     */
    private void initWindowLayout() {
//        mWindowView = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_gesture, null);

        // add
//        mImageView = mWindowView.findViewById(R.id.gesture_iv_me);
        mImageView = findViewById(R.id.main_iv_me);

//        mWindowLayoutParams = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
//                xpos, ypos, // X, Y 좌표
//                WindowManager.LayoutParams.TYPE_TOAST,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                PixelFormat.TRANSLUCENT);
//        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
//        mWindowManager.addView(mWindowView, mWindowLayoutParams);

        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN: // 처음 위치를 기억해둔다.

                        prevX = event.getRawX();
                        prevY = event.getRawY();

                        Log.e("MotionEvent", "ACTION_DOWN prevX: " + prevX + "");
                        Log.e("MotionEvent", "ACTION_DOWN prevY: " + prevY + "");

                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDistance = spacing(event);

                        Log.e("MotionEvent", "ACTION_POINTER_DOWN oldDistance: " + oldDistance + "");

//                        if(oldDistance > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
//                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float rawX = event.getRawX(); // 절대 X 좌표 값을 가져온다.
                        float rawY = event.getRawY(); // 절대 Y 좌표값을 가져온다.

                        Log.e("MotionEvent", "ACTION_MOVE rawX: " + rawX + "");
                        Log.e("MotionEvent", "ACTION_MOVE rawY: " + rawY + "");

                        // 이동한 위치에서 처음 위치를 빼서 이동한 거리를 구한다.
                        float x = rawX - prevX;
                        float y = rawY - prevY;

//                        setCoordinateUpdate(x, y);

                        prevX = rawX;
                        prevY = rawY;

                        // =====

                        matrix.set(savedMatrix);

                        matrix.getValues(matrixValues);
                        matrixX = matrixValues[2];
                        matrixY = matrixValues[5];
                        width = matrixValues[0] * mImageView.getDrawable()
                                .getIntrinsicWidth();
                        height = matrixValues[4] * mImageView.getDrawable()
                                .getIntrinsicHeight();

                        dx = event.getX() - start.x;
                        dy = event.getY() - start.y;

                        //if image will go outside left bound
                        if (matrixX + dx < 0) {
                            dx = -matrixX;
                        }
                        //if image will go outside right bound
                        if ( matrixX + dx + width > mImageView.getWidth() ) {
                            dx = mImageView.getWidth() - matrixX - width;
                        }
                        //if image will go oustside top bound
                        if (matrixY + dy < 0) {
                            dy = -matrixY;
                        }
                        //if image will go outside bottom bound
                        if ( matrixY + dy + height > mImageView.getHeight() ) {
                            dy = mImageView.getHeight() - matrixY - height;
                        }

                        matrix.postTranslate(dx, dy);

                        Log.e("MotionEvent", "ACTION_MOVE postTranslate dx: " + dx + "");
                        Log.e("MotionEvent", "ACTION_MOVE postTranslate dy: " + dy + "");

                        // =====

                        v.setX(event.getRawX() - prevX);
                        v.setY(event.getRawY() - (prevY + v.getHeight()));

                        break;

//                    case MotionEvent.ACTION_UP:
//                        oldDistance = spacing(event);
//
//                        Log.e("MotionEvent", "ACTION_POINTER_DOWN oldDistance: " + oldDistance + "");
//
//                        v.setX(event.getRawX() - prevX);
//                        v.setY(prevY);
//                        break;

                }// end switch

                mImageView.setImageMatrix(matrix);
                return true;
            }
        });
    }

    /**
     * 이동한 거리를 x, y를 넘겨 LayoutParams 에 갱신한다.
     * @param x
     * @param y
     */
    private void setCoordinateUpdate(float x, float y) {
        if (mWindowLayoutParams != null) {
            mWindowLayoutParams.x += (int) x;
            mWindowLayoutParams.y += (int) y;

            mWindowManager.updateViewLayout(mWindowView, mWindowLayoutParams);
        }
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
