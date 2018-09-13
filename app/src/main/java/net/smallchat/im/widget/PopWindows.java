package net.smallchat.im.widget;

import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.Entity.PopItem;
import net.smallchat.im.global.FeatureFunction;

public class PopWindows {

	public interface PopWindowsInterface {
		public void onItemClick(int position,View view);
	}
	
	
	//List<Map<String, String>> moreList;
	public List<PopItem> morList;
	public PopupWindow mMyPopWindow;// popupwindow
	public ListView mListView;// popupwindow中的ListView
	public int mItemSize = 0;// 指定popupwindow中Item的数量
	public Context mContext;
	private int mColorId;
	/*public String[] mString;*/
	public PopWindowsInterface myInterface;
	public View mBelowView;
	
	public PopWindows() {
		super();
	}

	public PopWindows(Context contxt,List<PopItem> list,
			View view, PopWindowsInterface myInterface) {
		super();
		this.mBelowView = view;
		this.myInterface = myInterface;
		this.mContext = contxt;
		if (list!=null && list.size()>0) {
			this.morList = list;
			mItemSize = list.size();
		}
		//iniData(string);
	}

	public PopWindows(Context contxt,List<PopItem> list,
			View view) {
		super();
		this.mBelowView = view;
		this.mContext = contxt;
		if (list!=null && list.size()>0) {
			this.morList = list;
			mItemSize = list.size();
		}
		//iniData(string);
	}
	
	public PopWindows(Context contxt,List<PopItem> list) {
		super();
		this.mContext = contxt;
		if (list!=null && list.size()>0) {
			this.morList = list;
			mItemSize = list.size();
		}
		//iniData(string);
	}

	
	private void setMorList(List<PopItem> list){
		if(list == null || list.size()>0)
		this.morList = list;
	}
	private void iniPopupWindow() {

		/*LayoutInflater inflater = LayoutInflater.from(mContext);
		View layout = inflater.inflate(R.layout.task_detail_popupwindow, null);
		mListView = (ListView) layout.findViewById(R.tid.lv_popup_list);
		mMyPopWindow = new PopupWindow(layout);
		mListView.setCacheColorHint(0);
		mMyPopWindow.setFocusable(true);// 加上这个popupwindow中的ListView才可以接收点击事件

		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long tid) {

			}
		});

		// 控制popupwindow的宽度和高度自适应
		mListView.measure(View.MeasureSpec.UNSPECIFIED,
				View.MeasureSpec.UNSPECIFIED);
		mMyPopWindow.setWidth(mListView.getMeasuredWidth());
		mMyPopWindow.setHeight((mListView.getMeasuredHeight() + 20)
				* mItemSize);

		// 控制popupwindow点击屏幕其他地方消失
		mMyPopWindow.setBackgroundDrawable(mContext.getResources().getDrawable(
				R.drawable.bg_popupwindow));// 设置背景图片，不能在布局中设置，要通过代码来设置
		mMyPopWindow.setOutsideTouchable(true);// 触摸popupwindow外部，popupwindow消失。这个要求你的popupwindow要有背景图片才可以成功，如上
*/	}
	
	private void initView(View v,int showLoaction){
		
		mListView = (ListView) v.findViewById(R.id.lv_popup_list);
		if(showLoaction == 1){
			 mListView.setDivider(mContext.getResources().getDrawable(R.drawable.devider_line));
		}
		mMyPopWindow = new PopupWindow(v);
		mListView.setCacheColorHint(0);
		mListView.setAdapter(
				new popItemAdapter(mContext, morList));
		/*
		new SimpleAdapter(mContext, moreList,
		R.layout.list_item_popupwindow,
		new String[] { "share_key" },
		new int[] { R.tid.tv_list_item })*/
		mListView.setOnItemClickListener(groupItemListener);
	 }

	private class popItemAdapter extends BaseAdapter{

		LayoutInflater inflater;
		private Context mContext;
		private List<PopItem> mData;
		//private int mType; //0-周边商家 1-周边信息 2-商店
		
		public popItemAdapter(Context context,List<PopItem> list) {
			mContext = context;
			inflater = LayoutInflater.from(context);
			this.mData = list;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mData.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = View.inflate(mContext, R.layout.list_item_popupwindow, null);
			TextView title = (TextView)convertView.findViewById(R.id.tv_list_item);
			ImageView menuIcon = (ImageView)convertView.findViewById(R.id.menu_icon);
			menuIcon.setVisibility(View.VISIBLE);
			title.setText(mData.get(position).option);
			title.setTextColor(mContext.getResources().getColor(mColorId));
			if(mData.get(position).resource_id == 0){
				menuIcon.setVisibility(View.GONE);
			}else{
				menuIcon.setBackgroundResource(mData.get(position).resource_id);
				menuIcon.setVisibility(View.VISIBLE);
			}
			
			
			return convertView;
		}
		
	}
	
	
	OnItemClickListener groupItemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
			hideGroupPopView();
			/*Toast.makeText(mContext,moreList.get(position).get("share_key"),
					Toast.LENGTH_LONG).show();*/
			myInterface.onItemClick(morList.get(position).id,arg1);
		}
	};
	
	public void showGroupPopView(List<PopItem> string,int gravity,int bgId,int colorId,int showLoaction){
		if (string != null && string.size()>0) {
			setMorList(string);
		}
		mColorId = colorId;
		showGroupPopView(gravity,bgId,showLoaction);
	}
	
	public void showGroupPopView(View view,List<PopItem> string,int gravity,int bgId,
			int colorId,int showLoaction, PopWindowsInterface myInterface){
		this.mBelowView = view;
		this.myInterface = myInterface;
		if (string != null && string.size()>0) {
			setMorList(string);
		}
		mColorId = colorId;
		showGroupPopView(gravity,bgId,showLoaction);
		
	}
	
	
	// Gravity.RIGHT
	/**
	 * 显示泡泡
	 * @param gravity
	 * @param bgId R.drawable.white_pop_bg/pop_bg
	 * @param showLoaction 0-下方 1-上方
	 */
	private void showGroupPopView(int gravity,int bgId,int showLoaction){
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View layout = inflater.inflate(R.layout.task_detail_popupwindow, null);
		initView(layout,showLoaction);
	/*	int showHeight = mItemSize*80;
		Log.e("showHeight", "showHeight: "+ showHeight);*/
		if(showLoaction == 0){
			mMyPopWindow = new PopupWindow(layout,FeatureFunction.dip2px(mContext, 190), LayoutParams.WRAP_CONTENT);
		}else if(showLoaction == 1){
			mMyPopWindow = new PopupWindow(layout,FeatureFunction.dip2px(mContext, 100), LayoutParams.WRAP_CONTENT);
		}
		
		mMyPopWindow.setFocusable(true);
		mMyPopWindow.setOutsideTouchable(true);
		mMyPopWindow.setBackgroundDrawable(mContext.getResources().getDrawable(bgId));
		int loc[] = new int[2];
		mBelowView.getLocationInWindow(loc);
		//mBelowView.getLocationOnScreen(loc);  
		int yLoaction = 0;
		int xLoaction = 0;
		if(showLoaction == 0){
			yLoaction = loc[1]+mBelowView.getMeasuredHeight();
			xLoaction = 0;
		}else if(showLoaction == 1){
			xLoaction = loc[0]+FeatureFunction.dip2px(mContext, 10);
			yLoaction = loc[1]-mBelowView.getContext().getResources().getDimensionPixelSize(R.dimen.test_size);
		}
		mMyPopWindow.setAnimationStyle(R.style.mystyle);
		mMyPopWindow.showAtLocation(mBelowView, gravity|Gravity.TOP,xLoaction ,yLoaction);
		//mMyPopWindow.showAtLocation(mListView, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, FeatureFunction.dip2px(mContext, 70));
	}
	
	
	public void hideGroupPopView(){
		if (null != mMyPopWindow){
			/*mGroupPopView.setFocusable(false);
			mGroupPopView.update();*/
			if(mMyPopWindow.isShowing()){
				mMyPopWindow.dismiss();
				mMyPopWindow = null;
			}
		}
	}
}
