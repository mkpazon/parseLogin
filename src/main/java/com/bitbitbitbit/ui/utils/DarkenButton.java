package com.bitbitbitbit.ui.utils;

import android.content.Context;
import android.graphics.LightingColorFilter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

public class DarkenButton extends Button {

	public DarkenButton(Context context) {
		super(context);
	}

	public DarkenButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DarkenButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			getBackground().setColorFilter(
					new LightingColorFilter(0xff888888, 0x000000));
		} else if (event.getAction() == MotionEvent.ACTION_UP
				|| event.getAction() == MotionEvent.ACTION_CANCEL) {
			getBackground().setColorFilter(null);

		}

		return super.onTouchEvent(event);
	}
}