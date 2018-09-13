package net.smallchat.im.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.MessageTable;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.Room;
import net.smallchat.im.Entity.RoomList;
import net.smallchat.im.R;
import net.smallchat.im.adapter.RoomAdapter;
import net.smallchat.im.contact.ChooseUserActivity;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;
import net.smallchat.im.widget.SearchDialog;

public class MyGroupListActivity extends BaseActivity {
	public final static String DELETE_ROOM_SUCCESS = "im_delete_contact_room_success";
	public final static String DELETE_ROOM_ACTION = "im_delete_room_action";
	public final static String DELETE_ROOM_FAILED = "im_delete_room_failed";
	public final static String MY_ROOM_BE_DELETED_ACTION = "im_my_room_list_be_deleted_action";
	public final static String REFRESH_ROOM_ACTION = "im_refresh_room_list_action";
	public static final String DESTORY_MYGroupList_ACTION = "net.smallchat.im.intent.action.DESTORY_MYGROUPLIST_ACTION";
	private boolean mIsRegisterReceiver = false;
	private ListView mListView;
	private RelativeLayout mTitleLayout;
	private RoomAdapter mAdapter;
	private TextView mGroupCount;
	
	private DisplayMetrics mMetrics;
	private int mWdith;
	private List<Room> mRoomList = new ArrayList<Room>();
	private int mDelIndex;
	private int mIsHideSearcBtn = 0;


	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_tab);
		mContext = this;

		mMetrics= new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
		mIsHideSearcBtn = getIntent().getIntExtra("hide", 0);
		mWdith = mMetrics.widthPixels;
		registerReceiver();
		initCompent();
	}
	
	/*
	 * 注册通知
	 */
	private void registerReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(DELETE_ROOM_SUCCESS);
		filter.addAction(DELETE_ROOM_ACTION);
		filter.addAction(DELETE_ROOM_FAILED);
		filter.addAction(MY_ROOM_BE_DELETED_ACTION);
		filter.addAction(REFRESH_ROOM_ACTION);
		filter.addAction(DESTORY_MYGroupList_ACTION);
		filter.addAction(GlobalParam.ACTION_RESET_GROUP_NAME);
		registerReceiver(mReceiver, filter);
	}
	
	/*
	 * 处理通知
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent == null){
				return ;
			}
			String action = intent.getAction();
			if(action.equals(DELETE_ROOM_ACTION)){
				
			}else if(action.equals(DESTORY_MYGroupList_ACTION)){//释放页面
				MyGroupListActivity.this.finish();
			}
			else if(action.equals(DELETE_ROOM_SUCCESS)){//删除群承购
				int delPos = intent.getIntExtra("del_index", -1);
				if(delPos!=-1){
					delete(delPos);
					hideProgressDialog();
					Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.operate_success), Toast.LENGTH_SHORT).show();

				}
				
			}else if(action.equals(DELETE_ROOM_FAILED)){//删除群失败
				hideProgressDialog();
				Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.operate_failed), Toast.LENGTH_SHORT).show();
			}else if(action.equals(MY_ROOM_BE_DELETED_ACTION)){//我被管理员移除了
				Log.e("MyGropListActivity", "destroy_room");
				String groomID = intent.getStringExtra("roomID");
				if((groomID!=null && !groomID.equals("")) &&(mRoomList!=null && mRoomList.size()>0)){
					for (int i = 0; i < mRoomList.size(); i++) {
						if(mRoomList.get(i).groupId.equals(groomID)){
							SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
							MessageTable messageTable = new MessageTable(db);
							messageTable.delete(mRoomList.get(i).groupId, mRoomList.get(i).isOwner);
							SessionTable sessionTable = new SessionTable(db);
							sessionTable.delete(mRoomList.get(i).groupId, mRoomList.get(i).isOwner);
							mRoomList.remove(i);
							mAdapter.notifyDataSetChanged();
						}
					}
					mGroupCount.setText(mRoomList.size()+mContext.getResources().getString(R.string.group_chat_count));
				}
			}else if(action.equals(REFRESH_ROOM_ACTION)){//刷新群信息
				getGroupList(false);
			}else if(action.equals(GlobalParam.ACTION_RESET_GROUP_NAME)){//重置群名称
				String groupId = intent.getStringExtra("group_id");
				String groupName = intent.getStringExtra("group_name");
				if((groupId!=null && !groupId.equals(""))
						&& (groupName!=null && !groupName.equals(""))){
					for (int i = 0; i < mRoomList.size(); i++) {
						if(mRoomList.get(i).groupId.equals(groupId)){
							mRoomList.get(i).groupName = groupName;
							if(mAdapter!=null){
								mAdapter.notifyDataSetChanged();
							}
						}
					}
				}
			}
			
		}
	};

	
	/*
	 * 实例化控件
	 */
	private void initCompent(){
		mTitleLayout = (RelativeLayout)findViewById(R.id.title_layout);
		mTitleLayout.setVisibility(View.VISIBLE);
		mGroupCount = (TextView)findViewById(R.id.group_count);
		if(mIsHideSearcBtn == 1){
			setTitleContent(R.drawable.back_btn,0,R.string.group_chat_list);
			mGroupCount.setVisibility(View.GONE);
		}else{
			setTitleContent(R.drawable.back_btn,true,true,false,R.string.group_chat);
			mSearchBtn.setOnClickListener(this);
			mAddBtn.setOnClickListener(this);
		}
		
		mLeftBtn.setOnClickListener(this);
		
		
	
		mListView = (ListView) findViewById(R.id.chats_list);
		mListView.setCacheColorHint(0);
		mListView.setDivider(null);

		getGroupList(true);
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
			MyGroupListActivity.this.finish();
			break;
		case R.id.search_btn://搜索群
			SearchDialog searchDialog = new SearchDialog(mContext,mAdapter.getUserList());
			searchDialog.show();
			break;
		case R.id.add_btn://添加群组
			Intent intent = new Intent(mContext, ChooseUserActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

	/*
	 * 获取分组数据
	 */
	private void getGroupList(final boolean isShowProgress){
		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					if(isShowProgress){
						IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,"数据加载中,请稍后...");
					}
					RoomList roomList = IMCommon.getIMServerAPI().getRoomList(null);
					if(mRoomList!=null && mRoomList.size()>0){
						mRoomList.clear();
					}
					if(roomList.mRoomList!=null && roomList.mRoomList.size()>0){
						mRoomList.addAll(roomList.mRoomList);
					
					}
					if(isShowProgress){
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					}
					mHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_LISTVIEW_DATA);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR, 
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					if(isShowProgress){
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					}
				}
			};
		}.start();
	};
	

	/*
	 * 删除群组
	 */
	private void delete(int pos){
		SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
		MessageTable messageTable = new MessageTable(db);
		messageTable.delete(mRoomList.get(pos).groupId, mRoomList.get(pos).isOwner);
		SessionTable sessionTable = new SessionTable(db);
		sessionTable.delete(mRoomList.get(pos).groupId, mRoomList.get(pos).isOwner);
		mRoomList.remove(pos);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		//if(mIsRegisterReceiver){
			unregisterReceiver(mReceiver);
		//}
		super.onDestroy();
	}

	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GlobalParam.MSG_SHOW_LISTVIEW_DATA:
				if(mAdapter!=null){
					mAdapter.notifyDataSetChanged();
				}else{
					mAdapter = new RoomAdapter(mContext, mRoomList,mWdith,new ArrayList<Login>(),mIsHideSearcBtn);
					mListView.setAdapter(mAdapter);
				}
				if(mIsHideSearcBtn == 1){
					mGroupCount.setVisibility(View.GONE);
				}else{
					mGroupCount.setVisibility(View.VISIBLE);
				}
				
				mGroupCount.setText(mRoomList.size()+mContext.getResources().getString(R.string.group_chat_count));
				break;

			default:
				break;
			}
		}
		
	};
	
}
