package com.fred.QrScan.widgets;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.fred.QrScan.R;


public class SimpleDialog extends Dialog {
    private CharSequence mBtnLeftStr;
    private CharSequence mBtnRightStr;
    private CharSequence mMessageStr;
    private CharSequence title;
    private SimpleListener mListener;
    private int mGravity;
    // 最大行数
    private int mMaxLines = 2;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
            if (v.getId() == R.id.txv_left_btn) {
                if (mListener != null) {
                    mListener.onBtnLeftClick(SimpleDialog.this);
                }
            } else if (v.getId() == R.id.txv_right_btn) {
                if (mListener != null) {
                    mListener.onBtnRightClick(SimpleDialog.this);
                }
            }
        }
    };

    protected SimpleDialog(Context context) {
        super(context, R.style.Theme_AppCompat_Dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_dialog);

        TextView vTitle = (TextView) findViewById(R.id.title);
        if (TextUtils.isEmpty(title)) {
            vTitle.setVisibility(View.GONE);
        } else {
            vTitle.setVisibility(View.VISIBLE);
            vTitle.setText(title);
        }

        TextView txv = (TextView) findViewById(R.id.txv_message);
        txv.setText(mMessageStr);
        txv.setGravity(mGravity);
        txv.setMaxLines(mMaxLines);

        txv = (TextView) findViewById(R.id.txv_left_btn);
        txv.setText(mBtnLeftStr);
        txv.setOnClickListener(getOnClickListener());
        txv.setVisibility(TextUtils.isEmpty(mBtnLeftStr) ? View.GONE : View.VISIBLE);

        txv = (TextView) findViewById(R.id.txv_right_btn);
        txv.setText(mBtnRightStr);
        txv.setOnClickListener(getOnClickListener());
        txv.setVisibility(TextUtils.isEmpty(mBtnRightStr) ? View.GONE : View.VISIBLE);

        findViewById(R.id.middleDivider).setVisibility(
                TextUtils.isEmpty(mBtnLeftStr) || TextUtils.isEmpty(mBtnRightStr) ?
                        View.GONE : View.VISIBLE
        );
    }

    /**
     * 点击按钮响应，子类覆写之
     *
     * @return
     */
    protected View.OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public interface SimpleListener {
        void onBtnLeftClick(Dialog dialog);

        void onBtnRightClick(Dialog dialog);
    }

    public static class Builder {
        private CharSequence mBtnLeftStr;
        private CharSequence mBtnRightStr;
        private CharSequence mMessageStr;
        private CharSequence title;
        private SimpleListener mListener;
        private int mGravity = Gravity.CENTER;
        private int mMaxLines = Integer.MAX_VALUE; // 设置最大行数

        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(CharSequence str) {
            mMessageStr = str;
            return this;
        }

        public Builder setMessageGravity(int gravity) {
            mGravity = gravity;
            return this;
        }

        public Builder setLeftBtnStr(CharSequence str) {
            mBtnLeftStr = str;
            return this;
        }

        public Builder setRightBtnStr(CharSequence str) {
            mBtnRightStr = str;
            return this;
        }

        public Builder setSimpleListener(SimpleListener listener) {
            mListener = listener;
            return this;
        }

        public Builder setMaxLines(int maxLines) {
            mMaxLines = maxLines;
            return this;
        }

        public SimpleDialog create(Activity context) {
            if (mMessageStr == null) {
                return null;
            }

            SimpleDialog dialog = genereteInstance(context);
            dialog.mMessageStr = mMessageStr;
            dialog.mBtnLeftStr = mBtnLeftStr;
            dialog.mBtnRightStr = mBtnRightStr;
            dialog.mListener = mListener;
            dialog.mGravity = mGravity;
            dialog.mMaxLines = mMaxLines;
            dialog.title = title;
            return dialog;
        }

        protected SimpleDialog genereteInstance(Context context) {
            return new SimpleDialog(context);
        }
    }
}
