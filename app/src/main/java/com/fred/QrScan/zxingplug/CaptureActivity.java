package com.fred.QrScan.zxingplug;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import androidx.activity.ComponentActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.fred.QrScan.R;
import com.fred.QrScan.utils.CodeUtils;
import com.fred.QrScan.utils.SystemBarUtils;
import com.fred.QrScan.utils.Util;
import com.fred.QrScan.widgets.SimpleDialog;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

/**
 * Initial the camera
 * <p>
 * 默认的二维码扫描Activity
 */
public class CaptureActivity extends AppCompatActivity {
    public static final String RESULT_TYPE = "result_type";
    public static final String RESULT_STRING = "result_string";
    public static final String KEY_THEME_COLOR = "theme_color";

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILED = 1;

    private static final int REQUEST_CODE = 0x00ff;

    /**
     * 二维码解析回调函数
     */
    private final CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(RESULT_TYPE, RESULT_SUCCESS);
            bundle.putString(RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
            finish();
        }

        @Override
        public void onAnalyzeFailed() {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(RESULT_TYPE, RESULT_FAILED);
            bundle.putString(RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.init(this);
        super.onCreate(savedInstanceState);

        SystemBarUtils.setStatusBarTransparent(getWindow());

        Intent intent = getIntent();
        SystemBarUtils.setStatusBarTextColorBlack(this);


        setContentView(R.layout.activity_camera);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        checkPermissions();
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.VIBRATE);

        PermissionGen.with(this)
                .addRequestCode(REQUEST_CODE)
                .permissions(permissions.toArray(new String[permissions.size()]))
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = REQUEST_CODE)
    public void onRequestPermissionSuccess() {
        if (Util.isMeizu()) {
            if (!hasCameraPermission()) {
                showRequestPermissionDialog();
                return;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_THEME_COLOR, getIntent().getIntExtra(KEY_THEME_COLOR, 0));

        CaptureFragment captureFragment = new CaptureFragment();
        captureFragment.setArguments(bundle);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_zxing_container, captureFragment).commitAllowingStateLoss();
        captureFragment.setCameraInitCallBack(new CaptureFragment.CameraInitCallBack() {
            @Override
            public void callBack(Exception e) {
                if (e == null) {
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @PermissionFail(requestCode = REQUEST_CODE)
    public void onRequestPermissionFailed() {
        setResult(RESULT_CANCELED);
        showRequestPermissionDialog();
    }

    /**
     * 魅族手机权限检测始终返回true。
     * 使用暴力打开摄像头的方法判断是否拥有相机权限
     */
    private boolean hasCameraPermission() {
        boolean canUse = true;
        Camera camera = null;
        try {
            camera = Camera.open();
            // setParameters 是针对魅族MX5 做的。MX5 通过Camera.open() 拿到的Camera
            // 对象不为null。其他手机是null
            Camera.Parameters mParameters = camera.getParameters();
            camera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        } finally {
            if (camera != null) {
                camera.release();
            }
        }
        return canUse;
    }

    private void showRequestPermissionDialog() {
        Dialog dialog = new SimpleDialog.Builder()
                .setMessage("由于未开启拍摄权限，当前无法进行拍摄。\n" +
                        "请在系统的设置应用权限中，允许使用相机权限。")
                .setLeftBtnStr("取消")
                .setRightBtnStr("设置权限")
                .setSimpleListener(new SimpleDialog.SimpleListener() {
                    @Override
                    public void onBtnLeftClick(Dialog d) {
                        finish();
                    }

                    @Override
                    public void onBtnRightClick(Dialog d) {
                        Util.jumpPermissionPage();
                        finish();
                    }
                })
                .create(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}