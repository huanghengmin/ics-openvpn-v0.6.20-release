package com.zd.vpn.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zd.vpn.R;


public class RoundCornerItem extends LinearLayout {

	final static int TOP = 0;
	final static int CENTER = 1;
	final static int BOTTOM = 2;
	final static int ALL = 3;

	Drawable iconDrawable;
	String contentString;
	Drawable rightDrawable;
	int roundCornerItemDirection;

	ImageView icon;
	TextView content;
	ImageView arrow;

	public RoundCornerItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.roundCornerItem);
		iconDrawable = ta
				.getDrawable(R.styleable.roundCornerItem_roundCornerItemIcon);
		contentString = ta
				.getString(R.styleable.roundCornerItem_roundCornerItemContent);
		roundCornerItemDirection = ta.getInt(
				R.styleable.roundCornerItem_roundCornerItemDirection, ALL);
		rightDrawable = ta
				.getDrawable(R.styleable.roundCornerItem_roundCornerItemRightDrawable);
		ta.recycle();

		setClickable(true);
		setOrientation(VERTICAL);
	}

	public void setContent(String contentValue) {
		content.setText(contentValue);
	}

    public String getContent(){
        return content.getText().toString();
    }

	public void setRightDrawable(int resId) {
		arrow.setImageResource(resId);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		View root = LayoutInflater.from(getContext()).inflate(
				R.layout.round_corner_item, null);
		addView(root, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		icon = (ImageView) findViewById(R.id.icon);
		content = (TextView) findViewById(R.id.content);
		arrow = (ImageView) findViewById(R.id.arrow);

		if (iconDrawable != null) {
			icon.setImageDrawable(iconDrawable);
		}
		if (!TextUtils.isEmpty(contentString)) {
			content.setText(contentString);
		}
		if (rightDrawable == null) {
			arrow.setImageResource(R.drawable.me_arrow);
		} else {
			arrow.setImageDrawable(rightDrawable);
		}

		switch (roundCornerItemDirection) {
		case TOP:
			setBackgroundResource(R.drawable.round_corner_header_bg);
			break;
		case CENTER:
			setBackgroundResource(R.drawable.round_corner_center_bg);
			break;
		case BOTTOM:
			setBackgroundResource(R.drawable.round_corner_bottom_bg);
			break;
		case ALL:
			setBackgroundResource(R.drawable.round_corner_one_bg);
			break;
		default:
			break;
		}

	}
}
