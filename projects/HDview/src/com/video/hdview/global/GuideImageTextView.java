package com.video.hdview.global;

import com.video.hdview.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GuideImageTextView extends RelativeLayout {

	private ImageView imageView;
	private TextView textView;
	
	public GuideImageTextView(Context context) {
		super(context);
		View view = LayoutInflater.from(context).inflate(R.layout.guideactivity_self, this,true);
		imageView = (ImageView) view.findViewById(R.id.guide_imgeview);
		textView = (TextView) view.findViewById(R.id.guide_experiment_txt);
	}
	
	public void setViewInVisible(){
		imageView.setVisibility(View.GONE);
		textView.setVisibility(View.GONE);
	}
	
	public void setTextInVisible(){
		textView.setVisibility(View.GONE);
	}
	
	public void setBackGround(int resid){
		imageView.setBackgroundResource(resid);
	}
	
	public void setImageResource(int resid){
		imageView.setImageResource(resid);
	}
	
	public void setText(String text){
		textView.setText(text);
	}
	
	public TextView getTextView(){
		return textView;
	}
}