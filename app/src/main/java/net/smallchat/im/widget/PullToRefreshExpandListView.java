package net.smallchat.im.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.FeatureFunction;

public class PullToRefreshExpandListView extends LinearLayout{

	public static final int STATE_IDLE = 0;
	public static final int STATE_PULL = 1;
	public static final int STATE_RELEASE = 2;
	public static final int STATE_LOADING = 3;
	private int REFRESH_VIEW_HEIGHT = 60;
	private int mCurRefreshViewHeight = 48;
	private int TOP_LAYOUTHEIGHT = 48;
	private boolean mCurReleaseState;
	private RotateAnimation mFlipAnimation;
	private float mInterceptY;
	private int mLastMotionY;
	private ExpandableListView mList;
	private RelativeLayout mRefreshView;
	private ImageView mRefreshViewImage;
	private TextView mRefreshViewText;
	private RotateAnimation mReverseFlipAnimation;
	private boolean mScrollingList = false;
	private int mState;
	private ProgressBar mRefreshProgress;
	private OnChangeStateListener mOnChangeStateListener;
	private int now;
	private Context mContext;

	public PullToRefreshExpandListView(Context context) {
		super(context);
		init(context);
	}

	public PullToRefreshExpandListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		
	}

	private void applyHeaderHeight(MotionEvent ev) {
		final int historySize = ev.getHistorySize();

		if (historySize > 0) {
			for (int h = 0; h < historySize; h++) {
				int historicalY = (int) (ev.getHistoricalY(h));
				updateRefreshView((historicalY - mLastMotionY) / 2);
				now = (historicalY - mLastMotionY) / 2;
			}

		} else {
			int historicalY = (int) ev.getY();
			updateRefreshView((historicalY - mLastMotionY) / 2);
			now = (historicalY - mLastMotionY) / 2;
		}

	}

	private void changeRefreshView(boolean paramBoolean)
	  {
	    if (mCurReleaseState != paramBoolean)
	    {
	      mRefreshViewImage.setImageResource(R.drawable.arrow_down);
	      mRefreshViewImage.clearAnimation();
	      mRefreshViewImage.startAnimation(mReverseFlipAnimation);
	    }else{
	    
		  mCurReleaseState = paramBoolean;   
		  mRefreshViewImage.clearAnimation();
	      mRefreshViewImage.startAnimation(mFlipAnimation);
	      
	    }
	   
	  }
	
	private void changeState(int state) {
		switch (state) {
		case STATE_IDLE:
			//mRefreshViewText.setText("");
			setRefreshViewHeight(1);
			break;
		case STATE_PULL:
			mRefreshViewImage.setVisibility(View.VISIBLE);
			mRefreshProgress.setVisibility(View.GONE);
			 mRefreshViewText.setText(ChatApplication.getInstance().getResources().getString(R.string.continuetorefresh));
			changeRefreshView(true);
			break;
		case STATE_RELEASE:
			mRefreshViewImage.setVisibility(View.VISIBLE);
			mRefreshProgress.setVisibility(View.GONE);
			 mRefreshViewText.setText(ChatApplication.getInstance().getResources().getString(R.string.starttorefresh));
			changeRefreshView(false);
			break;
		case STATE_LOADING:
			mRefreshViewImage.setVisibility(View.GONE);
			mRefreshProgress.setVisibility(View.VISIBLE);
			mRefreshViewText.setText(ChatApplication.getInstance().getResources().getString(R.string.refreshloading));
			setRefreshViewHeight(100);
			break;
		}
		mState = state;

		notifyStateChanged();
	}

	private void init(Context context) {
		mState = STATE_IDLE;
		this.mContext =context;
		float densityFactor = context.getResources().getDisplayMetrics().density;
		REFRESH_VIEW_HEIGHT *= densityFactor;

		setVerticalFadingEdgeEnabled(false);
		setVerticalScrollBarEnabled(false);

		mFlipAnimation = new RotateAnimation(0.0F, -180.0F, 1, 0.5F, 1, 0.5F);
		mFlipAnimation.setDuration(250L);
		mFlipAnimation.setFillAfter(true);
		
		mReverseFlipAnimation = new RotateAnimation(-180.0F, 0.0F, 1, 0.5F, 1,0.5F);
		mReverseFlipAnimation.setDuration(250L);
		mReverseFlipAnimation.setFillAfter(true);
	}

	public void setRefreshViewHeight(int height) {

		if (height == 1) {
			//mRefreshView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,(int) (mContext.getResources().getDisplayMetrics().density*TOP_LAYOUTHEIGHT)));
			mRefreshView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, FeatureFunction.dip2px(mContext, 48)));
		} else {
			mCurRefreshViewHeight = height;
			mRefreshView.setLayoutParams(new LayoutParams(
					LayoutParams.FILL_PARENT, (int) (mContext.getResources().getDisplayMetrics().density*TOP_LAYOUTHEIGHT+height)));
					//LayoutParams.FILL_PARENT, height));
			/*Log.e("PullListView", "height= " + height);
			mRefreshView.setPadding(  
                    mRefreshView.getPaddingLeft(),  
                    (int)mContext.getResources().getDisplayMetrics().density*TOP_LAYOUTHEIGHT+height,  
                    mRefreshView.getPaddingRight(),  
                    mRefreshView.getPaddingBottom());*/
		}

	}

	private void updateRefreshView(int height) {
		if (height <= 0) {
			return;
		}
		if ((REFRESH_VIEW_HEIGHT / 4 <= mCurRefreshViewHeight)
				&& (mCurRefreshViewHeight < REFRESH_VIEW_HEIGHT)) {
			setRefreshViewHeight(height);
			
			if(mState == STATE_IDLE){
				 mRefreshViewText.setText(R.string.continuetorefresh);
				 mState = STATE_PULL;
				 mRefreshViewImage.setVisibility(View.VISIBLE);
				 mRefreshProgress.setVisibility(View.GONE);
			//	 notifyStateChanged();
			}else if(mState!=STATE_PULL&&mState != STATE_IDLE){
				changeState(STATE_PULL);
			}
		} else if (mCurRefreshViewHeight > REFRESH_VIEW_HEIGHT) {
			if (height > REFRESH_VIEW_HEIGHT) {
				height = (int) (REFRESH_VIEW_HEIGHT + (height - REFRESH_VIEW_HEIGHT)
						* REFRESH_VIEW_HEIGHT * 2.45f / height);
			}
			
			setRefreshViewHeight(height);
			if(mState!=STATE_RELEASE)
				changeState(STATE_RELEASE);
		} else {
			setRefreshViewHeight(height);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			break;
		case MotionEvent.ACTION_CANCEL:
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		float oldLastY = mInterceptY;
		mInterceptY = ev.getY();
	//	System.out.println("拦截");
		if (mState == STATE_LOADING) {
			return false;
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
		//	System.out.println("onInterceptTouchEvent.ACTION_DOWN");
			mLastMotionY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
		//	System.out.println("onInterceptTouchEvent.ACTION_MOVE");
			if (mList.getFirstVisiblePosition() == 0
					&& (mList.getChildCount() == 0 || mList.getChildAt(0)
							.getTop() == 0)) {
				if ((mInterceptY - oldLastY > 5) || (mState == STATE_PULL)
						|| (mState == STATE_RELEASE)) {
					mScrollingList=true;
					break;
				} else {
					break;
				}
			}else {
				break;
			}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			System.out.println("onInterceptTouchEvent.ACTION_CANCEL");
			break;
		}
		if(mScrollingList){
			mScrollingList=!mScrollingList;
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		//Log.e("PullListView", "onTouchEvent");
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
		//	System.out.println("onTouchEvent.ACTION_DOWN");
			break;
		case MotionEvent.ACTION_MOVE:
		//	System.out.println("onTouchEvent.ACTION_MOVE");
			applyHeaderHeight(ev);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			System.out.println("onTouchEvent.ACTION_CANCEL--"+now+"--"+mCurRefreshViewHeight);
			if (mState == STATE_RELEASE) {
				TranslateAnimation am = new TranslateAnimation(0, 0, 
						mCurRefreshViewHeight/mContext.getResources().getDisplayMetrics().density,0);
				am.setInterpolator(AnimationUtils.loadInterpolator(
						getContext(),
						android.R.anim.accelerate_interpolator));
				am.setDuration(300);
				startAnimation(am);
				mList.setFocusable(false);
				mList.setSelected(false);
				mRefreshViewImage.clearAnimation();
				refresh();
			}else if(mState==STATE_PULL){
				TranslateAnimation am = new TranslateAnimation(0, 0, now, 0);
				am.setDuration(200l);
				am.setInterpolator(AnimationUtils.loadInterpolator(
						getContext(),
						android.R.anim.accelerate_decelerate_interpolator));
				startAnimation(am);
				changeState(STATE_IDLE);
			}
			else{
				changeState(STATE_IDLE);
			}
			break;
		}
		return true;
	}


	public ExpandableListView getList() {
		return mList;
	}

	protected void onFinishInflate() {
		mRefreshView = (RelativeLayout) findViewById(R.id.refresh_view);
		mRefreshViewText = (TextView) findViewById(R.id.pull_to_refresh_text);
		mRefreshViewImage = (ImageView) findViewById(R.id.pull_to_refresh_image);
		mRefreshProgress = (ProgressBar) findViewById(R.id.pull_to_refresh_progress);
		mRefreshViewImage.setImageResource(R.drawable.arrow_down);
		mList = (ExpandableListView) findViewById(R.id.lv_my_album);
		mList.setSelected(false);
		mList.setFocusable(false);
		
	}

	private void notifyStateChanged() {
		if (mOnChangeStateListener != null) {
			mOnChangeStateListener.onChangeState(this, mState);
		}
	}
	
	public void onRefreshComplete() {
		mList.setFocusable(true);
		mList.setSelected(true);

		changeState(STATE_IDLE);
		TranslateAnimation am = new TranslateAnimation(0, 0, 80, 1);
		am.setDuration(600l);
		am.setInterpolator(AnimationUtils.loadInterpolator(getContext(),
				android.R.anim.accelerate_decelerate_interpolator));
		startAnimation(am);
		
	}

	public void refresh() {
		changeState(STATE_LOADING);
	}

	public void clickrefresh() {
//		TranslateAnimation am = new TranslateAnimation(0, 0, -80, 0);
//		am.setDuration(600l);
//		am.setInterpolator(AnimationUtils.loadInterpolator(getContext(),
//				android.R.anim.accelerate_decelerate_interpolator));
//		startAnimation(am);
//		changeState(STATE_LOADING);
		changeState(STATE_RELEASE);
		if (mState == STATE_RELEASE) {
			TranslateAnimation am = new TranslateAnimation(0, 0, 
					mCurRefreshViewHeight/mContext.getResources().getDisplayMetrics().density,0);
			am.setInterpolator(AnimationUtils.loadInterpolator(
					getContext(),
					android.R.anim.accelerate_interpolator));
			am.setDuration(300);
			startAnimation(am);
			mList.setFocusable(false);
			mList.setSelected(false);
			mRefreshViewImage.clearAnimation();
			refresh();
		}
	}

	public void setOnChangeStateListener(OnChangeStateListener listener) {
		mOnChangeStateListener = listener;
	}

	public interface OnChangeStateListener {
		public void onChangeState(PullToRefreshExpandListView container, int state);
	}


}
