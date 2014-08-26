package com.starnet.snview.images;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.images.utils.ImagePreviewGestureListener;

/**
 * 
 * @author 赵康
 * @date 2014-08-22
 * 
 */
public class ImagePreviewActivity extends BaseActivity{//implements OnGestureListener
	
	private final static String TAG = "ImagePreviewActivity";
	private GestureDetector gestureDetector;//滑动相关
	private ViewFlipper flipper;//滑动界面的填充器... 
	int showSum ;//导航栏中总数
	int showNum ;//导航栏中第几幅画面
	ArrayList<String> pathList;//画面的路径...
	
	private Context context;

	private Button imagepreview_delete_btn;// 图片删除按钮
	private ImageView imagepreview_imageView;// 预览的图片
	private TextView imagepreview_title_image_num;// 显示设备的数量，以及显示

	private Button imagepreview_leftBtn;// 返回按钮

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_manager_imagepreview_activity);

		super.setLeftButtonBg(R.drawable.image_manager_imagepreview_back_selector);
		super.getRightButton().setVisibility(View.GONE);
		super.setToolbarVisiable(false);
		super.hideRightButton();
		super.hideExtendButton();

		imagepreview_leftBtn = super.getLeftButton();
		imagepreview_title_image_num = super.getTitleView();

		imagepreview_delete_btn = (Button) findViewById(R.id.imagepreview_delete_btn);
		imagepreview_imageView = (ImageView) findViewById(R.id.imagepreview_imageView);
		
		flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		
		Intent intent = getIntent();
		if (intent != null) {
			String imgPosInMap = intent.getStringExtra("imgPosInMap");
			String sumMap = intent.getStringExtra("sumMap");
			String imagePath = intent.getStringExtra("imagePath");
			pathList = intent.getStringArrayListExtra("pathList");
			Bitmap mBitmap = BitmapFactory.decodeFile(imagePath);
			imagepreview_imageView.setImageBitmap(mBitmap);//暂时
			showSum = Integer.valueOf(sumMap);
			showNum = Integer.valueOf(imgPosInMap);
			imagepreview_title_image_num.setText("("+showNum+"/"+showSum+")");// 测试使用...
			Log.v(TAG, imagePath);
			int image_size = pathList.size();
			for (int i = 0; i < image_size; i++) {
				flipper.addView(getImageView(pathList.get(i)));//为flipper添加界面数据...
			}
		}
		setListenersForWadgets();
	}
	//从指定的路径处获取视图...
	private View getImageView(String imagePath ) {
		ImageView imageView = new ImageView(this);
		Drawable drawable = Drawable.createFromPath(imagePath);
        imageView.setImageDrawable(drawable);
        return imageView;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void setListenersForWadgets() {
		context = ImagePreviewActivity.this;
		ImagePreviewGestureListener ipgl = new ImagePreviewGestureListener(flipper, showNum, showSum, imagepreview_title_image_num, context);
		gestureDetector = new GestureDetector(context, ipgl);
		
		imagepreview_leftBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImagePreviewActivity.this.finish();
			}
		});
		
		imagepreview_imageView.setOnTouchListener(null);// 图片的缩放功能...
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int pointer = event.getPointerCount();//判断有几根手指触屏，若是有2个及以上，则调用图片的缩放操作；否则，进行滑动操作...
		if (pointer == 1) {//如果等于1，则证明是滑动操作
			String text = "你要做huadong操作...";
			Toast.makeText(ImagePreviewActivity.this, text, Toast.LENGTH_SHORT).show();
			return gestureDetector.onTouchEvent(event);
		}else {//否则，证明是缩放操作
			String text = "你要做缩放操作...";
			Toast.makeText(ImagePreviewActivity.this, text, Toast.LENGTH_SHORT).show();
			return imagepreview_imageView.onTouchEvent(event);
		}
	}
}