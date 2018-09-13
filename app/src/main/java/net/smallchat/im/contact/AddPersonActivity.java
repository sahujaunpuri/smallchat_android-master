package net.smallchat.im.contact;

import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.GroupTable;
import net.smallchat.im.DB.RoomTable;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.DB.UserTable;
import net.smallchat.im.Entity.Group;
import net.smallchat.im.Entity.GroupList;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.Room;
import net.smallchat.im.Entity.Session;
import net.smallchat.im.R;
import net.smallchat.im.adapter.ChooseUserListAdapter;
import net.smallchat.im.chat.GroupChatDetailActivity;
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
import net.smallchat.im.api.IMException;

/**
 * 添加联系人
 * @author dongli
 *
 */
public class AddPersonActivity extends BaseActivity implements OnItemClickListener, OnClickListener{


	/**
	 * 定义全局变量
	 */
	private boolean mNoMore = false;
	private LinearLayout mFootView;
	private List<Login> mUserList = new ArrayList<Login>();
	private List<Login> mList = new ArrayList<Login>();
	private List<Login> mSearchList = new ArrayList<Login>();
	private List<Login> mOriginalList = new ArrayList<Login>();
	private GroupList mGroup;
	private List<Group> mGroupList = new ArrayList<Group>();
	private ListView mListView;
	private ChooseUserListAdapter mAdapter;
	private RelativeLayout mBottomLayout;
	private HorizontalScrollView mScrollView;
	private LinearLayout mUserLayout;
	private EditText mSearchContent;
	private ImageLoader mImageLoader = new ImageLoader();
	private List<Login> mSelectedUser = new ArrayList<Login>();

	private Login mOldLogin ;
	private int mIsSignChat;
	private String mUids="";
	private String mNickName="";

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

	private int mType;

	/**
	 * 导入全局变量
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.contacts_tab);
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();

		initComponent();
		//+++查询本地联系人+++
		SQLiteDatabase db = DBHelper.getInstance(mContext).getReadableDatabase();
		GroupTable table = new GroupTable(db);
		mGroupList = table.query();
		if(mGroupList != null && mGroupList.size() != 0){
			for (int i = 0; i < mGroupList.size(); i++) {
				if(mGroupList.get(i).mUserList != null && mGroupList.get(i).mUserList.size()>0){
					mUserList.addAll(mGroupList.get(i).mUserList);

				}
			}

			if(mOriginalList != null){
				for (int i = 0; i < mOriginalList.size(); i++) {
					for (int j = 0; j < mUserList.size(); j++) {
						if(mOriginalList.get(i).uid.equals(mUserList.get(j).uid)){
							mUserList.remove(j);
							j--;
							break;
						}
					}
				}
			}


			if(mUserList != null){
				mList.addAll(mUserList);
			}
			//---查询本地联系人---
			updateListView(true);
		}else {
			Message message = new Message();
			message.obj = ChatApplication.getInstance().getResources().getString(R.string.add_more_loading);
			message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
			mHandler.sendMessage(message);
			getUserList(GlobalParam.LIST_LOAD_FIRST);
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 实例化控件
	 */
	private void initComponent(){
		mType = getIntent().getIntExtra("type", 0);
		if(mType == 1){
			setRightTextTitleContent(R.drawable.back_btn,R.string.ok , R.string.select_contact);
		}else{
			setRightTextTitleContent(R.drawable.back_btn,R.string.ok , R.string.add_person);
		}

		mLeftBtn.setOnClickListener(this);
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

		mOriginalList = (List<Login>) getIntent().getSerializableExtra("users");
		mIsSignChat = getIntent().getIntExtra("is_sign_chat",0);
		if(mIsSignChat == 1){
			mOldLogin = mOriginalList.get(0);
		}



		mBottomLayout = (RelativeLayout) findViewById(R.id.bottomlayout);
		mBottomLayout.setVisibility(View.VISIBLE);

		mScrollView = (HorizontalScrollView) findViewById(R.id.scrollview);
		mUserLayout = (LinearLayout) findViewById(R.id.userlayout);


		mSearchContent = (EditText) findViewById(R.id.searchcontent);
		mSearchContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			/*
			 * 实现搜索用户功能
			 * (non-Javadoc)
			 * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
			 */
			@Override
			public void afterTextChanged(Editable s) {
				if(s.toString() != null && !s.toString().equals("")){

					if(mSearchList != null){
						mSearchList.clear();
					}

					for (int i = 0; i < mUserList.size(); i++) {
						String realName = mUserList.get(i).nickname;
						if (realName.contains(s.toString())) {
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
				}
			}
		});

		;
		mListView = (ListView) findViewById(R.id.contact_list);
		mListView.setCacheColorHint(0);
		mListView.setOnItemClickListener(this);
		mListView.setItemsCanFocus(true);
		mListView.setDivider(null);
		mListView.setSelector(mContext.getResources().getDrawable(R.drawable.transparent_selector));

		setUIValue();
	}

	/**
	 * 设置按钮的值
	 */
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

			case GlobalParam.MSG_CREAT_ROOM_SUCCESS://创建群成功
				Room room = (Room)msg.obj;
				if(room!=null){
					sendBroadcast(new Intent(GroupChatDetailActivity.DESTORY_CHAT_DETAIL_ACTION));
					SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();

					Intent destroy = new Intent(ChatMainActivity.DESTORY_ACTION);
					destroy.putExtra("type", 1);
					sendBroadcast(destroy);
					List<Login> roomUsrList = room.mUserList;
					RoomTable roomTab = new RoomTable(db);
					roomTab.insert(room);
					String groupHeadUrl="";
					if (roomUsrList != null ) {
						//RoomUserTable roomUserTable = new RoomUserTable(db);
						UserTable userTable = new UserTable(db);


						for (int j = 0; j < roomUsrList.size(); j++) {
							Login user = userTable.query(roomUsrList.get(j).uid);
							if(user == null){
								userTable.insert(roomUsrList.get(j), -999);
							}
						}

						int size = 4;
						if(roomUsrList.size()<size){
							size = roomUsrList.size();
						}
						for (int i = 0; i < size; i++) {
							if(size-1 == i){
								groupHeadUrl+=roomUsrList.get(i).headSmall;
							}else{
								groupHeadUrl+=roomUsrList.get(i).headSmall+",";
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



					Login user = new Login();
					user.uid = room.groupId;
					user.nickname = room.groupName;
					user.headSmall = groupHeadUrl;
					user.mIsRoom = 300;
					//user.headSmall = mSessionList.get(position).heading;
					Intent intent = new Intent(mContext, ChatMainActivity.class);
					intent.putExtra("data", user);

					startActivity(intent);

					AddPersonActivity.this.finish();

				}
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
	 * 将选择中的用户添加到底部控件中
	 * @param login 选择的用户
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
		imageView.setImageResource(R.drawable.contact_default_header);
		LinearLayout layout = new LinearLayout(mContext);
		layout.addView(imageView);
		mImageLoader.getBitmap(mContext, imageView, null, login.headSmall, 0, false, true);
		mUserLayout.addView(layout);
		mUserLayout.invalidate();
		mScrollView.smoothScrollTo(mUserLayout.getMeasuredWidth(), 0);
	}


	/**
	 * 处理中的用户
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		if(arg2 < mList.size()){

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

	/*
	 * 更新listview 数据
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

	/*
	 * 刷新 listview 数据
	 */
	private void notifyChanged(boolean isFirst){
		if(isFirst){
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

	/*
	 * 获取用户列表
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
												if(mGroupList.get(i).mUserList != null && mGroupList.get(i).mUserList.size()>0){
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
			if(mIsSignChat == 1){
				if(mSelectedUser!=null && mSelectedUser.size()>0){
					mSelectedUser.add(mOldLogin);
					createRoom(mSelectedUser);
				}
			}else{
				Intent intent = new Intent();
				intent.putExtra("userlist", (Serializable)mSelectedUser);
				setResult(RESULT_OK, intent);
				AddPersonActivity.this.finish();
			}

			break;

		default:
			break;
		}
	}


	/*
	 * 创建会议
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
	 * 为ListView填充数据
	 * @param date
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




}
