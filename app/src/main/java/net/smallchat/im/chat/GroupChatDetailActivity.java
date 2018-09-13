package net.smallchat.im.chat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.smallchat.im.Entity.ChatType;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.contact.AddPersonActivity;
import net.smallchat.im.BaseActivity;
import net.smallchat.im.room.GroupCodeActivity;
import net.smallchat.im.mine.MyGroupListActivity;
import net.smallchat.im.R;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.MessageTable;
import net.smallchat.im.DB.RoomTable;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.DB.UserTable;
import net.smallchat.im.Entity.ChatDetailEntity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.Room;
import net.smallchat.im.Entity.Session;
import net.smallchat.im.contact.UserInfoActivity;
import net.smallchat.im.mine.WriteUserInfoActivity;
import net.smallchat.im.adapter.ChatPersonAdapter;
import net.smallchat.im.dialog.MMAlert;
import net.smallchat.im.dialog.MMAlert.OnAlertSelectId;
import net.smallchat.im.fragment.ChatFragment;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.GlobleType;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;
import net.smallchat.im.widget.MyGridView;

/**
 * 群资料
 * @author dongli
 *
 */
public class GroupChatDetailActivity extends BaseActivity implements OnClickListener, OnItemClickListener, OnItemLongClickListener{

	/*
	 * 定义全局变量
	 */
	public static final String DESTORY_CHAT_DETAIL_ACTION = "net.smallchat.im.intent.action.destroy.chatdetail.action";
	private MyGridView mGridView;

	private TextView mGroupNameView,mClearMessageBtn;
	private RelativeLayout  mExitLayout;
	private LinearLayout mChatDetailLayout,mGroupNameLayout,mGroupCodeLayout,
	mMyGroupNickNameLayout,mShowPartnerNickNameLayout;
	private TextView mGroupNameTextView,mMyGroupNickNameTextView;
	private TextView mSearchRecordBtn;


	private TextView mClearTextView;
	private ToggleButton mTipNewMsgBtn,mShowPartnerNickBtn,mTopMsgBtn;

	private ChatPersonAdapter mAdapter;
	private List<Login> mUserList = new ArrayList<Login>();
	private List<ChatDetailEntity> mList = new ArrayList<ChatDetailEntity>();
	private String mGroupID = "";
	private Room mGroupDetail;
	private ScrollView mScrollView;
	private final static int KICK_SUCCESS = 0x00001;
	private final static int INVITE_SUCCESS = 0x000031;
	private final static int INVITE_REQUEST = 1110;
	private static final int MODIFY_REQUEST = 5124;

	private int mIsSignChat = 1;
	private Login mToChatLogin;
	private String mUids="";
	private String mNickName="";

	private String mInputGroupName,mInputGroupNickName ;
	private boolean mIsTopMsg; //是否置顶聊天记录

	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		sendBroadcast(new Intent(DESTORY_CHAT_DETAIL_ACTION));
		setContentView(R.layout.group_chat_detail_page);
		initComponent();
		initRegister();
	}

	/*
	 * 实例化更多菜单
	 */
	private void initRegister(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.ACTION_RESET_GROUP_NAME);
		filter.addAction(GlobalParam.ACTION_RESET_MY_GROUP_NAME);
		filter.addAction(DESTORY_CHAT_DETAIL_ACTION);
		registerReceiver(mReceiver, filter);
	}


	/*
	 * 处理通知
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent != null){
				String action = intent.getAction();
				if(action.equals(GlobalParam.ACTION_RESET_GROUP_NAME)){
					String groupId = intent.getStringExtra("groupId");
					String groupName = intent.getStringExtra("group_name");
					if((groupId!=null && !groupId.equals(""))
							&& (groupName!=null && !groupName.equals(""))){
						if(mGroupDetail!=null &&
								(mGroupDetail.groupId!=null && !mGroupDetail.groupId.equals(""))){
							if(mGroupDetail.groupId.equals(groupId)){
								mGroupNameView.setText(groupName);
							}
						}
					}
				}else if(action.equals(GlobalParam.ACTION_RESET_MY_GROUP_NAME)){
					String myGroupNickName = intent.getStringExtra("my_group_nickname");
					String groupId = intent.getStringExtra("group_id");
					String uid = intent.getStringExtra("uid");
					if((myGroupNickName != null && !myGroupNickName.equals(""))
							&& (groupId != null && !groupId.equals("") )
							&& (uid != null && !uid.equals(""))){
						if(groupId.equals(mGroupDetail.groupId)){
							if(mList!=null && mList.size()>0){
								for (int i = 0; i < mList.size(); i++) {
									if(mList.get(i).mLogin!=null && mList.get(i).mLogin.uid.equals(uid)){
										mList.get(i).mLogin.nickname = myGroupNickName;
										if(mAdapter!=null){
											mAdapter.notifyDataSetChanged();
										}
										return;
									}
								}
							}
						}
					}
				}else if(action.equals(DESTORY_CHAT_DETAIL_ACTION)){
					GroupChatDetailActivity.this.finish();
				}
			}
		}

	};


	/*
	 * 示例化控件
	 */
	private void initComponent(){

		setTitleContent(R.drawable.back_btn, 0, mContext.getString(R.string.chat_detail_title));
		mLeftBtn.setOnClickListener(this);

		mGroupID = getIntent().getStringExtra("groupid");
		mIsSignChat = getIntent().getIntExtra("isSignChat",0);
		mIsTopMsg = getIntent().getBooleanExtra("isTop",false);
		if(mIsSignChat == 1){
			mToChatLogin = (Login)getIntent().getSerializableExtra("to_login");
		}

		mScrollView =(ScrollView) findViewById(R.id.scrollView);
		mScrollView.setVisibility(View.GONE);

		mChatDetailLayout = (LinearLayout)findViewById(R.id.group_normal_layout);
		mSearchRecordBtn = (TextView)findViewById(R.id.find_chat_message);
		mSearchRecordBtn.setOnClickListener(this);

		mTipNewMsgBtn = (ToggleButton)findViewById(R.id.tip_message_btn);

		SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getWritableDatabase();
		if(mIsSignChat == 1){

			UserTable userTable = new UserTable(dbDatabase);
			Login login = userTable.query(mGroupID);
			mTipNewMsgBtn.setChecked(login.isGetMsg == 1?true:false);
		}



		mTopMsgBtn = (ToggleButton)findViewById(R.id.top_chat_btn);
		mTopMsgBtn.setChecked(mIsTopMsg);
		mShowPartnerNickBtn = (ToggleButton)findViewById(R.id.show_partner_nickname_btn);

		mTipNewMsgBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked ){
					if(mIsSignChat == 1){
						SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getWritableDatabase();
						UserTable userTable = new UserTable(dbDatabase);
						Login login = userTable.query(mGroupID);
						if(login!=null && login.isGetMsg == 1){
							return;
						}
					}else{
						if(mGroupDetail!=null &&  mGroupDetail.isgetmsg==1){
							return;
						}
					}
					toggleListener(isChecked, 2);
				}else{
					if(mIsSignChat == 1){
						SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getWritableDatabase();
						UserTable userTable = new UserTable(dbDatabase);
						Login login = userTable.query(mGroupID);
						if(login!=null && login.isGetMsg == 0){
							return;
						}
					}else{
						if(mGroupDetail.isgetmsg == 0){
							return;
						}
					}

					toggleListener(isChecked, 2);
				}

			}
		});
		mTopMsgBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked ){
					if(mIsTopMsg){
						return;
					}else{//不置顶-置顶
						SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getWritableDatabase();
						SessionTable sessionTable = new SessionTable(dbDatabase);
						Session existSession = sessionTable.query(mGroupID,mIsSignChat==1? ChatType.PrivateMessage:ChatType.GroupMessage);
						if(existSession!=null && existSession.isTop <= 0){
							existSession.isTop =  sessionTable.getTopSize()+1;
							sessionTable.update(existSession, existSession.type);
							sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
						}
						mIsTopMsg = true;
					}
				}else{
					if(!mIsTopMsg){
						return;
					}else{//置顶-不置顶
						SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getWritableDatabase();
						SessionTable sessionTable = new SessionTable(dbDatabase);
						Session existSession = sessionTable.query(mGroupID,mIsSignChat==1?ChatType.PrivateMessage:ChatType.GroupMessage);
						if(existSession!=null && existSession.isTop >=1){
							List<Session> exitsSesList = sessionTable.getTopSessionList();
							if(exitsSesList!=null && exitsSesList.size()>0){
								for (int i = 0; i < exitsSesList.size(); i++) {
									Session ses = exitsSesList.get(i);
									if(ses.isTop>1){
										ses.isTop = ses.isTop-1;
										sessionTable.update(ses, ses.type);
									}
								}
							}
							existSession.isTop =  0;
							sessionTable.update(existSession, existSession.type);
							sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
						}
						mIsTopMsg = false;
					}
				}
			}
		});

		mShowPartnerNickBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				boolean isShow = mGroupDetail.isShowNickname == 1?true:false;
				if(isChecked && isShow){
					return;
				}else if(!isChecked && !isShow ){
					return;
				}
				Intent intent = new Intent(ChatMainActivity.ACTION_SHOW_NICKNAME);
				intent.putExtra("is_show_nickname",isChecked);
				if(mGroupDetail.isShowNickname ==1){
					mGroupDetail.isShowNickname =0;
				}else{
					mGroupDetail.isShowNickname=1;
				}

				SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
				RoomTable roomTab = new RoomTable(db);
				roomTab.update(mGroupDetail);
				sendBroadcast(intent);

			}
		});


		mClearMessageBtn = (TextView)findViewById(R.id.clear_chat_message);
		mClearMessageBtn.setOnClickListener(this);
		mExitLayout = (RelativeLayout) findViewById(R.id.exit_chat_layout);
		mExitLayout.setOnClickListener(this);
		mClearTextView = (TextView)findViewById(R.id.clear);


		mGroupNameView = (TextView) findViewById(R.id.group_name);

		mGridView = (MyGridView) findViewById(R.id.gridview);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);


		mGroupNameLayout = (LinearLayout)findViewById(R.id.group_name_layout);
		mGroupCodeLayout = (LinearLayout)findViewById(R.id.group_code_layout);
		mMyGroupNickNameLayout = (LinearLayout)findViewById(R.id.my_group_name_layout);
		mShowPartnerNickNameLayout = (LinearLayout)findViewById(R.id.show_partner_nickname_layout);

		mGroupCodeLayout.setOnClickListener(this);
		mMyGroupNickNameLayout.setOnClickListener(this);
		mGroupNameTextView = (TextView)findViewById(R.id.group_name);
		mMyGroupNickNameTextView = (TextView)findViewById(R.id.my_group_name);


		if(mIsSignChat == 1){
			showSignChatLayout();
		}else{
			IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
					mContext.getResources().getString(R.string.add_more_loading)); 
			getGroupDetail();
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
			unregisterReceiver(mReceiver);
		}

	}

	/*
	 * 按钮点击事件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.left_btn:
			this.finish();
			break;
		case R.id.group_name_layout://分组名称
			Intent intent = new Intent();
			intent.setClass(mContext, WriteUserInfoActivity.class);
			intent.putExtra("type",GlobleType.MODIFY_GROUP_INFO);
			intent.putExtra("content", mGroupDetail.groupName);
			startActivityForResult(intent, MODIFY_REQUEST);
			break;
		case R.id.group_code_layout://群二维码
			//room_id
			Intent codeIntent = new Intent();
			codeIntent.setClass(mContext, GroupCodeActivity.class);
			codeIntent.putExtra("room_id",mGroupDetail.groupId);
			codeIntent.putExtra("sString", getString());
			startActivity(codeIntent);
			break;
		case R.id.my_group_name_layout:
			Intent nickNameIntent = new Intent();
			nickNameIntent.setClass(mContext, WriteUserInfoActivity.class);
			nickNameIntent.putExtra("type",GlobleType.MODIFY_GROUP_NICKNAME);
			nickNameIntent.putExtra("content", mGroupDetail.groupnickname);
			startActivityForResult(nickNameIntent, MODIFY_REQUEST);
			break;
		case R.id.find_chat_message:
			Intent searchIntent = new Intent();
			searchIntent.setClass(mContext, ChatMainActivity.class);

			Login user = new Login();
			if(mIsSignChat == 1){
				user.uid = mToChatLogin.uid;
				user.nickname = mToChatLogin.nickname;
				user.headSmall = mToChatLogin.headSmall;
				user.mIsRoom = 100;
			}else{
				user.uid = mGroupDetail.groupId;
				user.nickname = mGroupDetail.groupName;
				List<Login> roomUsrList = mGroupDetail.mUserList;
				String groupHeadUrl="";
				if (roomUsrList != null && roomUsrList.size()>0) {
					//RoomUserTable roomUserTable = new RoomUserTable(db);
					int size = 4;
					if(roomUsrList.size()<4){
						size = roomUsrList.size();
					}
					for (int j = 0; j < size; j++) {
						if(mGroupDetail.groupCount-1 == j){
							groupHeadUrl+=roomUsrList.get(j).headSmall;
						}else{
							groupHeadUrl+=roomUsrList.get(j).headSmall+",";
						}
					}
				}
				user.headSmall = groupHeadUrl;
				user.mIsRoom = 300;
			}

			//user.headSmall = mSessionList.get(position).heading;

			searchIntent.putExtra("data", user);
			searchIntent.putExtra("is_show_dialog",1);
			startActivity(searchIntent);
			break;
		case R.id.clear_chat_message:
			showPromptDialog(mContext);
			break;

		case R.id.exit_chat_layout://退出登陆
			if(mGroupDetail.isOwner == 1){
				deleteRoom(mGroupID);
			}else{
				exitRoom(mGroupID);
			}
			break;
		default:
			break;
		}
	}


	/*
	 * 子项点击事件
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(arg2 < mList.size()){
			if(mList.get(arg2).mType == 0){
				if(mAdapter.getIsDelete()){
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
							mContext.getResources().getString(R.string.send_request)); 
					kickPerson(arg2);
				}else {
					if(mIsSignChat == 1){
						Intent intent = new Intent(mContext, UserInfoActivity.class);
						intent.putExtra("uid", mToChatLogin.uid);
						intent.putExtra("type",2);
						startActivity(intent);

					}else{
						Intent intent = new Intent(mContext, UserInfoActivity.class);
						intent.putExtra("uid", mGroupDetail.mUserList.get(arg2).uid);
						intent.putExtra("type",2);
						if(mGroupDetail.mUserList.get(arg2).uid.equals(IMCommon.getUserId(mContext))){
							intent.putExtra("isLogin",1);
						}
						startActivity(intent);
					}

				}

			}else if(mList.get(arg2).mType == 1){
				if(mAdapter.getIsDelete()){
					mAdapter.setIsDelete(false);
					mAdapter.notifyDataSetChanged();
				}else {
					Intent intent = new Intent(mContext, AddPersonActivity.class);
					//intent.putExtra("users", (Serializable)mGroupDetail.mUserList);^^
					if(mGroupDetail !=null && mGroupDetail.mUserList!=null
							&& mGroupDetail.mUserList.size()>0){
						intent.putExtra("users", (Serializable)mGroupDetail.mUserList);
					}
					if(mIsSignChat == 1){
						mGroupDetail = new Room();
						mGroupDetail.mUserList = new ArrayList<Login>();
						mGroupDetail.mUserList.add(mToChatLogin);
						intent.putExtra("is_sign_chat", mIsSignChat);
						intent.putExtra("users", (Serializable)mGroupDetail.mUserList);
						startActivity(intent);
					}else{
						startActivityForResult(intent,INVITE_REQUEST);
					}
				}

			}else {
				if(mAdapter.getIsDelete()){
					mAdapter.setIsDelete(false);
					mAdapter.notifyDataSetChanged();
				}else {
					mAdapter.setIsDelete(true);
					mAdapter.notifyDataSetChanged();
				}

			}
		}else {
			if(mAdapter.getIsDelete()){
				mAdapter.setIsDelete(false);
				mAdapter.notifyDataSetChanged();
			}
		}

	}

	/**
	 * 清楚聊天记录
	 * @param context
	 */
	private void showPromptDialog(final Context context){

		MMAlert.showAlert(mContext, "", mContext.getResources().
				getStringArray(R.array.clear_session_item), 
				null, new OnAlertSelectId() {

			@Override
			public void onClick(int whichButton) {
				switch (whichButton) {
				case 0:
					SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
					MessageTable messageTable = new MessageTable(db);
					if(mIsSignChat == 1){
						messageTable.delete(mGroupID, 100);
					}else{
						messageTable.delete(mGroupID, 300);
					}

					/*SessionTable sessionTable = new SessionTable(db);
					if(mIsSignChat == 1){
						sessionTable.delete(mGroupID, 100);
					}else{
						sessionTable.delete(mGroupID, 300);
					}*/

					Intent chatIntent = new Intent(ChatMainActivity.REFRESH_ADAPTER);
					chatIntent.putExtra("id", mGroupID);
					mContext.sendBroadcast(chatIntent);
					mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
					break;

				default:
					break;
				}
			}
		});

	}


	/*
	 * 子项长按显示删除按钮
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if(arg2 < mList.size()){
			if(mList.get(arg2).mType == 0){
				if(!mAdapter.getIsDelete()){
					mAdapter.setIsDelete(true);
					mAdapter.notifyDataSetChanged();
				}
				return true;
			}
		}

		return false;
	}

	/*
	 * 页面回调事件
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case INVITE_REQUEST:
			if(resultCode == RESULT_OK){
				List<Login> userList = (List<Login>) data.getSerializableExtra("userlist");

				if(userList != null  && userList.size() != 0){

					if(mIsSignChat == 1){
						userList.add(mToChatLogin);
						createRoom(userList);
					}else{

						IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
								mContext.getResources().getString(R.string.send_request)); 
						inviteUser(userList);
					}

				}

			}
			break;
		case MODIFY_REQUEST:
			if(resultCode == RESULT_OK){
				String groupName = data.getStringExtra("group_name");
				String groupNickName = data.getStringExtra("group_nick_name");
				if(groupName!=null && !groupName.equals("")){
					mGroupNameTextView.setText(groupName);
					modifyChatInfo();
				}
				if(groupNickName!=null && !groupNickName.equals("")){
					mMyGroupNickNameTextView.setText(groupNickName);
					modifyMyGroupNickName();
				}

			}
			break;
		default:
			break;
		}
	}

	/**
	 * 更改群名称
	 */
	private void modifyChatInfo(){
		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		mInputGroupName = mGroupNameTextView.getText().toString();
		if(mInputGroupName == null || mInputGroupName.equals("")){
			Toast.makeText(mContext, mContext.getResources().getString(R.string.group_chat_please_input_nickname), Toast.LENGTH_LONG).show();
			return;
		}
		if(mInputGroupName.equals(mGroupDetail.groupName)){
			Toast.makeText(mContext,mContext.getResources().getString(R.string.please_modify_content),Toast.LENGTH_LONG).show();
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
							mContext.getResources().getString(R.string.commit_dataing));
					IMResponseState statue = IMCommon.getIMServerAPI().modifyGroupNickName(mGroupDetail.groupId, mInputGroupName);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_GROUP_NAME,statue);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}

	/**
	 * 更改我的群昵称
	 */
	private void modifyMyGroupNickName(){
		if(!IMCommon.getNetWorkState()){
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		mInputGroupNickName = mMyGroupNickNameTextView.getText().toString();
		if(mInputGroupNickName == null || mInputGroupNickName.equals("")){
			Toast.makeText(mContext, mContext.getResources().getString(R.string.group_chat_please_input_nickname), Toast.LENGTH_LONG).show();
			return;
		}
		if(mInputGroupNickName.equals(mGroupDetail.groupnickname)){
			Toast.makeText(mContext,mContext.getResources().getString(R.string.please_modify_content),Toast.LENGTH_LONG).show();
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
							mContext.getResources().getString(R.string.commit_dataing));
					IMResponseState statue = IMCommon.getIMServerAPI().modifyMyNickName(mGroupDetail.groupId, mInputGroupNickName);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_MY_GROUP_NAME,statue);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}

	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case GlobalParam.MSG_CREAT_ROOM_SUCCESS:
				Room room = (Room)msg.obj;
				if(room!=null){
					SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();

					Intent destroy = new Intent(ChatMainActivity.DESTORY_ACTION);
					destroy.putExtra("type", 1);
					sendBroadcast(destroy);
					String roomId = room.groupId;
					List<Login> roomUsrList = room.mUserList;
					RoomTable roomTab = new RoomTable(db);
					roomTab.insert(room);
					String groupHeadUrl="";
					if (roomUsrList != null ) {
						//RoomUserTable roomUserTable = new RoomUserTable(db);
						UserTable userTable = new UserTable(db);

						for (int j = 0; j < roomUsrList.size(); j++) {

							if(room.groupCount-1 == j){
								groupHeadUrl+=roomUsrList.get(j).headSmall;
							}else{
								groupHeadUrl+=roomUsrList.get(j).headSmall+",";
							}


							Login user = userTable.query(roomUsrList.get(j).uid);
							if(user == null){
								userTable.insert(roomUsrList.get(j), -999);
							}
						}
					}

					Session session = new Session();
					session.type = 300;
					session.name = room.groupName;
					session.heading = groupHeadUrl;
					session.lastMessageTime = System.currentTimeMillis();
					session.setFromId(room.groupId);
					session.mUnreadCount = 0;

					SessionTable table = new SessionTable(db);
					table.insert(session);
					sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));



					/*	if(room.mUserList!=null && room.mUserList.size()>0){
						IMCommon.sendMsg(mHandler, INVITE_SUCCESS, room.mUserList);
					}*/
					mGroupDetail = room;
					if(mList!=null && mList.size()>0){
						mList.clear();
					}
					update();

					Login user = new Login();
					user.uid = room.groupId;
					user.nickname = room.groupName;
					user.headSmall = groupHeadUrl;
					user.mIsRoom = 300;
					//user.headSmall = mSessionList.get(position).heading;
					Intent intent = new Intent(mContext, ChatMainActivity.class);
					intent.putExtra("data", user);

					startActivity(intent);
					GroupChatDetailActivity.this.finish();

				}
				break;
			case GlobalParam.MSG_CHECK_STATE:
				hideProgressDialog();
				update();
				break;

			case GlobalParam.MSG_LOAD_ERROR:
				hideProgressDialog();
				String error_Detail = (String)msg.obj;
				if(error_Detail != null && !error_Detail.equals("")){
					Toast.makeText(mContext,error_Detail,Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(mContext,R.string.load_error,Toast.LENGTH_LONG).show();
				}
				break;
			case KICK_SUCCESS:
				hideProgressDialog();
				int pos = msg.arg1;
				hideProgressDialog();
				mGroupDetail.mUserList.remove(pos);
				mList.remove(pos);

				if(mGroupDetail.mUserList.size() == 0){
					mList.remove(mList.size() - 1);
				}
				if(mAdapter != null){
					mAdapter.notifyDataSetChanged();
				}
				break;

			case INVITE_SUCCESS:
				hideProgressDialog();
				List<Login> userList = (List<Login>)msg.obj;
				int addPos = 0;
				if(mGroupDetail.mUserList == null || mGroupDetail.mUserList.size() == 0){
					addPos = 0;
				}else {
					addPos = mUserList.size();
				}

				if(mGroupDetail.mUserList == null){
					mGroupDetail.mUserList = new ArrayList<Login>();
				}

				mGroupDetail.mUserList.addAll(userList);
				for (int i = 0; i < userList.size(); i++) {
					mList.add(i + addPos, new ChatDetailEntity(userList.get(i), 0));
				}

				if(mAdapter != null){
					mAdapter.notifyDataSetChanged();
				}else {
					mAdapter = new ChatPersonAdapter(mContext, mList);
					mGridView.setAdapter(mAdapter);
				}
				break;
			case GlobalParam.MSG_GROUP_ISGET_CHECK:
				IMResponseState getRecv_status = (IMResponseState)msg.obj;
				if (getRecv_status == null || getRecv_status.code != 0) {
					if (getRecv_status != null && getRecv_status.errorMsg != null) {
						Toast.makeText(mContext, getRecv_status.errorMsg, Toast.LENGTH_LONG).show();
					}
				}
				else {
					if(getRecv_status.errorMsg!=null && !getRecv_status.errorMsg.equals("")){
						Toast.makeText(mContext, getRecv_status.errorMsg, Toast.LENGTH_LONG).show();
					}
					SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getWritableDatabase();
					UserTable userTable = new UserTable(dbDatabase);
					RoomTable roomTable = new RoomTable(dbDatabase);
					if(mIsSignChat == 1){
						Login login = userTable.query(mGroupID);
						if(login!=null && login.isGetMsg == 1){
							login.isGetMsg = 0;
							//mTipNewMsgBtn.setChecked(false);

						}else{
							login.isGetMsg = 1;
							//mTipNewMsgBtn.setChecked(true);

						}
						userTable.update(login);
					}else{
						if (mGroupDetail.isgetmsg==1) {
							mGroupDetail.isgetmsg = 0;
							//mTipNewMsgBtn.setChecked(false);

						}
						else {
							mGroupDetail.isgetmsg = 1;
							//mTipNewMsgBtn.setChecked(true);

						}
						roomTable.updateIsGetMsg(mGroupDetail.isgetmsg, mGroupID);
					}


				}
				break;
			case GlobalParam.MSG_CHECK_GROUP_NAME:
				IMResponseState statue = (IMResponseState)msg.obj;
				if(statue == null || statue == null ){
					Toast.makeText(mContext, "提交数据失败",Toast.LENGTH_LONG).show();
					return;
				}
				if(statue.code == 0){
					mGroupDetail.groupName = mInputGroupName;
					mGroupDetail.groupnickname = mInputGroupNickName;
					SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getReadableDatabase();
					RoomTable roomTable = new RoomTable(dbDatabase);
					roomTable.update(mGroupDetail);

					SessionTable sessionTable = new SessionTable(dbDatabase);
					Session session = sessionTable.query(mGroupDetail.groupId, 300);
					if(session!=null){
						session.name = mInputGroupName;
						sessionTable.update(session, 300);
					}

					Intent intent = new Intent(GlobalParam.ACTION_RESET_GROUP_NAME);
					intent.putExtra("group_id",mGroupDetail.groupId);
					intent.putExtra("group_name",mInputGroupName);
					sendBroadcast(intent);
				}else{
					String hintMsg = statue.errorMsg;
					if(hintMsg == null || hintMsg.equals("")){
						hintMsg =mContext.getResources().getString(R.string.commit_data_error);
					}
					Toast.makeText(mContext, hintMsg, Toast.LENGTH_LONG).show();
				}
				break;
			case GlobalParam.MSG_CHECK_MY_GROUP_NAME:
				IMResponseState returnStatue = (IMResponseState)msg.obj;
				if(returnStatue == null || returnStatue == null ){
					Toast.makeText(mContext, R.string.commit_data_error,Toast.LENGTH_LONG).show();
					return;
				}
				if(returnStatue.code == 0){
					mGroupDetail.groupnickname = mInputGroupNickName;
					SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getReadableDatabase();
					RoomTable roomTable = new RoomTable(dbDatabase);
					roomTable.update(mGroupDetail);
					if(mList!=null && mList.size()>0){
						for (int j = 0; j < mList.size(); j++) {
							if(mList.get(j).mLogin!=null){
								if(mList.get(j).mLogin.uid.equals(IMCommon.getUserId(mContext))){
									mList.get(j).mLogin.nickname = mInputGroupNickName;
									if(mAdapter!=null){
										mAdapter.notifyDataSetChanged();
									}
									return;
								}	
							}
						}
					}
				}else{
					String hintMsg = returnStatue.errorMsg;
					if(hintMsg == null || hintMsg.equals("")){
						hintMsg = mContext.getResources().getString(R.string.commit_data_error);
					}
					Toast.makeText(mContext, hintMsg, Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	};


	private void showSignChatLayout(){
		
		mList.add(new ChatDetailEntity(mToChatLogin, 0));
		mList.add(new ChatDetailEntity(null, 1));
	
		mAdapter = new ChatPersonAdapter(mContext, mList);
		mGridView.setAdapter(mAdapter);

		mScrollView.setVisibility(View.VISIBLE);
	}

	private void update(){
		if(mGroupDetail != null){

			mTipNewMsgBtn.setChecked(mGroupDetail.isgetmsg ==1?true:false);
			mShowPartnerNickBtn.setChecked(mGroupDetail.isShowNickname==1?true:false);
			mChatDetailLayout.setVisibility(View.VISIBLE);
			mExitLayout.setVisibility(View.VISIBLE);
			mMyGroupNickNameLayout.setVisibility(View.VISIBLE);
			mShowPartnerNickNameLayout.setVisibility(View.VISIBLE);
			mGroupNameView.setText(mGroupDetail.groupName);

			mMyGroupNickNameTextView.setText(mGroupDetail.groupnickname);
			titileTextView.setText(mContext.getResources().getString(
					R.string.chat_detail_title)+"("+mGroupDetail.groupCount+")");

			if(mGroupDetail.mUserList != null){
				for (int i = 0; i < mGroupDetail.mUserList.size(); i++) {
					mList.add(new ChatDetailEntity(mGroupDetail.mUserList.get(i), 0));
				}
			}
			mList.add(new ChatDetailEntity(null, 1));
			if(mGroupDetail.isOwner == 1){
				if(mIsSignChat != 1){
					mGroupNameLayout.setOnClickListener(this);
				}
				if(mGroupDetail.mUserList != null && mGroupDetail.mUserList.size() != 0){
					mList.add(new ChatDetailEntity(null, 2));
				}
				mClearTextView.setText("删除并退出");
			}else{
				mClearTextView.setText("退出");
			}

			mAdapter = new ChatPersonAdapter(mContext, mList);
			mGridView.setAdapter(mAdapter);

			mScrollView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 获取群详情信息
	 */
	private void getGroupDetail(){
		new Thread(){
			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){
					try {
						Room result = IMCommon.getIMServerAPI().getRommInfoById(mGroupID);
						if(result != null && result.state != null && result.state.code == 0){
							mGroupDetail = result;
							SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
							RoomTable roomTab = new RoomTable(db);
							Room room = roomTab.query(mGroupID);
							if(room!=null){
								result.isShowNickname = room.isShowNickname;
							}
							roomTab.insert(result);
							mHandler.sendEmptyMessage(GlobalParam.MSG_CHECK_STATE);
						}else {
							Message msg=new Message();
							msg.what=GlobalParam.MSG_LOAD_ERROR;
							if(result != null && result.state != null && result.state.errorMsg != null 
									&& !result.state.errorMsg.equals("")){
								msg.obj = result.state.errorMsg;
							}else {
								msg.obj = mContext.getString(R.string.load_error);
							}
							mHandler.sendMessage(msg);
						}
					} catch (IMException e) {
						e.printStackTrace();
						IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
								mContext.getResources().getString(R.string.timeout));
					}

				}else {
					mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
				}
			}
		}.start();
	}

	/**
	 * 将某个用户踢出房间
	 * @param pos
	 */
	private void kickPerson(final int pos){
		new Thread(){
			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().kickParticipant(mGroupID, mList.get(pos).mLogin.uid);
						if(state != null && state.code == 0){
							IMCommon.sendMsg(mHandler, KICK_SUCCESS, pos);
						}else {
							Message msg=new Message();
							msg.what=GlobalParam.MSG_LOAD_ERROR;
							if(state != null && state.errorMsg != null && !state.errorMsg.equals("")){
								msg.obj = state.errorMsg;
							}else {
								msg.obj = mContext.getString(R.string.operate_failed);
							}
							mHandler.sendMessage(msg);
						}
					} catch (IMException e) {
						e.printStackTrace();
						IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
								mContext.getResources().getString(R.string.timeout));
					}

				}else {
					mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
				}
			}
		}.start();
	}


	private String[] getString(){
		String[] sString=null;
		if(mGroupDetail!=null 
				&& (mGroupDetail.mUserList!=null && mGroupDetail.mUserList.size()>0)){
			sString = new String[2];
			String name ="";
			String headUrl = "";
			int count = 4;
			if(mGroupDetail.mUserList.size()<=4){
				count = mGroupDetail.mUserList.size();
			}
			for (int i = 0; i <count; i++) {
				if(i == count - 1){
					name += mGroupDetail.mUserList.get(i).nickname;
					headUrl += mGroupDetail.mUserList.get(i).headSmall;
					continue;
				}
				name += mGroupDetail.mUserList.get(i).nickname+",";
				headUrl += mGroupDetail.mUserList.get(i).headSmall+",";
			}
			sString[0] = name;
			sString[1] = headUrl;
		}
		return sString;
	} 


	/*
	 * 创建临时会话并添加用户
	 */
	private void createRoom(final List<Login> list){
		if(!IMCommon.getNetWorkState()){
			mBaseHandler.sendEmptyMessage(BASE_MSG_TIMEOUT_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
							mContext.getResources().getString(R.string.group_chat_creating));
					for (int i = 0; i < list.size(); i++) {
						if(i == list.size() - 1){
							mUids += list.get(i).uid;
							mNickName += list.get(i).nickname;
							continue;
						}

						mUids += list.get(i).uid + ",";
						mNickName += list.get(i).nickname+",";
					}
					Room createRoom = IMCommon.getIMServerAPI().createRoom( mNickName, mUids);

					if(createRoom != null && createRoom.state !=null && createRoom.state.code == 0){
						IMCommon.sendMsg(mHandler, GlobalParam.MSG_CREAT_ROOM_SUCCESS, createRoom);
					}else {
						Message msg=new Message();
						msg.what=GlobalParam.MSG_LOAD_ERROR;
						if(createRoom != null && createRoom.state != null
								&& createRoom.state != null
								&& !createRoom.state.errorMsg.equals("")){
							msg.obj = createRoom.state.errorMsg;
						}else {
							msg.obj = mContext.getString(R.string.group_chat_create_failed);
						}
						mHandler.sendMessage(msg);
					}

					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (NotFoundException e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
							mContext.getResources().getString(R.string.timeout));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}

	/**
	 * 邀请用户加入群
	 * @param userList
	 */
	private void inviteUser(final List<Login> userList){
		new Thread(){
			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){

					String uids = "";
					for (int i = 0; i < userList.size(); i++) {
						if(i < userList.size() - 1){
							uids += userList.get(i).uid + ",";
							continue;
						}

						uids += userList.get(i).uid;
					}

					try {
						IMResponseState state = IMCommon.getIMServerAPI().inviteUsers(mGroupID, uids);
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
						if(state != null && state.code == 0){
							IMCommon.sendMsg(mHandler, INVITE_SUCCESS, userList);
						}else {
							Message msg=new Message();
							msg.what=GlobalParam.MSG_LOAD_ERROR;
							if(state != null && state.errorMsg != null && !state.errorMsg.equals("")){
								msg.obj = state.errorMsg;
							}else {
								msg.obj = mContext.getString(R.string.operate_failed);
							}
							mHandler.sendMessage(msg);
						}
					} catch (IMException e) {
						e.printStackTrace();
						IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
								mContext.getResources().getString(R.string.timeout));
					}catch (Exception e) {
						e.printStackTrace();
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);

					}

				}else {
					mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
				}
			}
		}.start();
	}

	private void toggleListener(final boolean isRecv,final int type){
		if(!IMCommon.getNetWorkState()){
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int recv = 0;
					if (isRecv) {
						recv = 1;
					}
					else {
						recv = 0;
					}
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
							mContext.getResources().getString(R.string.send_request));
					IMResponseState stauts = null;
					if(type == 1){
						//stauts = IMCommon.getIMServerAPI().isPublicGroup(mRoomId,recv );
					}else if(type == 2){
						if(mIsSignChat == 1){
							//setMsg
							stauts = IMCommon.getIMServerAPI().setMsg(mGroupID);
						}else{
							stauts = IMCommon.getIMServerAPI().isGetGroupMsg(mGroupID,recv );
						}

					}
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					if(type == 1){
						IMCommon.sendMsg(mHandler, GlobalParam.MSG_GROUP_PUBLISH_CHECK, stauts);
					}else if(type == 2){
						IMCommon.sendMsg(mHandler, GlobalParam.MSG_GROUP_ISGET_CHECK, stauts);
					}
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			}
		}).start();
	}

	/**
	 * 删除群
	 * @param roomId
	 */
	private void deleteRoom(final String roomId){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
								mContext.getResources().getString(R.string.send_request));
						IMResponseState state = IMCommon.getIMServerAPI().deleteRoom(roomId);
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
						if(state != null && state.code == 0){

							SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
							//RoomUserTable roomUserTable = new RoomUserTable(db);
							//roomUserTable.delete(roomId);
							RoomTable table = new RoomTable(db);
							table.delete(roomId);
							Intent intent = new Intent(ChatFragment.DELETE_ROOM_SUCCESS);
							intent.putExtra("froom_id", roomId);
							sendBroadcast(intent);
							sendBroadcast(new Intent(ChatMainActivity.DESTORY_ACTION));
							Intent delRoomList = new Intent(MyGroupListActivity.MY_ROOM_BE_DELETED_ACTION);//roomID
							delRoomList.putExtra("roomID",roomId );
							sendBroadcast(delRoomList);
							GroupChatDetailActivity.this.finish();
						}

					} catch (IMException e) {
						e.printStackTrace();
						IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
								mContext.getResources().getString(R.string.timeout));
					}catch (Exception e) {
						e.printStackTrace();
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					}

				}
			}.start();
		}else{
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
		}
	}

	/**
	 * 退出群
	 * @param roomId
	 */
	private void exitRoom(final String roomId/*,final int delType*/){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
								mContext.getResources().getString(R.string.exit_rooming));
						IMResponseState state = IMCommon.getIMServerAPI().exitRoom(roomId);
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
						if(state != null && state.code == 0){
							SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
							//RoomUserTable roomUserTable = new RoomUserTable(db);
							//roomUserTable.delete(roomId);
							RoomTable table = new RoomTable(db);
							table.delete(roomId);
							Intent intent = new Intent(ChatFragment.DELETE_ROOM_SUCCESS);
							sendBroadcast(new Intent(ChatMainActivity.DESTORY_ACTION));
							intent.putExtra("froom_id", roomId);
							sendBroadcast(intent);

							Intent delRoomList = new Intent(MyGroupListActivity.MY_ROOM_BE_DELETED_ACTION);//roomID
							delRoomList.putExtra("roomID",roomId );
							sendBroadcast(delRoomList);
							GroupChatDetailActivity.this.finish();
						}

					} catch (IMException e) {
						e.printStackTrace();
						IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR, 
								mContext.getResources().getString(e.getStatusCode()));
					}catch (Exception e) {
						e.printStackTrace();
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					}
				}
			}.start();
		}else {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
		}
	}


}
