/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fred.QrScan.zxingplug;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.fred.QrScan.R;
import com.fred.QrScan.camera.CameraManager;
import com.google.zxing.ResultPoint;

import java.util.Collection;
import java.util.HashSet;

/**
 * 自定义组件实现,扫描功能
 */
public final class ViewfinderView extends View {

    private static final long ANIMATION_DELAY = 16L;
    private static final int OPAQUE = 0xFF;
    public static final int MIN_HEIGHT = 500; // 高度临界值，当app高度小于此值时，扫描框居中显示

    private final Paint paint;
    private final int maskColor;
    private final int resultColor;
    private final int resultPointColor;
    private Bitmap resultBitmap;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;
    // 扫描线移动的y
    private int scanLineTop;
    // 扫描线移动速度
    private int SCAN_VELOCITY;
    // 扫描线
    private Bitmap scanLight;
    // 是否展示小圆点
    private boolean isCircle;
    // 扫描框边角颜色
    private int innercornercolor;
    // 扫描框边角长度
    private float innercornerlength;
    // 扫描框边角宽度
    private float innercornerwidth;

    private int themeColor;
    // 初始设置的扫描框上间距
    private float initMarginTop;

    public ViewfinderView(Context context) {
        this(context, null);
    }

    public ViewfinderView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);

    }

    public ViewfinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        possibleResultPoints = new HashSet<>(5);

        scanLight = BitmapFactory.decodeResource(resources,
                R.drawable.scan_light);

        initInnerRect(context, attrs);

        post(new Runnable() {
            @Override
            public void run() {
                resetCameraManagerFrameMarginTop(getHeight());
            }
        });
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void setThemeColor(int themeColor) {
        this.themeColor = themeColor;
    }

    /**
     * 初始化内部框的大小
     *
     * @param context
     * @param attrs
     */
    private void initInnerRect(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewfinderView);

        // 扫描框距离顶部
        float innerMarginTop = ta.getDimension(R.styleable.ViewfinderView_inner_margintop, -1);
        if (innerMarginTop != -1) {
            CameraManager.FRAME_MARGINTOP = (int) innerMarginTop;
        }
        initMarginTop = innerMarginTop;

        int defaultSize = dip2px(getContext(), 200);
        // 扫描框的宽度
        CameraManager.FRAME_WIDTH = (int) ta.getDimension(R.styleable.ViewfinderView_inner_width, defaultSize);
        // 扫描框的高度
        CameraManager.FRAME_HEIGHT = (int) ta.getDimension(R.styleable.ViewfinderView_inner_height, defaultSize);

        // 扫描框边角颜色
        innercornercolor = ta.getColor(R.styleable.ViewfinderView_inner_corner_color, Color.parseColor("#45DDDD"));
        // 扫描框边角长度
        innercornerlength = ta.getDimension(R.styleable.ViewfinderView_inner_corner_length, 65);
        // 扫描框边角宽度
        innercornerwidth = ta.getDimension(R.styleable.ViewfinderView_inner_corner_width, 15);

//        // 扫描bitmap
//        Drawable drawable = ta.getDrawable(R.styleable.ViewfinderView_inner_scan_bitmap);
//        if (drawable != null) {
//        }

        // 扫描控件
        scanLight = BitmapFactory.decodeResource(getResources(), ta.getResourceId(R.styleable.ViewfinderView_inner_scan_bitmap, R.drawable.scan_light));
        // 扫描速度
        SCAN_VELOCITY = ta.getInt(R.styleable.ViewfinderView_inner_scan_speed, 5);

        isCircle = ta.getBoolean(R.styleable.ViewfinderView_inner_scan_iscircle, true);

        ta.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
        canvas.drawRect(0, frame.bottom, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {

            drawFrameBounds(canvas, frame);

            drawScanLight(canvas, frame);

            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new HashSet<>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(OPAQUE);
                paint.setColor(themeColor == 0 ? resultPointColor : themeColor);

                if (isCircle) {
                    for (ResultPoint point : currentPossible) {
                        canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
                    }
                }
            }
            if (currentLast != null) {
                paint.setAlpha(OPAQUE / 2);
                paint.setColor(themeColor == 0 ? resultPointColor : themeColor);

                if (isCircle) {
                    for (ResultPoint point : currentLast) {
                        canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
                    }
                }
            }

            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    /**
     * 绘制移动扫描线
     *
     * @param canvas
     * @param frame
     */
    private void drawScanLight(Canvas canvas, Rect frame) {

        if (scanLineTop == 0) {
            scanLineTop = frame.top;
        }

        if (scanLineTop >= frame.bottom - 30) {
            scanLineTop = frame.top;
        } else {
            scanLineTop += SCAN_VELOCITY;
        }
        Rect scanRect = new Rect(frame.left, scanLineTop, frame.right,
                scanLineTop + 30);
        ColorFilter colorFilter = paint.getColorFilter();
        if (themeColor != 0) {
            paint.setColorFilter(new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN));
        }
        canvas.drawBitmap(scanLight, null, scanRect, paint);
        paint.setColorFilter(colorFilter);
    }

    /**
     * 绘制取景框边框
     *
     * @param canvas
     * @param frame
     */
    private void drawFrameBounds(Canvas canvas, Rect frame) {

        /*paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(frame, paint);*/

        if (themeColor == 0) {
            paint.setColor(innercornercolor);
        } else {
            paint.setColor(themeColor);
        }
        paint.setStyle(Paint.Style.FILL);

        float corWidth = innercornerwidth;
        float corLength = innercornerlength;

        // 左上角
        canvas.drawRect(frame.left, frame.top, frame.left + corWidth, frame.top
                + corLength, paint);
        canvas.drawRect(frame.left, frame.top, frame.left
                + corLength, frame.top + corWidth, paint);
        // 右上角
        canvas.drawRect(frame.right - corWidth, frame.top, frame.right,
                frame.top + corLength, paint);
        canvas.drawRect(frame.right - corLength, frame.top,
                frame.right, frame.top + corWidth, paint);
        // 左下角
        canvas.drawRect(frame.left, frame.bottom - corLength,
                frame.left + corWidth, frame.bottom, paint);
        canvas.drawRect(frame.left, frame.bottom - corWidth, frame.left
                + corLength, frame.bottom, paint);
        // 右下角
        canvas.drawRect(frame.right - corWidth, frame.bottom - corLength,
                frame.right, frame.bottom, paint);
        canvas.drawRect(frame.right - corLength, frame.bottom - corWidth,
                frame.right, frame.bottom, paint);
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }


    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 这里不用 getHeight() 获取高度的原因是:
        // 先在多窗口（左侧a区域，右上b区域，右下c区域）b区域打开扫一扫页面，然后打开另一app（展示在c区域），此时总共三个app显示在屏幕上。
        // 如果使用 getHeight()，则获取的仍是原来的高度，依然存在显示不全的问题。因此使用 newConfig.screenHeightDp 最新高度重新计算 marginTop ！
        resetCameraManagerFrameMarginTop(dip2px(getContext(), newConfig.screenHeightDp - 48)); // 48 为页面 toolbar 的高度
        drawViewfinder();
    }

    /**
     * 更新 marginTop
     *
     * @param height 显示区域高度
     */
    private void resetCameraManagerFrameMarginTop(int height) {
        if (height <= dip2px(getContext(), MIN_HEIGHT)) {
            CameraManager.FRAME_MARGINTOP = (height - CameraManager.FRAME_HEIGHT) / 2;
        } else {
            CameraManager.FRAME_MARGINTOP = (int) initMarginTop;
        }
    }
}
