package net.smallchat.im.meeting;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.ChatType;
import net.smallchat.im.R;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.MessageTable;
import net.smallchat.im.Entity.Meeting;
import net.smallchat.im.Entity.MeetingItem;
import net.smallchat.im.Entity.PopItem;
import net.smallchat.im.adapter.MettingAdapter;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;
import net.smallchat.im.widget.MeetingSearchDialog;
import net.smallchat.im.widget.MyPullToRefreshListView;
import net.smallchat.im.widget.PopWindows;
import net.smallchat.im.widget.MyPullToRefreshListView.OnChangeStateListener;
import net.smallchat.im.widget.PopWindows.PopWindowsInterface;

/**
 * 正在进行中的会议
 * @author dongli
 *
 */
public class MettingActivity extends BaseActivity implements OnChangeStateListener, OnItemClickListener{

	/*
	 * 定义全局变量
	 */
	private MyPullToRefreshListView mContainer;
	private TextView mRefreshViewLastUpdated;
	private ListView mListView;
	private LinearLayout mFootView;
	private boolean mIsRefreshing = false;

	private RelativeLayout mTitleLayout;
	

	private MettingAdapter mAdapter;
	private Meeting mMyAlbum;
	private List<MeetingItem> mDataList = new ArrayList<MeetingItem>();
	private List<MeetingItem> mSearchList = new ArrayList<MeetingItem>();

	private PopWindows mPopWindows;
	private List<PopItem> mPopList = new ArrayList<PopItem>();
	private int mSelectType = 1; //1-正在进行中 2-往期 3-我的

	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.metting_view);


		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.ACTION_REFRESH_MEETING_LIST);
		registerReceiver(mRefreshReceiver, filter);


		IMCommon.saveReadMeetingTip(mContext, true);
		initCompent();
		getMeetingData(GlobalParam.LIST_LOAD_FIRST);
	}
	
	/*
	 * 处理通知
	 */
	BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent !=null){
				String action = intent.getAction();
				if(action!=null && !action.equals("")){
					if(action.equals(GlobalParam.ACTION_REFRESH_MEETING_LIST)){
						mMyAlbum = null;
						getMeetingData(GlobalParam.LIST_LOAD_FIRST);
					}
				}
			}
		}
	};

	/*
	 * 实例化控件
	 */
	private void initCompent(){
		mContext.sendBroadcast(new Intent(GlobalParam.ACTION_HIDE_FOUND_NEW_TIP));
		mContext.sendBroadcast(new Intent(GlobalParam.ACTION_HIDE_NEW_MEETING));

		mTitleLayout = (RelativeLayout)findViewById(R.id.title_layout);
		setTitleContent(R.drawable.back_btn,true,true,true,R.string.metting_doding);
		RelativeLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF, R.id.left_btn);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		mCenterLayout.setLayoutParams(params);
		mCenterLayout.setGravity(Gravity.CENTER_VERTICAL);
		mSearchBtn.setOnClickListener(this);
		mAddBtn.setOnClickListener(this);
		mMoreBtn.setOnClickListener(this);
		mLeftBtn.setOnClickListener(this);

		mRefreshViewLastUpdated = (TextView) findViewById(R.id.pull_to_refresh_time);
		mContainer = (MyPullToRefreshListView)findViewById(R.id.container);
		mListView = mContainer.getList();
		mListView.setDivider(getResources().getDrawable(R.drawable.splite));
		mListView.setCacheColorHint(0);
		mListView.setSelector(mContext.getResources().getDrawable(R.drawable.transparent_selector));
		mContainer.setOnChangeStateListener(this);
		mListView.setOnItemClickListener(this);
		initMoreMenu();
	}

	/*
	 * 初始化更多菜单
	 */
	private void initMoreMenu(){
		String[] menuString = mContext.getResources().getStringArray(R.array.meting_more_item);
		for (int i = 0; i < menuString.length; i++) {
			mPopList.add(new PopItem(i+1, menuString[i],""));
		}

		mPopWindows = new PopWindows(mContext, mPopList, mTitleLayout, new PopWindowsInterface() {

			@Override
			public void onItemClick(int position, View view) {
				switch (position) {
				case 1://进行中的会议
					mMyAlbum = null;
					mSelectType = 1;
					titileTextView.setText(mPopList.get(0).option);
					getMeetingData(GlobalParam.LIST_LOAD_FIRST);
					break;
				case 2://往期会议
					mSelectType = 2;
					mMyAlbum = null;
					titileTextView.setText(mPopList.get(1).option);
					getMeetingData(GlobalParam.LIST_LOAD_FIRST);
					break;
				case 3://
					mMyAlbum = null;
					mSelectType = 3;
					titileTextView.setText(mPopList.get(2).option);
					getMeetingData(GlobalParam.LIST_LOAD_FIRST);
					break;
				default:
					break;
				}
			}
		});

	}

	
	/*
	 * 获取会议列表
	 */
	private void getMeetingData(final int loadType){
		if (!IMCommon.getNetWorkState()) {
			switch (loadType) {
			case GlobalParam.LIST_LOAD_FIRST:
				mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
				break;

			case GlobalParam.LIST_LOAD_MORE:
				mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
				break;
			case GlobalParam.LIST_LOAD_REFERSH:
				mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
				break;

			default:
				break;
			}
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					if (loadType == GlobalParam.LIST_LOAD_FIRST) {
						IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
								mContext.getResources().getString(R.string.get_dataing));
					}
					boolean isExitsData = true;
					if (mMyAlbum!=null && mMyAlbum.pageInfo!=null && mMyAlbum.pageInfo.currentPage == mMyAlbum.pageInfo.totalPage) {
						isExitsData = false;
					}
					int page = 0;
					if (loadType == GlobalParam.LIST_LOAD_FIRST || loadType == GlobalParam.LIST_LOAD_REFERSH) {
						page = 1;
					}else if(loadType == GlobalParam.LIST_LOAD_MORE){
						page = mMyAlbum.pageInfo.currentPage+1;
					}
					if (isExitsData) {
						mMyAlbum = IMCommon.getIMServerAPI().meetingList(mSelectType,page);
						if ((loadType == GlobalParam.LIST_LOAD_FIRST || loadType == GlobalParam.LIST_LOAD_REFERSH)) {
							if(mDataList!=null && mDataList.size()>0){
								mDataList.clear();
							}
							if(mSearchList!=null && mSearchList.size()>0){
								mSearchList.clear();
							}
						}
						if (mMyAlbum != null && mMyAlbum.childList!=null && mMyAlbum.childList.size() > 0) {
							isExitsData = true;
							SQLiteDatabase db = DBHelper.getInstance(mContext).getReadableDatabase();
							MessageTable messageTable = new MessageTable(db);
							for (int i = 0; i < mMyAlbum.childList.size(); i++) {
								mMyAlbum.childList.get(i).unread = messageTable.queryUnreadCountByID(mMyAlbum.childList.get(i).id+"", ChatType.MeetingMessage);
								//mSearchList.add(new MainSearchEntity("",0,"", content, headSmall, time, olwerModle, uid, searchContent, remarkname))
							}
							mDataList.addAll(mMyAlbum.childList); 
							mSearchList.addAll(mMyAlbum.childList);
						} else{
							isExitsData = false;
						}
					}

					if (loadType == GlobalParam.LIST_LOAD_FIRST) {
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					}

					switch (loadType) {
					case GlobalParam.LIST_LOAD_FIRST:
						mHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_LISTVIEW_DATA);
						break;

					case GlobalParam.LIST_LOAD_MORE:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
						break;
					case GlobalParam.LIST_LOAD_REFERSH:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
						break;

					default:
						break;
					}
					if (!isExitsData) {
						mHandler.sendEmptyMessage(GlobalParam.MSG_CHECK_STATE);
					}

				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler,BASE_MSG_NETWORK_ERROR, 
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					switch (loadType) {
					case GlobalParam.LIST_LOAD_FIRST:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
						break;

					case GlobalParam.LIST_LOAD_MORE:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
						break;
					case GlobalParam.LIST_LOAD_REFERSH:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
						break;

					default:
						break;
					}
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}

	/*
	 * 显示listview 数据
	 */
	private void updateListView(){
		if (mDataList == null || mDataList.size() == 0) {
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
			return;
		}

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}else{
			mAdapter = new MettingAdapter(mContext, mDataList,mHandler);
		
			mListView.setAdapter(mAdapter);
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
			MettingActivity.this.finish();
			break;
		case R.id.search_btn:
			MeetingSearchDialog searchDialog = new MeetingSearchDialog(mContext, mSearchList,3);
			searchDialog.show();
			break;
		case R.id.add_btn:
			Intent createIntent = new Intent();
			createIntent.setClass(mContext, ApplyMettingActivity.class);
			startActivity(createIntent);
			break;
		case R.id.more_btn:
			mPopWindows.showGroupPopView(mPopList,Gravity.RIGHT,R.drawable.pop_bg,R.color.white,0);
			break;

		default:
			break;
		}
	}

	/*
	 * 下拉刷新
	 * (non-Javadoc)
	 * @see net.smallchat.im.widget.MyPullToRefreshListView.OnChangeStateListener#onChangeState(net.smallchat.im.widget.MyPullToRefreshListView, int)
	 */
	@Override
	public void onChangeState(MyPullToRefreshListView container, int state) {
		mRefreshViewLastUpdated.setText(FeatureFunction.getRefreshTime());
		switch (state) {
		case MyPullToRefreshListView.STATE_LOADING:
			mHandler.sendEmptyMessage(GlobalParam.SHOW_SCROLLREFRESH);
			break;
		}
	}

	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GlobalParam.SHOW_SCROLLREFRESH:
				if (mIsRefreshing) {
					mContainer.onRefreshComplete();
					break;
				}
				mIsRefreshing = true;

				mMyAlbum = null;
				getMeetingData(GlobalParam.LIST_LOAD_REFERSH);
				break;

			case GlobalParam.HIDE_SCROLLREFRESH:
				mIsRefreshing = false;
				mContainer.onRefreshComplete();
				updateListView();
				break;
			case GlobalParam.MSG_SHOW_LISTVIEW_DATA:
			
				updateListView();
				break;
			case GlobalParam.HIDE_LOADINGMORE_INDECATOR:

				if (mListView.getFooterViewsCount() != 0) {
					mListView.removeFooterView(mFootView);
				}
			
				if (mAdapter != null){
					mAdapter.notifyDataSetChanged();
				}
				break;



			default:
				break;
			}
		}

	};

	/*
	 * 子项点击事件
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(0<=arg2 && arg2<mDataList.size()){
			Intent detailIntent = new Intent();
			detailIntent.setClass(mContext, MettingDetailActivity.class);
			detailIntent.putExtra("met_id",mDataList.get(arg2).id);
			startActivity(detailIntent);
		}
	}

	/*
	 * 页面销毁释放通知
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mReceiver!=null){
			unregisterReceiver(mRefreshReceiver);
		}
	}

	
	


}
