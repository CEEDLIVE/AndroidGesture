package com.ceedlive.ggesture.activity;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.patternlockview.utils.ResourceUtils;
import com.ceedlive.ggesture.R;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PatternLockView mPatternLockView;
    private PatternLockViewListener mPatternLockViewListener;

    private ImageView mImageView;

    float oldXvalue;
    float oldYvalue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mPatternLockView = findViewById(R.id.pattern_lock_view);

//        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);       // Set the current viee more
//        mPatternLockView.setInStealthMode(false);                                     // Set the pattern in stealth mode (pattern drawing is hidden)
//        mPatternLockView.setTactileFeedbackEnabled(true);                            // Enables vibration feedback when the pattern is drawn
//        mPatternLockView.setInputEnabled(false);                                     // Disables any input from the pattern lock view completely
//
        mPatternLockView.setDotCount(7);
//        mPatternLockView.setDotNormalSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_size));
//        mPatternLockView.setDotSelectedSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_selected_size));
//        mPatternLockView.setPathWidth((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_path_width));
//        mPatternLockView.setAspectRatioEnabled(true);
//        mPatternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_HEIGHT_BIAS);
        mPatternLockView.setNormalStateColor(ResourceUtils.getColor(this, R.color.white));
        mPatternLockView.setCorrectStateColor(ResourceUtils.getColor(this, R.color.colorPrimary));
        mPatternLockView.setWrongStateColor(ResourceUtils.getColor(this, R.color.pomegranate));
        mPatternLockView.setDotAnimationDuration(150);
//        mPatternLockView.setPathEndAnimationDuration(100);


        mPatternLockViewListener = new PatternLockViewListener() {
            @Override
            public void onStarted() {
                Log.e(getClass().getName(), "Pattern drawing started");
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
                Log.e(getClass().getName(), "Pattern progress: " +
                        PatternLockUtils.patternToString(mPatternLockView, progressPattern));
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                Log.e(getClass().getName(), "Pattern complete: " +
                        PatternLockUtils.patternToString(mPatternLockView, pattern));
            }

            @Override
            public void onCleared() {
                Log.e(getClass().getName(), "Pattern has been cleared");
            }
        };

        mPatternLockView.addPatternLockListener(mPatternLockViewListener);

        initWindowLayout();
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

}
