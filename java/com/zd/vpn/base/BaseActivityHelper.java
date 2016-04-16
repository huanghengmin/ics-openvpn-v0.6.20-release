
package com.zd.vpn.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zd.vpn.R;
import com.zd.vpn.util.ToastUtils;


public class BaseActivityHelper implements BaseActivityListener {
    private Activity mActivity;
    private ProgressDialog mDialog;
    private BaseActivityListener listener;
    private Handler handler;
    private LayoutInflater layoutInflater;
    private TitleBar titleBar;

    public BaseActivityHelper(Activity activity, BaseActivityListener listener) {
        this.mActivity = activity;
        this.listener = listener;
    }

    @Override
    public void showProgressDialog(String msg) {
        if (mDialog != null && mDialog.isShowing()) {
            hideProgressDialog();
        }

        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(msg);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    @Override
    public void showAlertDialog(String title, String message,
            String positiveValue, OnClickListener linstener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity)
                .setMessage(message);
        dialog.setTitle(title);
        dialog.setPositiveButton(positiveValue, linstener);
        dialog.show();
    }

    @Override
    public void showChoiceDialog(String title, String content,
            String positiveValue, OnClickListener positiveListener,
            String nagetiveValue, OnClickListener nagetiveListener) {
        AlertDialog.Builder budiler = new AlertDialog.Builder(mActivity);
        budiler.setTitle(title);
        budiler.setMessage(content);
        budiler.setPositiveButton(positiveValue, positiveListener);
        budiler.setNegativeButton(nagetiveValue, nagetiveListener);
        budiler.create().show();
    }

    @Override
    public void showItemsDialog(String title, String[] items,
            OnClickListener listener) {
        AlertDialog.Builder budiler = new AlertDialog.Builder(mActivity);
        budiler.setTitle(title);
        budiler.setItems(items, listener);
        budiler.setNegativeButton("取消", null);
        budiler.create().show();
    }

    @Override
    public void hideProgressDialog() {
        try {
            mDialog.dismiss();
            mDialog = null;
        } catch (Exception e) {
        }
    }

    @Override
    public SharedPreferences getDefaultPref() {
        return mActivity.getSharedPreferences("default", 0);
    }

    @Override
    public void doOnCreate(Bundle savedInstanceState) {
        mActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityStack.getInstance().push(mActivity);
        if (listener.rootViewRes() != 0) {
            mActivity.setContentView(listener.rootViewRes());
        }
        mInitTitleBar();
    }

    public void doOnDestory() {
        hideProgressDialog();
        ActivityStack.getInstance().remove(mActivity);
    }

    @SuppressLint("HandlerLeak")
    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int cmd = msg.what;
                switch (msg.arg1) {
                   
                }
            }
        };
    }

    @Override
    public Handler getHandler() {
        if (handler == null)
            initHandler();
        return handler;
    }

    @Override
    public void onHttpResponse(int cmd, String json) {
        listener.onHttpResponse(cmd, json);
    }

    private void mInitTitleBar() {
        if (mActivity.findViewById(R.id.titleBar) != null) {

            titleBar = initTitlebar();
            if (titleBar != null) {
                Button titlebarLeftButton = (Button) mActivity
                        .findViewById(R.id.titlebarLeftButton);
                TextView titlebarTV = (TextView) mActivity
                        .findViewById(R.id.titlebarTV);
                LinearLayout titlebarRightContainerLayout = (LinearLayout) mActivity
                        .findViewById(R.id.titlebarRightContainerLayout);

                titlebarLeftButton.setOnClickListener(titleBar
                        .getmLeftBtnOnClickListener());
                titlebarTV.setText(titleBar.getmTitleValue());
                if (titleBar.getmRightContainerLayout() != null)
                    titlebarRightContainerLayout.addView(titleBar
                            .getmRightContainerLayout(),
                            new LinearLayout.LayoutParams(
                                    LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT));
            } else {
                mActivity.findViewById(R.id.titleBar).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public void showToast(String msg) {
        ToastUtils.show(mActivity, msg);
    }

    @Override
    public int rootViewRes() {
        return listener.rootViewRes();
    }

    @Override
    public void hideSofyKeyboard() {
        try {
            ((InputMethodManager) mActivity
                    .getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(mActivity.getCurrentFocus()
                            .getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public TitleBar initTitlebar() {
        return listener.initTitlebar();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends View> V findView(int id) {
        return (V) mActivity.findViewById(id);
    }

    public TextView getTitleTextView() {
        return (TextView) mActivity.findViewById(R.id.titlebarTV);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends View> V inflateView(int layout) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(mActivity);
        }
        return (V) layoutInflater.inflate(layout, null);
    }

}
