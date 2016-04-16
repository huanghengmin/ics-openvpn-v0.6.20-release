package com.zd.vpn.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.zd.vpn.R;
import com.zd.vpn.base.TitleBar;

public class TitlebarFactory {

	/**
	 * @param parent
	 * @param title
	 * @return 只有返回按钮的Titlebar
	 */
	public static TitleBar createCustomBackTitlebar(final Activity parent,
			String title) {
		TitleBar result = new TitleBar(new OnClickListener() {

			@Override
			public void onClick(View v) {
				parent.finish();
			}
		}, title, null);
		return result;
	}

	/**
	 * @param parent
	 * @param title
	 * @return 只有返回按钮的Titlebar,返回事件是finish页面
	 */
	public static TitleBar createCustomBackTitlebar(final Activity parent,
			String title, final OnClickListener backListener) {
		TitleBar result = new TitleBar(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backListener.onClick(v);
			}
		}, title, null);
		return result;
	}

	/**
	 * 
	 * @param parent
	 * @param title
	 * @return 右边有文字按钮的TitleBar
	 */
	public static TitleBar createRightTextBtnTitlebar(final Activity parent,
			String title, String rightButtonValue, OnClickListener listener) {

		LayoutInflater inflater = LayoutInflater.from(parent);
		Button btn = (Button) inflater.inflate(R.layout.custom_right_btn, null);
		btn.setText(rightButtonValue);
		btn.setOnClickListener(listener);

		TitleBar result = new TitleBar(new OnClickListener() {

			@Override
			public void onClick(View v) {
				parent.finish();
			}
		}, title, btn);

		return result;
	}

	/**
	 * 
	 * @param parent
	 * @param title
	 * @return 右边有文字按钮的TitleBar
	 */
	public static TitleBar createRightTextBtnTitlebar(final Activity parent,
			String title, String rightButtonValue,
			final OnClickListener backListener, OnClickListener listener) {

		LayoutInflater inflater = LayoutInflater.from(parent);
		Button btn = (Button) inflater.inflate(R.layout.custom_right_btn, null);
		btn.setText(rightButtonValue);
		btn.setOnClickListener(listener);

		TitleBar result = new TitleBar(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backListener.onClick(v);
			}
		}, title, btn);

		return result;
	}

	/**
	 * 
	 * @param parent
	 * @param title
	 * @return 右边有图标的Titlebar
	 */
	public static TitleBar createRightIconBtnTitlebar(final Activity parent,
			String title, int rightButtonRes, OnClickListener listener) {

		ImageButton btn = (ImageButton) LayoutInflater.from(parent).inflate(
				R.layout.custom_right_icon_btn, null);
		btn.setImageResource(rightButtonRes);
		btn.setOnClickListener(listener);

		TitleBar result = new TitleBar(new OnClickListener() {

			@Override
			public void onClick(View v) {
				parent.finish();
			}
		}, title, btn);

		return result;
	}

	/**
	 * 
	 * @param parent
	 * @param title
	 * @return 右边有图标的Titlebar
	 */
	public static TitleBar createRightIconBtnTitlebar(final Activity parent,
			String title, int rightButtonRes,
			final OnClickListener backListener, OnClickListener listener) {

		ImageButton btn = (ImageButton) LayoutInflater.from(parent).inflate(
				R.layout.custom_right_icon_btn, null);
		btn.setImageResource(rightButtonRes);
		btn.setOnClickListener(listener);

		TitleBar result = new TitleBar(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backListener.onClick(v);
			}
		}, title, btn);

		return result;
	}

	/**
	 * @param parent
	 * @param title
	 * @param rightLayoutRes
	 * @return 右边是一个布局的Titlebar
	 */
	public static TitleBar createRightLayoutTitlebar(final Activity parent,
			String title, int rightLayoutRes) {

		TitleBar result = new TitleBar(new OnClickListener() {

			@Override
			public void onClick(View v) {
				parent.finish();
			}
		}, title, LayoutInflater.from(parent).inflate(rightLayoutRes, null));

		return result;
	}

	/**
	 * @param parent
	 * @param title
	 * @param rightLayoutRes
	 * @return 右边是一个布局的Titlebar
	 */
	public static TitleBar createRightLayoutTitlebar(final Activity parent,
			String title, int rightLayoutRes, final OnClickListener backListener) {

		TitleBar result = new TitleBar(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backListener.onClick(v);
			}
		}, title, LayoutInflater.from(parent).inflate(rightLayoutRes, null));
		return result;
	}
}
