package com.fred.QrScan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import com.fred.QrScan.utils.QrUtils;

/**
 * Created by JayRay on 19/12/2016.
 * Info: 二维码相关方法
 */

public class QrScanUtils {
//    private static final String PLUGIN_ZXING = YdPluginManager.PLUGIN_ZXING;



    /**
     * 解码Bitmap，获取字符串
     * @param qrBitmap
     * @return
     */
    public static String decode(Bitmap qrBitmap) {
//        ClassLoader loader = RePlugin.fetchClassLoader(PLUGIN_ZXING);
//        if (loader != null) {
//            try {
//                return (String) ReflectUtil.invokeStaticMethod(loader, ZIXING_QRUTILS_CLASS, "decode",
//                        new Class[]{Bitmap.class}, qrBitmap);
//            } catch (Exception e) {
//                LogUtils.printStackTrace(e);
//            }
//        }
        return null;
    }

//    public static boolean canEncode() {
//        return RePlugin.isPluginInstalled(PLUGIN_ZXING);
//    }

    public static void startScan(final Activity activity) {
//        if (!QrUtils.canScan()) {
//            DownloadDialogMgr.startLoadPlugin(activity, YdPluginManager.PLUGIN_ZXING,
//                    new DownloadDialogMgr.FinishDownloadCallback() {
//                        @Override
//                        public void onFinish(boolean needStartActiivty) {
//                            if (needStartActiivty
//                                    && !activity.isFinishing()) {
//                                QrUtils.startScanInner(activity);
//                            }
//                        }
//
//                        @Override
//                        public void onDownloadFinish(boolean finish) {
//
//                        }
//                    });
//        } else {
//            QrUtils.startScanInner(activity);
//        }
    }

    private static boolean canScan() {
//        if (!RePlugin.isPluginInstalled(PLUGIN_ZXING)) {
//            return false;
//        }
//
//        return RePlugin.getPluginVersion(PLUGIN_ZXING) >= 103;
        return false;
    }


    private static void startScanInner(Activity activity) {
//        Intent intent = RePlugin.createIntent(PLUGIN_ZXING, "com.yidian.news.zxingplug.CaptureActivity");
//        intent.putExtra("theme_color", SkinLoaderProvider.getInstance().getPrimaryColor());
//        intent.putExtra("night_mode", true);
//        RepluginMgr.startActivityForResult(activity, intent, IntentConstants.RquestCode.SWEEP, PLUGIN_ZXING);
//
//        ReportProxy.logEvent(activity, ReportConstants.OPEN_SCAN);
//        new Report.Builder(ActionMethod.START_SCAN)
//                .submit();
    }

    public static void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
//        if (requestCode == IntentConstants.RquestCode.SWEEP) {
//            if (resultCode == Activity.RESULT_OK) {
//                int errorCode = data.getIntExtra("result_type", 0);
//                if (errorCode == 0) {
//                    String message = data.getStringExtra("result_string");
//                    processMessage(activity, message);
//                } else {
//                    CustomizedToastUtil.showPrompt("识别失败", false);
//                }
//            }
//        }
    }

    private static void processMessage(final Activity context, String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }

        String messageType;

        //todo： handle message
    }



    /**
     * 生成划词分享的图片，由于不在 app 中显示，直接分享，所以固定 1080 的图片
     * 短图长图样式不同
     * 短图（不超过 1920）  长图（超过1920）
     * ///////////////////////////////////
     * //              //               //
     * //    title     //     title     //
     * //              //               //
     * //   content    //    content    //
     * //              //               //
     * //      logo QR //      QR       //
     * //      hint QR //      QR       //
     * //              //     hint      //
     * //////////////////               //
     * //     logo      //
     * //               //
     * ///////////////////
     *
     * @param title   标题
     * @param content 内容
     * @param url     链接，用于生成二维码
     * @param context 上下文
     * @return 图片
     */
    public static Bitmap createContentBitmap(String title, String content, String url, Context context) {
        int width = 1080;
        int qrSize = 300;
        int paddingTop = 81;
        int paddingLeft = 165;
        int paddingRight = 90;
        int paddingBottom = 45;
        int titleHeight = 360;
        int height = 369;
        int titleSize = 60;
        int contentSize = 51;
        int hintSize = 30;
        int paddingH = 30;
        int paddingW = 30;
        int halfLineW = 3;
        int qrRight = 45;
        int screenHeight = 1920;
        int twoLinesHeight = 200;
        int longPicDiff = 400;
        int longPicBottom = 87;
        int longPicPadding = 111;
        int drawableHeight;
        int drawableWidth;
        //todo: need to update string here
        String pressHint = "xxx 分享";
        StaticLayout titleLayout = null;
        StaticLayout contentLayout;

        TextPaint titlePaint = new TextPaint();
        titlePaint.setAntiAlias(true);
        titlePaint.setColor(0x222222);
        titlePaint.setTextSize(titleSize);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        // calculate title height
        if (!TextUtils.isEmpty(title)) {
            title = TextUtils.ellipsize(title, titlePaint,
                    (width - paddingLeft - paddingRight) * 3 - 100, TextUtils.TruncateAt.END).toString();
            titleLayout = new StaticLayout(title, titlePaint, width - paddingLeft - paddingRight,
                    Layout.Alignment.ALIGN_NORMAL, 1.2f, 1, false);
            titleHeight = Math.max(titleHeight, titleLayout.getHeight() + (paddingTop << 1));
        }
        height += titleHeight;

        // calculate content height
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(0x222222);
        textPaint.setTextSize(contentSize);
        contentLayout = new StaticLayout(content, textPaint, width - paddingLeft - paddingRight,
                Layout.Alignment.ALIGN_NORMAL, 1.4f, 1, false);
        height += contentLayout.getHeight();

        // layout of long pics and short pics are different
        qrSize = height <= screenHeight ? 225 : 300;
        height = height <= screenHeight ? height : height + longPicDiff;
        hintSize = height <= screenHeight ? 30 : 33;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        // draw title background
        //todo: need update drawable here
        Drawable drawable = context.getResources().getDrawable(
                titleHeight <= twoLinesHeight ?
                        R.drawable.ic_launcher_background :
                        R.drawable.ic_launcher_background
        );
        drawable.setBounds(0, 0, width, titleHeight);
        drawable.draw(canvas);

        // draw title
        if (titleLayout != null) {
            canvas.save();
            canvas.translate(paddingLeft, (titleHeight - titleLayout.getHeight()) / 2);
            titleLayout.draw(canvas);
            canvas.restore();
        }

        // draw content
        canvas.save();
        canvas.translate(paddingLeft, titleHeight);
        contentLayout.draw(canvas);
        canvas.restore();

        // draw mark
        //todo: need update drawable here
        drawable = context.getResources().getDrawable(R.drawable.ic_launcher_foreground);
        drawableWidth = drawable.getIntrinsicWidth();
        drawableHeight = drawable.getIntrinsicHeight();
        drawable.setBounds(paddingLeft / 2 - drawableWidth / 2, titleHeight - paddingH - drawableHeight,
                paddingLeft / 2 + drawableWidth / 2, titleHeight - paddingH);
        drawable.draw(canvas);

        // draw blue line
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setARGB(0xFF, 0xEF, 0xF6, 0xFF);
        canvas.drawRect(paddingLeft / 2 - halfLineW, titleHeight,
                paddingLeft / 2 + halfLineW, titleHeight + contentLayout.getHeight(), paint);

        // generate QR code
        Bitmap qrCode = null;
        if (!TextUtils.isEmpty(url)) {
            try {
                qrCode = QrUtils.encode(url, qrSize);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // draw Logo and press scan QR Code hint
        //todo: need update drawable here
        drawable = context.getResources().getDrawable(R.drawable.ic_launcher_foreground);
        drawableWidth = drawable.getIntrinsicWidth();
        drawableHeight = drawable.getIntrinsicHeight();
        Rect hintRect = new Rect();
        textPaint.setColor(Color.rgb(0x0D, 0x0D, 0x0D));
        textPaint.setTextSize(hintSize);
        textPaint.getTextBounds(pressHint, 0, pressHint.length(), hintRect);

        if (height <= screenHeight) {

            // hint && logo align center
            int widthMid = width - (Math.max(drawableWidth, hintRect.width()) / 2 + paddingW + qrSize + qrRight);
            // total height of logo and hint
            int footHeight = drawableHeight + hintRect.height() + paddingW / 2;
            // hint and logo align center of QR code
            int heightMid = height - (Math.max(footHeight, qrSize) / 2 + paddingBottom);
            // draw
            drawable.setBounds(widthMid - drawableWidth / 2, heightMid - footHeight / 2,
                    widthMid + drawableWidth / 2, heightMid - footHeight / 2 + drawableHeight);
            drawable.draw(canvas);
            canvas.drawText(pressHint, widthMid - hintRect.width() / 2,
                    heightMid + footHeight / 2, textPaint);

            // draw QR code
            if (qrCode != null) {
                canvas.drawBitmap(qrCode, width - qrRight - qrSize,
                        height - paddingBottom - qrSize, textPaint);
            }
        } else {

            textPaint.setTextSize(33);
            // hint && logo && QR code align center
            int widthMid = width / 2;
            // total height of logo, hint and QR code
            int footHeight = drawableHeight + hintRect.height() + qrSize + longPicPadding + longPicBottom;
            // draw QR code
            if (qrCode != null) {
                canvas.drawBitmap(qrCode, widthMid - qrSize / 2,
                        height - footHeight, textPaint);
            }
            // draw hint
            canvas.drawText(pressHint, widthMid - hintRect.width() / 2,
                    height - footHeight + qrSize + hintRect.height(), textPaint);
            // draw logo
            drawable.setBounds(widthMid - drawableWidth / 2, height - longPicBottom - drawableHeight,
                    widthMid + drawableWidth / 2, height - longPicBottom);
            drawable.draw(canvas);
        }

        return bitmap;
    }
}
