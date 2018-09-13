package net.smallchat.im.contact;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.UserList;
import net.smallchat.im.R;
import net.smallchat.im.adapter.BlockListAdapter;
import net.smallchat.im.fragment.ContactsFragment;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.GlobleType;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

/**
 * 黑名单页面
 * @author dongli
 *
 */
public class BlockListActivity extends BaseActivity implements OnItemClickListener, OnClickListener{

	/*
	 * 定义全局变量
	 */
	private boolean mIsRegisterReceiver = false;
	private UserList mUser;
	private boolean mNoMore = false;
	private LinearLayout mFootView;
	private List<Login> mBlockList = new ArrayList<Login>();
	private ListView mListView;
	private BlockListAdapter mAdapter;
	public static final String CANCEL_ACTION = "im_cancel_action";
	public static final String CANCEL_FOLLOW_ACTION = "im_cancel_follow_action";
	private static final int CANCEL_SUCCESS = 15443;
	private static final int CANCEL_FAILED = 15446;
	private static final int HIDE_PROGRESS_DIALOG = 15447;
	private int mType;//0-黑名单 1-关注列表
	private int mIsHide;

	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.block_page);
		mType = getIntent().getIntExtra("type",GlobleType.BLOCKLISTACTIVITY_BLOCK_TYPE);
		mIsHide = getIntent().getIntExtra("ishide",0);
		IntentFilter filter = new IntentFilter();
		filter.addAction(CANCEL_ACTION);
		filter.addAction(CANCEL_FOLLOW_ACTION);
		registerReceiver(mReceiver, filter);
		mIsRegisterReceiver = true;
		initComponent();

		Message message = new Message();
		message.obj = ChatApplication.getInstance().getResources().getString(R.string.add_more_loading);
		message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
		mHandler.sendMessage(message);

		getBlockList(GlobalParam.LIST_LOAD_FIRST,mType);
	}

	/*
	 * 处理通知
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent){
			if(intent != null){
				if(intent.getAction().equals(CANCEL_ACTION)
						|| intent.getAction().equals(CANCEL_FOLLOW_ACTION)){
					String fuid = intent.getStringExtra("fuid");
					Message message = new Message();
					message.obj = ChatApplication.getInstance().getResources().getString(R.string.add_more_loading);
					message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
					mHandler.sendMessage(message);
					int isFollow = intent.getIntExtra("isFollow",1);
					cancelBlock(fuid,isFollow);
				}
			}
		}
	};

	/*
	 * 页面销毁释放通知
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if(mIsRegisterReceiver){
			unregisterReceiver(mReceiver);
		}
		super.onDestroy();
	}

	/*
	 * 实例化控件
	 */
	private void initComponent(){
		if(mType == GlobleType.BLOCKLISTACTIVITY_BLOCK_TYPE){
			setTitleContent(R.drawable.back_btn, 0, R.string.blocked_list);

		}else if(mType == GlobleType.BLOCKLISTACTIVITY_RECOMMEND_TYPE ){
			setTitleContent(R.drawable.back_btn, 0, R.string.tui_user);
		}else if(mType == GlobleType.BLOCKLISTACTIVITY_SUB_SERVER_TYPE){
			setTitleContent(R.drawable.back_btn, 0, R.string.my_focus);
		}else if(mType == GlobleType.BLOCKLISTACTIVITY_USER_FANS_TYPE){
			setTitleContent(R.drawable.back_btn, 0, R.string.fen_list);
		}else if(mType == GlobleType.BLOCKLISTACTIVITY_USER_FOCUS_TYPE){
			setTitleContent(R.drawable.back_btn, 0, R.string.focus_list);
		}
		mLeftBtn.setOnClickListener(this);


		mListView = (ListView) findViewById(R.id.contact_list);

		mListView.setCacheColorHint(0);
		mListView.setOnItemClickListener(this);
		mListView.setDivider(null);
		mListView.setSelector(mContext.getResources().getDrawable(R.drawable.transparent_selector));
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE://处理加载更多

					if(view.getLastVisiblePosition() == (view.getCount()-1) && !mNoMore){
						if (IMCommon.verifyNetwork(mContext)){
							mHandler.sendEmptyMessage(GlobalParam.SHOW_LOADINGMORE_INDECATOR);
						}
						else{
							Toast.makeText(mContext, mContext.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
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
	}

	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case GlobalParam.SHOW_PROGRESS_DIALOG:
				String dialogMsg = (String)msg.obj;
				showProgressDialog(dialogMsg);
				break;
			case GlobalParam.HIDE_PROGRESS_DIALOG:
				hideProgressDialog();
				updateListView();
				break;

			case GlobalParam.MSG_LOAD_ERROR:
				String error_Detail = (String)msg.obj;
				if(error_Detail != null && !error_Detail.equals("")){
					Toast.makeText(mContext,error_Detail,Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(mContext,R.string.load_error,Toast.LENGTH_LONG).show();
				}
				break;
			case GlobalParam.MSG_NETWORK_ERROR:
				Toast.makeText(mContext,R.string.network_error,Toast.LENGTH_LONG).show();
				break;
			case GlobalParam.MSG_TIME_OUT_EXCEPTION:

				String message=(String)msg.obj;
				if (message==null || message.equals("")) {
					message= ChatApplication.getInstance().getResources().getString(R.string.timeout);
				}
				Toast.makeText(mContext,message, Toast.LENGTH_LONG).show();
				break;
			case GlobalParam.SHOW_LOADINGMORE_INDECATOR:
				if (mFootView == null) {
					mFootView = (LinearLayout) LayoutInflater.from(mContext)
							.inflate(R.layout.hometab_listview_footer, null);
				}

				if (mListView.getFooterViewsCount() == 0) {
					mListView.addFooterView(mFootView);	
				}

				ProgressBar pb = (ProgressBar)mFootView.findViewById(R.id.hometab_addmore_progressbar);
				pb.setVisibility(View.VISIBLE);		 		
				TextView more = (TextView)mFootView.findViewById(R.id.hometab_footer_text);
				more.setText(ChatApplication.getInstance().getResources().getString(R.string.add_more_loading));
				getBlockList(GlobalParam.LIST_LOAD_MORE,mType);
				break;
			case GlobalParam.HIDE_LOADINGMORE_INDECATOR:
				if (mListView.getFooterViewsCount() != 0) {
					mListView.removeFooterView(mFootView);
				}
				if (mAdapter != null){
					mAdapter.notifyDataSetChanged();
				}
				break;

			case CANCEL_SUCCESS:
				hideProgressDialog();
				Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.cancel_block_success),Toast.LENGTH_LONG).show();
				getBlockList(GlobalParam.LIST_LOAD_FIRST,mType);
				sendBroadcast(new Intent(ContactsFragment.REFRESH_FRIEND_ACTION));
				break;

			case CANCEL_FAILED:
				hideProgressDialog();
				Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.cancel_block_failed),Toast.LENGTH_LONG).show();
				break;

			case HIDE_PROGRESS_DIALOG:
				hideProgressDialog();
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

		if(arg2 < mBlockList.size()){
			Intent intent = new Intent(mContext, UserInfoActivity.class);
			intent.putExtra("uid", mBlockList.get(arg2).uid);
			intent.putExtra("type",2);
			intent.putExtra("ishide",mIsHide);
			if(mType == GlobleType.BLOCKLISTACTIVITY_BLOCK_TYPE){
				intent.putExtra("is_black_jump", 1);
			}

			startActivity(intent);
		}else if(arg2 == mBlockList.size() && !mNoMore){
			if (IMCommon.verifyNetwork(mContext)){
				mHandler.sendEmptyMessage(GlobalParam.SHOW_LOADINGMORE_INDECATOR);
			} else{
				Toast.makeText(mContext, mContext.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			}
		}
	}

	/*
	 * 显示listview 数据
	 */
	private void updateListView(){

		if(mAdapter != null){
			mAdapter.notifyDataSetChanged();
		}

		if(mUser != null){

			mListView.setVisibility(View.VISIBLE);

			if(mUser.mPageInfo != null){
				int currentPage = mUser.mPageInfo.currentPage + 1;
				if(currentPage > mUser.mPageInfo.totalPage){
					mNoMore = true;
				} else {
					mNoMore = false;
				}
			}

			mAdapter = new BlockListAdapter(mContext, mBlockList,mType);

			mListView.setAdapter(mAdapter); 
		}

	}

	/*
	 * 获取黑名单数据
	 */
	private void getBlockList(final int loadType,final int type){
		new Thread(){

			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){
					new Thread(){
						public void run() {
							try {
								if(type == GlobleType.BLOCKLISTACTIVITY_BLOCK_TYPE){
									mUser = IMCommon.getIMServerAPI().getBlockList(/*currentPage*/);
								}

								if(mUser != null){
									if(mUser.mState != null && mUser.mState.code == 0){
										mNoMore = true;

										if (mBlockList != null) {
											mBlockList.clear();
										}
										if (mUser.mUserList != null) {
											mBlockList.addAll(mUser.mUserList);
										}
									}else {
										Message msg=new Message();
										msg.what=GlobalParam.MSG_LOAD_ERROR;
										if(mUser.mState != null && mUser.mState.errorMsg != null && !mUser.mState.errorMsg.equals("")){
											msg.obj = mUser.mState.errorMsg;
										}else {
											msg.obj = ChatApplication.getInstance().getResources().getString(R.string.load_error);
										}
										mHandler.sendMessage(msg);
									}
								}else {
									mHandler.sendEmptyMessage(GlobalParam.MSG_LOAD_ERROR);
								}


							} catch (IMException e) {
								e.printStackTrace();
								Message msg=new Message();
								msg.what=GlobalParam.MSG_TIME_OUT_EXCEPTION;
								msg.obj= ChatApplication.getInstance().getResources().getString(R.string.timeout);
								mHandler.sendMessage(msg);
							}

							switch (loadType) {
							case GlobalParam.LIST_LOAD_FIRST:
								mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
								break;
							case GlobalParam.LIST_LOAD_MORE:
								mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);

							case GlobalParam.LIST_LOAD_REFERSH:
								mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
								break;

							default:
								break;
							}
						}
					}.start();
				}else {
					switch (loadType) {
					case GlobalParam.LIST_LOAD_FIRST:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
						break;
					case GlobalParam.LIST_LOAD_MORE:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);

					case GlobalParam.LIST_LOAD_REFERSH:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
						break;

					default:
						break;
					}
					mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
				}
			}

		}.start();
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
			this.finish();
			break;

		default:
			break;
		}
	}

	/*
	 * 取消黑名单
	 */
	private void cancelBlock(final String fuid,final int isFollow){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = null;
						if(mType == 1){
							state = IMCommon.getIMServerAPI().addfocus(fuid/*,isFollow*/);
						}else{
							state=IMCommon.getIMServerAPI().cancelBlock(fuid);
						}

						if(state != null && state.code == 0){
							mHandler.sendEmptyMessage(CANCEL_SUCCESS);
						}else {
							mHandler.sendEmptyMessage(CANCEL_FAILED);
						}
					} catch (IMException e) {
						e.printStackTrace();
						mHandler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
						mHandler.sendEmptyMessage(GlobalParam.MSG_TIME_OUT_EXCEPTION);
					}
				}
			}.start();
		}else {
			mHandler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
		}
	}
}
