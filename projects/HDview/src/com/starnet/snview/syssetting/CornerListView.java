package com.starnet.snview.syssetting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ListView;

import com.starnet.snview.R;

public class CornerListView extends ListView {

	public CornerListView(Context context) {
		super(context);
	}

	public CornerListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CornerListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			int itemnum = pointToPosition(x, y);
			if (itemnum == AdapterView.INVALID_POSITION) {
				break;
			} else {
				if (itemnum == 0) {
					if (itemnum == (getAdapter().getCount() - 1)) {
						// 只有一项
						setSelector(R.drawable.system_setting_cornerlistview_round_copy);
					} else {
						// 第一项
						setSelector(R.drawable.system_setting_cornerlistview_round_top_copy);
					}
				} else if (itemnum == (getAdapter().getCount() - 1))
					// 最后一项
					setSelector(R.drawable.system_setting_cornerlistview_round_bottom_copy);
				else {
					// 中间一项
					setSelector(R.drawable.system_setting_cornerlistview_shape_copy);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}
}
