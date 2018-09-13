package net.smallchat.im.contact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.GroupTable;
import net.smallchat.im.DB.RoomTable;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.DB.UserTable;
import net.smallchat.im.Entity.Card;
import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.Group;
import net.smallchat.im.Entity.GroupList;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.MessageImage;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.Entity.Room;
import net.smallchat.im.Entity.Session;
import net.smallchat.im.R;
import net.smallchat.im.adapter.ChooseUserListAdapter;
import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.components.sortlist.CharacterParser;
import net.smallchat.im.components.sortlist.PinyinComparator;
import net.smallchat.im.components.sortlist.SideBar;
import net.smallchat.im.components.sortlist.SideBar.OnTouchingLetterChangedListener;
import net.smallchat.im.fragment.ChatFragment;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.mine.MyGroupListActivity;
import net.smallchat.im.api.IMException;

/**
 * 选择联系人
 * @author dongli
 *
 */
public class ChooseUserActivity extends BaseActivity implements OnItemClickListener, OnClickListener{
	public static final String FORWARD_MSG="forward_msg";
	private boolean mNoMore = false;
	private LinearLayout mFootView;
	private List<Login> mUserList = new ArrayList<Login>();
	private List<Login> mList = new ArrayList<Login>();
	private List<Login> mSearchList = new ArrayList<Login>();
	private GroupList mGroup;
	private List<Group> mGroupList = new ArrayList<Group>();
	private ListView mListView;
	private ChooseUserListAdapter mAdapter;
	private RelativeLayout mBottomLayout;
	private HorizontalScrollView mScrollView;
	//private Button mOkBtn;
	private LinearLayout mUserLayout;
	private RelativeLayout mSelectGroupLayout;
	private EditText mSearchContent;
	private ImageLoader mImageLoader = new ImageLoader();
	private List<Login> mSelectedUser = new ArrayList<Login>();
	public static final String ACTION_CREATE_SUCESS = "im_action_create_success";
	public static final String ACTION_CREATE_FAIED = "im_action_create_failed";

	public static final String ACTION_DESTROY_ACTIVITY= "im_action_destroy_activity";

	private boolean mIsRegisterReceiver = false;

	private Login mCardLogin,mToLogin;
	private ChatMessage mForMsg;
	private int mIsSendCard; //是否是名片标示
	private int mCardType; //发送好友名片标示
	private int mValicJoinMeeting; //是否邀请参会
	private int mMeetingid;
	private String mMeetName;
	private String mMeetHeadUrl;


	private int mJumpFrom; //1-发起群聊
	private String mUids = "";
	private String mNickName ="";

	private SideBar sideBar;
	private TextView dialog;

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	/*
	 *  汉字转换成拼音的类
	 */
	private CharacterParser characterParser;


	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.contacts_tab);
		mJumpFrom = getIntent().getIntExtra("jumpfrom",0);

		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();
		initComponent();
		mCardType = getIntent().getIntExtra("cardType",0);
		mIsSendCard = getIntent().getIntExtra("isJump",0);
		mValicJoinMeeting = getIntent().getIntExtra("join_meeting",0); 
		mMeetingid = getIntent().getIntExtra("meeting_id",0);
		mMeetName = getIntent().getStringExtra("meet_name");
		mMeetHeadUrl = getIntent().getStringExtra("meet_url");

		mCardLogin = (Login)getIntent().getSerializableExtra("cardLogin");
		mToLogin = (Login)getIntent().getSerializableExtra("toLogin");

		mForMsg = (ChatMessage)getIntent().getSerializableExtra("forward_msg");



		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_CREATE_SUCESS);
		filter.addAction(ACTION_DESTROY_ACTIVITY);
		registerReceiver(mReceiver, filter);
		mIsRegisterReceiver = true;

		SQLiteDatabase db = DBHelper.getInstance(mContext).getReadableDatabase();
		GroupTable table = new GroupTable(db);
		mGroupList = table.query();
		if(mGroupList != null && mGroupList.size() != 0){
			for (int i = 0; i < mGroupList.size(); i++) {
				if(mGroupList.get(i).mUserList != null && mGroupList.get(i).mUserList.size()>0){
					mUserList.addAll(mGroupList.get(i).mUserList);
					mList.addAll(mGroupList.get(i).mUserList);
				}
			}

			updateListView(true);
		}else {
			mGroupList = new ArrayList<Group>();
			Message message = new Message();
			message.obj = ChatApplication.getInstance().getResources().getString(R.string.add_more_loading);
			message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
			mHandler.sendMessage(message);
			getUserList(GlobalParam.LIST_LOAD_FIRST);
		}
	}

	/**
	 * 根据通知类型处理界面的操作
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if(action.equals(ACTION_DESTROY_ACTIVITY)){
				ChooseUserActivity.this.finish();
			}else {
				hideProgressDialog();
				Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.group_chat_create_failed), Toast.LENGTH_SHORT).show();
			}
		}

	};


	@Override
	protected void onDestroy() {
		if(mIsRegisterReceiver){
			unregisterReceiver(mReceiver);
		}
		super.onDestroy();
	}

	/**
	 * 初始化控件
	 */
	private void initComponent(){
		mBottomLayout = (RelativeLayout) findViewById(R.id.bottomlayout);
		if(mJumpFrom == 1){
			setRightTextTitleContent(R.drawable.back_btn,R.string.ok , R.string.group_chat_create);
			mBottomLayout.setVisibility(View.VISIBLE);
		}else{
			if(mIsSendCard != 1){
				setRightTextTitleContent(R.drawable.back_btn,R.string.ok , R.string.select_contact);
				mBottomLayout.setVisibility(View.VISIBLE);
			}
		}


		mLeftBtn.setOnClickListener(this);


		mRightTextBtn.setEnabled(false);
		mRightTextBtn.setOnClickListener(this);



		sideBar = (SideBar)findViewById(R.id.sidrbar);
		dialog = (TextView)findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		//设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				//该字母首次出现的位置
				int position = mAdapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					mListView.setSelection(position);
				}
			}
		});

		mScrollView = (HorizontalScrollView) findViewById(R.id.scrollview);
		mUserLayout = (LinearLayout) findViewById(R.id.userlayout);

		mSelectGroupLayout = (RelativeLayout)findViewById(R.id.select_group_layout);
		mSelectGroupLayout.setVisibility(View.VISIBLE);
		mSelectGroupLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myGroupIntent = new Intent();
				myGroupIntent.setClass(mContext, MyGroupListActivity.class);
				myGroupIntent.putExtra("hide", 1);
				startActivity(myGroupIntent);

			}
		});
		mSearchContent = (EditText) findViewById(R.id.searchcontent);
		mSearchContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if(s.toString() != null && !s.toString().equals("")){

					if(mSearchList != null){
						mSearchList.clear();
					}

					for (int i = 0; i < mUserList.size(); i++) {
						String name = mUserList.get(i).remark;
						if(name == null || name.equals("")){
							name = mUserList.get(i).nickname;
						}
						if(name.contains(s.toString())){
							mSearchList.add(mUserList.get(i));
						}
					}

					if(mList != null){
						mList.clear();
					}
					if(mSearchList != null){
						mList.addAll(mSearchList);
					}
					notifyChanged(false);

					mAdapter.setIsShow(false);
				}else {

					if(mList != null){
						mList.clear();
					}

					if(mUserList != null){
						mList.addAll(mUserList);
					}

					notifyChanged(false);
					mAdapter.setIsShow(true);
				}
			}
		});



		mListView = (ListView) findViewById(R.id.contact_list);
		mListView.setCacheColorHint(0);
		mListView.setOnItemClickListener(this);
		mListView.setItemsCanFocus(true);
		mListView.setDivider(null);
		mListView.setSelector(mContext.getResources().getDrawable(R.drawable.transparent_selector));

		setUIValue();

	}

	private void setUIValue(){
		mRightTextBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.ok));
	}

	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case GlobalParam.MSG_CHECK_STATE:
				Room room = (Room)msg.obj;
				if(room == null){
					Toast.makeText(mContext, mContext.getResources().
							getString(R.string.group_chat_create_failed),Toast.LENGTH_LONG).show();
					return;
				}
				SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
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
							groupHeadUrl += roomUsrList.get(j).headSmall;
						}else{
							groupHeadUrl += roomUsrList.get(j).headSmall+",";
						}

						/*if(!roomUsrList.get(j).uid.equals(IMCommon.getUserId(mContext))){
							roomUserTable.insert(roomId, roomUsrList.get(j).uid);
						}*/
						Login user = userTable.query(roomUsrList.get(j).uid);
						if(user == null){
							userTable.insert(roomUsrList.get(j), -999);
						}
					}
				}


				/*ChatMessage SysMsg = new ChatMessage();
				SysMsg.content = mContext.getResources().getString(R.string.create_chat_grop_hint_start)
						+ mNickName+mContext.getResources().getString(R.string.create_chat_grop_hint_end);
				SysMsg.fromId = IMCommon.getUserId(mContext);
				SysMsg.tag = UUID.randomUUID().toString();
				SysMsg.toId = room.groupId;
				SysMsg.systeMessage = 1;
				SysMsg.time = System.currentTimeMillis();
				SysMsg.messageType = MessageType.TEXT;


				MessageTable table = new MessageTable(db);
				table.insert(SysMsg);
				 */

				Session session = new Session();
				session.setFromId(room.groupId/*IMCommon.getUserId(mContext)*/);
				session.name = room.groupName;
				session.heading = groupHeadUrl;
				session.type = 300;
				session.lastMessageTime = System.currentTimeMillis();

				SessionTable sessionTable = new SessionTable(db);
				sessionTable.insert(session);

				sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));

				Login user = new Login();
				user.uid = room.groupId;
				user.nickname = room.groupName;
				user.headSmall = groupHeadUrl;
				user.mIsRoom = 300;
				//user.headSmall = mSessionList.get(position).heading;
				Intent intent = new Intent(mContext, ChatMainActivity.class);
				intent.putExtra("data", user);
				intent.putExtra("cardType",mCardType);
				if(mCardLogin!=null && mCardLogin.uid!=null
						&& !mCardLogin.uid.equals("")){
					intent.putExtra("cardMsg",createMsg(300,room.groupId,room.groupName,groupHeadUrl));
				}
				if(mForMsg !=null){
					intent.putExtra("forMsg",createNormalMsg(300,room.groupId,room.groupName,groupHeadUrl,mForMsg));
				}
				startActivity(intent);
				ChooseUserActivity.this.finish();
				break;
			case GlobalParam.MSG_CHECK_INVALID_MEETING:
				mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MEETING_LIST));
				ChooseUserActivity.this.finish();
				break;
			case GlobalParam.SHOW_PROGRESS_DIALOG:
				String dialogMsg = (String)msg.obj;
				showProgressDialog(dialogMsg);
				break;
			case GlobalParam.HIDE_PROGRESS_DIALOG:
				hideProgressDialog();
				updateListView(true);
				mAdapter.setIsShow(true);
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
				LinearLayout footView = (LinearLayout)msg.obj;				
				ProgressBar pb = (ProgressBar)footView.findViewById(R.id.hometab_addmore_progressbar);
				pb.setVisibility(View.VISIBLE);		 		
				TextView more = (TextView)footView.findViewById(R.id.hometab_footer_text);
				more.setText(ChatApplication.getInstance().getResources().getString(R.string.add_more_loading));
				getUserList(GlobalParam.LIST_LOAD_MORE);
				break;
			case GlobalParam.HIDE_LOADINGMORE_INDECATOR:
				if (mFootView != null){
					ProgressBar pbar = (ProgressBar)mFootView.findViewById(R.id.hometab_addmore_progressbar);
					pbar.setVisibility(View.GONE);
					TextView moreView = (TextView)mFootView.findViewById(R.id.hometab_footer_text);
					moreView.setText(R.string.add_more);
				}

				if(mNoMore){
					((TextView)mFootView.findViewById(R.id.hometab_footer_text)).setText(ChatApplication.getInstance().getResources().getString(R.string.no_more_data));
				}else {
					((TextView)mFootView.findViewById(R.id.hometab_footer_text)).setText(ChatApplication.getInstance().getResources().getString(R.string.add_more));
				}

				if (mAdapter != null){
					mAdapter.notifyDataSetChanged();
				}
				break;
			}
		}
	};

	/**
	 * 显示选择的联系人
	 * @param login
	 */
	private void addView(final Login login){

		ImageView imageView = new ImageView(mContext);
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for (int i = 0; i < mSelectedUser.size(); i++) {
					if(mSelectedUser.get(i).uid.equals(login.uid)){
						mSelectedUser.remove(i);
						mUserLayout.removeViewAt(i);
						for (int j = 0; j < mUserList.size(); j++) {
							if(mUserList.get(j).uid.equals(login.uid)){
								mUserList.get(j).isShow = false;
								notifyChanged(false);
							}
						}
						mRightTextBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.ok) + "(" + mSelectedUser.size() + ")");
						if(mSelectedUser.size() == 0){
							mRightTextBtn.setEnabled(false);
						}else {
							mRightTextBtn.setEnabled(true);
						}
						break;
					}
				}
			}
		});
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FeatureFunction.dip2px(mContext, 40), FeatureFunction.dip2px(mContext, 40));
		params.rightMargin = FeatureFunction.dip2px(mContext, 5);
		imageView.setLayoutParams(params);
		imageView.setScaleType(ScaleType.FIT_XY);
		imageView.setImageResource(R.drawable.contact_default_header);
		LinearLayout layout = new LinearLayout(mContext);
		layout.addView(imageView);
		mImageLoader.getBitmap(mContext, imageView, null, login.headSmall, 0, false, true);
		mUserLayout.addView(layout);
		mUserLayout.invalidate();
		mScrollView.smoothScrollTo(mUserLayout.getMeasuredWidth(), 0);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(arg2 < mList.size()){
			if(mIsSendCard == 1){
				if(mToLogin == null){
					return;
				}
				Intent intent = new Intent();
				intent.setClass(mContext, CardUserDetailActivity.class);
				intent.putExtra("user",mList.get(arg2));
				intent.putExtra("toLogin",mToLogin);
				startActivity(intent);
				return;
			}

			if(mSearchList != null && mSearchList.size() != 0){
				for (int i = 0; i < mUserList.size(); i++) {
					if(mUserList.get(i).uid.equals(mList.get(arg2).uid)){
						if(!mUserList.get(i).isShow){
							mSelectedUser.add(mUserList.get(i));
							addView(mUserList.get(i));
							mUserList.get(i).isShow = true;
						}
						mList.clear();
						mList.addAll(mUserList);
						updateListView(false);
						mAdapter.setIsShow(true);
						mSearchContent.setText("");
						mSearchList.clear();
						break;
					}
				}				
			}else {
				if(mUserList.get(arg2).isShow){
					mUserList.get(arg2).isShow = false;

					for (int i = 0; i < mSelectedUser.size(); i++) {
						if( mSelectedUser.get(i).uid.equals(mUserList.get(arg2).uid)){
							mSelectedUser.remove(i);
							mUserLayout.removeViewAt(i);
							break;
						}
					}
				}else {
					mSelectedUser.add(mUserList.get(arg2));
					addView(mUserList.get(arg2));
					mUserList.get(arg2).isShow = true;
				}

				notifyChanged(false);
			}

			mRightTextBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.ok) + "(" + mSelectedUser.size() + ")");
			if(mSelectedUser.size() == 0){
				mRightTextBtn.setEnabled(false);
			}else {
				mRightTextBtn.setEnabled(true);
			}
		}
	}

	/**
	 * 为listview 填充数据
	 * @param isFirst
	 */
	private void updateListView(boolean isFirst){
		if(isFirst){
			filledData(mList);
			filledData(mUserList);
			//根据a-z排序

			Collections.sort(mList, pinyinComparator);
			Collections.sort(mUserList, pinyinComparator);
		}

		mAdapter = new ChooseUserListAdapter(mContext, mList);
		mListView.setAdapter(mAdapter); 

	}

	/**
	 * 刷新listview 填充数据
	 * @param isFrist
	 */
	private void notifyChanged(boolean isFrist){
		if(isFrist){
			filledData(mList);
			filledData(mUserList);
			//根据a-z排序
			Collections.sort(mList, pinyinComparator);
			Collections.sort(mUserList, pinyinComparator);
		}

		if(mAdapter != null){
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 根据字母顺序排序用户
	 * @return
	 */
	private void filledData(List<Login> list){

		for(int i=0; i<list.size(); i++){
			String name="";
			if(list.get(i).nameType == 1  ){
				name = list.get(i).nickname;
			}else{
				name = list.get(i).remark;
			}
			if(name == null || name.equals("")){
				name = list.get(i).nickname;
			}



			//汉字转换成拼音
			String pinyin = characterParser.getSelling(name);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// 正则表达式，判断首字母是否是英文字母
			if(sortString.matches("[A-Z]")){
				list.get(i).sort = (sortString.toUpperCase());
				list.get(i).sortName = sortString.toUpperCase();
			}else{
				list.get(i).sortName = "#";
				list.get(i).sort = "#";
			}


		}

	}


	/**
	 * 获取用户列表
	 * @param loadType 加载数据的类型
	 */
	private void getUserList(final int loadType) {
		new Thread() {

			@Override
			public void run() {
				if (IMCommon.verifyNetwork(mContext)) {
					new Thread() {
						public void run() {
							try {

								mGroup = IMCommon.getIMServerAPI().getUserList();

								if (mGroup != null) {
									if (mGroup.mState != null
											&& mGroup.mState.code == 0) {

										if (loadType != GlobalParam.LIST_LOAD_MORE) {
											if (mGroupList != null) {
												mGroupList.clear();
											}

											if(mUserList != null){
												mUserList.clear();
											}

											if(mList != null){
												mList.clear();
											}
										}

										if (mGroup.mGroupList != null) {
											mGroupList.addAll(mGroup.mGroupList);
											SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
											GroupTable table = new GroupTable(db);
											table.insert(mGroup.mGroupList);

											for (int i = 0; i < mGroup.mGroupList.size(); i++) {

												if(mGroupList.get(i).mUserList != null){
													mUserList.addAll(mGroupList.get(i).mUserList);
												}

											}
										}

										if(mUserList != null){
											mList.addAll(mUserList);
										}

									} else {
										Message msg = new Message();
										msg.what = GlobalParam.MSG_LOAD_ERROR;
										if (mGroup.mState != null && mGroup.mState.errorMsg != null && !mGroup.mState.errorMsg.equals("")) {
											msg.obj = mGroup.mState.errorMsg;
										} else {
											msg.obj = ChatApplication.getInstance().getResources().getString(R.string.load_error);
										}
										mHandler.sendMessage(msg);
									}
								} else {
									mHandler.sendEmptyMessage(GlobalParam.MSG_LOAD_ERROR);
								}

							} catch (IMException e) {
								e.printStackTrace();
								Message msg = new Message();
								msg.what = GlobalParam.MSG_TIME_OUT_EXCEPTION;
								msg.obj = ChatApplication.getInstance().getResources().getString(R.string.timeout);
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
				} else {
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

		case R.id.right_text_btn:
			if(mSelectedUser != null){
				if (mIsSendCard == 1) {
					Intent intent = new Intent();
					ArrayList<String> selectedUser=new ArrayList<String>();
					for(int j=0;j<mSelectedUser.size();j++){
						/*String desc = mSelectedUser.get(j).remark;
						if(desc == null || desc.equals("")){*/
						String desc = mSelectedUser.get(j).name;
						/*	}*/
						selectedUser.add(desc);
					}
					intent.putStringArrayListExtra("userlist", selectedUser);
					setResult(2,intent);
					ChooseUserActivity.this.finish();
				}else{
					if(mSelectedUser.size() == 1){
						if(mValicJoinMeeting == 1){
							createRoom(mSelectedUser,mValicJoinMeeting,mMeetingid);
						}else{
							Login login = mSelectedUser.get(0);
							login.mIsRoom = 100;
							Intent intent = new Intent(mContext, ChatMainActivity.class);
							intent.putExtra("data", login);
							intent.putExtra("cardType",mCardType);
							if(mCardLogin!=null && mCardLogin.uid!=null
									&& !mCardLogin.uid.equals("")){
								intent.putExtra("cardMsg",createMsg(100,login.uid,login.nickname,login.headSmall));
							}

							if(mForMsg !=null){
								intent.putExtra("forMsg",createNormalMsg(100,login.uid,login.nickname,login.headSmall,mForMsg));
							}
							startActivity(intent);
							ChooseUserActivity.this.finish();
						}

					}else {
						createRoom(mSelectedUser,mValicJoinMeeting,mMeetingid);
					}
				}
			}

			break;

		default:
			break;
		}
	}

	//拼接消息内容
	private ChatMessage createMsg(int chatType, String toId, String toName, String toUrl){
		Login login = IMCommon.getLoginResult(mContext);

		Card card = new Card(mCardLogin.uid, mCardLogin.headSmall, mCardLogin.nickname, mCardLogin.sign);

		ChatMessage msg = new ChatMessage();
		msg.fromId = IMCommon.getUserId(mContext);
		msg.tag = UUID.randomUUID().toString();
		msg.fromName = login.nickname;
		msg.fromUrl = login.headSmall;

		msg.toId = toId;
		msg.toName = toName;
		msg.toUrl = toUrl;



		msg.messageType = MessageType.CARD;
		msg.chatType = chatType;
		msg.cardOwerName = mCardLogin.nickname;
		msg.content = Card.getInfo(card);
		msg.time = System.currentTimeMillis();
		msg.readState = 1;
		return msg;
	}

	//拼接消息内容
	private ChatMessage createNormalMsg(int chatType, String toId, String toName, String toUrl, ChatMessage chatMessage){
		Login login = IMCommon.getLoginResult(mContext);


		chatMessage.fromId = IMCommon.getUserId(mContext);
		chatMessage.tag = UUID.randomUUID().toString();
		chatMessage.fromName = login.nickname;
		chatMessage.fromUrl = login.headSmall;

		chatMessage.toId = toId;
		chatMessage.toName = toName;
		chatMessage.toUrl = toUrl;
		if(chatMessage.messageType == MessageType.IMAGE){
			MessageImage mp = MessageImage.getInfo(chatMessage.content);
			chatMessage.content = MessageImage.getInfo(mp);
		}

		chatMessage.time = System.currentTimeMillis();
		chatMessage.readState = 1;
		chatMessage.chatType = chatType;
		return chatMessage;
	}


	/**
	 * 
	 * @param list
	 * @param type 0-创建群组 1-邀请参会
	 */
	private void createRoom(final List<Login> list,final int type,final int meetingid){
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
							continue;
						}

						mUids += list.get(i).uid + ",";
					}
					int size = 4;
					if(list.size()<size){
						size = list.size();
					}
					for (int i = 0; i < size; i++) {
						if(size-1 == i){
							mNickName += list.get(i).nickname;
						}else{
							mNickName += list.get(i).nickname+",";
						}

					}


					if(type == 0){
						Room createRoom = IMCommon.getIMServerAPI().createRoom( mNickName, mUids);
						if(createRoom != null && createRoom.state !=null && createRoom.state.code == 0){
							IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE, createRoom);
						}else {
							Message msg=new Message();
							msg.what=GlobalParam.MSG_LOAD_ERROR;
							if(createRoom != null && createRoom.state != null
									&& !createRoom.state.errorMsg.equals("")){
								msg.obj = createRoom.state.errorMsg;
							}else {
								msg.obj = mContext.getString(R.string.group_chat_create_failed);
							}
							mHandler.sendMessage(msg);
						}
					}else if(type == 1){
						IMResponseState state = IMCommon.getIMServerAPI().inviteMeeting(meetingid, mUids);
						if(state != null && state.code == 0){
							IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_INVALID_MEETING, state);
						}else {
							Message msg=new Message();
							msg.what=GlobalParam.MSG_LOAD_ERROR;
							if(state != null 
									&& !state.errorMsg.equals("")){
								msg.obj = state.errorMsg;
							}else {
								msg.obj = mContext.getString(R.string.valic_meeting);
							}
							mHandler.sendMessage(msg);
						}
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





}
