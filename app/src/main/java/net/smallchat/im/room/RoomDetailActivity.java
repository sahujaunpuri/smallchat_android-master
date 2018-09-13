package net.smallchat.im.room;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.ChatDetailEntity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.Room;
import net.smallchat.im.R;
import net.smallchat.im.contact.UserInfoActivity;
import net.smallchat.im.adapter.ChatPersonAdapter;
import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.widget.MyGridView;

/*
 * 我的群详情
 */
public class RoomDetailActivity extends BaseActivity implements OnItemClickListener, OnItemLongClickListener{

	public static final String DESTORY_ACTION = "net.smallchat.im.intent.action.DESTORY_ACTION";
	private LinearLayout mRoomSetingLayout;
	private MyGridView mGridView;
	private TextView mClearTextView;
	private RelativeLayout  mExitLayout;

	private Room mRoom;
	private String mGroupUrl;
	private ChatPersonAdapter mAdapter;
	private List<ChatDetailEntity> mList = new ArrayList<ChatDetailEntity>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_chat_detail_page);
		mContext = this;
		mRoom = (Room) getIntent().getSerializableExtra("room");
		mGroupUrl = getIntent().getStringExtra("groupurl");
		initCompent();
		initRegister();
	}

	private void initRegister(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(DESTORY_ACTION);
		registerReceiver(mReceiver, filter);
	}


	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent != null){
				String action = intent.getAction();
				if(action.equals(DESTORY_ACTION)){
					RoomDetailActivity.this.finish();
				}
			}
		}

	};

	private void initCompent(){
		setTitleContent(R.drawable.back_btn,0,R.string.detail_material);
		mLeftBtn.setOnClickListener(this);
		mRoomSetingLayout = (LinearLayout)findViewById(R.id.group_setting_layout);
		mRoomSetingLayout.setVisibility(View.GONE);

		mGridView = (MyGridView) findViewById(R.id.gridview);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);

		mExitLayout = (RelativeLayout) findViewById(R.id.exit_chat_layout);
		mExitLayout.setOnClickListener(this);
		mExitLayout.setVisibility(View.VISIBLE);
		mClearTextView = (TextView)findViewById(R.id.clear);
		mClearTextView.setText(mContext.getResources().getString(R.string.group_chat_entry));
		showItem();
	}

	private void showItem(){
		if(mRoom!=null && mRoom.mUserList != null){
			for (int i = 0; i < mRoom.mUserList.size(); i++) {
				mList.add(new ChatDetailEntity(mRoom.mUserList.get(i), 0));
			}

			mAdapter = new ChatPersonAdapter(mContext, mList);
			mGridView.setAdapter(mAdapter);
		}
	}


	@Override
	public void onClick(View v) {
		super.onClick(v);

		switch (v.getId()) {
		case R.id.left_btn:
			RoomDetailActivity.this.finish();
			break;
		case R.id.exit_chat_layout:
			Login user = new Login();
			user.uid = mRoom.groupId;
			user.nickname = mRoom.groupName;
			user.headSmall = mGroupUrl;
			user.mIsRoom = 300;
			Intent intent = new Intent(mContext, ChatMainActivity.class);
			intent.putExtra("data", user);
			mContext.startActivity(intent);
			RoomDetailActivity.this.finish();
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(arg2 < mList.size()){
			Intent intent = new Intent(mContext, UserInfoActivity.class);
			intent.putExtra("uid", mList.get(arg2).mLogin.uid);
			intent.putExtra("type",2);

			if(mList.get(arg2).mLogin.uid.equals(IMCommon.getUserId(mContext))){
				intent.putExtra("isLogin",1);
			}
			startActivity(intent);

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mReceiver!=null){
			unregisterReceiver(mReceiver);
		}
	}




}
