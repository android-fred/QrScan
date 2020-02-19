package com.fred.QrScan.zxingplug;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.fred.QrScan.R;

public class MainActivity extends Activity {
    private TextView vText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTheme(R.style.theme_day_base);
        setContentView(R.layout.activity_main);

        vText = (TextView) findViewById(R.id.text);
    }

    public void open(View view) {
        Intent intent = new Intent(this, CaptureActivity.class);
        intent.putExtra(CaptureActivity.KEY_THEME_COLOR, getResources().getColor(R.color.scan_corner_color));//Color.RED);
        startActivityForResult(intent, 0x10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x10) {
            String message;
            if (resultCode == RESULT_OK) {
                int errorCode = data.getIntExtra("result_type", 0);
                if (errorCode == 0) {
                    message = data.getStringExtra("result_string");
                } else {
                    message = "失败了";
                }
            } else {
                message = "取消了";
            }
            vText.setText(message);
        }
    }
}
