package net.smallchat.im.meeting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
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
import net.smallchat.im.dialog.MMAlert;
import net.smallchat.im.dialog.MMAlert.OnAlertSelectId;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.GlobleType;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

/**
 * 申请列表
 * @author dongli
 *
 */
public class ApplyMetListActivity extends BaseActivity implements OnItemClickListener{

	/*
	 * 定义全局变量
	 */
	public static final String REMOVE_USER_ACTION = "im_remove_uesr_action";
	
	private ListView mListView;
	private LinearLayout mFootView;
	private UserList mUser;
	private boolean mNoMore = false;
	private List<Login> mBlockList = new ArrayList<Login>();
	private BlockListAdapter mAdapter;
	
	private int mMeetingId;
	private int mType ;
	
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
		mMeetingId = getIntent().getIntExtra("met_id", 0);
		mType = getIntent().getIntExtra("type",0);
		IntentFilter filter = new IntentFilter();
		filter.addAction(REMOVE_USER_ACTION);
		registerReceiver(mBroadcastReceiver, filter);
		initCompent();
		getUserList(GlobalParam.LIST_LOAD_FIRST,0);
	}

	/*
	 * 处理通知
	 */
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent!=null){
				String action = intent.getAction();
				if(action.equals(REMOVE_USER_ACTION)){
					String fuid = intent.getStringExtra("fuid");
					int pos = intent.getIntExtra("pos", -1);
					if((fuid!=null && !fuid.equals("")) && pos !=-1){
						removeMetUser(fuid,pos);
					}
				}
			}
		}
	};
	
	/*
	 * 实例化控件
	 */
	private void initCompent(){
		if(mType == 1){
			setTitleContent(R.drawable.back_btn,0,R.string.top_list);
		}else{
			setTitleContent(R.drawable.back_btn,0,R.string.apply_met_list);
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
						}else{
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
	 * 显示listviw 数据
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
    		if(mType == 1){
    			 mAdapter = new BlockListAdapter(mContext, mBlockList,GlobleType.BLOCKLISTACTIVITY_TOP_MEETING_USER_TYPE);
    		}else{
    			 mAdapter = new BlockListAdapter(mContext, mBlockList,GlobleType.BLOCKLISTACTIVITY_APPLY_MEETING_USER_TYPE);
    		}
    	
    		mListView.setAdapter(mAdapter); 
    	}
 		
    }
	
	/*
	 * 获取用户数据
	 */
	private void getUserList(final int loadType,final int type){
		new Thread(){
			
			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){
					new Thread(){
						public void run() {
							try {
														
								int currentPage = 1;
								switch (loadType) {
								case GlobalParam.LIST_LOAD_FIRST:
								case GlobalParam.LIST_LOAD_REFERSH:
									currentPage = 1;
									break;
								case GlobalParam.LIST_LOAD_MORE:
									if(mUser != null && mUser.mPageInfo != null){
										currentPage = mUser.mPageInfo.currentPage + 1;
										if(currentPage >= mUser.mPageInfo.totalPage){
											mNoMore = true;
										} else {
						    				mNoMore = false;
						    			}
									}
									break;
								default:
									break;
								}
								
								if(mType == 1){
									mUser = IMCommon.getIMServerAPI().huoyueList(currentPage,mMeetingId);
								}else{
									mUser = IMCommon.getIMServerAPI().meetingApplyList(currentPage,mMeetingId);
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
			ApplyMetListActivity.this.finish();
			break;

		default:
			break;
		}
	}

	/*
	 * listview 子项点击事件
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(0<=arg2 && arg2<mBlockList.size() ){
			if(mType !=1){
				showBottomMenu(mMeetingId,mBlockList.get(arg2).uid,arg2);
			}
		}
	}
	
	/*
	 * 处理用户功能
	 */
	private void showBottomMenu(final int metId,final String fuid,final int arg2){
		MMAlert.showAlert(mContext, "", mContext.getResources().
				getStringArray(R.array.appley_meeting_menu_item), 
				null, new OnAlertSelectId() {

			@Override
			public void onClick(int whichButton) {
				switch (whichButton) {
				case 0://同意加入
					agreeApplyMeeting(fuid,arg2);
					break;
				case 1://拒接加入
					disAgreeApplyMeeting(fuid,arg2);
					break;
				default:
					break;
				}
			}
		});
	}
	
	/*
	 * 同意加入
	 */
	private void agreeApplyMeeting(final String fuid,final int pos){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().agreeApplyMeeting(mMeetingId,fuid);
						if(state!=null){
							state.positon = pos;
						}
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
						IMCommon.sendMsg(mHandler,GlobalParam.MSG_AGREE_APPLY_METTING,state);
					} catch (IMException e) {
						e.printStackTrace();
						mHandler.sendEmptyMessage(GlobalParam.MSG_TIME_OUT_EXCEPTION);
					}catch (Exception e) {
						e.printStackTrace();
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					}
				}
			}.start();
		}else {
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
		}
	}
	
	/*
	 * 拒接加入
	 */
	private void disAgreeApplyMeeting(final String fuid,final int pos){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().disagreeApplyMeeting(mMeetingId, fuid);
						if(state!=null){
							state.positon = pos;
						}
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
						IMCommon.sendMsg(mHandler,GlobalParam.MSG_DIS_AGREE_APPLY_METTING,state);
					} catch (IMException e) {
						e.printStackTrace();
						mHandler.sendEmptyMessage(GlobalParam.MSG_TIME_OUT_EXCEPTION);
					}catch (Exception e) {
						e.printStackTrace();
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					}
				}
			}.start();
		}else {
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
		}
	}
	
	/*
	 * 移除用户
	 */
	private void removeMetUser(final String fuid,final int pos){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().removeMetUser(mMeetingId, fuid);
						if(state!=null){
							state.positon = pos;
						}
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
						IMCommon.sendMsg(mHandler,GlobalParam.MSG_DIS_AGREE_APPLY_METTING,state);
					} catch (IMException e) {
						e.printStackTrace();
						mHandler.sendEmptyMessage(GlobalParam.MSG_TIME_OUT_EXCEPTION);
					}catch (Exception e) {
						e.printStackTrace();
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					}
				}
			}.start();
		}else {
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
		}
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
			case GlobalParam.MSG_AGREE_APPLY_METTING:
				IMResponseState agreeStatus = (IMResponseState)msg.obj;
				if(agreeStatus == null){
					Toast.makeText(mContext, R.string.commit_data_error,Toast.LENGTH_LONG).show();
					return;
				}
				if(agreeStatus.code == 0){
					if(mBlockList!=null && mBlockList.size()>0){
						mBlockList.remove(agreeStatus.positon);
						if(mAdapter!=null){
							mAdapter.notifyDataSetChanged();
						}
						mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MEETING_LIST));
					}
				}else{
					Toast.makeText(mContext, agreeStatus.errorMsg,Toast.LENGTH_LONG).show();
				}
				break;
			case GlobalParam.MSG_DIS_AGREE_APPLY_METTING:
				IMResponseState disAgreeStatus = (IMResponseState)msg.obj;
				if(disAgreeStatus == null){
					Toast.makeText(mContext, R.string.commit_data_error,Toast.LENGTH_LONG).show();
					return;
				}
				if(disAgreeStatus.code == 0){
					if(mBlockList!=null && mBlockList.size()>0){
						mBlockList.remove(disAgreeStatus.positon);
						mAdapter.notifyDataSetChanged();
					}
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MEETING_LIST));
				}else{
					Toast.makeText(mContext, disAgreeStatus.errorMsg,Toast.LENGTH_LONG).show();
				}
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
		 		getUserList(GlobalParam.LIST_LOAD_MORE,0);
				break;
			case GlobalParam.HIDE_LOADINGMORE_INDECATOR:
				if (mListView.getFooterViewsCount() != 0) {
					mListView.removeFooterView(mFootView);
				}
	
				if (mAdapter != null){
					mAdapter.notifyDataSetChanged();
				}
				break;
		
			}
		}
	};

}
