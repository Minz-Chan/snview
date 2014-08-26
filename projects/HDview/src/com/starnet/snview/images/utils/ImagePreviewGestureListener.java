package com.starnet.snview.images.utils;

import com.starnet.snview.R;

import android.content.Context;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ImagePreviewGestureListener implements OnGestureListener {
	
	private ViewFlipper flipper ;//视图翻页器...
	private int showNum;//显示画面的序数
	private int showSum;//显示画面的总数
	private TextView imagepreview_title_image_num;//显示画面的翻页信息
	private Context context;//所在的上下文信息(activity/application)
	
	public ImagePreviewGestureListener(ViewFlipper flipper, int showNum,
			int showSum, TextView imagepreview_title_image_num, Context context) {
		super();
		this.flipper = flipper;
		this.showNum = showNum;
		this.showSum = showSum;
		this.imagepreview_title_image_num = imagepreview_title_image_num;
		this.context = context;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		float endx = e2.getX();
		float startx = e1.getX();
		if (endx - startx > 30) {
			Animation rInAnim = AnimationUtils.loadAnimation(context, R.anim.push_right_in); 	// 向右滑动左侧进入的渐变效果（alpha  0.1 -> 1.0）
			Animation rOutAnim = AnimationUtils.loadAnimation(context, R.anim.push_right_out); // 向右滑动右侧滑出的渐变效果（alpha 1.0  -> 0.1）
		
			flipper.setInAnimation(rInAnim);
			flipper.setOutAnimation(rOutAnim);
			flipper.showPrevious();//展示前一个画面
			showNum--;
			if (showNum < 1) {
				showNum = showSum;
			}
//			int loadNum = showNum;
//			if ((loadNum < showSum)&&(loadNum >= 0)) {
//				String cur_path = pathList.get(loadNum);
//				flipper.addView(getImageView(cur_path));
//			}
			imagepreview_title_image_num.setText("("+showNum+"/"+showSum+")");// 测试使用...
			
			
			return true;
		}else if (endx - startx < -30) {
			Animation lInAnim = AnimationUtils.loadAnimation(context, R.anim.push_left_in);		// 向左滑动左侧进入的渐变效果（alpha 0.1  -> 1.0）
			Animation lOutAnim = AnimationUtils.loadAnimation(context, R.anim.push_left_out); 	// 向左滑动右侧滑出的渐变效果（alpha 1.0  -> 0.1)
			flipper.setInAnimation(lInAnim);
			flipper.setOutAnimation(lOutAnim);
			flipper.showNext();//展示下一个画面
			showNum++;
			if (showNum > showSum) {
				showNum = 1;
			}
			
			imagepreview_title_image_num.setText("("+showNum+"/"+showSum+")");// 测试使用...
			
			return true;
		}
		return true;
	}

}
