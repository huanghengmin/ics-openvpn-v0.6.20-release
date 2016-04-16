
package com.zd.vpn.base;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

public abstract class BaseActivity extends FragmentActivity implements
        BaseActivityListener {
    private BaseActivityHelper baseActivityHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivityHelper = new BaseActivityHelper(this, this);
        doOnCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        baseActivityHelper.doOnDestory();
    }

    @Override
    public void showProgressDialog(String msg) {
        baseActivityHelper.showProgressDialog(msg);
    }

    @Override
    public void showAlertDialog(String title, String message,
            String positiveValue, OnClickListener linstener) {
        baseActivityHelper.showAlertDialog(title, message, positiveValue,
                linstener);
    }

    @Override
    public void showChoiceDialog(String title, String content,
            String positiveValue, OnClickListener positiveListener,
            String nagetiveValue, OnClickListener nagetiveListener) {
        baseActivityHelper.showChoiceDialog(title, content, positiveValue,
                positiveListener, nagetiveValue, nagetiveListener);
    }

    @Override
    public void showItemsDialog(String title, String[] items, OnClickListener listener) {
        baseActivityHelper.showItemsDialog(title, items, listener);
    }

    @Override
    public void hideProgressDialog() {
        baseActivityHelper.hideProgressDialog();
    }

    @Override
    public SharedPreferences getDefaultPref() {
        return baseActivityHelper.getDefaultPref();
    }

    @Override
    public void doOnCreate(Bundle savedInstanceState) {
        baseActivityHelper.doOnCreate(savedInstanceState);
    }

    @Override
    public Context getContext() {
        return baseActivityHelper.getContext();
    }

    @Override
    public void showToast(String msg) {
        baseActivityHelper.showToast(msg);
    }

    @Override
    public void hideSofyKeyboard() {
        baseActivityHelper.hideSofyKeyboard();
    }

    @Override
    public <V extends View> V findView(int id) {
        return baseActivityHelper.findView(id);
    }

    @Override
    public void onHttpResponse(int cmd, String json) {
    }

    @Override
    public Handler getHandler() {
        return baseActivityHelper.getHandler();
    }

    public TextView getTitleTextView() {
        return baseActivityHelper.getTitleTextView();
    }

    @Override
    public <V extends View> V inflateView(int layout) {
        return baseActivityHelper.inflateView(layout);
    }
}
