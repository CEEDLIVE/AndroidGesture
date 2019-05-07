package com.ceedlive.ggesture.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.ceedlive.ggesture.DrawView9;
import com.ceedlive.ggesture.R;

import androidx.appcompat.app.AppCompatActivity;

public class Draw9Activity extends AppCompatActivity {

    private ImageView mImageViewBg;
    private ImageView mImageViewMe;

    float oldXvalue;
    float oldYvalue;

    private Bitmap mBitmap;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(new DrawView9(this));
//        setContentView(R.layout.activity_draw9);
//        initWindowLayout();
    }


    /**
     * Window View 를 초기화 한다. X, Y 좌표는 0, 0으로 지정한다.
     */
    private void initWindowLayout() {
        mImageViewBg = findViewById(R.id.main_iv_bg);
        mImageViewMe = findViewById(R.id.main_iv_me);

//        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_genesis_g);

        mBitmap = getScaledBitmap(R.drawable.logo_genesis_g);

        mImageViewBg.setImageBitmap(mBitmap);

        mImageViewBg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN: // 처음 위치를 기억해둔다.
                        Log.e("MotionEvent", "ACTION_DOWN: ");

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
                            int alpha = Color.alpha(pixel);

                            int intColor = Color.rgb(red, blue, green);
                            int parsedColor = Color.parseColor("#000000");

                            Log.e("intColor", "" + intColor);

                            String hexColor = String.format("#%06X", (0xFFFFFF & intColor));

                            String addAlpha = addAlpha(hexColor, alpha);

                            Log.e("intColor", "" + intColor);
                            Log.e("hexColor", "" + hexColor);
                            Log.e("addAlpha", "" + addAlpha);
                        }

                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        break;

                }// end switch

//                mImageView.setImageMatrix(matrix);
                return true;
            }
        });


        mImageViewMe.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int width = ((ViewGroup) v.getParent()).getWidth() - v.getWidth();
                int height = ((ViewGroup) v.getParent()).getHeight() - v.getHeight();

                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN: // 처음 위치를 기억해둔다.
                        Log.e("MotionEvent", "ACTION_DOWN: ");

                        oldXvalue = event.getX();
                        oldYvalue = event.getY();

                        int touchedX = (int) event.getX();
                        int touchedY = (int) event.getY();
                        int touchCount = event.getPointerCount();

                        if ( touchedX < mBitmap.getWidth() && touchedY < mBitmap.getHeight() ) {
                            int pixel = mBitmap.getPixel(touchedX, touchedY);

                            int red = Color.red(pixel);
                            int blue = Color.blue(pixel);
                            int green = Color.green(pixel);
                            int alpha = Color.alpha(pixel);

                            int intColor = Color.rgb(red, blue, green);
                            int parsedColor = Color.parseColor("#000000");

                            Log.e("intColor", "" + intColor);

                            String hexColor = String.format("#%06X", (0xFFFFFF & intColor));

                            String addAlpha = addAlpha(hexColor, alpha);

                            Log.e("intColor", "" + intColor);
                            Log.e("hexColor", "" + hexColor);
                            Log.e("addAlpha", "" + addAlpha);
                        }

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

    /**
     * @param originalColor color, without alpha
     * @param alpha         from 0.0 to 1.0
     * @return
     */
    public static String addAlpha(String originalColor, double alpha) {
        long alphaFixed = Math.round(alpha * 255);
        String alphaHex = Long.toHexString(alphaFixed);
        if (alphaHex.length() == 1) {
            alphaHex = "0" + alphaHex;
        }
        originalColor = originalColor.replace("#", "#" + alphaHex);


        return originalColor;
    }

    /**
     *
     * @return
     */
    private Bitmap getScaledBitmap(int resourceId) {
        // 화면 크기 구하기
        Display display = getWindowManager().getDefaultDisplay();
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        // 리사이즈할 이미지 크기 구하기
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(filePath, options);
        BitmapFactory.decodeResource(getResources(), R.drawable.logo_genesis_g, options);

        // 화면 크기에 가장 근접하는 이미지의 리스케일 사이즈를 구한다.
        float widthScale = options.outWidth / displayWidth;
        float heightScale = options.outHeight / displayHeight;
        float scale = widthScale > heightScale ? widthScale : heightScale;

        if (scale >= 8) {
            options.inSampleSize = 8;
        } else if (scale >= 4) {
            options.inSampleSize = 4;
        } else if (scale >= 2) {
            options.inSampleSize = 2;
        } else {
            options.inSampleSize = 1;
        }
        options.inJustDecodeBounds = false;

//        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId, options);

        return bitmap;
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
