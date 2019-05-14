package com.ceedlive.ggesture.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class GraphicsUtil {

    /**
     * Get Bitmap From Vector Drawable
     * @param context
     * @param drawableId
     * @return
     */
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * 비트맵 이미지의 가로, 세로 이미지 사이즈를 리사이징
     * @param source 원본 Bitmap 객체
     * @param maxResolution 제한 해상도
     * @return 리사이즈된 이미지 Bitmap 객체
     */
    public static Bitmap getResizedBitmapByResolution(Bitmap source, int maxResolution){
        int width = source.getWidth();
        int height = source.getHeight();
        int newWidth = width;
        int newHeight = height;
        float rate = 0.0f;

        if (width > height) {
            if (maxResolution < width) {
                rate = maxResolution / (float) width;
                newHeight = (int) (height * rate);
                newWidth = maxResolution;
            }
        } else {
            if (maxResolution < height) {
                rate = maxResolution / (float) height;
                newWidth = (int) (width * rate);
                newHeight = maxResolution;
            }
        }

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }

    /**
     * 이미지를 주어진 너비와 높이에 맞게 리사이즈 하는 코드.
     * 원본 이미지를 크롭 하는게 아니라 리사이즈 하는 것이어서,
     * 주어진 너비:높이 의 비율이 원본 bitmap 의 비율과 다르다면 변환 후의 너비:높이의 비율도 주어진 비율과는 다를 수 있다.
     *
     * 가로가 넓거나 세로가 긴 이미지를 정사각형이나 원형의 view 에 맞추려 할 때,
     * 이 메쏘드를 호출한 후 반환된 bitmap 을 crop 하면 찌그러지지 않는 이미지를 얻을 수 있다.
     *
     * @param bitmap bitmap 원본 비트맵
     * @param width width 뷰의 가로 길이
     * @param height height 뷰의 세로 길이
     * @return Bitmap 리사이즈 된 bitmap
     */
    public static Bitmap getResizedBitmapByScale(Bitmap bitmap, int width, int height) {
        if ( bitmap.getWidth() != width || bitmap.getHeight() != height ) {
            float ratio = 1.0f;

            if (width > height) {
                ratio = (float)width / (float)bitmap.getWidth();
            } else {
                ratio = (float)height / (float)bitmap.getHeight();
            }

            bitmap = Bitmap.createScaledBitmap(bitmap,
                    (int) ( ( (float) bitmap.getWidth() ) * ratio), // Width
                    (int) ( ( (float) bitmap.getHeight() ) * ratio), // Height
                    false);
        }

        return bitmap;
    }

    /**
     * 이미지를 주어진 사이즈에 맞추어 crop 하는 코드
     * 원본의 가운데를 중심으로 crop 한다.
     * @param bitmap bitmap 원본 비트맵
     * @param width 뷰의 가로 길이
     * @param height 뷰의 세로 길이
     * @return
     */
    public static Bitmap getCroppedBitmap(Bitmap bitmap, int width, int height) {
        int originWidth = bitmap.getWidth();
        int originHeight = bitmap.getHeight();

        // 이미지를 crop 할 좌상단 좌표
        int x = 0;
        int y = 0;

        if (originWidth > width) { // 이미지의 가로가 view 의 가로보다 크면..
            x = (originWidth - width)/2;
        }

        if (originHeight > height) { // 이미지의 세로가 view 의 세로보다 크면..
            y = (originHeight - height)/2;
        }

        Bitmap cropedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
        return cropedBitmap;
    }

    /**
     * Project position on ImageView to position on Bitmap
     * return the color on the position
     * @param iv
     * @param bm
     * @param x
     * @param y
     * @return
     */
    public static int getProjectedColor(ImageView iv, Bitmap bm, int x, int y) {
        if ( x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight() ) {
            // outside ImageView
//			return color.background_light;
            return 0;
        }

        int projectedX = (int) ( (double) x * ( (double) bm.getWidth() / (double) iv.getWidth() ) );
        int projectedY = (int) ( (double) y * ( (double) bm.getHeight() / (double) iv.getHeight() ) );

        Log.e("getProjectedColor", x + ":" + y + "/" + iv.getWidth() + " : " + iv.getHeight() + "\n" +
                projectedX + " : " + projectedY + "/" + bm.getWidth() + " : " + bm.getHeight()
        );

        return bm.getPixel(projectedX, projectedY);
    }

    /**
     * 리스케일된 비트맵 이미지 객체 얻기
     * @return
     */
    private Bitmap getScaledBitmap(Context context, int resourceId) {
        try {
            // 화면 크기 구하기
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context.getSystemService( Context.WINDOW_SERVICE );
            Display display = windowManager.getDefaultDisplay();
            display.getMetrics(metrics);

//		int displayWidth = display.getWidth();
//		int displayHeight = display.getHeight();
            int displayWidth = metrics.widthPixels;
            int displayHeight = metrics.heightPixels;

            // 리사이즈할 이미지 크기 구하기
            BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(), resourceId, options);

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

            return BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 알파값이 더해진 hexadecimal color
     * @param originalColor color, without alpha
     * @param alpha         from 0.0 to 1.0
     * @return
     */
    public static String getHexaDecimalColorAddedAlpha(String originalColor, double alpha) {
        long alphaFixed = Math.round(alpha * 255);
        String alphaHex = Long.toHexString(alphaFixed);
        if (alphaHex.length() == 1) {
            alphaHex = "0" + alphaHex;
        }
        originalColor = originalColor.replace("#", "#" + alphaHex);
        return originalColor;
    }

    //

    /**
     * get dpi: dot per inch
     * @param context
     * @return
     */
    public static float getDotPerInch(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService( Context.WINDOW_SERVICE );
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(metrics);
        return metrics.density;
    }

    /**
     *
     * @param pixel
     * @param dpi
     * @return
     */
    public static float getDotPoint(float pixel, float dpi) {
        float dp;
        try {
            dp = pixel / dpi;
        } catch (ArithmeticException e) {
            return -1;
        }
        return dp;
    }

    public static float getPixel(Resources res, float density, float dp) {
        if (density < 0) {
            density = res.getDisplayMetrics().density;
        }

        float px = dp * density;

        return px;
    }

}
