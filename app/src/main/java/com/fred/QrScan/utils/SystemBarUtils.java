package com.fred.QrScan.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemBarUtils {
    public static void setStatusBarTextColorBlack(Activity activity) {
        if (activity == null) {
            return;
        }
        updateStatusBarTextColor(activity, true);
    }

    public static void setStatusBarTextColorWhite(Activity activity) {
        if (activity == null) {
            return;
        }
        updateStatusBarTextColor(activity, false);
    }

    private static void updateStatusBarTextColor(Activity activity, boolean isDarkMode) {
        if (!supportTranslucentStatusBar()) {
            return;
        }
        if (activity == null) {
            return;
        }
        if (Build.BRAND.equalsIgnoreCase("meizu")) {
            SystemBarUtils.setDarkModeForMeizu(activity, isDarkMode);
        } else if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
            //miui7.7.13及以后版本支持原生的更改状态栏字体颜色的方法，为了对老版本兼容，这里需要同时使用两种方法来更改颜色
            //http://www.miui.com/thread-8946673-1-1.html
            SystemBarUtils.setDarkModeInMi6(activity, isDarkMode);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                SystemBarUtils.setDarkModeAbove23(activity, isDarkMode);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SystemBarUtils.setDarkModeAbove23(activity, isDarkMode);
        }
    }

    private static void setDarkModeInMi6(Activity activity, boolean isBlack) {
        Window window = activity.getWindow();
        Class<?> clazz = window.getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (isBlack) {
                //黑色样式
                darkModeFlag = field.getInt(layoutParams);
            } else {
                extraFlagField.invoke(window, 0, field.getInt(layoutParams)); //清除黑色字体
            }
            int finalFlag = darkModeFlag;
            extraFlagField.invoke(window, finalFlag, finalFlag);
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void setDarkModeForMeizu(@NonNull Activity activity, boolean isDarkMode) {
        Window window = activity.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            try {
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlag = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlag.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlag.getInt(lp);
                if (isDarkMode) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlag.setInt(lp, value);
                window.setAttributes(lp);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static void setDarkModeAbove23(@NonNull Activity activity, boolean isDarkMode) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        View decorView = window.getDecorView();
        int visibility = decorView.getSystemUiVisibility();
        if (isDarkMode) {
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            visibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(visibility);
    }

    private static boolean supportTranslucentStatusBar() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static void setStatusBarTransparent(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            try {
                //先catch这个崩溃吧，不像是代码的问题
                //http://mobile.umeng.com/apps/f13f10a340b04265b74bcf25/error_types/show?error_type_id=52fcb47b56240b043a01f31f_7284942895977219141_4.6.3.2
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
