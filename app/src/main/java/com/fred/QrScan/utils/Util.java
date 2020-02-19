package com.fred.QrScan.utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author admin on 2018/10/15.
 */
public class Util {
    private static Context context;

    public static void init(Context context) {
        Util.context = context.getApplicationContext();
    }

    public static Context context() {
        return context;
    }

    public static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void toast(CharSequence msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static boolean isOppo() {
        return "oppo".equalsIgnoreCase(Build.BRAND);
    }

    public static boolean isMiUi() {
        return "xiaomi".equalsIgnoreCase(Build.BRAND);
    }

    public static boolean isMeizu() {
        return "meizu".equalsIgnoreCase(Build.BRAND);
    }

    public static void jumpPermissionPage() {
        String brandName = Build.MANUFACTURER;
        String pkgName = Util.context().getPackageName();
        switch (brandName) {
            case "HUAWEI":
                goHuaWeiMainager(pkgName);
                break;
            case "vivo":
                goVivoMainager();
                break;
            case "OPPO":
                goOppoMainager();
                break;
            case "Coolpad":
                goCoolpadMainager();
                break;
            case "Meizu":
                goMeizuMainager(pkgName);
                break;
            case "Xiaomi":
                goXiaoMiMainager(pkgName);
                break;
            case "samsung":
                goSangXinMainager(pkgName);
                break;
            case "Sony":
                goSonyMainager(pkgName);
                break;
            case "LG":
                goLGMainager(pkgName);
                break;
            default:
                goIntentSetting(pkgName);
                break;
        }
    }

    private static void goHuaWeiMainager(String packageName) {
        try {
            Intent intent = new Intent(packageName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(comp);
            Util.context().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(packageName);
        }
    }

    private static void goXiaoMiMainager(String packageName) {
        String rom = getMiuiVersion();
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if ("V6".equals(rom) || "V7".equals(rom)) {
            intent.setAction("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intent.putExtra("extra_pkgname", packageName);
        } else {
            intent.setAction("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", packageName);
        }

        try {
            Util.context().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(packageName);
        }
    }

    private static String getMiuiVersion() {
        String propName = "ro.miui.ui.version.name";
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            Util.close(input);
        }
        return line;
    }

    private static void goLGMainager(String packageName) {
        Intent intent = new Intent(packageName);
        ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
        intent.setComponent(comp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            Util.context().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(packageName);
        }
    }

    private static void goSonyMainager(String packageName) {
        Intent intent = new Intent(packageName);
        ComponentName comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
        intent.setComponent(comp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            Util.context().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(packageName);
        }
    }

    private static void goMeizuMainager(String packageName) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            Util.context().startActivity(intent);
        } catch (ActivityNotFoundException localActivityNotFoundException) {
            localActivityNotFoundException.printStackTrace();
            goIntentSetting(packageName);
        }
    }

    private static void goSangXinMainager(String packageName) {
        //三星4.3可以直接跳转
        goIntentSetting(packageName);
    }

    private static void goIntentSetting(String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            Util.context().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Util.toast("跳转失败，请手动打开权限设置页面进行修改");
        }
    }

    private static void goOppoMainager() {
        doStartApplicationWithPackageName("com.coloros.safecenter");
    }

    /**
     * doStartApplicationWithPackageName("com.yulong.android.security:remote")
     * 和Intent open = getPackageManager().getLaunchIntentForPackage("com.yulong.android.security:remote");
     * startActivity(open);
     * 本质上没有什么区别，通过Intent open...打开比调用doStartApplicationWithPackageName方法更快，也是android本身提供的方法
     */
    private static void goCoolpadMainager() {
        doStartApplicationWithPackageName("com.yulong.android.security:remote");
    }

    private static void goVivoMainager() {
        doStartApplicationWithPackageName("com.bairenkeji.icaller");
    }

    private static void doStartApplicationWithPackageName(String packagename) {
        boolean result = doStartApplicationWithPackageNameInner(packagename);
        if (!result) {
            goIntentSetting(Util.context().getPackageName());
        }
    }

    private static boolean doStartApplicationWithPackageNameInner(String packagename) {
        if (TextUtils.isEmpty(packagename)) {
            return false;
        }
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = Util.context().getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return false;
        }
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);
        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = Util.context().getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        if (resolveinfoList == null || resolveinfoList.isEmpty()) {
            return false;
        }
        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo == null) {
            return false;
        }

        // packageName参数2 = 参数 packname
        String packageName = resolveinfo.activityInfo.packageName;
        // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packageName参数2.mainActivityname]
        String className = resolveinfo.activityInfo.name;
        // LAUNCHER Intent
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 设置ComponentName参数1:packageName参数2:MainActivity路径
        ComponentName cn = new ComponentName(packageName, className);
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            Util.context().startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
