package net.smallchat.im.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import net.smallchat.im.R;

/**
 * 重新scrollview 避免冲突
 * @author dongli
 *
 */
public class LazyScrollView extends ScrollView {
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(onScrollListener != null){
			onScrollListener.onAutoScroll(l, t, oldl, oldt);
		}
	}

	public static final String TAG = "LazyScrollView";
	private Handler handler;
	private View view;
	
	private LinearLayout innerLayout;

	public LazyScrollView(Context context) {
		super(context);
		init(context);
	}

	public LazyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LazyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	// 这个获得总的高度
	public int computeVerticalScrollRange() {
		return super.computeHorizontalScrollRange();
	}

	public int computeVerticalScrollOffset() {
		return super.computeVerticalScrollOffset();
	}

	private void init() {

		this.setOnTouchListener(onTouchListener);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// process incoming messages here
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
					if (view.getMeasuredHeight() - 20 <= getScrollY()
							+ getHeight()) {
						if (onScrollListener != null) {
							onScrollListener.onBottom();
						}

					} else if (getScrollY() == 0) {
						if (onScrollListener != null) {
							onScrollListener.onTop();
						}
					} else {
						if (onScrollListener != null) {
							onScrollListener.onScroll();
						}
					}
					break;
				default:
					break;
				}
			}
		};

	}

	OnTouchListener onTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_UP:
				if (view != null && onScrollListener != null) {
					handler.sendMessageDelayed(handler.obtainMessage(1), 200);
				}
				break;

			default:
				break;
			}
			return false;
		}

	};

	/**
	 * 获得参考的View，主要是为了获得它的MeasuredHeight，然后和滚动条的ScrollY+getHeight作比较。
	 */
	public void getView() {
		this.view = getChildAt(0);
		if (view != null) {
			init();
		}
	}

	/**
	 * 定义接口
	 * 
	 * @author admin
	 * 
	 */
	public interface OnScrollListener {
		void onBottom();

		void onTop();

		void onScroll();

		void onAutoScroll(int l, int t, int oldl, int oldt);
	}

	private OnScrollListener onScrollListener;

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}
	
	private void init(Context context) {
		//LayoutInflater inflater = LayoutInflater.from(context);
		innerLayout = new LinearLayout(context);
		innerLayout.setBackgroundColor(context.getResources().getColor(R.color.backgroud_color));//(R.drawable.page_bg);
		innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT));
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		
		
		addView(innerLayout);
	}
	
	public LinearLayout getLayout(){
		return innerLayout;
	}
}
