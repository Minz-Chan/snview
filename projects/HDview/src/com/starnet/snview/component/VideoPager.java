package com.starnet.snview.component;

import com.starnet.snview.R;
import com.starnet.snview.component.Toolbar.ItemLayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class VideoPager extends LinearLayout {
	private static final String TAG = "VideoPager";
	private ImageButton previous;
	private ImageButton next;
	private TextView text;
	private OnActionClickListener actionClickListener;
	
	private int amount;
	private int num;
	private final String seperator = "/";
	
	public static enum ACTION{
		PREVIOUS,
		NEXT
	};
	
	public VideoPager(Context context) {
		super(context);
		init();
	}

	public VideoPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public void init() {
		this.setOrientation(LinearLayout.HORIZONTAL);
	    this.setGravity(Gravity.CENTER);
	    this.setBaselineAligned(false);
	}
	
	public void initContent(int width, int height) {
		int itemWidth = width / 5;
		LinearLayout.LayoutParams itemLayoutParams = new LinearLayout.LayoutParams(itemWidth, height);
		LinearLayout.LayoutParams itemContentLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		LinearLayout previousLinearLayout = new LinearLayout(getContext());
		LinearLayout textLinearLayout = new LinearLayout(getContext());
		LinearLayout nextLinearLayout = new LinearLayout(getContext());
		
		super.addView(new LinearLayout(getContext()), itemLayoutParams);
		
		
		previous = new ImageButton(getContext());
		previous.setBackgroundResource(R.drawable.pager_bar_previous_selector);
		previous.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (actionClickListener != null) {
					actionClickListener.OnActionClick(v, ACTION.PREVIOUS);
				}				
			}
			
		});
		
		previousLinearLayout.setGravity(Gravity.CENTER);
		previousLinearLayout.setPadding(0, 0, 0, 0);
		previousLinearLayout.setOrientation(VERTICAL);
		previousLinearLayout.addView(previous, itemContentLayoutParams);
		super.addView(previousLinearLayout, itemLayoutParams);

		
		text = new TextView(getContext());
		text.setGravity(Gravity.CENTER);
		text.setText("");
		
		textLinearLayout.setGravity(Gravity.CENTER);
		textLinearLayout.setPadding(0, 0, 0, 0);
		textLinearLayout.setOrientation(VERTICAL);
		textLinearLayout.addView(text, 
				new LinearLayout.LayoutParams(itemWidth, height));
		super.addView(textLinearLayout, itemLayoutParams);
		
		next = new ImageButton(getContext());
		next.setBackgroundResource(R.drawable.pager_bar_next_selector);
		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (actionClickListener != null) {
					actionClickListener.OnActionClick(v, ACTION.NEXT);
				}				
			}
			
		});
		
		nextLinearLayout.setGravity(Gravity.CENTER);
		nextLinearLayout.setPadding(0, 0, 0, 0);
		nextLinearLayout.setOrientation(VERTICAL);
		nextLinearLayout.addView(next, itemContentLayoutParams);
		super.addView(nextLinearLayout, itemLayoutParams);
		
		super.addView(new LinearLayout(getContext()), itemLayoutParams);
		
	}
	public void setOnActionClickListener(OnActionClickListener actionClickListener) {
		this.actionClickListener = actionClickListener;
	}


	public ImageButton getPrevious() {
		return previous;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
		OnContentChanged();
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
		OnContentChanged();
	}
	
	public String getPagerText() {
		return text.getText().toString();
	}
	
	private void OnContentChanged() {
		text.setText(num + seperator + amount);
		Log.d(TAG, "Pager text: " + text.getText().toString());
	}
	
	public static abstract interface OnActionClickListener {
		public abstract void OnActionClick(View v, VideoPager.ACTION action);
	}
}
