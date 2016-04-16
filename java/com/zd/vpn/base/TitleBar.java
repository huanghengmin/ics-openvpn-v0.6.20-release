package com.zd.vpn.base;

import android.view.View;
import android.view.View.OnClickListener;

public class TitleBar {

	private OnClickListener mLeftBtnOnClickListener;
	private String mTitleValue;
	private View mRightContainerLayout;

	public TitleBar(
			OnClickListener leftBtnOnClickListener,
			String titleValue, View rightContainerLayout) {
		mLeftBtnOnClickListener = leftBtnOnClickListener;
		mTitleValue = titleValue;
		mRightContainerLayout = rightContainerLayout;
	}

	public OnClickListener getmLeftBtnOnClickListener() {
		return mLeftBtnOnClickListener;
	}

	public void setmLeftBtnOnClickListener(
			OnClickListener mLeftBtnOnClickListener) {
		this.mLeftBtnOnClickListener = mLeftBtnOnClickListener;
	}

	public String getmTitleValue() {
		return mTitleValue;
	}

	public void setmTitleValue(String mTitleValue) {
		this.mTitleValue = mTitleValue;
	}

	public View getmRightContainerLayout() {
		return mRightContainerLayout;
	}

	public void setmRightContainerLayout(View mRightContainerLayout) {
		this.mRightContainerLayout = mRightContainerLayout;
	}

}
