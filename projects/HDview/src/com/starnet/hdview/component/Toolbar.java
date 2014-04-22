package com.starnet.hdview.component;

import java.util.ArrayList;
import java.util.Iterator;





import com.starnet.hdview.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Toolbar extends HorizontalScrollView {

	private static final int MAX_ITEMCOUNT_ONE_SHEET = 5;
	private LinearLayout mContentLinearLayout;
	private ArrayList<ItemData> mItemDataList;
	private float mItemWidth;
	private OnItemClickListener mOnItemClickListener;
	private ScrollCallBack mScrollCallBack;
	private int mToolbarHeight;
	private View.OnClickListener mToolbarItemClickListener = new View.OnClickListener()
	{
		public void onClick(View view)
		{
			Toolbar.ActionImageButton actionImgBtn = (Toolbar.ActionImageButton)view;
			
			if (Toolbar.this.mOnItemClickListener != null) {
	          Toolbar.this.mOnItemClickListener.onItemClick(actionImgBtn);
	        }
		}
	};
	
	public Toolbar(Context context) {
		super(context);
		init(context);
	}

	public Toolbar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public Toolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
	    super.removeAllViews();
	    this.mContentLinearLayout = new LinearLayout(getContext());
	    this.mContentLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
	    this.mContentLinearLayout.setGravity(Gravity.CENTER_VERTICAL);
	    super.addView(this.mContentLinearLayout, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	 }
	
	
	private void cleanToolbar() {
	    this.mContentLinearLayout.removeAllViews();
	    if (this.mItemDataList != null)
	      this.mItemDataList.clear();
	}
	
	public void createToolbar(ArrayList<ItemData> itemDataList, int width, int height) {
		cleanToolbar();
		
	    this.mToolbarHeight = height;
	    this.mItemDataList = itemDataList;
	    
	    Iterator it;
	    
	    if (this.mItemDataList.size() > MAX_ITEMCOUNT_ONE_SHEET) {
	      this.mItemWidth = ((width + 1) / MAX_ITEMCOUNT_ONE_SHEET);
	    }
	    
	    it = this.mItemDataList.iterator();
	    
	    while (it.hasNext()) {
			ItemData item = (ItemData)it.next();
			ItemLayout itemLayout = new ItemLayout(getContext());
			itemLayout.initContent(item);
			
			LinearLayout.LayoutParams itemlLayoutParams = new LinearLayout.LayoutParams((int)this.mItemWidth, this.mToolbarHeight);
			this.mContentLinearLayout.addView(itemLayout, itemlLayoutParams);
			itemLayout.getActionImageButton().setOnClickListener(this.mToolbarItemClickListener);
	    }
	}
	

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mScrollCallBack != null) {
			this.mScrollCallBack.onScroll(getScrollX(), 0, this.mContentLinearLayout.getMeasuredWidth() - getMeasuredWidth());
		}		
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (mScrollCallBack != null) {
			this.mScrollCallBack.onScroll(getScrollX(), 0, this.mContentLinearLayout.getMeasuredWidth() - getMeasuredWidth());
		}		
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (mScrollCallBack != null) {
			this.mScrollCallBack.onScroll(getScrollX(), 0, this.mContentLinearLayout.getMeasuredWidth() - getMeasuredWidth());
		}		
	}
	

	public void setOnScrollCallBack(ScrollCallBack mScrollCallBack) {
		this.mScrollCallBack = mScrollCallBack;
	}

	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.mOnItemClickListener = onItemClickListener;
	}

	public void setActionImageButtonBg(ACTION_ENUM action, int resid) {
	    ActionImageButton actionImageButton = null;
	    
	    int i;
	    for (i = 0; i < this.mContentLinearLayout.getChildCount() ; i++) {
	      ItemLayout itemLayout = (ItemLayout)this.mContentLinearLayout.getChildAt(i);
	      actionImageButton = itemLayout.getActionImageButton();
	      
	      if (actionImageButton.getItemData().getActionID() == action) {
	    	  actionImageButton.setBackgroundResource(resid);
	      }	      
	    }
	}
	
	public void setActionImageButtonEnabled(ACTION_ENUM action, boolean flag) {
	    ActionImageButton actionImageButton = null;
	    
	    int i;
	    for (i = 0; i < this.mContentLinearLayout.getChildCount() ; i++) {
	      ItemLayout itemLayout = (ItemLayout)this.mContentLinearLayout.getChildAt(i);
	      actionImageButton = itemLayout.getActionImageButton();
	      
	      if (actionImageButton.getItemData().getActionID() == action) {
	    	  actionImageButton.setEnabled(flag);
	      }	      
	    }
	}
	
	public void setActionImageButtonSelected(ACTION_ENUM action, boolean flag) {
	    ActionImageButton actionImageButton = null;
	    
	    int i;
	    for (i = 0; i < this.mContentLinearLayout.getChildCount() ; i++) {
	      ItemLayout itemLayout = (ItemLayout)this.mContentLinearLayout.getChildAt(i);
	      actionImageButton = itemLayout.getActionImageButton();
	      
	      if (actionImageButton.getItemData().getActionID() == action) {
	    	  actionImageButton.setSelected(flag);
	      }	      
	    }
	}
	
	public void setActionItemEnabled(ACTION_ENUM action, boolean flag) {
		setActionImageButtonEnabled(action, flag);
	}
	
	public void setActionItemSelected(ACTION_ENUM action, boolean flag) {
	    ImageView down_arrow = null;
		ActionImageButton actionImageButton = null;
	    
	    int i;
	    for (i = 0; i < this.mContentLinearLayout.getChildCount() ; i++) {
	      ItemLayout itemLayout = (ItemLayout)this.mContentLinearLayout.getChildAt(i);
	      down_arrow = itemLayout.getDownArrow();
	      actionImageButton = itemLayout.getActionImageButton();
	      
	      if (actionImageButton.getItemData().getActionID() == action) {
	    	  down_arrow.setVisibility(flag ? VISIBLE : INVISIBLE);
	    	  actionImageButton.setSelected(flag);
	      }	  
	    }
	}
	
	



	public static enum ACTION_ENUM {
		PLAY_PAUSE,
		PICTURE,
		QUALITY,
		MICROPHONE,
		SOUND,
		VIDEO_RECORD,
		ALARM,
		PTZ
	}
	
	
	public static class ActionImageButton extends ImageButton {
		 private Toolbar.ItemData mItemData;
		
		public ActionImageButton(Context context) {
			super(context);
			
		}

		public ActionImageButton(Context context, AttributeSet attrs,
				int defStyle) {
			super(context, attrs, defStyle);
			
		}

		public ActionImageButton(Context context, AttributeSet attrs) {
			super(context, attrs);
		
		}

		public Toolbar.ItemData getItemData() {
			return mItemData;
		}

		public void setItemData(Toolbar.ItemData mItemData) {
			this.mItemData = mItemData;
		}
	}
	
	public static class ItemData {
		private Toolbar.ACTION_ENUM mActionID;
	    private int mResID;
	    
	    public ItemData(Toolbar.ACTION_ENUM actionID, int resId)
	    {
	      this.mActionID = actionID;
	      this.mResID = resId;
	    }

	    public Toolbar.ACTION_ENUM getActionID()
	    {
	      return this.mActionID;
	    }

	    public int getResID()
	    {
	      return this.mResID;
	    }
	}
	
	
	public static class ItemLayout extends FrameLayout{
		private ImageView down_arrow;
		private Toolbar.ActionImageButton button;
		
		public ItemLayout(Context context) {
			super(context);
			initLayout();
		}

		public ItemLayout(Context context, AttributeSet attrs) {
			super(context, attrs);
			initLayout();
		}
		
		private void initLayout() {
			//super.setGravity(Gravity.CENTER);
			super.setPadding(0, 0, 0, 0);
			//super.setOrientation(VERTICAL);
		}
		
		public void initContent(Toolbar.ItemData itemData)
	    {
		  this.down_arrow = new ImageView(getContext());
		  this.down_arrow.setBackgroundResource(R.drawable.toolbar_quality_sel);
		  this.down_arrow.setVisibility(INVISIBLE);
		  
		  FrameLayout.LayoutParams layout1 = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		  layout1.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		  
		  super.addView(this.down_arrow, layout1);
			
			
	      this.button = new Toolbar.ActionImageButton(getContext());
	      this.button.setBackgroundResource(itemData.getResID());
	      this.button.setItemData(itemData);
	      
	      FrameLayout.LayoutParams layout2 = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	      layout2.gravity = Gravity.CENTER;
	      
	      super.addView(this.button, layout2);
	    }
		
		public Toolbar.ActionImageButton getActionImageButton()
	    {
	      return this.button;
	    }

		public ImageView getDownArrow() {
			return down_arrow;
		}

		
	}
	
	
	
	public static abstract interface OnItemClickListener {
	    public abstract void onItemClick(Toolbar.ActionImageButton imgBtn);
	}

	public static abstract interface ScrollCallBack {
	    public abstract void onScroll(int scrollX, int scrollY, int offset);
	}
}
