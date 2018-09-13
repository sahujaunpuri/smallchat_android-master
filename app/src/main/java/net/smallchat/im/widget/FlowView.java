package net.smallchat.im.widget;



import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import net.smallchat.im.R;
import net.smallchat.im.global.FeatureFunction;


public class FlowView extends ImageView{

	/*private String namespace = "http://shadow.com";
	private int color;
	*/
	//private AnimationDrawable loadingAnimation;
	private FlowTag flowTag;
	private Context context;
	public Bitmap bitmap;
	//private ImageLoaderTask task;
	private int columnIndex;// 图片属于第几列
	private int rowIndex;// 图片属于第几行
	private Handler viewHandler;

	public FlowView(Context c, AttributeSet attrs, int defStyle) {
		super(c, attrs, defStyle);
		this.context = c;
		Init();
	}

	public FlowView(Context c, AttributeSet attrs) {
		super(c, attrs);
	//	color=Color.parseColor(attrs.getAttributeValue(namespace,"BorderColor"));
		this.context = c;
		Init();
	}

	public FlowView(Context c) {
		super(c);
		this.context = c;
		Init();
	}

	private void Init() {

		//setOnClickListener(this);
		//this.setOnLongClickListener(this);
		setAdjustViewBounds(true);
		setScaleType(ScaleType.FIT_XY);

	}

	
	
	/*@Override
	public boolean onLongClick(View v) {
		Log.d("FlowView", "LongClick");
		Toast.makeText(context, "长按：" + this.flowTag.getFlowId(),
				Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public void onClick(View v) {
		Log.d("FlowView", "Click");
		Toast.makeText(context, "单击：" + this.flowTag.getFlowId(),
				Toast.LENGTH_SHORT).show();
	}*/

/*	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Rect rec=canvas.getClipBounds();
		rec.bottom--;
		rec.right--;
		Paint paint=new Paint();
		paint.setColor(color);
		
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(rec, paint);
	}*/

	/**
	 * 加载图片
	 */
	public void LoadImage() {
		if (getFlowTag() != null) {

			new LoadImageThread().start();
		}
	}

	/**
	 * 重新加载图片
	 */
	public void Reload() {
		if (this.bitmap == null && getFlowTag() != null) {

			new ReloadImageThread().start();
		}
	}

	/**
	 * 回收内存
	 */
	public void recycle() {
		setImageBitmap(null);
		if ((this.bitmap == null) || (this.bitmap.isRecycled()))
			return;
		this.bitmap.recycle();
		this.bitmap = null;
	}

	public FlowTag getFlowTag() {
		if(flowTag == null){
			Log.e("flowTag", "flowTag is null");
		}
		return flowTag;
	}

	public void setFlowTag(FlowTag flowTag) {
		this.flowTag = flowTag;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public Handler getViewHandler() {
		return viewHandler;
	}

	public FlowView setViewHandler(Handler viewHandler) {
		this.viewHandler = viewHandler;
		return this;
	}

	class ReloadImageThread extends Thread {

		@Override
		public void run() {
			if (flowTag != null) {

				bitmap= FeatureFunction.downLoadImage(flowTag.getThumbPicUrl());

				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						if (bitmap==null) {
							bitmap=BitmapFactory.decodeResource(context.getResources(),R.drawable.noraml_album); 
						}
						if (bitmap != null) {// 此处在线程过多时可能为null
							setImageBitmap(bitmap);
						}
					}
				});
			}

		}
	}

	class LoadImageThread extends Thread {
		LoadImageThread() {
		}

		public void run() {

			if (flowTag != null) {

				bitmap= FeatureFunction.downLoadImage(flowTag.getThumbPicUrl());
				//bitmap = BitmapFactory.decodeStream(buf);

				// 此处不能直接更新UI，否则会发生异常：
				// CalledFromWrongThreadException: Only the original thread that
				// created a view hierarchy can touch its views.
				// 也可以使用Handler或者Looper发送Message解决这个问题

				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						if (bitmap==null) {
							bitmap=BitmapFactory.decodeResource(context.getResources(),R.drawable.default_image);
						}
						if (bitmap != null) {// 此处在线程过多时可能为null
							int width = bitmap.getWidth();// 获取真实宽高
							int height = bitmap.getHeight();

							LayoutParams lp = getLayoutParams();

							int layoutHeight = (height * flowTag.getItemWidth())
									/ width;// 调整高度
							if (lp == null) {
								lp = new LayoutParams(flowTag.getItemWidth(),
										layoutHeight);
							}
							setLayoutParams(lp);

							setImageBitmap(bitmap);
							Handler h = getViewHandler();
							Message m = h.obtainMessage(flowTag.what, width,
									layoutHeight, FlowView.this);
							h.sendMessage(m);
						}
					}
				});

				// }

			}

		}
	}
}
