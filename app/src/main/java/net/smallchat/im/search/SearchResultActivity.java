package net.smallchat.im.search;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.UserList;
import net.smallchat.im.R;
import net.smallchat.im.adapter.SearchResultAdapter;
import net.smallchat.im.contact.UserInfoActivity;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

/**
 * 搜索结果
 * @author dongli
 *
 */
public class SearchResultActivity extends BaseActivity implements OnClickListener, OnItemClickListener{

	public static final String REFRSCH_ITEM_ACTION = "net.smallchat.im.intent.action.refresh_ACTION";

	private ListView mListView;

	private UserList mUser;
	private List<Login> mUserList = new ArrayList<Login>();
	private SearchResultAdapter mAdapter;
	private LinearLayout mFootView;

	private String mSearchContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		mContext = this;
		mUser = (UserList)getIntent().getSerializableExtra("user_list");
		mSearchContent = getIntent().getStringExtra("searchContent");
		initCompent();
		registerReceiver();
	}

	private void registerReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(REFRSCH_ITEM_ACTION);

		registerReceiver(chatReceiver, filter);
	}

	/**  聊天广播 */
	private BroadcastReceiver chatReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(REFRSCH_ITEM_ACTION.equals(action)){
				String uid = intent.getStringExtra("uid");
				int userType = intent.getIntExtra("user_type",0);
				if(uid!=null && !uid.equals("")){
					for (int i = 0; i < mUserList.size(); i++) {
						if(mUserList.get(i).uid.equals(uid)){
							mUserList.get(i).userType = userType;
							if(mAdapter!=null){
								mAdapter.notifyDataSetChanged();
								return;
							}
						}
					}
				}
			}
		}
	};

	private void initCompent(){
		setTitleContent(R.drawable.back_btn,0,"用户列表");
		mLeftBtn.setOnClickListener(this);

		mListView = (ListView)findViewById(R.id.result_list);
		mListView.setDivider(null);
		mListView.setCacheColorHint(0);

		if(mUser!=null){
			if(mUser.mUserList!=null && mUser.mUserList.size()>0){
				mUserList.addAll(mUser.mUserList);
			}
		}
		mAdapter = new SearchResultAdapter(mContext,mUserList,false,null,true);
		boolean isLoadMore = (mUser!=null && mUser.mPageInfo.currentPage == mUser.mPageInfo.totalPage)?false:true;
		if (isLoadMore) {
			if (mFootView == null) {
				mFootView = (LinearLayout) LayoutInflater.from(mContext)
						.inflate(R.layout.hometab_listview_footer, null);
			}
			if (mListView.getFooterViewsCount() == 0) {
				mListView.addFooterView(mFootView);	
			}
		}
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getLastVisiblePosition() == mUserList.size()/* && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE*/) {
					if(mUser == null){
						return;
					}
					if ( mUser.mPageInfo.hasMore == 1) {
						if (mFootView == null) {
							mFootView = (LinearLayout) LayoutInflater.from(mContext)
									.inflate(R.layout.hometab_listview_footer, null);
						}

						ProgressBar pb = (ProgressBar)mFootView.findViewById(R.id.hometab_addmore_progressbar);
						pb.setVisibility(View.VISIBLE);		 		
						TextView more = (TextView)mFootView.findViewById(R.id.hometab_footer_text);
						more.setText(mContext.getString(R.string.add_more_loading));
						if (mListView.getFooterViewsCount() == 0) {
							mListView.addFooterView(mFootView);	
						}
						loadMoreUser();

					}else{//没有更多数据时
						if (mFootView == null) {
							mFootView = (LinearLayout) LayoutInflater.from(mContext)
									.inflate(R.layout.hometab_listview_footer, null);
						}
						ProgressBar pb = (ProgressBar)mFootView.findViewById(R.id.hometab_addmore_progressbar);
						pb.setVisibility(View.GONE);	
						TextView more = (TextView)mFootView.findViewById(R.id.hometab_footer_text);
						more.setText("");
						if (mListView.getFooterViewsCount() == 0) {
							mListView.addFooterView(mFootView);	
						}

					}

				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
	}



	private void loadMoreUser(){
		if (!IMCommon.getNetWorkState()) {
			mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {

					boolean isExitsData = true;
					if (mUser!=null && mUser.mPageInfo.currentPage == mUser.mPageInfo.totalPage) {
						isExitsData = false;
					}
					int	page = mUser.mPageInfo.currentPage+1;
					if (isExitsData) {
						mUser = IMCommon.getIMServerAPI().search_number(mSearchContent,page);

						if (mUser != null && mUser.mUserList!=null && mUser.mUserList.size() > 0) {
							isExitsData = true;
							mUserList.addAll(mUser.mUserList); 
						} else{
							isExitsData = false;
						}
					}
					mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
					if (!isExitsData) {
						mHandler.sendEmptyMessage(GlobalParam.MSG_CHECK_STATE);
					}

				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler,BASE_MSG_NETWORK_ERROR, 
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
				}
			};
		}.start();
	}



	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			SearchResultActivity.this.finish();
			break;

		default:
			break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (0<=arg2 && arg2<mUserList.size()) {
			Intent profileDetailIntent = new Intent();
			profileDetailIntent.setClass(mContext, UserInfoActivity.class);
			Login login = mUserList.get(arg2);
			profileDetailIntent.putExtra("type", 1);
			profileDetailIntent.putExtra("user",login);
			startActivity(profileDetailIntent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(chatReceiver);
	}


	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {


			case GlobalParam.HIDE_LOADINGMORE_INDECATOR:

				if (mAdapter != null){
					mAdapter.notifyDataSetChanged();
				}
				break;
			case GlobalParam.MSG_CHECK_STATE:
				if (mFootView != null && mListView.getFooterViewsCount()>0) {
					mListView.removeFooterView(mFootView); 
				}
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				break;
			default:
				break;
			}
		}

	};



}
