package net.smallchat.im.mine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.Favorite;
import net.smallchat.im.Entity.FavoriteItem;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.Entity.MovingLoaction;
import net.smallchat.im.Entity.MovingPic;
import net.smallchat.im.favorite.FavoriteDetailActivity;
import net.smallchat.im.widget.LazyScrollView;
import net.smallchat.im.components.LocationActivity;
import net.smallchat.im.R;
import net.smallchat.im.components.ShowImageActivity;
import net.smallchat.im.adapter.MyFavoriteListAdapter;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;
import net.smallchat.im.widget.FlowTag;
import net.smallchat.im.widget.FlowView;
import net.smallchat.im.widget.MyPullToRefreshListView;
import net.smallchat.im.widget.MyPullToRefreshListView.OnChangeStateListener;
import net.smallchat.im.widget.MyPullToRefreshScrollView;
import net.smallchat.im.widget.SearchFavoriteDialog;

/**
 * 我的收藏
 * @author dongli
 *
 */
public class MyFavoriteActivity extends BaseActivity implements OnChangeStateListener,
OnItemClickListener, net.smallchat.im.widget.MyPullToRefreshScrollView.OnChangeStateListener {

	private LinearLayout mSearchHeader;
	private ListView mListView;
	private MyPullToRefreshListView mContainer;
	private TextView mRefreshViewLastUpdated;
	private boolean mIsRefreshing = false;
	private LinearLayout mFootView;
	private LinearLayout mListLayout;

	//++图片模式++
	private LinearLayout mMapLayout;
	private MyPullToRefreshScrollView mScrollContainer;
	private LazyScrollView mScrollView;
	private LinearLayout mLayout;
	private TextView mRefreshScrollViewLastUpdated;
	private LinearLayout mWaterFallContainer;
	private ArrayList<LinearLayout> mWaterFallItems;
	private Display mDisplay;
	private Handler mHandler;
	private int mItemWidth;
	private DisplayMetrics mDMetrics;
	private int mColumnCount = 3;
	private HashMap<Integer, Integer>[] mPinMark = null;
	private HashMap<Integer, FlowView> mIViews;
	private HashMap<Integer, String> mPins;

	private int[] mTopIndex;
	private int[] mBottomIndex;
	private int[] mLineIndex;
	private int[] mColumnHeight;

	public boolean isLoadMore = false;
	public static final int mTopHeight = 10;
	private int mCount = 0;
	int mScrollHeight;

	private List<String> mPicList = new ArrayList<String>();
	//--图片模式--

	private int mIsRun;
	private String mInputText;

	private Favorite mWeibo;
	private List<FavoriteItem> mWeiboList = new ArrayList<FavoriteItem>();

	private MyFavoriteListAdapter mAdapter;
	private int mSelectType = 1; //1-列表模式 2-图片模式

	private boolean isShowRighIcon;


	/*
	 * 处理列表模式消息
	 */
	private Handler mCheckHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GlobalParam.SHOW_SCROLLREFRESH:
				if (mIsRefreshing) {
					mContainer.onRefreshComplete();
					break;
				}
				mIsRefreshing = true;
				if(mWeibo !=null){
					mWeibo = null;
				}
				getPublishWeiboData(GlobalParam.LIST_LOAD_REFERSH);
				break;

			case GlobalParam.HIDE_SCROLLREFRESH:
				mIsRefreshing = false;
				mContainer.onRefreshComplete();
				updateListView();
				break;

			case GlobalParam.SHOW_PROGRESS_DIALOG:
				String dialogMsg = (String)msg.obj;
				showProgressDialog(dialogMsg);
				break;
			case GlobalParam.HIDE_PROGRESS_DIALOG:
				hideProgressDialog();
				//updateListView();
				break;
			case GlobalParam.MSG_CHECK_STATE:
				// mHandler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
				hideProgressDialog();
				if (mFootView != null) {
					mListView.removeFooterView(mFootView);
				}
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				//Toast.makeText(mContext, "没有更多数据了", Toast.LENGTH_LONG);
				break;
			case GlobalParam.MSG_SHOW_LISTVIEW_DATA:
				updateListView();
				break;

			case GlobalParam.MSG_LOAD_ERROR:
				String error_Detail = (String)msg.obj;
				if(error_Detail != null && !error_Detail.equals("")){
					Toast.makeText(mContext,error_Detail,Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(mContext, R.string.load_error,Toast.LENGTH_LONG).show();
				}
				break;
			case GlobalParam.MSG_NETWORK_ERROR:
				Toast.makeText(mContext,R.string.network_error,Toast.LENGTH_LONG).show();
				break;
			case GlobalParam.MSG_TIME_OUT_EXCEPTION:
				String message=(String)msg.obj;
				if (message==null || message.equals("")) {
					message=mContext.getResources().getString(R.string.timeout);
				}
				Toast.makeText(mContext,message, Toast.LENGTH_LONG).show();
				break;
			case GlobalParam.SHOW_LOADINGMORE_INDECATOR:
				LinearLayout footView = (LinearLayout)msg.obj;				
				ProgressBar pb = (ProgressBar)footView.findViewById(R.id.hometab_addmore_progressbar);
				pb.setVisibility(View.VISIBLE);		 		
				TextView more = (TextView)footView.findViewById(R.id.hometab_footer_text);
				more.setText(mContext.getString(R.string.add_more_loading));
				//getLoveList(GlobalParam.LIST_LOAD_MORE);
				getPublishWeiboData(GlobalParam.LIST_LOAD_MORE);
				break;
			case GlobalParam.HIDE_LOADINGMORE_INDECATOR:
				if (mFootView != null){
					ProgressBar pbar = (ProgressBar)mFootView.findViewById(R.id.hometab_addmore_progressbar);
					pbar.setVisibility(View.GONE);
					TextView moreView = (TextView)mFootView.findViewById(R.id.hometab_footer_text);
					moreView.setText(R.string.add_more);
				}

				if (mAdapter != null){
					mAdapter.notifyDataSetChanged();
				}
				break;
			case GlobalParam.MSG_CHECK_FAVORITE_STATUS:
				IMResponseState canclefavResult = (IMResponseState)msg.obj;
				if(canclefavResult == null){
					Toast.makeText(mContext, R.string.commit_dataing, Toast.LENGTH_LONG).show();
					return;
				}
				if(canclefavResult.code!=0){
					Toast.makeText(mContext, canclefavResult.errorMsg, Toast.LENGTH_LONG).show();
					return;
				}else{

					mWeiboList.remove(canclefavResult.positon);
					if(mAdapter!=null){
						mAdapter.notifyDataSetChanged();
					}
				}
				break;
			default:
				break;
			}
		}
	};

	/*
	 * 处理图片模式消息
	 */
	private Handler mCategoryHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case GlobalParam.SHOW_SCROLLREFRESH:
				if (mIsRefreshing) {
					mScrollContainer.onRefreshComplete();
					break;
				}
				mIsRefreshing = true;
				getPublishWeiboData(GlobalParam.LIST_LOAD_REFERSH);

				break;
			case GlobalParam.HIDE_SCROLLREFRESH:
				mIsRefreshing = false;
				mScrollContainer.onRefreshComplete();
				for (int i = 0; i < mColumnCount; i++) {
					clearMap(mPinMark[i]);
				}
				clearArray(mColumnHeight, true);
				clearMap(mIViews);
				clearMap(mPins);

				clearArray(mLineIndex, false);
				clearArray(mBottomIndex, false);
				clearArray(mTopIndex, true);
				if(mWaterFallContainer != null){
					for (int i = 0; i < mWaterFallContainer.getChildCount(); i++) {
						if(mWaterFallContainer.getChildAt(i) != null){
							LinearLayout linearLayout = (LinearLayout)mWaterFallContainer.getChildAt(i);
							linearLayout.removeAllViews();
						}
					}
					mWaterFallContainer.requestLayout();
				}

				AddItemToContainer(0);
				if (mPicList!=null && mPicList.size()>0) {
					mCount = mPicList.size();
				}
				break;


			case GlobalParam.HIDE_PROGRESS_DIALOG:
				if(isLoadMore){
					isLoadMore = false;
					if(mCount < mPicList.size()){
						AddItemToContainer(mCount);
					}
				}else {
					InitLayout();
				}
				if (mPicList!=null && mPicList.size()>0) {
					mCount = mPicList.size();
				}

				hideProgressDialog();
				break;
			case GlobalParam.MSG_NETWORK_ERROR:
				hideProgressDialog();
				if (mIsRefreshing) {
					mIsRefreshing = false;
					mScrollContainer.onRefreshComplete();
				}
				Toast.makeText(mContext, R.string.network_error, Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		}
	};



	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.topic_view);
		isShowRighIcon = getIntent().getBooleanExtra("isShow",true);

		mDMetrics = new DisplayMetrics();
		mDisplay = this.getWindowManager().getDefaultDisplay();
		mItemWidth = mDisplay.getWidth() / mColumnCount;

		initCompent();
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.ACTION_REFRESH_MY_FAVORITE);
		registerReceiver(mRefreshReceiver,filter);
		getPublishWeiboData(GlobalParam.LIST_LOAD_FIRST);
	}

	/*
	 * 处理通知
	 */
	BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent!=null){
				String action =  intent.getAction();//重新加载数据
				if(action.equals(GlobalParam.ACTION_REFRESH_MY_FAVORITE)){
					getPublishWeiboData(GlobalParam.LIST_LOAD_FIRST);
				}
			}
		}
	};

	/*
	 * 实例化控件
	 */
	private void initCompent(){
		setTitleContent(R.drawable.back_btn,true,R.drawable.map_favorite_btn,R.string.my_favorite);
		if(isShowRighIcon){
			mRightBtn.setVisibility(View.VISIBLE);
			mRightBtn.setOnClickListener(this);
		}else{
			mRightBtn.setVisibility(View.GONE);
		}
		mLeftBtn.setOnClickListener(this);

		mSearchBtn.setOnClickListener(this);


		mListLayout = (LinearLayout)findViewById(R.id.category_linear);
		mRefreshViewLastUpdated = (TextView) findViewById(R.id.pull_to_refresh_time);
		mContainer = (MyPullToRefreshListView)findViewById(R.id.container);
		mListView = mContainer.getList();
		mListView.setDivider(mContext.getResources().getDrawable(R.drawable.splite));
		mListView.setCacheColorHint(0);
		mListView.setSelector(mContext.getResources().getDrawable(R.drawable.transparent_selector));
		mListView.setOnItemClickListener(this);
		mListView.setOnCreateContextMenuListener(this);
		mContainer.setOnChangeStateListener(this);


		mListView.setHeaderDividersEnabled(false);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE://处理加载更多
					if(view.getLastVisiblePosition() == (view.getCount()-1) ){
						if (mWeibo!=null && mWeibo.page!=null && mWeibo.page.hasMore == 1) {
							if(mFootView!=null){
								Message message = new Message();
								message.what = GlobalParam.SHOW_LOADINGMORE_INDECATOR;
								message.obj = mFootView; 
								mCheckHandler.sendMessage(message);
							}
						}else{
							mCheckHandler.sendEmptyMessage(GlobalParam.MSG_CHECK_STATE);
						}
						
					}
					break;

				default:
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});


		mMapLayout = (LinearLayout)findViewById(R.id.map_category_linear);
		mRefreshScrollViewLastUpdated = (TextView) findViewById(R.id.scrollview_pull_to_refresh_time);

		mScrollContainer = (MyPullToRefreshScrollView) findViewById(R.id.scrollview_container);
		mScrollContainer.setOnChangeStateListener(this);
		mScrollView = mScrollContainer.getScrollView();
		mLayout = mScrollView.getLayout();

	}


	/*
	 * 初始化图片图片控件
	 */
	private void InitLayout() {

		mColumnHeight = new int[mColumnCount];
		mIViews = new HashMap<Integer, FlowView>();
		mPins = new HashMap<Integer, String>();
		mPinMark = new HashMap[mColumnCount];
		this.mLineIndex = new int[mColumnCount];
		this.mBottomIndex = new int[mColumnCount];
		this.mTopIndex = new int[mColumnCount];

		for (int i = 0; i < mColumnCount; i++) {
			mLineIndex[i] = -1;
			mBottomIndex[i] = -1;
			mPinMark[i] = new HashMap<Integer, Integer>();
		}

		if(mLayout != null){
			mLayout.removeAllViews();
		}



		mScrollView.getView();
		mScrollView.setOnScrollListener(new LazyScrollView.OnScrollListener() {

			@Override
			public void onTop() {
				// 滚动到最顶端
				Log.d("LazyScroll", "Scroll to top");
			}

			@Override
			public void onScroll() {
				Log.d("LazyScroll", "onScroll");
			}

			@Override
			public void onBottom() {
				Log.d("LazyScroll", "onBottom");
				// 滚动到最低端
				if(mWeibo!=null && mWeibo.page.currentPage != mWeibo.page.totalPage){
					isLoadMore = true;
					mCategoryHandler.sendEmptyMessage(GlobalParam.SHOW_LOADINGMORE_INDECATOR);
					getPublishWeiboData(GlobalParam.LIST_LOAD_MORE);
				}

			}
			//滑动式释放图片
			@Override
			public void onAutoScroll(int l, int t, int oldl, int oldt) {

				mScrollHeight = mScrollView.getMeasuredHeight();
				Log.d("SmallVideoRecorderActivity", "mScrollHeight:" + mScrollHeight);

				if (t > oldt) {// 向下滚动
					if (t > 2 * mScrollHeight) {// 超过两屏幕后

						for (int k = 0; k < mColumnCount; k++) {
							if(mWaterFallItems != null && mWaterFallItems.size() > k && mWaterFallItems.get(k) != null){
								LinearLayout localLinearLayout = mWaterFallItems
										.get(k);

								if (mPinMark[k] != null && mPinMark[k].get(Math.min(mBottomIndex[k] + 1,
										mLineIndex[k])) != null && mPinMark[k].get(Math.min(mBottomIndex[k] + 1,
												mLineIndex[k])) <= t + 3 * mScrollHeight) {// 最底部的图片位置小于当前t+3*屏幕高度
									RelativeLayout singleLayout = (RelativeLayout) localLinearLayout.getChildAt(Math.min(1 + mBottomIndex[k], mLineIndex[k]));
									if(singleLayout != null && singleLayout.getChildAt(0) != null){
										((FlowView) singleLayout.getChildAt(0)).Reload();
									}
									
									mBottomIndex[k] = Math.min(1 + mBottomIndex[k],
											mLineIndex[k]);

								}
								Log.d("SmallVideoRecorderActivity",
										"headIndex:" + mTopIndex[k]
												+ "  footIndex:" + mBottomIndex[k]
														+ "  headHeight:"
														+ mPinMark[k].get(mTopIndex[k]));
								if (mPinMark[k]!= null &&  mPinMark[k].get(mTopIndex[k]) != null && mPinMark[k].get(mTopIndex[k]) < t - 2
										* mScrollHeight) {// 未回收图片的最高位置<t-两倍屏幕高度

									int i1 = mTopIndex[k];
									mTopIndex[k]++;
									RelativeLayout singleLayout = (RelativeLayout) localLinearLayout.getChildAt(i1);
									if(singleLayout != null && singleLayout.getChildAt(0) != null){
										((FlowView) singleLayout.getChildAt(0)).recycle();
									}
									/*((FlowView) localLinearLayout.getChildAt(i1))
											.recycle();*/
									Log.d("SmallVideoRecorderActivity", "recycle,k:" + k
											+ " headindex:" + mTopIndex[k]);

								}
							}
						}

					}
				} else {// 向上滚动

					for (int k = 0; k < mColumnCount; k++) {
						if(mWaterFallItems != null && mWaterFallItems.size() > k && mWaterFallItems.get(k) != null){
							LinearLayout localLinearLayout = mWaterFallItems.get(k);
							if (mPinMark[k] != null && mPinMark[k].get(mBottomIndex[k]) != null && mPinMark[k].get(mBottomIndex[k]) > t + 3
									* mScrollHeight) {
								RelativeLayout singleLayout = (RelativeLayout) localLinearLayout.getChildAt(mBottomIndex[k]);
								if(singleLayout != null && singleLayout.getChildAt(0) != null){
									((FlowView) singleLayout.getChildAt(0)).recycle();
								}

								mBottomIndex[k]--;
							}

							if (mPinMark[k] != null && mPinMark[k].get(Math.max(mTopIndex[k] - 1, 0))!= null && mPinMark[k].get(Math.max(mTopIndex[k] - 1, 0)) >= t
									- 2 * mScrollHeight) {
								RelativeLayout singleLayout = (RelativeLayout) localLinearLayout.getChildAt(Math.max(-1 + mTopIndex[k], 0));
								if(singleLayout != null && singleLayout.getChildAt(0) != null){
									((FlowView) singleLayout.getChildAt(0)).Reload();
								}
								/*((FlowView) localLinearLayout.getChildAt(Math.max(
										-1 + mTopIndex[k], 0))).Reload();*/
								mTopIndex[k] = Math.max(mTopIndex[k] - 1, 0);
							}
						}
					}

				}

			}
		});

		mWaterFallContainer = new LinearLayout(mContext);
		mWaterFallContainer.setBackgroundColor(mContext.getResources().getColor(R.color.backgroud_color));
		mWaterFallContainer.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT));
		mWaterFallContainer.setOrientation(LinearLayout.HORIZONTAL);
		mLayout.addView(mWaterFallContainer);
	
		mHandler = new Handler() {

			@Override
			public void dispatchMessage(Message msg) {

				super.dispatchMessage(msg);
			}

			@Override
			public void handleMessage(Message msg) {
				
				//显示图片
				switch (msg.what) {
				case 1:

					final FlowView v = (FlowView) msg.obj;
					//int w = msg.arg1;
					int h = msg.arg2;
					// Log.d("SmallVideoRecorderActivity",
					// String.format(
					// "获取实际View高度:%d,ID：%d,columnIndex:%d,rowIndex:%d,filename:%s",
					// v.getHeight(), v.getId(), v
					// .getColumnIndex(), v.getRowIndex(),
					// v.getFlowTag().getFileName()));
					String f = v.getFlowTag().getThumbPicUrl();

					// 此处计算列值
					int columnIndex;
					int row;
					row = v.getId() / mColumnCount;
					columnIndex = v.getId() % mColumnCount;
					//int columnIndex = GetMinValue(mColumnHeight);

					v.setColumnIndex(columnIndex);

					mColumnHeight[columnIndex] += h;

					mPins.put(v.getId(), f);
					mIViews.put(v.getId(), v);
					View view=null;
					view = LayoutInflater.from(mContext).inflate(R.layout.image_display, null);

					RelativeLayout singleLayout = (RelativeLayout) view.findViewById(R.id.imagelayout);
					singleLayout.setPadding(2, 2, 2, 2);
					singleLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
					singleLayout.addView(v);
					//mWaterFallItems.get(columnIndex).addView(v);
					TextView nicknameTextView = (TextView) view.findViewById(R.id.content);
					/*textView.setGravity(Gravity.CENTER_HORIZONTAL);
					textView.setPadding(0, -15, 0, 0);*/
					nicknameTextView.bringToFront();
					if(v.getId() < mPicList.size()){
						//nicknameTextView.setVisibility(View.GONE);
						singleLayout.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View view) {
								// TODO Auto-generated method stub

								Intent intent = new Intent(mContext, ShowImageActivity.class);
								intent.putExtra("imageurl", mPicList.get(v.getId()));
								intent.putExtra("type",2);

								mContext.startActivity(intent);

							}
						});
						nicknameTextView.setText("sss");
					

					}
					if(mWaterFallItems.get(columnIndex).getChildCount() > row){
						mWaterFallItems.get(columnIndex).addView(singleLayout, row);
					}else {
						mWaterFallItems.get(columnIndex).addView(singleLayout);
					}

					mLineIndex[columnIndex]++;

					mPinMark[columnIndex].put(mLineIndex[columnIndex],
							mColumnHeight[columnIndex]);
					mBottomIndex[columnIndex] = mLineIndex[columnIndex];
					break;
				}

			}

			@Override
			public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
				return super.sendMessageAtTime(msg, uptimeMillis);
			}
		};

		mWaterFallItems = new ArrayList<LinearLayout>();

		for (int i = 0; i < mColumnCount; i++) {
			LinearLayout itemLayout = new LinearLayout(this);
			LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(
					mItemWidth, LayoutParams.WRAP_CONTENT);

			itemLayout.setPadding(3, 3, 3, 3);
			itemLayout.setOrientation(LinearLayout.VERTICAL);
			itemLayout.setLayoutParams(itemParam);
			mWaterFallItems.add(itemLayout);
			mWaterFallContainer.addView(itemLayout);
		}
	
		// 第一次加载
		AddItemToContainer(0);
	}

	private void AddItemToContainer(int pos) {

		for (int i = pos; mPicList != null && i < mPicList.size(); i++) {
			if (mPicList.get(i)!=null) {
				AddImage(mPicList.get(i),
						(int) Math.ceil(i / (double) mColumnCount),
						i);
			}

			//mLoadedCount++;
		}

	}
	
	/*
	 * 将图片传入下载队列
	 */
	private void AddImage(String thumbUrl, int rowIndex, int id) {

		FlowView item = new FlowView(mContext);
		item.setRowIndex(rowIndex);
		item.setId(id);
		item.setViewHandler(this.mHandler);
		// 多线程参数
		FlowTag param = new FlowTag();
		param.setFlowId(id);
		param.setThumbUrl(thumbUrl);
		param.setItemWidth(mItemWidth);
		item.setFlowTag(param);
		item.LoadImage();
	}
	
	/*
	 * 清空图片缓存
	 */
	public void clearMap(HashMap map){
		if(map != null && !map.isEmpty()){
			map.clear();
		}
	}

	public void clearArray(int[] array, boolean isDefault){
		if(array != null){
			for (int i = 0; i < array.length; i++) {
				if(isDefault){
					array[i] = 0;
				}else{
					array[i] = -1;
				}
			}
		}
	}



	/*
	 * 按钮点击事件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			MyFavoriteActivity.this.finish();
			break;
		
		case R.id.search_btn:
			SearchFavoriteDialog searchDialog = new SearchFavoriteDialog(mContext, mWeiboList);
			searchDialog.show();
			break;
		case R.id.right_btn:
			if(mSelectType == 1){//列表模式
				mScrollContainer.setVisibility(View.VISIBLE);
				mMapLayout.setVisibility(View.VISIBLE);
				mContainer.setVisibility(View.GONE);
				mListLayout.setVisibility(View.GONE);
				mSearchBtn.setVisibility(View.GONE);
				mRightBtn.setImageResource(R.drawable.list_favorite_btn);
				mSelectType = 2;
				getPublishWeiboData(GlobalParam.LIST_LOAD_FIRST);
			}else {
				mSelectType = 1;//图片模式

				mSearchBtn.setVisibility(View.VISIBLE);
				mRightBtn.setImageResource(R.drawable.map_favorite_btn);
				mContainer.setVisibility(View.VISIBLE);
				mListLayout.setVisibility(View.VISIBLE);
				mScrollContainer.setVisibility(View.GONE);
				mMapLayout.setVisibility(View.GONE);
				getPublishWeiboData(GlobalParam.LIST_LOAD_FIRST);
			}
			break;

		default:
			break;
		}
	}





	private void getPublishWeiboData(final int loadType){
		if (!IMCommon.getNetWorkState()) {
			switch (loadType) {
			case GlobalParam.LIST_LOAD_FIRST:
				mCheckHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
				break;
			case GlobalParam.LIST_LOAD_MORE:
				mCheckHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);

			case GlobalParam.LIST_LOAD_REFERSH:
				mCheckHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
				break;

			default:
				break;
			}
			mCheckHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
			return;
		}

		new Thread(){
			public void run() {

				try {
					boolean isExitsData = true;
					if (loadType == GlobalParam.LIST_LOAD_FIRST) {
						IMCommon.sendMsg(mCheckHandler, GlobalParam.SHOW_PROGRESS_DIALOG,
								mContext.getResources().getString(R.string.add_more_loading));
					}

					if (mWeibo!=null && mWeibo.page.currentPage == mWeibo.page.totalPage
							&& loadType == GlobalParam.LIST_LOAD_MORE) {
						isExitsData = false;
					}

					int currentPage = 0;
					if (loadType == GlobalParam.LIST_LOAD_REFERSH || loadType == GlobalParam.LIST_LOAD_FIRST) {
						currentPage = 1;
					}else if(loadType == GlobalParam.LIST_LOAD_MORE){
						currentPage =mWeibo.page.currentPage+1;
					}


					if (isExitsData) {
						mWeibo = IMCommon.getIMServerAPI().favoriteList(currentPage);
						if ((loadType ==GlobalParam.LIST_LOAD_FIRST || loadType == GlobalParam.LIST_LOAD_REFERSH)){
							if( mWeiboList!=null && mWeiboList.size()>0) {
								mWeiboList.clear();
							}
							if(mPicList!=null && mPicList.size()>0){
								mPicList.clear();
							}
						}
						if (mWeibo != null && mWeibo.childList!=null && mWeibo.childList.size() > 0) {
							
							if(mWeibo.page!=null && mWeibo.page.hasMore == 1){
								isExitsData = true;
							}else{
								isExitsData = false;
							}
							
							mWeiboList.addAll(mWeibo.childList);
							for (int i = 0; i < mWeibo.childList.size(); i++) {
								if(mWeibo.childList.get(i).messageType == MessageType.IMAGE){
									MovingPic movingPic = MovingPic.getInfo(mWeibo.childList.get(i).content);
									if(movingPic!=null ){
										mPicList.add(movingPic.urlsmall);
									}
								}else if(mWeibo.childList.get(i).messageType == MessageType.LOCATION){
									MovingLoaction movingMap = MovingLoaction.getInfo(mWeibo.childList.get(i).content);
									mPicList.add("http://api.map.baidu.com/staticimage?center="+movingMap.lng+","+movingMap.lat+
											"&width=200&height=120&zoom=16&markers="+movingMap.lng+","+movingMap.lat+"&markerStyles=s");
								}
							}
						} else{
							isExitsData = false;
						}
					}
					mCheckHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);

					switch (loadType) {
					case GlobalParam.LIST_LOAD_FIRST:
						isLoadMore = false;
						if(mSelectType == 1){
							mCheckHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_LISTVIEW_DATA);
						}else if(mSelectType == 2){
							mCategoryHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
						}
						break;
					case GlobalParam.LIST_LOAD_MORE:
						isLoadMore = true;
						if(mSelectType == 1){
							mCheckHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
						}else if(mSelectType == 2){
							mCategoryHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
						}

					case GlobalParam.LIST_LOAD_REFERSH:
						isLoadMore = false;
						if(mSelectType == 1){
							mCheckHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
						}else if(mSelectType == 2){
							mCategoryHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
						}

						break;

					default:
						break;
					}
					if (!isExitsData) {
						mCheckHandler.sendEmptyMessage(GlobalParam.MSG_CHECK_STATE);
					}
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mCheckHandler, GlobalParam.MSG_TIME_OUT_EXCEPTION,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					switch (loadType) {
					case GlobalParam.LIST_LOAD_FIRST:
						mCheckHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
						break;
					case GlobalParam.LIST_LOAD_MORE:
						mCheckHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
						break;
					case GlobalParam.LIST_LOAD_REFERSH:
						mCheckHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
						break;
					default:
						break;
					}
					mCheckHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();

	}

	private void updateListView(){
		if(mAdapter != null){
			mAdapter.notifyDataSetChanged();
		}
		if (mWeiboList == null || mWeiboList.size() == 0) {

			return;
		}
		mListView.setVisibility(View.VISIBLE);
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}

		mAdapter = new MyFavoriteListAdapter(mContext, mWeiboList);
		if (mListView.getFooterViewsCount() == 0) {
			mFootView = (LinearLayout) LayoutInflater.from(this)
					.inflate(R.layout.hometab_listview_footer, null);
			ProgressBar pb = (ProgressBar)mFootView.findViewById(R.id.hometab_addmore_progressbar);
			pb.setVisibility(View.GONE);
			mListView.addFooterView(mFootView);	
		}
	
		mListView.setAdapter(mAdapter);

	}

	@Override
	public void onChangeState(MyPullToRefreshListView container, int state) {
		mRefreshViewLastUpdated.setText(FeatureFunction.getRefreshTime());
		switch (state) {
		case MyPullToRefreshListView.STATE_LOADING:
			mCheckHandler.sendEmptyMessage(GlobalParam.SHOW_SCROLLREFRESH);
			break;
		}
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		if(mWeiboList == null || mWeiboList.size() == 0){
			return;
		}
		if (info.position > mWeiboList.size()){
			return;
		}
		menu.add(Menu.NONE,0, 0,mContext.getResources().getString(R.string.del));
	}


	@Override 
	public boolean onContextItemSelected(MenuItem item) {  

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo(); 

		int longClickItemIndex = info.position - mListView.getHeaderViewsCount();
		if(longClickItemIndex < mWeiboList.size()){
			int menuItemIndex = item.getItemId();  
			FavoriteItem favoriteItem = mWeiboList.get(longClickItemIndex);
			switch (menuItemIndex){
			case 0:
				canclefavoriteMoving(favoriteItem.id,longClickItemIndex);
				break;

			default:
				break;
			}
		}

		return true;  
	} 


	private void canclefavoriteMoving(final int favoriteid,final int pos){
		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
							mContext.getResources().getString(R.string.send_request));
					IMResponseState status = IMCommon.getIMServerAPI().canclefavMoving(favoriteid);
					if(status!=null){
						status.positon = pos;
					}
					IMCommon.sendMsg(mCheckHandler, GlobalParam.MSG_CHECK_FAVORITE_STATUS,status);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler,BASE_MSG_TIMEOUT_ERROR,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(0<=arg2 && arg2<mWeiboList.size()){
			FavoriteItem item = mWeiboList.get(arg2);
			if(item.messageType == MessageType.LOCATION){
				Intent intent  = new Intent(mContext, LocationActivity.class);
				MovingLoaction movingLoaction = MovingLoaction.getInfo(item.content);
				intent.putExtra("show", true);
				intent.putExtra("lat", movingLoaction.lat);
				intent.putExtra("lng", movingLoaction.lng);
				intent.putExtra("addr", movingLoaction.address);

				intent.putExtra("fuid", item.id);
				startActivity(intent);
			}else{
				Intent detailIntent = new Intent();
				detailIntent.setClass(mContext, FavoriteDetailActivity.class);
				detailIntent.putExtra("entity",item);
				startActivity(detailIntent);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mRefreshReceiver);
	}

	@Override
	public void onChangeState(MyPullToRefreshScrollView container, int state) {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);

		mRefreshScrollViewLastUpdated.setText(getResources().getString(R.string.updatetime)+month+"/"+day+"/"+year+" " + hour+":"+minute+":"+second);
		switch (state) {
		case MyPullToRefreshScrollView.STATE_LOADING:
			//mHandler.sendEmptyMessage(SHOW_SCROLLREFRESH);
			mCategoryHandler.sendEmptyMessage(GlobalParam.SHOW_SCROLLREFRESH);
			break;
		}
	}



}
