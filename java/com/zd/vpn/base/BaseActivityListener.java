package com.zd.vpn.base;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public interface BaseActivityListener {

	public SharedPreferences getDefaultPref();

	public Context getContext();

	public int rootViewRes();

	public void showToast(String msg);

	public void showProgressDialog(String msg);

	public void showAlertDialog(String title, String message,
                                String positiveValue, OnClickListener linstener);

	public void showChoiceDialog(String title, String content,
                                 String positiveValue,
                                 OnClickListener positiveListener,
                                 String nagetiveValue,
                                 OnClickListener nagetiveListener);

	public void showItemsDialog(String title, String[] items,
                                OnClickListener listener);

	public void hideProgressDialog();

	public void doOnCreate(Bundle savedInstanceState);

	public TitleBar initTitlebar();

	public void hideSofyKeyboard();

	public void onHttpResponse(int cmd, String json);

	public <V extends View> V findView(int id);

	public Handler getHandler();

	public <V extends View> V inflateView(int layout);

}
